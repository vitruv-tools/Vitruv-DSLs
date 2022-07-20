package tools.vitruv.dsls.reactions.codegen.classgenerators

import tools.vitruv.dsls.reactions.codegen.classgenerators.ClassGenerator
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmVisibility
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import org.eclipse.xtext.common.types.JvmOperation
import tools.vitruv.dsls.reactions.language.toplevelelements.CodeBlock
import org.eclipse.xtext.common.types.JvmTypeReference
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCallBlock
import tools.vitruv.dsls.reactions.runtime.AbstractRepairRoutineRealization
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import org.eclipse.xtext.common.types.JvmGenericType
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*

class UserExecutionClassGenerator extends ClassGenerator {
	val EObject objectMappedToClass;
	val String qualifiedClassName;
	var counterCallRoutineMethods = 1
	var JvmGenericType generatedClass
	
	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider, EObject objectMappedToClass,
		String qualifiedClassName) {
		super(typesBuilderExtensionProvider)
		this.objectMappedToClass = objectMappedToClass;
		this.qualifiedClassName = qualifiedClassName;
	}

	def String getQualifiedClassName() {
		return qualifiedClassName;
	}

	override generateEmptyClass() {
		this.generatedClass = objectMappedToClass.toClass(qualifiedClassName) [
			visibility = JvmVisibility.PRIVATE
			static = true
		]
		return generatedClass;
	}

	override generateBody() {
		generatedClass => [
			superTypes += typeRef(AbstractRepairRoutineRealization.Update)
			members += generateConstructor()
			members += generatedMethods
		]
	}

	def private generateConstructor() {
		objectMappedToClass.toConstructor() [
			val reactionExecutionStateParameter = generateReactionExecutionStateParameter()
			val calledByParameter = generateParameter("calledBy", typeRef(CallHierarchyHaving))
			parameters += reactionExecutionStateParameter
			parameters += calledByParameter
			body = '''
				super(«parameters.get(0).name»);
			'''
		]
	}

	protected def JvmOperation generateMethodCallRoutine(RoutineCallBlock routineCall,
		Iterable<AccessibleElement> accessibleElements, JvmTypeReference facadeClassTypeReference) {
		val methodName = "callRoutine" + counterCallRoutineMethods++;
		return generateExecutionMethod(routineCall, methodName, accessibleElements, facadeClassTypeReference)
	}

	private def JvmOperation generateExecutionMethod(CodeBlock codeBlock, String methodName,
		Iterable<AccessibleElement> accessibleElements, JvmTypeReference facadeClassTypeReference) {
		if (codeBlock === null) {
			return null;
		}
		return codeBlock?.getOrGenerateMethod(methodName, typeRef(Void.TYPE)) [
			parameters += generateAccessibleElementsParameters(accessibleElements);
			val facadeParam = toParameter(REACTION_USER_EXECUTION_ROUTINE_CALL_FACADE_PARAMETER_NAME,
				facadeClassTypeReference);
			facadeParam.annotations += annotationRef(Extension);
			parameters += facadeParam
			body = codeBlock.code;
		]
	}

}
