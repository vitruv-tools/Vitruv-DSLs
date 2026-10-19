package tools.vitruv.dsls.reactions.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.InputBuilder;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import org.eclipse.emf.ecore.EClass;

/**
 * Entry point for fluent reaction builders. The offered methods each create a 
 * builder for a reactions language element. The builders offer only methods
 * that make sense in the current context and are named to match  the reactions
 * language’s syntax closely. Because of that, using them should be
 * self-explanatory.
 * 
 * <p>Unlike the builders, this class does not hold any state and can thus be
 * reused indefinitely.
 */
@Singleton
public class FluentReactionsLanguageBuilder {

	@Inject
	private static FluentBuilderContext context;

	public FluentReactionsFileBuilder reactionsFile(String name) {
		return new FluentReactionsFileBuilder(name, context).start();
	}

	public FluentReactionsSegmentBuilder reactionsSegment(String name) {
		return new FluentReactionsSegmentBuilder(name, context).start();
	}

	public FluentRoutineBuilder routine(String name) {
		return new FluentRoutineBuilder(name, context).start();
	}

	public ExistingRoutineBuilder from(Routine routine, Consumer<InputBuilder> inputBuilder) {
		return new ExistingRoutineBuilder(routine, inputBuilder, context);
	}

	public ExistingRoutineBuilder from(Routine routine) {
		return new ExistingRoutineBuilder(routine, context);
	}

	public FluentReactionBuilder reaction(String name) {
		new FluentReactionBuilder(name, context).start();
	}

	public ExistingReactionBuilder from(Reaction reaction) {
		return new ExistingReactionBuilder(reaction, context);
	}

	public static class ExistingRoutineBuilder extends FluentRoutineBuilder {
		public ExistingRoutineBuilder(Routine routine, Consumer<InputBuilder> inputBuilder, FluentBuilderContext context) {
			super(null, context);
			this.routine = routine;
			this.routine.setInput(TopLevelElementsFactory.eINSTANCE.createRoutineInput());
			start().input(inputBuilder);
			init();
		}

		public ExistingRoutineBuilder(Routine routine, FluentBuilderContext context) {
			super(null, context);
			this.routine = routine;
			init();
		}

		private void init() {
			this.readyToBeAttached = true;
			EList<EObject> contents = EcoreUtil.getAllContents(List.of(routine));
			contents.stream().filter(eObject -> eObject instanceof MetaclassReference)
					.forEach(eObject -> {
						MetaclassReference ref = (MetaclassReference) eObject;
						this.reference((EClass) ref.getMetaclass());
					});
		}
	}

	public static class ExistingReactionBuilder extends FluentReactionBuilder {
		public ExistingReactionBuilder(Reaction reaction, FluentBuilderContext context) {
			super(reaction, context);
			this.readyToBeAttached = true;
		}
	}
}
