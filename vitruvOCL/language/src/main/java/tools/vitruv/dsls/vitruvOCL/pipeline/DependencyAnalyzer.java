package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.*;
import org.antlr.v4.runtime.*;
import tools.vitruv.dsls.vitruvOCL.*;

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
        if (next.getType() == VitruvOCLLexer.COLONCOLON) {
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
}