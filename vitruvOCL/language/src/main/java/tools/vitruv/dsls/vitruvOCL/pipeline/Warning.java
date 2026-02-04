package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.nio.file.Path;

/**
 * Non-fatal warning issued during constraint processing.
 *
 * <p>Warnings indicate potential issues that don't prevent evaluation: duplicate definitions,
 * unused files, or constraint violations.
 */
public class Warning {
  private final WarningType type;
  private final String message;
  private final Path affectedFile;

  /** Categories of warnings. */
  public enum WarningType {
    /** Same constraint appears multiple times */
    DUPLICATE_CONSTRAINT("Duplicate constraint"),

    /** Same metamodel loaded from multiple files */
    DUPLICATE_METAMODEL("Duplicate metamodel"),

    /** Same model instance loaded multiple times */
    DUPLICATE_MODEL("Duplicate model instance"),

    /** Metamodel file not referenced by any constraint */
    UNUSED_METAMODEL("Unused metamodel"),

    /** No instances found for required metamodel */
    UNUSED_MODEL("Unused model instance"),

    /** Constraint evaluated but was not satisfied */
    CONSTRAINT_VIOLATION("Constraint violation");

    private final String description;

    WarningType(String description) {
      this.description = description;
    }

    /**
     * @return Human-readable warning category
     */
    public String getDescription() {
      return description;
    }
  }

  /**
   * Creates warning without associated file.
   *
   * @param type Warning category
   * @param message Detailed warning message
   */
  public Warning(WarningType type, String message) {
    this(type, message, null);
  }

  /**
   * Creates warning with associated file.
   *
   * @param type Warning category
   * @param message Detailed warning message
   * @param affectedFile File related to warning, or null
   */
  public Warning(WarningType type, String message, Path affectedFile) {
    this.type = type;
    this.message = message;
    this.affectedFile = affectedFile;
  }

  /**
   * @return Warning category
   */
  public WarningType getType() {
    return type;
  }

  /**
   * @return Detailed message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return Associated file, or null if not file-specific
   */
  public Path getAffectedFile() {
    return affectedFile;
  }

  @Override
  public String toString() {
    if (affectedFile != null) {
      return String.format("%s: %s (%s)", type.getDescription(), message, affectedFile);
    }
    return String.format("%s: %s", type.getDescription(), message);
  }
}