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
package tools.vitruv.dsls.vitruvocl.pipeline;

import java.util.*;
import org.antlr.v4.runtime.*;
import tools.vitruv.dsls.vitruvocl.*;

/**
 * Analyzes VitruvOCL constraints to extract metamodel dependencies.
 *
 * <p>Identifies required EPackages by detecting fully qualified names using the {@code ::}
 * namespace separator (e.g., {@code spacecraft::Spacecraft}). Used during constraint validation to
 * ensure all referenced metamodels are registered in the VSUM before evaluation.
 *
 * <p>Uses token-based analysis for robustness - extracts package names even from syntactically
 * invalid constraints, falling back to regex if lexing fails.
 */
public class DependencyAnalyzer {

  private DependencyAnalyzer() {}

  /**
   * Extracts all package names referenced in a constraint.
   *
   * <p>Detects patterns like {@code packageName::ClassName} and returns the set of unique package
   * identifiers. Uses ANTLR lexer for primary analysis with regex fallback for error resilience.
   *
   * @param constraint OCL constraint expression to analyze
   * @return Set of package names (empty if none found)
   */
  public static Set<String> analyzeConstraint(String constraint) {
    Set<String> requiredPackages = new HashSet<>();

    try {
      CharStream input = CharStreams.fromString(constraint);
      VitruvOCLLexer lexer = new VitruvOCLLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      tokens.fill();

      List<Token> tokenList = tokens.getTokens();
      for (int i = 0; i < tokenList.size() - 1; i++) {
        Token current = tokenList.get(i);
        Token next = tokenList.get(i + 1);

        // Pattern: ID followed by ::
        if (next.getType() == VitruvOCLLexer.COLONCOLON
            && !isPrecededByComparisonOrLogicalOp(tokenList, i)) {
          requiredPackages.add(current.getText());
        }
      }

    } catch (Exception e) {
      // Fallback to regex if parsing fails
      java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\w+)::");
      java.util.regex.Matcher matcher = pattern.matcher(constraint);
      while (matcher.find()) {
        requiredPackages.add(matcher.group(1));
      }
    }

    return requiredPackages;
  }

  /**
   * Returns {@code true} if the token at {@code index} is immediately preceded (ignoring hidden
   * channel tokens) by a comparison or logical operator token.
   *
   * <p>Used to detect enum-literal references such as {@code p.unit == Unit::MM}: there the
   * {@code "Unit"} token is preceded by {@code "=="} and must not be treated as a metamodel
   * package name.
   */
  private static boolean isPrecededByComparisonOrLogicalOp(List<Token> tokens, int index) {
    // Walk backwards, skipping whitespace / hidden-channel tokens
    for (int j = index - 1; j >= 0; j--) {
      Token t = tokens.get(j);
      if (t.getChannel() != Token.DEFAULT_CHANNEL) continue; // skip hidden tokens
      String txt = t.getText();
      // Only equality operators reliably indicate an enum-literal value context.
      // Single '=', '<', '>', 'and', 'or', etc. may appear in let-bindings or
      // other positions where the next ID::ID IS a metamodel reference.
      return txt.equals("==") || txt.equals("!=");
    }
    return false;
  }
}
