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

import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Level-3 analysis of a constraint: every feature read together with the {@link Origin} of its
 * receiver, so that the context instances affected by a change on a given object can be computed
 * by inverting the receiver's path (Cabot &amp; Teniente 2009, §3.2, {@code Inverse(PathVar)}).
 *
 * @param reads every feature read; a read whose origin is not traceable makes every event on it a
 *     class event
 * @param contextTypes the classifier context types
 * @param classEventTypes extension types whose structural changes are class events (all {@code
 *     allInstances()} receivers, owners of container features); insertions of correspondents
 *     under the idiom rule are handled separately
 * @param correspondentTypes the types {@code Y} of idiom uses {@code Y.allInstances()->select(~)}
 *     (rule 3c): inserting a {@code Y} object affects the context instances corresponding to it
 * @param correspondenceOnlyInIdiom whether every use of correspondence is the idiom {@code
 *     Y.allInstances()->select/exists(~ …)} (needed to apply rule 3c at all)
 * @param unknownExtension some context or {@code allInstances()} type is unknown: every
 *     structural event is a class event
 */
public record RelevancePlan(
    List<Read> reads,
    Set<EClass> contextTypes,
    Set<EClass> classEventTypes,
    Set<EClass> correspondentTypes,
    boolean correspondenceOnlyInIdiom,
    boolean unknownExtension) {

  /**
   * One feature read.
   *
   * @param feature the feature name
   * @param receiverClass the static receiver class, {@code null} if unknown (matches every class)
   * @param origin the origin of the receiver objects
   */
  public record Read(String feature, EClass receiverClass, Origin origin) {

    /** Whether a change of {@code feature} on {@code object} is a change of this read. */
    public boolean matches(String changedFeature, EObject object) {
      return feature.equals(changedFeature)
          && (receiverClass == null
              || receiverClass == object.eClass()
              || receiverClass.isSuperTypeOf(object.eClass()));
    }
  }

  public RelevancePlan {
    reads = List.copyOf(reads);
    contextTypes = Set.copyOf(contextTypes);
    classEventTypes = Set.copyOf(classEventTypes);
    correspondentTypes = Set.copyOf(correspondentTypes);
  }
}
