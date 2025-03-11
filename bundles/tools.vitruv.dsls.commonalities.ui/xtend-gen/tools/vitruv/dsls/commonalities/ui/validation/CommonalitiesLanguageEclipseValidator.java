package tools.vitruv.dsls.commonalities.ui.validation;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.common.ui.validation.ProjectValidation;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.runtime.marker.RuntimeProjectMarker;
import tools.vitruv.dsls.commonalities.validation.CommonalitiesLanguageValidator;

/**
 * Validations that are only applicable when running in Eclipse.
 */
@SuppressWarnings("all")
public class CommonalitiesLanguageEclipseValidator extends CommonalitiesLanguageValidator {
  @Check(CheckType.NORMAL)
  public void checkProjectSetup(final CommonalityFile commonalityFile) {
    ProjectValidation.checkIsJavaPluginProject(this, commonalityFile, LanguagePackage.Literals.COMMONALITY_FILE__CONCEPT);
    ProjectValidation.checkRuntimeProjectIsOnClasspath(this, this.getServices().getTypeReferences(), RuntimeProjectMarker.class, commonalityFile, LanguagePackage.Literals.COMMONALITY_FILE__CONCEPT);
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
