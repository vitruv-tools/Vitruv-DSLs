/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Integration tests for {@link DocumentAnalyzer}.
 *
 * <p>Uses the spaceMission metamodel from the language module's test resources so that type-level
 * diagnostics can be verified alongside syntax diagnostics.
 */
class DocumentAnalyzerTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("../language/src/test/resources/test-metamodels/spaceMission.ecore");

  private DocumentAnalyzer analyzer;

  @BeforeEach
  void setUp() throws IOException {
    MetamodelWrapper wrapper = new MetamodelWrapper();
    wrapper.loadMetamodel(SPACEMISSION_ECORE);
    analyzer = new DocumentAnalyzer(wrapper);
  }

  @Test
  void validOcl_producesNoDiagnostics() {
    String ocl = "context spaceMission::Spacecraft inv hasName: self.name.size() > 0";

    DocumentAnalysis result = analyzer.analyze(ocl);

    assertThat(result).isNotNull();
    assertThat(result.getTree()).isNotNull();
    assertThat(result.getDiagnostics()).isEmpty();
  }

  @Test
  void syntaxError_producesErrorDiagnosticAtCorrectLine() {
    // 'kontext' is not a valid keyword — should produce a syntax error on line 0 (0-based)
    String ocl = "kontext spaceMission::Spacecraft inv bad: self.name <> null";

    DocumentAnalysis result = analyzer.analyze(ocl);

    assertThat(result.getDiagnostics()).isNotEmpty();
    Diagnostic first = result.getDiagnostics().get(0);
    assertThat(first.getSeverity()).isEqualTo(DiagnosticSeverity.Error);
    assertThat(first.getRange().getStart().getLine()).isZero();
  }

  @Test
  void typeError_producesErrorDiagnostic() {
    // Comparing Integer with String is a type error
    String ocl = "context spaceMission::Spacecraft inv bad: self.mass = \"not-a-number\"";

    DocumentAnalysis result = analyzer.analyze(ocl);

    List<Diagnostic> errors =
        result.getDiagnostics().stream()
            .filter(d -> d.getSeverity() == DiagnosticSeverity.Error)
            .toList();
    assertThat(errors).isNotEmpty();
  }

  @Test
  void annotationKeywordTypo_producesDiagnosticWithSuggestion() {
    // '@severit' is close to '@severity' — should be flagged with a quick-fix suggestion
    String ocl =
        """
        context spaceMission::Spacecraft inv check:
          @severit WARNING
          self.mass > 0
        """;

    DocumentAnalysis result = analyzer.analyze(ocl);

    List<Diagnostic> typos =
        result.getDiagnostics().stream()
            .filter(d -> d.getMessage().contains("Did you mean"))
            .toList();
    assertThat(typos).isNotEmpty();
    assertThat(typos.get(0).getData()).isEqualTo("@severity");
  }

  @Test
  void importLines_areStrippedAndLineNumbersPreserved() {
    // The import line must be stripped; the constraint starts on line 1 (0-based).
    // A syntax error on line 2 (0-based) must still point to line 2, not line 1.
    String ocl =
        """
        import spaceMission.ecore
        context spaceMission::Spacecraft inv check:
          INVALID_EXPRESSION_@@@
        """;

    DocumentAnalysis result = analyzer.analyze(ocl);

    assertThat(result.getDiagnostics()).isNotEmpty();
    // All diagnostics must be on line >= 1 (the import line was line 0 and must be invisible)
    result
        .getDiagnostics()
        .forEach(d -> assertThat(d.getRange().getStart().getLine()).isGreaterThan(0));
  }

  @Test
  void emptyDocument_doesNotThrow() {
    DocumentAnalysis result = analyzer.analyze("");

    assertThat(result).isNotNull();
    assertThat(result.getDiagnostics()).isNotNull();
  }

  @Test
  void multipleConstraints_allValidated() {
    String ocl =
        """
        context spaceMission::Spacecraft inv massPositive: self.mass > 0
        context spaceMission::Spacecraft inv nameNotEmpty: self.name.size() > 0
        """;

    DocumentAnalysis result = analyzer.analyze(ocl);

    assertThat(result.getDiagnostics()).isEmpty();
    assertThat(result.getTree().classifierContextCS()).hasSize(2);
  }

  @Test
  void diagnosticRange_isZeroBasedAndNonNegative() {
    String ocl = "badkeyword Something::Foo inv x: 1 = 2";

    DocumentAnalysis result = analyzer.analyze(ocl);

    result
        .getDiagnostics()
        .forEach(
            d -> {
              assertThat(d.getRange().getStart().getLine()).isGreaterThanOrEqualTo(0);
              assertThat(d.getRange().getStart().getCharacter()).isGreaterThanOrEqualTo(0);
              assertThat(d.getRange().getEnd().getLine()).isGreaterThanOrEqualTo(0);
              assertThat(d.getRange().getEnd().getCharacter()).isGreaterThanOrEqualTo(0);
            });
  }
}
