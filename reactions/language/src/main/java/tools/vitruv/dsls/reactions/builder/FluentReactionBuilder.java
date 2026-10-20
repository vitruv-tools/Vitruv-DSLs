package tools.vitruv.dsls.reactions.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.RoutineCallParameter;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder.RoutineStartBuilder;
import tools.vitruv.dsls.reactions.language.ElementChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionInListChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalFromListChangeType;
import tools.vitruv.dsls.reactions.language.ElementReplacementChangeType;
import tools.vitruv.dsls.reactions.language.ModelElementChange;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

import static com.google.common.base.Preconditions.*;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.reactions.language.LanguageFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;

public class FluentReactionBuilder extends FluentReactionsSegmentChildBuilder {

  private Reaction reaction;
  private int anonymousRoutineCounter = 0;
  private EClassifier valueType;
  private EClass affectedObjectType;

  FluentReactionBuilder(Reaction reaction, FluentBuilderContext context) {
    super(context);
    this.reaction = reaction;
  }

  FluentReactionBuilder(String reactionName, FluentBuilderContext context) {
    super(context);
    reaction = TopLevelElementsFactory.eINSTANCE.createReaction();
    reaction.setName(reactionName);
  }

  OverrideOrTriggerBuilder start() {
    return new OverrideOrTriggerBuilder(this);
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    checkState(reaction.getTrigger() != null, "No trigger was set on the " +this+ "!");
    checkState(reaction.getCallRoutine() != null, "No routine call was set on the " +this+ "!");
  }

  public static class OverrideOrTriggerBuilder extends TriggerBuilder {
    private OverrideOrTriggerBuilder(FluentReactionBuilder builder) {
      super(builder);
    }

    public TriggerBuilder overrideSegment(FluentReactionsSegmentBuilder segmentBuilder) {
      builder.reaction.setOverriddenReactionsSegment(segmentBuilder.getSegment());
      return new TriggerBuilder(builder);
    }
  }

  public static class TriggerBuilder {
    protected FluentReactionBuilder builder;

    private TriggerBuilder(FluentReactionBuilder builder) {
      this.builder = builder;
    }

    public PreconditionOrRoutineCallBuilder afterAnyChange() {
      builder.reaction.setTrigger(LanguageFactory.eINSTANCE.createArbitraryModelChange());
      return new PreconditionOrRoutineCallBuilder(builder);
    }

    public ChangeTypeBuilder afterElement(EClass element) {
      ModelElementChange change = LanguageFactory.eINSTANCE.createModelElementChange();
      change.setElementType(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassReference(), element)
      );
      builder.reaction.setTrigger(change);
      return new ChangeTypeBuilder(builder, change, element);
    }

    public ChangeTypeBuilder afterElement() {
      ModelElementChange change = LanguageFactory.eINSTANCE.createModelElementChange();
      builder.reaction.setTrigger(change);
      return new ChangeTypeBuilder(builder, change, null);
    }

    public PreconditionOrRoutineCallBuilder afterAttributeInsertIn(EAttribute attribute) {
      return afterAttributeInsertIn(attribute.getEContainingClass(), attribute);
    }

    public PreconditionOrRoutineCallBuilder afterAttributeInsertIn(EClass eClass, EAttribute attribute) {
      builder.valueType = attribute.getEType();
      builder.affectedObjectType = eClass;
      var trigger = LanguageFactory.eINSTANCE.createModelAttributeInsertedChange();
      trigger.setFeature(builder.reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute));
      builder.reaction.setTrigger(trigger);
      return new PreconditionOrRoutineCallBuilder(builder);
    }

    public PreconditionOrRoutineCallBuilder  afterAttributeReplacedAt(EAttribute attribute) {
      return afterAttributeReplacedAt(attribute.getEContainingClass(), attribute);
    }

    public PreconditionOrRoutineCallBuilder afterAttributeReplacedAt(EClass eClass, EAttribute attribute) {
      builder.valueType = attribute.getEType();
      builder.affectedObjectType = eClass;
      var trigger = LanguageFactory.eINSTANCE.createModelAttributeReplacedChange();
      trigger.setFeature(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute)
      );
      return new PreconditionOrRoutineCallBuilder(builder);
    }

    public PreconditionOrRoutineCallBuilder afterAttributeRemoveFrom(EAttribute attribute) {
      return afterAttributeRemoveFrom(attribute.getEContainingClass(), attribute);
    }

    public PreconditionOrRoutineCallBuilder afterAttributeRemoveFrom(EClass eClass, EAttribute attribute) {
      builder.valueType = attribute.getEType();
      builder.affectedObjectType = eClass;
      var trigger = LanguageFactory.eINSTANCE.createModelAttributeRemovedChange();
      trigger.setFeature(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute)
      );
      return new PreconditionOrRoutineCallBuilder(builder);
    }
  }

  public static class ChangeTypeBuilder {
    private final FluentReactionBuilder builder;
    private final ModelElementChange modelElementChange;
    private final EClass element;

    private ChangeTypeBuilder(FluentReactionBuilder builder, ModelElementChange modelElementChange, EClass element) {
      this.builder = builder;
      this.modelElementChange = modelElementChange;
      this.element = element;
    }

    public PreconditionOrRoutineCallBuilder created() {
      builder.affectedObjectType = element != null ? element : EcorePackage.eINSTANCE.getEObject();
      return continueWithChangeType(LanguageFactory.eINSTANCE.createElementCreationChangeType());
    }

    public PreconditionOrRoutineCallBuilder deleted() {
      builder.affectedObjectType = element != null ? element : EcorePackage.eINSTANCE.getEObject();
      return continueWithChangeType(LanguageFactory.eINSTANCE.createElementDeletionChangeType());
    }

    public PreconditionOrRoutineCallBuilder insertedAsRoot() {
      builder.valueType = element != null ? element : EcorePackage.eINSTANCE.getEObject();
      return continueWithChangeType(LanguageFactory.eINSTANCE.createElementInsertionAsRootChangeType());
    }

    public PreconditionOrRoutineCallBuilder insertedIn(EReference reference) {
      return insertedIn(reference.getEContainingClass(), reference);
    }

    public PreconditionOrRoutineCallBuilder insertedIn(EClass eClass, EReference reference) {
      builder.valueType = element != null ? element : reference.getEReferenceType();
      builder.affectedObjectType = eClass;
      ElementInsertionInListChangeType insertionChangeType =
          LanguageFactory.eINSTANCE.createElementInsertionInListChangeType();
      insertionChangeType.setFeature(builder.reference(
          ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference
      ));
      return continueWithChangeType(insertionChangeType);
    }

    public PreconditionOrRoutineCallBuilder removedFrom(EReference reference) {
      return removedFrom(reference.getEContainingClass(), reference);
    }

    public PreconditionOrRoutineCallBuilder removedFrom(EClass eClass, EReference reference) {
      builder.valueType = element != null ? element : reference.getEReferenceType();
      builder.affectedObjectType = eClass;
      ElementRemovalFromListChangeType removalChangeType =
          LanguageFactory.eINSTANCE.createElementRemovalFromListChangeType();
      removalChangeType.setFeature(builder.reference(
          ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference
      ));
      return continueWithChangeType(removalChangeType);
    }

    public PreconditionOrRoutineCallBuilder removedAsRoot() {
      builder.valueType = element != null ? element : EcorePackage.eINSTANCE.getEObject();
      return continueWithChangeType(LanguageFactory.eINSTANCE.createElementRemovalAsRootChangeType());
    }

    public PreconditionOrRoutineCallBuilder replacedAt(EReference reference) {
      return replacedAt(reference.getEContainingClass(), reference);
    }

    public PreconditionOrRoutineCallBuilder replacedAt(EClass eClass, EReference reference) {
      builder.valueType = element != null ? element : reference.getEReferenceType();
      builder.affectedObjectType = eClass;
      ElementReplacementChangeType changeType = LanguageFactory.eINSTANCE.createElementReplacementChangeType();
      changeType.setFeature(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference)
      );
      return continueWithChangeType(changeType);
    }

    private PreconditionOrRoutineCallBuilder continueWithChangeType(ElementChangeType changeType) {
      modelElementChange.setChangeType(changeType);
      return new PreconditionOrRoutineCallBuilder(builder);
    }
  }

  public static class RoutineCallBuilder {
    protected FluentReactionBuilder builder;

    private RoutineCallBuilder(FluentReactionBuilder builder) {
      this.builder = builder;
    }

    public FluentReactionBuilder call(FluentRoutineBuilder[] routineBuilders) {
      checkNotNull(routineBuilders);
      checkArgument(routineBuilders.length > 0, "Must provide at least one routineBuilder!");
      for (FluentRoutineBuilder routineBuilder : routineBuilders) {
        call(routineBuilder);
      }
      return builder;
    }

    public FluentReactionBuilder call(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
      checkNotNull(routineBuilder);
      checkState(routineBuilder.readyToBeAttached,
          "The " +routineBuilder+ " is not sufficiently initialised to be set on the " +builder);
      checkState(!routineBuilder.requireNewValue || builder.valueType != null,
          "The " +routineBuilder+ " requires a new value, but the " +builder+ " doesn’t create one!");
      checkState(!routineBuilder.requireOldValue || builder.valueType != null,
          "The " +routineBuilder+ " requires an old value, but the " +builder+ " doesn’t create one!");
      checkState(!routineBuilder.requireAffectedEObject || builder.affectedObjectType != null,
          "The " +routineBuilder+ " requires an affectedElement, but the " +builder+ " doesn’t create one!");
      // TODO check validity of explicitly specified parameters

      if (builder.affectedObjectType != null && routineBuilder.requireAffectedEObject) {
        routineBuilder.setAffectedObjectType(builder.affectedObjectType);
      }
      if (builder.valueType != null && (routineBuilder.requireNewValue || routineBuilder.requireOldValue)) {
        routineBuilder.setValueType(builder.valueType);
      }

      builder.transferReactionsSegmentTo(builder, routineBuilder);
      addRoutineCall(routineBuilder, parameters);
      builder.readyToBeAttached = true;
      return builder;
    }

    /**
     * Adds a {@link RoutineCall} to the {@code builder.reaction}.
     *
     * @param routineBuilder {@link FluentRoutineBuilder}
     * @param parameters {@link RoutineCallParameter}
     * @return false if the appropriate call already existed, true if it did not
     */
    private boolean addRoutineCall(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
      if (builder.reaction.getCallRoutine() == null) {
        RoutineCall newRoutineCall = TopLevelElementsFactory.eINSTANCE.createRoutineCall();
        newRoutineCall.setCode(routineCall(newRoutineCall, routineBuilder, parameters));
        builder.reaction.setCallRoutine(newRoutineCall);
        return false;
      } else {
        RoutineCall callRoutine = builder.reaction.getCallRoutine();
        XExpression callRoutineCode = callRoutine.getCode();
        if (callRoutineCode instanceof XBlockExpression blockExpression) {
          return
              blockExpression.getExpressions().add(routineCall(callRoutine, routineBuilder, parameters));
        } else {
          XBlockExpression newBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
          newBlockExpression.getExpressions().add(callRoutineCode);
          newBlockExpression.getExpressions().add(routineCall(callRoutine, routineBuilder, parameters));
          return true;
        }
      }
    }

    public FluentReactionBuilder call(String routineName, Consumer<RoutineStartBuilder> routineInitializer) {
      FluentRoutineBuilder routineBuilder = new FluentRoutineBuilder(routineName, builder.context);
      routineInitializer.accept(routineBuilder.start());
      return call(routineBuilder);
    }

    public FluentReactionBuilder call(Consumer<RoutineStartBuilder> routineInitializer) {
      builder.anonymousRoutineCounter++;
      return call(StringExtensions.toFirstLower(builder.reaction.getName())
              + "Repair" +
              (builder.anonymousRoutineCounter != 1 ? "anonymousRoutineCounter" : ""),
          routineInitializer);
    }

    private XFeatureCall routineCall(RoutineCall routineCall, FluentRoutineBuilder routineBuilder,
                                     RoutineCallParameter... parameters) {
      XFeatureCall featureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      featureCall.setExplicitOperationCall(true);

      return builder.whenJvmTypes(featureCall, call -> {
        call.setFeature(routineBuilder.getJvmOperation());
        call.setImplicitReceiver(builder.getJvmOperationRoutineFacade(call));
        TypeProvider typeProvider = builder.getTypeProvider(featureCall);

        if (parameters == null || parameters.length == 0) {
          call.getFeatureCallArguments().addAll(
            requiredArguments(routineBuilder, typeProvider)
          );
        }
        else {
          call.getFeatureCallArguments().addAll(
              Arrays.stream(parameters)
                  .map(parameter -> parameter.getExpression(typeProvider))
                  .toList()
          );
        }
      });
    }

    private List<XExpression> requiredArguments(FluentRoutineBuilder routineBuilder, TypeProvider typeProvider) {
      List<XExpression> parameterList = new ArrayList<>(3);
      if (routineBuilder.requireAffectedEObject) {
        parameterList.add(typeProvider.affectedEObject());
      }
      if (routineBuilder.requireNewValue) {
        parameterList.add(typeProvider.newValue());
      }
      if (routineBuilder.requireOldValue) {
        parameterList.add( typeProvider.oldValue());
      }
      return parameterList;
    }
  }

  public static class PreconditionOrRoutineCallBuilder extends RoutineCallBuilder {
    private PreconditionOrRoutineCallBuilder(FluentReactionBuilder builder) {
      super(builder);
    }

    public RoutineCallBuilder with(Function<TypeProvider, XExpression> expressionProvider) {
      XBlockExpression precondition = XbaseFactory.eINSTANCE.createXBlockExpression();
      builder.whenJvmTypes(precondition, blockExpression -> {
        blockExpression.getExpressions().addAll(
            extractExpressions(expressionProvider.apply(builder.getTypeProvider(blockExpression)))
        );
      });
      builder.reaction.getTrigger().setPrecondition(precondition);
      return new RoutineCallBuilder(builder);
    }
  }

  Reaction getReaction() {
    return reaction;
  }

  @Override
  public String toString() {
    return "reaction builder for " +reaction.getName();
  }

  @Override
  protected String getCreatedElementName() {
    return reaction.getName();
  }

  @Override
  protected String getCreatedElementType() {
    return "reaction";
  }

  public FluentReactionBuilder call(FluentRoutineBuilder[] routineBuilders) {
    return new RoutineCallBuilder(this).call(routineBuilders);
  }

  public FluentReactionBuilder call(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
    return new RoutineCallBuilder(this).call(routineBuilder, parameters);
  }

  public FluentReactionBuilder call(String routineName, Consumer<RoutineStartBuilder> routineInitializer) {
    return new RoutineCallBuilder(this).call(routineName, routineInitializer);
  }

  public FluentReactionBuilder call(Consumer<RoutineStartBuilder> routineInitializer) {
    return new RoutineCallBuilder(this).call(routineInitializer);
  }
}
