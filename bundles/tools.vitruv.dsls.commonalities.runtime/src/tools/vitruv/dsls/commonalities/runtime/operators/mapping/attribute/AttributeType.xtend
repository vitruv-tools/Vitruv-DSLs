package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute

import java.lang.annotation.Retention
import java.lang.annotation.Target

@Target(TYPE)
@Retention(RUNTIME)
annotation AttributeType {

	boolean multiValued
	Class<?> type
}
