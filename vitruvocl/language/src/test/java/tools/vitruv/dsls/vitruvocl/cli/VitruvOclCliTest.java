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

package tools.vitruv.dsls.vitruvocl.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Integration tests for the {@link VitruvOclCli} command-line interface.
 *
 * <p>Tests the {@code check}, {@code eval}, {@code eval-batch}, and {@code version} commands with
 * real constraint files and metamodels, verifying JSON output structure and content. Only valid
 * inputs are tested to avoid {@code System.exit} calls that would crash the JVM.
 */
class VitruvOclCliTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");

  private PrintStream originalOut;
  private PrintStream originalErr;
  private ByteArrayOutputStream capturedOut;
  private ByteArrayOutputStream capturedErr;

  /** Sets up the test model path before all tests. */
  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  /** Redirects stdout and stderr before each test to capture CLI output. */
  @BeforeEach
  void captureOutput() {
    originalOut = System.out;
    originalErr = System.err;
    capturedOut = new ByteArrayOutputStream();
    capturedErr = new ByteArrayOutputStream();
    System.setOut(new PrintStream(capturedOut));
    System.setErr(new PrintStream(capturedErr));
  }

  /** Restores stdout and stderr after each test. */
  @AfterEach
  void restoreOutput() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  private String output() {
    return capturedOut.toString();
  }

  // ══════════════════════════════════════════════════════════════
  // version command
  // ══════════════════════════════════════════════════════════════

  /** Tests that the version command prints version information. */
  @Test
  void testVersionCommand() {
    VitruvOclCli.main(new String[] {"version"});
    assertTrue(output().contains("OCL"), "Version output should contain 'OCL'");
  }

  // ══════════════════════════════════════════════════════════════
  // check command
  // ══════════════════════════════════════════════════════════════

  /** Tests that check command returns success:true for a syntactically valid constraint. */
  @Test
  void testCheckValidConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "check", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Valid constraint should report success");
    assertTrue(out.contains("\"diagnostics\""), "Output should contain diagnostics field");
  }

  /** Tests that check command returns success:false for a type error. */
  @Test
  void testCheckInvalidConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("invalid.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft inv:\n  self.nonExistentProperty > 0");

    VitruvOclCli.main(
        new String[] {
          "check", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":false"), "Invalid constraint should report failure");
    assertTrue(out.contains("\"diagnostics\""), "Output should contain diagnostics");
  }

  /** Tests that check command includes line number and message in error output. */
  @Test
  void testCheckOutputContainsErrorDetails(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("typeerror.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft inv:\n  self.nonExistentProperty > 0");

    VitruvOclCli.main(
        new String[] {
          "check", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString()
        });

    String out = output();
    assertTrue(out.contains("\"line\""), "Error should include line number");
    assertTrue(out.contains("\"message\""), "Error should include message");
    assertTrue(out.contains("\"severity\""), "Error should include severity");
  }

  /** Tests that check command produces empty diagnostics for valid constraint. */
  @Test
  void testCheckValidConstraintEmptyDiagnostics(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass >= 0");

    VitruvOclCli.main(
        new String[] {
          "check", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString()
        });

    String out = output();
    assertTrue(
        out.contains("\"diagnostics\":[]"), "Valid constraint should have empty diagnostics");
  }

  // ══════════════════════════════════════════════════════════════
  // eval command
  // ══════════════════════════════════════════════════════════════

  /** Tests that eval command returns satisfied:true for a satisfied constraint. */
  @Test
  void testEvalSatisfiedConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Eval should succeed");
    assertTrue(out.contains("\"satisfied\":true"), "Constraint should be satisfied");
  }

  /** Tests that eval command returns satisfied:false for a violated constraint. */
  @Test
  void testEvalViolatedConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass < 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Eval should compile successfully");
    assertTrue(out.contains("\"satisfied\":false"), "Violated constraint should not be satisfied");
  }

  /** Tests that eval command output contains required JSON fields. */
  @Test
  void testEvalOutputStructure(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\""), "Output should contain success field");
    assertTrue(out.contains("\"satisfied\""), "Output should contain satisfied field");
    assertTrue(out.contains("\"errors\""), "Output should contain errors field");
    assertTrue(out.contains("\"warnings\""), "Output should contain warnings field");
  }

  /** Tests that eval with no XMI files produces vacuously true result. */
  @Test
  void testEvalWithNoInstances(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Eval with no instances should succeed");
    assertTrue(
        out.contains("\"satisfied\":true"), "Constraint over empty set should be vacuously true");
  }

  /** Tests that eval works with multiple ecore files for cross-metamodel constraints. */
  @Test
  void testEvalWithMultipleEcoreFiles(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(
        oclFile,
        "context spaceMission::Spacecraft inv:\n"
            + "  satelliteSystem::Satellite.allInstances().size() >= 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE + "," + SATELLITE_ECORE,
          "--xmi", SPACECRAFT_VOYAGER + "," + SATELLITE_VOYAGER
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Cross-metamodel eval should succeed");
  }

  /** Tests that eval with named constraint works correctly. */
  @Test
  void testEvalWithNamedConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft inv massIsPositive:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    assertTrue(output().contains("\"success\":true"), "Named constraint eval should succeed");
  }

  /**
   * Tests that eval skips a {@code post} block (this CLI path has no transaction context) and
   * reports it as a PRE/POST SKIPPED warning rather than evaluating it — and, critically, does not
   * report the constraint as violated just because the skipped block would otherwise have been
   * vacuously false (see {@code EvaluationVisitor#transactionSupported}).
   */
  @Test
  void testEvalSkipsPostBlockAndReportsIt(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft post:\n  self.OCLisModified");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Eval should compile successfully");
    assertTrue(
        out.contains("\"satisfied\":true"),
        "A skipped post block must not be reported as violated: " + out);
    assertTrue(
        out.contains("PRE/POST SKIPPED:"), "Output should contain a PRE/POST SKIPPED warning: " + out);
    assertTrue(
        out.contains("spaceMission::Spacecraft"), "Skip notice should name the context: " + out);
  }

  // ══════════════════════════════════════════════════════════════
  // eval-batch command
  // ══════════════════════════════════════════════════════════════

  /**
   * Tests that a shared {@code context} header followed by several {@code inv} blocks (a
   * perfectly valid, idiomatic OCL style — one header, many invariants below it — distinct from
   * this repo's own example-file convention of repeating the header per invariant) is split into
   * independently evaluated units by eval-batch, exactly like separate header-per-inv blocks
   * already are. Before this fix, a single broken invariant under a shared header silently
   * discarded every sibling invariant under that same header — none of them appeared in the batch
   * result at all, not even as a generic failure.
   */
  @Test
  void testEvalBatchSharedContextHeaderIsolatesBrokenInvariant(@TempDir Path tempDir)
      throws java.io.IOException {
    Path oclFile = tempDir.resolve("shared.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft
        inv good1:
          self.mass > 0
        inv broken:
          self.thisPropertyDoesNotExist > 0
        inv good2:
          self.mass >= 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch itself should succeed: " + out);
    assertTrue(
        out.contains("\"name\":\"good1\",\"success\":true,\"satisfied\":true"),
        "good1 should be evaluated and satisfied independently of 'broken': " + out);
    assertTrue(
        out.contains("\"name\":\"good2\",\"success\":true,\"satisfied\":true"),
        "good2 should be evaluated and satisfied independently of 'broken': " + out);
    assertTrue(
        out.contains("\"name\":\"broken\",\"success\":false"),
        "broken should be reported as its own failed entry, not swallow its siblings: " + out);
    assertTrue(
        out.contains("thisPropertyDoesNotExist"), "broken's own compiler error should appear: " + out);
  }

  /**
   * Same as {@link #testEvalBatchSharedContextHeaderIsolatesBrokenInvariant} but for {@code pre}/
   * {@code post} sharing an operation-context header, since the fix generalizes the same
   * header-splitting logic to all three constraint keywords.
   */
  @Test
  void testEvalBatchSharedContextHeaderIsolatesBrokenPrePost(@TempDir Path tempDir)
      throws java.io.IOException {
    Path oclFile = tempDir.resolve("shared_pp.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft::launch(dest: String)
        pre good:
          self.mass > 0
        post broken:
          self.thisPropertyDoesNotExist > 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch itself should succeed: " + out);
    assertTrue(
        out.contains("\"name\":\"good\",\"success\":true"),
        "The pre block should be evaluated independently of the broken post block: " + out);
    assertTrue(
        out.contains("\"name\":\"broken\",\"success\":false"),
        "The post block should fail on its own, not swallow its sibling pre block: " + out);
  }

  /** Tests that eval-batch returns array of results for multiple constraints. */
  @Test
  void testEvalBatchMultipleConstraints(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("batch.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv massPositive:
          self.mass > 0

        context spaceMission::Spacecraft inv massNotNegative:
          self.mass >= 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch eval should succeed");
    assertTrue(out.contains("\"constraints\""), "Output should contain constraints array");
    assertTrue(out.contains("massPositive"), "Output should contain first constraint name");
    assertTrue(out.contains("massNotNegative"), "Output should contain second constraint name");
  }

  /** Tests that eval-batch handles mixed satisfied and violated constraints. */
  @Test
  void testEvalBatchMixedResults(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("mixed.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv satisfied:
          self.mass > 0

        context spaceMission::Spacecraft inv violated:
          self.mass < 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"satisfied\":true"), "First constraint should be satisfied");
    assertTrue(out.contains("\"satisfied\":false"), "Second constraint should be violated");
  }

  /** Tests that eval-batch skips comment lines in constraint files. */
  @Test
  void testEvalBatchSkipsComments(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("withcomments.ocl");
    Files.writeString(
        oclFile,
        """
        -- This is a comment
        context spaceMission::Spacecraft inv myConstraint:
          self.mass > 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch with comments should succeed");
    assertTrue(out.contains("myConstraint"), "Constraint name should be extracted");
  }

  /** Tests that eval-batch handles unnamed constraints with 'unknown' fallback. */
  @Test
  void testEvalBatchUnnamedConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("unnamed.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv:\n  self.mass > 0\n");

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    assertTrue(output().contains("unknown"), "Unnamed constraint should use 'unknown' as name");
  }

  /** Tests that eval-batch output contains name, success and satisfied fields per constraint. */
  @Test
  void testEvalBatchOutputStructure(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("batch.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft inv myConstraint:\n  self.mass > 0\n");

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"name\""), "Output should contain name field");
    assertTrue(out.contains("\"success\""), "Output should contain success field");
    assertTrue(out.contains("\"satisfied\""), "Output should contain satisfied field");
  }

  /**
   * Tests that eval-batch also skips {@code post} blocks (same no-transaction-context reasoning as
   * {@link #testEvalSkipsPostBlockAndReportsIt}), while still evaluating the {@code inv} in the same
   * context normally.
   */
  @Test
  void testEvalBatchSkipsPostBlockAndReportsIt(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("batch.ocl");
    Files.writeString(
        oclFile,
        "context spaceMission::Spacecraft inv massPositive:\n"
            + "  self.mass > 0\n"
            + "post:\n"
            + "  self.OCLisModified\n");

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch eval should succeed: " + out);
    assertTrue(out.contains("massPositive"), "Output should contain the inv's name: " + out);
    assertTrue(
        out.contains("\"satisfied\":true"),
        "inv is satisfied and the post block must not count as a violation: " + out);
    assertTrue(out.contains("PRE/POST SKIPPED:"), "Output should contain a skip warning: " + out);
  }

  /**
   * Tests that eval-batch accepts the optional {@code --threads <N>} flag (passed through to
   * {@code VitruvOCL.evaluateConstraints(..., threadPoolSize)}) without changing output shape or
   * content compared to the default (no {@code --threads}) invocation.
   */
  @Test
  void testEvalBatchWithThreadsFlag(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("batch.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv massPositive:
          self.mass > 0

        context spaceMission::Spacecraft inv massNotNegative:
          self.mass >= 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString(),
          "--threads", "4"
        });

    String out = output();
    assertTrue(out.contains("\"success\":true"), "Batch eval with --threads should succeed");
    assertTrue(out.contains("massPositive"), "Output should contain first constraint name");
    assertTrue(out.contains("massNotNegative"), "Output should contain second constraint name");
  }

  /**
   * Documents the (accepted) behavior change from switching {@code evalBatch} to the {@code
   * evaluateConstraints(...)} batch path: byte-identical duplicate constraints within one batch file
   * are now detected — the second (and any later) occurrence is reported as {@code satisfied:false}
   * with a duplicate-constraint warning rather than being independently evaluated again, matching
   * {@code evaluateConstraints}'s existing duplicate-detection pre-pass (see {@code
   * VitruvOclCliTest#testEvalBatchMultipleConstraints} and {@code ParallelBatchEvaluationTest
   * #duplicateConstraintDetectionStillWorksWithParallelEvaluation} for the underlying, unmodified
   * batch-path behavior this now surfaces through the CLI too).
   */
  @Test
  void testEvalBatchDetectsDuplicateConstraints(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("duplicates.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv dup:
          true

        context spaceMission::Spacecraft inv dup:
          true
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(
        out.contains("Constraint specified multiple times"),
        "Second occurrence of a duplicate constraint should carry a duplicate-constraint warning"
            + " (Warning#getMessage(), as serialized into the JSON \"warnings\" array), got: "
            + out);
  }

  /**
   * Tests that eval-batch correctly evaluates constraints which reference <em>different</em>
   * metamodel packages within the same batch (e.g. one constraint using only {@code spaceMission},
   * another additionally using {@code satelliteSystem}).
   *
   * <p>History: {@code evalBatch} originally evaluated each constraint via a loop calling {@code
   * VitruvOCL.evaluateConstraint(...)}, re-running {@code SmartLoader}'s dependency analysis and
   * reloading the model context on every iteration. It could not simply be switched to the {@code
   * ConstraintListEvaluator}-backed batch path ({@code VitruvOCL.evaluateConstraints(...)}) at the
   * time, because that method's loading step analyzed only the <em>first</em> constraint's
   * dependencies for the whole list — a later constraint referencing a package the first one didn't
   * need would fail to resolve it (confirmed empirically). {@code SmartLoader#loadForConstraints}
   * was then changed to union every constraint's dependencies before loading, which removed that
   * limitation; {@code evalBatch} now loads the context once via {@code evaluateConstraints(...)}
   * and this test guards that the cross-metamodel case keeps working end-to-end.
   */
  @Test
  void testEvalBatchHandlesConstraintsWithDifferentMetamodelDependencies(@TempDir Path tempDir)
      throws java.io.IOException {
    Path oclFile = tempDir.resolve("crossMetamodel.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv spaceMissionOnly:
          true

        context spaceMission::Spacecraft inv usesSatelliteSystemToo:
          satelliteSystem::Satellite.allInstances().size() >= 0
        """);

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE + "," + SATELLITE_ECORE,
          "--xmi", SPACECRAFT_VOYAGER + "," + SATELLITE_VOYAGER
        });

    String out = output();
    assertTrue(out.contains("spaceMissionOnly"), "First constraint name should appear");
    assertTrue(out.contains("usesSatelliteSystemToo"), "Second constraint name should appear");
    assertFalse(
        out.contains("\"success\":false"),
        "Both constraints must succeed even though only the second one references"
            + " satelliteSystem — each constraint gets its own dependency-scoped model load, got: "
            + out);
  }

  /** Tests that eval-batch with single constraint returns single result in array. */
  @Test
  void testEvalBatchSingleConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("single.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv onlyOne:\n  self.mass >= 0\n");

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("onlyOne"), "Single constraint name should appear in output");
    assertTrue(out.contains("\"satisfied\":true"), "Single constraint should be satisfied");
  }

  /**
   * Tests that a file-load failure (here: a nonexistent {@code --ecore} path) is surfaced in
   * eval-batch's JSON as an actual error message on every constraint, not just a bare {@code
   * "success":false} with no explanation.
   *
   * <p>Before this fix, {@code evalBatch()}'s JSON builder only rendered {@link
   * ConstraintResult#getCompilerErrors()}. When {@code SmartLoader} fails to load the batch's
   * metamodel/instance files, every {@link ConstraintResult} in the batch gets the load failure in
   * {@link ConstraintResult#getFileErrors()} instead — {@code compilerErrors} stays empty, so the
   * old JSON had no {@code "errors"} field at all, making a genuine (and identical, batch-wide) file
   * error indistinguishable from "every constraint happens to be individually broken."
   */
  @Test
  void testEvalBatchSurfacesFileErrorsNotJustCompilerErrors(@TempDir Path tempDir)
      throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(
        oclFile, "context spaceMission::Spacecraft inv massIsPositive:\n  self.mass > 0\n");

    VitruvOclCli.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", tempDir.resolve("does-not-exist.ecore").toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(
        out.contains("\"success\":false"), "The failed load should be reported as unsuccessful: " + out);
    assertTrue(
        out.contains("\"errors\":["),
        "The file-load failure must appear as an actual error, not silently: " + out);
    assertTrue(
        out.contains("does-not-exist.ecore"),
        "The error message should name the missing file: " + out);
  }

  /** Same proof as above, but for the single-constraint {@code eval} command. */
  @Test
  void testEvalSurfacesFileErrorsNotJustCompilerErrors(@TempDir Path tempDir)
      throws java.io.IOException {
    Path oclFile = tempDir.resolve("test.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv massIsPositive:\n  self.mass > 0");

    VitruvOclCli.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", tempDir.resolve("does-not-exist.ecore").toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("\"success\":false"), "The failed load should be unsuccessful: " + out);
    assertTrue(
        out.contains("does-not-exist.ecore"),
        "The error message should name the missing file, not leave \"errors\" empty: " + out);
  }

  // ══════════════════════════════════════════════════════════════
  // extractConstraintName (unit tests)
  // ══════════════════════════════════════════════════════════════

  /** Tests extraction of named invariant from multiline constraint. */
  @Test
  void testExtractNamedConstraint() {
    String constraint = "context spaceMission::Spacecraft inv myInvariant:\n  self.mass > 0";
    assertEquals("myInvariant", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests that unnamed invariant returns 'unknown'. */
  @Test
  void testExtractUnnamedConstraint() {
    String constraint = "context spaceMission::Spacecraft inv:\n  self.mass > 0";
    assertEquals("unknown", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests extraction with whitespace around constraint name. */
  @Test
  void testExtractConstraintNameWithWhitespace() {
    String constraint = "context spaceMission::Spacecraft inv  myName  :\n  self.mass > 0";
    assertEquals("myName", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests extraction from single-line constraint. */
  @Test
  void testExtractConstraintNameSingleLine() {
    String constraint = "context spaceMission::Spacecraft inv singleLine: self.mass > 0";
    assertEquals("singleLine", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests extraction from qualified context name. */
  @Test
  void testExtractConstraintNameQualifiedContext() {
    String constraint = "context brakesystem::BrakeDisk inv overlapping:\n  self.radius > 0";
    assertEquals("overlapping", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests extraction returns 'unknown' when no inv keyword present. */
  @Test
  void testExtractConstraintNameNoInv() {
    String constraint = "context spaceMission::Spacecraft\n  self.mass > 0";
    assertEquals("unknown", VitruvOclCli.extractConstraintName(constraint));
  }

  /** Tests extraction from constraint with parenthesized name. */
  @Test
  void testExtractConstraintNameWithParenthesizedSpec() {
    String constraint = "context spaceMission::Spacecraft inv myName(self.mass > 0): true";
    // The method extracts up to the colon — parenthesized form may not be standard
    // but the fallback should still not crash
    assertNotNull(VitruvOclCli.extractConstraintName(constraint));
  }
}
