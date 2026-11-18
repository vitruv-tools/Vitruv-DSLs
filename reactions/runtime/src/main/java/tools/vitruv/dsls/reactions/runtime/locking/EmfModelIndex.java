package tools.vitruv.dsls.reactions.runtime.locking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Maps generated EMF classes back to their {@link EClass} and accessor methods back to their
 * {@link EStructuralFeature}, using the packages in the global {@link EPackage.Registry}. Built
 * lazily on first use; registering new packages later requires {@link #refresh()}.
 */
final class EmfModelIndex {
  private Map<Class<?>, EClass> classesByInstanceClass;
  private Map<EClass, Set<EClass>> subclasses;
  private final Map<Class<?>, List<EClass>> implementors = new HashMap<>();

  /** Accessor kinds of generated EMF methods. */
  enum AccessorKind { GET, SET, UNSET, IS_SET }

  record Accessor(EStructuralFeature feature, AccessorKind kind) { }

  synchronized void refresh() {
    classesByInstanceClass = null;
    subclasses = null;
    implementors.clear();
  }

  private synchronized void ensureIndexed() {
    if (classesByInstanceClass != null) {
      return;
    }
    classesByInstanceClass = new HashMap<>();
    subclasses = new HashMap<>();
    Set<EPackage> visited = new HashSet<>();
    for (String nsUri : new ArrayList<>(EPackage.Registry.INSTANCE.keySet())) {
      try {
        index(EPackage.Registry.INSTANCE.getEPackage(nsUri), visited);
      } catch (RuntimeException e) {
        // a broken package descriptor must not break the analysis of the others
      }
    }
  }

  private void index(EPackage ePackage, Set<EPackage> visited) {
    if (ePackage == null || !visited.add(ePackage)) {
      return;
    }
    for (EClassifier classifier : ePackage.getEClassifiers()) {
      if (classifier instanceof EClass eClass) {
        Class<?> instanceClass = eClass.getInstanceClass();
        if (instanceClass != null) {
          classesByInstanceClass.putIfAbsent(instanceClass, eClass);
        }
        for (EClass superType : eClass.getEAllSuperTypes()) {
          subclasses.computeIfAbsent(superType, s -> new HashSet<>()).add(eClass);
        }
      }
    }
    ePackage.getESubpackages().forEach(sub -> index(sub, visited));
  }

  /** The EClass of a generated interface or implementation class, or {@code null}. */
  synchronized EClass getEClass(Class<?> javaClass) {
    if (javaClass == null || !EObject.class.isAssignableFrom(javaClass)) {
      return null;
    }
    ensureIndexed();
    EClass direct = classesByInstanceClass.get(javaClass);
    if (direct != null) {
      return direct;
    }
    // implementation class XImpl: its EClass is the one of the generated interface X it implements
    for (Class<?> implemented : javaClass.getInterfaces()) {
      EClass viaInterface = classesByInstanceClass.get(implemented);
      if (viaInterface != null
          && javaClass.getSimpleName().equals(implemented.getSimpleName() + "Impl")) {
        return viaInterface;
      }
    }
    return null;
  }

  /**
   * The EClasses whose instance class implements {@code javaInterface}, for generated mixin
   * interfaces that have no EClass of their own (e.g. UML's {@code OperationOwner}).
   */
  synchronized List<EClass> implementing(Class<?> javaInterface) {
    ensureIndexed();
    // mixins like UML's OperationOwner do not even extend EObject
    if (javaInterface == null || !javaInterface.isInterface()) {
      return List.of();
    }
    return implementors.computeIfAbsent(javaInterface, i -> {
      List<EClass> result = new ArrayList<>();
      classesByInstanceClass.forEach((instanceClass, eClass) -> {
        if (i.isAssignableFrom(instanceClass)) {
          result.add(eClass);
        }
      });
      return result;
    });
  }

  /** The EClasses of the given class and all its (known) subclasses. */
  synchronized List<EClass> withSubclasses(EClass eClass) {
    ensureIndexed();
    List<EClass> result = new ArrayList<>();
    result.add(eClass);
    result.addAll(subclasses.getOrDefault(eClass, Set.of()));
    return result;
  }

  /** Whether an object can be an instance of both classes. */
  synchronized boolean overlap(EClass a, EClass b) {
    if (a == null || b == null || a == b || a.isSuperTypeOf(b) || b.isSuperTypeOf(a)) {
      return true;
    }
    ensureIndexed();
    Set<EClass> subsOfA = subclasses.getOrDefault(a, Set.of());
    for (EClass sub : subclasses.getOrDefault(b, Set.of())) {
      if (subsOfA.contains(sub)) {
        return true;
      }
    }
    return false;
  }

  /** The feature a generated accessor method accesses, or {@code null} for other methods. */
  static Accessor findAccessor(EClass eClass, String methodName, int parameterCount) {
    for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
      String name = feature.getName();
      String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
      if (parameterCount == 0) {
        if (matches(methodName, "get" + capitalized) || matches(methodName, "is" + capitalized)) {
          return new Accessor(feature, AccessorKind.GET);
        }
        if (matches(methodName, "isSet" + capitalized)) {
          return new Accessor(feature, AccessorKind.IS_SET);
        }
        if (matches(methodName, "unset" + capitalized)) {
          return new Accessor(feature, AccessorKind.UNSET);
        }
      } else if (parameterCount == 1 && matches(methodName, "set" + capitalized)) {
        return new Accessor(feature, AccessorKind.SET);
      }
    }
    return null;
  }

  private static boolean matches(String methodName, String accessorName) {
    // EMF appends "_" to accessors that would clash with java.lang.Object methods (getClass_)
    return methodName.equals(accessorName) || methodName.equals(accessorName + "_");
  }
}
