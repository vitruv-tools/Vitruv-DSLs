package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.Concept;

@Utility
@SuppressWarnings("all")
final class CommonalityExtension {
  public static Concept getConcept(final Commonality commonality) {
    return CommonalitiesLanguageElementExtension.<CommonalityFile>getDirectEContainer(commonality, CommonalityFile.class).getConcept();
  }

  private CommonalityExtension() {
    
  }
}
