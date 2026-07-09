package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*
import static extension tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentationExtractor.*
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentation
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.StepExecutionClassGenerator
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.UpdateBlockClassGenerator
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.EmptyStepExecutionClassGenerator
import org.eclipse.xtend2.lib.StringConcatenationClient
import static com.google.common.base.Preconditions.checkArgument
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import java.util.function.Function
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.change.atomic.EChange
import tools.vitruv.change.propagation.ChangePropagationSpecification

class ReactionClassGenerator extends ClassGenerator {
	static val EXECUTION_STATE_VARIABLE = "executionState"
	static val ROUTINES_FACADE_VARIABLE = "routinesFacade"

	static val EXECUTE_REACTION_METHOD_NAME = "executeReaction"
	static val MATCH_CHANGE_METHOD_NAME = "isCurrentChangeMatchingTrigger"
	static val USER_DEFINED_PRECONDITION_METHOD_NAME = "isUserDefinedPreconditionFulfilled"

	final Reaction reaction
	final ChangeTypeRepresentation changeType
	final String reactionClassQualifiedName
	final StepExecutionClassGenerator routineCallClassGenerator
	var JvmGenericType generatedClass

	new(Reaction reaction, TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider)
		checkArgument(reaction !== null, "reaction must not be null")
		checkArgument(!reaction.name.nullOrEmpty, "reaction must have a name")
		checkArgument(reaction.trigger !== null, "reaction must have a defined trigger")
		this.reaction = reaction
		this.changeType = reaction.trigger.extractChangeType
		this.reactionClassQualifiedName = reaction.reactionClassNameGenerator.qualifiedName
		this.routineCallClassGenerator = if (reaction.callRoutine !== null) {
			val routinesFacadeClassName = reaction.reactionsSegment.routinesFacadeClassNameGenerator.qualifiedName
			new UpdateBlockClassGenerator(typesBuilderExtensionProvider, reactionClassQualifiedName + ".Call",
				reaction.callRoutine, typeRef(routinesFacadeClassName), changeType.generatePropertiesParameterList)
		} else {
			new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider)
		}
	}

	override JvmGenericType generateEmptyClass() {
		routineCallClassGenerator.generateEmptyClass()
		generatedClass = reaction.toClass(reactionClassQualifiedName) [
			visibility = JvmVisibility.PUBLIC
		]
	}

	override generateBody() {
		generatedClass => [
			documentation = getCommentWithoutMarkers(reaction.documentation)
			superTypes += typeRef(AbstractReaction)
			members +=
				reaction.toField(changeType.name, changeType.accessibleElement.generateTypeRef(_typeReferenceBuilder))
			members += reaction.generateConstructor()
			members += routineCallClassGenerator.generateBody()
			members += generateMethodExecuteReactionAndDependentMethods()
		]
	}

	private def JvmConstructor generateConstructor(Reaction reaction) {
		return reaction.toConstructor [
			visibility = JvmVisibility.PUBLIC
			val routinesFacadeParameter = generateParameter(new AccessibleElement(ROUTINES_FACADE_VARIABLE + "Generator", Function.name, ReactionExecutionState.name, RoutinesFacade.name))
			parameters += routinesFacadeParameter
			body = '''
				super(«routinesFacadeParameter.name»);
			'''
		]
	}

	private def Iterable<JvmOperation> generateMethodExecuteReactionAndDependentMethods() {
		val matchChangeMethod = generateMatchChangeMethod
		val userDefinedPreconditionMethod = generateUserDefinedPreconditionMethod
		val executeReactionMethod = reaction.toMethod(EXECUTE_REACTION_METHOD_NAME, typeRef(Void.TYPE)) [
			visibility = JvmVisibility.PUBLIC
			val changeParameter = generateParameter(new AccessibleElement("change", EChange))
			val reactionExecutionStateParameter = generateParameter(new AccessibleElement(EXECUTION_STATE_VARIABLE, ReactionExecutionState))
			val routinesFacadeParameter = generateParameter(new AccessibleElement(ROUTINES_FACADE_VARIABLE + "Untyped", RoutinesFacade))
			val specificationLocal = new AccessibleElement("specification", ChangePropagationSpecification)
			parameters += changeParameter
			parameters += reactionExecutionStateParameter
			parameters += routinesFacadeParameter

			val facadeClassName = reaction.reactionsSegment.routinesFacadeClassNameGenerator.qualifiedName
			body = '''
				«facadeClassName» «ROUTINES_FACADE_VARIABLE» = («facadeClassName»)«ROUTINES_FACADE_VARIABLE»Untyped;
				«generateMatchChangeMethodCallCode(matchChangeMethod, changeParameter.name)»
				«changeType.generatePropertiesAssignmentCode»
				«generateUserDefinedPreconditionMethodCall(userDefinedPreconditionMethod)»
			  «ChangePropagationSpecification» «specificationLocal.name» = («specificationLocal») «reactionExecutionStateParameter.name».getChangePropagationObservable();
				«specificationLocal.name».notifyChangePropagationStarted(«specificationLocal.name», «changeParameter.name»);
				if (getLogger().isTraceEnabled()) {
					getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
				}
				
				«generateCallRoutineCode»
				«specificationLocal.name».notifyChangePropagationStopped(«specificationLocal.name», «changeParameter.name»);
			'''
		]
		return #[matchChangeMethod, userDefinedPreconditionMethod, executeReactionMethod].filterNull
	}

	private def generateCallRoutineCode() {
		return routineCallClassGenerator.generateStepExecutionCode(
			'''''',
			EXECUTION_STATE_VARIABLE,
			ROUTINES_FACADE_VARIABLE,
			changeType.generatePropertiesParameterList().map[name],
			''''''
		)
	}

	private def StringConcatenationClient generateMatchChangeMethodCallCode(JvmOperation matchChangeMethod,
		String changeParameterName) {
		return '''
			if (!«matchChangeMethod.simpleName»(«changeParameterName»)) {
				return;
			}
		'''
	}

	private def JvmOperation generateMatchChangeMethod() {
		return reaction.trigger.toMethod(MATCH_CHANGE_METHOD_NAME, typeRef(Boolean.TYPE)) [
			val changeParameter = generateParameter(new AccessibleElement("change", EChange))
			visibility = JvmVisibility.PUBLIC
			parameters += changeParameter
			body = changeType.generateCheckMethodBody(changeParameter.name)
		]
	}

	private def StringConcatenationClient generateUserDefinedPreconditionMethodCall(
		JvmOperation userDefinedPreconditionMethod) {
		return if (hasUserDefinedPrecondition) {
			'''
				if (getLogger().isTraceEnabled()) {
					getLogger().trace("Passed change matching of Reaction " + this.getClass().getName());
				}
				if (!«userDefinedPreconditionMethod.simpleName»(«FOR argument : changeType.generatePropertiesParameterList.map[name] SEPARATOR ", "»«argument»«ENDFOR»)) {
					return;
				}
			'''
		} else {
			''''''
		}
	}

	private def JvmOperation generateUserDefinedPreconditionMethod() {
		if (!hasUserDefinedPrecondition) {
			return null
		}
		val precondition = reaction.trigger.precondition
		val methodName = USER_DEFINED_PRECONDITION_METHOD_NAME
		return reaction.trigger.toMethod(methodName, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PRIVATE
			parameters += generateAccessibleElementsParameters(changeType.generatePropertiesParameterList)
			body = precondition
		]
	}

	private def boolean hasUserDefinedPrecondition() {
		return reaction.trigger?.precondition !== null
	}

}
