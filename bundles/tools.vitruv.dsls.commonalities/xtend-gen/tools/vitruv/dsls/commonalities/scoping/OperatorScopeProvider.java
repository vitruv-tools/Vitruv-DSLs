package tools.vitruv.dsls.commonalities.scoping;

import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;

@SuppressWarnings("all")
public final class OperatorScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {
  public static class Factory extends InjectingFactoryBase {
    public OperatorScopeProvider.OptionalImportsFactory forOperatorType(final Class<?> operatorType) {
      return this.<OperatorScopeProvider.OptionalImportsFactory>injectMembers(new OperatorScopeProvider.OptionalImportsFactory(operatorType));
    }
  }

  @FinalFieldsConstructor
  public static class OptionalImportsFactory extends InjectingFactoryBase {
    private final Class<?> operatorBaseType;

    public OperatorScopeProvider withDefaultImports(final String... defaultImports) {
      return this.<OperatorScopeProvider>injectMembers(new OperatorScopeProvider(this.operatorBaseType, (List<String>)Conversions.doWrapArray(defaultImports)));
    }

    public OperatorScopeProvider build() {
      return this.withDefaultImports();
    }

    public OptionalImportsFactory(final Class<?> operatorBaseType) {
      super();
      this.operatorBaseType = operatorBaseType;
    }
  }

  private List<String> translatedImports;

  private OperatorScopeProvider(final Class<?> operatorBaseType, final List<String> defaultImports) {
    final Function1<String, String> _function = (String it) -> {
      return CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(it);
    };
    this.translatedImports = IterableUtil.<String, String>mapFixed(defaultImports, _function);
  }

  @Override
  protected List<ImportNormalizer> getImplicitImports(final boolean ignoreCase) {
    final Function1<String, ImportNormalizer> _function = (String it) -> {
      return this.createImportedNamespaceResolver(it, ignoreCase);
    };
    return ListExtensions.<String, ImportNormalizer>map(this.translatedImports, _function);
  }

  @Override
  protected IScope getLocalElementsScope(final IScope parent, final EObject context, final EReference reference) {
    return parent;
  }

  @Override
  protected IScope getResourceScope(final IScope parent, final EObject context, final EReference reference) {
    IScope _xblockexpression = null;
    {
      final boolean ignoreCase = this.isIgnoreCase(reference);
      final List<ImportNormalizer> namespaceResolvers = this.getImportedNamespaceResolvers(context, ignoreCase);
      IScope _xifexpression = null;
      boolean _isEmpty = namespaceResolvers.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        _xifexpression = this.createImportScope(parent, namespaceResolvers, null, reference.getEReferenceType(), ignoreCase);
      } else {
        _xifexpression = parent;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  @Override
  public String getWildCard() {
    return "_";
  }

  @Override
  protected boolean isRelativeImport() {
    return false;
  }
}
