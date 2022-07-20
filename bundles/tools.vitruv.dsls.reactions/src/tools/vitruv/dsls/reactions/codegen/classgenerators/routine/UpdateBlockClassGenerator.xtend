package tools.vitruv.dsls.reactions.codegen.classgenerators.routine

import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmTypeReference
import tools.vitruv.dsls.reactions.runtime.AbstractRepairRoutineRealization
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import org.eclipse.xtext.common.types.JvmGenericType
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull
import org.eclipse.xtend2.lib.StringConcatenationClient

/**
 * Generates for an {@link UpdateBlock} of a routine a class with a method (with the name defined in 
 * {@link #UPDATE_MODELS_METHOD_NAME}) that accepts the specified accessible elements and performs the 
 * given update code on them.
 */
class UpdateBlockClassGenerator extends StepExecutionClassGenerator {
	static val UPDATE_MODELS_METHOD_NAME = "updateModels"
	static val ROUTINES_FACADE_CLASS_PARAMETER_NAME = "_routinesFacade"

	val String qualifiedClassName
	val UpdateBlock updateBlock
	val JvmTypeReference routinesFacadeClassReference
	val Iterable<AccessibleElement> accessibleElements

	var JvmGenericType generatedClass

	/**
	 * Create a class generator for the given routine update block. The generated class has
	 * the given qualified name and accepts the given accessible elements as parameters in the
	 * provided {@code updateModels} method.
	 * 
	 * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
	 * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
	 * @param updateBlock the code block with update code to create a class for, must not be {@code null}
	 * @param routinesFacadeClassReference a type reference to the facade class for calling other routines
	 * @param accessibleElements the elements to be passed to the generated {@code match} method, must not be {@code null}
	 */
	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider, String qualifiedClassName, UpdateBlock updateBlock,
		JvmTypeReference routinesFacadeClassReference, Iterable<AccessibleElement> accessibleElements) {
		super(typesBuilderExtensionProvider)
		checkArgument(!qualifiedClassName.nullOrEmpty, "class name must not be null or empty")
		this.qualifiedClassName = qualifiedClassName
		this.updateBlock = checkNotNull(updateBlock, "update block must not be null")
		this.routinesFacadeClassReference = checkNotNull(routinesFacadeClassReference,
			"facade class reference must not be null")
		this.accessibleElements = checkNotNull(accessibleElements, "accessible elements must not be null")
	}

	override generateEmptyClass() {
		this.generatedClass = updateBlock.toClass(qualifiedClassName) [
			visibility = JvmVisibility.PRIVATE
			static = true
		]
		return generatedClass
	}

	override generateBody() {
		generatedClass => [
			superTypes += typeRef(AbstractRepairRoutineRealization.Update)
			members += generateConstructor()
			members += generateUpdateMethod()
		]
	}

	private def generateConstructor() {
		updateBlock.toConstructor [
			val reactionExecutionStateParameter = generateReactionExecutionStateParameter()
			parameters += reactionExecutionStateParameter
			body = '''super(«reactionExecutionStateParameter.name»);'''
		]
	}

	private def JvmOperation generateUpdateMethod() {
		updateBlock.toMethod(UPDATE_MODELS_METHOD_NAME, typeRef(Void.TYPE)) [
			parameters += generateAccessibleElementsParameters(accessibleElements)
			val facadeParam = toParameter(ROUTINES_FACADE_CLASS_PARAMETER_NAME, routinesFacadeClassReference)
			facadeParam.annotations += annotationRef(Extension)
			parameters += facadeParam
			body = updateBlock?.code
		]
	}

	override StringConcatenationClient generateStepExecutionCode(
		StringConcatenationClient prefix,
		String executionStateArgument,
		String routineFacadeArgument,
		Iterable<String> accessibleElementArguments,
		StringConcatenationClient suffix
	) '''
		«prefix»new «qualifiedClassName»(«executionStateArgument»).«UPDATE_MODELS_METHOD_NAME»(« //
		FOR argument : accessibleElementArguments SEPARATOR ", " AFTER ", "»«argument»«ENDFOR»«routineFacadeArgument»);
		«suffix»
	'''

	override getNewlyAccessibleElementsAfterExecution() {
		return emptyList
	}

	override getNewlyAccessibleElementsContainerType() {
		return null
	}

}
