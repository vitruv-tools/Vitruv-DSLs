package tools.vitruv.dsls.reactions.codegen.changetyperepresentation

import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement

import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*
import java.util.List

/**
 * This class is responsible for representing the relevant change information for generating reactions
 * code for changes. 
 * The information for the changes are extracted by the {@link ChangeTypeRepresentationExtractor} from 
 * a {@link tools.vitruv.dsls.reactions.language.toplevelelements.Trigger} of the reactions language.
 */
class ChangeTypeRepresentation {
	static final String TEMPORARY_TYPED_CHANGE_NAME = "_localTypedChange"
	static val primitveToWrapperTypesMap = newHashMap(#[
		short -> Short,
		int-> Integer,
		long -> Long,
		double -> Double,
		float -> Float,
		boolean -> Boolean,
		char -> Character,
		byte -> Byte,
		void -> Void
	].map [key.canonicalName -> value.canonicalName])
		
	private static def mapToNonPrimitiveType(String potentiallyPrimitiveTypeCName) {
		return primitveToWrapperTypesMap.getOrDefault(potentiallyPrimitiveTypeCName, potentiallyPrimitiveTypeCName)
	}	

	@Accessors(PUBLIC_GETTER)
	final Class<?> changeType
	@Accessors(PUBLIC_GETTER)
	final String affectedElementClassCanonicalName
	final String affectedValueClassCanonicalName
	final boolean hasOldValue
	final boolean hasNewValue
	final boolean hasIndex
	@Accessors(PUBLIC_GETTER)
	final EStructuralFeature affectedFeature
	@Accessors(PUBLIC_GETTER)
	final String name
	final List<String> explicitGenericTypeParameters
	
	protected new(String name, Class<?> changeType, String affectedElementClassCanonicalName, String affectedValueClassCanonicalName, boolean hasOldValue,
		boolean hasNewValue, EStructuralFeature affectedFeature, boolean hasIndex) {
		this(name, changeType, affectedElementClassCanonicalName, affectedValueClassCanonicalName, hasOldValue, hasNewValue, affectedFeature, hasIndex, null)
	}
	
	protected new(String name, Class<?> changeType, String affectedElementClassCanonicalName, String affectedValueClassCanonicalName, boolean hasOldValue,
		boolean hasNewValue, EStructuralFeature affectedFeature, boolean hasIndex, List<String> explicitGenericTypeParameters) {
		this.name = name
		this.changeType = changeType
		this.affectedElementClassCanonicalName = affectedElementClassCanonicalName.mapToNonPrimitiveType
		this.affectedValueClassCanonicalName = affectedValueClassCanonicalName.mapToNonPrimitiveType
		this.affectedFeature = affectedFeature
		this.hasOldValue = hasOldValue
		this.hasNewValue = hasNewValue
		this.hasIndex = hasIndex
		this.explicitGenericTypeParameters = explicitGenericTypeParameters
	}

	def getAffectedElementClass() {
		affectedElementClassCanonicalName
	}

	def getAffectedValueClass() {
		affectedValueClassCanonicalName
	}

	def boolean hasAffectedElement() {
		return affectedElementClass !== null
	}
	
	def boolean hasAffectedFeature() {
		return affectedFeature !== null
	}
	
	def getGenericTypeParameters() {
		if (explicitGenericTypeParameters !== null) {
			return explicitGenericTypeParameters
		}
		#[affectedElementClassCanonicalName, affectedValueClassCanonicalName].filterNull
	}

	def StringConcatenationClient getSetFieldCode(String parameterName) '''
		this.«name» = («typedChangeTypeRepresentation») «parameterName»;
	'''
	
	def AccessibleElement getAccessibleElement() {
		return new AccessibleElement(name, changeType.name, genericTypeParameters)
	}
	
	def StringConcatenationClient generateCheckMethodBody(String parameterName) '''
		if (!(«parameterName» instanceof «changeTypeRepresentationWithWildcards»)) {
			return false;
		}
		
		«typedChangeTypeRepresentation» «TEMPORARY_TYPED_CHANGE_NAME» = («typedChangeTypeRepresentation») «parameterName»;
		«TEMPORARY_TYPED_CHANGE_NAME.generateElementChecks»
		«parameterName.setFieldCode»
		return true;
	'''
	
	private def StringConcatenationClient generateElementChecks(String parameterName) '''
		«IF hasAffectedElement»
			if (!(«parameterName».getAffectedElement() instanceof «affectedElementClass»)) {
				return false;
			}
		«ENDIF»
		«IF hasAffectedFeature»
			if (!«parameterName».getAffectedFeature().getName().equals("«affectedFeature.name»")) {
				return false;
			}
		«ENDIF»
		«IF hasOldValue»
			if («IF ReplaceSingleValuedFeatureEChange.isAssignableFrom(changeType)»«parameterName».isFromNonDefaultValue() && «
				ENDIF»!(«parameterName».getOldValue() instanceof «affectedValueClass»)) {
				return false;
			}
		«ENDIF»
		«IF hasNewValue»
			if («IF ReplaceSingleValuedFeatureEChange.isAssignableFrom(changeType)»«parameterName».isToNonDefaultValue() && «
				ENDIF»!(«parameterName».getNewValue() instanceof «affectedValueClass»)) {
				return false;
			}
		«ENDIF»
	'''

	def Iterable<AccessibleElement> generatePropertiesParameterList() {
		val result = <AccessibleElement>newArrayList()
		result.add(new AccessibleElement(name, changeType))
		if (affectedElementClass !== null) {
			result.add(new AccessibleElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE, affectedElementClass))
		}
		if (affectedFeature !== null) {
			result.add(new AccessibleElement(CHANGE_AFFECTED_FEATURE_ATTRIBUTE, affectedFeature.eClass.instanceClass))
		}
		if (hasOldValue) {
			result.add(new AccessibleElement(CHANGE_OLD_VALUE_ATTRIBUTE, affectedValueClass))
		}
		if (hasNewValue) {
			result.add(new AccessibleElement(CHANGE_NEW_VALUE_ATTRIBUTE, affectedValueClass))
		}
		if (hasIndex) {
			result.add(new AccessibleElement(CHANGE_INDEX_ATTRIBUTE, int))
		}
		return result
	}

	def StringConcatenationClient generatePropertiesAssignmentCode() {
		'''
			«IF affectedElementClass !== null»
				«affectedElementClass» «CHANGE_AFFECTED_ELEMENT_ATTRIBUTE» = («affectedElementClass»)«name».«CHANGE_AFFECTED_ELEMENT_ACCESSOR»;
			«ENDIF»
			«IF affectedFeature !== null»
				«affectedFeature.eClass.instanceClass» «CHANGE_AFFECTED_FEATURE_ATTRIBUTE» = «name».get«CHANGE_AFFECTED_FEATURE_ATTRIBUTE.toFirstUpper»();
			«ENDIF»
			«IF hasOldValue»
				«affectedValueClass» «CHANGE_OLD_VALUE_ATTRIBUTE» = («affectedValueClass»)«name».get«CHANGE_OLD_VALUE_ATTRIBUTE.toFirstUpper»();
			«ENDIF»
			«IF hasNewValue»
				«affectedValueClass» «CHANGE_NEW_VALUE_ATTRIBUTE» = («affectedValueClass»)«name».get«CHANGE_NEW_VALUE_ATTRIBUTE.toFirstUpper»();
			«ENDIF»
			«IF hasIndex»
				int «CHANGE_INDEX_ATTRIBUTE» = «name».get«CHANGE_INDEX_ATTRIBUTE.toFirstUpper»();
			«ENDIF»
		'''
	}
	
	def StringConcatenationClient getUntypedChangeTypeRepresentation() {
		return '''«changeType»'''
	}

	def StringConcatenationClient getTypedChangeTypeRepresentation() {
		return '''«changeType»«FOR param : genericTypeParameters BEFORE "<" SEPARATOR ", " AFTER ">"»«param»«ENDFOR»'''
	}
	
	def StringConcatenationClient getChangeTypeRepresentationWithWildcards() {
		return '''«changeType»«FOR param : genericTypeParameters BEFORE "<" SEPARATOR ", " AFTER ">"»?«ENDFOR»'''
	}

}
