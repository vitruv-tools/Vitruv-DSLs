package tools.vitruv.dsls.reactions.runtime.locking;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;

/**
 * Computes {@link AccessSummary access summaries} of generated reactions from their bytecode, at
 * runtime. A reaction is analyzed from its {@code executeReaction} method through everything it
 * calls: the generated routines (also through the routines facades and the {@code new Routine(..)
 * .execute()} indirection), Xbase lambdas and helper classes, as long as their bytecode is
 * available. Calls into EMF are mapped to structural features, calls into the reactions runtime to
 * correspondence accesses; JDK and library calls are treated as not touching model data (except
 * mutations of collections returned by EMF getters).
 *
 * <p>Per method, only the local effects and the callees are cached; the summary of a reaction is
 * the union over all reachable methods, so recursion needs no fixpoint iteration.
 */
final class BytecodeAccessAnalyzer {
  private static final String EXECUTE_REACTION = "executeReaction";
  private static final String MATCH_TRIGGER = "isCurrentChangeMatchingTrigger";
  private static final String RUNTIME = "tools/vitruv/dsls/reactions/runtime/";

  private static final List<String> LIBRARY_PREFIXES = List.of("java/", "javax/", "jdk/", "sun/",
      "com/sun/", "org/eclipse/xtext/", "org/eclipse/xtend/", "org/eclipse/xtend2/",
      "com/google/", "org/apache/", "org/slf4j/", "lombok/", "org/eclipse/core/",
      "org/eclipse/equinox/", "org/osgi/", "org/objectweb/");
  private static final Set<String> COLLECTION_MUTATORS = Set.of("add", "addAll", "remove",
      "removeAll", "removeIf", "retainAll", "clear", "set", "move", "replaceAll", "sort",
      "addUnique", "setUnique", "basicAdd", "basicRemove");
  private static final Set<String> STATIC_COLLECTION_MUTATORS = Set.of("operator_add",
      "operator_remove", "addAll", "removeAll", "removeIf", "retainAll", "sort", "reverse", "swap",
      "fill", "shuffle", "rotate");
  private static final Set<String> STATIC_COLLECTION_OWNERS = Set.of(
      "org/eclipse/xtext/xbase/lib/CollectionExtensions", "com/google/common/collect/Iterables",
      "com/google/common/collect/Iterators", "java/util/Collections",
      "org/eclipse/emf/ecore/util/EcoreUtil");
  private static final List<String> MUTATING_OPERATION_PREFIXES = List.of("create", "set", "add",
      "remove", "apply", "unapply", "destroy", "delete", "unset", "define", "insert", "move",
      "clear");

  record MethodRef(String owner, String name, String desc) { }

  private record MethodInfo(AccessSummary local, Set<MethodRef> callees) { }

  private final EmfModelIndex index;
  private final ClassLoader classLoader;
  /** the method being analyzed, to resolve parameter types */
  private final ThreadLocal<MethodNode> currentMethod = new ThreadLocal<>();
  private final Map<MethodRef, MethodInfo> methods = new ConcurrentHashMap<>();
  private final Map<String, Optional<ClassNode>> classNodes = new ConcurrentHashMap<>();
  private final Map<String, Optional<Class<?>>> loadedClasses = new ConcurrentHashMap<>();

  BytecodeAccessAnalyzer(EmfModelIndex index, ClassLoader classLoader) {
    this.index = index;
    this.classLoader = classLoader;
  }

  // ------------------------------------------------------------------ reactions

  /** The accesses of everything the reaction may execute (without its trigger check). */
  AccessSummary analyzeReaction(Class<?> reactionClass) {
    String owner = Type.getInternalName(reactionClass);
    Set<MethodRef> roots = new HashSet<>();
    classNode(owner).ifPresent(node -> node.methods.stream()
        .filter(m -> m.name.equals(EXECUTE_REACTION) || m.name.equals("userDefinedPrecondition"))
        .forEach(m -> roots.add(new MethodRef(owner, m.name, m.desc))));
    AccessSummary result = new AccessSummary();
    if (roots.isEmpty()) {
      result.opaque(owner + "." + EXECUTE_REACTION);
      return result;
    }
    Set<MethodRef> visited = new HashSet<>();
    ArrayDeque<MethodRef> todo = new ArrayDeque<>(roots);
    while (!todo.isEmpty()) {
      MethodRef next = todo.pop();
      if (!visited.add(next)) {
        continue;
      }
      MethodInfo info = methods.computeIfAbsent(next, this::analyzeMethod);
      result.addAll(info.local());
      todo.addAll(info.callees());
    }
    return result;
  }

  /** The trigger the reaction checks, from its {@code isCurrentChangeMatchingTrigger} method. */
  ReactionTrigger analyzeTrigger(Class<?> reactionClass) {
    String owner = Type.getInternalName(reactionClass);
    Optional<MethodNode> method = classNode(owner).flatMap(node -> node.methods.stream()
        .filter(m -> m.name.equals(MATCH_TRIGGER)).findFirst());
    if (method.isEmpty()) {
      return new ReactionTrigger(EChange.class, null, null, null,
          AccessSummary.ValueSide.NONE); // anychange
    }
    MethodNode node = method.get();
    Frame<SourceValue>[] frames = frames(owner, node);
    Class<?> changeType = null;
    EClass affectedClass = null;
    String featureName = null;
    EClass valueClass = null;
    AccessSummary.ValueSide valueSide = AccessSummary.ValueSide.NONE;
    InsnList insns = node.instructions;
    for (int i = 0; i < insns.size(); i++) {
      AbstractInsnNode insn = insns.get(i);
      if (insn instanceof TypeInsnNode typeInsn && insn.getOpcode() == Opcodes.INSTANCEOF) {
        if (changeType == null) {
          changeType = loadClass(typeInsn.desc).orElse(EChange.class);
          continue;
        }
        if (frames != null && frames[i] != null) {
          SourceValue operand = frames[i].getStack(frames[i].getStackSize() - 1);
          boolean onAffectedElement = producers(operand, frames, insns).stream()
              .anyMatch(p -> p instanceof MethodInsnNode m && m.name.equals("getAffectedElement"));
          if (onAffectedElement) {
            affectedClass = loadClass(typeInsn.desc).map(index::getEClass).orElse(null);
          }
          for (AbstractInsnNode p : producers(operand, frames, insns)) {
            if (p instanceof MethodInsnNode m
                && (m.name.equals("getNewValue") || m.name.equals("getOldValue"))) {
              EClass checked = loadClass(typeInsn.desc).map(index::getEClass).orElse(null);
              if (checked != null) {
                valueClass = checked;
                valueSide = m.name.equals("getNewValue") ? AccessSummary.ValueSide.NEW
                    : AccessSummary.ValueSide.OLD;
              }
            }
          }
        }
      } else if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String name
          && insn.getNext() instanceof MethodInsnNode m && m.name.equals("equals")) {
        featureName = name;
      }
    }
    return new ReactionTrigger(changeType == null ? EChange.class : changeType, affectedClass,
        featureName, valueClass, valueSide);
  }

  // ------------------------------------------------------------------ methods

  private MethodInfo analyzeMethod(MethodRef ref) {
    AccessSummary local = new AccessSummary();
    Set<MethodRef> callees = new HashSet<>();
    MethodNode method = null;
    String owner = ref.owner();
    // resolve along the superclass chain (the static owner may inherit the method)
    while (owner != null) {
      Optional<ClassNode> node = classNode(owner);
      if (node.isEmpty()) {
        local.opaque("<no bytecode> " + ref.owner() + "." + ref.name());
        return new MethodInfo(local, callees);
      }
      String currentOwner = owner;
      method = node.get().methods.stream()
          .filter(m -> m.name.equals(ref.name()) && m.desc.equals(ref.desc())).findFirst()
          .orElse(null);
      if (method != null) {
        if (!currentOwner.equals(ref.owner())) {
          callees.add(new MethodRef(currentOwner, ref.name(), ref.desc()));
          return new MethodInfo(local, callees);
        }
        break;
      }
      owner = node.get().superName;
      if (owner != null && (isLibrary(owner) || owner.startsWith(RUNTIME))) {
        return new MethodInfo(local, callees); // inherited from a library class
      }
    }
    if (method == null) {
      local.opaque("<unresolved> " + ref.owner() + "." + ref.name());
      return new MethodInfo(local, callees);
    }
    if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
      local.opaque("<abstract> " + ref.owner() + "." + ref.name());
      return new MethodInfo(local, callees);
    }
    Frame<SourceValue>[] frames = frames(ref.owner(), method);
    InsnList insns = method.instructions;
    currentMethod.set(method);
    try {
      analyzeInstructions(frames, insns, local, callees);
    } finally {
      currentMethod.remove();
    }
    return new MethodInfo(local, callees);
  }

  private void analyzeInstructions(Frame<SourceValue>[] frames, InsnList insns,
      AccessSummary local, Set<MethodRef> callees) {
    for (int i = 0; i < insns.size(); i++) {
      AbstractInsnNode insn = insns.get(i);
      if (insn instanceof MethodInsnNode call) {
        analyzeCall(call, frames == null ? null : frames[i], frames, insns, local, callees);
      } else if (insn instanceof TypeInsnNode typeInsn && insn.getOpcode() == Opcodes.NEW) {
        // generated code runs routines as `new XRoutine(..).execute()`, and AbstractRoutine.execute
        // dispatches to the abstract executeRoutine
        if (isSubclassOf(typeInsn.desc, AbstractRoutine.class)) {
          callees.add(new MethodRef(typeInsn.desc, "executeRoutine", "()Z"));
        }
      } else if (insn instanceof InvokeDynamicInsnNode indy) {
        for (Object argument : indy.bsmArgs) {
          if (argument instanceof Handle handle && !isLibrary(handle.getOwner())) {
            callees.add(new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc()));
          }
        }
      }
    }
  }

  private void analyzeCall(MethodInsnNode call, Frame<SourceValue> frame,
      Frame<SourceValue>[] frames, InsnList insns, AccessSummary local, Set<MethodRef> callees) {
    int argumentCount = Type.getArgumentTypes(call.desc).length;
    // generated match/update blocks call the runtime API with their own (sub)class as owner
    String owner = isLibrary(call.owner) ? call.owner
        : declaringOwner(call.owner, call.name, call.desc);

    if (owner.startsWith(RUNTIME)) {
      analyzeRuntimeCall(call, frame, frames, insns, argumentCount, local);
      return;
    }
    if (owner.equals("org/eclipse/emf/ecore/util/EcoreUtil")) {
      switch (call.name) {
        case "delete", "remove", "replace" -> {
          EClass deleted = frame == null ? null
              : staticEClass(argument(frame, argumentCount, 0), frames, insns);
          if (deleted != null) {
            local.deletedClasses.add(deleted);
          } else {
            local.unknownDelete = true;
          }
        }
        default -> {
          if (STATIC_COLLECTION_MUTATORS.contains(call.name)) {
            markMutatedCollection(call.name, frame, argumentCount, 0, frames, insns, local);
          } else {
            local.unknownRead = true; // copy, equals, getAllContents, ...: anything reachable
          }
        }
      }
      return;
    }
    if (owner.equals("org/eclipse/emf/ecore/EObject")
        || owner.equals("org/eclipse/emf/ecore/InternalEObject")) {
      analyzeReflectiveCall(call, frame, frames, insns, argumentCount, local);
      return;
    }
    if (owner.startsWith("org/eclipse/emf/ecore/resource/")) {
      if (call.name.equals("getContents") || call.name.equals("createResource")
          || call.name.equals("delete") || call.name.equals("save")) {
        local.persists = true;
      }
      return;
    }
    if (owner.startsWith("tools/vitruv/change/correspondence/")) {
      if (call.name.startsWith("add") || call.name.startsWith("remove")) {
        local.correspondenceWrites.add("*");
      } else {
        local.correspondenceReads.add("*");
      }
      return;
    }
    if (owner.startsWith("tools/vitruv/")) {
      return; // other framework code (logging, observers, change descriptions)
    }
    if (owner.startsWith("org/eclipse/emf/common/") || owner.startsWith("java/util/")) {
      if (call.getOpcode() != Opcodes.INVOKESTATIC && COLLECTION_MUTATORS.contains(call.name)) {
        markMutatedCollection(call.name, frame, argumentCount, -1, frames, insns, local);
      }
      return;
    }
    if (STATIC_COLLECTION_OWNERS.contains(owner)) {
      if (STATIC_COLLECTION_MUTATORS.contains(call.name)) {
        markMutatedCollection(call.name, frame, argumentCount, 0, frames, insns, local);
      }
      return;
    }
    Optional<Class<?>> ownerClass = isLibrary(owner) ? Optional.empty() : loadClass(owner);
    EClass eClass = ownerClass.map(index::getEClass).orElse(null);
    if (eClass != null) {
      analyzeModelCall(call, eClass, argumentCount, frame, frames, insns, local);
      return;
    }
    if (ownerClass.isPresent() && ownerClass.get().isInterface()) {
      List<EClass> implementing = index.implementing(ownerClass.get());
      if (!implementing.isEmpty()) {
        // mixin interface: the accessor belongs to the feature of each implementing class
        boolean resolved = false;
        for (EClass implementor : implementing) {
          if (EmfModelIndex.findAccessor(implementor, call.name, argumentCount) != null) {
            analyzeModelCall(call, implementor, argumentCount, frame, frames, insns, local);
            resolved = true;
          }
        }
        if (!resolved) {
          implementing.forEach(implementor -> analyzeModelCall(call, implementor,
              argumentCount, frame, frames, insns, local));
        }
        return;
      }
    }
    if (ownerClass.isPresent() && EFactory.class.isAssignableFrom(ownerClass.get())) {
      if (call.name.startsWith("create")) {
        loadClass(Type.getReturnType(call.desc).getInternalName()).map(index::getEClass)
            .ifPresent(local.createdClasses::add);
      }
      return;
    }
    if (owner.startsWith("org/eclipse/emf/ecore/")) {
      return; // metamodel access (EClass, EPackage, ...)
    }
    if (isLibrary(owner)) {
      return;
    }
    if (call.getOpcode() == Opcodes.INVOKEINTERFACE
        && ownerClass.map(c -> c.isInterface()).orElse(true)) {
      // interface without known implementation: only its default method (if any) is visible
      boolean hasBody = classNode(owner).map(node -> node.methods.stream().anyMatch(m ->
          m.name.equals(call.name) && m.desc.equals(call.desc)
              && (m.access & Opcodes.ACC_ABSTRACT) == 0)).orElse(false);
      if (!hasBody) {
        local.opaque("<interface> " + owner + "." + call.name);
        return;
      }
    }
    callees.add(new MethodRef(owner, call.name, call.desc));
  }

  private void analyzeRuntimeCall(MethodInsnNode call, Frame<SourceValue> frame,
      Frame<SourceValue>[] frames, InsnList insns, int argumentCount, AccessSummary local) {
    switch (call.name) {
      case "getCorrespondingElement", "getCorrespondingElements", "hasCorrespondingElements" -> {
        String tag = argumentCount > 3 ? stringConstant(frame, argumentCount, 3, frames, insns)
            : "*";
        EClass source = argumentCount > 0 && frame != null
            ? staticEClass(argument(frame, argumentCount, 0), frames, insns) : null;
        EClass target = argumentCount > 1 && frame != null
            ? classLiteral(argument(frame, argumentCount, 1), frames, insns) : null;
        local.correspondenceReads.add(correspondenceKey(tag, source, target));
      }
      case "addCorrespondenceBetween", "removeCorrespondenceBetween" -> {
        String tag = argumentCount > 2 ? stringConstant(frame, argumentCount, 2, frames, insns)
            : "*";
        EClass first = frame == null ? null
            : staticEClass(argument(frame, argumentCount, 0), frames, insns);
        EClass second = frame == null ? null
            : staticEClass(argument(frame, argumentCount, 1), frames, insns);
        local.correspondenceWrites.add(correspondenceKey(tag, first, second));
      }
      case "removeObject" -> {
        EClass deleted = frame == null ? null
            : staticEClass(argument(frame, argumentCount, 0), frames, insns);
        if (deleted != null) {
          local.deletedClasses.add(deleted);
        } else {
          local.unknownDelete = true;
        }
        local.correspondenceWrites.add(correspondenceKey("*", deleted, null));
      }
      case "persistProjectRelative" -> local.persists = true;
      case "userInteractor" -> local.interactive = true;
      default -> {
        // execute, call hierarchy, logging, change annotations: no model access
      }
    }
  }

  private void analyzeReflectiveCall(MethodInsnNode call, Frame<SourceValue> frame,
      Frame<SourceValue>[] frames, InsnList insns, int argumentCount, AccessSummary local) {
    EClass receiver = frame == null ? null : staticEClass(receiver(frame, argumentCount), frames,
        insns);
    switch (call.name) {
      case "eSet", "eUnset" -> {
        if (receiver != null) {
          local.writtenClasses.add(receiver);
        } else {
          local.unknownWrite = true;
          local.opaqueCalls.add("<reflective> EObject." + call.name);
        }
      }
      case "eGet", "eIsSet", "eContents", "eAllContents", "eCrossReferences" -> {
        if (receiver != null && !call.name.equals("eAllContents")) {
          local.readClasses.add(receiver);
        } else {
          local.unknownRead = true;
        }
      }
      default -> {
        // eClass, eContainer, eResource, eIsProxy: navigation, read of containment pointers
      }
    }
  }

  private void analyzeModelCall(MethodInsnNode call, EClass eClass, int argumentCount,
      Frame<SourceValue> frame, Frame<SourceValue>[] frames, InsnList insns,
      AccessSummary local) {
    if (eClass.getEPackage() == EcorePackage.eINSTANCE) {
      return; // metamodel access, e.g. change.getAffectedFeature().getName()
    }
    EmfModelIndex.Accessor accessor = EmfModelIndex.findAccessor(eClass, call.name, argumentCount);
    if (accessor != null) {
      EStructuralFeature feature = accessor.feature();
      switch (accessor.kind()) {
        case GET, IS_SET -> {
          local.readFeatures.add(feature);
          if (feature.isDerived()) {
            local.readClasses.add(feature.getEContainingClass());
          }
        }
        case SET, UNSET -> {
          // the owner of the call instruction is the static type of the receiver
          EClass value = accessor.kind() == EmfModelIndex.AccessorKind.SET && frame != null
              && feature instanceof org.eclipse.emf.ecore.EReference
              ? staticEClass(argument(frame, argumentCount, 0), frames, insns) : null;
          local.write(feature, eClass, value, accessor.kind() == EmfModelIndex.AccessorKind.SET
              ? AccessSummary.ValueSide.NEW : AccessSummary.ValueSide.NONE);
          if (feature.isDerived()) {
            local.writtenClasses.add(feature.getEContainingClass());
          }
        }
        default -> throw new IllegalStateException();
      }
      return;
    }
    // EOperation (or other generated method): any feature of the object may be involved
    if (MUTATING_OPERATION_PREFIXES.stream().anyMatch(call.name::startsWith)) {
      local.writtenClasses.add(eClass);
    } else {
      local.readClasses.add(eClass);
    }
  }

  /**
   * A collection is mutated (receiver for {@code argumentIndex} -1, else that argument): if it was
   * returned by the getter of a many-valued EMF feature, that feature is written.
   */
  private void markMutatedCollection(String mutationName, Frame<SourceValue> frame,
      int argumentCount, int argumentIndex, Frame<SourceValue>[] frames, InsnList insns,
      AccessSummary local) {
    if (frame == null) {
      return;
    }
    SourceValue collection = argumentIndex < 0 ? receiver(frame, argumentCount)
        : argument(frame, argumentCount, argumentIndex);
    AccessSummary.ValueSide mutationSide = mutationName.startsWith("add")
        || mutationName.equals("operator_add") ? AccessSummary.ValueSide.NEW
        : mutationName.equals("remove") || mutationName.equals("operator_remove")
            ? AccessSummary.ValueSide.OLD : AccessSummary.ValueSide.NONE;
    for (AbstractInsnNode producer : producers(collection, frames, insns)) {
      if (producer instanceof MethodInsnNode getter) {
        EClass eClass = isLibrary(getter.owner) ? null
            : loadClass(getter.owner).map(index::getEClass).orElse(null);
        if (eClass == null) {
          continue;
        }
        EmfModelIndex.Accessor accessor = EmfModelIndex.findAccessor(eClass, getter.name,
            Type.getArgumentTypes(getter.desc).length);
        if (accessor != null && accessor.feature().isMany()) {
          local.write(accessor.feature(), eClass,
              mutationValue(frame, argumentCount, argumentIndex, frames, insns),
              mutationSide);
        }
      }
    }
  }

  /**
   * The static type of the single element added or removed by a collection mutation, or
   * {@code null} (bulk operations, unknown, attributes).
   */
  private EClass mutationValue(Frame<SourceValue> frame, int argumentCount, int collectionIndex,
      Frame<SourceValue>[] frames, InsnList insns) {
    // list.add(e), list.add(i, e), list.remove(e): last argument; operator_add(list, e): second
    int elementArguments = collectionIndex < 0 ? argumentCount : argumentCount - 1;
    if (elementArguments < 1 || elementArguments > 2) {
      return null;
    }
    EClass value = staticEClass(argument(frame, argumentCount, argumentCount - 1), frames, insns);
    return value;
  }

  // ------------------------------------------------------------------ data flow helpers

  private Frame<SourceValue>[] frames(String owner, MethodNode method) {
    try {
      return new Analyzer<>(new SourceInterpreter()).analyze(owner, method);
    } catch (AnalyzerException e) {
      return null;
    }
  }

  private static SourceValue receiver(Frame<SourceValue> frame, int argumentCount) {
    return frame.getStack(frame.getStackSize() - argumentCount - 1);
  }

  private static SourceValue argument(Frame<SourceValue> frame, int argumentCount, int index) {
    return frame.getStack(frame.getStackSize() - argumentCount + index);
  }

  /** The instructions that produced a value, looking through locals, DUP and CHECKCAST. */
  private static Set<AbstractInsnNode> producers(SourceValue value, Frame<SourceValue>[] frames,
      InsnList insns) {
    Set<AbstractInsnNode> result = new HashSet<>();
    if (frames == null || value == null) {
      return result;
    }
    ArrayDeque<SourceValue> todo = new ArrayDeque<>();
    Set<AbstractInsnNode> seen = new HashSet<>();
    todo.push(value);
    while (!todo.isEmpty()) {
      for (AbstractInsnNode insn : todo.pop().insns) {
        if (!seen.add(insn)) {
          continue;
        }
        Frame<SourceValue> before = frames[insns.indexOf(insn)];
        if (before == null) {
          continue;
        }
        int opcode = insn.getOpcode();
        if (insn instanceof VarInsnNode var && opcode == Opcodes.ALOAD) {
          SourceValue stored = before.getLocal(var.var);
          if (stored.insns.isEmpty()) {
            result.add(insn); // a parameter (or this)
          } else {
            todo.push(stored);
          }
        } else if (opcode == Opcodes.ASTORE || opcode == Opcodes.DUP
            || opcode == Opcodes.CHECKCAST) {
          if (opcode == Opcodes.CHECKCAST) {
            result.add(insn);
          }
          todo.push(before.getStack(before.getStackSize() - 1));
        } else {
          result.add(insn);
        }
      }
    }
    return result;
  }

  private EClass staticEClass(SourceValue value, Frame<SourceValue>[] frames, InsnList insns) {
    for (AbstractInsnNode producer : producers(value, frames, insns)) {
      String internalName = null;
      if (producer instanceof TypeInsnNode typeInsn) {
        internalName = typeInsn.desc;
      } else if (producer instanceof MethodInsnNode m) {
        internalName = objectType(Type.getReturnType(m.desc));
        if ("org/eclipse/emf/ecore/EObject".equals(internalName) && !isLibrary(m.owner)) {
          // generated helpers such as getCorrepondenceSource(Root root) { return root; }
          internalName = classNode(declaringOwner(m.owner, m.name, m.desc)).flatMap(node ->
              node.methods.stream().filter(x -> x.name.equals(m.name) && x.desc.equals(m.desc))
                  .findFirst()).map(BytecodeAccessAnalyzer::returnedParameterType)
              .map(BytecodeAccessAnalyzer::objectType).orElse(internalName);
        }
      } else if (producer instanceof FieldInsnNode field) {
        internalName = objectType(Type.getType(field.desc));
      } else if (producer instanceof VarInsnNode var) {
        internalName = objectType(parameterType(var.var));
      }
      if (internalName != null && !internalName.equals("org/eclipse/emf/ecore/EObject")) {
        EClass eClass = loadClass(internalName).map(index::getEClass).orElse(null);
        if (eClass != null) {
          return eClass;
        }
      }
    }
    return null;
  }

  private static String objectType(Type type) {
    return type != null && type.getSort() == Type.OBJECT ? type.getInternalName() : null;
  }

  /** The declared type of the parameter in local variable slot {@code slot}, or {@code null}. */
  private Type parameterType(int slot) {
    MethodNode method = currentMethod.get();
    if (method == null) {
      return null;
    }
    int current = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
    for (Type argument : Type.getArgumentTypes(method.desc)) {
      if (current == slot) {
        return argument;
      }
      current += argument.getSize();
    }
    return null;
  }

  /** The EClass of a class literal ({@code Root.class}) passed as value. */
  private EClass classLiteral(SourceValue value, Frame<SourceValue>[] frames, InsnList insns) {
    for (AbstractInsnNode producer : producers(value, frames, insns)) {
      if (producer instanceof LdcInsnNode ldc && ldc.cst instanceof Type type) {
        return loadClass(type.getInternalName()).map(index::getEClass).orElse(null);
      }
    }
    return null;
  }

  private static String correspondenceKey(String tag, EClass first, EClass second) {
    return tag + "|" + (first == null ? "*" : AccessSummary.classKey(first)) + "|"
        + (second == null ? "*" : AccessSummary.classKey(second));
  }

  private String stringConstant(Frame<SourceValue> frame, int argumentCount, int index,
      Frame<SourceValue>[] frames, InsnList insns) {
    if (frame == null) {
      return "*";
    }
    Set<AbstractInsnNode> producers = producers(argument(frame, argumentCount, index), frames,
        insns);
    if (producers.size() != 1) {
      return "*";
    }
    AbstractInsnNode producer = producers.iterator().next();
    if (producer instanceof LdcInsnNode ldc && ldc.cst instanceof String tag) {
      return tag;
    }
    if (producer instanceof MethodInsnNode call && !isLibrary(call.owner)) {
      // Xbase compiles tag expressions into small methods returning a constant
      return classNode(declaringOwner(call.owner, call.name, call.desc)).flatMap(node ->
          node.methods.stream().filter(m -> m.name.equals(call.name) && m.desc.equals(call.desc))
              .findFirst()).map(BytecodeAccessAnalyzer::returnedConstant).orElse("*");
    }
    return "*"; // null (no tag: any tag) or computed
  }

  /** The declared type of the parameter a method just returns ({@code return param;}), or null. */
  private static Type returnedParameterType(MethodNode method) {
    Integer slot = null;
    for (AbstractInsnNode insn : method.instructions) {
      if (insn.getOpcode() < 0) {
        continue;
      }
      if (insn instanceof VarInsnNode var && insn.getOpcode() == Opcodes.ALOAD && slot == null) {
        slot = var.var;
      } else if (insn.getOpcode() != Opcodes.ARETURN) {
        return null;
      }
    }
    if (slot == null) {
      return null;
    }
    int current = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
    for (Type argument : Type.getArgumentTypes(method.desc)) {
      if (current == slot) {
        return argument;
      }
      current += argument.getSize();
    }
    return null;
  }

  /** The string a method always returns, if its body is just {@code return "constant"}. */
  private static String returnedConstant(MethodNode method) {
    String constant = null;
    for (AbstractInsnNode insn : method.instructions) {
      if (insn.getOpcode() < 0) {
        continue; // labels, line numbers, frames
      }
      if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String value && constant == null) {
        constant = value;
      } else if (insn.getOpcode() != Opcodes.ARETURN) {
        return "*";
      }
    }
    return constant == null ? "*" : constant;
  }

  // ------------------------------------------------------------------ class access

  /** The class along the superclass chain of {@code owner} that declares the method. */
  private String declaringOwner(String owner, String name, String desc) {
    String current = owner;
    while (current != null && !isLibrary(current)) {
      Optional<ClassNode> node = classNode(current);
      if (node.isEmpty()) {
        return owner;
      }
      if (node.get().methods.stream().anyMatch(m -> m.name.equals(name) && m.desc.equals(desc))) {
        return current;
      }
      current = node.get().superName;
    }
    return owner; // not declared along the class chain (e.g. inherited from an interface)
  }

  private static boolean isLibrary(String internalName) {
    return LIBRARY_PREFIXES.stream().anyMatch(internalName::startsWith);
  }

  private Optional<ClassNode> classNode(String internalName) {
    return classNodes.computeIfAbsent(internalName, name -> {
      try (InputStream in = classLoader.getResourceAsStream(name + ".class")) {
        if (in == null) {
          return Optional.empty();
        }
        ClassNode node = new ClassNode();
        new ClassReader(in).accept(node, ClassReader.SKIP_FRAMES);
        return Optional.of(node);
      } catch (IOException e) {
        return Optional.empty();
      }
    });
  }

  private Optional<Class<?>> loadClass(String internalName) {
    return loadedClasses.computeIfAbsent(internalName, name -> {
      try {
        return Optional.of(Class.forName(name.replace('/', '.'), false, classLoader));
      } catch (ClassNotFoundException | LinkageError e) {
        return Optional.empty();
      }
    });
  }

  private boolean isSubclassOf(String internalName, Class<?> superClass) {
    return !isLibrary(internalName)
        && loadClass(internalName).map(superClass::isAssignableFrom).orElse(false);
  }
}
