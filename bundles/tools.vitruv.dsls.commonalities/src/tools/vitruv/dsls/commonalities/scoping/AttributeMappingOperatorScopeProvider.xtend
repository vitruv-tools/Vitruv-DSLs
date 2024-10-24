package tools.vitruv.dsls.commonalities.scoping

import com.google.inject.Singleton
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.IAttributeMappingOperator

import org.eclipse.xtend.lib.annotations.Delegate
import org.eclipse.xtext.scoping.IScopeProvider
import jakarta.inject.Inject

@Singleton
class AttributeMappingOperatorScopeProvider implements IScopeProvider {
	@Delegate val IScopeProvider delegate
	
	@Inject
	new(OperatorScopeProvider.Factory factory) {
		delegate = factory.forOperatorType(IAttributeMappingOperator)
			.withDefaultImports("tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute._")
	}
}
