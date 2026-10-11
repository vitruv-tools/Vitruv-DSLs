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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvocl.incremental.ModelEvent;
import tools.vitruv.dsls.vitruvocl.incremental.TransactionDelta;

/**
 * The potentially satisfaction-violating events (PSEs) of a constraint (incremental level 2;
 * Cabot &amp; Teniente 2009, Tab. 3.4), represented by the annotated reads they come from:
 *
 * <ul>
 *   <li>attribute reads: every change of the attribute is a PSE (UpdateAttribute, in all rows);
 *   <li>reference reads annotated with {@link Polarity}: InsertRT is a PSE iff an increase may
 *       violate, DeleteRT iff a decrease may violate;
 *   <li>context type: only insertion of instances (InsertET, "Access self variable" column);
 *   <li>{@code allInstances()} types: insertion for {@code +}, deletion for {@code −}.
 * </ul>
 *
 * @param typedReads feature name → static receiver class → annotation
 * @param untypedReads feature name → annotation, for reads on receivers of unknown class
 * @param insertSensitiveTypes classes whose gaining an instance may violate
 * @param deleteSensitiveTypes classes whose losing an instance may violate
 * @param unknownExtension some extension type is unknown: every structural event is a PSE
 */
public record PseSet(
    Map<String, Map<EClass, Polarity>> typedReads,
    Map<String, Polarity> untypedReads,
    Set<EClass> insertSensitiveTypes,
    Set<EClass> deleteSensitiveTypes,
    boolean unknownExtension) {

  public PseSet {
    Map<String, Map<EClass, Polarity>> copy = new LinkedHashMap<>();
    typedReads.forEach((k, v) -> copy.put(k, Collections.unmodifiableMap(new LinkedHashMap<>(v))));
    typedReads = Collections.unmodifiableMap(copy);
    untypedReads = Collections.unmodifiableMap(new LinkedHashMap<>(untypedReads));
    insertSensitiveTypes = Collections.unmodifiableSet(new LinkedHashSet<>(insertSensitiveTypes));
    deleteSensitiveTypes = Collections.unmodifiableSet(new LinkedHashSet<>(deleteSensitiveTypes));
  }

  /** Whether some event of {@code delta} is a PSE of this constraint. */
  public boolean matches(TransactionDelta delta) {
    for (ModelEvent event : delta.events()) {
      if (event.feature() != null && event.object() != null && isFeaturePse(event)) {
        return true;
      }
    }
    if (unknownExtension
        && !(delta.structurallyInsertedClasses().isEmpty()
            && delta.structurallyRemovedClasses().isEmpty())) {
      return true;
    }
    for (EClass type : insertSensitiveTypes) {
      if (delta.insertsInstanceOf(type)) {
        return true;
      }
    }
    for (EClass type : deleteSensitiveTypes) {
      if (delta.removesInstanceOf(type)) {
        return true;
      }
    }
    return false;
  }

  private boolean isFeaturePse(ModelEvent event) {
    EStructuralFeature feature = event.feature();
    Polarity polarity = polarityOf(feature.getName(), event.object().eClass());
    if (polarity == null) {
      return false; // feature not read on this class at all
    }
    if (feature instanceof EAttribute) {
      return true; // UpdateAttribute is a PSE in every row of Tab. 3.4
    }
    return switch (event.kind()) {
      case INSERT_REFERENCE -> polarity.increaseMayViolate();
      case REMOVE_REFERENCE -> polarity.decreaseMayViolate();
      case REPLACE_REFERENCE ->
          event.oldValue() != null && polarity.decreaseMayViolate()
              || event.newValue() != null && polarity.increaseMayViolate();
      default -> true;
    };
  }

  /** Join of the annotations of all reads of {@code name} that apply to runtime class {@code runtime}. */
  private Polarity polarityOf(String name, EClass runtime) {
    Polarity result = untypedReads.get(name);
    Map<EClass, Polarity> typed = typedReads.get(name);
    if (typed != null) {
      for (Map.Entry<EClass, Polarity> e : typed.entrySet()) {
        if (TransactionDelta.conforms(runtime, e.getKey())) {
          result = result == null ? e.getValue() : result.join(e.getValue());
        }
      }
    }
    return result;
  }
}
