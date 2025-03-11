package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.language.ElementChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionInListChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalFromListChangeType;
import tools.vitruv.dsls.reactions.language.ElementReplacementChangeType;
import tools.vitruv.dsls.reactions.language.LanguageFactory;
import tools.vitruv.dsls.reactions.language.ModelAttributeInsertedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeRemovedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeReplacedChange;
import tools.vitruv.dsls.reactions.language.ModelElementChange;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;

@SuppressWarnings("all")
public class FluentReactionBuilder extends FluentReactionsSegmentChildBuilder {
  public static class OverrideOrTriggerBuilder extends FluentReactionBuilder.TriggerBuilder {
    private OverrideOrTriggerBuilder(final FluentReactionBuilder builder) {
      super(builder);
    }

    public FluentReactionBuilder.TriggerBuilder overrideSegment(final FluentReactionsSegmentBuilder segmentBuilder) {
      FluentReactionBuilder.TriggerBuilder _xblockexpression = null;
      {
        ReactionsSegment _segment = null;
        if (segmentBuilder!=null) {
          _segment=segmentBuilder.getSegment();
        }
        this.builder.reaction.setOverriddenReactionsSegment(_segment);
        _xblockexpression = new FluentReactionBuilder.TriggerBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class TriggerBuilder {
    @Extension
    protected final FluentReactionBuilder builder;

    private TriggerBuilder(final FluentReactionBuilder builder) {
      this.builder = builder;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAnyChange() {
      this.builder.reaction.setTrigger(LanguageFactory.eINSTANCE.createArbitraryModelChange());
      return new FluentReactionBuilder.PreconditionOrRoutineCallBuilder(this.builder);
    }

    public FluentReactionBuilder.ChangeTypeBuilder afterElement(final EClass element) {
      ModelElementChange _createModelElementChange = LanguageFactory.eINSTANCE.createModelElementChange();
      final Procedure1<ModelElementChange> _function = (ModelElementChange it) -> {
        it.setElementType(this.builder.<MetaclassReference>reference(ElementsFactory.eINSTANCE.createMetaclassReference(), element));
      };
      final ModelElementChange change = ObjectExtensions.<ModelElementChange>operator_doubleArrow(_createModelElementChange, _function);
      this.builder.reaction.setTrigger(change);
      return new FluentReactionBuilder.ChangeTypeBuilder(this.builder, change, element);
    }

    public FluentReactionBuilder.ChangeTypeBuilder afterElement() {
      final ModelElementChange change = LanguageFactory.eINSTANCE.createModelElementChange();
      this.builder.reaction.setTrigger(change);
      return new FluentReactionBuilder.ChangeTypeBuilder(this.builder, change, null);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeInsertIn(final EAttribute attribute) {
      return this.afterAttributeInsertIn(attribute.getEContainingClass(), attribute);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeInsertIn(final EClass eClass, final EAttribute attribute) {
      this.builder.valueType = attribute.getEType();
      this.builder.affectedObjectType = eClass;
      ModelAttributeInsertedChange _createModelAttributeInsertedChange = LanguageFactory.eINSTANCE.createModelAttributeInsertedChange();
      final Procedure1<ModelAttributeInsertedChange> _function = (ModelAttributeInsertedChange it) -> {
        it.setFeature(this.builder.<MetaclassEAttributeReference>reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute));
      };
      ModelAttributeInsertedChange _doubleArrow = ObjectExtensions.<ModelAttributeInsertedChange>operator_doubleArrow(_createModelAttributeInsertedChange, _function);
      this.builder.reaction.setTrigger(_doubleArrow);
      return new FluentReactionBuilder.PreconditionOrRoutineCallBuilder(this.builder);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeReplacedAt(final EAttribute attribute) {
      return this.afterAttributeReplacedAt(attribute.getEContainingClass(), attribute);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeReplacedAt(final EClass eClass, final EAttribute attribute) {
      this.builder.valueType = attribute.getEType();
      this.builder.affectedObjectType = eClass;
      ModelAttributeReplacedChange _createModelAttributeReplacedChange = LanguageFactory.eINSTANCE.createModelAttributeReplacedChange();
      final Procedure1<ModelAttributeReplacedChange> _function = (ModelAttributeReplacedChange it) -> {
        it.setFeature(this.builder.<MetaclassEAttributeReference>reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute));
      };
      ModelAttributeReplacedChange _doubleArrow = ObjectExtensions.<ModelAttributeReplacedChange>operator_doubleArrow(_createModelAttributeReplacedChange, _function);
      this.builder.reaction.setTrigger(_doubleArrow);
      return new FluentReactionBuilder.PreconditionOrRoutineCallBuilder(this.builder);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeRemoveFrom(final EAttribute attribute) {
      return this.afterAttributeRemoveFrom(attribute.getEContainingClass(), attribute);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterAttributeRemoveFrom(final EClass eClass, final EAttribute attribute) {
      this.builder.valueType = attribute.getEType();
      this.builder.affectedObjectType = eClass;
      ModelAttributeRemovedChange _createModelAttributeRemovedChange = LanguageFactory.eINSTANCE.createModelAttributeRemovedChange();
      final Procedure1<ModelAttributeRemovedChange> _function = (ModelAttributeRemovedChange it) -> {
        it.setFeature(this.builder.<MetaclassEAttributeReference>reference(ElementsFactory.eINSTANCE.createMetaclassEAttributeReference(), eClass, attribute));
      };
      ModelAttributeRemovedChange _doubleArrow = ObjectExtensions.<ModelAttributeRemovedChange>operator_doubleArrow(_createModelAttributeRemovedChange, _function);
      this.builder.reaction.setTrigger(_doubleArrow);
      return new FluentReactionBuilder.PreconditionOrRoutineCallBuilder(this.builder);
    }
  }

  public static class ChangeTypeBuilder {
    @Extension
    private final FluentReactionBuilder builder;

    private final ModelElementChange modelElementChange;

    private final EClass element;

    private ChangeTypeBuilder(final FluentReactionBuilder builder, final ModelElementChange modelElementChange, final EClass element) {
      this.builder = builder;
      this.modelElementChange = modelElementChange;
      this.element = element;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder created() {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eObject = EcorePackage.eINSTANCE.getEObject();
          _elvis = _eObject;
        }
        this.builder.affectedObjectType = _elvis;
        _xblockexpression = this.continueWithChangeType(LanguageFactory.eINSTANCE.createElementCreationChangeType());
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder deleted() {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eObject = EcorePackage.eINSTANCE.getEObject();
          _elvis = _eObject;
        }
        this.builder.affectedObjectType = _elvis;
        _xblockexpression = this.continueWithChangeType(LanguageFactory.eINSTANCE.createElementDeletionChangeType());
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder insertedAsRoot() {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eObject = EcorePackage.eINSTANCE.getEObject();
          _elvis = _eObject;
        }
        this.builder.valueType = _elvis;
        _xblockexpression = this.continueWithChangeType(LanguageFactory.eINSTANCE.createElementInsertionAsRootChangeType());
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder insertedIn(final EReference reference) {
      return this.insertedIn(reference.getEContainingClass(), reference);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder insertedIn(final EClass eClass, final EReference reference) {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eReferenceType = reference.getEReferenceType();
          _elvis = _eReferenceType;
        }
        this.builder.valueType = _elvis;
        this.builder.affectedObjectType = eClass;
        ElementInsertionInListChangeType _createElementInsertionInListChangeType = LanguageFactory.eINSTANCE.createElementInsertionInListChangeType();
        final Procedure1<ElementInsertionInListChangeType> _function = (ElementInsertionInListChangeType it) -> {
          it.setFeature(this.builder.<MetaclassEReferenceReference>reference(ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference));
        };
        ElementInsertionInListChangeType _doubleArrow = ObjectExtensions.<ElementInsertionInListChangeType>operator_doubleArrow(_createElementInsertionInListChangeType, _function);
        _xblockexpression = this.continueWithChangeType(_doubleArrow);
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder removedFrom(final EReference reference) {
      return this.removedFrom(reference.getEContainingClass(), reference);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder removedFrom(final EClass eClass, final EReference reference) {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eReferenceType = reference.getEReferenceType();
          _elvis = _eReferenceType;
        }
        this.builder.valueType = _elvis;
        this.builder.affectedObjectType = eClass;
        ElementRemovalFromListChangeType _createElementRemovalFromListChangeType = LanguageFactory.eINSTANCE.createElementRemovalFromListChangeType();
        final Procedure1<ElementRemovalFromListChangeType> _function = (ElementRemovalFromListChangeType it) -> {
          it.setFeature(this.builder.<MetaclassEReferenceReference>reference(ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference));
        };
        ElementRemovalFromListChangeType _doubleArrow = ObjectExtensions.<ElementRemovalFromListChangeType>operator_doubleArrow(_createElementRemovalFromListChangeType, _function);
        _xblockexpression = this.continueWithChangeType(_doubleArrow);
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder removedAsRoot() {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eObject = EcorePackage.eINSTANCE.getEObject();
          _elvis = _eObject;
        }
        this.builder.valueType = _elvis;
        _xblockexpression = this.continueWithChangeType(LanguageFactory.eINSTANCE.createElementRemovalAsRootChangeType());
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder replacedAt(final EReference reference) {
      return this.replacedAt(reference.getEContainingClass(), reference);
    }

    public FluentReactionBuilder.PreconditionOrRoutineCallBuilder replacedAt(final EClass eClass, final EReference reference) {
      FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xblockexpression = null;
      {
        EClass _elvis = null;
        if (this.element != null) {
          _elvis = this.element;
        } else {
          EClass _eReferenceType = reference.getEReferenceType();
          _elvis = _eReferenceType;
        }
        this.builder.valueType = _elvis;
        this.builder.affectedObjectType = eClass;
        ElementReplacementChangeType _createElementReplacementChangeType = LanguageFactory.eINSTANCE.createElementReplacementChangeType();
        final Procedure1<ElementReplacementChangeType> _function = (ElementReplacementChangeType it) -> {
          it.setFeature(this.builder.<MetaclassEReferenceReference>reference(ElementsFactory.eINSTANCE.createMetaclassEReferenceReference(), eClass, reference));
        };
        ElementReplacementChangeType _doubleArrow = ObjectExtensions.<ElementReplacementChangeType>operator_doubleArrow(_createElementReplacementChangeType, _function);
        _xblockexpression = this.continueWithChangeType(_doubleArrow);
      }
      return _xblockexpression;
    }

    private FluentReactionBuilder.PreconditionOrRoutineCallBuilder continueWithChangeType(final ElementChangeType changeType) {
      this.modelElementChange.setChangeType(changeType);
      return new FluentReactionBuilder.PreconditionOrRoutineCallBuilder(this.builder);
    }
  }

  public static class RoutineCallBuilder {
    @Extension
    protected final FluentReactionBuilder builder;

    private RoutineCallBuilder(final FluentReactionBuilder builder) {
      this.builder = builder;
    }

    public FluentReactionBuilder call(final FluentRoutineBuilder[] routineBuilders) {
      Preconditions.<FluentRoutineBuilder[]>checkNotNull(routineBuilders);
      int _length = routineBuilders.length;
      boolean _greaterThan = (_length > 0);
      Preconditions.checkArgument(_greaterThan, "Must provide at least one routineBuilder!");
      for (final FluentRoutineBuilder routineBuilder : routineBuilders) {
        this.call(routineBuilder);
      }
      return this.builder;
    }

    public FluentReactionBuilder call(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      Preconditions.<FluentRoutineBuilder>checkNotNull(routineBuilder);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The ");
      _builder.append(routineBuilder);
      _builder.append(" is not sufficiently initialised to be set on the ");
      _builder.append(this.builder);
      Preconditions.checkState(routineBuilder.readyToBeAttached, _builder);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("The ");
      _builder_1.append(routineBuilder);
      _builder_1.append(" requires a new value, but the ");
      _builder_1.append(this.builder);
      _builder_1.append(" doesn’t create one!");
      Preconditions.checkState(((!routineBuilder.requireNewValue) || (this.builder.valueType != null)), _builder_1);
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("The ");
      _builder_2.append(routineBuilder);
      _builder_2.append(" requires an old value, but the ");
      _builder_2.append(this.builder);
      _builder_2.append(" doesn’t create one!");
      Preconditions.checkState(((!routineBuilder.requireOldValue) || (this.builder.valueType != null)), _builder_2);
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("The ");
      _builder_3.append(routineBuilder);
      _builder_3.append(" requires an affectedElement, but the ");
      _builder_3.append(this.builder);
      _builder_3.append(" doesn’t create one!");
      Preconditions.checkState(((!routineBuilder.requireAffectedEObject) || (this.builder.affectedObjectType != null)), _builder_3);
      if (((this.builder.affectedObjectType != null) && routineBuilder.requireAffectedEObject)) {
        routineBuilder.setAffectedObjectType(this.builder.affectedObjectType);
      }
      if (((this.builder.valueType != null) && (routineBuilder.requireNewValue || routineBuilder.requireOldValue))) {
        routineBuilder.setValueType(this.builder.valueType);
      }
      this.builder.transferReactionsSegmentTo(this.builder, routineBuilder);
      this.addRoutineCall(routineBuilder, parameters);
      this.builder.readyToBeAttached = true;
      return this.builder;
    }

    private Boolean addRoutineCall(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      boolean _xifexpression = false;
      RoutineCall _callRoutine = this.builder.reaction.getCallRoutine();
      boolean _tripleEquals = (_callRoutine == null);
      if (_tripleEquals) {
        RoutineCall _createRoutineCall = TopLevelElementsFactory.eINSTANCE.createRoutineCall();
        final Procedure1<RoutineCall> _function = (RoutineCall it) -> {
          it.setCode(this.routineCall(it, routineBuilder, parameters));
        };
        RoutineCall _doubleArrow = ObjectExtensions.<RoutineCall>operator_doubleArrow(_createRoutineCall, _function);
        this.builder.reaction.setCallRoutine(_doubleArrow);
      } else {
        boolean _xblockexpression = false;
        {
          final RoutineCall callRoutine = this.builder.reaction.getCallRoutine();
          final XExpression callRoutineCode = callRoutine.getCode();
          boolean _xifexpression_1 = false;
          if ((callRoutineCode instanceof XBlockExpression)) {
            EList<XExpression> _expressions = ((XBlockExpression)callRoutineCode).getExpressions();
            XFeatureCall _routineCall = this.routineCall(callRoutine, routineBuilder, parameters);
            _xifexpression_1 = _expressions.add(_routineCall);
          } else {
            RoutineCall _callRoutine_1 = this.builder.reaction.getCallRoutine();
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_1 = (XBlockExpression it) -> {
              EList<XExpression> _expressions_1 = it.getExpressions();
              _expressions_1.add(callRoutineCode);
              EList<XExpression> _expressions_2 = it.getExpressions();
              XFeatureCall _routineCall_1 = this.routineCall(callRoutine, routineBuilder, parameters);
              _expressions_2.add(_routineCall_1);
            };
            XBlockExpression _doubleArrow_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_1);
            _callRoutine_1.setCode(_doubleArrow_1);
          }
          _xblockexpression = _xifexpression_1;
        }
        _xifexpression = _xblockexpression;
      }
      return Boolean.valueOf(_xifexpression);
    }

    public FluentReactionBuilder call(final String routineName, final Consumer<FluentRoutineBuilder.RoutineStartBuilder> routineInitializer) {
      FluentReactionBuilder _xblockexpression = null;
      {
        final FluentRoutineBuilder routineBuilder = new FluentRoutineBuilder(routineName, this.builder.context);
        routineInitializer.accept(routineBuilder.start());
        _xblockexpression = this.call(routineBuilder);
      }
      return _xblockexpression;
    }

    public FluentReactionBuilder call(final Consumer<FluentRoutineBuilder.RoutineStartBuilder> routineInitializer) {
      FluentReactionBuilder _xblockexpression = null;
      {
        this.builder.anonymousRoutineCounter++;
        StringConcatenation _builder = new StringConcatenation();
        String _firstLower = StringExtensions.toFirstLower(this.builder.reaction.getName());
        _builder.append(_firstLower);
        _builder.append("Repair");
        {
          if ((this.builder.anonymousRoutineCounter != 1)) {
            _builder.append("anonymousRoutineCounter");
          }
        }
        _xblockexpression = this.call(_builder.toString(), routineInitializer);
      }
      return _xblockexpression;
    }

    private XFeatureCall routineCall(final RoutineCall routineCall, final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        it.setExplicitOperationCall(true);
      };
      final Consumer<XFeatureCall> _function_1 = (XFeatureCall it) -> {
        it.setFeature(routineBuilder.getJvmOperation());
        it.setImplicitReceiver(this.builder.getJvmOperationRoutineFacade(it));
        final TypeProvider typeProvider = this.builder.getTypeProvider(it);
        boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(((Iterable<?>)Conversions.doWrapArray(parameters)));
        if (_isNullOrEmpty) {
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          List<XExpression> _requiredArguments = this.requiredArguments(routineBuilder, typeProvider);
          Iterables.<XExpression>addAll(_featureCallArguments, _requiredArguments);
        } else {
          EList<XExpression> _featureCallArguments_1 = it.getFeatureCallArguments();
          final Function1<FluentRoutineBuilder.RoutineCallParameter, XExpression> _function_2 = (FluentRoutineBuilder.RoutineCallParameter it_1) -> {
            return it_1.getExpression(typeProvider);
          };
          List<XExpression> _map = ListExtensions.<FluentRoutineBuilder.RoutineCallParameter, XExpression>map(((List<FluentRoutineBuilder.RoutineCallParameter>)Conversions.doWrapArray(parameters)), _function_2);
          Iterables.<XExpression>addAll(_featureCallArguments_1, _map);
        }
      };
      return this.builder.<XFeatureCall>whenJvmTypes(ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function), _function_1);
    }

    private List<XExpression> requiredArguments(final FluentRoutineBuilder routineBuilder, final TypeProvider typeProvider) {
      final ArrayList<XExpression> parameterList = new ArrayList<XExpression>(3);
      if (routineBuilder.requireAffectedEObject) {
        XFeatureCall _affectedEObject = typeProvider.affectedEObject();
        parameterList.add(_affectedEObject);
      }
      if (routineBuilder.requireNewValue) {
        XFeatureCall _newValue = typeProvider.newValue();
        parameterList.add(_newValue);
      }
      if (routineBuilder.requireOldValue) {
        XFeatureCall _oldValue = typeProvider.oldValue();
        parameterList.add(_oldValue);
      }
      return parameterList;
    }
  }

  public static class PreconditionOrRoutineCallBuilder extends FluentReactionBuilder.RoutineCallBuilder {
    private PreconditionOrRoutineCallBuilder(final FluentReactionBuilder builder) {
      super(builder);
    }

    public FluentReactionBuilder.RoutineCallBuilder with(final Function<TypeProvider, XExpression> expressionProvider) {
      Trigger _trigger = this.builder.reaction.getTrigger();
      final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
        EList<XExpression> _expressions = it.getExpressions();
        List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(expressionProvider.apply(this.builder.getTypeProvider(it)));
        Iterables.<XExpression>addAll(_expressions, _extractExpressions);
      };
      _trigger.setPrecondition(this.builder.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function));
      return new FluentReactionBuilder.RoutineCallBuilder(this.builder);
    }
  }

  private Reaction reaction;

  private int anonymousRoutineCounter = 0;

  private EClassifier valueType;

  private EClass affectedObjectType;

  FluentReactionBuilder(final Reaction reaction, final FluentBuilderContext context) {
    super(context);
    this.reaction = reaction;
  }

  FluentReactionBuilder(final String reactionName, final FluentBuilderContext context) {
    super(context);
    Reaction _createReaction = TopLevelElementsFactory.eINSTANCE.createReaction();
    final Procedure1<Reaction> _function = (Reaction it) -> {
      it.setName(reactionName);
    };
    Reaction _doubleArrow = ObjectExtensions.<Reaction>operator_doubleArrow(_createReaction, _function);
    this.reaction = _doubleArrow;
  }

  FluentReactionBuilder.OverrideOrTriggerBuilder start() {
    return new FluentReactionBuilder.OverrideOrTriggerBuilder(this);
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    Trigger _trigger = this.reaction.getTrigger();
    boolean _tripleNotEquals = (_trigger != null);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("No trigger was set on the ");
    _builder.append(this);
    _builder.append("!");
    Preconditions.checkState(_tripleNotEquals, _builder);
    RoutineCall _callRoutine = this.reaction.getCallRoutine();
    boolean _tripleNotEquals_1 = (_callRoutine != null);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("No routine call was set on the ");
    _builder_1.append(this);
    _builder_1.append("!");
    Preconditions.checkState(_tripleNotEquals_1, _builder_1);
  }

  Reaction getReaction() {
    return this.reaction;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("reaction builder for “");
    String _name = this.reaction.getName();
    _builder.append(_name);
    _builder.append("”");
    return _builder.toString();
  }

  @Override
  protected String getCreatedElementName() {
    return this.reaction.getName();
  }

  @Override
  protected String getCreatedElementType() {
    return "reaction";
  }

  public FluentReactionBuilder call(final FluentRoutineBuilder[] routineBuilders) {
    return new FluentReactionBuilder.RoutineCallBuilder(this).call(routineBuilders);
  }

  public FluentReactionBuilder call(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
    return new FluentReactionBuilder.RoutineCallBuilder(this).call(routineBuilder, parameters);
  }

  public FluentReactionBuilder call(final String routineName, final Consumer<FluentRoutineBuilder.RoutineStartBuilder> routineInitializer) {
    return new FluentReactionBuilder.RoutineCallBuilder(this).call(routineName, routineInitializer);
  }

  public FluentReactionBuilder call(final Consumer<FluentRoutineBuilder.RoutineStartBuilder> routineInitializer) {
    return new FluentReactionBuilder.RoutineCallBuilder(this).call(routineInitializer);
  }
}
