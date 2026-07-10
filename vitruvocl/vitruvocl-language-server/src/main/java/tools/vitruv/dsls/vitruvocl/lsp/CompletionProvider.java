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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

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

  private static final String OP_ALL_INSTANCES = "allInstances";
  private static final String OP_ALL_INSTANCES_CALL = "allInstances()";
  private static final String ANNOTATION_SEVERITY = "@severity";

  // Regex patterns for context detection (applied to the text before the cursor).
  private static final Pattern MM_COLON_COLON = Pattern.compile("(\\w++)::$");
  private static final Pattern MM_CLASS_DOT = Pattern.compile("(\\w++)::(\\w++)\\.$");
  private static final Pattern SELF_DOT = Pattern.compile("\\bself\\.$");
  private static final Pattern CONTEXT_DECL = Pattern.compile("\\bcontext\\s+(\\w+)::(\\w+)");

  // 'context ' with no '::' yet → suggest metamodel package names.
  private static final Pattern CONTEXT_NEEDS_PKG = Pattern.compile("^\\s*context\\s+\\w*$");
  // 'context Pkg::Class ' without 'inv' yet → only 'inv' makes sense.
  private static final Pattern CONTEXT_NEEDS_INV =
      Pattern.compile("^\\s*context\\s+\\w+::\\w+\\s+\\w*$");

  // Type-position patterns: after "let x :" and after type-cast / type-test operations.
  private static final Pattern LET_TYPE_POS = Pattern.compile("\\blet\\s+\\w+\\s*:\\s*\\w*$");
  private static final Pattern TYPE_CAST_POS =
      Pattern.compile("\\b(oclIsKindOf|oclIsTypeOf|oclAsType)\\s*\\(\\s*\\w*$");

  // Annotation patterns
  private static final Pattern AT_SEVERITY_PREFIX = Pattern.compile("@severity\\s+\\w*$");
  // Matches '@severity ' (with trailing space) — triggers the severity-level list.
  private static final Pattern AT_SEVERITY_SPACE = Pattern.compile("@severity\\s$");
  // Matches a bare '@' or '@<partial>' that could be the start of an annotation keyword.
  // Only fires when there is an 'inv … :' somewhere before the cursor on a prior line.
  private static final Pattern AT_ANNOTATION_START = Pattern.compile("@\\w*$");
  private static final Pattern INV_BEFORE_CURSOR = Pattern.compile("\\binv\\b[^:]*:");
  // Detect already-present annotations in the constraint block before the cursor.
  private static final Pattern HAS_SEVERITY = Pattern.compile("@severity\\s+\\w+");
  private static final Pattern HAS_MESSAGE = Pattern.compile("@message\\s+\"");
  // Non-annotation, non-blank line — signals we are in the OCL body, not the annotation zone.
  private static final Pattern ANNOTATION_LINE = Pattern.compile("^\\s*@(severity|message)\\b");

  // OCL# primitive type names.
  private static final List<String> PRIMITIVE_TYPES =
      List.of("Integer", "Real", "String", "Boolean", "OclAny", "OclVoid");

  // OCL# collection operations: name → insert snippet (uses LSP snippet syntax).
  // Iterator-based operations use "x | $0" to place cursor after the body.
  private static final java.util.Map<String, String> COLLECTION_OPS =
      java.util.Map.ofEntries(
          java.util.Map.entry("select", "select(${1:x} | $0)"),
          java.util.Map.entry("reject", "reject(${1:x} | $0)"),
          java.util.Map.entry("collect", "collect(${1:x} | $0)"),
          java.util.Map.entry("forAll", "forAll(${1:x} | $0)"),
          java.util.Map.entry("exists", "exists(${1:x} | $0)"),
          java.util.Map.entry("sortedBy", "sortedBy(${1:x} | $0)"),
          java.util.Map.entry("includes", "includes($0)"),
          java.util.Map.entry("excludes", "excludes($0)"),
          java.util.Map.entry("including", "including($0)"),
          java.util.Map.entry("excluding", "excluding($0)"),
          java.util.Map.entry("union", "union($0)"),
          java.util.Map.entry("append", "append($0)"),
          java.util.Map.entry("prepend", "prepend($0)"),
          java.util.Map.entry("isEmpty", "isEmpty()"),
          java.util.Map.entry("notEmpty", "notEmpty()"),
          java.util.Map.entry("size", "size()"),
          java.util.Map.entry("flatten", "flatten()"),
          java.util.Map.entry("sum", "sum()"),
          java.util.Map.entry("max", "max()"),
          java.util.Map.entry("min", "min()"),
          java.util.Map.entry("avg", "avg()"),
          java.util.Map.entry("first", "first()"),
          java.util.Map.entry("last", "last()"),
          java.util.Map.entry("reverse", "reverse()"),
          java.util.Map.entry("oclIsKindOf", "oclIsKindOf($0)"),
          java.util.Map.entry("oclIsTypeOf", "oclIsTypeOf($0)"),
          java.util.Map.entry("oclAsType", "oclAsType($0)"));

  private final MetamodelWrapper wrapper;

  /**
   * Creates a completion provider backed by the given metamodel wrapper.
   *
   * @param wrapper metamodel and instance access used to resolve completion candidates
   */
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
    String currentLine = textBefore.substring(textBefore.lastIndexOf('\n') + 1);

    List<Supplier<List<CompletionItem>>> candidates =
        List.of(
            () -> contextPackageCompletions(currentLine),
            () -> contextInvCompletions(currentLine),
            () -> invHeaderGuard(currentLine),
            () -> annotationZoneCompletions(currentLine, textBefore),
            () -> annotationKeywordCompletions(textBefore),
            () -> severityCompletions(textBefore),
            () -> typePositionCompletions(textBefore),
            () -> packageClassNameCompletions(textBefore),
            () -> classFeatureCompletions(textBefore),
            () -> selfFeatureCompletions(textBefore, documentText, cursor),
            () -> dotCompletions(textBefore, cursor, lastAnalysis));

    for (Supplier<List<CompletionItem>> candidate : candidates) {
      List<CompletionItem> result = candidate.get();
      if (result != null) {
        return result;
      }
    }

    // -----------------------------------------------------------------------
    // 5. Top-level: keywords + known package names
    // -----------------------------------------------------------------------
    return topLevelItems();
  }

  /**
   * 'context ' (no '::' yet) → suggest all metamodel package names, or {@code null} if no match.
   */
  private List<CompletionItem> contextPackageCompletions(String currentLine) {
    if (!CONTEXT_NEEDS_PKG.matcher(currentLine).find()) {
      return null;
    }
    List<CompletionItem> items = new ArrayList<>();
    for (String pkgName : wrapper.getAvailableMetamodels()) {
      CompletionItem item = new CompletionItem(pkgName);
      item.setKind(CompletionItemKind.Module);
      item.setDetail("Metamodel package");
      items.add(item);
    }
    return items;
  }

  /** 'context Pkg::Class ' without 'inv' yet → only 'inv', or {@code null} if no match. */
  private List<CompletionItem> contextInvCompletions(String currentLine) {
    if (!CONTEXT_NEEDS_INV.matcher(currentLine).find()) {
      return null;
    }
    CompletionItem inv = new CompletionItem("inv");
    inv.setKind(CompletionItemKind.Keyword);
    inv.setDetail("Introduce a named invariant");
    inv.setInsertText("inv $1:\n  $0");
    inv.setInsertTextFormat(InsertTextFormat.Snippet);
    return List.of(inv);
  }

  /**
   * Guard: never offer completions while the cursor is still on the {@code context Pkg::Class inv
   * name:} header line itself. Returns {@code null} if the cursor is not on such a line.
   */
  private List<CompletionItem> invHeaderGuard(String currentLine) {
    if (currentLine.matches(".*\\binv\\s+\\w+\\s*:.*")) {
      return List.of();
    }
    return null;
  }

  /**
   * Blank line in the annotation zone (between {@code inv …:} and the OCL body) → offer
   * {@code @severity} / {@code @message}. Returns {@code null} if both are already present, so
   * the caller falls through to body completions.
   */
  private List<CompletionItem> annotationZoneCompletions(String currentLine, String textBefore) {
    if (!currentLine.isBlank() || !isInAnnotationZone(textBefore)) {
      return null;
    }
    List<CompletionItem> annItems = annotationKeywordItems(true, textBefore);
    return annItems.isEmpty() ? null : annItems;
  }

  /**
   * {@code @} or {@code @<partial>} after an {@code inv … :} → annotation keyword suggestions,
   * insertText without {@code @} because the user already typed it. Returns {@code null} if no
   * match.
   */
  private List<CompletionItem> annotationKeywordCompletions(String textBefore) {
    if (AT_ANNOTATION_START.matcher(textBefore).find()
        && INV_BEFORE_CURSOR.matcher(textBefore).find()) {
      return annotationKeywordItems(false, textBefore);
    }
    return null;
  }

  /**
   * {@code @severity } (space just typed) or {@code @severity <partial>} → severity level
   * completions, or {@code null} if no match.
   */
  private List<CompletionItem> severityCompletions(String textBefore) {
    if (AT_SEVERITY_SPACE.matcher(textBefore).find()
        || AT_SEVERITY_PREFIX.matcher(textBefore).find()) {
      return severityItems();
    }
    return null;
  }

  /**
   * Type-position: after "let x :" or inside oclIsKindOf/oclAsType/oclIsTypeOf( → primitive type
   * names + all metamodel packages + qualified EClass names, or {@code null} if no match.
   */
  private List<CompletionItem> typePositionCompletions(String textBefore) {
    if (LET_TYPE_POS.matcher(textBefore).find() || TYPE_CAST_POS.matcher(textBefore).find()) {
      return typeItems();
    }
    return null;
  }

  /** {@code PackageName::} → list EClass names, or {@code null} if no match. */
  private List<CompletionItem> packageClassNameCompletions(String textBefore) {
    Matcher mmMatcher = MM_COLON_COLON.matcher(textBefore);
    if (mmMatcher.find()) {
      return classNamesFor(mmMatcher.group(1));
    }
    return null;
  }

  /** {@code PackageName::ClassName.} → list EClass features, or {@code null} if no match. */
  private List<CompletionItem> classFeatureCompletions(String textBefore) {
    Matcher mmClassDotMatcher = MM_CLASS_DOT.matcher(textBefore);
    if (mmClassDotMatcher.find()) {
      EClass eClass = wrapper.resolveEClass(mmClassDotMatcher.group(1), mmClassDotMatcher.group(2));
      if (eClass != null) {
        return featuresFor(eClass);
      }
    }
    return null;
  }

  /** {@code self.} → features of the context class declared above, or {@code null} if no match. */
  private List<CompletionItem> selfFeatureCompletions(
      String textBefore, String documentText, Position cursor) {
    if (!SELF_DOT.matcher(textBefore).find()) {
      return null;
    }
    Matcher ctxMatcher = CONTEXT_DECL.matcher(documentText);
    // Find the last context declaration before the cursor offset.
    int cursorOffset = offsetOf(documentText, cursor);
    String matchedPkg = null;
    String matchedClass = null;
    while (ctxMatcher.find()) {
      if (ctxMatcher.start() <= cursorOffset) {
        matchedPkg = ctxMatcher.group(1);
        matchedClass = ctxMatcher.group(2);
      }
    }
    if (matchedPkg == null) {
      return null;
    }
    EClass eClass = wrapper.resolveEClass(matchedPkg, matchedClass);
    return eClass != null ? featuresFor(eClass) : null;
  }

  /**
   * {@code expr.} → look up the type of the receiver from the last analysis. Returns {@code null}
   * if there is no last analysis to consult (the caller then falls through to top-level items).
   */
  private List<CompletionItem> dotCompletions(
      String textBefore, Position cursor, DocumentAnalysis lastAnalysis) {
    boolean canLookUpType =
        textBefore.endsWith(".") && lastAnalysis != null && lastAnalysis.getNodeTypes() != null;
    if (!canLookUpType) {
      return null;
    }
    List<CompletionItem> fromType = completionsFromType(cursor, lastAnalysis);
    // Fall through to collection ops as default dot-completion.
    return !fromType.isEmpty() ? fromType : collectionOpItems();
  }

  // ---------------------------------------------------------------------------
  // Annotation-zone detection
  // ---------------------------------------------------------------------------

  /**
   * Returns true when the cursor is in the annotation zone of a constraint — i.e. between the
   * {@code inv …:} header and the first real OCL body line. Walking backward through the lines
   * before the cursor, every non-blank line must be an {@code @severity} or {@code @message}
   * annotation until we reach the {@code inv} header. Any other content means we are in the body.
   */
  private boolean isInAnnotationZone(String textBefore) {
    String[] lines = textBefore.split("\n", -1);
    // Start from the line just before the current (blank) line.
    for (int i = lines.length - 2; i >= 0; i--) {
      String line = lines[i];
      if (INV_BEFORE_CURSOR.matcher(line).find()) {
        return true;
      }
      if (line.isBlank() || ANNOTATION_LINE.matcher(line).find()) {
        continue;
      }
      return false; // OCL body line — not in annotation zone
    }
    return false;
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

    // Also suggest common instance-level operations with their parameter signatures.
    // label = readable call form, filterText = bare name so typing "oclAs" still matches.
    for (var entry :
        List.of(
            new String[] {
              "oclIsKindOf",
              "oclIsKindOf($0)",
              "oclIsKindOf(type : OclType) : Boolean",
              "oclIsKindOf(type)"
            },
            new String[] {
              "oclIsTypeOf",
              "oclIsTypeOf($0)",
              "oclIsTypeOf(type : OclType) : Boolean",
              "oclIsTypeOf(type)"
            },
            new String[] {
              "oclAsType", "oclAsType($0)", "oclAsType(type : OclType) : T", "oclAsType(type)"
            },
            new String[] {
              OP_ALL_INSTANCES,
              OP_ALL_INSTANCES_CALL,
              "allInstances() : Set(self)",
              OP_ALL_INSTANCES_CALL
            })) {
      CompletionItem item = new CompletionItem(entry[3]);
      item.setFilterText(entry[0]);
      item.setKind(CompletionItemKind.Method);
      item.setDetail(entry[2]);
      item.setInsertText(entry[1]);
      item.setInsertTextFormat(InsertTextFormat.Snippet);
      items.add(item);
    }

    return items;
  }

  private List<CompletionItem> collectionOpItems() {
    List<CompletionItem> items = new ArrayList<>();
    for (var entry : COLLECTION_OPS.entrySet()) {
      // Label shows the readable signature; filterText ensures typing the name still matches.
      String label =
          entry
              .getValue()
              .replaceAll("\\$\\{\\d+:([^}]+)\\}", "$1") // ${1:x} → x
              .replaceAll("\\$\\d+", ""); // $0 → (removed)
      CompletionItem item = new CompletionItem(label);
      item.setFilterText(entry.getKey());
      item.setKind(CompletionItemKind.Method);
      item.setInsertText(entry.getValue());
      item.setInsertTextFormat(InsertTextFormat.Snippet);
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
  @SuppressWarnings("java:S3776")
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

    // ── Group 1: Metamodel packages ──────────────────────────────────────────
    for (String pkgName : wrapper.getAvailableMetamodels()) {
      CompletionItem item = new CompletionItem(pkgName);
      item.setKind(CompletionItemKind.Module);
      item.setDetail("Metamodel — add :: to list classes");
      item.setSortText("1_" + pkgName);
      items.add(item);
    }

    // ── Group 2: Keywords ────────────────────────────────────────────────────
    for (String kw :
        List.of(
            "self", "let", "in", "if", "then", "else", "endif", "not", "and", "or", "xor",
            "implies", "null")) {
      CompletionItem item = new CompletionItem(kw);
      item.setKind(CompletionItemKind.Keyword);
      item.setSortText("2_" + kw);
      items.add(item);
    }

    // ── Group 2b: Top-level OCL operations ──────────────────────────────────
    CompletionItem allInst = new CompletionItem(OP_ALL_INSTANCES);
    allInst.setKind(CompletionItemKind.Method);
    allInst.setDetail("allInstances() : Set(self)");
    allInst.setInsertText(OP_ALL_INSTANCES_CALL);
    allInst.setInsertTextFormat(InsertTextFormat.Snippet);
    allInst.setSortText("2_" + OP_ALL_INSTANCES);
    items.add(allInst);

    // ── Group 3: Boolean literals ────────────────────────────────────────────
    for (String lit : List.of("true", "false")) {
      CompletionItem item = new CompletionItem(lit);
      item.setKind(CompletionItemKind.Value);
      item.setDetail("Boolean literal");
      item.setSortText("3_" + lit);
      items.add(item);
    }

    // ── Group 4: Collection literals ─────────────────────────────────────────
    for (String col : List.of("Set{}", "Bag{}", "Sequence{}", "OrderedSet{}")) {
      CompletionItem item = new CompletionItem(col);
      item.setKind(CompletionItemKind.Constructor);
      item.setDetail("Collection literal");
      item.setInsertText(col.replace("{}", "{$0}"));
      item.setInsertTextFormat(InsertTextFormat.Snippet);
      item.setSortText("4_" + col);
      items.add(item);
    }

    return items;
  }

  private List<CompletionItem> annotationKeywordItems(boolean includeAt, String textBefore) {
    // Extract only the text after the last 'inv …:' so we don't see annotations from
    // earlier constraints in the same file.
    String blockText = textBefore;
    java.util.regex.Matcher invMatcher = INV_BEFORE_CURSOR.matcher(textBefore);
    int lastInvEnd = 0;
    while (invMatcher.find()) {
      lastInvEnd = invMatcher.end();
    }
    if (lastInvEnd > 0) {
      blockText = textBefore.substring(lastInvEnd);
    }

    boolean severityPresent = HAS_SEVERITY.matcher(blockText).find();
    boolean messagePresent = HAS_MESSAGE.matcher(blockText).find();

    List<CompletionItem> items = new ArrayList<>();

    if (!severityPresent) {
      CompletionItem severity = new CompletionItem(ANNOTATION_SEVERITY);
      severity.setKind(CompletionItemKind.Keyword);
      severity.setDetail("Set violation severity — type a space to choose level");
      severity.setFilterText(ANNOTATION_SEVERITY);
      severity.setInsertText(includeAt ? ANNOTATION_SEVERITY : "severity");
      items.add(severity);
    }

    if (!messagePresent) {
      CompletionItem message = new CompletionItem("@message");
      message.setKind(CompletionItemKind.Keyword);
      message.setDetail("Custom violation message — supports {self} and {self.attr}");
      message.setFilterText("@message");
      message.setInsertText(includeAt ? "@message \"$0\"" : "message \"$0\"");
      message.setInsertTextFormat(InsertTextFormat.Snippet);
      items.add(message);
    }

    return items;
  }

  private List<CompletionItem> severityItems() {
    List<String> levels = List.of("CRITICAL", "WARNING", "MAJOR", "MINOR", "INFO");
    List<CompletionItem> items = new ArrayList<>();
    for (String level : levels) {
      CompletionItem item = new CompletionItem(level);
      item.setKind(CompletionItemKind.EnumMember);
      item.setDetail("Severity level");
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
  private List<CompletionItem> completionsFromType(Position cursor, DocumentAnalysis analysis) {

    // The cursor is right after '.'. We want the type of the expression that ends just before '.'.
    // Find the parse-tree node at (cursor.line, cursor.character - 2) — just before the dot.
    int dotCharPos = cursor.getCharacter() - 1;
    if (dotCharPos < 0) {
      return List.of();
    }

    Position beforeDot = new Position(cursor.getLine(), dotCharPos - 1);
    if (beforeDot.getCharacter() < 0) {
      return List.of();
    }

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
    if (offset <= 0) {
      return "";
    }
    return text.substring(0, offset);
  }

  /** Converts a 0-based LSP Position to an absolute character offset in {@code text}. */
  private static int offsetOf(String text, Position pos) {
    int line = 0;
    int offset = 0;
    while (offset < text.length() && line < pos.getLine()) {
      if (text.charAt(offset) == '\n') {
        line++;
      }
      offset++;
    }
    return Math.min(offset + pos.getCharacter(), text.length());
  }
}
