package tools.vitruv.dsls.reactions.codegen.typesbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.language.inputTypes.InputTypesPackage;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import org.eclipse.emf.ecore.EEnum;

public class ParameterGenerator {
	private final String MISSING_PARAMETER_NAME = "/* Name missing */";
	
	protected final JvmTypeReferenceBuilder typeReferenceBuilder;
	protected final JvmTypesBuilderWithoutAssociations typesBuilder;
	
	public ParameterGenerator(JvmTypeReferenceBuilder typeReferenceBuilder, JvmTypesBuilderWithoutAssociations typesBuilder) {
		this.typeReferenceBuilder = typeReferenceBuilder;
		this.typesBuilder = typesBuilder;
	}
	
	public JvmFormalParameter generateParameter(EObject contextObject, AccessibleElement element) {
		return typesBuilder.toParameter(
				contextObject,
				element.getName(),
				element.generateTypeRef(typeReferenceBuilder));
	}
	
	public List<JvmFormalParameter> generateParameters(
			EObject contextObject, Iterable<AccessibleElement> elements) {
		List<JvmFormalParameter> parameters = new LinkedList<>();
		for(AccessibleElement element: elements) {
			parameters.add(typesBuilder.toParameter(
				contextObject, element.getName(), element.generateTypeRef(typeReferenceBuilder)
			));
		}
		return parameters;
	}
	
	public List<AccessibleElement> getInputElements(Iterable<NamedMetaclassReference> metaclassReferences,
																									Iterable<NamedJavaElementReference> javaElements) {
		List<AccessibleElement> inputElements = new ArrayList<>();
		for (var metaclassReference: metaclassReferences) {
			String name = metaclassReference.getName();
			if (name == null) {
				name = MISSING_PARAMETER_NAME;
			}
			String metaclassCanonicalName = null;
			if (metaclassReference.getMetaclass() != null) {
				metaclassCanonicalName = getMappedInstanceClassCanonicalName(metaclassReference.getMetaclass());
			}
			inputElements.add(new AccessibleElement(name, metaclassCanonicalName));
		}

		for (var javaElement: javaElements) {
			String name = javaElement.getName();
			if (name == null) {
				name = MISSING_PARAMETER_NAME;
			}
			String qualifiedName = null;
			if (javaElement.getType() != null) {
				qualifiedName = javaElement.getType().getQualifiedName();
			}
			inputElements.add(new AccessibleElement(name, qualifiedName));
		}
		return inputElements;
	}
	
	private static String getMappedInstanceClassCanonicalName(EClassifier classifier) {
		if (classifier.equals(InputTypesPackage.Literals.STRING)) {
			return String.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.INTEGER)) {
			return Integer.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.LONG)) {
			return Long.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.SHORT)) {
			return Short.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.BOOLEAN)) {
			return Boolean.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.CHARACTER)) {
			return Character.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.BYTE)) {
			return Byte.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.FLOAT)) {
			return Float.class.getName();
		}
		if (classifier.equals(InputTypesPackage.Literals.DOUBLE)) {
			return Double.class.getName();
		}
		if (classifier instanceof EEnum || classifier instanceof EClass) {
			return ReactionsLanguageHelper.getJavaClassName(classifier);
		}
		throw new IllegalArgumentException("Unhandled parameter types: " + classifier);
	}
}
							