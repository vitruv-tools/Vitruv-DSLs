package tools.vitruv.dsls.reactions.codegen.classgenerators.routine

import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import tools.vitruv.dsls.reactions.language.Taggable
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.RetrieveModelElement
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.RETRIEVAL_PRECONDITION_METHOD_TARGET
import org.eclipse.xtend2.lib.StringConcatenationClient
import tools.vitruv.dsls.reactions.language.RetrieveManyModelElements
import tools.vitruv.dsls.reactions.language.RetrieveOneModelElement
import java.util.Optional
import java.util.List
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement
import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.mapFixed
import java.io.IOException
import org.eclipse.xtext.common.types.JvmFormalParameter
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.*
import tools.vitruv.dsls.reactions.runtime.AbstractRepairRoutineRealization
import static com.google.common.base.Preconditions.checkNotNull
import static com.google.common.base.Preconditions.checkArgument
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock
import tools.vitruv.dsls.reactions.language.MatchCheckStatement

/**
 * Generates for a {@link Matcher} block of a routine a class providing a method (with the name defined in 
 * {@link #MATCH_METHOD_NAME}) that accepts the inputs values of the routine as parameters and performs the 
 * defined match statements.
 */
class MatchBlockClassGenerator extends StepExecutionClassGenerator {
	static val MATCH_METHOD_NAME = "match"
	static val MISSING_TYPE = "/* Type missing */"
	static val RETRIEVED_ELEMENTS_SIMPLE_CLASS_NAME = "RetrievedValues"

	val String qualifiedClassName
	val MatchBlock matchBlock
	val Iterable<AccessibleElement> inputElements

	val JvmGenericType retrievedElementsClass
	var JvmGenericType generatedClass
	var counterGetRetrieveTagMethods = 1
	var counterCheckMatcherPreconditionMethods = 1
	var counterGetCorrespondenceSource = 1

	/**
	 * Creates a class generator for the given routine matcher. The generated class has
	 * the given qualified name and accepts the given input elements as parameters in the
	 * provided {@code match} method.
	 * 
	 * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
	 * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
	 * @param matcher the matcher to create a class for, must not be {@code null}
	 * @param inputElements the elements to be passed to the generated {@code match} method, must not be {@code null}
	 */
	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider, String qualifiedClassName, MatchBlock matchBlock,
		Iterable<AccessibleElement> inputElements) {
		super(typesBuilderExtensionProvider)
		checkArgument(!qualifiedClassName.nullOrEmpty, "class name must not be null or empty")
		this.qualifiedClassName = qualifiedClassName
		this.matchBlock = checkNotNull(matchBlock, "match block must not be null")
		this.inputElements = checkNotNull(inputElements, "input elements must not be null")
		this.retrievedElementsClass = generateNewlyAccessibleElementsContainerClass(retrievedValuesClassQualifiedName)
	}

	private def getRetrievedValuesClassQualifiedName() {
		qualifiedClassName + "." + RETRIEVED_ELEMENTS_SIMPLE_CLASS_NAME
	}

	override generateEmptyClass() {
		generatedClass = matchBlock.toClass(qualifiedClassName) [
			visibility = JvmVisibility.PRIVATE
			static = true
		]
		return generatedClass
	}

	override generateBody() {
		generatedClass => [
			superTypes += typeRef(AbstractRepairRoutineRealization.Match)
			members += retrievedElementsClass
			members += generateConstructor()
			members += generateMatchMethod()
			members += generatedMethods
		]
	}

	private def generateConstructor() {
		matchBlock.toConstructor [
			val executionStateParameter = generateReactionExecutionStateParameter()
			parameters += executionStateParameter
			body = '''super(«executionStateParameter.name»);'''
		]
	}

	private def generateMatchMethod() {
		val List<AccessibleElement> currentlyAccessibleElements = newArrayList(inputElements)
		val matcherStatements = matchBlock.matchStatements.mapFixed [
			createStatements(it, currentlyAccessibleElements)
		]
		return matchBlock.toMethod(MATCH_METHOD_NAME, typeRef(newlyAccessibleElementsContainerType)) [
			visibility = JvmVisibility.PUBLIC
			exceptions += typeRef(IOException)
			parameters += generateAccessibleElementsParameters(inputElements)
			body = '''
				«FOR matcherStatement : matcherStatements»
					«matcherStatement»
				«ENDFOR»
				return new «retrievedValuesClassQualifiedName»(« //
				FOR retrievedElement : newlyAccessibleElementsAfterExecution SEPARATOR ", "»«retrievedElement.name»«ENDFOR»);
			'''
		]
	}

	private def dispatch StringConcatenationClient createStatements(RequireAbscenceOfModelElement elementAbscence,
		List<AccessibleElement> currentlyAccessibleElements) {
		val retrieveStatementArguments = getGeneralGetCorrespondingElementStatementArguments(elementAbscence, null,
			currentlyAccessibleElements)
		val StringConcatenationClient statements = '''
			if (!getCorrespondingElements(
				«retrieveStatementArguments»
			).isEmpty()) {
				return null;
			}
		'''
		return statements
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveModelElement retrieveElement,
		List<AccessibleElement> currentlyAccessibleElements) {
		val retrieveStatementArguments = getGeneralGetCorrespondingElementStatementArguments(retrieveElement,
			retrieveElement.name, currentlyAccessibleElements)
		val affectedElementClass = retrieveElement.elementType?.metaclass
		val createdStatements = createStatements(retrieveElement.retrievalType, retrieveElement.name,
			affectedElementClass?.javaClassName, retrieveStatementArguments)
		currentlyAccessibleElements += retrieveElement.accessibleElement
		return createdStatements
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveManyModelElements retrieveElement,
		String name, String typeName, StringConcatenationClient generalArguments) {
		val StringConcatenationClient statement = '''
			«IF !name.nullOrEmpty»«List»<«typeName»> «name» = «ENDIF»getCorrespondingElements(
				«generalArguments»
			);
		'''
		return statement
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveOneModelElement retrieveElement,
		String name, String typeName, StringConcatenationClient generalArguments) {
		val retrieveStatement = '''
		getCorrespondingElement(
			«generalArguments», 
			«retrieveElement.asserted» // asserted
			)''';
		if (name.nullOrEmpty) {
			if (!retrieveElement.optional) {
				return '''if («retrieveStatement» == null) {
					return null;
				}'''
			}
		} else {
			return '''
				«IF retrieveElement.optional»
					«Optional»<«typeName»> «name» = «Optional».ofNullable(«retrieveStatement»
					);
				«ELSE»
					«typeName» «name» = «retrieveStatement»;
				«ENDIF»
				«IF !retrieveElement.optional»
					if («name» == null) {
						return null;
					}«ENDIF»
			'''
		}
	}

	private def dispatch StringConcatenationClient createStatements(MatchCheckStatement checkStatement,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val checkMethod = generateMethodMatcherPrecondition(checkStatement, currentlyAccessibleElements)
		val checkMethodCall = checkMethod?.userExecutionMethodCallString
		return '''
		if (!«checkMethodCall») {
			«IF checkStatement.asserted»
				throw new «IllegalStateException»();
			«ELSE»
				return null;
			«ENDIF»
		}''';
	}

	private def StringConcatenationClient getTagString(RetrieveOrRequireAbscenceOfModelElement retrieveElement,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		if (retrieveElement.tag !== null) {
			val tagMethod = generateMethodGetRetrieveTag(retrieveElement, currentlyAccessibleElements)
			return '''«tagMethod.userExecutionMethodCallString»'''
		} else {
			return '''null'''
		}
	}

	private def StringConcatenationClient getPreconditionChecker(
		RetrieveOrRequireAbscenceOfModelElement retrieveElement, String name,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val affectedElementClass = retrieveElement.elementType?.javaClassName
		if (retrieveElement.precondition === null) {
			return '''(«affectedElementClass» _element) -> true'''
		}
		val preconditionMethod = generateMethodCorrespondencePrecondition(retrieveElement, name,
			currentlyAccessibleElements)
		return '''(«affectedElementClass» _element) -> «preconditionMethod.simpleName»(« //
		preconditionMethod.generateMethodParameterCallList.toString.replace(name?: 
			RETRIEVAL_PRECONDITION_METHOD_TARGET, "_element"
		)»)'''
	}

	private def StringConcatenationClient getGeneralGetCorrespondingElementStatementArguments(
		RetrieveOrRequireAbscenceOfModelElement retrieveElement, String name,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val affectedElementClass = retrieveElement.elementType?.javaClassName
		val correspondingElementPreconditionChecker = getPreconditionChecker(retrieveElement, name,
			currentlyAccessibleElements)
		val correspondenceSourceMethod = generateMethodGetCorrespondenceSource(retrieveElement,
			currentlyAccessibleElements)
		val correspondenceSourceMethodCall = correspondenceSourceMethod?.userExecutionMethodCallString
		val tagString = getTagString(retrieveElement, currentlyAccessibleElements)
		return '''
		«correspondenceSourceMethodCall», // correspondence source supplier
		«affectedElementClass ?: MISSING_TYPE».class,
		«correspondingElementPreconditionChecker», // correspondence precondition checker
		«tagString»'''
	}

	private def JvmOperation generateMethodGetRetrieveTag(Taggable taggable,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val methodName = "getRetrieveTag" + counterGetRetrieveTagMethods++;

		return taggable.tag?.getOrGenerateMethod(methodName, typeRef(String)) [
			parameters += generateAccessibleElementsParameters(currentlyAccessibleElements)
			body = taggable.tag.code;
		];
	}

	private def generateMethodCorrespondencePrecondition(RetrieveOrRequireAbscenceOfModelElement elementRetrieve,
		String name, Iterable<AccessibleElement> currentlyAccessibleElements) {
		val methodName = "getCorrespondingModelElementsPrecondition" +
			(elementRetrieve.retrieveOrRequireAbscenceMethodSuffix ?: counterGetCorrespondenceSource++)
		return elementRetrieve.precondition?.getOrGenerateMethod(methodName, typeRef(Boolean.TYPE)) [
			val elementParameter = generateModelElementParameter(elementRetrieve.elementType,
				name ?: RETRIEVAL_PRECONDITION_METHOD_TARGET)
			parameters += generateAccessibleElementsParameters(currentlyAccessibleElements)
			parameters += elementParameter
			body = elementRetrieve.precondition.code
		];
	}

	private def generateMethodGetCorrespondenceSource(RetrieveOrRequireAbscenceOfModelElement elementRetrieve,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val methodName = "getCorrepondenceSource" +
			(elementRetrieve.retrieveOrRequireAbscenceMethodSuffix ?: counterGetCorrespondenceSource++);

		val correspondenceSourceBlock = elementRetrieve.correspondenceSource;
		return correspondenceSourceBlock?.getOrGenerateMethod(methodName, typeRef(EObject)) [
			parameters += generateAccessibleElementsParameters(currentlyAccessibleElements)
			body = correspondenceSourceBlock.code;
		];
	}

	private def String getUserExecutionMethodCallString(JvmOperation method) '''
	«method.simpleName»(«method.generateMethodParameterCallList»)'''

	private def JvmOperation generateMethodMatcherPrecondition(MatchCheckStatement checkStatement,
		Iterable<AccessibleElement> currentlyAccessibleElements) {
		val methodName = "checkMatcherPrecondition" + counterCheckMatcherPreconditionMethods++;
		return checkStatement.getOrGenerateMethod(methodName, typeRef(Boolean.TYPE)) [
			parameters += generateAccessibleElementsParameters(currentlyAccessibleElements)
			body = checkStatement.code;
		];
	}

	private def dispatch getRetrieveOrRequireAbscenceMethodSuffix(RetrieveOrRequireAbscenceOfModelElement statement) {
		null
	}

	private def dispatch getRetrieveOrRequireAbscenceMethodSuffix(RetrieveModelElement statement) {
		if (statement.name.nullOrEmpty) {
			return null
		} else {
			return statement.name.toFirstUpper
		}
	}

	private def generateMethodParameterCallList(JvmOperation method) {
		return method.parameters.generateMethodParameterCallList;
	}

	private def generateMethodParameterCallList(Iterable<JvmFormalParameter> parameters) '''
	«FOR parameter : parameters.filterNull SEPARATOR ', '»«parameter.name»«ENDFOR»'''

	override generateStepExecutionCode(
		StringConcatenationClient prefix,
		String executionStateAccessExpression,
		String routinesFacadeAccessExpression,
		Iterable<String> accessibleElementsAccessExpressions,
		StringConcatenationClient suffix
	) '''
		«prefix»new «qualifiedClassName»(«executionStateAccessExpression»).«MATCH_METHOD_NAME»(« //
		FOR accessibleElement : accessibleElementsAccessExpressions SEPARATOR ", "»«accessibleElement»«ENDFOR»);
		«suffix»
	'''

	override Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
		return matchBlock.matchStatements.filter(RetrieveModelElement).filter[!name.nullOrEmpty].mapFixed[accessibleElement]
	}

	private def getAccessibleElement(RetrieveModelElement retrieveElement) {
		val retrievalType = retrieveElement.retrievalType
		val retrieveElementType = retrieveElement.elementType?.metaclass?.javaClassName
		switch (retrievalType) {
			RetrieveOneModelElement:
				if (retrievalType.optional) {
					return new AccessibleElement(retrieveElement.name, Optional.name, retrieveElementType)
				} else {
					return new AccessibleElement(retrieveElement.name, retrieveElementType)
				}
			RetrieveManyModelElements:
				return new AccessibleElement(retrieveElement.name, List.name, retrieveElementType)
		}

	}

	override getNewlyAccessibleElementsContainerType() {
		return retrievedElementsClass
	}

}
