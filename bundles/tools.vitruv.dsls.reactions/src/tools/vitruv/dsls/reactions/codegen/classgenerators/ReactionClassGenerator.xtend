package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import tools.vitruv.dsls.reactions.language.toplevelelements.PreconditionCodeBlock
import tools.vitruv.dsls.reactions.runtime.AbstractReactionRealization
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*
import static extension tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentationExtractor.*
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.common.ClassNameGenerator
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker.isReferenceable
import tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentation

class ReactionClassGenerator extends ClassGenerator {
	static val MATCH_CHANGE_METHOD_NAME = "isCurrentChangeMatchingTrigger"
	static val USER_DEFINED_PRECONDITION_METHOD_NAME = "isUserDefinedPreconditionFulfilled"
	
	final Reaction reaction
	final boolean hasPreconditionBlock
	final ChangeTypeRepresentation changeType
	final ClassNameGenerator reactionClassNameGenerator
	final UserExecutionClassGenerator userExecutionClassGenerator
	final ClassNameGenerator routinesFacadeClassNameGenerator
	var JvmGenericType generatedClass
	
	new(Reaction reaction, TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider);
		if (!reaction.isReferenceable) {
			throw new IllegalArgumentException("incomplete");
		}
		this.reaction = reaction;
		this.hasPreconditionBlock = reaction.trigger?.precondition !== null;
		this.changeType = reaction.trigger?.extractChangeType;
		this.reactionClassNameGenerator = reaction.reactionClassNameGenerator;
		this.routinesFacadeClassNameGenerator = reaction.reactionsSegment.routinesFacadeClassNameGenerator;
		this.userExecutionClassGenerator = new UserExecutionClassGenerator(typesBuilderExtensionProvider, reaction, 
			reactionClassNameGenerator.qualifiedName + "." + EFFECT_USER_EXECUTION_SIMPLE_NAME);
	}
		
	override JvmGenericType generateEmptyClass() {
		userExecutionClassGenerator.generateEmptyClass()
		generatedClass = reaction.toClass(reactionClassNameGenerator.qualifiedName) [
			visibility = JvmVisibility.PUBLIC
		]
	}
	
	override generateBody() {
		if (changeType !== null) generateMethodExecuteReaction()
		
		generatedClass => [
			documentation = getCommentWithoutMarkers(reaction.documentation)
			superTypes += typeRef(AbstractReactionRealization)
			members += reaction.toField(changeType.name, changeType.accessibleElement.generateTypeRef(_typeReferenceBuilder))
			members += reaction.generateConstructor();
			members += generatedMethods
			members += userExecutionClassGenerator.generateBody()
		]
	}
	
	private def JvmConstructor generateConstructor(Reaction reaction) {
		return reaction.toConstructor [
			visibility = JvmVisibility.PUBLIC;
			val routinesFacadeParameter = generateRoutinesFacadeParameter(reaction.reactionsSegment);
			parameters += routinesFacadeParameter;
			body = '''
			super(«routinesFacadeParameter.name»);'''
		]
	}
	
	private def generateMethodExecuteReaction() {
		val methodName = "executeReaction";
		val matchChangesMethod = generateMatchChangeMethod
		val accessibleElementList = changeType.generatePropertiesParameterList();
		val callRoutineMethod = userExecutionClassGenerator.generateMethodCallRoutine(reaction.callRoutine, 
			accessibleElementList, typeRef(routinesFacadeClassNameGenerator.qualifiedName));
		val userDefinedPreconditionMethod = if (hasPreconditionBlock) {
			generateMethodCheckUserDefinedPrecondition(reaction.trigger.precondition);	
		}
		return getOrGenerateMethod(methodName, typeRef(Void.TYPE)) [
			visibility = JvmVisibility.PUBLIC;
			val changeParameter = generateUntypedChangeParameter();
			parameters += changeParameter;
			body = '''
				if (!«matchChangesMethod.simpleName»(«changeParameter.name»)) {
					return;
				}
				«changeType.generatePropertiesAssignmentCode()»
								
				«IF hasPreconditionBlock»
					if (getLogger().isTraceEnabled()) {
						getLogger().trace("Passed change matching of Reaction " + this.getClass().getName());
					}
					if (!«userDefinedPreconditionMethod.simpleName»(«
						FOR argument : accessibleElementList.generateArgumentsForAccesibleElements SEPARATOR ", "»«argument»«ENDFOR»)) {
						return;
					}
				«ENDIF»
				if (getLogger().isTraceEnabled()) {
					getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
				}
								
				«userExecutionClassGenerator.qualifiedClassName» userExecution = new «userExecutionClassGenerator.qualifiedClassName»(this.executionState, this);
				userExecution.«callRoutineMethod.simpleName»(«
					FOR argument : accessibleElementList.generateArgumentsForAccesibleElements SEPARATOR ", " AFTER ", "»«argument»«ENDFOR»this.getRoutinesFacade());
			'''
		];
	}
	
	private def Iterable<String> generateArgumentsForAccesibleElements(Iterable<AccessibleElement> elements) {
		elements.map[name];
	}
	
	private def generateMatchChangeMethod() {
		return getOrGenerateMethod(tools.vitruv.dsls.reactions.codegen.classgenerators.ReactionClassGenerator.MATCH_CHANGE_METHOD_NAME, typeRef(Boolean.TYPE)) [
			val changeParameter = generateUntypedChangeParameter(reaction);
			visibility = JvmVisibility.PUBLIC;
			parameters += changeParameter
			body = changeType.generateCheckMethodBody(changeParameter.name)
		];
	}

	private def JvmOperation generateMethodCheckUserDefinedPrecondition(PreconditionCodeBlock preconditionBlock) {
		val methodName = tools.vitruv.dsls.reactions.codegen.classgenerators.ReactionClassGenerator.USER_DEFINED_PRECONDITION_METHOD_NAME;
		return preconditionBlock.getOrGenerateMethod(methodName, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PRIVATE;
			parameters += generateAccessibleElementsParameters(changeType.generatePropertiesParameterList());
			body = preconditionBlock.code;
		];		
	}
	
}