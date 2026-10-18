package tools.vitruv.dsls.reactions.builder;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import tools.vitruv.change.utils.ResourceAccess;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;

public class TypeProvider implements IJvmTypeProvider {

  protected IJvmTypeProvider delegate;
  @Getter(AccessLevel.PUBLIC)
  protected JvmTypeReferenceBuilder jvmTypeReferenceBuilder;
  private final FluentReactionsSegmentChildBuilder builder;
  protected XExpression scopeExpression;

  protected TypeProvider(IJvmTypeProvider delegate, JvmTypeReferenceBuilder jvmTypeReferenceBuilder,
      FluentReactionsSegmentChildBuilder builder, XExpression scopeExpression) {
    this.delegate = delegate;
    this.builder = builder;
    this.jvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
    this.scopeExpression = scopeExpression;
  }

  @Override
  public JvmType findTypeByName(String name) {
    return builder.possiblyImported(delegate.findTypeByName(name));
  }

  @Override
  public JvmType findTypeByName(String name, boolean binaryNestedTypeDelimiter) {
    return builder.possiblyImported(delegate.findTypeByName(name, binaryNestedTypeDelimiter));
  }

  public JvmOperation findMethod(Class<?> containingClass, String methodName) {
    JvmType type = findTypeByName(containingClass.getName());
    if (type instanceof JvmGenericType) {
      return findMethod((JvmDeclaredType) type, methodName);
    } else {
      throw new IllegalStateException("Could not find type " + containingClass.getName() + "!");
    }
  }

  @Override
  public ResourceSet getResourceSet() {
    return delegate.getResourceSet();
  }

  public <T extends JvmIdentifiableElement> T imported(T type) {
    return builder.<T>possiblyImported(type);
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

  public JvmDeclaredType staticExtensionAllImported(JvmDeclaredType type) {
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

  public JvmFormalParameter variableRaw(String variableName) {
    return this.builder.correspondingMethodParameter(this.scopeExpression, variableName);
  }

  /**
   * Retrieves a feature call to a previously declared variable or
   * routine/reaction parameter if it is present.
   */
  public XFeatureCall variable(String variableName) {
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
    return findAttribute(this.userExecutionType(), "executionState");
  }

  public JvmDeclaredType executionStateType() {
    return (JvmDeclaredType) this.executionStateField().getType().getType();
  }

  public XFeatureCall executionState() {
    return this.builder.featureCall(this.executionStateField());
  }

  public JvmOperation resourceAccessMethod() {
    return findMethod(this.executionStateType(), "getResourceAccess");
  }

  public JvmDeclaredType resourceAccessType() {
    return ((JvmDeclaredType) this.findTypeByName(ResourceAccess.class.getName()));
  }

  public XFeatureCall resourceAccess() {
    XFeatureCall featureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    featureCall.setFeature(resourceAccessMethod());
    featureCall.setImplicitReceiver(executionState());
    return featureCall;
  }

  public JvmOperation correspondenceModelMethod() {
    return findMethod(this.executionStateType(), "getCorrespondenceModel");
  }

  public XFeatureCall correspondenceModel() {
    XFeatureCall featureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    featureCall.setFeature(correspondenceModelMethod());
    featureCall.setImplicitReceiver(this.executionState());
    return featureCall;
  }

  public static JvmField findAttribute(JvmDeclaredType declaredType, String attributeName) {
    Stream<JvmField> directMembers =
        declaredType.getMembers().stream()
            .filter(member -> member.getSimpleName().equals(attributeName))
            .filter(JvmField.class::isInstance)
            .map(JvmField.class::cast);
    Stream<JvmField> superTypeMembers = declaredType.getSuperTypes()
        .stream().map(JvmTypeReference::getType)
        .filter(superType -> superType instanceof JvmDeclaredType)
        .map(superType -> findAttribute((JvmDeclaredType) superType, attributeName));
    Optional<JvmField> result = Stream.concat(directMembers, superTypeMembers).findFirst();

    if (result.isEmpty()) {
      throw new IllegalStateException(
          "Could not find the attribute " + attributeName
              + "in " + declaredType.getQualifiedName() + "!");
    }
    return result.get();
  }

  public static JvmOperation findMethod(JvmDeclaredType declaredType, String methodName) {
    Optional<JvmOperation> result =
        declaredType.getMembers().stream()
            .filter(member -> member.getSimpleName().equals(methodName))
            .filter(JvmOperation.class::isInstance)
            .map(JvmOperation.class::cast)
            .findFirst();

    if (result.isEmpty()) {
      throw new IllegalStateException(
          "Could not find the method " + methodName
              + " in " + declaredType.getQualifiedName() + "!");
    }
    return result.get();
  }

}
