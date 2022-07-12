package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition

import java.lang.annotation.Target
import java.lang.annotation.Retention
import tools.vitruv.dsls.commonalities.runtime.operators.ClassProcessor
import org.eclipse.xtend.lib.annotations.Delegate
import tools.vitruv.dsls.commonalities.runtime.operators.OperatorNameProcessor
import org.eclipse.xtend.lib.macro.Active

@Target(TYPE)
@Retention(RUNTIME)
@Active(ParticipationConditionOperatorProcessor)
annotation ParticipationConditionOperator {
	String name
}

final class ParticipationConditionOperatorProcessor implements ClassProcessor {
	@Delegate val ClassProcessor delegate = new OperatorNameProcessor(ParticipationConditionOperator)
}