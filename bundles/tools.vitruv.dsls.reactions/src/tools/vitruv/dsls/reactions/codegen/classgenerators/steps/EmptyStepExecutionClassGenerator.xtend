package tools.vitruv.dsls.reactions.codegen.classgenerators.steps

import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import org.eclipse.xtend2.lib.StringConcatenationClient

class EmptyStepExecutionClassGenerator extends StepExecutionClassGenerator {

	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider)
	}

	override isEmpty() {
		return true
	}

	override generateStepExecutionCode(
		StringConcatenationClient prefix,
		String executionStateAccessExpression,
		String routinesFacadeAccessExpression,
		Iterable<String> accessibleElementsAccessExpressions,
		StringConcatenationClient suffix
	) {
		return '''// This execution step is empty'''
	}

	override getNewlyAccessibleElementsAfterExecution() {
		return emptyList
	}

	override getNewlyAccessibleElementsContainerType() {
		return null
	}

	override generateEmptyClass() {
		return null
	}

	override generateBody() {
		return null
	}

}
