package tools.vitruv.dsls.reactions.codegen.typesbuilder

import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import org.eclipse.emf.ecore.EClass
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment
import tools.vitruv.dsls.reactions.language.inputTypes.InputTypesPackage
import tools.vitruv.change.atomic.EChange
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.*;
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference
import tools.vitruv.dsls.common.elements.NamedMetaclassReference

class ParameterGenerator {
	static val MISSING_PARAMETER_NAME = "/* Name missing */"
	
	protected final extension JvmTypeReferenceBuilder _typeReferenceBuilder
	protected final extension JvmTypesBuilderWithoutAssociations _typesBuilder
	
	new (JvmTypeReferenceBuilder typeReferenceBuilder, JvmTypesBuilderWithoutAssociations typesBuilder) {
		_typeReferenceBuilder = typeReferenceBuilder
		_typesBuilder = typesBuilder
	}
	
	def JvmFormalParameter generateUntypedChangeParameter(EObject parameterContext) {
		return parameterContext.generateParameter(new AccessibleElement(CHANGE_PARAMETER_NAME, EChange))
	}
	
	def JvmFormalParameter generateRoutinesFacadeParameter(EObject parameterContext, ReactionsSegment reactionsSegment) {
		return generateParameter(parameterContext, new AccessibleElement(ROUTINES_FACADE_PARAMETER_NAME, reactionsSegment.routinesFacadeClassNameGenerator.qualifiedName))
	}
	
	def JvmFormalParameter generateReactionExecutionStateParameter(EObject parameterContext) {
		return generateParameter(parameterContext, new AccessibleElement(REACTION_EXECUTION_STATE_PARAMETER_NAME, ReactionExecutionState))
	}
	
	def generateParameter(EObject contextObject, AccessibleElement element) {
		toParameter(contextObject, element.name, element.generateTypeRef(_typeReferenceBuilder))
	}
	
	def Iterable<JvmFormalParameter> generateParameters(EObject contextObject, Iterable<AccessibleElement> elements) {
		elements.map[toParameter(contextObject, it.name, it.generateTypeRef(_typeReferenceBuilder))]
	}
	
	def Iterable<AccessibleElement> getInputElements(Iterable<NamedMetaclassReference> metaclassReferences, Iterable<NamedJavaElementReference> javaElements) {
		return metaclassReferences.map[new AccessibleElement(it.name ?: MISSING_PARAMETER_NAME, it.metaclass?.mappedInstanceClassCanonicalName)]
			+ javaElements.map[new AccessibleElement(it.name ?: MISSING_PARAMETER_NAME, it.type?.qualifiedName)];
	}
	
	private def getMappedInstanceClassCanonicalName(EClass eClass) {
		switch eClass {
			case InputTypesPackage.Literals.STRING: String.name
			case InputTypesPackage.Literals.INTEGER: Integer.name
			case InputTypesPackage.Literals.LONG: Long.name
			case InputTypesPackage.Literals.SHORT: Short.name
			case InputTypesPackage.Literals.BOOLEAN: Boolean.name
			case InputTypesPackage.Literals.CHARACTER: Character.name
			case InputTypesPackage.Literals.BYTE: Byte.name
			case InputTypesPackage.Literals.FLOAT: Float.name
			case InputTypesPackage.Literals.DOUBLE: Double.name
			default: eClass.javaClassName
		}
	}
	
}
							