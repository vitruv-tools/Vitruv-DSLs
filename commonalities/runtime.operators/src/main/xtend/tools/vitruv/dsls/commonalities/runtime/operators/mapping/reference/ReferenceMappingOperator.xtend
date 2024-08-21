package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference

import java.lang.annotation.Target
import java.lang.annotation.Retention
import tools.vitruv.dsls.commonalities.runtime.operators.ClassProcessor
import org.eclipse.xtend.lib.annotations.Delegate
import tools.vitruv.dsls.commonalities.runtime.operators.OperatorNameProcessor
import org.eclipse.xtend.lib.macro.Active

@Target(TYPE)
@Retention(RUNTIME)
@Active(ReferenceMappingOperatorProcessor)
annotation ReferenceMappingOperator {
	String name
	boolean isMultiValued
	boolean isAttributeReference = false
}

final class ReferenceMappingOperatorProcessor implements ClassProcessor {
	@Delegate val ClassProcessor processsor = new OperatorNameProcessor(ReferenceMappingOperator)
}