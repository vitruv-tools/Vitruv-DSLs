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
 ******************************************************************************/

package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * Integration tests for cross-metamodel OCL constraints over two LabelGraph metamodels.
 *
 * <h2>Metamodels Under Test</h2>
 *
 * <ul>
 *   <li><b>Labelgraph1</b> ({@code http://st.tud.de/Labelgraph1}): Graph with {@code SimpleNode}
 *       (has {@code name}, {@code label}) and {@code Region} nodes.
 *   <li><b>Labelgraph2</b> ({@code http://st.tud.de/Labelgraph2}): Structurally identical graph
 *       metamodel in a separate namespace, representing a correspondent model.
 * </ul>
 *
 * <h2>Name Convention</h2>
 *
 * <p>Both models use {@code SimpleNode} names of the form {@code "SN|G2|NNN"} where {@code NNN} is
 * a zero-padded three-digit number. {@code substring(7, 9)} (1-indexed, inclusive) extracts {@code
 * NNN} and serves as the unique correspondence key across the two metamodels.
 *
 * <h2>Constraint Semantics</h2>
 *
 * <p>The four constraints tested here correspond to four levels of cross-metamodel consistency
 * checking:
 *
 * <ol>
 *   <li><b>exists</b>: Every LG1 node has at least one correspondent in LG2.
 *   <li><b>select + size == 1</b>: Correspondence is unique (exactly one match per node).
 *   <li><b>select + label.size() == 1</b>: The label collection of matching LG2 nodes has size 1
 *       (flat collection navigation over a singleton select result).
 *   <li><b>forAll label == self.label</b>: Corresponding nodes carry the same label.
 * </ol>
 *
 * <h2>Model Instances</h2>
 *
 * <p>Both {@code base.labelgraph1} and {@code base.labelgraph2} contain 1000 {@code SimpleNode}
 * objects each. Every name from LG1 appears exactly once in LG2 with the same label, so all four
 * constraints are satisfied on the base models.
 *
 * @see dsls.vitruvOCLInterface#evaluateConstraint(String, Path[], Path[])
 */
class LabelGraphCrossMetamodelTest {

  // ==================== Metamodels ====================

  /** Labelgraph1 metamodel definition. */
  private static final Path LABELGRAPH1_ECORE =
      Path.of("src/test/resources/test-metamodels/idlabelgraph1.ecore");

  /** Labelgraph2 metamodel definition. */
  private static final Path LABELGRAPH2_ECORE =
      Path.of("src/test/resources/test-metamodels/idlabelgraph2.ecore");

  // ==================== Model Instances ====================

  /**
   * Labelgraph1 instance: 1000 {@code SimpleNode} objects with names {@code "SN|G2|NNN"} and labels
   * drawn from {@code {GREY, GREEN, ORANGE, BLUE}}.
   */
  private static final Path BASE_LABELGRAPH1 = Path.of("base.labelgraph1");

  /**
   * Labelgraph2 instance: 1000 {@code SimpleNode} objects mirroring {@code base.labelgraph1} — same
   * names, same labels. All cross-metamodel constraints are satisfied against this instance.
   */
  private static final Path BASE_LABELGRAPH2 = Path.of("base.labelgraph2");

  /** Registers the test model base path before all tests run. */
  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  // ==================== Constraints 1-4: satisfied correspondence checks ====================

  /**
   * Tests the four levels of cross-metamodel correspondence checking described in the class
   * Javadoc (exists, unique select, label-collection navigation, forAll label consistency).
   *
   * <p>Expected: satisfied for every variant, because every LG1 name suffix appears exactly once
   * in LG2 with a matching label.
   *
   * @param constraint the OCL constraint variant under test
   * @param message assertion message describing what satisfaction means for this variant
   */
  @ParameterizedTest
  @MethodSource("satisfiedCorrespondenceConstraints")
  void testCorrespondenceConstraintSatisfied(String constraint, String message) {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1, BASE_LABELGRAPH2});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(result.isSatisfied(), message);
  }

  static Stream<Arguments> satisfiedCorrespondenceConstraints() {
    return Stream.of(
        // Constraint 1: exists
        Arguments.of(
            """
            context Labelgraph1::SimpleNode inv:
              Labelgraph2::SimpleNode.allInstances().exists(n |
                n.name.substring(7, 9) == self.name.substring(7, 9)
              )
            """,
            "Every LG1 SimpleNode should find at least one matching LG2 SimpleNode by name suffix"),
        // Constraint 2: select + size == 1
        Arguments.of(
            """
            context Labelgraph1::SimpleNode inv:
              Labelgraph2::SimpleNode.allInstances().select(n |
                n.name.substring(7, 9) == self.name.substring(7, 9)
              ).size() == 1
            """,
            "Each LG1 SimpleNode should find exactly one matching LG2 SimpleNode (unique"
                + " correspondence)"),
        // Constraint 3: select + label navigation + size
        Arguments.of(
            """
            context Labelgraph1::SimpleNode inv:
              Labelgraph2::SimpleNode.allInstances().select(n |
                n.name.substring(7, 9) == self.name.substring(7, 9)
              ).label.size() == 1
            """,
            "Label collection of the singleton select result should have size 1"),
        // Constraint 4: forAll label consistency
        Arguments.of(
            """
            context Labelgraph1::SimpleNode inv:
              Labelgraph2::SimpleNode.allInstances().select(n |
                n.name.substring(7, 9) == self.name.substring(7, 9)
              ).forAll(e | e.label == self.label)
            """,
            "Corresponding LG1/LG2 SimpleNodes should have identical label values"));
  }

  // ==================== Violation test: forAll vacuously true ====================

  /**
   * Tests that the {@code forAll} constraint is vacuously true when no LG2 instances are loaded.
   *
   * <p>Without any {@code Labelgraph2::SimpleNode} instances, {@code allInstances()} returns an
   * empty collection. {@code forAll} on an empty collection is vacuously true, so this variant
   * documents that the constraint requires both models to be present for a meaningful check.
   *
   * <p>Expected: satisfied (vacuously), because without LG2 instances the {@code forAll} body is
   * never evaluated.
   */
  @Test
  void testForAllVacuouslyTrueWithoutLG2Instances() {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().select(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          ).forAll(e | e.label == self.label)
        """;

    // Load only LG1 instance — LG2 allInstances() is empty
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(
        result.isSatisfied(),
        "forAll over empty collection is vacuously true — both models must be present for a"
            + " meaningful check");
  }

  // ==================== Violation test: exists fails without LG2 ====================

  /**
   * Tests that the {@code exists} constraint is violated when no LG2 instances are loaded.
   *
   * <p>Without any {@code Labelgraph2::SimpleNode} instances, {@code allInstances()} returns an
   * empty collection and {@code exists} can never be satisfied.
   *
   * <p>Expected: not satisfied, because the LG2 model is absent.
   */
  @Test
  void testExistsFailsWithoutLG2Instances() {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().exists(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          )
        """;

    // Load only LG1 instance — LG2 allInstances() returns empty collection
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertFalse(
        result.isSatisfied(),
        "exists over empty collection must be false — no LG2 nodes to match against");
  }
}
