package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.change.propagation.ResourceAccess;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;

@SuppressWarnings("all")
public class TypeProvider implements IJvmTypeProvider {
  protected final IJvmTypeProvider delegate;

  @Accessors(AccessorType.PUBLIC_GETTER)
  protected final JvmTypeReferenceBuilder jvmTypeReferenceBuilder;

  @Extension
  protected final FluentReactionsSegmentChildBuilder builder;

  protected final XExpression scopeExpression;

  protected TypeProvider(final IJvmTypeProvider delegate, final JvmTypeReferenceBuilder jvmTypeReferenceBuilder, final FluentReactionsSegmentChildBuilder builder, final XExpression scopeExpression) {
    this.delegate = delegate;
    this.builder = builder;
    this.jvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
    this.scopeExpression = scopeExpression;
  }

  @Override
  public JvmType findTypeByName(final String name) {
    return this.builder.<JvmType>possiblyImported(this.delegate.findTypeByName(name));
  }

  @Override
  public JvmType findTypeByName(final String name, final boolean binaryNestedTypeDelimiter) {
    return this.builder.<JvmType>possiblyImported(this.delegate.findTypeByName(name, binaryNestedTypeDelimiter));
  }

  public JvmOperation findMethod(final Class<?> containingClass, final String methodName) {
    final JvmType type = this.findTypeByName(containingClass.getName());
    if ((type instanceof JvmGenericType)) {
      return TypeProvider.findMethod(((JvmDeclaredType)type), methodName);
    } else {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find type “");
      String _name = containingClass.getName();
      _builder.append(_name);
      _builder.append("”!");
      throw new IllegalStateException(_builder.toString());
    }
  }

  @Override
  public ResourceSet getResourceSet() {
    return this.delegate.getResourceSet();
  }

  public <T extends JvmIdentifiableElement> T imported(final T type) {
    return this.builder.<T>possiblyImported(type);
  }

  public JvmOperation staticImported(final JvmOperation operation) {
    return this.builder.staticImported(operation);
  }

  public JvmOperation staticExtensionImported(final JvmOperation operation) {
    return this.builder.staticExtensionImported(operation);
  }

  public JvmOperation staticExtensionWildcardImported(final JvmOperation operation) {
    return this.builder.staticExtensionWildcardImported(operation);
  }

  public JvmDeclaredType staticExtensionAllImported(final JvmDeclaredType type) {
    return this.builder.staticExtensionAllImported(type);
  }

  public XFeatureCall affectedEObject() {
    return this.variable(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE);
  }

  public XFeatureCall oldValue() {
    return this.variable(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE);
  }

  public XFeatureCall newValue() {
    return this.variable(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE);
  }

  public JvmFormalParameter variableRaw(final String variableName) {
    return this.builder.correspondingMethodParameter(this.scopeExpression, variableName);
  }

  /**
   * Retrieves a feature call to a previously declared variable or
   * routine/reaction parameter if it is present.
   */
  public XFeatureCall variable(final String variableName) {
    return this.builder.featureCall(this.variableRaw(variableName));
  }

  /**
   * Retrieves the user execution class.
   */
  public JvmDeclaredType userExecutionType() {
    return this.builder.getCorrespondingMethod(this.scopeExpression).getDeclaringType();
  }

  /**
   * Retrieves a feature call to the user execution class.
   */
  public XFeatureCall userExecution() {
    return this.builder.featureCall(this.userExecutionType());
  }

  public JvmField executionStateField() {
    return TypeProvider.findAttribute(this.userExecutionType(), "executionState");
  }

  public JvmDeclaredType executionStateType() {
    JvmType _type = this.executionStateField().getType().getType();
    return ((JvmDeclaredType) _type);
  }

  public XFeatureCall executionState() {
    return this.builder.featureCall(this.executionStateField());
  }

  public JvmOperation resourceAccessMethod() {
    return TypeProvider.findMethod(this.executionStateType(), "getResourceAccess");
  }

  public JvmDeclaredType resourceAccessType() {
    JvmType _findTypeByName = this.findTypeByName(ResourceAccess.class.getName());
    return ((JvmDeclaredType) _findTypeByName);
  }

  public XFeatureCall resourceAccess() {
    XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setFeature(this.resourceAccessMethod());
      it.setImplicitReceiver(this.executionState());
    };
    return ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
  }

  public JvmOperation correspondenceModelMethod() {
    return TypeProvider.findMethod(this.executionStateType(), "getCorrespondenceModel");
  }

  public XFeatureCall correspondenceModel() {
    XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setFeature(this.correspondenceModelMethod());
      it.setImplicitReceiver(this.executionState());
    };
    return ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
  }

  public static JvmField findAttribute(final JvmDeclaredType declaredType, final String attributeName) {
    final Function1<JvmMember, Boolean> _function = (JvmMember it) -> {
      String _simpleName = it.getSimpleName();
      return Boolean.valueOf(Objects.equal(_simpleName, attributeName));
    };
    Iterable<JvmField> _filter = Iterables.<JvmField>filter(IterableExtensions.<JvmMember>filter(declaredType.getMembers(), _function), JvmField.class);
    final Function1<JvmTypeReference, JvmType> _function_1 = (JvmTypeReference it) -> {
      return it.getType();
    };
    final Function1<JvmDeclaredType, JvmField> _function_2 = (JvmDeclaredType it) -> {
      return TypeProvider.findAttribute(it, attributeName);
    };
    Iterable<JvmField> _map = IterableExtensions.<JvmDeclaredType, JvmField>map(Iterables.<JvmDeclaredType>filter(ListExtensions.<JvmTypeReference, JvmType>map(declaredType.getSuperTypes(), _function_1), JvmDeclaredType.class), _function_2);
    final JvmField result = IterableExtensions.<JvmField>head(Iterables.<JvmField>concat(_filter, _map));
    if ((result == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find the attribute “");
      _builder.append(attributeName);
      _builder.append("” in ‹");
      String _qualifiedName = declaredType.getQualifiedName();
      _builder.append(_qualifiedName);
      _builder.append("›!");
      throw new IllegalStateException(_builder.toString());
    }
    return result;
  }

  public static JvmOperation findMethod(final JvmDeclaredType declaredType, final String methodName) {
    final Function1<JvmOperation, Boolean> _function = (JvmOperation it) -> {
      String _simpleName = it.getSimpleName();
      return Boolean.valueOf(Objects.equal(_simpleName, methodName));
    };
    final JvmOperation result = IterableExtensions.<JvmOperation>head(IterableExtensions.<JvmOperation>filter(Iterables.<JvmOperation>filter(declaredType.getMembers(), JvmOperation.class), _function));
    if ((result == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find the method “");
      _builder.append(methodName);
      _builder.append("” in ‹");
      String _qualifiedName = declaredType.getQualifiedName();
      _builder.append(_qualifiedName);
      _builder.append("›!");
      throw new IllegalStateException(_builder.toString());
    }
    return result;
  }

  @Pure
  public JvmTypeReferenceBuilder getJvmTypeReferenceBuilder() {
    return this.jvmTypeReferenceBuilder;
  }
}
