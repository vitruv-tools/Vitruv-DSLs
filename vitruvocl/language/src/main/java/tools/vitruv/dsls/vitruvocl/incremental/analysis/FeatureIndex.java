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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Feature name → every class (of the known metamodels) on which that name resolves, with the
 * resolved feature. Needed because the evaluator resolves features by name on the runtime class,
 * and because Vitruvius does not record changes of some features at all (see {@link
 * #isUnrecorded}).
 */
public final class FeatureIndex {

  /** A class on which a feature name resolves, and the feature it resolves to. */
  public record Resolution(EClass owner, EStructuralFeature feature) {}

  private final Map<String, List<Resolution>> byName = new HashMap<>();

  private FeatureIndex(Collection<EPackage> packages) {
    Set<EPackage> all = new LinkedHashSet<>();
    for (EPackage p : packages) {
      collect(p, all);
    }
    for (EPackage p : all) {
      for (EClassifier classifier : p.getEClassifiers()) {
        if (classifier instanceof EClass c) {
          for (EStructuralFeature f : c.getEAllStructuralFeatures()) {
            byName.computeIfAbsent(f.getName(), k -> new ArrayList<>()).add(new Resolution(c, f));
          }
        }
      }
    }
  }

  private static void collect(EPackage p, Set<EPackage> sink) {
    if (p != null && sink.add(p)) {
      for (EPackage sub : p.getESubpackages()) {
        collect(sub, sink);
      }
    }
  }

  /**
   * Index over every {@link EPackage} instance in the global registry plus the metamodels the
   * wrapper knows (for file-based wrappers, whose packages need not be registered globally).
   */
  public static FeatureIndex forWrapper(MetamodelWrapperInterface wrapper) {
    List<EPackage> packages = new ArrayList<>();
    for (Object value : new ArrayList<>(EPackage.Registry.INSTANCE.values())) {
      if (value instanceof EPackage p) {
        packages.add(p);
      }
    }
    if (wrapper instanceof MetamodelWrapper mw) {
      for (String name : mw.getAvailableMetamodels()) {
        packages.add(mw.getEPackage(name));
      }
    }
    return new FeatureIndex(packages);
  }

  /** All resolutions of {@code name} on classes conforming to {@code staticType} (or all, if null). */
  public List<Resolution> resolve(String name, EClass staticType) {
    List<Resolution> result = new ArrayList<>();
    for (Resolution r : byName.getOrDefault(name, List.of())) {
      if (staticType == null || staticType == r.owner() || staticType.isSuperTypeOf(r.owner())) {
        result.add(r);
      }
    }
    if (staticType != null) {
      // the static class itself may come from a package outside the index
      EStructuralFeature own = staticType.getEStructuralFeature(name);
      if (own != null) {
        result.add(new Resolution(staticType, own));
      }
    }
    return result;
  }

  /**
   * Whether Vitruvius' change recording never reports value changes of {@code feature}: its
   * {@code NotificationToEChangeConverter} drops notifications of transient features, and {@code
   * EChangeCreationUtil} skips unchangeable, derived, transient and container features (verified
   * in the bytecode of {@code tools.vitruv.change.composite}). Volatile features are included
   * because their value is computed, not stored.
   */
  public static boolean isUnrecorded(EStructuralFeature feature) {
    return feature.isTransient()
        || feature.isDerived()
        || feature.isVolatile()
        || !feature.isChangeable()
        || isContainer(feature);
  }

  /** Whether {@code feature} is the container side of a containment (changes with moves). */
  public static boolean isContainer(EStructuralFeature feature) {
    return feature instanceof EReference ref && ref.isContainer();
  }
}
