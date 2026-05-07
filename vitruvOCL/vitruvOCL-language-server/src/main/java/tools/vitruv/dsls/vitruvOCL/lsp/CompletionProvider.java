/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Builds context-sensitive completion lists for OCL# documents.
 *
 * <h2>Detected contexts (in priority order)</h2>
 *
 * <ol>
 *   <li>{@code PackageName::} → all EClass names in that EPackage (loaded from the registry)
 *   <li>{@code PackageName::ClassName.} → all EStructuralFeatures of that EClass (incl. inherited)
 *   <li>{@code self.} → features of the context class declared by the nearest {@code context} stmt
 *   <li>{@code expr.} where the receiver type is known from the last analysis → features / ops
 *   <li>After {@code .} on a collection type → OCL collection operations
 *   <li>Top-level → OCL# keywords + all known package names
 * </ol>
 *
 * <p>All metamodel lookups go through {@link MetamodelWrapper#getEPackage} which in turn reflects
 * whatever {@code .ecore} files were loaded into the server on startup — exactly the same registry
 * used by the type checker.
 */
public class CompletionProvider {

  // Regex patterns for context detection (applied to the text before the cursor).
  private static final Pattern MM_COLON_COLON = Pattern.compile("(\\w+)::$");
  private static final Pattern MM_CLASS_DOT = Pattern.compile("(\\w+)::(\\w+)\\.$");
  private static final Pattern SELF_DOT = Pattern.compile("\\bself\\.$");
  private static final Pattern CONTEXT_DECL = Pattern.compile("\\bcontext\\s+(\\w+)::(\\w+)");

  // Type-position patterns: after "let x :" and after type-cast / type-test operations.
  private static final Pattern LET_TYPE_POS = Pattern.compile("\\blet\\s+\\w+\\s*:\\s*\\w*$");
  private static final Pattern TYPE_CAST_POS =
      Pattern.compile("\\b(oclIsKindOf|oclIsTypeOf|oclAsType)\\s*\\(\\s*\\w*$");

  // OCL# primitive type names.
  private static final List<String> PRIMITIVE_TYPES =
      List.of("Integer", "Real", "String", "Boolean", "OclAny", "OclVoid");

  // OCL# collection operations shown after any collection-typed receiver.
  private static final List<String> COLLECTION_OPS =
      List.of(
          "select",
          "reject",
          "collect",
          "forAll",
          "exists",
          "includes",
          "excludes",
          "including",
          "excluding",
          "isEmpty",
          "notEmpty",
          "size",
          "flatten",
          "union",
          "append",
          "prepend",
          "sum",
          "max",
          "min",
          "avg",
          "first",
          "last",
          "reverse",
          "sortedBy",
          "oclIsKindOf",
          "oclIsTypeOf",
          "oclAsType");

  // Top-level keywords always offered when no specific context is detected.
  private static final List<String> TOP_LEVEL_KEYWORDS =
      List.of(
          "self",
          "allInstances",
          "let",
          "in",
          "if",
          "then",
          "else",
          "endif",
          "null",
          "true",
          "false",
          "not",
          "and",
          "or",
          "xor",
          "implies");

  private final MetamodelWrapper wrapper;

  public CompletionProvider(MetamodelWrapper wrapper) {
    this.wrapper = wrapper;
  }

  /**
   * Returns completion items for the given cursor position.
   *
   * @param documentText full document text
   * @param cursor LSP cursor position (0-based line and character)
   * @param lastAnalysis most recent analysis snapshot, or {@code null} on first edit
   */
  public List<CompletionItem> getCompletions(
      String documentText, Position cursor, DocumentAnalysis lastAnalysis) {

    String textBefore = textBefore(documentText, cursor);

    // -----------------------------------------------------------------------
    // 0. Type-position: after "let x :" or inside oclIsKindOf/oclAsType/oclIsTypeOf(
    //    → primitive type names + all metamodel packages + qualified EClass names
    // -----------------------------------------------------------------------
    if (LET_TYPE_POS.matcher(textBefore).find() || TYPE_CAST_POS.matcher(textBefore).find()) {
      return typeItems();
    }

    // -----------------------------------------------------------------------
    // 1. PackageName:: → list EClass names
    // -----------------------------------------------------------------------
    Matcher mmMatcher = MM_COLON_COLON.matcher(textBefore);
    if (mmMatcher.find()) {
      String pkgName = mmMatcher.group(1);
      return classNamesFor(pkgName);
    }

    // -----------------------------------------------------------------------
    // 2. PackageName::ClassName. → list EClass features
    // -----------------------------------------------------------------------
    Matcher mmClassDotMatcher = MM_CLASS_DOT.matcher(textBefore);
    if (mmClassDotMatcher.find()) {
      EClass eClass = wrapper.resolveEClass(mmClassDotMatcher.group(1), mmClassDotMatcher.group(2));
      if (eClass != null) {
        return featuresFor(eClass);
      }
    }

    // -----------------------------------------------------------------------
    // 3. self. → features of the context class declared above
    // -----------------------------------------------------------------------
    if (SELF_DOT.matcher(textBefore).find()) {
      Matcher ctxMatcher = CONTEXT_DECL.matcher(documentText);
      // Find the last context declaration before the cursor offset.
      int cursorOffset = offsetOf(documentText, cursor);
      String matchedPkg = null, matchedClass = null;
      while (ctxMatcher.find()) {
        if (ctxMatcher.start() <= cursorOffset) {
          matchedPkg = ctxMatcher.group(1);
          matchedClass = ctxMatcher.group(2);
        }
      }
      if (matchedPkg != null) {
        EClass eClass = wrapper.resolveEClass(matchedPkg, matchedClass);
        if (eClass != null) {
          return featuresFor(eClass);
        }
      }
    }

    // -----------------------------------------------------------------------
    // 4. expr. → look up the type of the receiver from the last analysis
    // -----------------------------------------------------------------------
    if (textBefore.endsWith(".") && lastAnalysis != null && lastAnalysis.getNodeTypes() != null) {
      List<CompletionItem> fromType = completionsFromType(textBefore, cursor, lastAnalysis);
      if (!fromType.isEmpty()) {
        return fromType;
      }
      // Fall through to collection ops as default dot-completion.
      return collectionOpItems();
    }

    // -----------------------------------------------------------------------
    // 5. Top-level: keywords + known package names
    // -----------------------------------------------------------------------
    return topLevelItems();
  }

  // ---------------------------------------------------------------------------
  // Context-specific builders
  // ---------------------------------------------------------------------------

  private List<CompletionItem> classNamesFor(String pkgName) {
    EPackage pkg = wrapper.getEPackage(pkgName);
    if (pkg == null) {
      // Package not loaded yet — return nothing so the user sees the issue.
      return List.of();
    }
    List<CompletionItem> items = new ArrayList<>();
    for (EClassifier classifier : pkg.getEClassifiers()) {
      if (classifier instanceof EClass eClass) {
        CompletionItem item = new CompletionItem(eClass.getName());
        item.setKind(CompletionItemKind.Class);
        item.setDetail("EClass in " + pkgName);
        if (eClass.isAbstract()) {
          item.setDetail("abstract EClass in " + pkgName);
        }
        items.add(item);
      }
    }
    return items;
  }

  private List<CompletionItem> featuresFor(EClass eClass) {
    List<CompletionItem> items = new ArrayList<>();

    // eClass.getEAllStructuralFeatures() includes inherited features.
    for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
      CompletionItem item = new CompletionItem(feature.getName());
      item.setKind(CompletionItemKind.Field);

      String typeName = feature.getEType() != null ? feature.getEType().getName() : "?";
      boolean isMany = feature.isMany();
      item.setDetail((isMany ? "Collection(" : "") + typeName + (isMany ? ")" : ""));
      items.add(item);
    }

    // Also suggest common instance-level operations.
    for (String op : List.of("oclIsKindOf", "oclIsTypeOf", "oclAsType", "allInstances")) {
      CompletionItem item = new CompletionItem(op);
      item.setKind(CompletionItemKind.Method);
      items.add(item);
    }

    return items;
  }

  private List<CompletionItem> collectionOpItems() {
    List<CompletionItem> items = new ArrayList<>();
    for (String op : COLLECTION_OPS) {
      CompletionItem item = new CompletionItem(op);
      item.setKind(CompletionItemKind.Method);
      items.add(item);
    }
    return items;
  }

  /**
   * Returns type-completion items for positions where an OCL# type name is expected (e.g. after
   * {@code let x :} or inside {@code oclIsKindOf(}).
   *
   * <p>Three tiers, each with a distinct sort prefix so VS Code lists them in order:
   *
   * <ol>
   *   <li>OCL# primitive types ({@code Integer}, {@code String}, …) — sort prefix {@code "0"}
   *   <li>Metamodel package names ({@code JavaMM}, …) — useful as namespace prefix before {@code
   *       ::} — sort prefix {@code "1"}
   *   <li>Fully-qualified EClass names ({@code JavaMM::Class}, …) — sort prefix {@code "2"}
   * </ol>
   */
  private List<CompletionItem> typeItems() {
    List<CompletionItem> items = new ArrayList<>();

    // Tier 1 — primitive types.
    for (String typeName : PRIMITIVE_TYPES) {
      CompletionItem item = new CompletionItem(typeName);
      item.setKind(CompletionItemKind.Class);
      item.setDetail("OCL# primitive type");
      item.setSortText("0" + typeName);
      items.add(item);
    }

    // Tiers 2 & 3 — metamodel packages and their EClass names.
    for (String pkgName : wrapper.getAvailableMetamodels()) {
      // Package name alone — typing '::' after it triggers the class-name list (case 1 above).
      CompletionItem pkgItem = new CompletionItem(pkgName);
      pkgItem.setKind(CompletionItemKind.Module);
      pkgItem.setDetail("Metamodel package — add :: to list classes");
      pkgItem.setSortText("1" + pkgName);
      items.add(pkgItem);

      // Fully-qualified class names so the user can pick them in one step.
      EPackage pkg = wrapper.getEPackage(pkgName);
      if (pkg != null) {
        for (EClassifier classifier : pkg.getEClassifiers()) {
          if (classifier instanceof EClass eClass) {
            String qualifiedName = pkgName + "::" + eClass.getName();
            CompletionItem classItem = new CompletionItem(qualifiedName);
            classItem.setKind(CompletionItemKind.Class);
            classItem.setDetail(eClass.isAbstract() ? "abstract EClass" : "EClass");
            classItem.setSortText("2" + qualifiedName);
            items.add(classItem);
          }
        }
      }
    }

    return items;
  }

  private List<CompletionItem> topLevelItems() {
    List<CompletionItem> items = new ArrayList<>();

    // Keywords.
    for (String kw : TOP_LEVEL_KEYWORDS) {
      CompletionItem item = new CompletionItem(kw);
      item.setKind(CompletionItemKind.Keyword);
      items.add(item);
    }

    // All registered package names — useful as namespace prefixes (e.g. JavaMM::).
    for (String pkgName : wrapper.getAvailableMetamodels()) {
      CompletionItem item = new CompletionItem(pkgName);
      item.setKind(CompletionItemKind.Module);
      item.setDetail("Metamodel package");
      items.add(item);
    }

    return items;
  }

  // ---------------------------------------------------------------------------
  // Type-based completion via last analysis
  // ---------------------------------------------------------------------------

  /**
   * Tries to determine the receiver type by finding the parse-tree node just before the trailing
   * {@code .} and looking up its type in the last analysis.
   */
  private List<CompletionItem> completionsFromType(
      String textBefore, Position cursor, DocumentAnalysis analysis) {

    // The cursor is right after '.'. We want the type of the expression that ends just before '.'.
    // Find the parse-tree node at (cursor.line, cursor.character - 2) — just before the dot.
    int dotCharPos = cursor.getCharacter() - 1;
    if (dotCharPos < 0) return List.of();

    Position beforeDot = new Position(cursor.getLine(), dotCharPos - 1);
    if (beforeDot.getCharacter() < 0) return List.of();

    ParseTreeProperty<Type> nodeTypes = analysis.getNodeTypes();
    ParseTree node =
        NodeFinder.findAt(analysis.getTree(), beforeDot.getLine(), beforeDot.getCharacter());

    while (node != null) {
      Type type = nodeTypes.get(node);
      if (type != null) {
        if (type.isCollection()) {
          return collectionOpItems();
        }
        // Singleton metaclass type — offer its structural features.
        if (type.isMetaclassType() && type.getEClass() != null) {
          return featuresFor(type.getEClass());
        }
        break;
      }
      node = node.getParent();
    }

    return List.of();
  }

  // ---------------------------------------------------------------------------
  // Utilities
  // ---------------------------------------------------------------------------

  /** Returns the substring of {@code text} from the start up to (but not including) the cursor. */
  private static String textBefore(String text, Position cursor) {
    int offset = offsetOf(text, cursor);
    if (offset <= 0) return "";
    return text.substring(0, offset);
  }

  /** Converts a 0-based LSP Position to an absolute character offset in {@code text}. */
  private static int offsetOf(String text, Position pos) {
    int line = 0;
    int offset = 0;
    while (offset < text.length() && line < pos.getLine()) {
      if (text.charAt(offset) == '\n') line++;
      offset++;
    }
    return Math.min(offset + pos.getCharacter(), text.length());
  }
}
