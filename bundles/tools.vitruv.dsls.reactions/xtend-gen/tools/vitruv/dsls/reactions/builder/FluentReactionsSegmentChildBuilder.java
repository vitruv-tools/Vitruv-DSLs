package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;

@SuppressWarnings("all")
abstract class FluentReactionsSegmentChildBuilder extends FluentReactionElementBuilder {
  @Accessors({ AccessorType.PACKAGE_SETTER, AccessorType.PACKAGE_GETTER })
  private FluentReactionsSegmentBuilder segmentBuilder;

  protected FluentReactionsSegmentChildBuilder(final FluentBuilderContext context) {
    super(context);
  }

  @Override
  public boolean willGenerateCode() {
    return true;
  }

  protected void transferReactionsSegmentTo(final FluentReactionsSegmentChildBuilder infector, final FluentReactionsSegmentChildBuilder infected) {
    final Consumer<FluentReactionsSegmentChildBuilder> _function = (FluentReactionsSegmentChildBuilder it) -> {
      this.infect(it, infected);
    };
    this.<FluentReactionsSegmentChildBuilder>beforeAttached(infector, _function);
    final Consumer<FluentReactionsSegmentChildBuilder> _function_1 = (FluentReactionsSegmentChildBuilder it) -> {
      this.checkReactionsSegmentIsCompatibleTo(it, infector);
    };
    this.<FluentReactionsSegmentChildBuilder>beforeAttached(infected, _function_1);
  }

  private Object infect(final FluentReactionsSegmentChildBuilder infector, final FluentReactionsSegmentChildBuilder infected) {
    Object _xblockexpression = null;
    {
      this.checkReactionsSegmentIsCompatibleTo(infector, infected);
      Object _xifexpression = null;
      if (((infector.segmentBuilder != null) && (infected.segmentBuilder == null))) {
        _xifexpression = infector.segmentBuilder.add(infected);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void checkReactionsSegmentIsCompatibleTo(final FluentReactionsSegmentChildBuilder a, final FluentReactionsSegmentChildBuilder b) {
    if ((((a.getAttachedReactionsFile() != null) && (b.getAttachedReactionsFile() != null)) && 
      (!Objects.equal(a.getAttachedReactionsFile(), b.getAttachedReactionsFile())))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(a);
      _builder.append(" is in a different reaction file than ");
      _builder.append(b);
      _builder.append("!");
      throw new RuntimeException(_builder.toString());
    }
  }

  protected abstract String getCreatedElementName();

  protected abstract String getCreatedElementType();

  protected JvmFormalParameter correspondingMethodParameter(final XExpression correpondingExpression, final String parameterName) {
    final JvmOperation method = this.getCorrespondingMethod(correpondingExpression);
    final Function1<JvmFormalParameter, Boolean> _function = (JvmFormalParameter it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, parameterName));
    };
    final JvmFormalParameter parameter = IterableExtensions.<JvmFormalParameter>findFirst(method.getParameters(), _function);
    if ((parameter == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find the variable or parameter “");
      _builder.append(parameterName);
      _builder.append("” in the ");
      String _createdElementType = this.getCreatedElementType();
      _builder.append(_createdElementType);
      _builder.append(" “");
      String _createdElementName = this.getCreatedElementName();
      _builder.append(_createdElementName);
      _builder.append("”.");
      throw new IllegalStateException(_builder.toString());
    }
    return parameter;
  }

  protected JvmOperation getCorrespondingMethod(final XExpression correpondingExpression) {
    final JvmIdentifiableElement method = this.context.getJvmModelAssociator().getNearestLogicalContainer(correpondingExpression);
    if ((method instanceof JvmOperation)) {
      return ((JvmOperation)method);
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Could not find the method corresponding to “");
    _builder.append(correpondingExpression);
    _builder.append("” in the ");
    String _createdElementType = this.getCreatedElementType();
    _builder.append(_createdElementType);
    _builder.append(" “");
    String _createdElementName = this.getCreatedElementName();
    _builder.append(_createdElementName);
    _builder.append("”");
    throw new IllegalStateException(_builder.toString());
  }

  protected static List<XExpression> extractExpressions(final XExpression expression) {
    List<XExpression> _xblockexpression = null;
    {
      if ((expression == null)) {
        return Collections.<XExpression>unmodifiableList(CollectionLiterals.<XExpression>newArrayList());
      }
      List<XExpression> _switchResult = null;
      boolean _matched = false;
      if (expression instanceof XBlockExpression) {
        _matched=true;
        _switchResult = ((XBlockExpression)expression).getExpressions();
      }
      if (!_matched) {
        _switchResult = Collections.<XExpression>unmodifiableList(CollectionLiterals.<XExpression>newArrayList(expression));
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  protected XFeatureCall featureCall(final JvmIdentifiableElement element) {
    XFeatureCall _xblockexpression = null;
    {
      if ((element == null)) {
        return null;
      }
      XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        it.setFeature(element);
      };
      _xblockexpression = ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
    }
    return _xblockexpression;
  }

  protected TypeProvider getTypeProvider(final XExpression scopeExpression) {
    IJvmTypeProvider _delegateTypeProvider = this.delegateTypeProvider();
    JvmTypeReferenceBuilder _referenceBuilderFactory = this.referenceBuilderFactory();
    return new TypeProvider(_delegateTypeProvider, _referenceBuilderFactory, this, scopeExpression);
  }

  public XFeatureCall getJvmOperationRoutineFacade(final XExpression codeBlock) {
    return this.featureCall(this.correspondingMethodParameter(codeBlock, ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME));
  }

  @Pure
  FluentReactionsSegmentBuilder getSegmentBuilder() {
    return this.segmentBuilder;
  }

  void setSegmentBuilder(final FluentReactionsSegmentBuilder segmentBuilder) {
    this.segmentBuilder = segmentBuilder;
  }
}
