package tools.vitruv.dsls.reactions.codegen.helper;

import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import static tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil.*;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;

@Utility
public final class ReactionsElementsCompletionChecker {
	private ReactionsElementsCompletionChecker() {}

	public static boolean nullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

 	public static boolean isReferenceable(ReactionsSegment reactionsSegment) {
		return reactionsSegment != null
				&& !nullOrEmpty(reactionsSegment.getName())
				&& !reactionsSegment.getFromMetamodels().isEmpty()
				&& !reactionsSegment.getToMetamodels().isEmpty();
	}
	
	public static boolean isReferenceable(ReactionsImport reactionsImport) {
		return reactionsImport != null && reactionsImport.getImportedReactionsSegment() != null;
	}
	
	public static boolean isReferenceable(Reaction reaction) {
		return reaction != null && !nullOrEmpty(reaction.getName());
	}
	
	public static boolean isReferenceable(Routine routine) {
		return routine != null && !nullOrEmpty(routine.getName());
	}

	/**
	 * Note: this triggers a resolve of cross-references
 	 */
	public static boolean isComplete(RoutineOverrideImportPath routineOverrideImportPath) {
		return routineOverrideImportPath != null
				&& getFullPath(routineOverrideImportPath)
				.stream()
				.allMatch(path ->
					path.getReactionsSegment() != null && !nullOrEmpty(path.getReactionsSegment().getName())
				);
	}
}