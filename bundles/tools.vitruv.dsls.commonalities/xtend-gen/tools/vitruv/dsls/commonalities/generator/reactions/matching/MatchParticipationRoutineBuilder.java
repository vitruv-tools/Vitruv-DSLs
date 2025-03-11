package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.helper.ContainmentHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.condition.CheckedParticipationConditionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.CreateIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.reference.ReferenceMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.Containment;
import tools.vitruv.dsls.commonalities.participation.OperatorContainment;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.commonalities.participation.ReferenceContainment;
import tools.vitruv.dsls.commonalities.runtime.BooleanResult;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.matching.ContainmentContext;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationMatcher;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
class MatchParticipationRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<MatchParticipationRoutineBuilder> {
    @Override
    protected MatchParticipationRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<MatchParticipationRoutineBuilder>injectMembers(new MatchParticipationRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getMatchParticipationRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      return this.getFor(segment).getMatchParticipationRoutine(participationContext);
    }
  }

  @Inject
  @Extension
  private ContainmentHelper containmentHelper;

  @Inject
  @Extension
  private ReferenceMappingOperatorHelper referenceMappingOperatorHelper;

  @Inject
  @Extension
  private CreateIntermediateRoutineBuilder.Provider createIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private CheckedParticipationConditionsHelper checkedParticipationConditionsHelper;

  private final FluentReactionsSegmentBuilder segment;

  private final Map<ParticipationContext, FluentRoutineBuilder> matchParticipationRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private MatchParticipationRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
    this.segment = segment;
  }

  MatchParticipationRoutineBuilder() {
    this.segment = null;
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  /**
   * Sets up the {@link ContainmentContext} for the given
   * {@link ParticipationContext}, invokes the {@link ParticipationMatcher}
   * and instantiates the corresponding Commonality if a match is found.
   * <p>
   * If there are multiple candidates for a structural match, the first found
   * candidate that also fulfills the non-structural conditions is used to
   * establish the participation.
   * <p>
   * Note: We don't check if there already is a corresponding Intermediate
   * for the given start object. If the participation context is for a
   * commonality reference, the start object may be a containment context
   * root object specified by the mappings, which already corresponds to an
   * Intermediate (possibly even the same type of Intermediate that the
   * participation will correspond to in case we find a match). Otherwise, if
   * the participation context is not for a commonality reference, the
   * {@link ParticipationMatcher} will already verify that the participation
   * objects (including the passed start object) do not already correspond to
   * an Intermediate.
   */
  public FluentRoutineBuilder getMatchParticipationRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Participation participation = participationContext.getParticipation();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("matchParticipation_");
        String _name = participation.getName();
        _builder.append(_name);
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(it);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.eINSTANCE.getEObject(), ReactionsGeneratorConventions.START_OBJECT);
          it_1.plain(Boolean.class, ReactionsGeneratorConventions.FOLLOW_ATTRIBUTE_REFERENCES);
          it_1.plain(BooleanResult.class, ReactionsGeneratorConventions.FOUND_MATCH_RESULT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
            return this.matchParticipation(participationContext, routineCallContext, typeProvider);
          };
          routineCallContext.setCallerContext(it_1.execute(_function_3));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2));
      }
      return _xblockexpression;
    };
    return this.matchParticipationRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Returns a block expressions which sets up a ContainmentContext for the
   * participation context, invokes the ParticipationMatcher and then creates
   * a new intermediate for the first found candidate match which also
   * fulfills the non-structural participation conditions.
   */
  private XBlockExpression matchParticipation(final ParticipationContext participationContext, final ReactionsHelper.RoutineCallContext routineCallContext, @Extension final TypeProvider typeProvider) {
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      final ContainmentContextBuilder containmentContextBuilder = this.setupContainmentContext(participationContext, typeProvider);
      final XVariableDeclaration containmentContextVar = containmentContextBuilder.getContainmentContextVar();
      XbaseHelper.join(it, containmentContextBuilder.getResultExpressions());
      final JvmDeclaredType participationMatcherType = typeProvider.<JvmDeclaredType>imported(JvmTypeProviderHelper.findDeclaredType(typeProvider, ParticipationMatcher.class));
      XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_1 = (XVariableDeclaration it_1) -> {
        it_1.setName("participationMatcher");
        XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
        final Procedure1<XConstructorCall> _function_2 = (XConstructorCall it_2) -> {
          it_2.setConstructor(JvmTypeProviderHelper.findConstructor(participationMatcherType));
          it_2.setExplicitConstructorCall(true);
          EList<XExpression> _arguments = it_2.getArguments();
          List<XExpression> _expressions = XbaseHelper.expressions(
            XbaseHelper.featureCall(containmentContextVar), 
            typeProvider.variable(ReactionsGeneratorConventions.START_OBJECT), 
            typeProvider.variable(ReactionsGeneratorConventions.FOLLOW_ATTRIBUTE_REFERENCES), 
            typeProvider.correspondenceModel(), 
            typeProvider.resourceAccess());
          Iterables.<XExpression>addAll(_arguments, _expressions);
        };
        XConstructorCall _doubleArrow = ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function_2);
        it_1.setRight(_doubleArrow);
      };
      final XVariableDeclaration participationMatcherVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_1);
      EList<XExpression> _expressions = it.getExpressions();
      _expressions.add(participationMatcherVar);
      final JvmOperation matchObjectsMethod = JvmTypeProviderHelper.findMethod(participationMatcherType, "matchObjects");
      XVariableDeclaration _createXVariableDeclaration_1 = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_2 = (XVariableDeclaration it_1) -> {
        it_1.setName("candidateMatches");
        it_1.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(Iterable.class, typeProvider.getJvmTypeReferenceBuilder().typeRef(ParticipationObjects.class)));
        it_1.setRight(XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(participationMatcherVar), matchObjectsMethod));
      };
      final XVariableDeclaration candidateMatchesVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration_1, _function_2);
      EList<XExpression> _expressions_1 = it.getExpressions();
      _expressions_1.add(candidateMatchesVar);
      JvmFormalParameter _createJvmFormalParameter = TypesFactory.eINSTANCE.createJvmFormalParameter();
      final Procedure1<JvmFormalParameter> _function_3 = (JvmFormalParameter it_1) -> {
        it_1.setParameterType(typeProvider.getJvmTypeReferenceBuilder().typeRef(ParticipationObjects.class));
        it_1.setName(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
      };
      final JvmFormalParameter participationObjectsVar = ObjectExtensions.<JvmFormalParameter>operator_doubleArrow(_createJvmFormalParameter, _function_3);
      EList<XExpression> _expressions_2 = it.getExpressions();
      XForLoopExpression _createXForLoopExpression = XbaseFactory.eINSTANCE.createXForLoopExpression();
      final Procedure1<XForLoopExpression> _function_4 = (XForLoopExpression it_1) -> {
        it_1.setDeclaredParam(participationObjectsVar);
        it_1.setForExpression(XbaseHelper.featureCall(candidateMatchesVar));
        XBlockExpression _createXBlockExpression_1 = XbaseFactory.eINSTANCE.createXBlockExpression();
        final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
          EList<XExpression> _expressions_3 = it_2.getExpressions();
          XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
          final Procedure1<XIfExpression> _function_6 = (XIfExpression it_3) -> {
            it_3.setIf(this.checkNonStructuralConditions(participationContext, XbaseHelper.featureCall(participationObjectsVar), typeProvider));
            XBlockExpression _createXBlockExpression_2 = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_7 = (XBlockExpression it_4) -> {
              EList<XExpression> _expressions_4 = it_4.getExpressions();
              XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(typeProvider.variable(ReactionsGeneratorConventions.FOUND_MATCH_RESULT));
              final Procedure1<XMemberFeatureCall> _function_8 = (XMemberFeatureCall it_5) -> {
                it_5.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, BooleanResult.class), "setValue"));
                EList<XExpression> _memberCallArguments = it_5.getMemberCallArguments();
                XBooleanLiteral _booleanLiteral = XbaseHelper.booleanLiteral(true);
                _memberCallArguments.add(_booleanLiteral);
                it_5.setExplicitOperationCall(true);
              };
              XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function_8);
              _expressions_4.add(_doubleArrow);
              EList<XExpression> _expressions_5 = it_4.getExpressions();
              XFeatureCall _createRoutineCall = ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                this.createIntermediateRoutineBuilderProvider.getCreateIntermediateRoutine(this.segment, participationContext), XbaseHelper.featureCall(participationObjectsVar));
              _expressions_5.add(_createRoutineCall);
              EList<XExpression> _expressions_6 = it_4.getExpressions();
              XReturnExpression _createXReturnExpression = XbaseFactory.eINSTANCE.createXReturnExpression();
              _expressions_6.add(_createXReturnExpression);
            };
            XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_2, _function_7);
            it_3.setThen(_doubleArrow);
          };
          XIfExpression _doubleArrow = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_6);
          _expressions_3.add(_doubleArrow);
        };
        XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_1, _function_5);
        it_1.setEachExpression(_doubleArrow);
      };
      XForLoopExpression _doubleArrow = ObjectExtensions.<XForLoopExpression>operator_doubleArrow(_createXForLoopExpression, _function_4);
      _expressions_2.add(_doubleArrow);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  private boolean isRootCommonalityParticipation(final ParticipationContext participationContext) {
    return (participationContext.isRootContext() && CommonalitiesLanguageModelExtensions.isCommonalityParticipation(participationContext.getParticipation()));
  }

  private ContainmentContextBuilder setupContainmentContext(final ParticipationContext participationContext, @Extension final TypeProvider typeProvider) {
    final Participation participation = participationContext.getParticipation();
    final boolean isRootCommonalityParticipation = this.isRootCommonalityParticipation(participationContext);
    final ContainmentContextBuilder containmentContextBuilder = new ContainmentContextBuilder(typeProvider);
    containmentContextBuilder.newContainmentContext("containmentContext");
    boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
    if (_isForReferenceMapping) {
      final EClass rootIntermediateType = this._generationContext.getChangeClass(participationContext.getReferencingCommonality());
      containmentContextBuilder.setRootIntermediateType(rootIntermediateType);
    }
    if (isRootCommonalityParticipation) {
      final EClass intermediateRootEClass = this._generationContext.getIntermediateMetamodelRootClass(CommonalitiesLanguageModelExtensions.getParticipationConcept(participation));
      containmentContextBuilder.addNode(ReactionsGeneratorConventions.INTERMEDIATE_ROOT, intermediateRootEClass, null);
    }
    final Consumer<ParticipationContext.ContextClass> _function = (ParticipationContext.ContextClass contextClass) -> {
      final ParticipationClass participationClass = contextClass.getParticipationClass();
      containmentContextBuilder.addNode(ReactionsGeneratorConventions.getName(contextClass), this._generationContext.getChangeClass(participationClass), 
        ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
    };
    participationContext.getClasses().forEach(_function);
    boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
    if (_isForAttributeReferenceMapping) {
      final ParticipationContext.ContextClass attributeReferenceRoot = participationContext.getAttributeReferenceRoot();
      final ParticipationClass attributeReferenceRootClass = attributeReferenceRoot.getParticipationClass();
      containmentContextBuilder.setAttributeReferenceRootNode(ReactionsGeneratorConventions.getName(attributeReferenceRoot), 
        this._generationContext.getChangeClass(attributeReferenceRootClass), ReactionsGeneratorConventions.getCorrespondenceTag(attributeReferenceRootClass));
    }
    if (isRootCommonalityParticipation) {
      final String containerName = ReactionsGeneratorConventions.INTERMEDIATE_ROOT;
      final EReference containmentEReference = IntermediateModelBasePackage.Literals.ROOT__INTERMEDIATES;
      final Consumer<ParticipationClass> _function_1 = (ParticipationClass contained) -> {
        containmentContextBuilder.addReferenceEdge(containerName, contained.getName(), containmentEReference);
      };
      ParticipationContextHelper.getNonRootBoundaryClasses(participation).forEach(_function_1);
    }
    final Consumer<ParticipationContext.ContextContainment<?>> _function_2 = (ParticipationContext.ContextContainment<?> contextContainment) -> {
      final Containment containment = contextContainment.getContainment();
      if ((containment instanceof ReferenceContainment)) {
        containmentContextBuilder.addReferenceEdge(ReactionsGeneratorConventions.getName(contextContainment.getContainer()), ReactionsGeneratorConventions.getName(contextContainment.getContained()), this.containmentHelper.getEReference(((ReferenceContainment)containment)));
      } else {
        if ((containment instanceof OperatorContainment)) {
          final OperatorReferenceMapping operatorMapping = ((OperatorContainment)containment).getMapping();
          final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext = new ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext(typeProvider);
          final XConstructorCall operatorInstance = this.referenceMappingOperatorHelper.constructOperator(operatorMapping, operatorContext);
          boolean _isAttributeReference = CommonalitiesLanguageModelExtensions.isAttributeReference(operatorMapping);
          if (_isAttributeReference) {
            containmentContextBuilder.addAttributeReferenceEdge(ReactionsGeneratorConventions.getName(contextContainment.getContained()), operatorInstance);
          } else {
            containmentContextBuilder.addOperatorEdge(ReactionsGeneratorConventions.getName(contextContainment.getContainer()), ReactionsGeneratorConventions.getName(contextContainment.getContained()), operatorInstance);
          }
        } else {
          String _name = containment.getClass().getName();
          String _plus = ("Unexpected containment type: " + _name);
          throw new IllegalStateException(_plus);
        }
      }
    };
    participationContext.getContainments().forEach(_function_2);
    return containmentContextBuilder;
  }

  private XExpression checkNonStructuralConditions(final ParticipationContext participationContext, final XFeatureCall participationObjects, @Extension final TypeProvider typeProvider) {
    return this.checkedParticipationConditionsHelper.checkParticipationConditions(participationContext, typeProvider, participationObjects);
  }
}
