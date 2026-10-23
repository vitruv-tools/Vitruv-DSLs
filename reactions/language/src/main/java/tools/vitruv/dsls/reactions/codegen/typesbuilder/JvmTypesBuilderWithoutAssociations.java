package tools.vitruv.dsls.reactions.codegen.typesbuilder;

import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.common.types.TypesFactory;
import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociator;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import com.google.inject.Singleton;
import org.eclipse.xtext.common.types.JvmConstructor;

@Singleton
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
	public JvmGenericType generateUnassociatedClass(
			/* @Nullable */ String name,
			/* @Nullable */ Procedure1<? super JvmGenericType> initializer) {
		if (name == null) {
			return null;
		}
		Pair<String, String> fullName = splitQualifiedName(name);
		JvmGenericType result = typesFactory.createJvmGenericType();
		result.setSimpleName(fullName.getSecond());
		if (fullName.getFirst() != null) {
			result.setPackageName(fullName.getFirst());
		}
		result.setVisibility(JvmVisibility.PUBLIC);
		return initializeSafely(result, initializer);
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
	public JvmField generateUnassociatedField(
			/* @Nullable */ String name,
			JvmTypeReference typeRef,
			/* @Nullable */ Procedure1<? super JvmField> initializer) {
		if (name == null) {
			return null;
		}
		JvmField result = typesFactory.createJvmField();
		result.setSimpleName(name);
		result.setVisibility(JvmVisibility.PRIVATE);
		result.setType(cloneWithProxies(typeRef));
		return initializeSafely(result, initializer);
	}

	/**
	 * Creates a public constructor.
	 * 
	 * @param initializer the initializer to apply on the created constructor. If {@code null}, no initialization will be applied.
	 * 
	 * @return a {@link JvmConstructor} representing a Java constructor
	 */
	public JvmConstructor generateUnassociatedConstructor(
			/* @Nullable */ Procedure1<? super JvmConstructor> initializer) {
		JvmConstructor result = typesFactory.createJvmConstructor();
		return initializeSafely(result, initializer);
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
	public JvmOperation generateUnassociatedMethod( /* @Nullable */ String name,
			JvmTypeReference returnType,
			/* @Nullable */ Procedure1<? super JvmOperation> initializer) {
		if (name == null) {
			return null;
		}
		JvmOperation result = typesFactory.createJvmOperation();
		result.setSimpleName(name);
		result.setVisibility(JvmVisibility.PUBLIC);
		result.setReturnType(cloneWithProxies(returnType));
		return initializeSafely(result, initializer);
	}

	public JvmFormalParameter createParameter(String name, JvmTypeReference typeRef) {
		JvmFormalParameter result = typesFactory.createJvmFormalParameter();
		result.setName(name);
		result.setParameterType(cloneWithProxies(typeRef));
		return result;
	}
	
	/**
	 * Associates a source element with a target element. This association is used for tracing. Navigation, for
	 * instance, uses this information to find the real declaration of a Jvm element.
	 * 
	 * @see org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociator
	 * @see org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
	 * 
	 * @return the target for convenience.
	 */
	@Override
	public <T extends EObject> T associate(
			/* @Nullable */ EObject sourceElement,
			/* @Nullable */ T target) {
		if (sourceElement != null && target != null && sourceElement.eResource() != null &&
			isValidSource(sourceElement))
			associator.associate(sourceElement, target);
		return target;
	}

	/**
	 * Associates a source element with a target element and marks the association as primary
	 * on both sides. This association is used for tracing. Navigation, for
	 * instance, uses this information to find the real declaration of a Jvm element.
	 * 
	 * @see org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociator
	 * @see org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
	 * 
	 * @return the target for convenience.
	 */
	public <T extends EObject> T associatePrimary(
			/* @Nullable */ EObject sourceElement,
			/* @Nullable */ T target) {
		if (sourceElement != null && target != null && sourceElement.eResource() != null &&
			isValidSource(sourceElement))
			associator.associatePrimary(sourceElement, target);
		return target;
	}
}
