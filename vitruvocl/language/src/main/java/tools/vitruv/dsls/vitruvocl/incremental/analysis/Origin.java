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
import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

/**
 * Where the objects an expression evaluates to come from, relative to the context instance —
 * the {@code PathVar} of Cabot &amp; Teniente 2009, §3.2, restricted to what can be inverted.
 *
 * @param kind the kind of origin
 * @param steps for {@link Kind#SELF} and {@link Kind#CORRESPONDENTS}: the references navigated,
 *     in navigation order, from {@code self} (resp. from the correspondents of {@code self})
 * @param correspondentType for {@link Kind#CORRESPONDENTS}: the type the correspondents were
 *     taken from ({@code Y} in {@code Y.allInstances()->select(~)})
 */
public record Origin(Kind kind, List<EReference> steps, EClass correspondentType) {

  /** Origin kinds. */
  public enum Kind {
    /** Objects reachable from {@code self} by navigating {@link #steps()}. */
    SELF,
    /**
     * Objects reachable by navigating {@link #steps()} from the correspondents of {@code self}
     * (idiom rule, level 3c).
     */
    CORRESPONDENTS,
    /** Any other origin: an event on such an object is a class event (whole extension). */
    CLASS,
    /** Not objects (numbers, strings, Booleans): no feature can be read on them. */
    VALUE
  }

  public static final Origin SELF_ROOT = new Origin(Kind.SELF, List.of(), null);
  public static final Origin CLASS = new Origin(Kind.CLASS, List.of(), null);
  public static final Origin VALUE = new Origin(Kind.VALUE, List.of(), null);

  public Origin {
    steps = List.copyOf(steps);
  }

  /** Whether events on objects with this origin can be traced back to context instances. */
  public boolean traceable() {
    return kind == Kind.SELF || kind == Kind.CORRESPONDENTS;
  }

  /** This origin extended by navigating {@code reference}; untraceable origins stay {@link #CLASS}. */
  public Origin navigate(EReference reference) {
    if (!traceable()) {
      return CLASS;
    }
    List<EReference> extended = new ArrayList<>(steps);
    extended.add(reference);
    return new Origin(kind, extended, correspondentType);
  }
}
