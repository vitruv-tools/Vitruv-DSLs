package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.List;
import org.eclipse.emf.common.util.DelegatingEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.language.elements.impl.EClassMetaclassImpl;

@SuppressWarnings("all")
public class EClassAdapter extends EClassMetaclassImpl implements Wrapper<EClass> {
  private EClass wrappedEClass;

  @Extension
  private ClassifierProvider classifierProvider;

  private Domain containingDomain;

  @Override
  public EClassMetaclass withClassifierProvider(final ClassifierProvider classifierProvider) {
    this.classifierProvider = Preconditions.<ClassifierProvider>checkNotNull(classifierProvider);
    return this;
  }

  @Override
  public EClassMetaclass forEClass(final EClass eClass) {
    this.wrappedEClass = Preconditions.<EClass>checkNotNull(eClass);
    return this;
  }

  @Override
  public EClassMetaclass fromDomain(final Domain domain) {
    this.containingDomain = domain;
    return this;
  }

  private void checkEClassSet() {
    Preconditions.checkState((this.wrappedEClass != null), "No EClass was set on this adapter!");
  }

  private void checkClassifierProviderSet() {
    Preconditions.checkState((this.classifierProvider != null), "No classifier provider was set on this element!");
  }

  private void checkDomainSet() {
    Preconditions.checkState((this.containingDomain != null), "No domain was set on this metaclass!");
  }

  @Override
  public String getName() {
    String _xblockexpression = null;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      this.checkEClassSet();
      _xblockexpression = this.wrappedEClass.getName();
    }
    return _xblockexpression;
  }

  @Override
  public EList<Attribute> getAttributes() {
    boolean _eIsProxy = this.eIsProxy();
    if (_eIsProxy) {
      return super.getAttributes();
    }
    if ((this.attributes == null)) {
      this.checkEClassSet();
      this.checkClassifierProviderSet();
      EList<Attribute> _attributes = super.getAttributes();
      List<EFeatureAttribute> _loadAttributes = this.loadAttributes();
      Iterables.<Attribute>addAll(_attributes, _loadAttributes);
      this.classifierProvider = null;
    }
    return this.attributes;
  }

  private List<EFeatureAttribute> loadAttributes() {
    final Function1<EStructuralFeature, EFeatureAttribute> _function = (EStructuralFeature eFeature) -> {
      return LanguageElementsFactory.eINSTANCE.createEFeatureAttribute().withClassifierProvider(this.classifierProvider).forEFeature(eFeature).fromMetaclass(this);
    };
    return ListExtensions.<EStructuralFeature, EFeatureAttribute>map(this.wrappedEClass.getEAllStructuralFeatures(), _function);
  }

  @Override
  public EClass getWrapped() {
    return this.wrappedEClass;
  }

  @Override
  public PackageLike basicGetPackageLikeContainer() {
    Domain _xblockexpression = null;
    {
      this.checkDomainSet();
      _xblockexpression = this.getDomain();
    }
    return _xblockexpression;
  }

  @Override
  public Domain basicGetDomain() {
    boolean _eIsProxy = this.eIsProxy();
    if (_eIsProxy) {
      return null;
    }
    this.checkDomainSet();
    return this.containingDomain;
  }

  protected boolean _isSuperTypeOf(final Classifier subType) {
    return false;
  }

  protected boolean _isSuperTypeOf(final EClassAdapter eClassAdapter) {
    if ((this == eClassAdapter)) {
      return true;
    }
    EClass _eObject = EcorePackage.eINSTANCE.getEObject();
    boolean _tripleEquals = (this.wrappedEClass == _eObject);
    if (_tripleEquals) {
      return true;
    }
    return this.wrappedEClass.isSuperTypeOf(eClassAdapter.wrappedEClass);
  }

  protected boolean _isSuperTypeOf(final MostSpecificType mostSpecificType) {
    return true;
  }

  protected boolean _isSuperTypeOf(final LeastSpecificType leastSpecificType) {
    return false;
  }

  @Override
  public EList<MetaclassMember> getAllMembers() {
    EList<Attribute> _attributes = this.getAttributes();
    return new DelegatingEList.UnmodifiableEList(_attributes);
  }

  @Override
  public String toString() {
    String _xifexpression = null;
    boolean _eIsProxy = this.eIsProxy();
    if (_eIsProxy) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("unresolved ");
      String _simpleName = this.getClass().getSimpleName();
      _builder.append(_simpleName);
      _builder.append(": ");
      URI _eProxyURI = this.eProxyURI();
      _builder.append(_eProxyURI);
      _xifexpression = _builder.toString();
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      String _name = this.containingDomain.getName();
      _builder_1.append(_name);
      _builder_1.append(":");
      String _name_1 = null;
      if (this.wrappedEClass!=null) {
        _name_1=this.wrappedEClass.getName();
      }
      _builder_1.append(_name_1);
      _xifexpression = _builder_1.toString();
    }
    return _xifexpression;
  }

  @Override
  public boolean isAbstract() {
    boolean _xblockexpression = false;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return false;
      }
      this.checkEClassSet();
      _xblockexpression = this.wrappedEClass.isAbstract();
    }
    return _xblockexpression;
  }

  @Override
  public boolean equals(final Object o) {
    boolean _xifexpression = false;
    if ((this == o)) {
      _xifexpression = true;
    } else {
      boolean _xifexpression_1 = false;
      if ((o == null)) {
        _xifexpression_1 = false;
      } else {
        boolean _xifexpression_2 = false;
        if ((o instanceof EClassAdapter)) {
          _xifexpression_2 = (Objects.equal(this.containingDomain, ((EClassAdapter)o).containingDomain) && Objects.equal(this.wrappedEClass, ((EClassAdapter)o).wrappedEClass));
        } else {
          _xifexpression_2 = false;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  @Override
  public int hashCode() {
    final int prime = 53;
    int _xifexpression = (int) 0;
    if ((this.containingDomain == null)) {
      _xifexpression = 0;
    } else {
      _xifexpression = this.containingDomain.hashCode();
    }
    int _plus = (prime + _xifexpression);
    int _multiply = (_plus * prime);
    int _xifexpression_1 = (int) 0;
    if ((this.wrappedEClass == null)) {
      _xifexpression_1 = 0;
    } else {
      _xifexpression_1 = this.wrappedEClass.hashCode();
    }
    return (_multiply + _xifexpression_1);
  }

  public boolean isSuperTypeOf(final Classifier eClassAdapter) {
    if (eClassAdapter instanceof EClassAdapter) {
      return _isSuperTypeOf((EClassAdapter)eClassAdapter);
    } else if (eClassAdapter instanceof LeastSpecificType) {
      return _isSuperTypeOf((LeastSpecificType)eClassAdapter);
    } else if (eClassAdapter instanceof MostSpecificType) {
      return _isSuperTypeOf((MostSpecificType)eClassAdapter);
    } else if (eClassAdapter != null) {
      return _isSuperTypeOf(eClassAdapter);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(eClassAdapter).toString());
    }
  }
}
