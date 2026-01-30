package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;

public class VitruvOCL {

  /** Validates OCL constraints from file. */
  public static ValidationResult validate(Path oclFile, Path[] ecoreFiles, Path... xmiFiles)
      throws IOException {
    MetamodelWrapper wrapper = new MetamodelWrapper();

    for (Path ecore : ecoreFiles) {
      wrapper.loadMetamodel(ecore);
    }

    for (Path xmi : xmiFiles) {
      wrapper.loadModelInstance(xmi.getFileName().toString());
    }

    VitruvOCLCompiler compiler = new VitruvOCLCompiler(wrapper, oclFile);
    return compiler.compile();
  }

  /** Evaluates OCL constraint string with files. */
  public static Value evaluate(String oclSource, Path[] ecoreFiles, Path... xmiFiles)
      throws IOException {
    MetamodelWrapper wrapper = new MetamodelWrapper();

    for (Path ecore : ecoreFiles) {
      wrapper.loadMetamodel(ecore);
    }

    for (Path xmi : xmiFiles) {
      wrapper.loadModelInstance(xmi.getFileName().toString());
    }

    VitruvOCLCompiler compiler = new VitruvOCLCompiler(wrapper, null);
    return compiler.compile(oclSource);
  }
}