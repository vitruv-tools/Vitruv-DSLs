/*******************************************************************************
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

import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;

/**
 * Unit tests for {@link SymbolProvider}.
 *
 * <p>Parse trees are built directly from the ANTLR parser without a full metamodel — the symbol
 * provider only traverses the parse tree structure and does not need type information.
 */
class SymbolProviderTest {

  private SymbolProvider provider;

  @BeforeEach
  void setUp() {
    provider = new SymbolProvider();
  }

  /** Creates a {@link DocumentAnalysis} from raw OCL text using only the ANTLR parser. */
  private static DocumentAnalysis parse(String ocl) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(ocl));
    lexer.removeErrorListeners();
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    parser.removeErrorListeners();
    return new DocumentAnalysis(parser.contextDeclCS(), null, List.of());
  }

  @Test
  void nullAnalysis_returnsEmptyList() {
    assertThat(provider.getSymbols(null)).isEmpty();
  }

  @Test
  void emptyDocument_returnsEmptyList() {
    DocumentAnalysis analysis = parse("");

    assertThat(provider.getSymbols(analysis)).isEmpty();
  }

  @Test
  void singleContext_withNamedInvariant_producesClassAndPropertySymbol() {
    String ocl = "context MM::MyClass inv myRule: self = self";
    DocumentAnalysis analysis = parse(ocl);

    List<Either<SymbolInformation, DocumentSymbol>> symbols = provider.getSymbols(analysis);

    assertThat(symbols).hasSize(1);
    DocumentSymbol classSymbol = symbols.get(0).getRight();
    assertThat(classSymbol.getKind()).isEqualTo(SymbolKind.Class);
    assertThat(classSymbol.getName()).isEqualTo("MM::MyClass");

    assertThat(classSymbol.getChildren()).hasSize(1);
    DocumentSymbol invSymbol = classSymbol.getChildren().get(0);
    assertThat(invSymbol.getKind()).isEqualTo(SymbolKind.Property);
    assertThat(invSymbol.getName()).isEqualTo("myRule");
  }

  @Test
  void anonymousInvariant_appearsAsAnonymousChild() {
    String ocl = "context MM::MyClass inv: self = self";
    DocumentAnalysis analysis = parse(ocl);

    List<Either<SymbolInformation, DocumentSymbol>> symbols = provider.getSymbols(analysis);

    assertThat(symbols).hasSize(1);
    DocumentSymbol classSymbol = symbols.get(0).getRight();
    assertThat(classSymbol.getChildren()).hasSize(1);
    assertThat(classSymbol.getChildren().get(0).getName()).isEqualTo("<anonymous>");
  }

  @Test
  void multipleContextBlocks_produceMultipleClassSymbols() {
    String ocl = """
        context MM::ClassA inv ruleA: self = self
        context MM::ClassB inv ruleB: self = self
        """;
    DocumentAnalysis analysis = parse(ocl);

    List<Either<SymbolInformation, DocumentSymbol>> symbols = provider.getSymbols(analysis);

    assertThat(symbols).hasSize(2);
    assertThat(symbols.get(0).getRight().getName()).isEqualTo("MM::ClassA");
    assertThat(symbols.get(1).getRight().getName()).isEqualTo("MM::ClassB");
  }

  @Test
  void multipleInvariantsInOneContext_allAppearAsChildren() {
    // All invs on one line to avoid grammar ambiguity with multi-line specificationCS
    // "first" is an OCL keyword — use non-keyword names to avoid lexer conflict
    String ocl = "context MM::MyClass inv ruleA: 1 = 1 inv ruleB: 1 = 1 inv ruleC: 1 = 1";
    DocumentAnalysis analysis = parse(ocl);

    List<Either<SymbolInformation, DocumentSymbol>> symbols = provider.getSymbols(analysis);

    assertThat(symbols).hasSize(1);
    List<DocumentSymbol> children = symbols.get(0).getRight().getChildren();
    assertThat(children).hasSize(3);
    assertThat(children).extracting(DocumentSymbol::getName)
        .containsExactlyInAnyOrder("ruleA", "ruleB", "ruleC");
  }

  @Test
  void symbolRanges_areNonNegative() {
    String ocl = "context MM::MyClass inv check: self = self";
    DocumentAnalysis analysis = parse(ocl);

    List<Either<SymbolInformation, DocumentSymbol>> symbols = provider.getSymbols(analysis);

    DocumentSymbol classSymbol = symbols.get(0).getRight();
    assertThat(classSymbol.getRange().getStart().getLine()).isGreaterThanOrEqualTo(0);
    assertThat(classSymbol.getRange().getEnd().getLine()).isGreaterThanOrEqualTo(0);
    assertThat(classSymbol.getSelectionRange().getStart().getLine()).isGreaterThanOrEqualTo(0);
  }
}
