/*******************************************************************************
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

package tools.vitruv.dsls.vitruvOCL.metamodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

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
public class LabelGraphCrossMetamodelTest {

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
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ==================== Constraint 1: exists ====================

  /**
   * Tests that every {@code Labelgraph1::SimpleNode} has a correspondent in {@code Labelgraph2}
   * identified by matching the three-digit suffix of their names.
   *
   * <p>OCL constraint:
   *
   * <pre>{@code
   * context Labelgraph1::SimpleNode inv:
   *   Labelgraph2::SimpleNode.allInstances().exists(n |
   *     n.name.substring(7, 9) == self.name.substring(7, 9)
   *   )
   * }</pre>
   *
   * <p>Expected: satisfied, because every LG1 name suffix appears exactly once in LG2.
   */
  @Test
  public void testExistsCorrespondentByNameSuffix() throws Exception {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().exists(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1, BASE_LABELGRAPH2});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(
        result.isSatisfied(),
        "Every LG1 SimpleNode should find at least one matching LG2 SimpleNode by name suffix");
  }

  // ==================== Constraint 2: select + size == 1 ====================

  /**
   * Tests that the correspondence is unique: for every {@code Labelgraph1::SimpleNode}, the {@code
   * select} over {@code Labelgraph2::SimpleNode.allInstances()} returns exactly one match.
   *
   * <p>OCL constraint:
   *
   * <pre>{@code
   * context Labelgraph1::SimpleNode inv:
   *   Labelgraph2::SimpleNode.allInstances().select(n |
   *     n.name.substring(7, 9) == self.name.substring(7, 9)
   *   ).size() == 1
   * }</pre>
   *
   * <p>Expected: satisfied, because each three-digit suffix is unique within LG2.
   */
  @Test
  public void testCorrespondenceIsUnique() throws Exception {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().select(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          ).size() == 1
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1, BASE_LABELGRAPH2});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(
        result.isSatisfied(),
        "Each LG1 SimpleNode should find exactly one matching LG2 SimpleNode (unique"
            + " correspondence)");
  }

  // ==================== Constraint 3: select + label navigation + size ====================

  /**
   * Tests flat label-collection navigation over the select result. After selecting the (singleton)
   * correspondent in LG2, navigating {@code .label} yields a flat collection of label values whose
   * {@code size()} must equal 1.
   *
   * <p>OCL constraint:
   *
   * <pre>{@code
   * context Labelgraph1::SimpleNode inv:
   *   Labelgraph2::SimpleNode.allInstances().select(n |
   *     n.name.substring(7, 9) == self.name.substring(7, 9)
   *   ).label.size() == 1
   * }</pre>
   *
   * <p>Expected: satisfied. The select returns a singleton collection; navigating {@code .label} on
   * it produces a singleton label collection, so {@code size() == 1}.
   */
  @Test
  public void testCorrespondentLabelCollectionSizeIsOne() throws Exception {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().select(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          ).label.size() == 1
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1, BASE_LABELGRAPH2});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(
        result.isSatisfied(), "Label collection of the singleton select result should have size 1");
  }

  // ==================== Constraint 4: forAll label consistency ====================

  /**
   * Tests that corresponding nodes in LG1 and LG2 carry the same label value.
   *
   * <p>OCL constraint:
   *
   * <pre>{@code
   * context Labelgraph1::SimpleNode inv:
   *   Labelgraph2::SimpleNode.allInstances().select(n |
   *     n.name.substring(7, 9) == self.name.substring(7, 9)
   *   ).forAll(e | e.label == self.label)
   * }</pre>
   *
   * <p>Expected: satisfied. The base models are generated with identical label assignments for
   * every corresponding node pair.
   */
  @Test
  public void testCorrespondentLabelsMatch() throws Exception {
    String constraint =
        """
        context Labelgraph1::SimpleNode inv:
          Labelgraph2::SimpleNode.allInstances().select(n |
            n.name.substring(7, 9) == self.name.substring(7, 9)
          ).forAll(e | e.label == self.label)
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {LABELGRAPH1_ECORE, LABELGRAPH2_ECORE},
            new Path[] {BASE_LABELGRAPH1, BASE_LABELGRAPH2});

    assertTrue(result.isSuccess(), "Compilation and evaluation should succeed");
    assertTrue(
        result.isSatisfied(),
        "Corresponding LG1/LG2 SimpleNodes should have identical label values");
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
  public void testForAllVacuouslyTrueWithoutLG2Instances() throws Exception {
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
  public void testExistsFailsWithoutLG2Instances() throws Exception {
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