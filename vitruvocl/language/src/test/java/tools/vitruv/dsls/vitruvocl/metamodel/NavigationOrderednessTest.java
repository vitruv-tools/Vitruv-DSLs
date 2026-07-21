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

package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * Regression tests for the Ctype combination rule of implicit-collect navigation ({@code e.p}
 * where {@code e} is itself a collection).
 *
 * <p>{@code Spacecraft.payloads} is an {@code EReference} with {@code upperBound="-1"} and no
 * explicit {@code ordered}/{@code unique} overrides, so it defaults to EMF's {@code ordered=true,
 * unique=true} — an {@code OrderedSet(Payload)}. Navigating {@code .name} (a singleton {@code
 * EAttribute}) must combine that Ctype with the (neutral) feature Ctype via OCL#'s {@code ω ∨ ω'}
 * rule (Sect. 4.4.4), yielding {@code OrderedSet(String)} — not a flat {@code Set(String)} that
 * discards the payload order and would reject {@code first()}/{@code last()}.
 */
class NavigationOrderednessTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SPACECRAFT_WITH_PAYLOADS =
      Path.of("spacecraft-with-payloads.spacemission");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult eval(String constraint) {
    return VitruvOCL.evaluateConstraint(
        constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});
  }

  /**
   * {@code payloads} is ordered (EMF default) → {@code .name} must stay ordered, so {@code
   * first()} type-checks and returns the first payload's name in XMI document order ("Camera").
   */
  @Test
  void testNavigationOverOrderedCollectionPreservesOrderForFirst() {
    ConstraintResult r = eval("context spaceMission::Spacecraft inv: self.payloads.name.first() == \"Camera\"");
    assertTrue(r.isSuccess(), "Expected no type errors: " + r.getCompilerErrors());
    assertTrue(r.isSatisfied(), "Expected self.payloads.name.first() to be \"Camera\"");
  }

  /** Same reasoning for {@code last()} — the second (and last) payload is "Antenna". */
  @Test
  void testNavigationOverOrderedCollectionPreservesOrderForLast() {
    ConstraintResult r = eval("context spaceMission::Spacecraft inv: self.payloads.name.last() == \"Antenna\"");
    assertTrue(r.isSuccess(), "Expected no type errors: " + r.getCompilerErrors());
    assertTrue(r.isSatisfied(), "Expected self.payloads.name.last() to be \"Antenna\"");
  }

  /**
   * {@code asSequence()} only type-checks on an ordered receiver — confirms {@code
   * self.payloads.name} is inferred as an ordered Ctype (Sequence/OrderedSet), not {@code
   * Set(String)}.
   */
  @Test
  void testNavigationResultIsOrderedCollection() {
    ConstraintResult r =
        eval("context spaceMission::Spacecraft inv: self.payloads.name.asSequence().size() == 2");
    assertTrue(r.isSuccess(), "Expected no type errors: " + r.getCompilerErrors());
    assertTrue(r.isSatisfied());
  }
}
