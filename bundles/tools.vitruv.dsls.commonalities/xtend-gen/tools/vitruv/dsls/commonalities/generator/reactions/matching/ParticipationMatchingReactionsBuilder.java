package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.helper.ContainmentHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.CreateIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertReferencedIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.AttributeReferenceMatchingReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.MatchParticipationRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.ParticipationConditionMatchingReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
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
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationMatcher;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

/**
 * Generates the reactions and routines that match participations in given
 * contexts. These reactions are responsible for detecting if a participation
 * exists, according to the specified structural and non-structural conditions,
 * and then instantiate the corresponding Commonality.
 * <p>
 * The contexts are those specified by participations themselves or contexts
 * for participations that are referenced in commonality reference mappings.
 * See also {@link ParticipationContextHelper} and {@link ParticipationMatcher}
 * regarding the different contexts in which a participation may exist and
 * therefore need to be matched.
 * <p>
 * One reactions segment may contain the matching reactions and routines for
 * multiple participation contexts that involve the same participation (for
 * example when a commonality defines multiple references to the same other
 * commonality). To not generate common routines multiple times, we keep track
 * of the already generated shared routines for that segment. This requires
 * that there only exists one instance of this class per reactions segment.
 * However, we also expect that one reactions segment contains the matching
 * reactions and routines for at most one participation's own context.
 * <p>
 * Rationale for taking the containment context into account:</br>
 * The objects specified by participation classes may have different roles
 * depending on the context in which they exist. For instance, a Java class may
 * represent a PCM component, PCM system or PCM datatype, depending on the Java
 * package in which it resides. Even within a single Commonality, a metaclass
 * may occur multiple times and therefore require aliasing of the respective
 * participation classes that represent the metaclass in those different roles.
 * <p>
 * Instead of reacting to individual object creations and then having to ask
 * the user up-front about the role in which the newly created object is going
 * to be used, we defer the participation instantiation until all the
 * structural and non-structural conditions specified by the participation are
 * fulfilled.
 * <p>
 * The structural conditions are derived from the participation's containment
 * relations and conditions (<code>'in'</code>) and commonality reference
 * mappings. These form one or multiple trees of containment relationships
 * between the participation's objects and the context's root objects. The
 * non-structural conditions are all other conditions. Those may for example
 * represent requirements on the attributes of the involved objects.
 * <p>
 * Since every object inside a model is at some point either contained within
 * another object (its container) or a resource, the matching of a
 * participation always involves at least one containment relationship (even if
 * it specifies only a single participation class). It is therefore sufficient
 * to only react to inserts and removes in order to detect when a
 * participation's structural pattern has been established or decomposed again.
 * It is not required to react to individual object creations and deletions.
 * <p>
 * Matching a participation consists of the following two steps:
 * <ol>
 * <li>On every structural change (insert into a containment reference) that
 * may involve an object of the participation that we try to match: Check if we
 * find a set of objects that matches the participations structural conditions.
 * See {@link ParticipationMatcher} for the details about the actual matching
 * procedure. The result of this are candidate matches of model objects
 * according to the participation's containment context.
 * <p>
 * TODO: Currently we only supports participations with a single Resource root
 * class. A workaround for participations with multiple Resource roots could be
 * to split the commonality into separate child commonalities and one parent
 * commonality that uses those as participations.
 * <li>For every found candidate match, we check if it also fulfills the
 * non-structural (i.e. attribute) conditions. The first found candidate match
 * that fulfills also fulfills those is used to establish the participation.
 * </ol>
 * <p>
 * TODO: One consequence of our contextual matching is that we need to deal
 * with cases in which objects get moved from one container to another (Eg. a
 * Java package gets moved from one parent package to another). Currently we
 * will destroy the participation, including the corresponding Commonality
 * instance and any other corresponding participation instances, when the
 * original containment relation is removed. And we then re-establish the
 * participation and rebuild the commonality afterwards during the insertion
 * into the new container. This is prone to information loss in the commonality
 * instance and any corresponding participations.</br>
 * One solution to this could be to defer the actual deletion of the
 * commonality instance and match and re-attach it again during a later
 * insertion. But this hasn't been implemented yet.
 * <p>
 * A similar consequence is that any model change that may break any of the
 * structural or non-structural conditions will lead to the deletion of the
 * commonality instance and the deletion of all corresponding participations.
 * This is prone to errors in a user's model editing leading to unexpected
 * deletions of parts in other models. It is therefore currently recommended to
 * limit any checked conditions to those that are absolutely necessary for the
 * participation's existence.</br>
 * One solution to this could be to prompt the user before performing any
 * deletions of other participations.
 * <p>
 * Other limitations / Possible TODOs:
 * <ul>
 * <li>We might not properly support commonalities that define multiple
 * participations for the same domain currently.
 * <li>Matching needs to happen for various contexts (own context and external
 * reference mappings) and on various occasions (containment reference changes
 * and attribute changes). Currently we generate very similar matching
 * reactions and routines for each of these contexts and occasions, which leads
 * to a lot of duplication in the generated reactions code. Ideally one could
 * generate reactions and routines for the common aspects only once (i.e.
 * anything affecting the non-root portion of the matching, as well as the
 * commonality creation and setup once a participation has been matched) and
 * then invoke those in the various contexts.
 * </ul>
 */
@SuppressWarnings("all")
public class ParticipationMatchingReactionsBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<ParticipationMatchingReactionsBuilder> {
    @Override
    protected ParticipationMatchingReactionsBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<ParticipationMatchingReactionsBuilder>injectMembers(new ParticipationMatchingReactionsBuilder(segment));
    }
  }

  private static final Logger logger = Logger.getLogger(ParticipationMatchingReactionsBuilder.class);

  @Inject
  @Extension
  private ContainmentHelper containmentHelper;

  @Inject
  @Extension
  private MatchParticipationRoutineBuilder.Provider matchParticipationRoutineBuilderProvider;

  @Inject
  @Extension
  private CreateIntermediateRoutineBuilder.Provider createIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private InsertIntermediateRoutineBuilder.Provider insertIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private InsertReferencedIntermediateRoutineBuilder.Provider insertReferencedIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private AttributeReferenceMatchingReactionsBuilder.Provider attributeReferenceMatchingReactionsBuilderProvider;

  @Inject
  @Extension
  private ParticipationConditionMatchingReactionsBuilder.Provider participationConditionMatchingReactionsBuilderProvider;

  private final FluentReactionsSegmentBuilder segment;

  private final Map<ParticipationContext, FluentRoutineBuilder> matchManyParticipationsRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private final Map<ParticipationContext, FluentRoutineBuilder> matchSubParticipationsRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private ParticipationMatchingReactionsBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
    this.segment = segment;
  }

  ParticipationMatchingReactionsBuilder() {
    this.segment = null;
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  /**
   * Generates the reactions and routines for matching participations in the
   * given context.
   */
  void generateMatchingReactions(final ParticipationContext participationContext) {
    ParticipationMatchingReactionsBuilder.logger.debug(this.getLogMessage(participationContext));
    this.generateRoutines(participationContext);
    this.generateReactions(participationContext);
  }

  private String getLogMessage(final ParticipationContext participationContext) {
    boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
    if (_isForReferenceMapping) {
      final Participation participation = participationContext.getParticipation();
      final CommonalityReference reference = participationContext.getDeclaringReference();
      final Commonality commonality = participationContext.getReferencingCommonality();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Commonality ");
      _builder.append(commonality);
      _builder.append(": Generating matching reactions for participation \'");
      _builder.append(participation);
      _builder.append("\' and reference \'");
      String _name = reference.getName();
      _builder.append(_name);
      _builder.append("\'.");
      return _builder.toString();
    } else {
      final Participation participation_1 = participationContext.getParticipation();
      final Commonality commonality_1 = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation_1);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Commonality ");
      _builder_1.append(commonality_1);
      _builder_1.append(": Generating matching reactions for participation \'");
      _builder_1.append(participation_1);
      _builder_1.append("\'.");
      return _builder_1.toString();
    }
  }

  private FluentReactionsSegmentBuilder generateRoutines(final ParticipationContext participationContext) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      final Participation participation = participationContext.getParticipation();
      final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
      FluentRoutineBuilder _matchParticipationRoutine = this.matchParticipationRoutineBuilderProvider.getMatchParticipationRoutine(this.segment, participationContext);
      this.segment.operator_add(_matchParticipationRoutine);
      FluentRoutineBuilder _createIntermediateRoutine = this.createIntermediateRoutineBuilderProvider.getCreateIntermediateRoutine(this.segment, participationContext);
      this.segment.operator_add(_createIntermediateRoutine);
      boolean _isRootContext = participationContext.isRootContext();
      if (_isRootContext) {
        FluentRoutineBuilder _insertIntermediateRoutine = this.insertIntermediateRoutineBuilderProvider.getInsertIntermediateRoutine(this.segment, CommonalitiesLanguageModelExtensions.getConcept(commonality));
        this.segment.operator_add(_insertIntermediateRoutine);
      }
      FluentReactionsSegmentBuilder _xifexpression = null;
      boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
      if (_isForReferenceMapping) {
        FluentReactionsSegmentBuilder _xblockexpression_1 = null;
        {
          final CommonalityReference reference = participationContext.getDeclaringReference();
          FluentRoutineBuilder _insertReferencedIntermediateRoutine = this.insertReferencedIntermediateRoutineBuilderProvider.getInsertReferencedIntermediateRoutine(this.segment, reference);
          this.segment.operator_add(_insertReferencedIntermediateRoutine);
          FluentRoutineBuilder _matchManyParticipationsRoutine = this.getMatchManyParticipationsRoutine(participationContext);
          _xblockexpression_1 = this.segment.operator_add(_matchManyParticipationsRoutine);
        }
        _xifexpression = _xblockexpression_1;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void generateReactions(final ParticipationContext participationContext) {
    boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
    if (_isForAttributeReferenceMapping) {
      this.attributeReferenceMatchingReactionsBuilderProvider.generateAttributeReferenceMatchingReactions(this.segment, participationContext);
    } else {
      this.generateContainmentReferenceMatchingReactions(participationContext);
    }
    this.participationConditionMatchingReactionsBuilderProvider.generateParticipationConditionReactions(this.segment, participationContext);
  }

  private void generateContainmentReferenceMatchingReactions(final ParticipationContext participationContext) {
    boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
    boolean _not = (!_isForAttributeReferenceMapping);
    XtendAssertHelper.assertTrue(_not);
    final Consumer<ParticipationContext.ContextContainment<?>> _function = (ParticipationContext.ContextContainment<?> contextContainment) -> {
      final Containment containment = contextContainment.getContainment();
      if ((containment instanceof ReferenceContainment)) {
        FluentReactionBuilder _reactionForParticipationClassInsert = this.reactionForParticipationClassInsert(participationContext, ((ReferenceContainment)containment));
        this.segment.operator_add(_reactionForParticipationClassInsert);
        FluentReactionBuilder _reactionForParticipationClassRemove = this.reactionForParticipationClassRemove(participationContext, ((ReferenceContainment)containment));
        this.segment.operator_add(_reactionForParticipationClassRemove);
      } else {
        if ((containment instanceof OperatorContainment)) {
          final OperatorReferenceMapping operatorMapping = ((OperatorContainment)containment).getMapping();
          boolean _isAttributeReference = CommonalitiesLanguageModelExtensions.isAttributeReference(operatorMapping);
          if (_isAttributeReference) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Not expecting attribute reference containments for ");
            _builder.append("non-attribute-reference participation context");
            throw new IllegalStateException(_builder.toString());
          } else {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("Operator reference mappings for non-attribute ");
            _builder_1.append("references are not supported yet");
            throw new UnsupportedOperationException(_builder_1.toString());
          }
        }
      }
    };
    participationContext.getContainments().forEach(_function);
  }

  /**
   * When one of the containment relationships between potential
   * participation objects is established, check if we can match the
   * participation in the given context.
   */
  private FluentReactionBuilder reactionForParticipationClassInsert(final ParticipationContext participationContext, final ReferenceContainment containment) {
    final Participation participation = participationContext.getParticipation();
    final ParticipationClass containerClass = containment.getContainer();
    final ParticipationClass containedClass = containment.getContained();
    FluentReactionBuilder.RoutineCallBuilder reaction = null;
    boolean _isForResource = CommonalitiesLanguageModelExtensions.isForResource(containerClass);
    if (_isForResource) {
      StringConcatenation _builder = new StringConcatenation();
      String _name = participation.getName();
      _builder.append(_name);
      _builder.append("_");
      String _name_1 = containedClass.getName();
      _builder.append(_name_1);
      _builder.append("_insertedAtRoot");
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      reaction = ReactionsHelper.afterElementInsertedAsRoot(this._reactionsGenerationContext.getCreate().reaction(_builder.toString()), this._generationContext.getChangeClass(containedClass));
    } else {
      final EReference containmentEReference = this.containmentHelper.getEReference(containment);
      StringConcatenation _builder_1 = new StringConcatenation();
      String _name_2 = participation.getName();
      _builder_1.append(_name_2);
      _builder_1.append("_");
      String _name_3 = containedClass.getName();
      _builder_1.append(_name_3);
      _builder_1.append("_insertedAt_");
      String _name_4 = containerClass.getName();
      _builder_1.append(_name_4);
      _builder_1.append("_");
      String _name_5 = containmentEReference.getName();
      _builder_1.append(_name_5);
      String _reactionNameSuffix_1 = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder_1.append(_reactionNameSuffix_1);
      reaction = this._reactionsGenerationContext.getCreate().reaction(_builder_1.toString()).afterElement(this._generationContext.getChangeClass(containedClass)).insertedIn(this._generationContext.getChangeClass(containerClass), containmentEReference);
    }
    final Function<TypeProvider, XExpression> _function = (TypeProvider it) -> {
      return it.newValue();
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function);
    return this.callMatchingRoutine(reaction, participationContext, _routineCallParameter, false);
  }

  private FluentReactionBuilder callMatchingRoutine(final FluentReactionBuilder.RoutineCallBuilder reaction, final ParticipationContext participationContext, final FluentRoutineBuilder.RoutineCallParameter startObject, final boolean followAttributeReferences) {
    FluentRoutineBuilder _matchParticipationRoutine = this.matchParticipationRoutineBuilderProvider.getMatchParticipationRoutine(this.segment, participationContext);
    final Function<TypeProvider, XExpression> _function = (TypeProvider it) -> {
      return XbaseHelper.booleanLiteral(followAttributeReferences);
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function);
    final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it) -> {
      return XbaseHelper.noArgsConstructorCall(JvmTypeProviderHelper.findDeclaredType(it, BooleanResult.class));
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_1);
    return reaction.call(_matchParticipationRoutine, startObject, _routineCallParameter, _routineCallParameter_1);
  }

  /**
   * When one of the the containment relationships between potential
   * participation objects is removed, delete the corresponding commonality
   * instance (if there is one).
   */
  private FluentReactionBuilder reactionForParticipationClassRemove(final ParticipationContext participationContext, final ReferenceContainment containment) {
    final Participation participation = participationContext.getParticipation();
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    final ParticipationClass containerClass = containment.getContainer();
    final ParticipationClass containedClass = containment.getContained();
    FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = null;
    boolean _isForResource = CommonalitiesLanguageModelExtensions.isForResource(containerClass);
    if (_isForResource) {
      StringConcatenation _builder = new StringConcatenation();
      String _name = participation.getName();
      _builder.append(_name);
      _builder.append("_");
      String _name_1 = containedClass.getName();
      _builder.append(_name_1);
      _builder.append("_removedFromRoot");
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      reaction = ReactionsHelper.afterElementRemovedAsRoot(this._reactionsGenerationContext.getCreate().reaction(_builder.toString()), this._generationContext.getChangeClass(containedClass));
    } else {
      final EReference containmentEReference = this.containmentHelper.getEReference(containment);
      StringConcatenation _builder_1 = new StringConcatenation();
      String _name_2 = participation.getName();
      _builder_1.append(_name_2);
      _builder_1.append("_");
      String _name_3 = containedClass.getName();
      _builder_1.append(_name_3);
      _builder_1.append("_removedFrom_");
      String _name_4 = containerClass.getName();
      _builder_1.append(_name_4);
      _builder_1.append("_");
      String _name_5 = containmentEReference.getName();
      _builder_1.append(_name_5);
      String _reactionNameSuffix_1 = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder_1.append(_reactionNameSuffix_1);
      reaction = this._reactionsGenerationContext.getCreate().reaction(_builder_1.toString()).afterElement(this._generationContext.getChangeClass(containedClass)).removedFrom(this._generationContext.getChangeClass(containerClass), containmentEReference);
    }
    final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function = (FluentRoutineBuilder.RoutineStartBuilder it) -> {
      final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
        it_1.vall(ReactionsGeneratorConventions.INTERMEDIATE).retrieve(this._generationContext.getChangeClass(commonality)).correspondingTo().oldValue();
        it_1.vall(ReactionsGeneratorConventions.RESOURCE_BRIDGE).retrieve(ResourcesPackage.eINSTANCE.getIntermediateResourceBridge()).correspondingTo().oldValue();
      };
      it.match(_function_1);
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
          return typeProvider.oldValue();
        };
        it_1.removeCorrespondenceBetween(_function_3).and(ReactionsGeneratorConventions.RESOURCE_BRIDGE).taggedWithAnything();
        it_1.delete(ReactionsGeneratorConventions.INTERMEDIATE);
      };
      it.update(_function_2);
    };
    return reaction.call(_function);
  }

  /**
   * Invokes the matching routine as often as new participation matches are
   * found.
   * <p>
   * This only really makes sense if the matching is invoked for the root
   * object of a reference mapping participation context.
   */
  private FluentRoutineBuilder getMatchManyParticipationsRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Participation participation = participationContext.getParticipation();
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("matchManyParticipations_");
        String _name = participation.getName();
        _builder.append(_name);
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(it);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.eINSTANCE.getEObject(), ReactionsGeneratorConventions.REFERENCE_ROOT);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_4 = (XBlockExpression it_2) -> {
              XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
              final Procedure1<XVariableDeclaration> _function_5 = (XVariableDeclaration it_3) -> {
                it_3.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(BooleanResult.class));
                it_3.setName(ReactionsGeneratorConventions.FOUND_MATCH_RESULT);
                it_3.setRight(XbaseHelper.noArgsConstructorCall(typeProvider.<JvmDeclaredType>imported(JvmTypeProviderHelper.findDeclaredType(typeProvider, BooleanResult.class))));
              };
              final XVariableDeclaration resultVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_5);
              EList<XExpression> _expressions = it_2.getExpressions();
              _expressions.add(resultVar);
              EList<XExpression> _expressions_1 = it_2.getExpressions();
              XDoWhileExpression _createXDoWhileExpression = XbaseFactory.eINSTANCE.createXDoWhileExpression();
              final Procedure1<XDoWhileExpression> _function_6 = (XDoWhileExpression it_3) -> {
                XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(resultVar));
                final Procedure1<XMemberFeatureCall> _function_7 = (XMemberFeatureCall it_4) -> {
                  it_4.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, BooleanResult.class), "getValue"));
                  it_4.setExplicitOperationCall(true);
                };
                XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function_7);
                it_3.setPredicate(_doubleArrow);
                XBlockExpression _createXBlockExpression_1 = XbaseFactory.eINSTANCE.createXBlockExpression();
                final Procedure1<XBlockExpression> _function_8 = (XBlockExpression it_4) -> {
                  EList<XExpression> _expressions_2 = it_4.getExpressions();
                  XMemberFeatureCall _memberFeatureCall_1 = XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(resultVar));
                  final Procedure1<XMemberFeatureCall> _function_9 = (XMemberFeatureCall it_5) -> {
                    it_5.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, BooleanResult.class), "setValue"));
                    EList<XExpression> _memberCallArguments = it_5.getMemberCallArguments();
                    XBooleanLiteral _booleanLiteral = XbaseHelper.booleanLiteral(false);
                    _memberCallArguments.add(_booleanLiteral);
                    it_5.setExplicitOperationCall(true);
                  };
                  XMemberFeatureCall _doubleArrow_1 = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall_1, _function_9);
                  _expressions_2.add(_doubleArrow_1);
                  EList<XExpression> _expressions_3 = it_4.getExpressions();
                  XFeatureCall _createRoutineCall = ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, 
                    this.matchParticipationRoutineBuilderProvider.getMatchParticipationRoutine(this.segment, participationContext), 
                    typeProvider.variable(ReactionsGeneratorConventions.REFERENCE_ROOT), XbaseHelper.booleanLiteral(true), XbaseHelper.featureCall(resultVar));
                  _expressions_3.add(_createRoutineCall);
                };
                XBlockExpression _doubleArrow_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression_1, _function_8);
                it_3.setBody(_doubleArrow_1);
              };
              XDoWhileExpression _doubleArrow = ObjectExtensions.<XDoWhileExpression>operator_doubleArrow(_createXDoWhileExpression, _function_6);
              _expressions_1.add(_doubleArrow);
            };
            return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_4);
          };
          routineCallContext.setCallerContext(it_1.execute(_function_3));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2));
      }
      return _xblockexpression;
    };
    return this.matchManyParticipationsRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Generates the routines for matching sub-participations for the given
   * commonality reference mapping.
   * <p>
   * Returns the routine that needs to be called on commonality insert in
   * order to invoke the matching.
   */
  public FluentRoutineBuilder getMatchSubParticipationsRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      XtendAssertHelper.assertTrue(participationContext.isForReferenceMapping());
      final Commonality referencingCommonality = participationContext.getReferencingCommonality();
      this.generateRoutines(participationContext);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("matchSubParticipations_");
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(this._generationContext.getChangeClass(referencingCommonality), ReactionsGeneratorConventions.INTERMEDIATE);
      };
      final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
        final ParticipationClass referenceRootClass = IterableExtensions.<ParticipationContext.ContextClass>head(participationContext.getReferenceRootClasses()).getParticipationClass();
        it_1.vall(ReactionsGeneratorConventions.REFERENCE_ROOT).retrieve(this._generationContext.getChangeClass(referenceRootClass)).correspondingTo(ReactionsGeneratorConventions.INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(referenceRootClass));
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
        if (_isForAttributeReferenceMapping) {
          FluentRoutineBuilder _matchAttributeReferenceElementsRoutine = this.attributeReferenceMatchingReactionsBuilderProvider.getMatchAttributeReferenceElementsRoutine(this.segment, participationContext);
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it_2) -> {
            return it_2.variable(ReactionsGeneratorConventions.REFERENCE_ROOT);
          };
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_4);
          it_1.call(_matchAttributeReferenceElementsRoutine, _routineCallParameter);
        } else {
          FluentRoutineBuilder _matchManyParticipationsRoutine = this.getMatchManyParticipationsRoutine(participationContext);
          final Function<TypeProvider, XExpression> _function_5 = (TypeProvider it_2) -> {
            return it_2.variable(ReactionsGeneratorConventions.REFERENCE_ROOT);
          };
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_5);
          it_1.call(_matchManyParticipationsRoutine, _routineCallParameter_1);
        }
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3);
    };
    return this.matchSubParticipationsRoutines.computeIfAbsent(participationContext, _function);
  }
}
