package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import java.util.List;
import java.util.function.BiFunction;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.EClassAdapter;
import tools.vitruv.dsls.commonalities.language.elements.EDataTypeAdapter;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;

@SuppressWarnings("all")
public class AttributeChangeReactionsHelper extends ReactionsGenerationHelper {
  public enum AttributeChangeReactionType {
    VALUE_REPLACED,

    VALUE_INSERTED,

    VALUE_REMOVED;
  }

  AttributeChangeReactionsHelper() {
  }

  public List<FluentReactionBuilder> getAttributeChangeReactions(final Attribute attribute, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    return this.getAttributeChangeReactions(attribute, "", actionBuilder);
  }

  public List<FluentReactionBuilder> getAttributeChangeReactions(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    List<FluentReactionBuilder> _switchResult = null;
    Classifier _type = attribute.getType();
    boolean _matched = false;
    if (_type instanceof EDataTypeAdapter) {
      boolean _isMultiValued = attribute.isMultiValued();
      boolean _not = (!_isMultiValued);
      if (_not) {
        _matched=true;
        _switchResult = List.<FluentReactionBuilder>of(
          this.singleAttributeReplacedReaction(attribute, reactionNameSuffix, actionBuilder));
      }
    }
    if (!_matched) {
      if (_type instanceof EDataTypeAdapter) {
        boolean _isMultiValued = attribute.isMultiValued();
        if (_isMultiValued) {
          _matched=true;
          _switchResult = List.<FluentReactionBuilder>of(
            this.multiAttributeInsertReaction(attribute, reactionNameSuffix, actionBuilder), 
            this.multiAttributeRemoveReaction(attribute, reactionNameSuffix, actionBuilder));
        }
      }
    }
    if (!_matched) {
      if (_type instanceof EClassAdapter) {
        boolean _isMultiValued = attribute.isMultiValued();
        boolean _not = (!_isMultiValued);
        if (_not) {
          _matched=true;
          _switchResult = List.<FluentReactionBuilder>of(
            this.singleReferenceReplacedReaction(attribute, reactionNameSuffix, actionBuilder));
        }
      }
    }
    if (!_matched) {
      if (_type instanceof EClassAdapter) {
        boolean _isMultiValued = attribute.isMultiValued();
        if (_isMultiValued) {
          _matched=true;
          _switchResult = List.<FluentReactionBuilder>of(
            this.multiReferenceInsertReaction(attribute, reactionNameSuffix, actionBuilder), 
            this.multiReferenceRemoveReaction(attribute, reactionNameSuffix, actionBuilder));
        }
      }
    }
    if (!_matched) {
      if (_type instanceof LeastSpecificType) {
        _matched=true;
        _switchResult = CollectionLiterals.<FluentReactionBuilder>emptyList();
      }
    }
    if (!_matched) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Unexpected attribute type ‹");
      Classifier _type_1 = attribute.getType();
      _builder.append(_type_1);
      _builder.append("›!");
      throw new IllegalStateException(_builder.toString());
    }
    return _switchResult;
  }

  private FluentReactionBuilder singleAttributeReplacedReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Change");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterAttributeReplacedAt(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEAttribute(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_REPLACED, reaction);
  }

  private FluentReactionBuilder multiAttributeInsertReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Insert");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterAttributeInsertIn(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEAttribute(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_INSERTED, reaction);
  }

  private FluentReactionBuilder multiAttributeRemoveReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Remove");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterAttributeRemoveFrom(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEAttribute(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_REMOVED, reaction);
  }

  private FluentReactionBuilder singleReferenceReplacedReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Change");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement().replacedAt(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEReference(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_REPLACED, reaction);
  }

  private FluentReactionBuilder multiReferenceInsertReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Insert");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement().insertedIn(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEReference(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_INSERTED, reaction);
  }

  private FluentReactionBuilder multiReferenceRemoveReaction(final Attribute attribute, final String reactionNameSuffix, final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> actionBuilder) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(attribute);
    _builder.append(_reactionName);
    _builder.append("Remove");
    _builder.append(reactionNameSuffix);
    final FluentReactionBuilder.PreconditionOrRoutineCallBuilder reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement().removedFrom(this._generationContext.getChangeClass(attribute.getClassLikeContainer()), this._generationContext.getCorrespondingEReference(attribute));
    return actionBuilder.apply(AttributeChangeReactionsHelper.AttributeChangeReactionType.VALUE_REMOVED, reaction);
  }
}
