package tools.vitruv.dsls.commonalities.scoping

import com.google.inject.Singleton
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator

import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtend.lib.annotations.Delegate
import jakarta.inject.Inject

@Singleton
class ReferenceMappingOperatorScopeProvider implements IScopeProvider {
	@Delegate val IScopeProvider delegate
	
	@Inject
	new(OperatorScopeProvider.Factory factory) {
		delegate = factory.forOperatorType(IReferenceMappingOperator)
			.withDefaultImports("tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference._")
	}
}
