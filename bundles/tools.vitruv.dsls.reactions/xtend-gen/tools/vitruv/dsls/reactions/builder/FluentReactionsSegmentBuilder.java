package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

@Accessors(AccessorType.PACKAGE_GETTER)
@SuppressWarnings("all")
public class FluentReactionsSegmentBuilder extends FluentReactionElementBuilder {
  public static class ReactionsSegmentSourceBuilder {
    @Extension
    private final FluentReactionsSegmentBuilder builder;

    private ReactionsSegmentSourceBuilder(final FluentReactionsSegmentBuilder builder) {
      this.builder = builder;
    }

    public FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder inReactionToChangesIn(final EPackage sourceMetamodelRootPackage) {
      return this.inReactionToChangesIn(Collections.<EPackage>unmodifiableSet(CollectionLiterals.<EPackage>newHashSet(sourceMetamodelRootPackage)));
    }

    public FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder inReactionToChangesIn(final Set<EPackage> sourceMetamodelRootPackages) {
      FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder _xblockexpression = null;
      {
        final Consumer<FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder> _function = (FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder it) -> {
          EList<MetamodelImport> _fromMetamodels = this.builder.segment.getFromMetamodels();
          final Function1<EPackage, MetamodelImport> _function_1 = (EPackage it_1) -> {
            return this.builder.metamodelImport(it_1);
          };
          Iterable<MetamodelImport> _map = IterableExtensions.<EPackage, MetamodelImport>map(sourceMetamodelRootPackages, _function_1);
          Iterables.<MetamodelImport>addAll(_fromMetamodels, _map);
        };
        this.builder.<FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder>beforeAttached(this, _function);
        _xblockexpression = new FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class ReactionsSegmentTargetBuilder {
    @Extension
    private final FluentReactionsSegmentBuilder builder;

    private ReactionsSegmentTargetBuilder(final FluentReactionsSegmentBuilder builder) {
      this.builder = builder;
    }

    public FluentReactionsSegmentBuilder executeActionsIn(final EPackage targetMetamodelRootPackage) {
      return this.executeActionsIn(Collections.<EPackage>unmodifiableSet(CollectionLiterals.<EPackage>newHashSet(targetMetamodelRootPackage)));
    }

    public FluentReactionsSegmentBuilder executeActionsIn(final Set<EPackage> targetMetamodelRootPackages) {
      FluentReactionsSegmentBuilder _xblockexpression = null;
      {
        final Consumer<FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder> _function = (FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder it) -> {
          EList<MetamodelImport> _toMetamodels = this.builder.segment.getToMetamodels();
          final Function1<EPackage, MetamodelImport> _function_1 = (EPackage it_1) -> {
            return this.builder.metamodelImport(it_1);
          };
          Iterable<MetamodelImport> _map = IterableExtensions.<EPackage, MetamodelImport>map(targetMetamodelRootPackages, _function_1);
          Iterables.<MetamodelImport>addAll(_toMetamodels, _map);
        };
        this.builder.<FluentReactionsSegmentBuilder.ReactionsSegmentTargetBuilder>beforeAttached(this, _function);
        this.builder.readyToBeAttached = true;
        _xblockexpression = this.builder;
      }
      return _xblockexpression;
    }
  }

  public static class ReactionsSegmentImportBuilder {
    @Extension
    private final FluentReactionsSegmentBuilder builder;

    private final ReactionsImport reactionsImport;

    private ReactionsSegmentImportBuilder(final FluentReactionsSegmentBuilder builder, final FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
      this.builder = builder;
      ReactionsImport _createReactionsImport = TopLevelElementsFactory.eINSTANCE.createReactionsImport();
      final Procedure1<ReactionsImport> _function = (ReactionsImport it) -> {
        it.setImportedReactionsSegment(reactionsSegmentBuilder.segment);
      };
      ReactionsImport _doubleArrow = ObjectExtensions.<ReactionsImport>operator_doubleArrow(_createReactionsImport, _function);
      this.reactionsImport = _doubleArrow;
      EList<ReactionsImport> _reactionsImports = builder.segment.getReactionsImports();
      _reactionsImports.add(this.reactionsImport);
    }

    public FluentReactionsSegmentBuilder.ReactionsSegmentImportBuilder routinesOnly() {
      FluentReactionsSegmentBuilder.ReactionsSegmentImportBuilder _xblockexpression = null;
      {
        this.reactionsImport.setRoutinesOnly(true);
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    public FluentReactionsSegmentBuilder usingQualifiedRoutineNames() {
      FluentReactionsSegmentBuilder _xblockexpression = null;
      {
        this.reactionsImport.setUseQualifiedNames(true);
        _xblockexpression = this.builder;
      }
      return _xblockexpression;
    }

    public FluentReactionsSegmentBuilder usingSimpleRoutineNames() {
      FluentReactionsSegmentBuilder _xblockexpression = null;
      {
        this.reactionsImport.setUseQualifiedNames(false);
        _xblockexpression = this.builder;
      }
      return _xblockexpression;
    }
  }

  private final ReactionsSegment segment;

  FluentReactionsSegmentBuilder(final String segmentName, final FluentBuilderContext context) {
    super(context);
    ReactionsSegment _createReactionsSegment = TopLevelElementsFactory.eINSTANCE.createReactionsSegment();
    final Procedure1<ReactionsSegment> _function = (ReactionsSegment it) -> {
      it.setName(segmentName);
    };
    ReactionsSegment _doubleArrow = ObjectExtensions.<ReactionsSegment>operator_doubleArrow(_createReactionsSegment, _function);
    this.segment = _doubleArrow;
  }

  FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder start() {
    return new FluentReactionsSegmentBuilder.ReactionsSegmentSourceBuilder(this);
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    int _size = this.segment.getRoutines().size();
    int _size_1 = this.segment.getReactions().size();
    int _plus = (_size + _size_1);
    int _size_2 = this.segment.getReactionsImports().size();
    int _plus_1 = (_plus + _size_2);
    boolean _greaterThan = (_plus_1 > 
      0);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Neither routines, nor reactions, nor imports were added to the reaction segment ");
    String _name = this.segment.getName();
    _builder.append(_name);
    _builder.append("!");
    Preconditions.checkState(_greaterThan, _builder);
  }

  public FluentReactionsSegmentBuilder.ReactionsSegmentImportBuilder importSegment(final FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
    return new FluentReactionsSegmentBuilder.ReactionsSegmentImportBuilder(this, reactionsSegmentBuilder);
  }

  public FluentReactionsSegmentBuilder operator_add(final FluentReactionBuilder[] reactionBuilders) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      final Consumer<FluentReactionBuilder> _function = (FluentReactionBuilder it) -> {
        this.operator_add(it);
      };
      ((List<FluentReactionBuilder>)Conversions.doWrapArray(reactionBuilders)).forEach(_function);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public FluentReactionsSegmentBuilder operator_add(final FluentRoutineBuilder[] routineBuilders) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      final Consumer<FluentRoutineBuilder> _function = (FluentRoutineBuilder it) -> {
        this.operator_add(it);
      };
      ((List<FluentRoutineBuilder>)Conversions.doWrapArray(routineBuilders)).forEach(_function);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  protected Object _add(final FluentReactionBuilder reactionBuilder) {
    return this.operator_add(reactionBuilder);
  }

  protected Object _add(final FluentRoutineBuilder routineBuilder) {
    return this.operator_add(routineBuilder);
  }

  public FluentReactionsSegmentBuilder operator_add(final FluentReactionBuilder reactionBuilder) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      this.checkNotYetAttached();
      FluentReactionsSegmentBuilder _segmentBuilder = reactionBuilder.getSegmentBuilder();
      boolean _tripleNotEquals = (_segmentBuilder != this);
      if (_tripleNotEquals) {
        FluentReactionsSegmentBuilder _segmentBuilder_1 = reactionBuilder.getSegmentBuilder();
        boolean _tripleEquals = (_segmentBuilder_1 == 
          null);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("The ");
        _builder.append(reactionBuilder);
        _builder.append(" has already been added to the ");
        FluentReactionsSegmentBuilder _segmentBuilder_2 = reactionBuilder.getSegmentBuilder();
        _builder.append(_segmentBuilder_2);
        Preconditions.checkArgument(_tripleEquals, _builder);
        final Function1<Reaction, Boolean> _function = (Reaction it) -> {
          String _name = it.getName();
          String _name_1 = reactionBuilder.getReaction().getName();
          return Boolean.valueOf(Objects.equal(_name, _name_1));
        };
        boolean _exists = IterableExtensions.<Reaction>exists(this.segment.getReactions(), _function);
        boolean _not = (!_exists);
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The ");
        _builder_1.append(this);
        _builder_1.append(" already contains a reaction with name \'");
        String _name = reactionBuilder.getReaction().getName();
        _builder_1.append(_name);
        _builder_1.append("\'!");
        Preconditions.checkArgument(_not, _builder_1);
        EList<Reaction> _reactions = this.segment.getReactions();
        Reaction _reaction = reactionBuilder.getReaction();
        _reactions.add(_reaction);
        reactionBuilder.setSegmentBuilder(this);
        FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> _childBuilders = this.getChildBuilders();
        _childBuilders.add(reactionBuilder);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public FluentReactionsSegmentBuilder operator_add(final FluentRoutineBuilder routineBuilder) {
    FluentReactionsSegmentBuilder _xblockexpression = null;
    {
      this.checkNotYetAttached();
      FluentReactionsSegmentBuilder _segmentBuilder = routineBuilder.getSegmentBuilder();
      boolean _tripleNotEquals = (_segmentBuilder != this);
      if (_tripleNotEquals) {
        FluentReactionsSegmentBuilder _segmentBuilder_1 = routineBuilder.getSegmentBuilder();
        boolean _tripleEquals = (_segmentBuilder_1 == 
          null);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("The ");
        _builder.append(routineBuilder);
        _builder.append(" has already been added to the ");
        FluentReactionsSegmentBuilder _segmentBuilder_2 = routineBuilder.getSegmentBuilder();
        _builder.append(_segmentBuilder_2);
        Preconditions.checkArgument(_tripleEquals, _builder);
        final Function1<Routine, Boolean> _function = (Routine it) -> {
          String _name = it.getName();
          String _name_1 = routineBuilder.routine.getName();
          return Boolean.valueOf(Objects.equal(_name, _name_1));
        };
        boolean _exists = IterableExtensions.<Routine>exists(this.segment.getRoutines(), _function);
        boolean _not = (!_exists);
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The ");
        _builder_1.append(this);
        _builder_1.append(" already contains a routine with name \'");
        String _name = routineBuilder.routine.getName();
        _builder_1.append(_name);
        _builder_1.append("\'!");
        Preconditions.checkArgument(_not, _builder_1);
        EList<Routine> _routines = this.segment.getRoutines();
        _routines.add(routineBuilder.routine);
        routineBuilder.setSegmentBuilder(this);
        FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> _childBuilders = this.getChildBuilders();
        _childBuilders.add(routineBuilder);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("reaction segment builder for “");
    String _name = this.segment.getName();
    _builder.append(_name);
    _builder.append("”");
    return _builder.toString();
  }

  public Object add(final FluentReactionsSegmentChildBuilder reactionBuilder) {
    if (reactionBuilder instanceof FluentReactionBuilder) {
      return _add((FluentReactionBuilder)reactionBuilder);
    } else if (reactionBuilder instanceof FluentRoutineBuilder) {
      return _add((FluentRoutineBuilder)reactionBuilder);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(reactionBuilder).toString());
    }
  }

  @Pure
  ReactionsSegment getSegment() {
    return this.segment;
  }
}
