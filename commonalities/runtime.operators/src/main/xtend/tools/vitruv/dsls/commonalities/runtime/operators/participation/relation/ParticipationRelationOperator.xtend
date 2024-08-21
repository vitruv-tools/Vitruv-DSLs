package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation

import java.lang.annotation.Target
import java.lang.annotation.Retention
import tools.vitruv.dsls.commonalities.runtime.operators.ClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.annotations.Delegate
import tools.vitruv.dsls.commonalities.runtime.operators.OperatorNameProcessor

@Target(TYPE)
@Retention(RUNTIME)
@Active(ParticipationRelationOperatorProcessor)
annotation ParticipationRelationOperator {
	String name
}

final class ParticipationRelationOperatorProcessor implements ClassProcessor {
	@Delegate val ClassProcessor processsor = new OperatorNameProcessor(ParticipationRelationOperator)
}