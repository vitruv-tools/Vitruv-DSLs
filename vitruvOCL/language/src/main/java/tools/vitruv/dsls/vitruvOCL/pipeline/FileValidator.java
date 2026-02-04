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

  /** Extracts package name from .ecore file. */
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

  /** Validates batch of files and collects all errors. */
  public static List<FileError> validateFiles(Path[] files) {
    List<FileError> errors = new ArrayList<>();

    for (Path file : files) {
      validateFile(file).ifPresent(errors::add);
    }

    return errors;
  }
}