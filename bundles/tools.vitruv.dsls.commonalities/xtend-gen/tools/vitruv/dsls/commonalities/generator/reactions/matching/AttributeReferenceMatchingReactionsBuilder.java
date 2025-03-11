package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeChangeReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.IntermediateContainmentReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.reference.ReferenceMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ReferenceMappingOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.OperatorContainment;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

/**
 * Builds reactions and routines related to the matching of attribute
 * references.
 */
@SuppressWarnings("all")
public class AttributeReferenceMatchingReactionsBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<AttributeReferenceMatchingReactionsBuilder> {
    @Override
    protected AttributeReferenceMatchingReactionsBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<AttributeReferenceMatchingReactionsBuilder>injectMembers(new AttributeReferenceMatchingReactionsBuilder(segment));
    }

    public void generateAttributeReferenceMatchingReactions(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      this.getFor(segment).generateAttributeReferenceMatchingReactions(participationContext);
    }

    public FluentRoutineBuilder getMatchAttributeReferenceElementsRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      this.getFor(segment).generateRoutines(participationContext);
      return this.getFor(segment).getMatchAttributeReferenceElementsRoutine(participationContext);
    }

    public FluentRoutineBuilder getMatchAttributeReferenceContainerRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      this.getFor(segment).generateRoutines(participationContext);
      return this.getFor(segment).getMatchAttributeReferenceContainerRoutine(participationContext);
    }

    public FluentRoutineBuilder getMatchAttributeReferenceContainerForIntermediateRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      this.getFor(segment).generateRoutines(participationContext);
      return this.getFor(segment).getMatchAttributeReferenceContainerForIntermediateRoutine(participationContext);
    }
  }

  @Inject
  @Extension
  private ReferenceMappingOperatorHelper referenceMappingOperatorHelper;

  @Inject
  @Extension
  private AttributeChangeReactionsHelper attributeChangeReactionsHelper;

  @Inject
  @Extension
  private IntermediateContainmentReactionsHelper intermediateContainmentReactionsHelper;

  @Inject
  @Extension
  private InsertIntermediateRoutineBuilder.Provider insertIntermediateRoutineBuilderProvider;

  private final FluentReactionsSegmentBuilder segment;

  private final Map<ParticipationContext, FluentRoutineBuilder> checkAttributeReferenceRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> checkAttributeReferenceElementsRemovedRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> checkAttributeReferenceElementRemovedRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> matchAttributeReferenceElementsRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> matchAttributeReferenceContainerRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> matchAttributeReferenceContainerForIntermediateRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private AttributeReferenceMatchingReactionsBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
    this.segment = segment;
  }

  AttributeReferenceMatchingReactionsBuilder() {
    this.segment = null;
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public void generateAttributeReferenceMatchingReactions(final ParticipationContext participationContext) {
    boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
    boolean _not = (!_isForAttributeReferenceMapping);
    if (_not) {
      return;
    }
    this.generateRoutines(participationContext);
    final Consumer<ParticipationContext.ContextContainment<OperatorContainment>> _function = (ParticipationContext.ContextContainment<OperatorContainment> contextContainment) -> {
      final OperatorContainment containment = contextContainment.getContainment();
      XtendAssertHelper.assertTrue(CommonalitiesLanguageModelExtensions.isAttributeReference(containment.getMapping()));
      Iterable<FluentReactionBuilder> _reactionsForAttributeReferenceChange = this.reactionsForAttributeReferenceChange(participationContext, containment);
      this.segment.operator_add(((FluentReactionBuilder[])Conversions.unwrapArray(_reactionsForAttributeReferenceChange, FluentReactionBuilder.class)));
    };
    participationContext.getAttributeReferenceContainments().forEach(_function);
  }

  private FluentReactionsSegmentBuilder generateRoutines(final ParticipationContext participationContext) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
      XtendAssertHelper.assertTrue(participationContext.isRootContext());
      final Commonality referencedCommonality = participationContext.getReferencedCommonality();
      FluentRoutineBuilder _insertIntermediateRoutine = this.insertIntermediateRoutineBuilderProvider.getInsertIntermediateRoutine(this.segment, CommonalitiesLanguageModelExtensions.getConcept(referencedCommonality));
      this.segment.operator_add(_insertIntermediateRoutine);
      FluentRoutineBuilder _checkAttributeReferenceRoutine = this.getCheckAttributeReferenceRoutine(participationContext);
      this.segment.operator_add(_checkAttributeReferenceRoutine);
      FluentRoutineBuilder _checkAttributeReferenceElementsRemovedRoutine = this.getCheckAttributeReferenceElementsRemovedRoutine(participationContext);
      this.segment.operator_add(_checkAttributeReferenceElementsRemovedRoutine);
      FluentRoutineBuilder _checkAttributeReferenceElementRemovedRoutine = this.getCheckAttributeReferenceElementRemovedRoutine(participationContext);
      this.segment.operator_add(_checkAttributeReferenceElementRemovedRoutine);
      FluentRoutineBuilder _matchAttributeReferenceElementsRoutine = this.getMatchAttributeReferenceElementsRoutine(participationContext);
      this.segment.operator_add(_matchAttributeReferenceElementsRoutine);
      FluentRoutineBuilder _matchAttributeReferenceContainerRoutine = this.getMatchAttributeReferenceContainerRoutine(participationContext);
      _xblockexpression = this.segment.operator_add(_matchAttributeReferenceContainerRoutine);
    }
    return _xblockexpression;
  }

  private Iterable<FluentReactionBuilder> reactionsForAttributeReferenceChange(final ParticipationContext participationContext, final OperatorContainment containment) {
    XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
    final Function1<ReferenceMappingOperand, List<FluentReactionBuilder>> _function = (ReferenceMappingOperand it) -> {
      return this.reactionsForAttributeReferenceChange(participationContext, containment, it);
    };
    return IterableExtensions.<ReferenceMappingOperand, FluentReactionBuilder>flatMap(containment.getMapping().getOperands(), _function);
  }

  private List<FluentReactionBuilder> _reactionsForAttributeReferenceChange(final ParticipationContext participationContext, final OperatorContainment containment, final ParticipationAttributeOperand operand) {
    final ParticipationAttribute attribute = operand.getParticipationAttribute();
    final String reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
    final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> _function = (AttributeChangeReactionsHelper.AttributeChangeReactionType changeType, FluentReactionBuilder.RoutineCallBuilder it) -> {
      FluentReactionBuilder _xblockexpression = null;
      {
        FluentRoutineBuilder _checkAttributeReferenceElementsRemovedRoutine = this.getCheckAttributeReferenceElementsRemovedRoutine(participationContext);
        final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it_1) -> {
          return it_1.affectedEObject();
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_1);
        it.call(_checkAttributeReferenceElementsRemovedRoutine, _routineCallParameter);
        FluentRoutineBuilder _matchAttributeReferenceElementsRoutine = this.getMatchAttributeReferenceElementsRoutine(participationContext);
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
          return it_1.affectedEObject();
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_2);
        _xblockexpression = it.call(_matchAttributeReferenceElementsRoutine, _routineCallParameter_1);
      }
      return _xblockexpression;
    };
    return this.attributeChangeReactionsHelper.getAttributeChangeReactions(attribute, reactionNameSuffix, _function);
  }

  private List<FluentReactionBuilder> _reactionsForAttributeReferenceChange(final ParticipationContext participationContext, final OperatorContainment containment, final ReferencedParticipationAttributeOperand operand) {
    final ParticipationAttribute attribute = operand.getParticipationAttribute();
    String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
    final String reactionNameSuffix = (_reactionNameSuffix + "_element");
    final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> _function = (AttributeChangeReactionsHelper.AttributeChangeReactionType changeType, FluentReactionBuilder.RoutineCallBuilder it) -> {
      FluentReactionBuilder _xblockexpression = null;
      {
        FluentRoutineBuilder _checkAttributeReferenceElementRemovedRoutine = this.getCheckAttributeReferenceElementRemovedRoutine(participationContext);
        final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it_1) -> {
          return it_1.affectedEObject();
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_1);
        it.call(_checkAttributeReferenceElementRemovedRoutine, _routineCallParameter);
        FluentRoutineBuilder _matchAttributeReferenceContainerRoutine = this.getMatchAttributeReferenceContainerRoutine(participationContext);
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
          return it_1.affectedEObject();
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_2);
        _xblockexpression = it.call(_matchAttributeReferenceContainerRoutine, _routineCallParameter_1);
      }
      return _xblockexpression;
    };
    return this.attributeChangeReactionsHelper.getAttributeChangeReactions(attribute, reactionNameSuffix, _function);
  }

  private List<FluentReactionBuilder> _reactionsForAttributeReferenceChange(final ParticipationContext participationContext, final OperatorContainment containment, final ReferenceMappingOperand operand) {
    return Collections.<FluentReactionBuilder>emptyList();
  }

  /**
   * Checks if any attribute references for the given container participation object are no longer fulfilled.
   */
  private FluentRoutineBuilder getCheckAttributeReferenceElementsRemovedRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
        final CommonalityReference commonalityReference = participationContext.getDeclaringReference();
        final Commonality referencingCommonality = participationContext.getReferencingCommonality();
        final Commonality referencedCommonality = participationContext.getReferencedCommonality();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("checkAttributeReferenceElementsRemoved");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.Literals.EOBJECT, ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).retrieve(this._generationContext.getChangeClass(referencingCommonality)).correspondingTo(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _xblockexpression_1 = null;
            {
              @Extension
              final JvmTypeReferenceBuilder jvmTypeReferenceBuilder = typeProvider.getJvmTypeReferenceBuilder();
              XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
              final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
                boolean _isMultiValued = commonalityReference.isMultiValued();
                if (_isMultiValued) {
                  XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
                  final Procedure1<XVariableDeclaration> _function_6 = (XVariableDeclaration it_3) -> {
                    it_3.setName(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATES);
                    it_3.setType(jvmTypeReferenceBuilder.typeRef(Iterable.class, jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencedCommonality)))));
                    it_3.setWriteable(false);
                    it_3.setRight(EmfAccessExpressions.getListFeatureValue(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), 
                      this._generationContext.getCorrespondingEReference(commonalityReference)));
                  };
                  final XVariableDeclaration referencedIntermediatesVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_6);
                  EList<XExpression> _expressions = it_2.getExpressions();
                  _expressions.add(referencedIntermediatesVar);
                  JvmFormalParameter _createJvmFormalParameter = TypesFactory.eINSTANCE.createJvmFormalParameter();
                  final Procedure1<JvmFormalParameter> _function_7 = (JvmFormalParameter it_3) -> {
                    it_3.setParameterType(jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencedCommonality))));
                    it_3.setName(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
                  };
                  final JvmFormalParameter referencedIntermediateVar = ObjectExtensions.<JvmFormalParameter>operator_doubleArrow(_createJvmFormalParameter, _function_7);
                  EList<XExpression> _expressions_1 = it_2.getExpressions();
                  XForLoopExpression _createXForLoopExpression = XbaseFactory.eINSTANCE.createXForLoopExpression();
                  final Procedure1<XForLoopExpression> _function_8 = (XForLoopExpression it_3) -> {
                    it_3.setDeclaredParam(referencedIntermediateVar);
                    it_3.setForExpression(XbaseHelper.featureCall(referencedIntermediatesVar));
                    XBlockExpression _createXBlockExpression_1 = XbaseFactory.eINSTANCE.createXBlockExpression();
                    final Procedure1<XBlockExpression> _function_9 = (XBlockExpression it_4) -> {
                      EList<XExpression> _expressions_2 = it_4.getExpressions();
                      XFeatureCall _createRoutineCall = ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                        this.getCheckAttributeReferenceRoutine(participationContext), 
                        typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), XbaseHelper.featureCall(referencedIntermediateVar));
                      _expressions_2.add(_createRoutineCall);
                    };
                    XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_1, _function_9);
                    it_3.setEachExpression(_doubleArrow);
                  };
                  XForLoopExpression _doubleArrow = ObjectExtensions.<XForLoopExpression>operator_doubleArrow(_createXForLoopExpression, _function_8);
                  _expressions_1.add(_doubleArrow);
                } else {
                  XVariableDeclaration _createXVariableDeclaration_1 = XbaseFactory.eINSTANCE.createXVariableDeclaration();
                  final Procedure1<XVariableDeclaration> _function_9 = (XVariableDeclaration it_3) -> {
                    it_3.setName(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
                    it_3.setType(jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencedCommonality))));
                    it_3.setWriteable(false);
                    it_3.setRight(EmfAccessExpressions.getFeatureValue(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), 
                      this._generationContext.getCorrespondingEReference(commonalityReference)));
                  };
                  final XVariableDeclaration referencedIntermediateVar_1 = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration_1, _function_9);
                  EList<XExpression> _expressions_2 = it_2.getExpressions();
                  _expressions_2.add(referencedIntermediateVar_1);
                  EList<XExpression> _expressions_3 = it_2.getExpressions();
                  XFeatureCall _createRoutineCall = ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                    this.getCheckAttributeReferenceRoutine(participationContext), 
                    typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), XbaseHelper.featureCall(referencedIntermediateVar_1));
                  _expressions_3.add(_createRoutineCall);
                }
              };
              _xblockexpression_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
            }
            return _xblockexpression_1;
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.checkAttributeReferenceElementsRemovedRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Checks if any attribute reference for the given contained participation object is no longer fulfilled.
   */
  private FluentRoutineBuilder getCheckAttributeReferenceElementRemovedRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
        final CommonalityReference commonalityReference = participationContext.getDeclaringReference();
        final Commonality referencedCommonality = participationContext.getReferencedCommonality();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("checkAttributeReferenceElementRemoved");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.Literals.EOBJECT, ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE).retrieve(this._generationContext.getChangeClass(referencedCommonality)).correspondingTo(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
              XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
              final Procedure1<XVariableDeclaration> _function_6 = (XVariableDeclaration it_3) -> {
                it_3.setName(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE);
                it_3.setWriteable(false);
                it_3.setRight(this.intermediateContainmentReactionsHelper.getIntermediateContainer(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE), commonalityReference));
              };
              final XVariableDeclaration referencingIntermediateVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_6);
              EList<XExpression> _expressions = it_2.getExpressions();
              _expressions.add(referencingIntermediateVar);
              EList<XExpression> _expressions_1 = it_2.getExpressions();
              XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
              final Procedure1<XIfExpression> _function_7 = (XIfExpression it_3) -> {
                it_3.setIf(XbaseHelper.notEqualsNull(XbaseHelper.featureCall(referencingIntermediateVar), typeProvider));
                it_3.setThen(ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                  this.getCheckAttributeReferenceRoutine(participationContext), 
                  XbaseHelper.featureCall(referencingIntermediateVar), typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE)));
              };
              XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_7);
              _expressions_1.add(_doubleArrow);
            };
            return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.checkAttributeReferenceElementRemovedRoutines.computeIfAbsent(participationContext, _function);
  }

  private FluentRoutineBuilder getMatchAttributeReferenceElementsRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
        CommonalityReferenceMapping _head = IterableExtensions.<CommonalityReferenceMapping>head(participationContext.getReferenceMappings());
        final OperatorReferenceMapping operatorMapping = ((OperatorReferenceMapping) _head);
        final Commonality referencingCommonality = participationContext.getReferencingCommonality();
        final Commonality referencedCommonality = participationContext.getReferencedCommonality();
        final ParticipationContext.ContextClass attributeReferenceRoot = participationContext.getAttributeReferenceRoot();
        final ParticipationClass attributeReferenceRootClass = attributeReferenceRoot.getParticipationClass();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("matchAttributeReferenceElements");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.Literals.EOBJECT, ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).retrieve(this._generationContext.getChangeClass(referencingCommonality)).correspondingTo(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
          it_1.vall(ReactionsGeneratorConventions.REFERENCE_ROOT).retrieveAsserted(this._generationContext.getChangeClass(attributeReferenceRootClass)).correspondingTo(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(attributeReferenceRootClass));
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _xblockexpression_1 = null;
            {
              @Extension
              final JvmTypeReferenceBuilder jvmTypeReferenceBuilder = typeProvider.getJvmTypeReferenceBuilder();
              XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
              final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
                final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext = new ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext(typeProvider);
                XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
                final Procedure1<XVariableDeclaration> _function_6 = (XVariableDeclaration it_3) -> {
                  it_3.setName(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATES);
                  it_3.setType(jvmTypeReferenceBuilder.typeRef(Iterable.class, jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencedCommonality)))));
                  it_3.setWriteable(false);
                  it_3.setRight(this.referenceMappingOperatorHelper.callGetPotentiallyContainedIntermediates(operatorMapping, 
                    typeProvider.variable(ReactionsGeneratorConventions.REFERENCE_ROOT), this._generationContext.getChangeClass(referencedCommonality), operatorContext));
                };
                final XVariableDeclaration referencedIntermediatesVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_6);
                EList<XExpression> _expressions = it_2.getExpressions();
                _expressions.add(referencedIntermediatesVar);
                JvmFormalParameter _createJvmFormalParameter = TypesFactory.eINSTANCE.createJvmFormalParameter();
                final Procedure1<JvmFormalParameter> _function_7 = (JvmFormalParameter it_3) -> {
                  it_3.setParameterType(jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencedCommonality))));
                  it_3.setName(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
                };
                final JvmFormalParameter referencedIntermediateVar = ObjectExtensions.<JvmFormalParameter>operator_doubleArrow(_createJvmFormalParameter, _function_7);
                EList<XExpression> _expressions_1 = it_2.getExpressions();
                XForLoopExpression _createXForLoopExpression = XbaseFactory.eINSTANCE.createXForLoopExpression();
                final Procedure1<XForLoopExpression> _function_8 = (XForLoopExpression it_3) -> {
                  it_3.setDeclaredParam(referencedIntermediateVar);
                  it_3.setForExpression(XbaseHelper.featureCall(referencedIntermediatesVar));
                  XBlockExpression _createXBlockExpression_1 = XbaseFactory.eINSTANCE.createXBlockExpression();
                  final Procedure1<XBlockExpression> _function_9 = (XBlockExpression it_4) -> {
                    EList<XExpression> _expressions_2 = it_4.getExpressions();
                    XFeatureCall _createRoutineCall = ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                      this.getCheckAttributeReferenceRoutine(participationContext), 
                      typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), XbaseHelper.featureCall(referencedIntermediateVar));
                    _expressions_2.add(_createRoutineCall);
                  };
                  XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_1, _function_9);
                  it_3.setEachExpression(_doubleArrow);
                };
                XForLoopExpression _doubleArrow = ObjectExtensions.<XForLoopExpression>operator_doubleArrow(_createXForLoopExpression, _function_8);
                _expressions_1.add(_doubleArrow);
              };
              _xblockexpression_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
            }
            return _xblockexpression_1;
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.matchAttributeReferenceElementsRoutines.computeIfAbsent(participationContext, _function);
  }

  private FluentRoutineBuilder getMatchAttributeReferenceContainerRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
        CommonalityReferenceMapping _head = IterableExtensions.<CommonalityReferenceMapping>head(participationContext.getReferenceMappings());
        final OperatorReferenceMapping operatorMapping = ((OperatorReferenceMapping) _head);
        final Commonality referencingCommonality = participationContext.getReferencingCommonality();
        final Commonality referencedCommonality = participationContext.getReferencedCommonality();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("matchAttributeReferenceContainer");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.Literals.EOBJECT, ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE).retrieve(this._generationContext.getChangeClass(referencedCommonality)).correspondingTo(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _xblockexpression_1 = null;
            {
              @Extension
              final JvmTypeReferenceBuilder jvmTypeReferenceBuilder = typeProvider.getJvmTypeReferenceBuilder();
              XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
              final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
                final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext = new ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext(typeProvider);
                XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
                final Procedure1<XVariableDeclaration> _function_6 = (XVariableDeclaration it_3) -> {
                  it_3.setName(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE);
                  it_3.setType(jvmTypeReferenceBuilder.typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(referencingCommonality))));
                  it_3.setWriteable(false);
                  it_3.setRight(this.referenceMappingOperatorHelper.callGetPotentialContainerIntermediate(operatorMapping, 
                    typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECT), this._generationContext.getChangeClass(referencingCommonality), operatorContext));
                };
                final XVariableDeclaration referencingIntermediateVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_6);
                EList<XExpression> _expressions = it_2.getExpressions();
                _expressions.add(referencingIntermediateVar);
                EList<XExpression> _expressions_1 = it_2.getExpressions();
                XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
                final Procedure1<XIfExpression> _function_7 = (XIfExpression it_3) -> {
                  it_3.setIf(XbaseHelper.notEqualsNull(XbaseHelper.featureCall(referencingIntermediateVar), typeProvider));
                  it_3.setThen(ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                    this.getCheckAttributeReferenceRoutine(participationContext), 
                    XbaseHelper.featureCall(referencingIntermediateVar), typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE)));
                };
                XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_7);
                _expressions_1.add(_doubleArrow);
              };
              _xblockexpression_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
            }
            return _xblockexpression_1;
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.matchAttributeReferenceContainerRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Checks if the participation corresponding to the given 'referenced
   * intermediate' is, according to the attribute reference operator,
   * contained by the participation corresponding to the given 'referencing
   * intermediate'.
   * <p>
   * If the attribute reference is fulfilled, but the referenced intermediate
   * is not actually referenced yet, we insert it into the respective
   * reference of the referencing intermediate.
   * <p>
   * If the attribute reference is not fulfilled, but the referenced
   * intermediate is currently referenced by the referencing intermediate, we
   * move it into the intermediate model root.
   */
  private FluentRoutineBuilder getCheckAttributeReferenceRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
        final CommonalityReference commonalityReference = participationContext.getDeclaringReference();
        final Commonality referencingCommonality = participationContext.getReferencingCommonality();
        final Commonality referencedCommonality = participationContext.getReferencedCommonality();
        final ParticipationContext.ContextClass attributeReferenceRoot = participationContext.getAttributeReferenceRoot();
        final ParticipationClass attributeReferenceRootClass = attributeReferenceRoot.getParticipationClass();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("checkAttributeReference");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(this._generationContext.getChangeClass(referencingCommonality), ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE);
          it_1.model(this._generationContext.getChangeClass(referencedCommonality), ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.REFERENCE_ROOT).retrieveAsserted(this._generationContext.getChangeClass(attributeReferenceRootClass)).correspondingTo(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(attributeReferenceRootClass));
          boolean _isEmpty = participationContext.getAttributeReferenceContainments().isEmpty();
          boolean _not = (!_isEmpty);
          XtendAssertHelper.assertTrue(_not);
          final Consumer<ParticipationContext.ContextContainment<OperatorContainment>> _function_3 = (ParticipationContext.ContextContainment<OperatorContainment> contextContainment) -> {
            boolean _isExternal = contextContainment.getContained().isExternal();
            boolean _not_1 = (!_isExternal);
            XtendAssertHelper.assertTrue(_not_1);
            final ParticipationClass containedClass = contextContainment.getContained().getParticipationClass();
            it_1.vall(ReactionsGeneratorConventions.correspondingVariableName(containedClass)).retrieveAsserted(this._generationContext.getChangeClass(containedClass)).correspondingTo(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(containedClass));
          };
          participationContext.getAttributeReferenceContainments().forEach(_function_3);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
              final Consumer<ParticipationContext.ContextContainment<OperatorContainment>> _function_6 = (ParticipationContext.ContextContainment<OperatorContainment> contextContainment) -> {
                XbaseHelper.join(it_2, this.checkAttributeReferenceContainment(participationContext, contextContainment.getContainment(), routineCallContext, typeProvider));
              };
              participationContext.getAttributeReferenceContainments().forEach(_function_6);
              EList<XExpression> _expressions = it_2.getExpressions();
              XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
              final Procedure1<XIfExpression> _function_7 = (XIfExpression it_3) -> {
                it_3.setIf(XbaseHelper.negated(
                  this.intermediateContainmentReactionsHelper.isIntermediateContainerMatching(typeProvider, 
                    typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE), typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), commonalityReference), typeProvider));
                final EReference commonalityEReference = this._generationContext.getCorrespondingEReference(commonalityReference);
                it_3.setThen(EmfAccessExpressions.insertFeatureValue(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), commonalityEReference, typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE)));
              };
              XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_7);
              _expressions.add(_doubleArrow);
            };
            return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.checkAttributeReferenceRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Checks if the given attribute reference containment is fulfilled.
   * <p>
   * If it is not, but the referenced intermediate is currently contained by the referencing intermediate, we move it
   * into the root of its intermediate model.
   */
  private XBlockExpression checkAttributeReferenceContainment(final ParticipationContext participationContext, final OperatorContainment containment, @Extension final ReactionsHelper.RoutineCallContext routineCallContext, @Extension final TypeProvider typeProvider) {
    final CommonalityReference commonalityReference = participationContext.getDeclaringReference();
    final Commonality referencedCommonality = participationContext.getReferencedCommonality();
    final OperatorReferenceMapping operatorMapping = containment.getMapping();
    final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext = new ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext(typeProvider);
    final ParticipationClass containedClass = containment.getContained();
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
      final Procedure1<XIfExpression> _function_1 = (XIfExpression it_1) -> {
        it_1.setIf(XbaseHelper.negated(
          this.referenceMappingOperatorHelper.callIsContained(operatorMapping, typeProvider.variable(ReactionsGeneratorConventions.REFERENCE_ROOT), 
            typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(containedClass)), operatorContext), typeProvider));
        XBlockExpression _createXBlockExpression_1 = XbaseFactory.eINSTANCE.createXBlockExpression();
        final Procedure1<XBlockExpression> _function_2 = (XBlockExpression it_2) -> {
          EList<XExpression> _expressions_1 = it_2.getExpressions();
          XIfExpression _createXIfExpression_1 = XbaseFactory.eINSTANCE.createXIfExpression();
          final Procedure1<XIfExpression> _function_3 = (XIfExpression it_3) -> {
            it_3.setIf(this.intermediateContainmentReactionsHelper.isIntermediateContainerMatching(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE), 
              typeProvider.variable(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE), commonalityReference));
            it_3.setThen(ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
              this.insertIntermediateRoutineBuilderProvider.getInsertIntermediateRoutine(this.segment, CommonalitiesLanguageModelExtensions.getConcept(referencedCommonality)), 
              typeProvider.variable(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE)));
          };
          XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression_1, _function_3);
          _expressions_1.add(_doubleArrow);
          EList<XExpression> _expressions_2 = it_2.getExpressions();
          XReturnExpression _createXReturnExpression = XbaseFactory.eINSTANCE.createXReturnExpression();
          _expressions_2.add(_createXReturnExpression);
        };
        XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_1, _function_2);
        it_1.setThen(_doubleArrow);
      };
      XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_1);
      _expressions.add(_doubleArrow);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  /**
   * Invoked on intermediate creation.
   */
  private FluentRoutineBuilder getMatchAttributeReferenceContainerForIntermediateRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      XtendAssertHelper.assertTrue(participationContext.isForAttributeReferenceMapping());
      final Commonality referencedCommonality = participationContext.getReferencedCommonality();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("matchAttributeReferenceContainerForIntermediate");
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(this._generationContext.getChangeClass(referencedCommonality), ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE);
      };
      final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
        final ParticipationContext.ContextContainment<OperatorContainment> attributeReferenceContainment = IterableExtensions.<ParticipationContext.ContextContainment<OperatorContainment>>head(participationContext.getAttributeReferenceContainments());
        XtendAssertHelper.assertTrue((attributeReferenceContainment != null));
        final ParticipationClass referencedClass = attributeReferenceContainment.getContained().getParticipationClass();
        it_1.vall(ReactionsGeneratorConventions.PARTICIPATION_OBJECT).retrieve(this._generationContext.getChangeClass(referencedClass)).correspondingTo(ReactionsGeneratorConventions.REFERENCED_INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(referencedClass));
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        FluentRoutineBuilder _matchAttributeReferenceContainerRoutine = this.getMatchAttributeReferenceContainerRoutine(participationContext);
        final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it_2) -> {
          return it_2.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_4);
        it_1.call(_matchAttributeReferenceContainerRoutine, _routineCallParameter);
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3);
    };
    return this.matchAttributeReferenceContainerForIntermediateRoutines.computeIfAbsent(participationContext, _function);
  }

  private List<FluentReactionBuilder> reactionsForAttributeReferenceChange(final ParticipationContext participationContext, final OperatorContainment containment, final ReferenceMappingOperand operand) {
    if (operand instanceof ParticipationAttributeOperand) {
      return _reactionsForAttributeReferenceChange(participationContext, containment, (ParticipationAttributeOperand)operand);
    } else if (operand instanceof ReferencedParticipationAttributeOperand) {
      return _reactionsForAttributeReferenceChange(participationContext, containment, (ReferencedParticipationAttributeOperand)operand);
    } else if (operand != null) {
      return _reactionsForAttributeReferenceChange(participationContext, containment, operand);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(participationContext, containment, operand).toString());
    }
  }
}
