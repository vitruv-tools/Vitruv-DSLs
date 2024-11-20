package tools.vitruv.dsls.commonalities.scoping

import com.google.inject.Singleton
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.IParticipationConditionOperator

import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtend.lib.annotations.Delegate
import jakarta.inject.Inject

@Singleton
class ParticipationConditionOperatorScopeProvider implements IScopeProvider {
	@Delegate val IScopeProvider delegate
	
	@Inject
	new(OperatorScopeProvider.Factory factory) {
		delegate = factory.forOperatorType(IParticipationConditionOperator)
			.withDefaultImports("tools.vitruv.dsls.commonalities.runtime.operators.participation.condition._")
	}
}
