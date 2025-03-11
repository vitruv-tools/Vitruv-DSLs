package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import edu.kit.ipd.sdq.activextendannotations.Lazy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsSubGenerator;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsRetrievalHelper;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationAttributeChangeReactionsBuilder extends ReactionsSubGenerator {
  public static class Factory extends InjectingFactoryBase {
    public ParticipationAttributeChangeReactionsBuilder createFor(final Participation participation) {
      return this.<ParticipationAttributeChangeReactionsBuilder>injectMembers(new ParticipationAttributeChangeReactionsBuilder(participation));
    }
  }

  @Inject
  @Extension
  private AttributeMappingHelper attributeMappingHelper;

  @Inject
  @Extension
  private AttributeChangeReactionsHelper attributeChangeReactionsHelper;

  @Inject
  @Extension
  private ParticipationObjectsRetrievalHelper participationObjectsRetrievalHelper;

  private final Participation participation;

  @Lazy
  private List<CommonalityAttributeMapping> _relevantMappings;

  @Lazy
  private Set<ParticipationClass> _relevantParticipationClasses;

  private ParticipationAttributeChangeReactionsBuilder(final Participation participation) {
    Preconditions.<Participation>checkNotNull(participation, "participation is null");
    this.participation = participation;
  }

  ParticipationAttributeChangeReactionsBuilder() {
    this.participation = null;
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  private List<CommonalityAttributeMapping> calculateRelevantMappings() {
    return IterableExtensions.<CommonalityAttributeMapping>toList(this.attributeMappingHelper.getRelevantReadMappings(this.participation));
  }

  private Set<ParticipationClass> calculateRelevantParticipationClasses() {
    final Function1<CommonalityAttributeMapping, Collection<ParticipationClass>> _function = (CommonalityAttributeMapping it) -> {
      return CommonalitiesLanguageModelExtensions.getInvolvedParticipationClasses(it);
    };
    return IterableExtensions.<ParticipationClass>toSet(IterableExtensions.<CommonalityAttributeMapping, ParticipationClass>flatMap(this.getRelevantMappings(), _function));
  }

  public void generateReactions(final FluentReactionsSegmentBuilder segment) {
    final Function1<CommonalityAttributeMapping, List<FluentReactionBuilder>> _function = (CommonalityAttributeMapping it) -> {
      return this.reactionsForAttributeMappingRightChange(it);
    };
    Iterable<FluentReactionBuilder> _flatMap = IterableExtensions.<CommonalityAttributeMapping, FluentReactionBuilder>flatMap(this.getRelevantMappings(), _function);
    segment.operator_add(((FluentReactionBuilder[])Conversions.unwrapArray(_flatMap, FluentReactionBuilder.class)));
  }

  private List<FluentReactionBuilder> reactionsForAttributeMappingRightChange(final CommonalityAttributeMapping mapping) {
    final ParticipationAttribute participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
    if ((participationAttribute == null)) {
      return CollectionLiterals.<FluentReactionBuilder>emptyList();
    }
    final CommonalityAttribute commonalityAttribute = CommonalitiesLanguageModelExtensions.getDeclaringAttribute(mapping);
    final String reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(commonalityAttribute);
    final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> _function = (AttributeChangeReactionsHelper.AttributeChangeReactionType changeType, FluentReactionBuilder.RoutineCallBuilder it) -> {
      final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function_1 = (FluentRoutineBuilder.RoutineStartBuilder it_1) -> {
        this.buildAttributeChangedRoutine(it_1, mapping);
      };
      return it.call(_function_1);
    };
    return this.attributeChangeReactionsHelper.getAttributeChangeReactions(participationAttribute, reactionNameSuffix, _function);
  }

  private FluentRoutineBuilder buildAttributeChangedRoutine(@Extension final FluentRoutineBuilder.RoutineStartBuilder routineBuilder, final CommonalityAttributeMapping mapping) {
    final Consumer<FluentRoutineBuilder.InputBuilder> _function = (FluentRoutineBuilder.InputBuilder it) -> {
      it.affectedEObject();
    };
    final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it) -> {
      this.retrieveIntermediate(it, mapping);
      this.retrieveRelevantParticipationObjects(it);
    };
    final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it) -> {
      this.applyMapping(it, mapping);
    };
    return routineBuilder.input(_function).match(_function_1).update(_function_2);
  }

  private void retrieveIntermediate(@Extension final FluentRoutineBuilder.UndecidedMatchStatementBuilder builder, final CommonalityAttributeMapping mapping) {
    ParticipationAttribute _participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
    boolean _tripleNotEquals = (_participationAttribute != null);
    XtendAssertHelper.assertTrue(_tripleNotEquals);
    final ParticipationClass participationClass = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping).getParticipationClass();
    builder.vall(ReactionsGeneratorConventions.INTERMEDIATE).retrieve(this._generationContext.getChangeClass(this._generationContext.getCommonality())).correspondingTo().affectedEObject().taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
  }

  private void retrieveRelevantParticipationObjects(@Extension final FluentRoutineBuilder.UndecidedMatchStatementBuilder matcherBuilder) {
    final Consumer<ParticipationClass> _function = (ParticipationClass participationClass) -> {
      final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it) -> {
        return it.variable(ReactionsGeneratorConventions.INTERMEDIATE);
      };
      this.participationObjectsRetrievalHelper.retrieveAssertedParticipationObject(matcherBuilder, participationClass, _function_1);
    };
    this.getRelevantParticipationClasses().forEach(_function);
  }

  private void applyMapping(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final CommonalityAttributeMapping mapping) {
    final Function<TypeProvider, XExpression> _function = (TypeProvider typeProvider) -> {
      XExpression _xblockexpression = null;
      {
        final Function<ParticipationClass, XExpression> participationClassToObject = this.attributeMappingHelper.participationClassToOptionalObject(typeProvider);
        final Supplier<XExpression> _function_1 = () -> {
          return typeProvider.variable(ReactionsGeneratorConventions.INTERMEDIATE);
        };
        final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext = new AttributeMappingOperatorHelper.AttributeMappingOperatorContext(typeProvider, _function_1, participationClassToObject);
        _xblockexpression = this.attributeMappingHelper.applyReadMapping(mapping, operatorContext);
      }
      return _xblockexpression;
    };
    updateBuilder.execute(_function);
  }

  private boolean _relevantMappings_isInitialised = false;

  private List<CommonalityAttributeMapping> _relevantMappings_initialise() {
    List<CommonalityAttributeMapping> _calculateRelevantMappings = this.calculateRelevantMappings();
    return _calculateRelevantMappings;
  }

  public List<CommonalityAttributeMapping> getRelevantMappings() {
    if (!_relevantMappings_isInitialised) {
    	try {
    		_relevantMappings = _relevantMappings_initialise();
    	} finally {
    		_relevantMappings_isInitialised = true;
    	}
    }
    return _relevantMappings;
  }

  private boolean _relevantParticipationClasses_isInitialised = false;

  private Set<ParticipationClass> _relevantParticipationClasses_initialise() {
    Set<ParticipationClass> _calculateRelevantParticipationClasses = this.calculateRelevantParticipationClasses();
    return _calculateRelevantParticipationClasses;
  }

  public Set<ParticipationClass> getRelevantParticipationClasses() {
    if (!_relevantParticipationClasses_isInitialised) {
    	try {
    		_relevantParticipationClasses = _relevantParticipationClasses_initialise();
    	} finally {
    		_relevantParticipationClasses_isInitialised = true;
    	}
    }
    return _relevantParticipationClasses;
  }
}
