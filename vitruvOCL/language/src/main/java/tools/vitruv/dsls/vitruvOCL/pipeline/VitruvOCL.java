package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;

/** Public API for VitruvOCL constraint validation. */
public class VitruvOCL {

  /**
   * Validates OCL constraints in standalone mode.
   *
   * @param oclFile Path to .ocl file
   * @param ecoreFiles Paths to .ecore metamodel files
   * @return ValidationResult
   */
  public static ValidationResult validate(Path oclFile, Path... ecoreFiles) throws IOException {
    MetamodelWrapper spec = new MetamodelWrapper();
    for (Path ecoreFile : ecoreFiles) {
      spec.loadMetamodel(ecoreFile);
    }

    tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCLCompiler compiler =
        new VitruvOCLCompiler(spec, oclFile);
    return compiler.compile();
  }

  /**
   * Validates with custom constraint specification (e.g., Vitruvius VSUM).
   *
   * @param specification Custom constraint specification
   * @param oclFile Path to .ocl file
   * @return ValidationResult
   */
  public static ValidationResult validate(MetamodelWrapperInterface specification, Path oclFile)
      throws IOException {
    tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCLCompiler compiler =
        new VitruvOCLCompiler(specification, oclFile);
    return compiler.compile();
  }
}