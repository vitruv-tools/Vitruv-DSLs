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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;

/**
 * Everything a constraint can read from the model, independent of event direction (incremental
 * level 1). Computed once per constraint text by {@link ReadSetAnalyzer}.
 *
 * @param typedFeatureReads feature name → static receiver classes it is read on; an object of a
 *     subclass of any of them reads the same name
 * @param untypedFeatureReads feature names read on a receiver of unknown or non-metaclass static
 *     type (OclAny etc.); such a name is read on <em>every</em> class
 * @param extensionTypes classes whose extension (set and order of instances) the constraint
 *     iterates: the context types and every {@code allInstances()} receiver
 * @param unknownExtension {@code allInstances()} on a receiver whose class could not be determined
 *     statically, or a context type that could not be resolved — every structural event counts
 * @param usesCorrespondence uses {@code ~}, {@code select/reject/exists(~ …)}
 * @param usesPre uses {@code @pre}
 * @param usesLifecycle uses {@code OCLisNew}/{@code OCLisModified}/{@code OCLisDeleted}
 * @param isPrePost contains a {@code pre} or {@code post} block
 * @param hasErrorNodes contains a grammar alternative that only exists for error reporting
 * @param readsUnrecordedFeature reads a feature whose changes Vitruvius does not record
 *     (transient, derived, volatile, unchangeable), or a reference in a message template
 */
public record ReadSet(
    Map<String, Set<EClass>> typedFeatureReads,
    Set<String> untypedFeatureReads,
    Set<EClass> extensionTypes,
    boolean unknownExtension,
    boolean usesCorrespondence,
    boolean usesPre,
    boolean usesLifecycle,
    boolean isPrePost,
    boolean hasErrorNodes,
    boolean readsUnrecordedFeature) {

  public ReadSet {
    Map<String, Set<EClass>> copy = new LinkedHashMap<>();
    typedFeatureReads.forEach(
        (k, v) -> copy.put(k, Collections.unmodifiableSet(new LinkedHashSet<>(v))));
    typedFeatureReads = Collections.unmodifiableMap(copy);
    untypedFeatureReads = Collections.unmodifiableSet(new LinkedHashSet<>(untypedFeatureReads));
    extensionTypes = Collections.unmodifiableSet(new LinkedHashSet<>(extensionTypes));
  }

  /**
   * Whether the constraint depends on the transaction itself and not only on the post-state
   * ({@code @pre}, lifecycle predicates, pre/post blocks) or could not be analysed reliably (error
   * nodes, reads of features whose changes are not recorded). Such constraints are always fully
   * evaluated.
   */
  public boolean requiresFullEvaluation() {
    return usesPre || usesLifecycle || isPrePost || hasErrorNodes || readsUnrecordedFeature;
  }

  /** Whether {@code featureName} is read at all (typed or untyped). */
  public boolean readsFeatureName(String featureName) {
    return untypedFeatureReads.contains(featureName) || typedFeatureReads.containsKey(featureName);
  }
}
