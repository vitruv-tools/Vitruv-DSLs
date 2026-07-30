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

package tools.vitruv.dsls.vitruvocl.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SmartLoader#loadForConstraints}, which replaced first-constraint-only
 * dependency analysis with a union across the whole constraint list (see {@code
 * ParallelBatchEvaluationTest#evaluateConstraintsLoadsUnionOfDependenciesAcrossTheWholeBatch} for
 * the end-to-end regression test through {@code VitruvOCL.evaluateConstraints}).
 */
class SmartLoaderTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");

  private static final Path[] BOTH_ECORES = {SPACEMISSION_ECORE, SATELLITE_ECORE};
  private static final Path[] BOTH_MODELS = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER};

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  /**
   * The bug this method fixes: a constraint list where only the *second* constraint references
   * {@code satelliteSystem} must still get that package loaded, even though the first constraint
   * never mentions it.
   */
  @Test
  void loadForConstraints_unionsPackagesAcrossAllConstraints() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv onlySpaceMission: true",
            "context spaceMission::Spacecraft inv alsoSatelliteSystem:"
                + " satelliteSystem::Satellite.allInstances().size() >= 0");

    SmartLoader.LoadResult result =
        SmartLoader.loadForConstraints(constraints, BOTH_ECORES, BOTH_MODELS);

    assertFalse(result.hasErrors(), "should load without file errors: " + result.fileErrors);
    Set<String> available = result.wrapper.getAvailableMetamodels();
    assertTrue(available.contains("spaceMission"), "spaceMission must be loaded");
    assertTrue(
        available.contains("satelliteSystem"),
        "satelliteSystem must be loaded even though only the second constraint needs it");
  }

  /**
   * Order must not matter: whichever constraint happens to be first, every referenced package ends
   * up loaded.
   */
  @Test
  void loadForConstraints_unionIsOrderIndependent() {
    List<String> reversedOrder =
        List.of(
            "context spaceMission::Spacecraft inv alsoSatelliteSystem:"
                + " satelliteSystem::Satellite.allInstances().size() >= 0",
            "context spaceMission::Spacecraft inv onlySpaceMission: true");

    SmartLoader.LoadResult result =
        SmartLoader.loadForConstraints(reversedOrder, BOTH_ECORES, BOTH_MODELS);

    assertFalse(result.hasErrors());
    Set<String> available = result.wrapper.getAvailableMetamodels();
    assertTrue(available.contains("spaceMission"));
    assertTrue(available.contains("satelliteSystem"));
  }

  /**
   * The common case — every constraint in the batch references the same single package — must load
   * exactly the same packages as before (single-constraint {@link
   * SmartLoader#loadForConstraint}), i.e. the union-based analysis must not load anything extra
   * when there is nothing extra to union.
   */
  @Test
  void loadForConstraints_withHomogeneousConstraints_loadsSamePackagesAsSingleConstraint() {
    String constraint = "context spaceMission::Spacecraft inv c: self.mass > 0";
    List<String> homogeneousBatch = List.of(constraint, constraint, constraint, constraint);

    SmartLoader.LoadResult single =
        SmartLoader.loadForConstraint(constraint, BOTH_ECORES, BOTH_MODELS);
    SmartLoader.LoadResult batch =
        SmartLoader.loadForConstraints(homogeneousBatch, BOTH_ECORES, BOTH_MODELS);

    assertFalse(single.hasErrors());
    assertFalse(batch.hasErrors());
    assertEquals(
        single.wrapper.getAvailableMetamodels(),
        batch.wrapper.getAvailableMetamodels(),
        "a homogeneous constraint list must load exactly the same package set as a single"
            + " constraint — no extra packages loaded just because the batch has repeats");
    // satelliteSystem is available among the candidate ecoreFiles but no constraint in either
    // scenario references it, so it must stay unloaded in both.
    assertFalse(single.wrapper.getAvailableMetamodels().contains("satelliteSystem"));
    assertFalse(batch.wrapper.getAvailableMetamodels().contains("satelliteSystem"));
  }

  /** A constraint referenced multiple times must not cause duplicate loading or errors. */
  @Test
  void loadForConstraints_withRepeatedIdenticalPackageReferences_doesNotError() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv a: true",
            "context spaceMission::Spacecraft inv b: self.mass > 0",
            "context spaceMission::Spacecraft inv c: self.operational");

    SmartLoader.LoadResult result =
        SmartLoader.loadForConstraints(constraints, BOTH_ECORES, BOTH_MODELS);

    assertFalse(result.hasErrors());
    assertEquals(Set.of("spaceMission"), result.wrapper.getAvailableMetamodels());
  }

  /** {@code loadForConstraint} must remain a pure single-constraint special case of the union. */
  @Test
  void loadForConstraint_delegatesToLoadForConstraintsWithSingletonList() {
    String constraint = "context spaceMission::Spacecraft inv c: true";

    SmartLoader.LoadResult direct =
        SmartLoader.loadForConstraint(constraint, BOTH_ECORES, BOTH_MODELS);
    SmartLoader.LoadResult viaList =
        SmartLoader.loadForConstraints(List.of(constraint), BOTH_ECORES, BOTH_MODELS);

    assertEquals(direct.wrapper.getAvailableMetamodels(), viaList.wrapper.getAvailableMetamodels());
    assertEquals(direct.hasErrors(), viaList.hasErrors());
  }
}
