package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import java.io.IOException
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.common.types.JvmFormalParameter
import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.*
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.*;
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.common.types.JvmConstructor
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.language.CreateCorrespondence
import java.util.List
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import tools.vitruv.dsls.reactions.language.RemoveCorrespondence
import java.util.ArrayList
import tools.vitruv.dsls.reactions.language.RoutineCallStatement
import tools.vitruv.dsls.reactions.language.MatcherCheckStatement
import tools.vitruv.dsls.reactions.language.toplevelelements.MatcherStatement
import tools.vitruv.dsls.reactions.runtime.AbstractRepairRoutineRealization
import tools.vitruv.dsls.reactions.language.CreateModelElement
import tools.vitruv.dsls.reactions.language.RetrieveModelElement
import tools.vitruv.dsls.reactions.language.DeleteModelElement
import tools.vitruv.dsls.reactions.language.UpdateModelElement
import tools.vitruv.dsls.reactions.language.toplevelelements.ActionStatement
import static extension tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators.*
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.language.ExecuteActionStatement
import tools.vitruv.dsls.reactions.language.RetrieveOneModelElement
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.RetrieveManyModelElements
import java.util.Optional
import tools.vitruv.dsls.common.ClassNameGenerator
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference
import tools.vitruv.dsls.common.elements.NamedMetaclassReference
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker.isReferenceable
import java.util.stream.Stream

class RoutineClassGenerator extends ClassGenerator {
	static val MISSING_NAME = "/* Name missing */"
	static val MISSING_TYPE = "/* Type missing */"
	protected final Routine routine;
	var String generalUserExecutionClassQualifiedName;
	final ClassNameGenerator routineClassNameGenerator;
	var ClassNameGenerator routinesFacadeClassNameGenerator;
	var Iterable<NamedMetaclassReference> modelInputElements;
	var Iterable<NamedJavaElementReference> javaInputElements;
	extension var UserExecutionClassGenerator userExecutionClassGenerator;
	var List<AccessibleElement> currentlyAccessibleElements;
	static val USER_EXECUTION_FIELD_NAME = "userExecution";
	int elementUpdateCounter;
	var JvmGenericType generatedClass
	var JvmGenericType userExecutionClass

	new(Routine routine, TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider)
		if (!routine.isReferenceable) {
			throw new IllegalArgumentException("incomplete");
		}
		this.routine = routine;
		this.routineClassNameGenerator = routine.routineClassNameGenerator;
		this.routinesFacadeClassNameGenerator = routine.reactionsSegment.routinesFacadeClassNameGenerator;
		this.generalUserExecutionClassQualifiedName = routineClassNameGenerator.qualifiedName + "." +
			EFFECT_USER_EXECUTION_SIMPLE_NAME;
		this.userExecutionClassGenerator = new UserExecutionClassGenerator(typesBuilderExtensionProvider, routine,
			routineClassNameGenerator.qualifiedName + "." + EFFECT_USER_EXECUTION_SIMPLE_NAME);
		this.modelInputElements = routine.input.modelInputElements
		this.javaInputElements = routine.input.javaInputElements
		this.elementUpdateCounter = 0;
		this.currentlyAccessibleElements = new ArrayList();
		this.currentlyAccessibleElements += routine.getInputElements(modelInputElements, javaInputElements);
	}

	private def Iterable<JvmFormalParameter> generateInputParameters(EObject contextObject) {
		return generateMethodInputParameters(contextObject, modelInputElements, javaInputElements);
	}

	protected def generateCurrentlyAccessibleElementsParameters(EObject sourceObject) {
		sourceObject.generateMethodInputParameters(currentlyAccessibleElements);
	}

	protected def generateMethodParameterCallList(JvmOperation method) {
		return method.parameters.generateMethodParameterCallList;
	}

	protected def generateMethodParameterCallList(Iterable<JvmFormalParameter> parameters) '''
	??FOR parameter : parameters.filterNull SEPARATOR ', '????parameter.name????ENDFOR??'''

	protected def getParameterCallList(String... parameterStrings) '''
	??FOR parameterName : parameterStrings SEPARATOR ', '????parameterName????ENDFOR??'''

	override JvmGenericType generateEmptyClass() {
		userExecutionClass = userExecutionClassGenerator.generateEmptyClass()
		generatedClass = routine.toClass(routineClassNameGenerator.qualifiedName) [
			visibility = JvmVisibility.PUBLIC
		]
	}

	override generateBody() {
		val executeMethod = generateMethodExecuteEffect()
		generatedClass => [
			documentation = getCommentWithoutMarkers(routine.documentation)
			superTypes += typeRef(AbstractRepairRoutineRealization)
			members += routine.toField(USER_EXECUTION_FIELD_NAME, typeRef(userExecutionClass))
			members += userExecutionClassGenerator.generateBody()
			members += routine.generateConstructor()
			members += generateInputFields()
			members += executeMethod
		]
	}

	private def String getUserExecutionMethodCallString(JvmOperation method) '''
	??USER_EXECUTION_FIELD_NAME??.??method.simpleName??(??method.generateMethodParameterCallList??)'''

	protected def Iterable<JvmMember> generateInputFields() {
		val inputParameters = routine.generateInputParameters()
		return inputParameters.map[toField(name, parameterType)]
	}

	protected def JvmConstructor generateConstructor(Routine routine) {
		return routine.toConstructor [
			visibility = JvmVisibility.PUBLIC;
			val routinesFacadeParameter = generateRoutinesFacadeParameter(routine.reactionsSegment);
			val executionStateParameter = generateReactionExecutionStateParameter();
			val calledByParameter = generateParameter("calledBy", typeRef(CallHierarchyHaving));
			val inputParameters = routine.generateInputParameters();
			parameters += routinesFacadeParameter;
			parameters += executionStateParameter;
			parameters += calledByParameter;
			parameters += inputParameters;
			body = '''
			super(??routinesFacadeParameter.name??, ??executionStateParameter.name??, ??calledByParameter.name??);
			this.??USER_EXECUTION_FIELD_NAME?? = new ??generalUserExecutionClassQualifiedName??(getExecutionState(), this);
			??FOR inputParameter : inputParameters??this.??inputParameter.name?? = ??inputParameter.name??;??ENDFOR??'''
		]
	}

	private def dispatch StringConcatenationClient createStatements(CreateModelElement createElement) {
		this.currentlyAccessibleElements += new AccessibleElement(createElement.name?: MISSING_NAME, createElement.elementType?.javaClassName)
		val initializeMethod = if (createElement.initializationBlock !== null)
				generateUpdateElementMethod(createElement.name ?: MISSING_NAME, createElement.initializationBlock,
					currentlyAccessibleElements);
		val initializeMethodCall = initializeMethod?.userExecutionMethodCallString;
		return '''
			??getElementCreationCode(createElement)??
			??initializeMethodCall??;
		'''
	}

	private def dispatch StringConcatenationClient createStatements(RequireAbscenceOfModelElement elementAbscence) {
		val retrieveStatementArguments = getGeneralGetCorrespondingElementStatementArguments(elementAbscence, null);
		val StringConcatenationClient statements = '''
			if (!getCorrespondingElements(
				??retrieveStatementArguments??
			).isEmpty()) {
				return false;
			}
		'''
		return statements;
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveModelElement retrieveElement) {
		val retrieveStatementArguments = getGeneralGetCorrespondingElementStatementArguments(retrieveElement,
			retrieveElement.name);
		val affectedElementClass = retrieveElement.elementType?.metaclass;
		return createStatements(retrieveElement.retrievalType, retrieveElement.name, affectedElementClass?.javaClassName,
			retrieveStatementArguments)
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveManyModelElements retrieveElement,
		String name, String typeName, StringConcatenationClient generalArguments) {
		val StringConcatenationClient statement = '''
			??IF !name.nullOrEmpty????List??<??typeName??> ??name?? = ??ENDIF??getCorrespondingElements(
				??generalArguments??
			);
		'''
		currentlyAccessibleElements += new AccessibleElement(name, List.name, typeName);
		return statement;
	}

	private def dispatch StringConcatenationClient createStatements(RetrieveOneModelElement retrieveElement,
		String name, String typeName, StringConcatenationClient generalArguments) {
		val retrieveStatement = '''
		getCorrespondingElement(
			??generalArguments??, 
			??retrieveElement.asserted?? // asserted
			)''';
		if (name.nullOrEmpty) {
			if (!retrieveElement.optional) {
				return '''if (??retrieveStatement?? == null) {
					return false;
				}'''
			}
		} else {
			val StringConcatenationClient statement = '''
				??IF retrieveElement.optional??
					??Optional??<??typeName??> ??name?? = ??Optional??.ofNullable(??retrieveStatement??
					);
				??ELSE??
					??typeName?? ??name?? = ??retrieveStatement??;
				??ENDIF??
				??IF !retrieveElement.optional??
					if (??name?? == null) {
						return false;
					}??ENDIF??
			'''
			if (retrieveElement.optional) {
				currentlyAccessibleElements += new AccessibleElement(name, Optional.name, typeName);
			} else {
				currentlyAccessibleElements += new AccessibleElement(name, typeName);
			}
			return statement;
		}
	}

	private def dispatch StringConcatenationClient createStatements(MatcherCheckStatement checkStatement) {
		val checkMethod = generateMethodMatcherPrecondition(checkStatement, currentlyAccessibleElements);
		val checkMethodCall = checkMethod?.userExecutionMethodCallString;
		return '''
		if (!??checkMethodCall??) {
			??IF checkStatement.asserted??
				throw new ??IllegalStateException??();
			??ELSE??
				return false;
			??ENDIF??
		}''';
	}

	private def dispatch StringConcatenationClient createStatements(DeleteModelElement deleteElement) {
		val getElementMethod = generateMethodGetElement(deleteElement.element, currentlyAccessibleElements, typeRef(EObject));
		val getElementMethodCall = getElementMethod?.userExecutionMethodCallString;
		return '''
			??Stream??.of(new ??Object??[] {??FOR accessibleElement : currentlyAccessibleElements SEPARATOR  ', '????accessibleElement.name????IF accessibleElement.optional??.orElse(null)??ENDIF????ENDFOR??})
				.filter(it -> it instanceof ??EObject??).map(it -> (EObject) it).forEach(accessibleElement ->
					removeCorrespondenceBetween(??getElementMethodCall??, accessibleElement, null));		
			deleteObject(??getElementMethodCall??);
		'''
	}

	private def dispatch StringConcatenationClient createStatements(UpdateModelElement updateElement) {
		val getElementMethod = generateMethodGetElement(updateElement.element, currentlyAccessibleElements, updateElement?.element.code?.inferredType);
		val getElementMethodCall = getElementMethod?.userExecutionMethodCallString;
		val updateMethod = generateUpdateElementMethod("" + elementUpdateCounter++, updateElement.updateBlock,
			currentlyAccessibleElements);
		val updateMethodCall = updateMethod?.userExecutionMethodCallString;
		return '''
			// val updatedElement ??getElementMethodCall??;
			??updateMethodCall??;
		'''
	}

	private def dispatch StringConcatenationClient createStatements(CreateCorrespondence createCorrespondence) {
		val getFirstElementMethod = generateMethodGetElement(createCorrespondence.firstElement,
			currentlyAccessibleElements, typeRef(EObject));
		val getFirstElementMethodCall = getFirstElementMethod?.userExecutionMethodCallString;
		val getSecondElementMethod = generateMethodGetElement(createCorrespondence.secondElement,
			currentlyAccessibleElements, typeRef(EObject));
		val getSecondElementMethodCall = getSecondElementMethod?.userExecutionMethodCallString;
		val tagMethod = if (createCorrespondence.tag !== null)
				generateMethodGetCreateTag(createCorrespondence, currentlyAccessibleElements);
		val tagMethodCall = if (tagMethod !== null) tagMethod.userExecutionMethodCallString else '''""''';
		return '''
			addCorrespondenceBetween(??getFirstElementMethodCall??, ??getSecondElementMethodCall??, ??tagMethodCall??);
		'''
	}

	private def dispatch StringConcatenationClient createStatements(RemoveCorrespondence removeCorrespondence) {
		val getFirstElementMethod = generateMethodGetElement(removeCorrespondence.firstElement,
			currentlyAccessibleElements, typeRef(EObject));
		val getFirstElementMethodCall = getFirstElementMethod?.userExecutionMethodCallString
		val getSecondElementMethod = generateMethodGetElement(removeCorrespondence.secondElement,
			currentlyAccessibleElements, typeRef(EObject));
		val getSecondElementMethodCall = getSecondElementMethod?.userExecutionMethodCallString;
		val tagMethod = if (removeCorrespondence.tag !== null)
				generateMethodGetCreateTag(removeCorrespondence, currentlyAccessibleElements);
		val tagMethodCall = if (tagMethod !== null) tagMethod.userExecutionMethodCallString else '''""''';
		return '''
			removeCorrespondenceBetween(??getFirstElementMethodCall??, ??getSecondElementMethodCall??, ??tagMethodCall??);
		'''
	}

	private def dispatch StringConcatenationClient createStatements(RoutineCallStatement routineCall) {
		val callRoutineMethod = generateMethodCallRoutine(routineCall, currentlyAccessibleElements,
			typeRef(routinesFacadeClassNameGenerator.qualifiedName));
		return generateExecutionMethodCall(callRoutineMethod);
	}

	private def dispatch StringConcatenationClient createStatements(ExecuteActionStatement executeAction) {
		val executeActionMethod = generateMethodExecuteAction(executeAction, currentlyAccessibleElements,
			typeRef(routinesFacadeClassNameGenerator.qualifiedName));
		return generateExecutionMethodCall(executeActionMethod);
	}

	private def StringConcatenationClient generateExecutionMethodCall(JvmOperation executionMethod) {
		val parameterCallList = executionMethod.generateCurrentlyAccessibleElementsParameters.
			generateMethodParameterCallList
		val StringConcatenationClient methodCall = '''??USER_EXECUTION_FIELD_NAME??.??executionMethod.simpleName??(??parameterCallList????IF !parameterCallList.toString.empty??, ??ENDIF??this.getRoutinesFacade());''';
		return methodCall;
	}

	protected def generateMethodExecuteEffect() {
		val methodName = "executeRoutine";
		val inputParameters = routine.generateInputParameters();
		val matcherStatements = if (routine.matcher !== null) routine.matcher.matcherStatements else #[];
		val effectStatements = routine.action.actionStatements;
		val matcherStatementsMap = <MatcherStatement, StringConcatenationClient>newHashMap();
		matcherStatements.mapFixed[matcherStatementsMap.put(it, createStatements(it))];
		val effectStatementsMap = <ActionStatement, StringConcatenationClient>newHashMap();
		effectStatements.mapFixed[effectStatementsMap.put(it, createStatements(it))];
		return generateUnassociatedMethod(methodName, typeRef(Boolean.TYPE)) [
			visibility = JvmVisibility.PROTECTED;
			exceptions += typeRef(IOException);
			body = '''
				if (getLogger().isTraceEnabled()) {
					getLogger().trace("Called routine ??routineClassNameGenerator.simpleName?? with input:");
					??FOR inputParameter : inputParameters??
						getLogger().trace("   ??inputParameter.name??: " + this.??inputParameter.name??);
					??ENDFOR??
				}
				
				??FOR matcherStatement : matcherStatements??
					??matcherStatementsMap.get(matcherStatement)??
				??ENDFOR??
				??FOR effectStatement : effectStatements??
					??effectStatementsMap.get(effectStatement)??
					
				??ENDFOR??
				return true;
			'''
		];
	}

	private def StringConcatenationClient getTagString(RetrieveOrRequireAbscenceOfModelElement retrieveElement) {
		if (retrieveElement.tag !== null) {
			val tagMethod = generateMethodGetRetrieveTag(retrieveElement, currentlyAccessibleElements);
			return '''??tagMethod.userExecutionMethodCallString??'''
		} else {
			return '''null'''
		}
	}

	private def StringConcatenationClient getPreconditionChecker(
		RetrieveOrRequireAbscenceOfModelElement retrieveElement, String name) {
		val affectedElementClass = retrieveElement.elementType?.javaClassName;
		if (retrieveElement.precondition === null) {
			return '''(??affectedElementClass?? _element) -> true''';
		}
		val preconditionMethod = generateMethodCorrespondencePrecondition(retrieveElement, name,
			currentlyAccessibleElements);
		return '''(??affectedElementClass?? _element) -> ??USER_EXECUTION_FIELD_NAME??.??preconditionMethod.simpleName??(??preconditionMethod.generateMethodParameterCallList.toString.replace(name?: RETRIEVAL_PRECONDITION_METHOD_TARGET, "_element")??)'''
	}

	private def StringConcatenationClient getGeneralGetCorrespondingElementStatementArguments(
		RetrieveOrRequireAbscenceOfModelElement retrieveElement, String name) {
		val affectedElementClass = retrieveElement.elementType?.javaClassName;
		val correspondingElementPreconditionChecker = getPreconditionChecker(retrieveElement, name);
		val correspondenceSourceMethod = generateMethodGetCorrespondenceSource(retrieveElement,
			currentlyAccessibleElements);
		val correspondenceSourceMethodCall = correspondenceSourceMethod?.userExecutionMethodCallString;
		val tagString = getTagString(retrieveElement);
		return '''
		??correspondenceSourceMethodCall??, // correspondence source supplier
		??affectedElementClass ?: MISSING_TYPE??.class,
		??correspondingElementPreconditionChecker??, // correspondence precondition checker
		??tagString??'''
	}

	private def StringConcatenationClient getElementCreationCode(CreateModelElement elementCreate) {
		val affectedElementClass = elementCreate.elementType?.metaclass;
		val createdClassFactory = affectedElementClass?.EPackage?.EFactoryInstance?.runtimeClassName
		return '''
		??affectedElementClass.javaClassName?? ??elementCreate.name ?: MISSING_NAME?? = ??createdClassFactory?: MISSING_TYPE??.eINSTANCE.create??affectedElementClass?.name?: MISSING_TYPE??();
		notifyObjectCreated(??elementCreate.name ?: MISSING_NAME??);'''
	}

}
