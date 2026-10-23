package tools.vitruv.dsls.reactions.codegen.helper;

import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper.getParsedOverriddenReactionsSegmentName;
import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper.getParsedOverrideImportPath;
import static tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil.*;

import static org.eclipse.xtext.xbase.lib.StringExtensions.toFirstUpper;
import static org.eclipse.xtext.xbase.lib.StringExtensions.toFirstLower;

import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.common.ClassNameGenerator;

@Utility
public final class ClassNamesGenerators {
	private ClassNamesGenerators() {}

	private static final String BASIC_PACKAGE = "mir";
	private static final String  REACTIONS_PACKAGE = "reactions";
	private static final String ROUTINES_PACKAGE = "routines";
	private static final String  REACTIONS_EXECUTOR_CLASS_NAME = "ReactionsExecutor";
	private static final String  ROUTINES_FACADE_CLASS_NAME = "RoutinesFacade";
	private static final String  ROUTINES_FACADES_PROVIDER_CLASS_NAME = "RoutinesFacadesProvider";
	
	public static String getBasicMirPackageQualifiedName() {
		return BASIC_PACKAGE;
	}
	
	public static String getBasicReactionsPackageQualifiedName() {
		return getBasicMirPackageQualifiedName()+"."+REACTIONS_PACKAGE;
	}
	
	public static String getBasicRoutinesPackageQualifiedName() {
		return getBasicMirPackageQualifiedName()+"."+ROUTINES_PACKAGE;
	}
	
	private static String getReactionsPackageQualifiedName(ReactionsSegment reactionSegment) {
		return getBasicReactionsPackageQualifiedName()+"."+getPackageName(reactionSegment);
	}
	
	private static String getRoutinesPackageQualifiedName(ReactionsSegment reactionSegment) {
		return getBasicRoutinesPackageQualifiedName()+"."+getPackageName(reactionSegment);
	}
	
	private static String getPackageName(ReactionsSegment reactionSegment) {
		return getReactionsSegmentPackageName(reactionSegment.getName());
	}
	
	private static String getReactionsSegmentPackageName(String reactionSegmentName) {
		return toFirstLower(reactionSegmentName);
	}
	
	public static ClassNameGenerator getChangePropagationSpecificationClassNameGenerator(ReactionsSegment reactionSegment) {
		return new ChangePropagationSpecificationClassNameGenerator(reactionSegment);
	}
	
	public static ClassNameGenerator getExecutorClassNameGenerator(ReactionsSegment reactionSegment) {
		return new ExecutorClassNameGenerator(reactionSegment);
	}
	
	public static ClassNameGenerator getRoutinesFacadesProviderClassNameGenerator(ReactionsSegment reactionSegment) {
		return new RoutinesFacadesProviderClassNameGenerator(reactionSegment);
	}
	
	public static ClassNameGenerator getRoutinesFacadeClassNameGenerator(ReactionsSegment reactionSegment) {
		return new RoutinesFacadeClassNameGenerator(reactionSegment);
	}
	
	// import path relative to reactions segment:
	public static ClassNameGenerator getOverriddenRoutinesFacadeClassNameGenerator(ReactionsSegment reactionsSegment, ReactionsImportPath relativeImportPath) {
		return new OverriddenRoutinesFacadeClassNameGenerator(reactionsSegment, relativeImportPath);
	}
	
	public static ClassNameGenerator getReactionClassNameGenerator(Reaction reaction) {
		return new ReactionClassNameGenerator(reaction);
	}
	
	public static ClassNameGenerator getRoutineClassNameGenerator(Routine routine) {
		return new RoutineClassNameGenerator(routine);
	}
	
	private static class ChangePropagationSpecificationClassNameGenerator implements ClassNameGenerator {
		private final ReactionsSegment reactionsSegment;
		
		public ChangePropagationSpecificationClassNameGenerator(ReactionsSegment reactionsSegment) {
			this.reactionsSegment = reactionsSegment;
		}
		
		@Override
		public String getSimpleName() {
			return toFirstUpper(reactionsSegment.getName()) + "ChangePropagationSpecification";
		}
		
		@Override
		public String getPackageName() {
			return getReactionsPackageQualifiedName(reactionsSegment);
		}
	}
	
	private static class ExecutorClassNameGenerator implements ClassNameGenerator {
		private final ReactionsSegment reactionsSegment;
		
		public ExecutorClassNameGenerator(ReactionsSegment reactionsSegment) {
			this.reactionsSegment = reactionsSegment;
		}
		
		@Override
		public String getSimpleName() {
			return REACTIONS_EXECUTOR_CLASS_NAME;
		}

		@Override
		public String getPackageName() {
			return getReactionsPackageQualifiedName(reactionsSegment);
		}
	}
	
	private static class RoutinesFacadesProviderClassNameGenerator implements ClassNameGenerator {
		private final ReactionsSegment reactionsSegment;
		
		public RoutinesFacadesProviderClassNameGenerator(ReactionsSegment reactionsSegment) {
			this.reactionsSegment = reactionsSegment;
		}
		
		@Override
		public String getSimpleName() {
			return toFirstUpper(reactionsSegment.getName()) + ROUTINES_FACADES_PROVIDER_CLASS_NAME;
		}
		
		@Override
		public String getPackageName() {
			return getBasicRoutinesPackageQualifiedName()
					+ "."
					+ ClassNamesGenerators.getPackageName(reactionsSegment);
		}
	}
	
	private static class ReactionClassNameGenerator implements ClassNameGenerator {
		private final Reaction reaction;
		public ReactionClassNameGenerator (Reaction reaction) {
			this.reaction = reaction;
		}
		
		@Override
		public String getSimpleName() {
			return toFirstUpper(reaction.getName()) + "Reaction";
		}
		@Override
		public String getPackageName() {
			var packageName = getReactionsPackageQualifiedName(reaction.getReactionsSegment());
			if (isOverride(reaction)) {
				// not resolving cross-references here to get the overridden reactions segment name,
				// if a parsed reactions segment name is available:
				packageName
						+= "."
						+ getReactionsSegmentPackageName(getParsedOverriddenReactionsSegmentName(reaction));
			}
			return packageName;
		}
	}
	
	private static class RoutineClassNameGenerator implements ClassNameGenerator {
		private final Routine routine;
		public RoutineClassNameGenerator(Routine routine) {
			this.routine = routine;
		}
		
		@Override
		public String getSimpleName() {
			return toFirstUpper(routine.getName()) + "Routine";
		}
		
		@Override
		public String getPackageName() {
			var packageName = getRoutinesPackageQualifiedName(routine.getReactionsSegment());
			if (isOverride(routine)) {
				// not resolving cross-references here to get the override import path,
				// if parsed reaction segment names are available:
				String joinedPath = String.join(".", getParsedOverrideImportPath(routine).segments());
				packageName += "." + joinedPath;
			}
			return packageName;
		}
	}
	
	private static class RoutinesFacadeClassNameGenerator implements ClassNameGenerator {
		private final ReactionsSegment reactionsSegment;
		public RoutinesFacadeClassNameGenerator(ReactionsSegment reactionsSegment) {
			this.reactionsSegment = reactionsSegment;
		}
		
		@Override
		public String getSimpleName() {
			return StringExtensions.toFirstUpper(reactionsSegment.getName()) + ROUTINES_FACADE_CLASS_NAME;
		}
		
		@Override
		public String getPackageName() {
			return getRoutinesPackageQualifiedName(reactionsSegment);
		}
	}
	
	private static class OverriddenRoutinesFacadeClassNameGenerator implements ClassNameGenerator {
		private final ReactionsSegment reactionsSegment;
		private final ReactionsImportPath relativeImportPath;
		
		public OverriddenRoutinesFacadeClassNameGenerator(ReactionsSegment reactionsSegment, ReactionsImportPath relativeImportPath) {
			this.reactionsSegment = reactionsSegment;
			this.relativeImportPath = relativeImportPath;
		}
		
		@Override
		public String getSimpleName() {
			return toFirstUpper(reactionsSegment.getName()) + ROUTINES_FACADE_CLASS_NAME;
		}
		
		@Override
		public String getPackageName() {
			return getRoutinesPackageQualifiedName(reactionsSegment)
					+ "."
					+ String.join(".", relativeImportPath.segments());
		}
	}
}
