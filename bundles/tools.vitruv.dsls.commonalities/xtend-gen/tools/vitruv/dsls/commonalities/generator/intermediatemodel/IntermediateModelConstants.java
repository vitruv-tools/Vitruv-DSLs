package tools.vitruv.dsls.commonalities.generator.intermediatemodel;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.common.GenericClassNameGenerator;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;

@Utility
@SuppressWarnings("all")
public final class IntermediateModelConstants {
  private static final String INTERMEDIATE_METAMODEL_PACKAGE_PREFIX = "tools.vitruv.commonalities";

  private static final String METAMODEL_FILE_EXTENSION = ".ecore";

  @Pure
  public static String getIntermediateModelFileExtension(final Concept concept) {
    return IntermediateModelConstants.getIntermediateModelFileExtension(concept.getName());
  }

  @Pure
  public static String getIntermediateModelFileExtension(final String conceptName) {
    return StringExtensions.toFirstLower(conceptName);
  }

  @Pure
  public static String getIntermediateMetamodelFilePath(final Concept concept) {
    return IntermediateModelConstants.getIntermediateMetamodelFilePath(concept.getName());
  }

  @Pure
  public static String getIntermediateMetamodelFilePath(final String conceptName) {
    return (conceptName + IntermediateModelConstants.METAMODEL_FILE_EXTENSION);
  }

  @Pure
  public static String getIntermediateMetamodelPackagePrefix() {
    return IntermediateModelConstants.INTERMEDIATE_METAMODEL_PACKAGE_PREFIX;
  }

  @Pure
  public static String getIntermediateMetamodelPackageSimpleName(final Concept concept) {
    return IntermediateModelConstants.getIntermediateMetamodelPackageSimpleName(concept.getName());
  }

  @Pure
  public static String getIntermediateMetamodelPackageSimpleName(final String conceptName) {
    return conceptName.toLowerCase();
  }

  @Pure
  public static String getIntermediateMetamodelPackageName(final Concept concept) {
    return IntermediateModelConstants.getIntermediateMetamodelPackageName(concept.getName());
  }

  @Pure
  public static String getIntermediateMetamodelPackageName(final String conceptName) {
    String _intermediateMetamodelPackagePrefix = IntermediateModelConstants.getIntermediateMetamodelPackagePrefix();
    String _plus = (_intermediateMetamodelPackagePrefix + ".");
    String _intermediateMetamodelPackageSimpleName = IntermediateModelConstants.getIntermediateMetamodelPackageSimpleName(conceptName);
    return (_plus + _intermediateMetamodelPackageSimpleName);
  }

  @Pure
  public static String getIntermediateMetamodelClassesPrefix(final String conceptName) {
    return StringExtensions.toFirstUpper(conceptName);
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelRootClassName(final Concept concept) {
    return IntermediateModelConstants.getIntermediateMetamodelRootClassName(concept.getName());
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelRootClassName(final String conceptName) {
    String _intermediateMetamodelPackageName = IntermediateModelConstants.getIntermediateMetamodelPackageName(conceptName);
    String _intermediateMetamodelClassesPrefix = IntermediateModelConstants.getIntermediateMetamodelClassesPrefix(conceptName);
    String _plus = (_intermediateMetamodelClassesPrefix + "Root");
    return new GenericClassNameGenerator(_intermediateMetamodelPackageName, _plus);
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelClassName(final Commonality commonality) {
    String _intermediateMetamodelPackageName = IntermediateModelConstants.getIntermediateMetamodelPackageName(CommonalitiesLanguageModelExtensions.getConcept(commonality));
    String _name = commonality.getName();
    return new GenericClassNameGenerator(_intermediateMetamodelPackageName, _name);
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelFactoryClassName(final String conceptName) {
    String _intermediateMetamodelPackageName = IntermediateModelConstants.getIntermediateMetamodelPackageName(conceptName);
    String _intermediateMetamodelClassesPrefix = IntermediateModelConstants.getIntermediateMetamodelClassesPrefix(conceptName);
    String _plus = (_intermediateMetamodelClassesPrefix + "Factory");
    return new GenericClassNameGenerator(_intermediateMetamodelPackageName, _plus);
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelPackageClassName(final Concept concept) {
    return IntermediateModelConstants.getIntermediateMetamodelPackageClassName(concept.getName());
  }

  @Pure
  public static GenericClassNameGenerator getIntermediateMetamodelPackageClassName(final String conceptName) {
    String _intermediateMetamodelPackageName = IntermediateModelConstants.getIntermediateMetamodelPackageName(conceptName);
    String _intermediateMetamodelClassesPrefix = IntermediateModelConstants.getIntermediateMetamodelClassesPrefix(conceptName);
    String _plus = (_intermediateMetamodelClassesPrefix + "Package");
    return new GenericClassNameGenerator(_intermediateMetamodelPackageName, _plus);
  }

  private IntermediateModelConstants() {
    
  }
}
