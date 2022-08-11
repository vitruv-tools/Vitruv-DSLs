package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import java.io.IOException
import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.*
import org.eclipse.xtext.common.types.JvmConstructor
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.common.ClassNameGenerator
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker.isReferenceable
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.CreateBlockClassGenerator
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.MatchBlockClassGenerator
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.UpdateBlockClassGenerator
import org.eclipse.xtend2.lib.StringConcatenationClient
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.EmptyStepExecutionClassGenerator
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.StepExecutionClassGenerator
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

class RoutineClassGenerator extends ClassGenerator {
	static val EXECUTION_STATE_VARIABLE = "getExecutionState()"
	static val ROUTINES_FACADE_VARIABLE = "getRoutinesFacade()"

	static val INPUT_VALUES_SIMPLE_CLASS_NAME = "InputValues"
	static val INPUT_VALUES_FIELD_NAME = "inputValues"
	static val RETRIEVED_VALUES_FIELD_NAME = "retrievedValues"
	static val CREATED_VALUES_FIELD_NAME = "createdValues"

	static val MATCH_SIMPLE_CLASS_NAME = "Match"
	static val CREATE_SIMPLE_CLASS_NAME = "Create"
	static val UPDATE_SIMPLE_CLASS_NAME = "Update"

	val Routine routine
	val ClassNameGenerator routineClassNameGenerator
	val Iterable<AccessibleElement> inputElements
	val StepExecutionClassGenerator matchBlockClassGenerator
	val StepExecutionClassGenerator createBlockClassGenerator
	val StepExecutionClassGenerator updateBlockClassGenerator
	val String routinesFacadeQualifiedName
	
	val JvmGenericType inputValuesClass
	var JvmGenericType generatedClass

	new(Routine routine, TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider)
		if (!routine.isReferenceable) {
			throw new IllegalArgumentException("incomplete")
		}
		this.routine = routine
		this.routineClassNameGenerator = routine.routineClassNameGenerator
		this.routinesFacadeQualifiedName = routine.reactionsSegment.routinesFacadeClassNameGenerator.qualifiedName
		this.inputElements = getInputElements(routine.input.modelInputElements, routine.input.javaInputElements)
		this.inputValuesClass = if (hasInputValues) {
			generateElementsContainerClass(INPUT_VALUES_SIMPLE_CLASS_NAME, inputElements)
		}
		this.matchBlockClassGenerator = if (routine.matchBlock !== null) {
			new MatchBlockClassGenerator(typesBuilderExtensionProvider, MATCH_SIMPLE_CLASS_NAME.nestedClassName,
				routine.matchBlock, inputElements)
		} else {
			new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider)
		}
		this.createBlockClassGenerator = if (routine.createBlock !== null) {
			new CreateBlockClassGenerator(typesBuilderExtensionProvider, CREATE_SIMPLE_CLASS_NAME.nestedClassName,
				routine.createBlock)
		} else {
			new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider)
		}
		val updateAccessibleElements = inputElements + matchBlockClassGenerator.newlyAccessibleElementsAfterExecution +
			createBlockClassGenerator.newlyAccessibleElementsAfterExecution
		this.updateBlockClassGenerator = if (routine.updateBlock !== null) {
			new UpdateBlockClassGenerator(typesBuilderExtensionProvider, UPDATE_SIMPLE_CLASS_NAME.nestedClassName,
				routine.updateBlock, typeRef(routinesFacadeQualifiedName), updateAccessibleElements)
		} else {
			new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider)
		}
	}

	private def getNestedClassName(String nestedClassSimpleName) {
		return routineClassNameGenerator.qualifiedName + "." + nestedClassSimpleName
	}

	override JvmGenericType generateEmptyClass() {
		matchBlockClassGenerator.generateEmptyClass()
		createBlockClassGenerator.generateEmptyClass()
		updateBlockClassGenerator.generateEmptyClass()
		generatedClass = routine.toClass(routineClassNameGenerator.qualifiedName) [
			visibility = JvmVisibility.PUBLIC
		]
	}

	override generateBody() {
		val executeMethod = generateMethodExecuteRoutine()
		generatedClass => [
			documentation = getCommentWithoutMarkers(routine.documentation)
			superTypes += typeRef(AbstractRoutine)
			if(hasInputValues) members += routine.toField(INPUT_VALUES_FIELD_NAME, typeRef(inputValuesClass))
			members += if (!matchBlockClassGenerator.empty)
				routine.toField(RETRIEVED_VALUES_FIELD_NAME,
					typeRef(matchBlockClassGenerator.newlyAccessibleElementsContainerType))
			members += if (!createBlockClassGenerator.empty)
				routine.toField(CREATED_VALUES_FIELD_NAME,
					typeRef(createBlockClassGenerator.newlyAccessibleElementsContainerType))
			members += inputValuesClass
			members += matchBlockClassGenerator.generateBody()
			members += createBlockClassGenerator.generateBody()
			members += updateBlockClassGenerator.generateBody()
			members += routine.generateConstructor()
			members += executeMethod
		]
	}

	private def boolean hasInputValues() {
		return !inputElements.empty
	}

	private def JvmConstructor generateConstructor(Routine routine) {
		return routine.toConstructor [
			visibility = JvmVisibility.PUBLIC
			val routinesFacadeParameter = generateParameter(new AccessibleElement("routinesFacade", routinesFacadeQualifiedName))
			val executionStateParameter = generateParameter(new AccessibleElement("reactionExecutionState", ReactionExecutionState))
			val calledByParameter = generateParameter(new AccessibleElement("calledBy", CallHierarchyHaving))
			val inputParameters = routine.generateParameters(inputElements)
			parameters += routinesFacadeParameter
			parameters += executionStateParameter
			parameters += calledByParameter
			parameters += inputParameters
			body = '''
				super(«routinesFacadeParameter.name», «executionStateParameter.name», «calledByParameter.name»);
				«IF hasInputValues»
					this.«INPUT_VALUES_FIELD_NAME» = new «inputValuesClass»(« 
						FOR inputParameter : inputParameters SEPARATOR ", "»«inputParameter.name»«ENDFOR»);
				«ENDIF»
			'''
		]
	}

	private def generateMethodExecuteRoutine() {
		val methodName = "executeRoutine"
		val inputElements = inputElements.mapFixed[INPUT_VALUES_FIELD_NAME + "." + name]
		val inputAndRetrievedElements = inputElements +
			matchBlockClassGenerator.newlyAccessibleElementsAfterExecution.mapFixed [
				RETRIEVED_VALUES_FIELD_NAME + "." + name
			]
		val inputAndRetrievedAndCreatedElements = inputAndRetrievedElements +
			createBlockClassGenerator.newlyAccessibleElementsAfterExecution.mapFixed [
				CREATED_VALUES_FIELD_NAME + "." + name
			]
		return generateUnassociatedMethod(methodName, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PROTECTED
			exceptions += typeRef(IOException)
			body = '''
				«generateDebugCode(inputElements)»
				«generateMatchCode(inputElements)»
				«generateCreateCode(inputAndRetrievedElements)»
				«generateUpdateCode(inputAndRetrievedAndCreatedElements)»
				return true;
			'''
		]
	}

	private def StringConcatenationClient generateDebugCode(Iterable<String> inputElementsAccessExpressions) {
		return '''
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("Called routine «routineClassNameGenerator.simpleName» with input:");
				«FOR inputElementAccessExpression : inputElementsAccessExpressions»
					getLogger().trace("   «inputElementAccessExpression»: " + «inputElementAccessExpression»);
				«ENDFOR»
			}
		'''
	}

	private def StringConcatenationClient generateMatchCode(Iterable<String> accessibleElementsAccessExpressions) {
		return matchBlockClassGenerator.generateStepExecutionCode(
				'''«RETRIEVED_VALUES_FIELD_NAME» = ''', EXECUTION_STATE_VARIABLE, ROUTINES_FACADE_VARIABLE,
			accessibleElementsAccessExpressions, '''
				if («RETRIEVED_VALUES_FIELD_NAME» == null) {
					return false;
				}
			''')
	}

	private def StringConcatenationClient generateCreateCode(Iterable<String> accessibleElementsAccessExpressions) {
		createBlockClassGenerator.generateStepExecutionCode(
			'''«CREATED_VALUES_FIELD_NAME» = ''',
			EXECUTION_STATE_VARIABLE,
			ROUTINES_FACADE_VARIABLE,
			null,
			''''''
		)
	}

	private def StringConcatenationClient generateUpdateCode(Iterable<String> accessibleElementsAccessExpressions) {
		updateBlockClassGenerator.generateStepExecutionCode(
			'''''',
			EXECUTION_STATE_VARIABLE,
			ROUTINES_FACADE_VARIABLE,
			accessibleElementsAccessExpressions,
			''''''
		)
	}
}
