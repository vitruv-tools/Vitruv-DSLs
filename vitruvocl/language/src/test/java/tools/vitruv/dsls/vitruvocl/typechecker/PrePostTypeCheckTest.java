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

package tools.vitruv.dsls.vitruvocl.typechecker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * M2 (type-checking) tests for {@code pre}/{@code post} blocks, {@code @pre}, and the
 * {@code OCLisNew}/{@code OCLisModified}/{@code OCLisDeleted} lifecycle predicates.
 *
 * <p>Uses the brakesystem metamodel (same fixture as {@link AnnotationTypeCheckTest}) so context
 * types resolve and property/attribute type-checking is actually exercised. {@code BrakeDisk} has
 * {@code diameterInMM}/{@code minimumThicknessInMM} (Integer), {@code ventilated} (Boolean), and
 * (inherited) {@code id} (String).
 */
class PrePostTypeCheckTest {

  private static final Path BRAKESYSTEM_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path BRAKESYSTEM_INSTANCE = Path.of("brakesystem.brakesystem");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  @AfterAll
  static void cleanupRegistry() {
    EPackage.Registry.INSTANCE.remove("http://vitruv.tools/brakesystem/model");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0");
    EPackage.Registry.INSTANCE.remove("http://vitruv.tools/metamodels/change/correspondence/1.0");
  }

  private ConstraintResult eval(String constraint) {
    return VitruvOCL.evaluateConstraint(
        constraint, new Path[] {BRAKESYSTEM_ECORE}, new Path[] {BRAKESYSTEM_INSTANCE});
  }

  // ==================== Cardinality ====================

  @Test
  void testSingleReAndPostAccepted() {
    ConstraintResult result =
        eval(
            """
        context brakesystem::BrakeDisk pre: self.diameterInMM > 0
        post: self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testDuplicatePreReportsError() {
    ConstraintResult result =
        eval(
            """
        context brakesystem::BrakeDisk
        pre: self.diameterInMM > 0
        pre: self.diameterInMM > 1
        post: true""");
    assertFalse(result.isSuccess(), "Expected failure for duplicate pre");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("pre")),
        "Error should mention 'pre': " + result.toDetailedErrorString());
  }

  @Test
  void testDuplicatePostReportsError() {
    ConstraintResult result =
        eval(
            """
        context brakesystem::BrakeDisk
        pre: true
        post: self.diameterInMM > 0
        post: self.diameterInMM > 1""");
    assertFalse(result.isSuccess(), "Expected failure for duplicate post");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("post")),
        "Error should mention 'post': " + result.toDetailedErrorString());
  }

  // ==================== Boolean conformance ====================

  @Test
  void testNonBooleanPostBodyReportsError() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk post: self.diameterInMM");
    assertFalse(result.isSuccess(), "Expected failure: post body is Integer, not Boolean");
  }

  @Test
  void testNonBooleanPreBodyReportsError() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk pre: self.diameterInMM");
    assertFalse(result.isSuccess(), "Expected failure: pre body is Integer, not Boolean");
  }

  // ==================== @pre placement ====================

  @Test
  void testPreOperatorInPostBlockAccepted() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.diameterInMM@pre > 0");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testPreOperatorInInvReportsError() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk inv: self.diameterInMM@pre > 0");
    assertFalse(result.isSuccess(), "Expected failure: @pre is only valid in post blocks");
    assertTrue(
        result.getCompilerErrors().stream()
            .anyMatch(e -> e.getMessage().contains("@pre") && e.getMessage().contains("post")),
        "Error should mention '@pre' and 'post': " + result.toDetailedErrorString());
  }

  @Test
  void testPreOperatorWithoutPrecedingPropertyAccessReportsError() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk post: self@pre = self");
    assertFalse(
        result.isSuccess(), "Expected failure: '@pre' must directly follow a property access");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("@pre")),
        "Error should mention '@pre': " + result.toDetailedErrorString());
  }

  // ==================== Lifecycle predicate placement ====================

  @Test
  void testOclIsNewInPostAccepted() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk post: self.OCLisNew");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsModifiedInPostAccepted() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk post: self.OCLisModified");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsDeletedInPostAccepted() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk post: self.OCLisDeleted");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsNewInInvReportsError() {
    ConstraintResult result = eval("context brakesystem::BrakeDisk inv: self.OCLisNew");
    assertFalse(result.isSuccess(), "Expected failure: OCLisNew is only valid in post blocks");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("post")),
        "Error should mention 'post': " + result.toDetailedErrorString());
  }

  // ==================== Lifecycle predicate receiver type ====================

  @Test
  void testOclIsNewOnNonMetaclassReceiverReportsError() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.diameterInMM.OCLisNew");
    assertFalse(result.isSuccess(), "Expected failure: OCLisNew receiver must be an object type");
  }

  // ==================== Aggregate attribute resolution ====================

  @Test
  void testOclIsNewAggregateKnownAttributeAccepted() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisNew(diameterInMM => 5)");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsNewAggregateUnknownAttributeReportsError() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisNew(bogusAttr => 5)");
    assertFalse(result.isSuccess(), "Expected failure: 'bogusAttr' does not exist on BrakeDisk");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("bogusAttr")),
        "Error should mention 'bogusAttr': " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsNewAggregateValueTypeMismatchReportsError() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisNew(diameterInMM => \"not a number\")");
    assertFalse(result.isSuccess(), "Expected failure: String value assigned to Integer attribute");
  }

  @Test
  void testOclIsNewAggregatePartialListIsNotAnError() {
    // BrakeDisk has several attributes (diameterInMM, minimumThicknessInMM, ventilated, id, ...).
    // Listing only one must NOT trigger a "missing attribute" error — the spec explicitly rejects
    // completeness enforcement for the aggregate form.
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisNew(diameterInMM => 5)");
    assertTrue(
        result.isSuccess(),
        "Partial aggregates must be accepted (no completeness check): "
            + result.toDetailedErrorString());
  }

  @Test
  void testOclIsModifiedAggregateKnownAttributeAccepted() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisModified(diameterInMM => 5)");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOclIsModifiedAggregateUnknownAttributeReportsError() {
    ConstraintResult result =
        eval("context brakesystem::BrakeDisk post: self.OCLisModified(bogusAttr => 5)");
    assertFalse(result.isSuccess(), "Expected failure: 'bogusAttr' does not exist on BrakeDisk");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("bogusAttr")),
        "Error should mention 'bogusAttr': " + result.toDetailedErrorString());
  }

  // ==================== Operation-context header ====================

  @Test
  void testOperationContextParamResolvesInPreAndPost() {
    ConstraintResult result =
        eval(
            """
        context brakesystem::BrakeDisk::inspect(threshold: Integer)
        pre: threshold > 0
        post: self.diameterInMM > threshold""");
    assertTrue(result.isSuccess(), "Expected success: " + result.toDetailedErrorString());
  }

  @Test
  void testOperationContextUndefinedParamReportsError() {
    ConstraintResult result =
        eval(
            """
        context brakesystem::BrakeDisk::inspect(threshold: Integer)
        pre: undefinedParam > 0
        post: true""");
    assertFalse(result.isSuccess(), "Expected failure: 'undefinedParam' was never declared");
  }
}
