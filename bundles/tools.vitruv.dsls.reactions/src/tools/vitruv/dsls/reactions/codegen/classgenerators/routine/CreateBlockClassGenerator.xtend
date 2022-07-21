package tools.vitruv.dsls.reactions.codegen.classgenerators.routine

import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtend2.lib.StringConcatenationClient
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.getRuntimeClassName
import static extension tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.getJavaClassName
import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.mapFixed
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock
import tools.vitruv.dsls.common.elements.NamedMetaclassReference
import tools.vitruv.dsls.reactions.runtime.AbstractRoutine

/**
 * Generates for a {@link CreateBlock} of a routine a class providing a creation method (with the name defined in {@link #CREATE_ELEMENTS_METHOD_NAME})
 * that returns the created elements.
 */
class CreateBlockClassGenerator extends StepExecutionClassGenerator {
	static val MISSING_NAME = "/* Name missing */"
	static val MISSING_TYPE = "/* Type missing */"
	static val CREATED_ELEMENTS_SIMPLE_CLASS_NAME = "CreatedValues"
	static val CREATE_ELEMENTS_METHOD_NAME = "createElements"

	val String qualifiedClassName
	val CreateBlock createBlock

	val JvmGenericType createdElementsClass
	var JvmGenericType generatedClass

	/**
	 * Create a class generator for the given routine creator block. The generated class has
	 * the given qualified name and provides the {@code createElements} method to create and return
	 * the defined elements.
	 * 
	 * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
	 * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
	 * @param createBlock the code block with create statements to create a class for, must not be {@code null}
	 */
	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider, String qualifiedClassName,
		CreateBlock createBlock) {
		super(typesBuilderExtensionProvider)
		checkArgument(!qualifiedClassName.nullOrEmpty, "class name must not be null or empty")
		this.qualifiedClassName = qualifiedClassName
		this.createBlock = checkNotNull(createBlock, "create block must not be null")
		this.createdElementsClass = generateNewlyAccessibleElementsContainerClass(createdValuesClassQualifiedName)
	}

	private def getCreatedValuesClassQualifiedName() {
		qualifiedClassName + "." + CREATED_ELEMENTS_SIMPLE_CLASS_NAME
	}

	override Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
		return createBlock.createStatements.mapFixed [
			new AccessibleElement(name ?: MISSING_NAME, metaclass?.javaClassName)
		]
	}

	override getNewlyAccessibleElementsContainerType() {
		return createdElementsClass
	}

	override generateEmptyClass() {
		this.generatedClass = createBlock.toClass(qualifiedClassName) [
			visibility = JvmVisibility.PRIVATE
			static = true
		]
		return generatedClass
	}

	override generateBody() {
		generatedClass => [
			superTypes += typeRef(AbstractRoutine.Create)
			members += createdElementsClass
			members += generateConstructor()
			members += generateMethodCreate()
		]
	}

	def private generateConstructor() {
		createBlock.toConstructor [
			val reactionExecutionStateParameter = generateReactionExecutionStateParameter()
			parameters += reactionExecutionStateParameter
			body = '''super(«reactionExecutionStateParameter.name»);'''
		]
	}

	private def JvmOperation generateMethodCreate() {
		createBlock.toMethod(CREATE_ELEMENTS_METHOD_NAME, typeRef(newlyAccessibleElementsContainerType)) [
			body = '''
				«FOR createStatement : createBlock.createStatements»
					«createStatement.elementCreationCode»
				«ENDFOR»
				return new «newlyAccessibleElementsContainerType»(« //
				FOR createdElement : newlyAccessibleElementsAfterExecution SEPARATOR ", "»«createdElement.name»«ENDFOR»);
			'''
		]
	}

	private def StringConcatenationClient getElementCreationCode(NamedMetaclassReference elementCreate) {
		val affectedElementClass = elementCreate.metaclass
		val createdClassFactory = affectedElementClass?.EPackage?.EFactoryInstance?.runtimeClassName
		return '''
		«affectedElementClass.javaClassName» «elementCreate.name ?: MISSING_NAME» = «createdClassFactory?: 
			MISSING_TYPE».eINSTANCE.create«affectedElementClass?.name?: MISSING_TYPE»();
		notifyObjectCreated(«elementCreate.name ?: MISSING_NAME»);'''
	}

	override generateStepExecutionCode(
		StringConcatenationClient prefix,
		String executionStateAccessExpression,
		String routinesFacadeAccessExpression,
		Iterable<String> accessibleElementsAccessExpressions,
		StringConcatenationClient suffix
	) '''
		«prefix»new «qualifiedClassName»(«executionStateAccessExpression»).«CREATE_ELEMENTS_METHOD_NAME»();
		«suffix»
	'''

}
