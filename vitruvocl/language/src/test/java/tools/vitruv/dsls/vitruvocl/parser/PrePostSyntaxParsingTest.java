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

package tools.vitruv.dsls.vitruvocl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;

/**
 * Grammar-level (M1) tests for the pre/post-constraints extension: {@code pre}/{@code post}
 * blocks, the {@code @pre} postfix operator, the {@code OCLisNew}/{@code OCLisModified}/{@code
 * OCLisDeleted} lifecycle predicates (including their named-argument aggregate call form), and
 * the {@code context Type::operation(params): ReturnType} header.
 *
 * <p>These tests exercise parsing only (zero syntax errors and full input consumption).
 * Type-checking and evaluation semantics are covered separately (see the {@code
 * typechecker}/{@code evaluator} packages).
 *
 * <p><b>Why not {@link VitruvOCLParserTestUtils#parseString}:</b> the grammar's {@code
 * contextDeclCS} entry rule is not EOF-anchored ({@code classifierContextCS+}, no trailing {@code
 * EOF}), which is deliberate elsewhere in this codebase — {@code TypeCheckVisitor}'s
 * "missing operand"/"missing operator" diagnostics (see {@code reportMissingOperandAfter}) rely on
 * ANTLR being able to stop early at a malformed tail without raising a hard syntax error, so a
 * later pass can produce a more specific message. That means {@code parseString} alone would
 * accept e.g. {@code "context Person pre: true post: true"} today by silently parsing just
 * {@code "context Person"} and ignoring the rest. {@link #parseFullyConsumed} additionally checks
 * that parsing reached the actual end of input, without changing the shared grammar/utility.
 */
class PrePostSyntaxParsingTest {

  /**
   * Parses {@code input} and asserts that the parser consumed every token (not just a leading
   * prefix). Throws {@link IllegalStateException} on any ANTLR syntax error (mirroring {@link
   * VitruvOCLParserTestUtils#parseString}) or if trailing, unconsumed input remains.
   */
  private static ParseTree parseFullyConsumed(String input) {
    CharStream cs = CharStreams.fromString(input);
    VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {
            throw new IllegalStateException(
                "Parse error at " + line + ":" + charPositionInLine + " - " + msg, e);
          }
        });

    ParseTree tree = parser.contextDeclCS();

    tokens.fill();
    if (tokens.LA(1) != Token.EOF) {
      Token next = tokens.LT(1);
      throw new IllegalStateException(
          "Parse stopped before consuming the full input; unparsed content starts with '"
              + next.getText()
              + "' at "
              + next.getLine()
              + ":"
              + next.getCharPositionInLine());
    }

    return tree;
  }

  @Nested
  @DisplayName("pre/post blocks")
  class PreAndPostBlocks {

    @Test
    @DisplayName("Should parse bare pre and post blocks")
    void testBarePreAndPost() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person pre: self.age > 0 post: self.age > 0"));
    }

    @Test
    @DisplayName("Should parse named pre and post blocks")
    void testNamedPreAndPost() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person pre p1: self.age > 0 post p1: self.age > 0"));
    }

    @Test
    @DisplayName("Should parse inv, pre and post coexisting in one context")
    void testInvPreAndPostCoexist() {
      assertDoesNotThrow(
          () ->
              parseFullyConsumed(
                  "context Person inv: self.age > 0 pre: true post: true"));
    }
  }

  @Nested
  @DisplayName("@pre postfix operator")
  class PreOperator {

    @Test
    @DisplayName("Should parse @pre directly after a property access")
    void testPreAfterPropertyAccess() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person post: self.attr = self.attr@pre + 1"));
    }

    @Test
    @DisplayName("Should parse @pre after a longer navigation chain")
    void testPreAfterLongerNavigationChain() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person post: self.child.attr@pre = 5"));
    }
  }

  @Nested
  @DisplayName("OCLisNew / OCLisModified / OCLisDeleted lifecycle predicates")
  class LifecyclePredicates {

    @Test
    @DisplayName("Should parse bare OCLisNew")
    void testBareOclIsNew() {
      assertDoesNotThrow(() -> parseFullyConsumed("context Person post: self.OCLisNew"));
    }

    @Test
    @DisplayName("Should parse bare OCLisModified")
    void testBareOclIsModified() {
      assertDoesNotThrow(() -> parseFullyConsumed("context Person post: self.OCLisModified"));
    }

    @Test
    @DisplayName("Should parse bare OCLisDeleted")
    void testBareOclIsDeleted() {
      assertDoesNotThrow(() -> parseFullyConsumed("context Person post: self.OCLisDeleted"));
    }

    @Test
    @DisplayName("Should parse OCLisNew aggregate with two named arguments")
    void testOclIsNewAggregateTwoArgs() {
      assertDoesNotThrow(
          () ->
              parseFullyConsumed(
                  "context Person post: self.OCLisNew(age => 5, name => \"x\")"));
    }

    @Test
    @DisplayName("Should parse OCLisNew aggregate with a single (partial) named argument")
    void testOclIsNewAggregatePartial() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person post: self.OCLisNew(age => 5)"));
    }

    @Test
    @DisplayName("Should parse OCLisModified aggregate with a nested @pre in the value expression")
    void testOclIsModifiedAggregateWithNestedPre() {
      assertDoesNotThrow(
          () ->
              parseFullyConsumed(
                  "context Person post: self.OCLisModified(age => self.age@pre + 1)"));
    }
  }

  @Nested
  @DisplayName("context Type::operation(params): ReturnType header")
  class OperationContextHeader {

    @Test
    @DisplayName("Should parse an operation context with one parameter")
    void testOperationContextWithOneParam() {
      assertDoesNotThrow(
          () ->
              parseFullyConsumed(
                  "context Person::birthday(years: Integer) pre: years > 0"
                      + " post: self.age = self.age@pre + years"));
    }

    @Test
    @DisplayName("Should parse an operation context with zero parameters")
    void testOperationContextWithZeroParams() {
      assertDoesNotThrow(
          () -> parseFullyConsumed("context Person::birthday() pre: true post: true"));
    }

    @Test
    @DisplayName("Should parse a qualified operation context with a return type")
    void testQualifiedOperationContextWithReturnType() {
      assertDoesNotThrow(
          () ->
              parseFullyConsumed(
                  "context spaceMission::Spacecraft::launch(dest: String): Boolean"
                      + " pre: true post: true"));
    }
  }

  @Nested
  @DisplayName("Malformed pre/post syntax")
  class MalformedSyntax {

    @Test
    @DisplayName("OCLisDeleted must not accept an aggregate argument list")
    void testOclIsDeletedRejectsAggregate() {
      assertThrows(
          IllegalStateException.class,
          () -> parseFullyConsumed("context Person post: self.OCLisDeleted(x => 1)"));
    }

    @Test
    @DisplayName("Aggregate argument missing '=>' is a syntax error")
    void testAggregateMissingArrow() {
      // Note: "self.OCLisNew(age 5)" (space, no comma) is deliberately NOT used here — without a
      // comma, "(age 5)" is itself a syntactically valid nestedExpCS ('(' expCS+ ')' accepts a
      // juxtaposed sequence of expressions with no separator), so the parser would happily
      // reinterpret it as a second, unrelated top-level expression in the post-block's
      // specificationCS rather than reporting an error. The comma below rules out that escape
      // hatch (no comma-accepting expression form exists outside the aggregate arg list).
      assertThrows(
          IllegalStateException.class,
          () -> parseFullyConsumed("context Person post: self.OCLisNew(age, 5)"));
    }

    @Test
    @DisplayName("Aggregate argument missing attribute name is a syntax error")
    void testAggregateMissingAttrName() {
      assertThrows(
          IllegalStateException.class,
          () -> parseFullyConsumed("context Person post: self.OCLisNew(=> 5)"));
    }

    @Test
    @DisplayName("Aggregate argument missing value is a syntax error")
    void testAggregateMissingValue() {
      assertThrows(
          IllegalStateException.class,
          () -> parseFullyConsumed("context Person post: self.OCLisNew(age =>)"));
    }
  }
}
