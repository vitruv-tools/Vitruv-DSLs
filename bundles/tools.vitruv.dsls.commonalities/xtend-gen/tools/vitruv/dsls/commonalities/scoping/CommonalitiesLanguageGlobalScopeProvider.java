package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.Set;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.FilteringScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.common.elements.EPackageRegistryScope;
import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.language.elements.MetamodelProvider;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;

@SuppressWarnings("all")
public class CommonalitiesLanguageGlobalScopeProvider extends TypesAwareDefaultGlobalScopeProvider {
  @Inject
  @Extension
  private IEObjectDescriptionProvider descriptionProvider;

  @Inject
  private Provider<EPackageRegistryScope> packagesScope;

  @Inject
  private MetamodelProvider metamodelProvider;

  @Override
  public IScope getScope(final Resource resource, final EReference reference, final Predicate<IEObjectDescription> filter) {
    IScope _scope = super.getScope(resource, reference, filter);
    IScope __getScope = this._getScope(resource, reference);
    Predicate<IEObjectDescription> _elvis = null;
    if (filter != null) {
      _elvis = filter;
    } else {
      Predicate<IEObjectDescription> _alwaysTrue = Predicates.<IEObjectDescription>alwaysTrue();
      _elvis = _alwaysTrue;
    }
    FilteringScope _filteringScope = new FilteringScope(__getScope, _elvis);
    return new ComposedScope(_scope, _filteringScope);
  }

  private IScope _getScope(final Resource resource, final EReference reference) {
    IScope _switchResult = null;
    boolean _matched = false;
    if (Objects.equal(reference, ElementsPackage.Literals.METAMODEL_IMPORT__PACKAGE)) {
      _matched=true;
      return this.packagesScope.get();
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_CLASS__SUPER_METACLASS)) {
        _matched=true;
        _switchResult = new MetamodelMetaclassesScope(resource, this.descriptionProvider, this.metamodelProvider);
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.COMMONALITY_REFERENCE__REFERENCE_TYPE)) {
        _matched=true;
        _switchResult = this.getLocalCommonalityScope(resource);
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.COMMONALITY_ATTRIBUTE_REFERENCE__COMMONALITY)) {
        _matched=true;
        _switchResult = this.getLocalCommonalityScope(resource);
      }
    }
    if (!_matched) {
      _switchResult = IScope.NULLSCOPE;
    }
    return _switchResult;
  }

  private SimpleScope getLocalCommonalityScope(final Resource resource) {
    Set<IEObjectDescription> _singleton = Collections.<IEObjectDescription>singleton(
      this.descriptionProvider.describe(CommonalitiesLanguageModelExtensions.getContainedCommonalityFile(resource).getCommonality()));
    return new SimpleScope(IScope.NULLSCOPE, _singleton);
  }
}
