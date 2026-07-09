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
import java.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Resolves go-to-definition requests for OCL# tokens.
 *
 * <p>When the cursor sits on a metaclass reference (e.g. {@code satelliteSystem::Satellite}), the
 * resolved {@link EClass} is already attached as a type annotation on the parse tree. This provider
 * reads that annotation, extracts the source {@code .ecore} file via {@link EClass#eResource()},
 * then does a lightweight text search for {@code name="ClassName"} to pinpoint the exact line so VS
 * Code opens the file at the right location.
 *
 * <p>Only metaclass references are supported; for other tokens {@code null} is returned and VS Code
 * shows its default "no definition found" message.
 */
public class DefinitionProvider {

  private static final Logger LOG = Logger.getLogger(DefinitionProvider.class.getName());

  /**
   * Returns the definition {@link Location} for the token at {@code cursor}, or {@code null} if no
   * definition can be found (i.e. the token is not a known metaclass reference).
   */
  public Location getDefinition(Position cursor, DocumentAnalysis analysis) {
    if (analysis == null || analysis.getTree() == null) return null;
    if (analysis.getNodeTypes() == null) return null;

    ParseTree node = NodeFinder.findAt(analysis.getTree(), cursor.getLine(), cursor.getCharacter());
    if (node == null) return null;

    // Walk up the parse tree looking for a MetaclassType annotation
    ParseTreeProperty<Type> nodeTypes = analysis.getNodeTypes();
    ParseTree current = node;
    while (current != null) {
      Type type = nodeTypes.get(current);
      if (type != null) {
        if (type.isMetaclassType() && type.getEClass() != null) {
          return locationFor(type.getEClass());
        }
        return null; // first annotation found but not a metaclass — stop searching
      }
      current = current.getParent();
    }

    return null;
  }

  // ---------------------------------------------------------------------------

  private static Location locationFor(EClass eClass) {
    Resource resource = eClass.eResource();
    if (resource == null) return null;

    org.eclipse.emf.common.util.URI emfUri = resource.getURI();
    if (emfUri == null) return null;

    String filePath = emfUri.toFileString();
    if (filePath == null) return null;

    Path ecorePath = Path.of(filePath);
    String lspUri = ecorePath.toUri().toString();

    Range range = findClassRange(ecorePath, eClass.getName());
    return new Location(lspUri, range != null ? range : zeroRange());
  }

  /**
   * Scans {@code ecoreFile} line by line for the attribute {@code name="className"} and returns a
   * {@link Range} that covers only the bare class name (inside the quotes).
   *
   * <p>This is reliable because every {@code eClassifiers} element in an Ecore XMI file must carry
   * {@code name="…"} as one of its attributes on the same element line.
   */
  private static Range findClassRange(Path ecoreFile, String className) {
    if (className == null || className.isEmpty()) return null;
    String needle = "name=\"" + className + "\"";
    try {
      List<String> lines = Files.readAllLines(ecoreFile);
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        int idx = line.indexOf(needle);
        if (idx >= 0) {
          // Position the cursor at the opening char of the class name (after name=")
          int nameStart = idx + "name=\"".length();
          int nameEnd = nameStart + className.length();
          return new Range(new Position(i, nameStart), new Position(i, nameEnd));
        }
      }
    } catch (IOException e) {
      LOG.fine(
          "[OCL-LS] Could not read ecore file for definition: "
              + ecoreFile
              + ": "
              + e.getMessage());
    }
    return null;
  }

  private static Range zeroRange() {
    return new Range(new Position(0, 0), new Position(0, 0));
  }
}


