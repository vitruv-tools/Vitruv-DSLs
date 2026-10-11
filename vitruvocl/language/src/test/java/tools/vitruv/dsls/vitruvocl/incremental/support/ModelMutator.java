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

package tools.vitruv.dsls.vitruvocl.incremental.support;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Random atomic model mutations with a fixed seed, for the incremental oracle tests: attribute
 * set/unset (single- and multi-valued), reference add/remove/replace, permutation within a list,
 * containment move, create, delete (with and without removing dangling cross references) and
 * silent correspondence edits.
 *
 * <p>Every mutation is a legal EMF edit; mutations that do not apply to the current model are
 * retried with another kind. Values are drawn mostly from values already present in the model, so
 * that constraints actually flip between satisfied and violated.
 */
public final class ModelMutator {

  /** Kinds of mutation. */
  public enum Kind {
    SET_ATTRIBUTE,
    UNSET_ATTRIBUTE,
    MANY_ATTRIBUTE,
    REFERENCE,
    PERMUTE,
    MOVE,
    CREATE,
    DELETE_KEEP_REFERENCES,
    DELETE,
    CORRESPONDENCE
  }

  private final LiveModel model;
  private final Random random;
  private final List<String> log = new ArrayList<>();

  public ModelMutator(LiveModel model, long seed) {
    this.model = model;
    this.random = new Random(seed);
  }

  /** Human-readable log of all applied mutations, for failure messages. */
  public List<String> log() {
    return log;
  }

  /** Applies one random mutation (falls back to other kinds if a kind is not applicable). */
  public String mutate() {
    Kind[] kinds = Kind.values();
    for (int attempt = 0; attempt < 40; attempt++) {
      Kind kind = kinds[random.nextInt(kinds.length)];
      String done = apply(kind);
      if (done != null) {
        log.add(done);
        return done;
      }
    }
    return "no applicable mutation";
  }

  /** Applies a mutation of the given kind, or returns {@code null} if none is applicable. */
  public String apply(Kind kind) {
    return switch (kind) {
      case SET_ATTRIBUTE -> setAttribute(false);
      case UNSET_ATTRIBUTE -> setAttribute(true);
      case MANY_ATTRIBUTE -> manyAttribute();
      case REFERENCE -> reference();
      case PERMUTE -> permute();
      case MOVE -> move();
      case CREATE -> create();
      case DELETE_KEEP_REFERENCES -> delete(false);
      case DELETE -> delete(true);
      case CORRESPONDENCE -> correspondence();
    };
  }

  // ---------------------------------------------------------------- attributes

  private String setAttribute(boolean unset) {
    List<Object[]> candidates = new ArrayList<>();
    for (EObject o : model.allObjects()) {
      for (EAttribute a : o.eClass().getEAllAttributes()) {
        if (!a.isMany() && a.isChangeable() && !a.isDerived() && supported(a.getEAttributeType())) {
          candidates.add(new Object[] {o, a});
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    Object[] pick = candidates.get(random.nextInt(candidates.size()));
    EObject o = (EObject) pick[0];
    EAttribute a = (EAttribute) pick[1];
    if (unset) {
      o.eUnset(a);
      return "unset " + label(o) + "." + a.getName();
    }
    Object value = randomValue(a);
    o.eSet(a, value);
    return "set " + label(o) + "." + a.getName() + " := " + value;
  }

  @SuppressWarnings("unchecked")
  private String manyAttribute() {
    List<Object[]> candidates = new ArrayList<>();
    for (EObject o : model.allObjects()) {
      for (EAttribute a : o.eClass().getEAllAttributes()) {
        if (a.isMany() && a.isChangeable() && !a.isDerived() && supported(a.getEAttributeType())) {
          candidates.add(new Object[] {o, a});
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    Object[] pick = candidates.get(random.nextInt(candidates.size()));
    EObject o = (EObject) pick[0];
    EAttribute a = (EAttribute) pick[1];
    EList<Object> values = (EList<Object>) o.eGet(a);
    if (!values.isEmpty() && random.nextBoolean()) {
      Object removed = values.remove(random.nextInt(values.size()));
      return "remove " + removed + " from " + label(o) + "." + a.getName();
    }
    Object value = randomValue(a);
    if (a.isUnique() && values.contains(value)) {
      return null;
    }
    values.add(value);
    return "add " + value + " to " + label(o) + "." + a.getName();
  }

  private boolean supported(EDataType type) {
    if (type instanceof EEnum e) {
      return !e.getELiterals().isEmpty();
    }
    Class<?> c = type.getInstanceClass();
    return c == String.class
        || c == int.class
        || c == Integer.class
        || c == boolean.class
        || c == Boolean.class
        || c == double.class
        || c == Double.class
        || c == float.class
        || c == Float.class
        || c == long.class
        || c == Long.class;
  }

  private Object randomValue(EAttribute a) {
    EDataType type = a.getEAttributeType();
    if (type instanceof EEnum e) {
      return e.getELiterals().get(random.nextInt(e.getELiterals().size())).getInstance();
    }
    List<Object> existing = new ArrayList<>();
    for (EObject o : model.allObjects()) {
      EStructuralFeature f = o.eClass().getEStructuralFeature(a.getName());
      if (f instanceof EAttribute other && other.getEAttributeType() == type) {
        Object v = o.eGet(other);
        if (v instanceof List<?> l) {
          existing.addAll(l);
        } else if (v != null) {
          existing.add(v);
        }
      }
    }
    Class<?> c = type.getInstanceClass();
    if (!existing.isEmpty() && random.nextInt(3) > 0) {
      Object base = existing.get(random.nextInt(existing.size()));
      return perturb(base, c);
    }
    return fresh(c);
  }

  private Object perturb(Object base, Class<?> c) {
    if (random.nextBoolean()) {
      return base;
    }
    if (base instanceof Integer i) {
      return i + (random.nextBoolean() ? 1 : -1) * (1 + random.nextInt(500));
    }
    if (base instanceof Long l) {
      return l + (random.nextBoolean() ? 1 : -1);
    }
    if (base instanceof Double d) {
      return d * (random.nextBoolean() ? 2.0 : 0.5) - random.nextInt(3);
    }
    if (base instanceof Float f) {
      return f * (random.nextBoolean() ? 2.0f : 0.5f) - random.nextInt(3);
    }
    if (base instanceof Boolean b) {
      return !b;
    }
    if (base instanceof String s) {
      return random.nextBoolean() ? s + "x" : s.isEmpty() ? "y" : s.substring(1);
    }
    return fresh(c);
  }

  private Object fresh(Class<?> c) {
    if (c == String.class) {
      String[] pool = {"", "a", "Voyager", "mut" + random.nextInt(5), "Lisa", "ACTIVE"};
      return pool[random.nextInt(pool.length)];
    }
    if (c == int.class || c == Integer.class) {
      int[] pool = {0, 1, -1, 3, 10, 100, 1000, 5000};
      return pool[random.nextInt(pool.length)];
    }
    if (c == long.class || c == Long.class) {
      return (long) random.nextInt(1000);
    }
    if (c == boolean.class || c == Boolean.class) {
      return random.nextBoolean();
    }
    if (c == double.class || c == Double.class) {
      return random.nextInt(2000) / 10.0 - 10;
    }
    if (c == float.class || c == Float.class) {
      return random.nextInt(2000) / 10.0f - 10;
    }
    return null;
  }

  // ---------------------------------------------------------------- references

  @SuppressWarnings("unchecked")
  private String reference() {
    List<Object[]> candidates = new ArrayList<>();
    for (EObject o : model.allObjects()) {
      for (EReference r : o.eClass().getEAllReferences()) {
        if (!r.isContainment() && !r.isContainer() && r.isChangeable() && !r.isDerived()) {
          candidates.add(new Object[] {o, r});
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    Object[] pick = candidates.get(random.nextInt(candidates.size()));
    EObject o = (EObject) pick[0];
    EReference r = (EReference) pick[1];
    List<EObject> targets =
        model.allObjects().stream().filter(t -> r.getEReferenceType().isSuperTypeOf(t.eClass())).toList();
    if (r.isMany()) {
      EList<EObject> values = (EList<EObject>) o.eGet(r);
      if (!values.isEmpty() && (targets.isEmpty() || random.nextBoolean())) {
        EObject removed = values.remove(random.nextInt(values.size()));
        return "unlink " + label(removed) + " from " + label(o) + "." + r.getName();
      }
      if (targets.isEmpty()) {
        return null;
      }
      EObject target = targets.get(random.nextInt(targets.size()));
      if (r.isUnique() && values.contains(target)) {
        return null;
      }
      values.add(random.nextInt(values.size() + 1), target);
      return "link " + label(target) + " to " + label(o) + "." + r.getName();
    }
    EObject target =
        targets.isEmpty() || random.nextInt(4) == 0 ? null : targets.get(random.nextInt(targets.size()));
    o.eSet(r, target);
    return "set " + label(o) + "." + r.getName() + " := " + (target == null ? "null" : label(target));
  }

  @SuppressWarnings("unchecked")
  private String permute() {
    List<Object[]> candidates = new ArrayList<>();
    for (EObject o : model.allObjects()) {
      for (EReference r : o.eClass().getEAllReferences()) {
        if (r.isMany() && r.isChangeable() && !r.isDerived() && ((List<?>) o.eGet(r)).size() > 1) {
          candidates.add(new Object[] {o, r});
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    Object[] pick = candidates.get(random.nextInt(candidates.size()));
    EObject o = (EObject) pick[0];
    EReference r = (EReference) pick[1];
    EList<EObject> values = (EList<EObject>) o.eGet(r);
    int from = random.nextInt(values.size());
    int to = random.nextInt(values.size());
    if (from == to) {
      to = (from + 1) % values.size();
    }
    values.move(to, from);
    return "permute " + label(o) + "." + r.getName() + " [" + from + "->" + to + "]";
  }

  // ---------------------------------------------------------------- containment

  @SuppressWarnings("unchecked")
  private String move() {
    List<EObject> movable = model.allObjects().stream().filter(o -> o.eContainer() != null).toList();
    if (movable.isEmpty()) {
      return null;
    }
    EObject o = movable.get(random.nextInt(movable.size()));
    List<Object[]> targets = new ArrayList<>();
    for (EObject c : model.allObjects()) {
      if (c == o || EcoreUtil.isAncestor(o, c)) {
        continue;
      }
      for (EReference r : c.eClass().getEAllContainments()) {
        if (r.isChangeable() && r.getEReferenceType().isSuperTypeOf(o.eClass())
            && (r.isMany() || c.eGet(r) == null)
            && !(c == o.eContainer() && r == o.eContainmentFeature())) {
          targets.add(new Object[] {c, r});
        }
      }
    }
    if (targets.isEmpty()) {
      return null;
    }
    Object[] pick = targets.get(random.nextInt(targets.size()));
    EObject c = (EObject) pick[0];
    EReference r = (EReference) pick[1];
    if (r.isMany()) {
      EList<EObject> list = (EList<EObject>) c.eGet(r);
      list.add(random.nextInt(list.size() + 1), o);
    } else {
      c.eSet(r, o);
    }
    return "move " + label(o) + " into " + label(c) + "." + r.getName();
  }

  @SuppressWarnings("unchecked")
  private String create() {
    List<Object[]> targets = new ArrayList<>();
    for (EObject c : model.allObjects()) {
      for (EReference r : c.eClass().getEAllContainments()) {
        if (r.isChangeable() && (r.isMany() || c.eGet(r) == null)) {
          for (EClass concrete : concreteSubclasses(r.getEReferenceType())) {
            targets.add(new Object[] {c, r, concrete});
          }
        }
      }
    }
    if (targets.isEmpty()) {
      return null;
    }
    Object[] pick = targets.get(random.nextInt(targets.size()));
    EObject c = (EObject) pick[0];
    EReference r = (EReference) pick[1];
    EObject created = EcoreUtil.create((EClass) pick[2]);
    for (EAttribute a : created.eClass().getEAllAttributes()) {
      if (!a.isMany() && a.isChangeable() && !a.isDerived() && supported(a.getEAttributeType())
          && random.nextBoolean()) {
        created.eSet(a, randomValue(a));
      }
    }
    if (r.isMany()) {
      EList<EObject> list = (EList<EObject>) c.eGet(r);
      list.add(random.nextInt(list.size() + 1), created);
    } else {
      c.eSet(r, created);
    }
    return "create " + created.eClass().getName() + " in " + label(c) + "." + r.getName();
  }

  private Set<EClass> concreteSubclasses(EClass type) {
    Set<EPackage> packages = new LinkedHashSet<>();
    packages.add(type.getEPackage());
    for (EObject o : model.allObjects()) {
      packages.add(o.eClass().getEPackage());
    }
    Set<EClass> result = new LinkedHashSet<>();
    for (EPackage p : packages) {
      for (EClassifier classifier : p.getEClassifiers()) {
        if (classifier instanceof EClass c && !c.isAbstract() && !c.isInterface() && type.isSuperTypeOf(c)) {
          result.add(c);
        }
      }
    }
    return result;
  }

  private String delete(boolean removeReferences) {
    List<EObject> deletable = model.allObjects().stream().filter(o -> o.eContainer() != null).toList();
    if (deletable.isEmpty()) {
      return null;
    }
    EObject o = deletable.get(random.nextInt(deletable.size()));
    String label = label(o);
    if (removeReferences) {
      EcoreUtil.delete(o, true);
      return "delete " + label;
    }
    EcoreUtil.remove(o);
    return "remove " + label + " (dangling references kept)";
  }

  // ---------------------------------------------------------------- correspondences

  private String correspondence() {
    FakeCorrespondenceModel corr = model.correspondences();
    if (corr.size() > 0 && random.nextBoolean()) {
      corr.removeAt(random.nextInt(corr.size()));
      return "silently remove a correspondence";
    }
    List<EObject> all = model.allObjects();
    if (all.size() < 2) {
      return null;
    }
    EObject a = all.get(random.nextInt(all.size()));
    EObject b = all.get(random.nextInt(all.size()));
    if (a == b) {
      return null;
    }
    corr.addCorrespondenceBetween(List.of(a), List.of(b), random.nextBoolean() ? null : "mut");
    return "silently add correspondence " + label(a) + " ~ " + label(b);
  }

  private static String label(EObject o) {
    EStructuralFeature name = o.eClass().getEStructuralFeature("name");
    Object value = name == null ? null : o.eGet(name);
    return o.eClass().getName() + (value == null ? "" : "('" + value + "')")
        + "@" + Integer.toHexString(System.identityHashCode(o));
  }
}
