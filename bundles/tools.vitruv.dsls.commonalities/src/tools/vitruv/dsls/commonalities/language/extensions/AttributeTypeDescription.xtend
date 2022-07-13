package tools.vitruv.dsls.commonalities.language.extensions

import org.eclipse.xtend.lib.annotations.Data
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType

/**
 * Mirrors the information available from {@link AttributeType}.
 */
@Data
class AttributeTypeDescription {
	val boolean multiValued
	val String qualifiedTypeName
}
