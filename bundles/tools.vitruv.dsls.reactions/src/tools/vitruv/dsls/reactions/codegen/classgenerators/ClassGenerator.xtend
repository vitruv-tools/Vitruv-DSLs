package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.mapFixed
import org.eclipse.xtext.common.types.JvmVisibility

/**
 * JVM Model inference should happen in two phases:
 * <ol>
 * <li>Create empty classes so they can be found when linking</li>
 * <li>After linking is done, generate the bodies</li>
 * </ol>
 * Therefore, the {@link ClassGenerator}s provide separated methods 
 * {@link ClassGenerator#generateEmptyClass() generateEmptyClass}
 * and {@link ClassGenerator#generateBody(JvmGenericType) generateBody} for those steps.
 */
abstract class ClassGenerator extends TypesBuilderExtensionProvider {
	protected def generateAccessibleElementsParameters(EObject sourceObject,
		Iterable<AccessibleElement> accessibleElements) {
			sourceObject.generateParameters(accessibleElements)
	}

	new(TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		typesBuilderExtensionProvider.copyBuildersTo(this);
	}

	def JvmGenericType generateEmptyClass()

	def JvmGenericType generateBody()

	protected def getCommentWithoutMarkers(String documentation) {
		if (documentation !== null && documentation.length > 4) {
			val withoutMultilineCommentMarkers = documentation.replaceAll("\\n \\* ","\\n")
			return withoutMultilineCommentMarkers.substring(2,withoutMultilineCommentMarkers.length-2)
		} else {
			return documentation
		}
	}
	
	protected def generateElementsContainerClass(String qualifiedClassName, Iterable<AccessibleElement> elements) {
		generateUnassociatedClass(qualifiedClassName) [
			val retrievedElementParameters = generateParameters(elements)
			members += retrievedElementParameters.mapFixed[
				toField(name, parameterType) => [
					final = true
					visibility = JvmVisibility.PUBLIC
				]
			]
			members += toConstructor [
				visibility = JvmVisibility.PUBLIC
				parameters += retrievedElementParameters
				body = '''
				«FOR parameter : parameters»this.«parameter.name» = «parameter.name»;
				«ENDFOR»'''
			]
		]
	}
}
