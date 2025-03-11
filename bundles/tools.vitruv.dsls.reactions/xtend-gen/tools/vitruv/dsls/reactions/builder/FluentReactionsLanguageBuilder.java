package tools.vitruv.dsls.reactions.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

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
@SuppressWarnings("all")
public class FluentReactionsLanguageBuilder {
  public static class ExistingRoutineBuilder extends FluentRoutineBuilder {
    public ExistingRoutineBuilder(final Routine routine, final Consumer<FluentRoutineBuilder.InputBuilder> inputBuilder, final FluentBuilderContext context) {
      super(null, context);
      this.routine = routine;
      this.routine.setInput(TopLevelElementsFactory.eINSTANCE.createRoutineInput());
      this.start().input(inputBuilder);
      this.init();
    }

    public ExistingRoutineBuilder(final Routine routine, final FluentBuilderContext context) {
      super(null, context);
      this.routine = routine;
      this.init();
    }

    private void init() {
      this.readyToBeAttached = true;
      final TreeIterator<Object> contents = EcoreUtil.<Object>getAllContents(Collections.<Object>unmodifiableList(CollectionLiterals.<Object>newArrayList(this.routine)));
      final Function1<Object, Boolean> _function = (Object it) -> {
        return Boolean.valueOf((it instanceof MetaclassReference));
      };
      final Procedure1<Object> _function_1 = (Object it) -> {
        final MetaclassReference ref = ((MetaclassReference) it);
        EClassifier _metaclass = ref.getMetaclass();
        this.<MetaclassReference>reference(ref, ((EClass) _metaclass));
      };
      IteratorExtensions.<Object>forEach(IteratorExtensions.<Object>filter(contents, _function), _function_1);
    }
  }

  public static class ExistingReactionBuilder extends FluentReactionBuilder {
    public ExistingReactionBuilder(final Reaction reaction, final FluentBuilderContext context) {
      super(reaction, context);
      this.readyToBeAttached = true;
    }
  }

  @Inject
  private static FluentBuilderContext context;

  public FluentReactionsFileBuilder reactionsFile(final String name) {
    return new FluentReactionsFileBuilder(name, FluentReactionsLanguageBuilder.context).start();
  }

  public FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder reactionsSegment(final String name) {
    return new FluentReactionsSegmentBuilder(name, FluentReactionsLanguageBuilder.context).start();
  }

  public FluentRoutineBuilder.RoutineStartBuilder routine(final String name) {
    return new FluentRoutineBuilder(name, FluentReactionsLanguageBuilder.context).start();
  }

  public FluentReactionsLanguageBuilder.ExistingRoutineBuilder from(final Routine routine, final Consumer<FluentRoutineBuilder.InputBuilder> inputBuilder) {
    return new FluentReactionsLanguageBuilder.ExistingRoutineBuilder(routine, inputBuilder, FluentReactionsLanguageBuilder.context);
  }

  public FluentReactionsLanguageBuilder.ExistingRoutineBuilder from(final Routine routine) {
    return new FluentReactionsLanguageBuilder.ExistingRoutineBuilder(routine, FluentReactionsLanguageBuilder.context);
  }

  public FluentReactionBuilder.OverrideOrTriggerBuilder reaction(final String name) {
    return new FluentReactionBuilder(name, FluentReactionsLanguageBuilder.context).start();
  }

  public FluentReactionsLanguageBuilder.ExistingReactionBuilder from(final Reaction reaction) {
    return new FluentReactionsLanguageBuilder.ExistingReactionBuilder(reaction, FluentReactionsLanguageBuilder.context);
  }
}
