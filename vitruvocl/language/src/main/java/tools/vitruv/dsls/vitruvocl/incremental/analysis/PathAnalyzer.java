/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.incremental.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Computes the {@link RelevancePlan} of a type-checked constraint (incremental level 3): for every
 * feature read, the {@link Origin} of its receiver, i.e. the {@code PathVar} of Cabot &amp;
 * Teniente 2009, §3.2.
 *
 * <p>Origins are followed through navigations along references, through operations whose result
 * elements are elements of their receiver ({@code select}, {@code reject}, {@code sortedBy},
 * {@code any}, {@code first}, {@code last}, {@code at}, {@code reverse}, {@code subSequence},
 * {@code excluding}, {@code asX}, {@code flatten}, {@code oclAsType}), through iterator and
 * {@code let} variables (bound to the origin of their source / initialiser) and through the body
 * of {@code collect}. Everything else — {@code allInstances()}, {@code union}, {@code including},
 * {@code iterate}, {@code if}, collection literals, operations with several possible origins —
 * yields {@link Origin#CLASS}: events on such objects are class events (whole extension), as for
 * {@code allInstances} in the paper. If the constraint uses {@code append} or {@code iterate},
 * every read is a class-event read (static types are not trusted, see {@link ReadSetAnalyzer}).
 */
public final class PathAnalyzer {

  private static final Pattern MESSAGE_SELF_ATTRIBUTE =
      Pattern.compile("\\{self\\.([a-zA-Z_]\\w*)\\}");

  private final ParseTreeProperty<Type> nodeTypes;
  private final boolean untrustedTypes;
  private final List<RelevancePlan.Read> reads = new ArrayList<>();
  private final Set<EClass> contextTypes = new LinkedHashSet<>();
  private final Set<EClass> classEventTypes = new LinkedHashSet<>();
  private final Set<EClass> correspondentTypes = new LinkedHashSet<>();
  private boolean correspondenceOnlyInIdiom = true;
  private boolean unknownExtension;
  private Type currentChainType;
  private boolean idiomChain;
  private EClass currentContext;

  private PathAnalyzer(ParseTreeProperty<Type> nodeTypes, boolean untrustedTypes) {
    this.nodeTypes = nodeTypes;
    this.untrustedTypes = untrustedTypes;
  }

  /**
   * Analyses {@code tree}. Reads of the level-1 read set that the path walk did not record
   * (safety net) become class-event reads; level-1 extension types not explained by the walk
   * (container-feature owners, the evaluator's {@code allInstances} fallback) become class-event
   * types.
   */
  public static RelevancePlan analyze(
      VitruvOCLParser.ContextDeclCSContext tree, ParseTreeProperty<Type> nodeTypes, ReadSet readSet) {
    PathAnalyzer analyzer =
        new PathAnalyzer(nodeTypes, ReadSetAnalyzer.containsUntrustedTypeProducer(tree));
    for (VitruvOCLParser.ClassifierContextCSContext ctx : tree.classifierContextCS()) {
      analyzer.visitContext(ctx);
    }
    analyzer.addSafetyNet(readSet);
    return new RelevancePlan(
        analyzer.reads,
        analyzer.contextTypes,
        analyzer.classEventTypes,
        analyzer.correspondentTypes,
        analyzer.correspondenceOnlyInIdiom,
        analyzer.unknownExtension || readSet.unknownExtension());
  }

  private void addSafetyNet(ReadSet readSet) {
    readSet
        .typedFeatureReads()
        .forEach(
            (name, classes) -> {
              for (EClass c : classes) {
                if (reads.stream().noneMatch(r -> r.feature().equals(name) && r.receiverClass() == c)) {
                  reads.add(new RelevancePlan.Read(name, c, Origin.CLASS));
                }
              }
            });
    for (String name : readSet.untypedFeatureReads()) {
      if (reads.stream().noneMatch(r -> r.feature().equals(name) && r.receiverClass() == null)) {
        reads.add(new RelevancePlan.Read(name, null, Origin.CLASS));
      }
    }
    for (EClass type : readSet.extensionTypes()) {
      if (!contextTypes.contains(type)
          && !classEventTypes.contains(type)
          && !correspondentTypes.contains(type)) {
        classEventTypes.add(type);
      }
    }
  }

  private void visitContext(VitruvOCLParser.ClassifierContextCSContext ctx) {
    currentContext = ReadSetAnalyzer.elementClass(nodeTypes.get(ctx));
    if (currentContext != null) {
      contextTypes.add(currentContext);
    } else {
      unknownExtension = true;
    }
    Scope scope = Scope.ROOT;
    for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
      visitBlock(inv, scope);
    }
    for (VitruvOCLParser.PreCSContext pre : ctx.preCS()) {
      visitBlock(pre, scope);
    }
    for (VitruvOCLParser.PostCSContext post : ctx.postCS()) {
      visitBlock(post, scope);
    }
  }

  private void visitBlock(ParserRuleContext block, Scope scope) {
    for (int i = 0; i < block.getChildCount(); i++) {
      ParseTree child = block.getChild(i);
      if (child instanceof VitruvOCLParser.MessageAnnotationContext msg) {
        Matcher m = MESSAGE_SELF_ATTRIBUTE.matcher(msg.message.getText());
        while (m.find()) {
          record(m.group(1), currentContext, Origin.SELF_ROOT);
        }
      } else {
        walk(child, scope);
      }
    }
  }

  // ------------------------------------------------------------------ expressions

  /** Records the reads in {@code node} and returns the origin of its value. */
  private Origin walk(ParseTree node, Scope scope) {
    if (node instanceof VitruvOCLParser.ExpCSContext e) {
      return walk(e.infixedExpCS(), scope);
    }
    if (node instanceof VitruvOCLParser.PrefixedExprContext e) {
      return walk(e.prefixedExpCS(), scope);
    }
    if (node instanceof VitruvOCLParser.SpecificationCSContext spec) {
      Origin last = Origin.VALUE;
      for (VitruvOCLParser.ExpCSContext e : spec.expCS()) {
        last = walk(e, scope);
      }
      return last;
    }
    if (node instanceof VitruvOCLParser.CorrespondenceContext) {
      correspondenceOnlyInIdiom = false;
      walkChildren(node, scope);
      return Origin.VALUE;
    }
    if (node instanceof VitruvOCLParser.PrimaryWithNavContext nav) {
      Origin base = walkPrimary(nav.base, scope);
      Type saved = currentChainType;
      currentChainType = nodeTypes.get(nav);
      try {
        return chain(base, nav.base, nav.navigationChainCS(), scope);
      } finally {
        currentChainType = saved;
      }
    }
    if (node instanceof VitruvOCLParser.PrefixedQualifiedContext q) {
      Type saved = currentChainType;
      currentChainType = null;
      try {
        return chain(Origin.CLASS, null, q.navigationChainCS(), scope);
      } finally {
        currentChainType = saved;
      }
    }
    walkChildren(node, scope);
    return isValueExpression(node) ? Origin.VALUE : Origin.CLASS;
  }

  private static boolean isValueExpression(ParseTree node) {
    return node instanceof VitruvOCLParser.InfixedExpCSContext
        || node instanceof VitruvOCLParser.LogicalNotContext
        || node instanceof VitruvOCLParser.UnaryMinusContext;
  }

  private void walkChildren(ParseTree node, Scope scope) {
    for (int i = 0; i < node.getChildCount(); i++) {
      walk(node.getChild(i), scope);
    }
  }

  private Origin walkPrimary(VitruvOCLParser.PrimaryExpCSContext base, Scope scope) {
    if (base instanceof VitruvOCLParser.SelfContext) {
      return Origin.SELF_ROOT;
    }
    if (base instanceof VitruvOCLParser.VariableContext v) {
      return scope.lookup(v.variableExpCS().varName.getText());
    }
    if (base instanceof VitruvOCLParser.LiteralContext) {
      return Origin.VALUE;
    }
    if (base instanceof VitruvOCLParser.NestedContext nested) {
      Origin last = Origin.VALUE;
      for (VitruvOCLParser.ExpCSContext e : nested.nestedExpCS().expCS()) {
        last = walk(e, scope);
      }
      return last;
    }
    if (base instanceof VitruvOCLParser.LetBindingContext let) {
      Scope inner = scope;
      for (VitruvOCLParser.VariableDeclarationContext decl :
          let.letExpCS().variableDeclarations().variableDeclaration()) {
        Origin init = walk(decl.varInit, inner);
        inner = inner.bind(decl.varName.getText(), init);
      }
      Origin last = Origin.VALUE;
      for (VitruvOCLParser.ExpCSContext e : let.letExpCS().expCS()) {
        last = walk(e, inner);
      }
      return last;
    }
    walkChildren(base, scope); // if, collection literals, type literals
    return Origin.CLASS;
  }

  // ------------------------------------------------------------------ navigation chains

  private Origin chain(
      Origin baseOrigin,
      VitruvOCLParser.PrimaryExpCSContext base,
      List<VitruvOCLParser.NavigationChainCSContext> steps,
      Scope scope) {
    Type type = base == null ? null : ReadSetAnalyzer.spineType(nodeTypes, base, 3);
    if (type == null && base instanceof VitruvOCLParser.SelfContext && currentContext != null) {
      type = Type.metaclassType(currentContext);
    }
    if (base == null && !steps.isEmpty()) {
      type = nodeTypes.get(steps.get(0).getParent());
    }
    Origin origin = baseOrigin;
    boolean savedIdiom = idiomChain;
    idiomChain =
        steps.size() >= 2
            && isAllInstancesStep(steps.get(0))
            && isCorrespondenceIdiomStep(steps.get(1));
    try {
      for (int i = 0; i < steps.size(); i++) {
        VitruvOCLParser.NavigationChainCSContext nav = steps.get(i);
        if (nav.AT_PRE() != null) {
          continue;
        }
        EClass receiverClass = ReadSetAnalyzer.elementClass(type);
        EClass idiomType =
            i == 1 && isAllInstancesStep(steps.get(0))
                ? receiverClassOfAllInstances(steps.get(0), base)
                : null;
        origin = step(nav, receiverClass, origin, scope, idiomType);
        type = ReadSetAnalyzer.spineType(nodeTypes, nav, 4);
      }
    } finally {
      idiomChain = savedIdiom;
    }
    return origin;
  }

  private static boolean isCorrespondenceIdiomStep(VitruvOCLParser.NavigationChainCSContext nav) {
    if (!(nav.navigationTargetCS() instanceof VitruvOCLParser.OperationNavContext op)) {
      return false;
    }
    ParserRuleContext concrete = concreteOperation(op.operationCall());
    return concrete instanceof VitruvOCLParser.SelectCorrespondenceContext
        || concrete instanceof VitruvOCLParser.ExistsCorrespondenceContext;
  }

  private static boolean isAllInstancesStep(VitruvOCLParser.NavigationChainCSContext nav) {
    return nav.navigationTargetCS() instanceof VitruvOCLParser.OperationNavContext op
        && ReadSetAnalyzer.isAllInstances(op.operationCall());
  }

  /** The type {@code Y} of a chain starting with {@code Y.allInstances()}. */
  private EClass receiverClassOfAllInstances(
      VitruvOCLParser.NavigationChainCSContext allInstancesStep,
      VitruvOCLParser.PrimaryExpCSContext base) {
    Type receiver =
        base == null
            ? nodeTypes.get(allInstancesStep.getParent())
            : ReadSetAnalyzer.spineType(nodeTypes, base, 3);
    return ReadSetAnalyzer.elementClass(receiver);
  }

  /**
   * Records the reads of one step and returns the origin of its result.
   *
   * @param idiomType {@code Y} if this step directly follows {@code Y.allInstances()}, else null
   */
  private Origin step(
      VitruvOCLParser.NavigationChainCSContext nav,
      EClass receiverClass,
      Origin receiver,
      Scope scope,
      EClass idiomType) {
    VitruvOCLParser.NavigationTargetCSContext target = nav.navigationTargetCS();
    if (target instanceof VitruvOCLParser.PropertyNavContext prop) {
      String name = prop.propertyAccess().propertyName.getText();
      record(name, receiverClass, receiver);
      EStructuralFeature feature =
          receiverClass == null ? null : receiverClass.getEStructuralFeature(name);
      if (feature instanceof EReference ref && !untrustedTypes) {
        return receiver.navigate(ref);
      }
      return feature == null || feature instanceof EReference ? Origin.CLASS : Origin.VALUE;
    }
    if (!(target instanceof VitruvOCLParser.OperationNavContext op)) {
      walkChildren(target, scope);
      return Origin.VALUE;
    }
    VitruvOCLParser.OperationCallContext call = op.operationCall();
    if (ReadSetAnalyzer.isAllInstances(call)) {
      // Paper 1, §3.2: allInstances makes every structural change of its type a class event
      if (receiverClass == null) {
        unknownExtension = true;
      } else if (!idiomChain) {
        classEventTypes.add(receiverClass); // in the idiom, Y is a correspondent type instead
      }
      if (nodeTypes.get(nav) == null && currentChainType != null) {
        EClass fallback = ReadSetAnalyzer.elementClass(currentChainType);
        if (fallback != null) {
          classEventTypes.add(fallback); // the evaluator's receiver fallback (visitAllInstancesOp)
        }
      }
      walkChildren(call, scope);
      return Origin.CLASS;
    }
    ParserRuleContext concrete = concreteOperation(call);
    if (concrete instanceof VitruvOCLParser.SelectCorrespondenceContext
        || concrete instanceof VitruvOCLParser.ExistsCorrespondenceContext) {
      walkChildren(concrete, scope);
      if (idiomType != null) {
        correspondentTypes.add(idiomType);
        return concrete instanceof VitruvOCLParser.SelectCorrespondenceContext
            ? new Origin(Origin.Kind.CORRESPONDENTS, List.of(), idiomType)
            : Origin.VALUE;
      }
      correspondenceOnlyInIdiom = false;
      return Origin.CLASS;
    }
    if (concrete instanceof VitruvOCLParser.RejectCorrespondenceContext
        || concrete instanceof VitruvOCLParser.UnknownCorrOpContext) {
      correspondenceOnlyInIdiom = false;
      walkChildren(concrete, scope);
      return Origin.CLASS;
    }

    List<String> iteratorVariables = iteratorVariables(concrete);
    Scope body = scope;
    for (String v : iteratorVariables) {
      body = body.bind(v, receiver);
    }
    Origin bodyOrigin = Origin.VALUE;
    for (int i = 0; i < concrete.getChildCount(); i++) {
      ParseTree child = concrete.getChild(i);
      if (child instanceof VitruvOCLParser.ExpCSContext arg) {
        bodyOrigin = walk(arg, body);
      } else if (child instanceof VitruvOCLParser.IterateVarSpecContext spec) {
        walk(spec.accInit, scope);
        body = body.bind(spec.iterVar.getText(), Origin.CLASS).bind(spec.accVar.getText(), Origin.CLASS);
      } else if (child instanceof ParserRuleContext other
          && !(child instanceof VitruvOCLParser.IteratorVarListContext)) {
        walkChildren(other, scope);
      }
    }
    if (concrete instanceof VitruvOCLParser.IterateOpContext) {
      // the body was walked with the accumulator unbound above; walk again with bindings
      for (int i = 0; i < concrete.getChildCount(); i++) {
        if (concrete.getChild(i) instanceof VitruvOCLParser.ExpCSContext arg) {
          walk(arg, body);
        }
      }
      return Origin.CLASS;
    }
    if (isElementPreserving(concrete)) {
      return receiver;
    }
    if (concrete instanceof VitruvOCLParser.CollectOpContext) {
      return bodyOrigin;
    }
    return isValueOperation(concrete) ? Origin.VALUE : Origin.CLASS;
  }

  private static List<String> iteratorVariables(ParserRuleContext op) {
    List<String> names = new ArrayList<>();
    for (int i = 0; i < op.getChildCount(); i++) {
      if (op.getChild(i) instanceof VitruvOCLParser.IteratorVarListContext vars) {
        for (TerminalNode id : vars.ID()) {
          names.add(id.getText());
        }
      }
    }
    return names;
  }

  private static boolean isElementPreserving(ParserRuleContext op) {
    return op instanceof VitruvOCLParser.SelectOpContext
        || op instanceof VitruvOCLParser.RejectOpContext
        || op instanceof VitruvOCLParser.SortedByOpContext
        || op instanceof VitruvOCLParser.AnyOpContext
        || op instanceof VitruvOCLParser.FirstOpContext
        || op instanceof VitruvOCLParser.LastOpContext
        || op instanceof VitruvOCLParser.AtOpContext
        || op instanceof VitruvOCLParser.ReverseOpContext
        || op instanceof VitruvOCLParser.SubSequenceOpContext
        || op instanceof VitruvOCLParser.ExcludingOpContext
        || op instanceof VitruvOCLParser.AsSetOpContext
        || op instanceof VitruvOCLParser.AsBagOpContext
        || op instanceof VitruvOCLParser.AsSequenceOpContext
        || op instanceof VitruvOCLParser.AsOrderedSetOpContext
        || op instanceof VitruvOCLParser.FlattenOpContext
        || op instanceof VitruvOCLParser.OclAsTypeOpContext;
  }

  private static boolean isValueOperation(ParserRuleContext op) {
    return op instanceof VitruvOCLParser.SizeOpContext
        || op instanceof VitruvOCLParser.CountOpContext
        || op instanceof VitruvOCLParser.IsEmptyOpContext
        || op instanceof VitruvOCLParser.NotEmptyOpContext
        || op instanceof VitruvOCLParser.IncludesOpContext
        || op instanceof VitruvOCLParser.ExcludesOpContext
        || op instanceof VitruvOCLParser.IncludesAllOpContext
        || op instanceof VitruvOCLParser.ExcludesAllOpContext
        || op instanceof VitruvOCLParser.ForAllOpContext
        || op instanceof VitruvOCLParser.ExistsOpContext
        || op instanceof VitruvOCLParser.OneOpContext
        || op instanceof VitruvOCLParser.IsUniqueOpContext
        || op instanceof VitruvOCLParser.SumOpContext
        || op instanceof VitruvOCLParser.OclIsKindOfOpContext
        || op instanceof VitruvOCLParser.OclIsTypeOfOpContext
        || op instanceof VitruvOCLParser.StringOpCSContext;
  }

  private static ParserRuleContext concreteOperation(VitruvOCLParser.OperationCallContext call) {
    if (call instanceof VitruvOCLParser.CollectionOperationContext c) {
      return c.collectionOpCS();
    }
    if (call instanceof VitruvOCLParser.IteratorOperationContext i) {
      return i.iteratorOpCS();
    }
    if (call instanceof VitruvOCLParser.StringOperationContext s) {
      return s.stringOpCS();
    }
    if (call instanceof VitruvOCLParser.TypeOperationContext t) {
      return t.typeOpCS();
    }
    return call;
  }

  private void record(String feature, EClass receiverClass, Origin origin) {
    EClass cls = untrustedTypes ? null : receiverClass;
    Origin effective = untrustedTypes ? Origin.CLASS : origin;
    if (effective.kind() == Origin.Kind.VALUE) {
      effective = Origin.CLASS; // a feature read on a "value" can only mean we lost track
    }
    reads.add(new RelevancePlan.Read(feature, cls, effective));
  }

  /** Immutable variable scope: name → origin of the objects bound to it. */
  private record Scope(Map<String, Origin> bindings) {
    static final Scope ROOT = new Scope(Map.of());

    Scope bind(String name, Origin origin) {
      Map<String, Origin> copy = new HashMap<>(bindings);
      copy.put(name, origin);
      return new Scope(copy);
    }

    Origin lookup(String name) {
      return bindings.getOrDefault(name, Origin.CLASS);
    }
  }
}
