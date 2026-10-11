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

package tools.vitruv.dsls.vitruvocl.incremental.support;

import java.util.List;

/**
 * The existing test metamodels and models under {@code src/test/resources}, grouped into sets that
 * are loaded together for the incremental oracle tests. The two large label-graph models are not
 * included (their size would dominate the mutation tests; see the implementation report).
 *
 * @param name display name
 * @param ecores metamodel files (relative to {@code test-metamodels/})
 * @param models instance files (relative to {@code test-models/})
 * @param correspondences correspondence file (relative to {@code test-models/}) or {@code null}
 * @param metamodelNames EPackage names whose constraints from the corpus are run on this set
 */
public record ModelSet(
    String name,
    List<String> ecores,
    List<String> models,
    String correspondences,
    List<String> metamodelNames) {

  public static final ModelSet SPACE =
      new ModelSet(
          "space",
          List.of("spaceMission.ecore", "satelliteSystem.ecore"),
          List.of(
              "spacecraft-voyager.spacemission",
              "spacecraft-atlas.spacemission",
              "spacecraft-active.spacemission",
              "spacecraft-inactive.spacemission",
              "spacecraft-operational.spacemission",
              "spacecraft-heavy.spacemission",
              "spacecraft-forall.spacemission",
              "spacecraft-power-sum.spacemission",
              "spacecraft-with-payloads.spacemission",
              "mission-apollo.spacemission",
              "satellite-voyager.satellitesystem",
              "satellite-atlas.satellitesystem",
              "satellite-hubble.satellitesystem"),
          "correspondences.correspondence",
          List.of("spaceMission", "satelliteSystem"));

  public static final ModelSet FAMILY =
      new ModelSet(
          "family",
          List.of("family.ecore", "persons.ecore"),
          List.of("testmodel.family", "testmodel.persons"),
          "testmodel.correspondence",
          List.of("family", "persons"));

  public static final ModelSet CAD =
      new ModelSet(
          "cad",
          List.of("cad.ecore"),
          List.of("Intersecting.cad", "NonIntersecting.cad", "brake_disc_and_caliper_plate.cad"),
          null,
          List.of("cad"));

  public static final ModelSet BRAKE =
      new ModelSet(
          "brake",
          List.of("brakesystem.ecore", "cad.ecore"),
          List.of("brakesystem.brakesystem", "brake_disc_and_caliper_plate.cad"),
          null,
          List.of("brakesystem", "cad"));

  public static final ModelSet COMPONENTS =
      new ModelSet(
          "components",
          List.of("model.ecore", "model2.ecore"),
          List.of(
              "component-no-protocol.model",
              "component-with-protocol.model",
              "system-empty.model",
              "entity-valid.model2"),
          null,
          List.of("model", "model2"));

  /** All sets used by the oracle tests. */
  public static List<ModelSet> all() {
    return List.of(SPACE, FAMILY, CAD, BRAKE, COMPONENTS);
  }

  @Override
  public String toString() {
    return name;
  }
}
