package tools.vitruv.dsls.commonalities.language.elements;

import tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsFactoryImpl;

@SuppressWarnings("all")
public class LanguageElementsAdapterFactory extends LanguageElementsFactoryImpl {
  @Override
  public MostSpecificType createMostSpecificType() {
    return new MostSpecificTypeI();
  }

  @Override
  public LeastSpecificType createLeastSpecificType() {
    return new LeastSpecificTypeI();
  }

  @Override
  public Metamodel createMetamodel() {
    return new MetamodelAdapter();
  }

  @Override
  public EFeatureAttribute createEFeatureAttribute() {
    return new EFeatureAdapter();
  }

  @Override
  public EClassMetaclass createEClassMetaclass() {
    return new EClassAdapter();
  }

  @Override
  public ResourceMetaclass createResourceMetaclass() {
    return new ResourceMetaclassI();
  }

  @Override
  public EDataTypeClassifier createEDataTypeClassifier() {
    return new EDataTypeAdapter();
  }
}
