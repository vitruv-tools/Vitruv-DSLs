package tools.vitruv.dsls.reactions.codegen

import edu.kit.ipd.sdq.activextendannotations.Utility

@Utility class ReactionsLanguageConstants {
	public static val OVERRIDDEN_REACTIONS_SEGMENT_SEPARATOR = "::";
	
	public static val CALL_BLOCK_FACADE_PARAMETER_NAME = "_routinesFacade"
	
	public static val CHANGE_AFFECTED_ELEMENT_ATTRIBUTE = "affectedEObject"
	public static val CHANGE_AFFECTED_ELEMENT_ACCESSOR = "getAffectedElement()"
	public static val CHANGE_AFFECTED_FEATURE_ATTRIBUTE = "affectedFeature"
	public static val CHANGE_OLD_VALUE_ATTRIBUTE = "oldValue"
	public static val CHANGE_NEW_VALUE_ATTRIBUTE = "newValue"
	public static val CHANGE_INDEX_ATTRIBUTE = "index"
}