package tools.vitruv.dsls.reactions.ide.hover

import com.google.inject.Inject
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider
import org.eclipse.xtext.documentation.impl.MultiLineCommentDocumentationProvider

/**
 * Extends the default, comment-based hover documentation with a generated summary of a
 * metaclass's attributes and references.
 * <p>
 * An {@code EClassifier} referenced from a {@code .reactions} file (e.g. {@code model::System})
 * lives in a separately loaded {@code .ecore} resource, so it never has an {@code ML_COMMENT} for
 * {@link MultiLineCommentDocumentationProvider} (the framework default) to find, and hovering over
 * it previously always showed an empty tooltip.
 */
class ReactionsEObjectDocumentationProvider implements IEObjectDocumentationProvider {

	@Inject MultiLineCommentDocumentationProvider commentDocumentationProvider

	override String getDocumentation(EObject object) {
		if (object instanceof EClassifier) {
			return object.describeClassifier
		}
		if (object instanceof EStructuralFeature) {
			return object.describeFeature
		}
		return commentDocumentationProvider.getDocumentation(object)
	}

	def private String describeClassifier(EClassifier classifier) {
		val builder = new StringBuilder
		switch classifier {
			EClass: {
				builder.append('''«IF classifier.abstract»_abstract_ «ENDIF»«IF classifier.interface»interface«ELSE»class«ENDIF» **«classifier.name»**''')
				if (!classifier.ESuperTypes.empty) {
					builder.append(''' extends «classifier.ESuperTypes.map[name].join(", ")»''')
				}
				builder.append("\n")
				if (!classifier.EAllAttributes.empty) {
					builder.append("\n**Attributes:**\n")
					for (attribute : classifier.EAllAttributes) {
						builder.append('''- «attribute.describeFeatureSignature»''')
						builder.append("\n")
					}
				}
				if (!classifier.EAllReferences.empty) {
					builder.append("\n**References:**\n")
					for (reference : classifier.EAllReferences) {
						builder.append('''- «reference.describeFeatureSignature»''')
						builder.append("\n")
					}
				}
			}
			EEnum: {
				builder.append('''enum **«classifier.name»**''')
				if (!classifier.ELiterals.empty) {
					builder.append("\n\n")
					builder.append(classifier.ELiterals.map[literal].join(", "))
				}
			}
			EDataType: {
				builder.append('''type **«classifier.name»**''')
				if (classifier.instanceClassName !== null) {
					builder.append(''' («classifier.instanceClassName»)''')
				}
			}
			default: {
				builder.append(classifier.name)
			}
		}
		return builder.toString.trim
	}

	def private String describeFeature(EStructuralFeature feature) {
		val declaringClass = feature.EContainingClass
		'''«feature.describeFeatureSignature»«IF declaringClass !== null» — declared in «declaringClass.name»«ENDIF»'''
	}

	def private String describeFeatureSignature(EStructuralFeature feature) {
		val typeName = feature.EType?.name ?: "?"
		val containment = if (feature instanceof EReference) {
			if ((feature as EReference).containment) " (containment)" else ""
		} else {
			""
		}
		'''«feature.name»: «typeName»«feature.describeMultiplicity»«containment»'''
	}

	def private String describeMultiplicity(EStructuralFeature feature) {
		val lower = feature.lowerBound
		val upper = feature.upperBound
		if (lower == 1 && upper == 1) {
			""
		} else if (upper == -1) {
			''' [«lower»..*]'''
		} else if (lower == upper) {
			''' [«lower»]'''
		} else {
			''' [«lower»..«upper»]'''
		}
	}

}
