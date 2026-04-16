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
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/** Tests for Methodologist-Template constraints using model and model2 metamodels. */
public class MethodologistTemplateConstraintTest {

  private static final Path MODEL_ECORE = Path.of("src/test/resources/test-metamodels/model.ecore");
  private static final Path MODEL2_ECORE =
      Path.of("src/test/resources/test-metamodels/model2.ecore");

  private static final Path SYSTEM_EMPTY =
      Path.of("src/test/resources/test-models/system-empty.model");
  private static final Path COMPONENT_WITH_PROTOCOL =
      Path.of("src/test/resources/test-models/component-with-protocol.model");
  private static final Path COMPONENT_NO_PROTOCOL =
      Path.of("src/test/resources/test-models/component-no-protocol.model");
  private static final Path ENTITY_VALID =
      Path.of("src/test/resources/test-models/entity-valid.model2");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // -------------------------------------------------------------------------
  // systemAlwaysFails — verletzt immer
  // -------------------------------------------------------------------------

  @Test
  public void testSystemAlwaysFails_violated() {
    String constraint =
        """
        context model::System inv systemAlwaysFails:
          false
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {MODEL_ECORE}, new Path[] {SYSTEM_EMPTY});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertFalse(result.isSatisfied(), "systemAlwaysFails should always be violated");
  }

  // -------------------------------------------------------------------------
  // systemAlwaysTrue — erfüllt immer
  // -------------------------------------------------------------------------

  @Test
  public void testSystemAlwaysTrue_satisfied() {
    String constraint =
        """
        context model::System inv systemAlwaysTrue:
          true
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {MODEL_ECORE}, new Path[] {SYSTEM_EMPTY});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "systemAlwaysTrue should always be satisfied");
  }

  // -------------------------------------------------------------------------
  // componentHasProtocols — Component muss mindestens ein Protocol haben
  // -------------------------------------------------------------------------

  @Test
  public void testComponentHasProtocols_satisfied() {
    String constraint =
        """
        context model::Component inv componentHasProtocols:
          self.supportedProtocols.size() > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {MODEL_ECORE}, new Path[] {COMPONENT_WITH_PROTOCOL});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Component with protocol should satisfy componentHasProtocols");
  }

  @Test
  public void testComponentHasProtocols_violated() {
    String constraint =
        """
        context model::Component inv componentHasProtocols:
          self.supportedProtocols.size() > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {MODEL_ECORE}, new Path[] {COMPONENT_NO_PROTOCOL});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertFalse(
        result.isSatisfied(), "Component without protocol should violate componentHasProtocols");
  }

  // -------------------------------------------------------------------------
  // entityExists — Entity immer vorhanden (model2)
  // -------------------------------------------------------------------------

  @Test
  public void testEntityExists_satisfied() {
    String constraint =
        """
        context model2::Entity inv entityExists:
          true
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {MODEL2_ECORE}, new Path[] {ENTITY_VALID});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "entityExists should always be satisfied");
  }
}