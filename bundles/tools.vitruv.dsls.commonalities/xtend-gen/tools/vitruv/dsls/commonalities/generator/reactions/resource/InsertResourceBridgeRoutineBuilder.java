package tools.vitruv.dsls.commonalities.generator.reactions.resource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.IntermediateModelHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.IntermediateModelManagement;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class InsertResourceBridgeRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<InsertResourceBridgeRoutineBuilder> {
    @Override
    protected InsertResourceBridgeRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<InsertResourceBridgeRoutineBuilder>injectMembers(new InsertResourceBridgeRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getInsertResourceBridgeRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationClass resourceClass) {
      return this.getFor(segment).getInsertResourceBridgeRoutine(resourceClass);
    }
  }

  private final Map<String, FluentRoutineBuilder> insertResourceBridgeRoutines = new HashMap<String, FluentRoutineBuilder>();

  private InsertResourceBridgeRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  InsertResourceBridgeRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getInsertResourceBridgeRoutine(final ParticipationClass resourceClass) {
    Preconditions.<ParticipationClass>checkNotNull(resourceClass, "resourceClass is null");
    Preconditions.checkArgument(CommonalitiesLanguageModelExtensions.isForResource(resourceClass), "The given resourceClass does to refer to the Resource metaclass");
    final Concept concept = CommonalitiesLanguageModelExtensions.getConcept(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(resourceClass));
    final Function<String, FluentRoutineBuilder> _function = (String it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("insertResourceBridge_");
      String _name = concept.getName();
      _builder.append(_name);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(ResourcesPackage.eINSTANCE.getIntermediateResourceBridge(), ReactionsGeneratorConventions.RESOURCE_BRIDGE);
        it_1.model(IntermediateModelBasePackage.eINSTANCE.getIntermediate(), ReactionsGeneratorConventions.INTERMEDIATE);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it_2) -> {
          return this.insertResourceBridge(it_2, concept, it_2.variable(ReactionsGeneratorConventions.RESOURCE_BRIDGE), it_2.variable(ReactionsGeneratorConventions.INTERMEDIATE));
        };
        it_1.execute(_function_3);
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2);
    };
    return this.insertResourceBridgeRoutines.computeIfAbsent(concept.getName(), _function);
  }

  private XBlockExpression insertResourceBridge(@Extension final TypeProvider typeProvider, final Concept concept, final XFeatureCall resourceBridge, final XFeatureCall intermediate) {
    XBlockExpression _xblockexpression = null;
    {
      XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function = (XVariableDeclaration it) -> {
        it.setName("intermediateModelURI");
        it.setRight(IntermediateModelHelper.callGetMetadataModelURI(typeProvider, concept));
      };
      final XVariableDeclaration intermediateModelURIVariable = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function);
      XVariableDeclaration _createXVariableDeclaration_1 = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_1 = (XVariableDeclaration it) -> {
        it.setName("intermediateModelResource");
        it.setRight(ReactionsHelper.callGetModelResource(typeProvider, XbaseHelper.featureCall(intermediateModelURIVariable)));
      };
      final XVariableDeclaration intermediateModelResourceVariable = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration_1, _function_1);
      XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
      final Procedure1<XBlockExpression> _function_2 = (XBlockExpression it) -> {
        EList<XExpression> _expressions = it.getExpressions();
        XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(intermediateModelResourceVariable));
        final Procedure1<XMemberFeatureCall> _function_3 = (XMemberFeatureCall it_1) -> {
          it_1.setFeature(typeProvider.staticExtensionWildcardImported(typeProvider.findMethod(IntermediateModelManagement.class, "addResourceBridge")));
          EList<XExpression> _memberCallArguments = it_1.getMemberCallArguments();
          Iterables.<XExpression>addAll(_memberCallArguments, Collections.<XFeatureCall>unmodifiableList(CollectionLiterals.<XFeatureCall>newArrayList(resourceBridge, intermediate)));
          it_1.setExplicitOperationCall(true);
        };
        XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function_3);
        List<XExpression> _expressions_1 = XbaseHelper.expressions(intermediateModelURIVariable, intermediateModelResourceVariable, _doubleArrow);
        Iterables.<XExpression>addAll(_expressions, _expressions_1);
      };
      _xblockexpression = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_2);
    }
    return _xblockexpression;
  }
}
