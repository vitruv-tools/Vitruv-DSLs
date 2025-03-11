package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
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
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.runtime.IntermediateModelManagement;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class InsertIntermediateRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<InsertIntermediateRoutineBuilder> {
    @Override
    protected InsertIntermediateRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<InsertIntermediateRoutineBuilder>injectMembers(new InsertIntermediateRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getInsertIntermediateRoutine(final FluentReactionsSegmentBuilder segment, final Concept concept) {
      return this.getFor(segment).getInsertIntermediateRoutine(concept);
    }
  }

  private final Map<String, FluentRoutineBuilder> insertIntermediateRoutines = new HashMap<String, FluentRoutineBuilder>();

  private InsertIntermediateRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  InsertIntermediateRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getInsertIntermediateRoutine(final Concept concept) {
    final Function<String, FluentRoutineBuilder> _function = (String it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("insertIntermediate_");
      String _name = concept.getName();
      _builder.append(_name);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(IntermediateModelBasePackage.Literals.INTERMEDIATE, ReactionsGeneratorConventions.INTERMEDIATE);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it_2) -> {
          return this.insertIntermediate(it_2, concept, it_2.variable(ReactionsGeneratorConventions.INTERMEDIATE));
        };
        it_1.execute(_function_3);
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2);
    };
    return this.insertIntermediateRoutines.computeIfAbsent(concept.getName(), _function);
  }

  private XBlockExpression insertIntermediate(@Extension final TypeProvider typeProvider, final Concept concept, final XFeatureCall intermediate) {
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
          it_1.setFeature(typeProvider.staticExtensionWildcardImported(typeProvider.findMethod(IntermediateModelManagement.class, "addIntermediate")));
          EList<XExpression> _memberCallArguments = it_1.getMemberCallArguments();
          _memberCallArguments.add(intermediate);
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
