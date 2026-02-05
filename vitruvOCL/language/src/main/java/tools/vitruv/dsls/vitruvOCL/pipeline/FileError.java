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

import java.nio.file.Path;

/**
 * Represents file system errors encountered during constraint processing.
 *
 * <p>Captures errors that prevent constraint files from being read or parsed, including missing
 * files, permission issues, and format problems. Used in {@link ConstraintResult} to distinguish
 * file-level failures from compilation or runtime errors.
 */
public class FileError {
  /** Path to the problematic file */
  private final Path path;

  /** Category of file error */
  private final FileErrorType type;

  /** Detailed error description */
  private final String message;

  /** Categories of file-related errors. */
  public enum FileErrorType {
    /** File does not exist at specified path */
    NOT_FOUND("File not found"),

    /** File exists but cannot be read (permissions, locks) */
    NOT_READABLE("File is not readable"),

    /** File content does not match expected format */
    INVALID_FORMAT("Invalid file format"),

    /** Error occurred while parsing file content */
    PARSE_ERROR("Error parsing file");

    private final String description;

    FileErrorType(String description) {
      this.description = description;
    }

    /**
     * @return Human-readable description of error type
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Creates a file error with full details.
   *
   * @param path Path to the problematic file
   * @param type Category of error
   * @param message Detailed error description
   */
  public FileError(Path path, FileErrorType type, String message) {
    this.path = path;
    this.type = type;
    this.message = message;
  }

  /**
   * @return Path to the file that caused the error
   */
  public Path getPath() {
    return path;
  }

  /**
   * @return Error category
   */
  public FileErrorType getType() {
    return type;
  }

  /**
   * @return Detailed error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Formats error as human-readable string.
   *
   * @return String in format "ErrorType: path - message"
   */
  @Override
  public String toString() {
    return String.format("%s: %s - %s", type.getDescription(), path, message);
  }
}