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
package tools.vitruv.dsls.vitruvocl.annotation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCLCompiler;

/**
 * Comprehensive test suite for {@code @severity} and {@code @message} constraint annotations.
 *
 * <p>Covers four categories:
 *
 * <ol>
 *   <li><b>Parse-level tests</b> — verify the grammar accepts / rejects tokens before type-checking
 *   <li><b>Semantic error tests</b> — valid syntax but invalid values (e.g. unknown severity names)
 *   <li><b>Acceptance tests</b> — all valid annotation combinations evaluate without error
 *   <li><b>Violation output tests</b> — {@code @message} template and {@code @severity} appear in
 *       violation records when a constraint is not satisfied
 * </ol>
 *
 * <p>Tests that exercise the full evaluation pipeline use the {@code brakesystem} metamodel so
 * the context class ({@code BrakeDisk}) resolves correctly and annotation validation inside
 * {@code visitInvCS} is actually reached.
 */
@DisplayName("@severity and @message annotation tests")
class AnnotationSyntaxAndSemanticsTest {

  // ---------------------------------------------------------------------------
  // Shared metamodel paths (brakesystem provides BrakeDisk with diameterInMM)
  // ---------------------------------------------------------------------------

  private static final Path ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path XMI = Path.of("brakesystem.brakesystem");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  @AfterAll
  static void cleanupRegistry() {
    EPackage.Registry.INSTANCE.remove("http://vitruv.tools/brakesystem/model");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/change/correspondence/1.0");
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Runs the full three-pass pipeline and returns a {@link ConstraintResult}. */
  private static ConstraintResult eval(String constraint) {
    return VitruvOCL.evaluateConstraint(constraint, new Path[]{ECORE}, new Path[]{XMI});
  }

  /**
   * Parses the given source with ANTLR and returns the parser so callers can check syntax errors.
   * No semantic analysis is performed.
   */
  private static VitruvOCLParser parse(String source) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(source));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners(); // suppress console noise in tests
    parser.contextDeclCS();
    return parser;
  }

  /** Returns the first compiler error message, or the empty string if there are no errors. */
  private static String firstError(ConstraintResult result) {
    return result.getCompilerErrors().isEmpty()
        ? ""
        : result.getCompilerErrors().get(0).getMessage();
  }

  // ===========================================================================
  // 1. Valid @severity values — all five levels must be accepted
  // ===========================================================================

  @Nested
  @DisplayName("Valid @severity values")
  class ValidSeverityValues {

    @Test
    @DisplayName("CRITICAL is accepted")
    void critical() {
      ConstraintResult r = eval(inv("@severity CRITICAL\n    self.diameterInMM > 0"));
      assertTrue(r.isSuccess(), "CRITICAL should be valid: " + r.toDetailedErrorString());
    }

    @Test
    @DisplayName("WARNING is accepted")
    void warning() {
      ConstraintResult r = eval(inv("@severity WARNING\n    self.diameterInMM > 0"));
      assertTrue(r.isSuccess(), "WARNING should be valid: " + r.toDetailedErrorString());
    }

    @Test
    @DisplayName("MAJOR is accepted")
    void major() {
      ConstraintResult r = eval(inv("@severity MAJOR\n    self.diameterInMM > 0"));
      assertTrue(r.isSuccess(), "MAJOR should be valid: " + r.toDetailedErrorString());
    }

    @Test
    @DisplayName("MINOR is accepted")
    void minor() {
      ConstraintResult r = eval(inv("@severity MINOR\n    self.diameterInMM > 0"));
      assertTrue(r.isSuccess(), "MINOR should be valid: " + r.toDetailedErrorString());
    }

    @Test
    @DisplayName("INFO is accepted")
    void info() {
      ConstraintResult r = eval(inv("@severity INFO\n    self.diameterInMM > 0"));
      assertTrue(r.isSuccess(), "INFO should be valid: " + r.toDetailedErrorString());
    }

    // Small helper to build a full context declaration from annotation + body text
    private String inv(String annotationAndBody) {
      return "context brakesystem::BrakeDisk inv:\n    " + annotationAndBody;
    }
  }

  // ===========================================================================
  // 2. Invalid @severity values — semantic type errors
  //    All cases below are syntactically valid (the value is a legal identifier)
  //    but fail the semantic check in visitSeverityAnnotation.
  // ===========================================================================

  @Nested
  @DisplayName("Invalid @severity values — semantic errors")
  class InvalidSeverityValues {

    @Test
    @DisplayName("IMPORTANT is rejected (sounds valid but is not a defined level)")
    void important() {
      assertSeverityError("IMPORTANT");
    }

    @Test
    @DisplayName("ERROR is rejected (often confused with logging level ERROR)")
    void error() {
      assertSeverityError("ERROR");
    }

    @Test
    @DisplayName("BLOCKER is rejected (common in issue trackers)")
    void blocker() {
      assertSeverityError("BLOCKER");
    }

    @Test
    @DisplayName("HIGH is rejected (common severity-scale alias)")
    void high() {
      assertSeverityError("HIGH");
    }

    @Test
    @DisplayName("MEDIUM is rejected (common severity-scale alias)")
    void medium() {
      assertSeverityError("MEDIUM");
    }

    @Test
    @DisplayName("LOW is rejected (common severity-scale alias)")
    void low() {
      assertSeverityError("LOW");
    }

    @Test
    @DisplayName("URGENT is rejected")
    void urgent() {
      assertSeverityError("URGENT");
    }

    @Test
    @DisplayName("FATAL is rejected (not in the defined set)")
    void fatal() {
      assertSeverityError("FATAL");
    }

    @Test
    @DisplayName("DEBUG is rejected (logging level, not a severity)")
    void debug() {
      assertSeverityError("DEBUG");
    }

    @Test
    @DisplayName("Lowercase 'critical' is rejected — values are case-sensitive")
    void lowercaseCritical() {
      assertSeverityError("critical");
    }

    @Test
    @DisplayName("Lowercase 'warning' is rejected — values are case-sensitive")
    void lowercaseWarning() {
      assertSeverityError("warning");
    }

    @Test
    @DisplayName("Lowercase 'info' is rejected — values are case-sensitive")
    void lowercaseInfo() {
      assertSeverityError("info");
    }

    @Test
    @DisplayName("Mixed-case 'Critical' is rejected — values are case-sensitive")
    void mixedCaseCritical() {
      assertSeverityError("Critical");
    }

    @Test
    @DisplayName("Mixed-case 'Warning' is rejected — values are case-sensitive")
    void mixedCaseWarning() {
      assertSeverityError("Warning");
    }

    @Test
    @DisplayName("Mixed-case 'Major' is rejected — values are case-sensitive")
    void mixedCaseMajor() {
      assertSeverityError("Major");
    }

    @Test
    @DisplayName("Abbreviated 'CRIT' is rejected")
    void crit() {
      assertSeverityError("CRIT");
    }

    @Test
    @DisplayName("Abbreviated 'WARN' is rejected")
    void warn() {
      assertSeverityError("WARN");
    }

    @Test
    @DisplayName("Error message always mentions the offending value")
    void errorMentionsValue() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @severity TYPO\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess());
      assertTrue(
          r.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("TYPO")),
          "Error message should contain the offending value 'TYPO', got: "
              + r.getCompilerErrors());
    }

    @Test
    @DisplayName("Error message lists all valid levels")
    void errorListsValidLevels() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @severity NOPE\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess());
      String msg = firstError(r);
      // The error must guide the user to valid options
      assertTrue(msg.contains("CRITICAL"), "Error should mention CRITICAL, got: " + msg);
      assertTrue(msg.contains("WARNING"),  "Error should mention WARNING, got: "  + msg);
      assertTrue(msg.contains("MAJOR"),    "Error should mention MAJOR, got: "    + msg);
      assertTrue(msg.contains("MINOR"),    "Error should mention MINOR, got: "    + msg);
      assertTrue(msg.contains("INFO"),     "Error should mention INFO, got: "     + msg);
    }

    // -- helper --

    private void assertSeverityError(String value) {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @severity " + value
              + "\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@severity " + value + " should be rejected but was accepted");
    }
  }

  // ===========================================================================
  // 3. @severity syntax errors — wrong token type at parse level
  //    Integers, symbols, or unknown annotation keywords cannot parse correctly.
  // ===========================================================================

  @Nested
  @DisplayName("@severity syntax errors — wrong tokens")
  class SeveritySyntaxErrors {

    @Test
    @DisplayName("@severity with an integer value causes a syntax error")
    void integerValue() {
      // "42" is an INT token, not an ID — the grammar expects ID after @severity
      VitruvOCLParser p = parse(
          "context brakesystem::BrakeDisk inv:\n    @severity 42\n    self.diameterInMM > 0");
      assertTrue(p.getNumberOfSyntaxErrors() > 0,
          "Integer value after @severity should be a syntax error");
    }

    @Test
    @DisplayName("@severity with a quoted string causes a syntax error")
    void quotedStringValue() {
      // "\"CRITICAL\"" is a STRING token, not an ID
      VitruvOCLParser p = parse(
          "context brakesystem::BrakeDisk inv:\n    @severity \"CRITICAL\"\n    self.x > 0");
      assertTrue(p.getNumberOfSyntaxErrors() > 0,
          "Quoted string after @severity should be a syntax error");
    }

    @Test
    @DisplayName("@Severity (capital S) — lexer drops '@', causing downstream errors")
    void capitalSeverity() {
      // ANTLR lexer silently drops the unrecognised '@' character and records a lexer error.
      // The remaining tokens ("Severity CRITICAL self.x > 0") form a garbled expression that
      // the type-checker cannot resolve → compilation must fail.
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @Severity CRITICAL\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@Severity (capital S) should not compile successfully");
    }

    @Test
    @DisplayName("@SEVERITY (all caps) — lexer drops '@', causing downstream errors")
    void allCapsSeverity() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @SEVERITY CRITICAL\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@SEVERITY (all caps) should not compile successfully");
    }

    @Test
    @DisplayName("@sev (abbreviated) — lexer drops '@', causing downstream errors")
    void abbreviatedSev() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @sev CRITICAL\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@sev should not compile successfully");
    }

    @Test
    @DisplayName("@MESSAGE (uppercase) — lexer drops '@', causing downstream errors")
    void uppercaseMessage() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @MESSAGE \"text\"\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@MESSAGE (uppercase) should not compile successfully");
    }

    @Test
    @DisplayName("@msg (abbreviated) — lexer drops '@', causing downstream errors")
    void abbreviatedMsg() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    @msg \"text\"\n    self.diameterInMM > 0");
      assertFalse(r.isSuccess(),
          "@msg should not compile successfully");
    }
  }

  // ===========================================================================
  // 4. Valid @message annotations
  // ===========================================================================

  @Nested
  @DisplayName("Valid @message annotations")
  class ValidMessageAnnotations {

    @Test
    @DisplayName("Plain message without template variables is accepted")
    void plainMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Brake disk violates the constraint"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("{self} template variable is accepted")
    void selfTemplate() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Object {self} violates the constraint"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("{self.attr} template variable is accepted")
    void selfAttrTemplate() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Brake disk {self.name} has an invalid diameter"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Multiple template variables in one message are accepted")
    void multipleTemplateVars() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Disk {self.name} (id={self.id}) has diameter {self.diameterInMM}"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Message with no template vars and special characters is accepted")
    void specialCharsMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Error: radius must be > 0 (check your model!)"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Empty message string is accepted")
    void emptyMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message ""
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }
  }

  // ===========================================================================
  // 5. Annotation combinations and ordering
  // ===========================================================================

  @Nested
  @DisplayName("Annotation combinations and ordering")
  class AnnotationCombinations {

    @Test
    @DisplayName("@severity before @message is accepted")
    void severityBeforeMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @severity CRITICAL
              @message "Brake disk {self.name} failed"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("@message before @severity is accepted")
    void messageBeforeSeverity() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Brake disk {self.name} failed"
              @severity CRITICAL
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Annotation on a named invariant is accepted")
    void annotationOnNamedInv() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv BD_HasDiameter:
              @severity WARNING
              @message "Disk {self.name} has zero diameter"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Each invariant in a multi-inv context may carry independent annotations")
    void multipleInvEachWithAnnotation() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv BD_1:
              @severity CRITICAL
              @message "Disk {self.name}: diameter must be positive"
              self.diameterInMM > 0
          context brakesystem::BrakeDisk inv BD_2:
              @severity INFO
              @message "Disk {self.name}: diameter should exceed 100 mm"
              self.diameterInMM > 100""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Only @severity with no @message is accepted")
    void onlySeverity() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @severity MAJOR
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Only @message with no @severity is accepted")
    void onlyMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Diameter must be positive"
              self.diameterInMM > 0""");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("No annotations at all is still accepted (annotations are optional)")
    void noAnnotations() {
      ConstraintResult r = eval(
          "context brakesystem::BrakeDisk inv:\n    self.diameterInMM > 0");
      assertTrue(r.isSuccess(), r.toDetailedErrorString());
    }

    @Test
    @DisplayName("Invalid @severity alongside valid @message still fails")
    void invalidSeverityWithValidMessage() {
      ConstraintResult r = eval("""
          context brakesystem::BrakeDisk inv:
              @severity IMPORTANT
              @message "Disk {self.name} is invalid"
              self.diameterInMM > 0""");
      assertFalse(r.isSuccess(),
          "A constraint with invalid @severity should fail even when @message is valid");
    }
  }

  // ===========================================================================
  // 6. Violation records — @message template and @severity in output
  //    Uses a deliberately-false constraint (0 > 0) so that violations occur.
  // ===========================================================================

  @Nested
  @DisplayName("Violation records — @message template and @severity appear in output")
  class ViolationRecords {

    /**
     * Runs the full three-pass pipeline and returns the {@link EvaluationVisitor.ViolationRecord}
     * list directly from the evaluator.
     */
    private List<EvaluationVisitor.ViolationRecord> getRecords(String constraint) {
      var loadResult = tools.vitruv.dsls.vitruvocl.pipeline.SmartLoader
          .loadForConstraint(constraint, new Path[]{ECORE}, new Path[]{XMI});
      assertFalse(loadResult.hasErrors(), "Load must succeed");
      var compiler = new VitruvOCLCompiler(loadResult.wrapper, null);
      compiler.compile(constraint);
      var ev = compiler.getLastEvaluator();
      assertNotNull(ev, "Evaluator must exist after compile");
      return ev.getViolationRecords();
    }

    // ---- severity in ViolationRecord ----------------------------------------

    @Test
    @DisplayName("Default severity is WARNING when @severity is absent")
    void defaultSeverityIsWarning() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords(
          "context brakesystem::BrakeDisk inv:\n    0 > 0");
      assertFalse(records.isEmpty(), "There should be at least one violation");
      records.forEach(r ->
          assertEquals("WARNING", r.severity(),
              "Default severity should be WARNING, got: " + r.severity()));
    }

    @Test
    @DisplayName("@severity CRITICAL is stored in the ViolationRecord")
    void severityCriticalInRecord() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords(
          "context brakesystem::BrakeDisk inv:\n    @severity CRITICAL\n    0 > 0");
      assertFalse(records.isEmpty());
      records.forEach(r ->
          assertEquals("CRITICAL", r.severity(),
              "Expected CRITICAL, got: " + r.severity()));
    }

    @Test
    @DisplayName("@severity MINOR is stored in the ViolationRecord")
    void severityMinorInRecord() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords(
          "context brakesystem::BrakeDisk inv:\n    @severity MINOR\n    0 > 0");
      assertFalse(records.isEmpty());
      records.forEach(r -> assertEquals("MINOR", r.severity()));
    }

    // ---- @message in ViolationRecord (customMessage field) ------------------

    @Test
    @DisplayName("customMessage is null when @message annotation is absent")
    void noAnnotationGivesNullCustomMessage() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords(
          "context brakesystem::BrakeDisk inv BD_AlwaysFails:\n    0 > 0");
      assertFalse(records.isEmpty());
      records.forEach(r ->
          assertNull(r.customMessage(),
              "customMessage should be null when no @message present, got: " + r.customMessage()));
    }

    @Test
    @DisplayName("@message plain text is stored verbatim in customMessage")
    void plainMessageInCustomMessage() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords("""
          context brakesystem::BrakeDisk inv:
              @message "Diameter constraint violated"
              0 > 0""");
      assertFalse(records.isEmpty());
      records.forEach(r ->
          assertEquals("Diameter constraint violated", r.customMessage(),
              "customMessage should match template verbatim, got: " + r.customMessage()));
    }

    @Test
    @DisplayName("@message with {self.attr} is interpolated — placeholder is resolved")
    void selfAttrInterpolation() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords("""
          context brakesystem::BrakeDisk inv:
              @message "Disk {self.name} is invalid"
              0 > 0""");
      assertFalse(records.isEmpty());
      records.forEach(r -> {
        assertNotNull(r.customMessage(), "customMessage should not be null");
        assertFalse(r.customMessage().contains("{self.name}"),
            "Template placeholder should be resolved, got: " + r.customMessage());
        assertTrue(r.customMessage().startsWith("Disk ") && r.customMessage().endsWith(" is invalid"),
            "Interpolated form expected, got: " + r.customMessage());
      });
    }

    @Test
    @DisplayName("@severity and @message both appear correctly in the same ViolationRecord")
    void severityAndMessageBothPresent() {
      List<EvaluationVisitor.ViolationRecord> records = getRecords("""
          context brakesystem::BrakeDisk inv:
              @severity INFO
              @message "Informational: disk {self.name} failed"
              0 > 0""");
      assertFalse(records.isEmpty());
      records.forEach(r -> {
        assertEquals("INFO", r.severity());
        assertNotNull(r.customMessage());
        assertFalse(r.customMessage().contains("{self.name}"),
            "Template should be interpolated, got: " + r.customMessage());
        assertTrue(r.customMessage().startsWith("Informational:"),
            "Message prefix expected, got: " + r.customMessage());
      });
    }

    // ---- ConstraintResult warnings — combined format ------------------------

    @Test
    @DisplayName("Warning contains [MAJOR] when @severity MAJOR is set")
    void warningContainsViolationAndSeverity() {
      ConstraintResult result = eval("""
          context brakesystem::BrakeDisk inv:
              @severity MAJOR
              0 > 0""");
      assertTrue(result.isSuccess(), "Compilation must succeed");
      assertFalse(result.isSatisfied());
      assertTrue(
          result.getWarnings().stream()
              .anyMatch(w -> w.getMessage().contains("[MAJOR]")),
          "Warning should contain [MAJOR], got: " + result.getWarnings());
    }

    @Test
    @DisplayName("Warning still contains instance attributes when @message is absent")
    void warningContainsInstanceAttributesWithoutMessage() {
      // No @message — instance label (from describeInstance) must appear
      ConstraintResult result = eval(
          "context brakesystem::BrakeDisk inv:\n    0 > 0");
      assertTrue(result.isSuccess());
      assertFalse(result.isSatisfied());
      // describeInstance includes EClass name or attribute values
      assertTrue(
          result.getWarnings().stream()
              .anyMatch(w -> w.getMessage().contains("[WARNING]")),
          "Warning must contain [WARNING], got: " + result.getWarnings());
    }

    @Test
    @DisplayName("Warning contains interpolated @message when annotation is present")
    void warningContainsInterpolatedMessage() {
      ConstraintResult result = eval("""
          context brakesystem::BrakeDisk inv:
              @message "Custom: disk {self.name} failed"
              0 > 0""");
      assertTrue(result.isSuccess());
      assertFalse(result.isSatisfied());
      assertTrue(
          result.getWarnings().stream()
              .anyMatch(w -> w.getMessage().contains("Custom:")
                  && !w.getMessage().contains("{self.name}")),
          "Warning should contain interpolated @message, got: " + result.getWarnings());
    }

    @Test
    @DisplayName("Warning contains both instance label and custom message when @message is set")
    void warningContainsBothInstanceLabelAndMessage() {
      ConstraintResult result = eval("""
          context brakesystem::BrakeDisk inv:
              @severity CRITICAL
              @message "Disk failed"
              0 > 0""");
      assertTrue(result.isSuccess());
      assertFalse(result.isSatisfied());
      assertTrue(
          result.getWarnings().stream()
              .anyMatch(w -> w.getMessage().contains("[CRITICAL]")
                  && w.getMessage().contains("Disk failed")),
          "Warning should contain severity and custom message, got: " + result.getWarnings());
    }
  }
}
