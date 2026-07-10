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

import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Unit tests for {@link CompletionProvider}.
 *
 * <p>Uses a minimal in-memory Ecore model injected via a {@link MetamodelWrapper} subclass so that
 * tests are independent of file system state and do not require Mockito.
 */
class CompletionProviderTest {

  private CompletionProvider provider;
  private EPackage testPackage;
  private EClass fooClass;

  @BeforeEach
  void setUp() {
    EcoreFactory factory = EcoreFactory.eINSTANCE;

    testPackage = factory.createEPackage();
    testPackage.setName("TestMM");
    testPackage.setNsPrefix("testmm");
    testPackage.setNsURI("http://testmm/1.0");

    fooClass = factory.createEClass();
    fooClass.setName("Foo");

    EAttribute barAttr = factory.createEAttribute();
    barAttr.setName("bar");
    fooClass.getEStructuralFeatures().add(barAttr);

    testPackage.getEClassifiers().add(fooClass);

    MetamodelWrapper wrapper = stubWrapper(testPackage, fooClass);
    provider = new CompletionProvider(wrapper);
  }

  /**
   * Creates a {@link MetamodelWrapper} stub backed by the given in-memory {@link EPackage}. Avoids
   * file I/O and does not require Mockito.
   */
  private static MetamodelWrapper stubWrapper(EPackage pkg, EClass cls) {
    return new MetamodelWrapper() {
      @Override
      public Set<String> getAvailableMetamodels() {
        return Set.of(pkg.getName());
      }

      @Override
      public EPackage getEPackage(String name) {
        return pkg.getName().equals(name) ? pkg : null;
      }

      @Override
      public EClass resolveEClass(String mm, String className) {
        if (pkg.getName().equals(mm) && cls.getName().equals(className)) return cls;
        return null;
      }
    };
  }

  // ── context keyword completions ────────────────────────────────────────────

  @Test
  void contextKeywordWithSpace_suggestsPackageNames() {
    String text = "context ";
    Position cursor = cursorAtEnd(text);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).extracting(CompletionItem::getLabel).contains("TestMM");
    assertThat(items).extracting(CompletionItem::getKind).containsOnly(CompletionItemKind.Module);
  }

  @Test
  void contextWithPackageAndClass_suggestsInvKeyword() {
    String text = "context TestMM::Foo ";
    Position cursor = cursorAtEnd(text);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).hasSize(1);
    assertThat(items.get(0).getLabel()).isEqualTo("inv");
    assertThat(items.get(0).getKind()).isEqualTo(CompletionItemKind.Keyword);
  }

  // ── package:: completions ──────────────────────────────────────────────────

  @Test
  void packageColonColon_suggestsClassNames() {
    String text = "context TestMM::";
    Position cursor = cursorAtEnd(text);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).extracting(CompletionItem::getLabel).contains("Foo");
    assertThat(items).extracting(CompletionItem::getKind).containsOnly(CompletionItemKind.Class);
  }

  @Test
  void unknownPackageColonColon_returnsEmptyList() {
    String text = "context Unknown::";
    Position cursor = cursorAtEnd(text);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).isEmpty();
  }

  // ── feature completions ────────────────────────────────────────────────────

  @Test
  void packageClassDot_suggestsFeatures() {
    // Expression must be on a line without an inv header to avoid the header-line guard
    // cursor must be AFTER the dot (offset 14 = 2 spaces + "TestMM::Foo.")
    String text = "context TestMM::Foo inv x:\n  TestMM::Foo.";
    Position cursor = new Position(1, 14);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).extracting(CompletionItem::getLabel).anyMatch(label -> label.contains("bar"));
  }

  @Test
  void selfDot_suggestsFeaturesOfContextClass() {
    String text = "context TestMM::Foo inv x:\n  self.";
    Position cursor = new Position(1, 7);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).extracting(CompletionItem::getLabel).anyMatch(label -> label.contains("bar"));
  }

  // ── annotation completions ─────────────────────────────────────────────────

  @Test
  void atSeveritySpace_suggestsSeverityLevels() {
    // "@severity " is 10 chars; with 2-space indent, char 12 is right after the trailing space
    String text = "context TestMM::Foo inv x:\n  @severity ";
    Position cursor = new Position(1, 12);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items)
        .extracting(CompletionItem::getLabel)
        .containsExactlyInAnyOrder("CRITICAL", "WARNING", "MAJOR", "MINOR", "INFO");
  }

  @Test
  void atAnnotationStart_afterInv_suggestsAnnotationKeywords() {
    String text = "context TestMM::Foo inv x:\n  @";
    Position cursor = new Position(1, 3);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items)
        .extracting(CompletionItem::getLabel)
        .anyMatch(label -> label.contains("severity"));
  }

  // ── top-level completions ──────────────────────────────────────────────────

  @Test
  void topLevel_containsKeywordsAndPackageNames() {
    String text = "";
    Position cursor = new Position(0, 0);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    List<String> labels = items.stream().map(CompletionItem::getLabel).toList();
    assertThat(labels).contains("self", "let", "if", "true", "false", "TestMM");
  }

  @Test
  void topLevel_containsCollectionLiterals() {
    String text = "";
    Position cursor = new Position(0, 0);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    assertThat(items).extracting(CompletionItem::getKind).contains(CompletionItemKind.Constructor);
  }

  // ── let / type-cast position completions ──────────────────────────────────

  @Test
  void letTypePosition_suggestsPrimitiveAndMetamodelTypes() {
    String text = "context TestMM::Foo inv x:\n  let v : ";
    Position cursor = new Position(1, 11);

    List<CompletionItem> items = provider.getCompletions(text, cursor, null);

    List<String> labels = items.stream().map(CompletionItem::getLabel).toList();
    assertThat(labels).contains("Integer", "String", "Boolean", "Real");
    assertThat(labels).contains("TestMM");
  }

  // ── utilities ─────────────────────────────────────────────────────────────

  private static Position cursorAtEnd(String text) {
    String[] lines = text.split("\n", -1);
    int lastLine = lines.length - 1;
    return new Position(lastLine, lines[lastLine].length());
  }
}
