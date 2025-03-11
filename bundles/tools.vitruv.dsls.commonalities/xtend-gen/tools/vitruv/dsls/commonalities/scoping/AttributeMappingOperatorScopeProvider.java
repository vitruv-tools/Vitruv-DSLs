package tools.vitruv.dsls.commonalities.scoping;

import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.IAttributeMappingOperator;
import tools.vitruv.dsls.commonalities.scoping.OperatorScopeProvider;

@Singleton
@SuppressWarnings("all")
public class AttributeMappingOperatorScopeProvider implements IScopeProvider {
  @Delegate
  private final IScopeProvider delegate;

  @Inject
  public AttributeMappingOperatorScopeProvider(final OperatorScopeProvider.Factory factory) {
    this.delegate = factory.forOperatorType(IAttributeMappingOperator.class).withDefaultImports("tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute._");
  }

  public IScope getScope(final EObject arg0, final EReference arg1) {
    return this.delegate.getScope(arg0, arg1);
  }
}
