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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import tools.vitruv.dsls.vitruvocl.lsp.OclOperationDocs.OperationDoc;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Produces hover cards for OCL# tokens.
 *
 * <p>Priority order:
 *
 * <ol>
 *   <li>Token is a known OCL# operator/operation → rich Javadoc card from {@link OclOperationDocs}
 *   <li>Nearest type annotation is a MetaclassType → EClass info card from the loaded Ecore model
 *   <li>Any other type annotation → plain OCL# type card
 * </ol>
 *
 * <p><b>Where to edit things:</b>
 *
 * <ul>
 *   <li>Operation documentation text → {@link OclOperationDocs}
 *   <li>Operation card visual format → {@link #buildOperationHover}
 *   <li>EClass card visual format → {@link #buildEClassHover}
 *   <li>Plain type card → {@link #buildTypeHover}
 * </ul>
 */
public class HoverProvider {

  private static final String MD_CODE_HEADER = "### `";

  /** Maximum number of inherited features shown before a "+N more" line is added. */
  private static final int MAX_INHERITED_SHOWN = 8;

  @SuppressWarnings("java:S3776")
  public Hover getHover(Position cursor, DocumentAnalysis analysis) {
    if  (analysis == null || analysis.getTree() == null) {
      return null;
    }

    // ------------------------------------------------------------------
    // 0. Diagnostics take priority — if the cursor sits inside an error
    //    or warning squiggle, show the message instead of type info.
    // ------------------------------------------------------------------
    if (analysis.getDiagnostics() != null) {
      List<Diagnostic> hits =
          analysis.getDiagnostics().stream()
              .filter(d -> containsCursor(d.getRange(), cursor))
              .toList();
      if (!hits.isEmpty()) {
        return buildDiagnosticHover(hits);
      }
    }

    ParseTree node = NodeFinder.findAt(analysis.getTree(), cursor.getLine(), cursor.getCharacter());
    if  (node == null) {
      return null;
    }

    // ------------------------------------------------------------------
    // 1. Annotation keywords (@severity / @message)
    // ------------------------------------------------------------------
    if (node instanceof TerminalNode terminal) {
      String tokenText = terminal.getSymbol().getText();
      if ("@severity".equals(tokenText)) {
        return buildAnnotationHover(
            "@severity",
            "Sets the severity of a constraint violation. Placed after the invariant `:` before the"
                + " body.",
            "**Values:** `CRITICAL` | `WARNING` *(default)* | `MAJOR` | `MINOR` | `INFO`",
            "@severity CRITICAL\nself.radius > 0");
      }
      if ("@message".equals(tokenText)) {
        return buildAnnotationHover(
            "@message",
            "Custom violation message. Supports `{self}` and `{self.attr}` template variables.",
            "**Template variables:** `{self}` — the context object · `{self.attr}` — an attribute"
                + " value",
            "@message \"Brake disk {self.name} has no radius defined\"");
      }
    }

    // ------------------------------------------------------------------
    // 2. Known operation keyword / method name
    // ------------------------------------------------------------------
    if (node instanceof TerminalNode terminal2) {
      String tokenText = terminal2.getSymbol().getText();
      Optional<OperationDoc> doc = OclOperationDocs.lookup(tokenText);
      if  (doc.isPresent()) {
        return buildOperationHover(doc.get());
      }
    }

    // ------------------------------------------------------------------
    // 2. Walk up the parse tree looking for a type annotation
    // ------------------------------------------------------------------
    if  (analysis.getNodeTypes() == null) {
      return null;
    }

    ParseTreeProperty<Type> nodeTypes = analysis.getNodeTypes();
    ParseTree current = node;
    while (current != null) {
      Type type = nodeTypes.get(current);
      if (type != null) {
        // 2a. MetaclassType → show full EClass card
        if (type.isMetaclassType() && type.getEClass() != null) {
          return buildEClassHover(type.getEClass());
        }
        // 2b. Any other type → plain type card
        return buildTypeHover(type);
      }
      current = current.getParent();
    }

    return null;
  }

  private static Hover buildEClassHover(EClass eClass) {
    StringBuilder sb = new StringBuilder();

    String pkg = eClass.getEPackage() != null ? eClass.getEPackage().getName() : "?";

    // ── Header ──────────────────────────────────────────────────────────────
    sb.append(MD_CODE_HEADER).append(pkg).append("::").append(eClass.getName()).append("`");
    if (eClass.isInterface()) {
      sb.append(" *interface*");
    } else if (eClass.isAbstract()) {
      sb.append(" *abstract*");
    }
    sb.append("\n\n");

    sb.append("EClass · package **").append(pkg).append("**\n");

    // ── EAnnotation documentation (GenModel) ────────────────────────────────
    String doc = genModelDoc(eClass.getEAnnotations());
    if (doc != null) {
      sb.append("\n> ").append(doc.replace("\n", "\n> ")).append("\n");
    }

    // ── Superclasses ─────────────────────────────────────────────────────────
    List<EClass> supers = eClass.getESuperTypes();
    if (!supers.isEmpty()) {
      sb.append("\n**Superclasses:** ");
      sb.append(
          supers.stream().map(s -> "`" + s.getName() + "`").collect(Collectors.joining(", ")));
      sb.append("\n");
    }

    // ── Own features ─────────────────────────────────────────────────────────
    List<EStructuralFeature> own = eClass.getEStructuralFeatures();
    if (!own.isEmpty()) {
      sb.append("\n**Own Features:**\n");
      for (EStructuralFeature f : own) {
        sb.append(featureLine(f));
      }
    }

    // ── Inherited features ───────────────────────────────────────────────────
    List<EStructuralFeature> inherited =
        eClass.getEAllStructuralFeatures().stream()
            .filter(f -> f.getEContainingClass() != eClass)
            .toList();
    if (!inherited.isEmpty()) {
      sb.append("\n**Inherited Features:**\n");
      int shown = Math.min(inherited.size(), MAX_INHERITED_SHOWN);
      for (int i = 0; i < shown; i++) {
        sb.append(featureLine(inherited.get(i)));
      }
      if (inherited.size() > MAX_INHERITED_SHOWN) {
        sb.append("- *… and ").append(inherited.size() - MAX_INHERITED_SHOWN).append(" more*\n");
      }
    }

    return hover(sb.toString());
  }

  /** One bullet line for an EStructuralFeature. */
  private static String featureLine(EStructuralFeature f) {
    String typeName = f.getEType() != null ? f.getEType().getName() : "?";
    String kind = f instanceof EReference ? "ref" : "attr";
    String mult = multiplicity(f);

    StringBuilder line = new StringBuilder("- `");
    line.append(f.getName()).append(" : ").append(typeName).append(" ").append(mult).append("`");
    line.append(" *(").append(kind).append(")*");

    // EAnnotation doc on individual features
    String doc = genModelDoc(f.getEAnnotations());
    if (doc != null) {
      // Keep it short: first sentence only
      String firstSentence = doc.contains(".") ? doc.substring(0, doc.indexOf('.') + 1) : doc;
      line.append(" — ").append(firstSentence.trim());
    }

    line.append("\n");
    return line.toString();
  }

  /** Converts EMF lower/upper bounds to a readable multiplicity string. */
  private static String multiplicity(EStructuralFeature f) {
    int lo = f.getLowerBound();
    int hi = f.getUpperBound();
    if  (lo == 1 && hi == 1) {
      return "[1]";
    }
    if  (lo == 0 && hi == 1) {
      return "[0..1]";
    }
    if  (lo == 0 && hi == -1) {
      return "[0..*]";
    }
    if  (lo == 1 && hi == -1) {
      return "[1..*]";
    }
    return "[" + lo + ".." + (hi == -1 ? "*" : hi) + "]";
  }

  /**
   * Extracts the {@code documentation} value from a GenModel EAnnotation, or {@code null} if none
   * exists. The standard GenModel annotation source is {@code
   * http://www.eclipse.org/emf/2002/GenModel}.
   */
  private static String genModelDoc(Iterable<EAnnotation> annotations) {
    for (EAnnotation ann : annotations) {
      if ("http://www.eclipse.org/emf/2002/GenModel".equals(ann.getSource())) {
        String doc = ann.getDetails().get("documentation");
        if  (doc != null && !doc.isBlank()) {
          return doc.strip();
        }
      }
    }
    return null;
  }

  private static Hover buildOperationHover(OperationDoc doc) {
    StringBuilder sb = new StringBuilder();

    sb.append(MD_CODE_HEADER).append(doc.signature()).append("`\n\n");
    sb.append(doc.description()).append("\n");

    if (!doc.params().isEmpty()) {
      sb.append("\n");
      for (var p : doc.params()) {
        sb.append("**@param** `")
            .append(p.name())
            .append("` : *")
            .append(p.type())
            .append("*")
            .append(" — ")
            .append(p.description())
            .append("  \n");
      }
    }

    if (doc.returnDescription() != null && !doc.returnDescription().isBlank()) {
      sb.append("\n**@return** ").append(doc.returnDescription());
    }

    return hover(sb.toString());
  }

  private static Hover buildTypeHover(Type type) {
    StringBuilder sb = new StringBuilder();
    sb.append("**Type:** `").append(type.getTypeName()).append("`");

    if (type.isCollection()) {
      sb.append("\n\n*Collection — use `.select`, `.reject`, `.collect`, …*");
    } else if (type.isOptional()) {
      sb.append("\n\n*Optional value — may be absent*");
    }

    return hover(sb.toString());
  }

  // ---------------------------------------------------------------------------

  private static Hover buildAnnotationHover(
      String keyword, String description, String details, String example) {
    String md =
        MD_CODE_HEADER
            + keyword
            + "`\n\n"
            + description
            + "\n\n"
            + details
            + "\n\n"
            + "**Example:**\n```ocl\n"
            + example
            + "\n```";
    return hover(md);
  }

  private static Hover buildDiagnosticHover(List<Diagnostic> hits) {
    StringBuilder sb = new StringBuilder();
    for (Diagnostic d : hits) {
      String icon =
          switch (d.getSeverity()) {
            case Error -> "$(error)";
            case Warning -> "$(warning)";
            default -> "$(info)";
          };
      sb.append(icon).append(" **").append(d.getMessage()).append("**\n\n");
    }
    return hover(sb.toString().stripTrailing());
  }

  /** Returns true when {@code range} contains {@code cursor} (end is exclusive). */
  private static boolean containsCursor(Range range, Position cursor) {
    if  (range == null || cursor == null) {
      return false;
    }
    Position start = range.getStart();
    Position end = range.getEnd();
    if  (cursor.getLine() < start.getLine() || cursor.getLine() > end.getLine()) {
      return false;
    }
    if (cursor.getLine() == start.getLine() && cursor.getCharacter() < start.getCharacter()) {
      return false;
    }
    return !(cursor.getLine() == end.getLine() && cursor.getCharacter() >= end.getCharacter());
  }

  // ---------------------------------------------------------------------------

  private static Hover hover(String markdown) {
    MarkupContent content = new MarkupContent();
    content.setKind(MarkupKind.MARKDOWN);
    content.setValue(markdown);
    return new Hover(content);
  }
}
