package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import org.eclipse.emf.common.util.DelegatingEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;

@SuppressWarnings("all")
public class ResourceMetaclassI extends ResourceMetaclassImpl implements Wrapper<EClass> {
  private static final String RESOURCE_METACLASS_NAME = "Resource";

  private Metaclass adapter;

  private Domain containingDomain;

  private ClassifierProvider classifierProvider;

  @Override
  public ResourceMetaclass withClassifierProvider(final ClassifierProvider classifierProvider) {
    this.classifierProvider = Preconditions.<ClassifierProvider>checkNotNull(classifierProvider);
    if ((this.containingDomain != null)) {
      this.readAdapter();
    }
    return this;
  }

  @Override
  public ResourceMetaclass fromDomain(final Domain domain) {
    this.containingDomain = Preconditions.<Domain>checkNotNull(domain);
    if ((this.classifierProvider != null)) {
      this.readAdapter();
    }
    return this;
  }

  private ClassifierProvider readAdapter() {
    ClassifierProvider _xblockexpression = null;
    {
      this.adapter = this.classifierProvider.toMetaclass(ResourcesPackage.eINSTANCE.getResource(), this.containingDomain);
      _xblockexpression = this.classifierProvider = null;
    }
    return _xblockexpression;
  }

  private void checkDomainSet() {
    Preconditions.checkState((this.containingDomain != null), "No domain was set on this metaclass!");
  }

  private void checkAdapterCreated() {
    if ((this.adapter == null)) {
      this.checkDomainSet();
      Preconditions.checkState((this.classifierProvider != null), "No classifierProvider was set on this adapter!");
    }
  }

  @Override
  public String getName() {
    return ResourceMetaclassI.RESOURCE_METACLASS_NAME;
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
  public EList<Attribute> getAttributes() {
    this.checkAdapterCreated();
    EList<Attribute> _attributes = this.adapter.<Attribute>getAttributes();
    return new DelegatingEList.UnmodifiableEList<Attribute>(_attributes);
  }

  @Override
  public Domain basicGetDomain() {
    this.checkDomainSet();
    return this.containingDomain;
  }

  @Override
  public EClass getWrapped() {
    return ResourcesPackage.eINSTANCE.getResource();
  }

  protected boolean _isSuperTypeOf(final Classifier subType) {
    return Objects.equal(subType, this);
  }

  protected boolean _isSuperTypeOf(final MostSpecificType mostSpecificType) {
    return true;
  }

  protected boolean _isSuperTypeOf(final LeastSpecificType leastSpecificType) {
    return false;
  }

  @Override
  public EList<MetaclassMember> getAllMembers() {
    DelegatingEList.UnmodifiableEList<MetaclassMember> _xblockexpression = null;
    {
      this.checkAdapterCreated();
      EList<MetaclassMember> _allMembers = this.adapter.getAllMembers();
      _xblockexpression = new DelegatingEList.UnmodifiableEList<MetaclassMember>(_allMembers);
    }
    return _xblockexpression;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Resource Metaclass (‹");
    Domain _domain = this.getDomain();
    _builder.append(_domain);
    _builder.append("›)");
    return _builder.toString();
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  public boolean isSuperTypeOf(final Classifier leastSpecificType) {
    if (leastSpecificType instanceof LeastSpecificType) {
      return _isSuperTypeOf((LeastSpecificType)leastSpecificType);
    } else if (leastSpecificType instanceof MostSpecificType) {
      return _isSuperTypeOf((MostSpecificType)leastSpecificType);
    } else if (leastSpecificType != null) {
      return _isSuperTypeOf(leastSpecificType);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(leastSpecificType).toString());
    }
  }
}
