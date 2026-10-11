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

package tools.vitruv.dsls.vitruvocl.incremental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.vitruv.dsls.vitruvocl.cli.VitruvOclCli;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * The CLI option {@code --incremental-level}: the CLI has no transaction, so every level must
 * report exactly the batch result of level 0 (the session's initial full evaluation).
 */
class IncrementalCliTest {

  private static final String ECORE = "src/test/resources/test-metamodels/spaceMission.ecore";
  private static final String XMI =
      "src/test/resources/test-models/spacecraft-voyager.spacemission,"
          + "src/test/resources/test-models/spacecraft-with-payloads.spacemission";

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static String run(String... args) {
    PrintStream original = System.out;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    try {
      VitruvOclCli.main(args);
    } finally {
      System.setOut(original);
    }
    return out.toString();
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "2", "3"})
  void everyLevelReportsTheLevelZeroBatchResult(String level, @TempDir Path dir) throws Exception {
    Path ocl = dir.resolve("c.ocl");
    Files.writeString(
        ocl,
        """
        context spaceMission::Spacecraft inv heavy:
          self.mass > 1000
        context spaceMission::Spacecraft inv named:
          self.name != ""
        context spaceMission::Spacecraft inv broken:
          self.doesNotExist > 0
        context spaceMission::Spacecraft post grown:
          self.mass >= self.mass@pre
        """);
    String baseline = run("eval-batch", ocl.toString(), "--ecore", ECORE, "--xmi", XMI, "--threads", "1");
    String incremental =
        run(
            "eval-batch", ocl.toString(), "--ecore", ECORE, "--xmi", XMI, "--threads", "1",
            "--incremental-level", level);
    assertTrue(baseline.contains("\"success\":true"), baseline);
    assertEquals(baseline, incremental);
  }
}
