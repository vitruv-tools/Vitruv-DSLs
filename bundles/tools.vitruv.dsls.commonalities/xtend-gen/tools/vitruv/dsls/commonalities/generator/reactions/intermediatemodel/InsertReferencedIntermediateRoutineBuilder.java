package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XExpression;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class InsertReferencedIntermediateRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<InsertReferencedIntermediateRoutineBuilder> {
    @Override
    protected InsertReferencedIntermediateRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<InsertReferencedIntermediateRoutineBuilder>injectMembers(new InsertReferencedIntermediateRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getInsertReferencedIntermediateRoutine(final FluentReactionsSegmentBuilder segment, final CommonalityReference reference) {
      return this.getFor(segment).getInsertReferencedIntermediateRoutine(reference);
    }
  }

  private final Map<CommonalityReference, FluentRoutineBuilder> insertReferencedIntermediateRoutines = new HashMap<CommonalityReference, FluentRoutineBuilder>();

  private InsertReferencedIntermediateRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  InsertReferencedIntermediateRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getInsertReferencedIntermediateRoutine(final CommonalityReference reference) {
    final Function<CommonalityReference, FluentRoutineBuilder> _function = (CommonalityReference it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Commonality referencedCommonality = reference.getReferenceType();
        final Commonality referencingCommonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(reference);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("insertReferencedIntermediate_");
        String _reactionName = ReactionsGeneratorConventions.getReactionName(reference);
        _builder.append(_reactionName);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(this._generationContext.getChangeClass(referencedCommonality), ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
          it_1.model(EcorePackage.eINSTANCE.getEObject(), ReactionsGeneratorConventions.REFERENCE_ROOT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).retrieveAsserted(this._generationContext.getChangeClass(referencingCommonality)).correspondingTo(ReactionsGeneratorConventions.REFERENCE_ROOT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it_2) -> {
            return EmfAccessExpressions.insertFeatureValue(it_2, it_2.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), this._generationContext.getCorrespondingEReference(reference), 
              it_2.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE));
          };
          it_1.execute(_function_4);
        };
        _xblockexpression = this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3);
      }
      return _xblockexpression;
    };
    return this.insertReferencedIntermediateRoutines.computeIfAbsent(reference, _function);
  }
}
