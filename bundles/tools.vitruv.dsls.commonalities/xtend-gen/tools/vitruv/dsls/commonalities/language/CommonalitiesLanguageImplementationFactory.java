package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.LanguageFactoryImpl;

@SuppressWarnings("all")
public class CommonalitiesLanguageImplementationFactory extends LanguageFactoryImpl {
  @Override
  public Participation createParticipation() {
    return new ParticipationI();
  }

  @Override
  public ParticipationClass createParticipationClass() {
    return new ParticipationClassI();
  }

  @Override
  public Commonality createCommonality() {
    return new CommonalityI();
  }

  @Override
  public ParticipationAttribute createParticipationAttribute() {
    return new ParticipationAttributeI();
  }

  @Override
  public CommonalityAttribute createCommonalityAttribute() {
    return new CommonalityAttributeI();
  }

  @Override
  public SimpleAttributeMapping createSimpleAttributeMapping() {
    return new SimpleAttributeMappingI();
  }

  @Override
  public OperatorAttributeMapping createOperatorAttributeMapping() {
    return new OperatorAttributeMappingI();
  }

  @Override
  public Concept createConcept() {
    return new ConceptI();
  }

  @Override
  public CommonalityReference createCommonalityReference() {
    return new CommonalityReferenceI();
  }

  @Override
  public SimpleReferenceMapping createSimpleReferenceMapping() {
    return new SimpleReferenceMappingI();
  }

  @Override
  public OperatorReferenceMapping createOperatorReferenceMapping() {
    return new OperatorReferenceMappingI();
  }
}
