package tools.vitruv.dsls.commonalities.generator.reactions.resource;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class SetupResourceBridgeRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<SetupResourceBridgeRoutineBuilder> {
    @Override
    protected SetupResourceBridgeRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<SetupResourceBridgeRoutineBuilder>injectMembers(new SetupResourceBridgeRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getSetupResourceBridgeRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationClass resourceClass) {
      return this.getFor(segment).getSetupResourceBridgeRoutine(resourceClass);
    }
  }

  @Inject
  @Extension
  private ResourceBridgeHelper resourceBridgeHelper;

  private final Map<String, FluentRoutineBuilder> setupResourceBridgeRoutines = new HashMap<String, FluentRoutineBuilder>();

  private SetupResourceBridgeRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  SetupResourceBridgeRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getSetupResourceBridgeRoutine(final ParticipationClass resourceClass) {
    Preconditions.<ParticipationClass>checkNotNull(resourceClass, "resourceClass is null");
    Preconditions.checkArgument(CommonalitiesLanguageModelExtensions.isForResource(resourceClass), "The given resourceClass does to refer to the Resource metaclass");
    final Concept concept = CommonalitiesLanguageModelExtensions.getConcept(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(resourceClass));
    final Function<String, FluentRoutineBuilder> _function = (String it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("setupResourceBridge_");
      String _name = concept.getName();
      _builder.append(_name);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(ResourcesPackage.eINSTANCE.getIntermediateResourceBridge(), ReactionsGeneratorConventions.RESOURCE_BRIDGE);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it_2) -> {
          return this.resourceBridgeHelper.initExistingResourceBridge(it_2, resourceClass, it_2.variable(ReactionsGeneratorConventions.RESOURCE_BRIDGE));
        };
        it_1.execute(_function_3);
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2);
    };
    return this.setupResourceBridgeRoutines.computeIfAbsent(concept.getName(), _function);
  }
}
