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

package tools.vitruv.dsls.vitruvocl.vitruv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.framework.vsum.constraints.ConstraintDiagnostic;
import tools.vitruv.framework.vsum.constraints.ConstraintSeverity;
import tools.vitruv.framework.vsum.constraints.InvalidConstraintException;

/**
 * Splits the content of a {@code .ocl} file into single invariants, each of which can be evaluated
 * on its own.
 */
final class OclInvariantParser {

  /**
   * A single invariant with its own context declaration.
   *
   * @param name the invariant name, {@code null} if the invariant is unnamed
   * @param contextType the context type as written, e.g. {@code shop::Item}
   * @param severity the value of the {@code @severity} annotation
   * @param source a complete constraint consisting of the context declaration and the invariant
   */
  record ParsedInvariant(
      String name, String contextType, ConstraintSeverity severity, String source) {}

  private OclInvariantParser() {}

  /**
   * Parses the given constraints.
   *
   * @param content the content of a {@code .ocl} file
   * @return the invariants in declaration order
   * @throws InvalidConstraintException if the content contains syntax errors or unknown severities
   */
  static List<ParsedInvariant> parse(String content) {
    CharStream input = CharStreams.fromString(withoutImports(content));
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    List<ConstraintDiagnostic> syntaxErrors = new ArrayList<>();
    BaseErrorListener collector =
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int column,
              String message,
              RecognitionException exception) {
            syntaxErrors.add(new ConstraintDiagnostic(line, column, message));
          }
        };
    lexer.removeErrorListeners();
    lexer.addErrorListener(collector);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    if (tokens.LT(1).getType() == Token.EOF && syntaxErrors.isEmpty()) {
      // empty or comment-only content
      return List.of();
    }
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(collector);

    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();
    // the grammar does not require EOF, so the parser silently stops before unparsable input
    Token next = tokens.LT(1);
    if (next.getType() != Token.EOF) {
      syntaxErrors.add(
          new ConstraintDiagnostic(
              next.getLine(),
              next.getCharPositionInLine(),
              "unexpected input '" + next.getText() + "'"));
    }
    if (!syntaxErrors.isEmpty()) {
      throw new InvalidConstraintException(syntaxErrors);
    }

    List<ParsedInvariant> invariants = new ArrayList<>();
    for (VitruvOCLParser.ClassifierContextCSContext context : tree.classifierContextCS()) {
      List<VitruvOCLParser.InvCSContext> invs = context.invCS();
      if (invs.isEmpty()) {
        continue;
      }
      String header =
          text(input, context.start.getStartIndex(), invs.get(0).start.getStartIndex());
      String contextType = contextType(context);
      for (VitruvOCLParser.InvCSContext inv : invs) {
        String invariant = text(input, inv.start.getStartIndex(), inv.stop.getStopIndex() + 1);
        String name = inv.ID() != null ? inv.ID().getText() : null;
        invariants.add(
            new ParsedInvariant(
                name, contextType, severity(inv), header.strip() + "\n  " + invariant.strip()));
      }
    }
    return invariants;
  }

  /** Removes import declarations, keeping the line count so that positions stay valid. */
  static String withoutImports(String content) {
    StringBuilder result = new StringBuilder();
    for (String line : content.split("\\R", -1)) {
      // keep the line count so that positions in error messages match the original content
      result.append(line.strip().startsWith("import ") ? "" : line).append('\n');
    }
    return result.toString();
  }

  private static String text(CharStream input, int start, int endExclusive) {
    return input.getText(Interval.of(start, endExclusive - 1));
  }

  private static String contextType(VitruvOCLParser.ClassifierContextCSContext context) {
    if (context.metamodel != null && context.className != null) {
      return context.metamodel.getText() + "::" + context.className.getText();
    }
    return context.contextName != null ? context.contextName.getText() : null;
  }

  private static ConstraintSeverity severity(VitruvOCLParser.InvCSContext inv) {
    for (VitruvOCLParser.AnnotationCSContext annotation : inv.annotationCS()) {
      if (annotation instanceof VitruvOCLParser.SeverityAnnotationContext severity) {
        String value = severity.severityValue.getText();
        try {
          // the type checker accepts exactly the upper-case names of the severity levels
          return ConstraintSeverity.valueOf(value);
        } catch (IllegalArgumentException unknownSeverity) {
          throw new InvalidConstraintException(
              List.of(
                  new ConstraintDiagnostic(
                      severity.start.getLine(),
                      severity.start.getCharPositionInLine(),
                      "unknown severity '"
                          + value
                          + "', valid values are "
                          + Arrays.toString(ConstraintSeverity.values()))));
        }
      }
    }
    return ConstraintSeverity.WARNING;
  }

  /**
   * Maps a severity reported by the VitruvOCL evaluator to the Vitruv severity, falling back to
   * {@link ConstraintSeverity#WARNING} for unknown values.
   *
   * @param value the severity of a violation, may be {@code null}
   * @return the severity
   */
  static ConstraintSeverity toSeverity(String value) {
    if (value == null) {
      return ConstraintSeverity.WARNING;
    }
    try {
      return ConstraintSeverity.valueOf(value);
    } catch (IllegalArgumentException unknownSeverity) {
      return ConstraintSeverity.WARNING;
    }
  }
}
