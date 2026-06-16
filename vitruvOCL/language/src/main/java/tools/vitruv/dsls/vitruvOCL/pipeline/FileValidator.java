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
package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/** Validates files and extracts metadata. */
public class FileValidator {

  /** Validates file existence and readability. */
  public static Optional<FileError> validateFile(Path path) {
    if (!Files.exists(path)) {
      return Optional.of(
          new FileError(path, FileError.FileErrorType.NOT_FOUND, "File does not exist"));
    }

    if (!Files.isReadable(path)) {
      return Optional.of(
          new FileError(path, FileError.FileErrorType.NOT_READABLE, "File is not readable"));
    }

    return Optional.empty();
  }

  /** Extracts the root package name from .ecore file. */
  public static String extractPackageNameFromEcore(Path ecorePath) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(ecorePath.toFile());
      Element root = doc.getDocumentElement();
      String name = root.getAttribute("name");

      if (name == null || name.isEmpty()) {
        throw new IOException("Ecore file missing 'name' attribute: " + ecorePath);
      }

      return name;
    } catch (Exception e) {
      throw new IOException("Failed to parse ecore file: " + ecorePath, e);
    }
  }

  /**
   * Extracts the names of the root package and all of its nested {@code eSubpackages}, since
   * model instances reference sub-package names directly (e.g. Palladio PCM's {@code repository}
   * and {@code allocation} sub-packages of {@code pcm.ecore}), not just the top-level name.
   */
  public static Set<String> extractAllPackageNamesFromEcore(Path ecorePath) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(ecorePath.toFile());
      Element root = doc.getDocumentElement();
      String rootName = root.getAttribute("name");

      if (rootName == null || rootName.isEmpty()) {
        throw new IOException("Ecore file missing 'name' attribute: " + ecorePath);
      }

      Set<String> names = new LinkedHashSet<>();
      names.add(rootName);

      NodeList subPackages = root.getElementsByTagName("eSubpackages");
      for (int i = 0; i < subPackages.getLength(); i++) {
        Element subPackage = (Element) subPackages.item(i);
        String subName = subPackage.getAttribute("name");
        if (subName != null && !subName.isEmpty()) {
          names.add(subName);
        }
      }

      return names;
    } catch (Exception e) {
      throw new IOException("Failed to parse ecore file: " + ecorePath, e);
    }
  }

  /** Extracts package name from .xmi file. */
  public static String extractPackageNameFromXmi(Path xmiPath) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(xmiPath.toFile());
      Element root = doc.getDocumentElement();
      String tagName = root.getTagName();

      // Format: "packageName:ClassName" or "ClassName"
      if (tagName.contains(":")) {
        return tagName.split(":")[0];
      }

      // Fallback: check xmlns attributes
      NamedNodeMap attrs = root.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        String attrName = attr.getNodeName();
        if (attrName.startsWith("xmlns:") && !attrName.equals("xmlns:xmi")) {
          return attrName.substring(6); // Remove "xmlns:"
        }
      }

      throw new IOException("Cannot determine package name from XMI: " + xmiPath);
    } catch (Exception e) {
      throw new IOException("Failed to parse XMI file: " + xmiPath, e);
    }
  }

  /**
   * Extracts all package names referenced by an .xmi file's root element: the root tag's own
   * namespace prefix (if any) plus every {@code xmlns:xxx} declaration on the root.
   *
   * <p>PCM instance files are heterogeneous — e.g. a {@code repository:Repository} root can
   * contain deeply nested {@code xsi:type="seff:..."} elements (inline SEFFs on a
   * {@code BasicComponent}), so the package(s) actually instantiated in the file are not limited
   * to the root tag's own package. Every {@code xmlns:xxx} declaration on the root corresponds to
   * a package that {@code xsi:type} references inside the document may use.
   */
  public static Set<String> extractAllPackageNamesFromXmi(Path xmiPath) throws IOException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(xmiPath.toFile());
      Element root = doc.getDocumentElement();
      String tagName = root.getTagName();

      Set<String> names = new LinkedHashSet<>();
      if (tagName.contains(":")) {
        names.add(tagName.split(":")[0]);
      }

      NamedNodeMap attrs = root.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        String attrName = attr.getNodeName();
        if (attrName.startsWith("xmlns:") && !attrName.equals("xmlns:xmi")
            && !attrName.equals("xmlns:xsi")) {
          names.add(attrName.substring(6)); // Remove "xmlns:"
        }
      }

      if (names.isEmpty()) {
        throw new IOException("Cannot determine package name from XMI: " + xmiPath);
      }

      return names;
    } catch (Exception e) {
      throw new IOException("Failed to parse XMI file: " + xmiPath, e);
    }
  }

  /** Validates batch of files and collects all errors. */
  public static List<FileError> validateFiles(Path[] files) {
    List<FileError> errors = new ArrayList<>();

    for (Path file : files) {
      validateFile(file).ifPresent(errors::add);
    }

    return errors;
  }
}
