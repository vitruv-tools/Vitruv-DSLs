package tools.vitruv.dsls.reactions.codegen.helper;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil;

@Utility
@SuppressWarnings("all")
public final class ReactionsElementsCompletionChecker {
  public static boolean isReferenceable(final ReactionsSegment reactionsSegment) {
    return ((((reactionsSegment != null) && (!StringExtensions.isNullOrEmpty(reactionsSegment.getName()))) && (!reactionsSegment.getFromMetamodels().isEmpty())) && (!reactionsSegment.getToMetamodels().isEmpty()));
  }

  public static boolean isReferenceable(final ReactionsImport reactionsImport) {
    return ((reactionsImport != null) && (reactionsImport.getImportedReactionsSegment() != null));
  }

  public static boolean isReferenceable(final Reaction reaction) {
    return ((reaction != null) && (!StringExtensions.isNullOrEmpty(reaction.getName())));
  }

  public static boolean isReferenceable(final Routine routine) {
    return ((routine != null) && (!StringExtensions.isNullOrEmpty(routine.getName())));
  }

  public static boolean isComplete(final RoutineOverrideImportPath routineOverrideImportPath) {
    return ((routineOverrideImportPath != null) && (!IterableExtensions.<RoutineOverrideImportPath>exists(ReactionsLanguageUtil.getFullPath(routineOverrideImportPath), ((Function1<RoutineOverrideImportPath, Boolean>) (RoutineOverrideImportPath it) -> {
      return Boolean.valueOf(((it.getReactionsSegment() == null) || StringExtensions.isNullOrEmpty(it.getReactionsSegment().getName())));
    }))));
  }

  private ReactionsElementsCompletionChecker() {
    
  }
}
