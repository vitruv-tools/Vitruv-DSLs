package tools.vitruv.dsls.reactions.ui.validation;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.common.ui.validation.ProjectValidation;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.runtime.marker.RuntimeProjectMarker;
import tools.vitruv.dsls.reactions.validation.ReactionsLanguageValidator;

@SuppressWarnings("all")
public class ReactionsLanguageEclipseValidator extends ReactionsLanguageValidator {
  @Check(CheckType.NORMAL)
  @Override
  public void checkReactionsFile(final ReactionsFile reactionsFile) {
    super.checkReactionsFile(reactionsFile);
    ProjectValidation.checkIsJavaPluginProject(this, reactionsFile);
    ProjectValidation.checkRuntimeProjectIsOnClasspath(this, this.getServices().getTypeReferences(), RuntimeProjectMarker.class, reactionsFile);
  }

  @Check(CheckType.NORMAL)
  public void checkMetamodelOnClasspath(final MetamodelImport metamodelImport) {
    EPackage _package = metamodelImport.getPackage();
    boolean _tripleNotEquals = (_package != null);
    if (_tripleNotEquals) {
      ProjectValidation.checkMetamodelProjectIsOnClasspath(this, this.getServices().getTypeReferences(), metamodelImport.getPackage(), metamodelImport);
    }
  }
}
