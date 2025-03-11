package tools.vitruv.dsls.reactions.codegen;

import edu.kit.ipd.sdq.activextendannotations.Utility;

@Utility
@SuppressWarnings("all")
public final class ReactionsLanguageConstants {
  public static final String OVERRIDDEN_REACTIONS_SEGMENT_SEPARATOR = "::";

  public static final String CALL_BLOCK_FACADE_PARAMETER_NAME = "_routinesFacade";

  public static final String CHANGE_AFFECTED_ELEMENT_ATTRIBUTE = "affectedEObject";

  public static final String CHANGE_AFFECTED_ELEMENT_ACCESSOR = "getAffectedElement()";

  public static final String CHANGE_AFFECTED_FEATURE_ATTRIBUTE = "affectedFeature";

  public static final String CHANGE_OLD_VALUE_ATTRIBUTE = "oldValue";

  public static final String CHANGE_NEW_VALUE_ATTRIBUTE = "newValue";

  public static final String CHANGE_INDEX_ATTRIBUTE = "index";

  private ReactionsLanguageConstants() {
    
  }
}
