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

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link SignatureHelpProvider}.
 *
 * <p>All tests are pure text-based — no metamodel or parse tree needed.
 */
class SignatureHelpProviderTest {

  private SignatureHelpProvider provider;

  @BeforeEach
  void setUp() {
    provider = new SignatureHelpProvider();
  }

  @Test
  void nullDocument_returnsNull() {
    assertThat(provider.getSignatureHelp(null, new Position(0, 0))).isNull();
  }

  @Test
  void outsideAnyCall_returnsNull() {
    String text = "context MM::Foo inv check: self.mass > 0";
    // Cursor at end, no open parenthesis
    Position cursor = new Position(0, text.length());

    assertThat(provider.getSignatureHelp(text, cursor)).isNull();
  }

  @Test
  void selectOperation_returnsSignatureHelp() {
    String text = "context MM::Foo inv x: self.items->select(";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getSignatures()).isNotEmpty();
    assertThat(help.getSignatures().get(0).getLabel()).contains("select");
  }

  @Test
  void selectOperation_cursorBeforePipe_firstParamActive() {
    // Cursor is inside the iterator variable declaration (before '|')
    String text = "context MM::Foo inv x: self.items->select(x ";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getActiveParameter()).isZero();
  }

  @Test
  void selectOperation_cursorAfterPipe_secondParamActive() {
    String text = "context MM::Foo inv x: self.items->select(x | ";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getActiveParameter()).isEqualTo(1);
  }

  @Test
  void substringOperation_firstParamActive_atStart() {
    String text = "context MM::Foo inv x: self.name.substring(";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getSignatures().get(0).getLabel()).contains("substring");
    assertThat(help.getActiveParameter()).isZero();
  }

  @Test
  void substringOperation_secondParamActive_afterComma() {
    String text = "context MM::Foo inv x: self.name.substring(1, ";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getActiveParameter()).isEqualTo(1);
  }

  @Test
  void unknownOperation_returnsNull() {
    // 'fooBarBaz' is not in OclOperationDocs
    String text = "context MM::Foo inv x: self.fooBarBaz(";
    Position cursor = new Position(0, text.length());

    assertThat(provider.getSignatureHelp(text, cursor)).isNull();
  }

  @ParameterizedTest
  @CsvSource({
      "forAll, 0",
      "exists, 0",
      "collect, 0",
      "reject, 0"
  })
  void iteratorOperations_firstParamActiveOnOpen(String opName, int expectedParam) {
    String text = "context MM::Foo inv x: self.items->" + opName + "(";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getActiveParameter()).isEqualTo(expectedParam);
  }

  @Test
  void nestedParens_innermostCallIdentified() {
    // The active call should be 'substring', not 'select'
    String text = "context MM::Foo inv x: self.items->select(x | x.name.substring(";
    Position cursor = new Position(0, text.length());

    SignatureHelp help = provider.getSignatureHelp(text, cursor);

    assertThat(help).isNotNull();
    assertThat(help.getSignatures().get(0).getLabel()).contains("substring");
  }
}
