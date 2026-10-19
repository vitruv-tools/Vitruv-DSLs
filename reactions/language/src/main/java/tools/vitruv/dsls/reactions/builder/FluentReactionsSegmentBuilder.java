package tools.vitruv.dsls.reactions.builder;

import lombok.AccessLevel;
import lombok.Getter;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;

import static com.google.common.base.Preconditions.*;
import java.util.Set;
import org.eclipse.emf.ecore.EPackage;

public class FluentReactionsSegmentBuilder extends FluentReactionElementBuilder {
	@Getter(AccessLevel.PACKAGE)
	private ReactionsSegment segment;

	FluentReactionsSegmentBuilder(String segmentName, FluentBuilderContext context) {
		super(context);
		segment = TopLevelElementsFactory.eINSTANCE.createReactionsSegment();
		segment.setName(segmentName);
	}

	ReactionsSegmentSourceBuilder start() {
		return new ReactionsSegmentSourceBuilder(this);
	}

	@Override
	protected void attachmentPreparation() {
		super.attachmentPreparation();
		checkState(
				segment.getRoutines().size() + segment.getReactions().size() + segment.getReactionsImports().size() > 0,
				"Neither routines, nor reactions, nor imports were added to the reaction segment " + segment.getName() + "!");
	}

	public static class ReactionsSegmentSourceBuilder {
		private final FluentReactionsSegmentBuilder builder;

		private ReactionsSegmentSourceBuilder(FluentReactionsSegmentBuilder builder) {
			this.builder = builder;
		}

		public ReactionsSegmentTargetBuilder inReactionToChangesIn(EPackage sourceMetamodelRootPackage) {
			return inReactionToChangesIn(Set.of(sourceMetamodelRootPackage));
		}

		public ReactionsSegmentTargetBuilder inReactionToChangesIn(Set<EPackage> sourceMetamodelRootPackages) {
			builder.beforeAttached(this, reactionsSegmentSourceBuilder ->
					builder.segment.getFromMetamodels().addAll(
          	sourceMetamodelRootPackages.stream().map(builder::metamodelImport).toList()
      ));
			return new ReactionsSegmentTargetBuilder(builder);
		}
	}

	public static class ReactionsSegmentTargetBuilder {
		private final FluentReactionsSegmentBuilder builder;

		private ReactionsSegmentTargetBuilder(FluentReactionsSegmentBuilder builder) {
			this.builder = builder;
		}

		public FluentReactionsSegmentBuilder executeActionsIn(EPackage targetMetamodelRootPackage) {
			return executeActionsIn(Set.of(targetMetamodelRootPackage));
		}

		public FluentReactionsSegmentBuilder executeActionsIn(Set<EPackage> targetMetamodelRootPackages) {
			builder.beforeAttached(this, reactionsSegmentTargetBuilder ->
					builder.segment.getToMetamodels().addAll(
          	targetMetamodelRootPackages.stream().map(builder::metamodelImport).toList()
      ));

			builder.readyToBeAttached = true;
			return builder;
		}
	}

	public ReactionsSegmentImportBuilder importSegment(FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
		return new ReactionsSegmentImportBuilder(this, reactionsSegmentBuilder);
	}

	public static class ReactionsSegmentImportBuilder {
		private final FluentReactionsSegmentBuilder builder;
		private final ReactionsImport reactionsImport;

		private ReactionsSegmentImportBuilder(FluentReactionsSegmentBuilder builder, FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
			this.builder = builder;
			this.reactionsImport = TopLevelElementsFactory.eINSTANCE.createReactionsImport();
			reactionsImport.setImportedReactionsSegment(reactionsSegmentBuilder.segment);
			builder.segment.getReactionsImports().add(reactionsImport);
		}

		public ReactionsSegmentImportBuilder routinesOnly() {
			reactionsImport.setRoutinesOnly(true);
			return this;
		}

		public FluentReactionsSegmentBuilder usingQualifiedRoutineNames() {
			reactionsImport.setUseQualifiedNames(true);
			return builder;
		}

		public FluentReactionsSegmentBuilder usingSimpleRoutineNames() {
			reactionsImport.setUseQualifiedNames(false);
			return builder;
		}
	}

	public FluentReactionsSegmentBuilder operator_add(FluentReactionBuilder[] reactionBuilders) {
		for (var reactionBuilder: reactionBuilders) {
			operator_add(reactionBuilder);
		}
		return this;
	}

	public FluentReactionsSegmentBuilder operator_add(FluentRoutineBuilder[] routineBuilders) {
		for (var routineBuilder: routineBuilders) {
			operator_add(routineBuilder);
		}
		return this;
	}

	public FluentReactionsSegmentBuilder add(FluentReactionsSegmentChildBuilder builder) {
		if (builder instanceof FluentReactionBuilder reactionBuilder) {
			return operator_add(reactionBuilder);
		}
		if (builder instanceof FluentRoutineBuilder routineBuilder) {
			return operator_add(routineBuilder);
		}
		throw new IllegalArgumentException("Unhandled parameter type: " + builder.getClass().getName());
	}

	public FluentReactionsSegmentBuilder operator_add(FluentReactionBuilder reactionBuilder) {
		checkNotYetAttached();
		if (reactionBuilder.getSegmentBuilder() != this) {
			checkArgument(
					reactionBuilder.getSegmentBuilder() == null,
					"The "+ reactionBuilder+ " has already been added to the " + reactionBuilder.getSegmentBuilder());
			checkArgument(
				segment.getReactions().stream()
						.anyMatch(reaction -> reaction.getName().equals(reactionBuilder.getReaction().getName())),
					"The " + this + " already contains a reaction with name '" + reactionBuilder.getReaction().getName() + "'!");
			segment.getReactions().add(reactionBuilder.getReaction());
			reactionBuilder.setSegmentBuilder(this);
			getChildBuilders().add(reactionBuilder);
		}
		return this;
	}

	public FluentReactionsSegmentBuilder operator_add(FluentRoutineBuilder routineBuilder) {
		checkNotYetAttached();
		if (routineBuilder.getSegmentBuilder() != this) {
			checkArgument(
					routineBuilder.getSegmentBuilder() == null,
					"The "+ routineBuilder+ " has already been added to the " + routineBuilder.getSegmentBuilder());
			checkArgument(
					segment.getRoutines().stream()
							.anyMatch(routine -> routine.getName().equals(routineBuilder.getRoutine().getName())),
					"The " + this + " already contains a routine with name '" + routineBuilder.getRoutine().getName() + "'!");
			segment.getRoutines().add(routineBuilder.getRoutine());
			routineBuilder.setSegmentBuilder(this);
			getChildBuilders().add(routineBuilder);
		}
		return this;
	}

	@Override
	public String toString() {
		return "reaction segment builder for \"" + segment.getName() + "\"";
	}
}
