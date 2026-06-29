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
package tools.vitruv.dsls.vitruvOCL.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;

/**
 * Integration tests for the {@link VitruviusOCLCLI} command-line interface.
 *
 * <p>Tests the {@code check}, {@code eval}, {@code eval-batch}, and {@code version} commands with
 * real constraint files and metamodels, verifying JSON output structure and content. Only valid
 * inputs are tested to avoid {@code System.exit} calls that would crash the JVM.
 */
class VitruviusOCLCLITest {

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
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
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
    VitruvOCLCLI.main(new String[] {"version"});
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
        new String[] {
          "eval", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    assertTrue(output().contains("\"success\":true"), "Named constraint eval should succeed");
  }

  // ══════════════════════════════════════════════════════════════
  // eval-batch command
  // ══════════════════════════════════════════════════════════════

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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

    VitruvOCLCLI.main(
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

  /** Tests that eval-batch with single constraint returns single result in array. */
  @Test
  void testEvalBatchSingleConstraint(@TempDir Path tempDir) throws java.io.IOException {
    Path oclFile = tempDir.resolve("single.ocl");
    Files.writeString(oclFile, "context spaceMission::Spacecraft inv onlyOne:\n  self.mass >= 0\n");

    VitruvOCLCLI.main(
        new String[] {
          "eval-batch", oclFile.toString(),
          "--ecore", SPACEMISSION_ECORE.toString(),
          "--xmi", SPACECRAFT_VOYAGER.toString()
        });

    String out = output();
    assertTrue(out.contains("onlyOne"), "Single constraint name should appear in output");
    assertTrue(out.contains("\"satisfied\":true"), "Single constraint should be satisfied");
  }

  // ══════════════════════════════════════════════════════════════
  // extractConstraintName (unit tests)
  // ══════════════════════════════════════════════════════════════

  /** Tests extraction of named invariant from multiline constraint. */
  @Test
  void testExtractNamedConstraint() {
    String constraint = "context spaceMission::Spacecraft inv myInvariant:\n  self.mass > 0";
    assertEquals("myInvariant", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests that unnamed invariant returns 'unknown'. */
  @Test
  void testExtractUnnamedConstraint() {
    String constraint = "context spaceMission::Spacecraft inv:\n  self.mass > 0";
    assertEquals("unknown", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests extraction with whitespace around constraint name. */
  @Test
  void testExtractConstraintNameWithWhitespace() {
    String constraint = "context spaceMission::Spacecraft inv  myName  :\n  self.mass > 0";
    assertEquals("myName", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests extraction from single-line constraint. */
  @Test
  void testExtractConstraintNameSingleLine() {
    String constraint = "context spaceMission::Spacecraft inv singleLine: self.mass > 0";
    assertEquals("singleLine", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests extraction from qualified context name. */
  @Test
  void testExtractConstraintNameQualifiedContext() {
    String constraint = "context brakesystem::BrakeDisk inv overlapping:\n  self.radius > 0";
    assertEquals("overlapping", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests extraction returns 'unknown' when no inv keyword present. */
  @Test
  void testExtractConstraintNameNoInv() {
    String constraint = "context spaceMission::Spacecraft\n  self.mass > 0";
    assertEquals("unknown", VitruvOCLCLI.extractConstraintName(constraint));
  }

  /** Tests extraction from constraint with parenthesized name. */
  @Test
  void testExtractConstraintNameWithParenthesizedSpec() {
    String constraint = "context spaceMission::Spacecraft inv myName(self.mass > 0): true";
    // The method extracts up to the colon — parenthesized form may not be standard
    // but the fallback should still not crash
    assertNotNull(VitruvOCLCLI.extractConstraintName(constraint));
  }
}
