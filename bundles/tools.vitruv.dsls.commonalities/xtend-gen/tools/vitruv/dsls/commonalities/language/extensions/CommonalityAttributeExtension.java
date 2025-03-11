package tools.vitruv.dsls.commonalities.language.extensions;

import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;

@SuppressWarnings("all")
public class CommonalityAttributeExtension {
  public static Commonality getDeclaringCommonality(final CommonalityAttribute attribute) {
    return CommonalitiesLanguageElementExtension.<Commonality>getDirectEContainer(attribute, Commonality.class);
  }
}
