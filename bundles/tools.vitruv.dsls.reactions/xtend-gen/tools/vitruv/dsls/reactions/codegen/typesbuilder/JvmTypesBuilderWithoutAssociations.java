package tools.vitruv.dsls.reactions.codegen.typesbuilder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociator;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@Singleton
@SuppressWarnings("all")
public class JvmTypesBuilderWithoutAssociations extends JvmTypesBuilder {
  @Inject
  private TypesFactory typesFactory;

  @Inject
  private IJvmModelAssociator associator;

  /**
   * Creates a public class with the given name.
   * 
   * @param name the simple name of the resulting class. If {@code null}, {@code null} will be returned.
   * @param initializer the initializer to apply to the class. If {@code null}, no initialization will be applied.
   * 
   * @return a {@link JvmGenericType} representing a Java class with the given name, {@code null} if name is {@code null}.
   */
  public JvmGenericType generateUnassociatedClass(final String name, final Procedure1<? super JvmGenericType> initializer) {
    if ((name == null)) {
      return null;
    }
    final Pair<String, String> fullName = this.splitQualifiedName(name);
    final JvmGenericType result = this.typesFactory.createJvmGenericType();
    result.setSimpleName(fullName.getSecond());
    String _first = fullName.getFirst();
    boolean _tripleNotEquals = (_first != null);
    if (_tripleNotEquals) {
      result.setPackageName(fullName.getFirst());
    }
    result.setVisibility(JvmVisibility.PUBLIC);
    return this.<JvmGenericType>initializeSafely(result, initializer);
  }

  /**
   * Creates a private field with the given name and the given type.
   * 
   * @param name the simple name of the resulting field. If {@code null}, {@code null} will be returned.
   * @param typeRef the type of the field, may not be {@code null}
   * @param initializer the initializer to apply to the field. If {@code null}, no initialization will be applied.
   * 
   * @return a {@link JvmField} representing a Java field with the given simple name and type, {@code null} if name is {@code null}.
   */
  public JvmField generateUnassociatedField(final String name, final JvmTypeReference typeRef, final Procedure1<? super JvmField> initializer) {
    if ((name == null)) {
      return null;
    }
    final JvmField result = this.typesFactory.createJvmField();
    result.setSimpleName(name);
    result.setVisibility(JvmVisibility.PRIVATE);
    result.setType(this.cloneWithProxies(typeRef));
    return this.<JvmField>initializeSafely(result, initializer);
  }

  /**
   * Creates a public constructor.
   * 
   * @param initializer the initializer to apply on the created constructor. If {@code null}, no initialization will be applied.
   * 
   * @return a {@link JvmConstructor} representing a Java constructor
   */
  public JvmConstructor generateUnassociatedConstructor(final Procedure1<? super JvmConstructor> initializer) {
    final JvmConstructor result = this.typesFactory.createJvmConstructor();
    return this.<JvmConstructor>initializeSafely(result, initializer);
  }

  /**
   * Creates a public method with the given name and the given return type.
   * 
   * @param name the simple name of the method to be created. If {@code null}, {@code null} will be returned.
   * @param returnType the return type of the created method, must not be {@code null}
   * @param initializer the initializer to apply on the created method. If {@code null}, no initialization will be applied.
   * 
   * @return a {@code JvmOperation} representing a Java method with the given name, {@code null} if name is {@code null}.
   */
  public JvmOperation generateUnassociatedMethod(final String name, final JvmTypeReference returnType, final Procedure1<? super JvmOperation> initializer) {
    if ((name == null)) {
      return null;
    }
    final JvmOperation result = this.typesFactory.createJvmOperation();
    result.setSimpleName(name);
    result.setVisibility(JvmVisibility.PUBLIC);
    result.setReturnType(this.cloneWithProxies(returnType));
    return this.<JvmOperation>initializeSafely(result, initializer);
  }

  public JvmFormalParameter createParameter(final String name, final JvmTypeReference typeRef) {
    final JvmFormalParameter result = this.typesFactory.createJvmFormalParameter();
    result.setName(name);
    result.setParameterType(this.cloneWithProxies(typeRef));
    return result;
  }

  /**
   * @Nullable
   */
  @Override
  public <T extends EObject> T associate(final EObject sourceElement, final T target) {
    if (((((sourceElement != null) && (target != null)) && (sourceElement.eResource() != null)) && 
      this.isValidSource(sourceElement))) {
      this.associator.associate(sourceElement, target);
    }
    return target;
  }

  /**
   * @Nullable
   */
  public <T extends EObject> T associatePrimary(final EObject sourceElement, final T target) {
    if (((((sourceElement != null) && (target != null)) && (sourceElement.eResource() != null)) && 
      this.isValidSource(sourceElement))) {
      this.associator.associatePrimary(sourceElement, target);
    }
    return target;
  }
}
