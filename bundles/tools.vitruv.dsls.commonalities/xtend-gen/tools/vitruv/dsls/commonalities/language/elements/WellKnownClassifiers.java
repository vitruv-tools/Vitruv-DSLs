package tools.vitruv.dsls.commonalities.language.elements;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EcorePackage;

@Utility
@SuppressWarnings("all")
public final class WellKnownClassifiers {
  public static final EDataTypeClassifier JAVA_OBJECT = ClassifierProvider.INSTANCE.toDataTypeAdapter(EcorePackage.eINSTANCE.getEJavaObject());

  public static final Classifier MOST_SPECIFIC_TYPE = LanguageElementsFactory.eINSTANCE.createMostSpecificType();

  public static final Classifier LEAST_SPECIFIC_TYPE = LanguageElementsFactory.eINSTANCE.createLeastSpecificType();

  private WellKnownClassifiers() {
    
  }
}
