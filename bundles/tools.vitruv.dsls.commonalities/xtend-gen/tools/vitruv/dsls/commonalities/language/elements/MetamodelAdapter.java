package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl;

@SuppressWarnings("all")
public class MetamodelAdapter extends MetamodelImpl implements Wrapper<EPackage> {
  private EPackage wrappedEPackage;

  @Extension
  private ClassifierProvider classifierProvider;

  @Override
  public Metamodel forEPackage(final EPackage ePackage) {
    this.wrappedEPackage = Preconditions.<EPackage>checkNotNull(ePackage);
    return this;
  }

  @Override
  public Metamodel withClassifierProvider(final ClassifierProvider classifierProvider) {
    this.classifierProvider = Preconditions.<ClassifierProvider>checkNotNull(classifierProvider);
    return this;
  }

  private void checkDomainSet() {
    Preconditions.checkState((this.wrappedEPackage != null), "No ePackage was set on this adapter!");
  }

  private void checkClassifierProviderSet() {
    Preconditions.checkState((this.classifierProvider != null), "No classifier provider was set on this element!");
  }

  @Override
  public EList<Metaclass> getMetaclasses() {
    EList<Metaclass> _xblockexpression = null;
    {
      if ((this.metaclasses == null)) {
        this.checkDomainSet();
        this.checkClassifierProviderSet();
        EList<Metaclass> _metaclasses = super.getMetaclasses();
        Iterable<Metaclass> _loadMetaclasses = this.loadMetaclasses();
        Iterables.<Metaclass>addAll(_metaclasses, _loadMetaclasses);
        this.classifierProvider = null;
      }
      _xblockexpression = this.metaclasses;
    }
    return _xblockexpression;
  }

  private Set<EPackage> getRootPackages() {
    return Set.<EPackage>of(this.wrappedEPackage);
  }

  public Set<EPackage> getAllPackages() {
    final Set<EPackage> rootPackages = this.getRootPackages();
    final Function1<EPackage, Iterable<EPackage>> _function = (EPackage it) -> {
      return MetamodelAdapter.getRecursiveSubPackages(it);
    };
    Iterable<EPackage> _flatMap = IterableExtensions.<EPackage, EPackage>flatMap(rootPackages, _function);
    return IterableExtensions.<EPackage>toSet(Iterables.<EPackage>concat(rootPackages, _flatMap));
  }

  private static Iterable<EPackage> getRecursiveSubPackages(final EPackage ePackage) {
    EList<EPackage> _eSubpackages = ePackage.getESubpackages();
    final Function1<EPackage, Iterable<EPackage>> _function = (EPackage it) -> {
      return MetamodelAdapter.getRecursiveSubPackages(it);
    };
    Iterable<EPackage> _flatMap = IterableExtensions.<EPackage, EPackage>flatMap(ePackage.getESubpackages(), _function);
    return Iterables.<EPackage>concat(_eSubpackages, _flatMap);
  }

  private Iterable<Metaclass> loadMetaclasses() {
    final Function1<EPackage, EList<EClassifier>> _function = (EPackage it) -> {
      return it.getEClassifiers();
    };
    final Function1<EClass, Metaclass> _function_1 = (EClass it) -> {
      return this.classifierProvider.toMetaclass(it, this);
    };
    Iterable<Metaclass> _map = IterableExtensions.<EClass, Metaclass>map(Iterables.<EClass>filter(IterableExtensions.<EPackage, EClassifier>flatMap(this.getAllPackages(), _function), EClass.class), _function_1);
    List<ResourceMetaclass> _of = List.<ResourceMetaclass>of(this.createResourceMetaclass());
    return Iterables.<Metaclass>concat(_map, _of);
  }

  private ResourceMetaclass createResourceMetaclass() {
    return LanguageElementsFactory.eINSTANCE.createResourceMetaclass().withClassifierProvider(this.classifierProvider).fromDomain(this);
  }

  @Override
  public String getName() {
    String _xblockexpression = null;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      this.checkDomainSet();
      _xblockexpression = this.wrappedEPackage.getName();
    }
    return _xblockexpression;
  }

  @Override
  public EPackage getWrapped() {
    return this.wrappedEPackage;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("{{");
    String _name = null;
    if (this.wrappedEPackage!=null) {
      _name=this.wrappedEPackage.getName();
    }
    _builder.append(_name);
    _builder.append("}}");
    return _builder.toString();
  }

  @Override
  public boolean equals(final Object o) {
    boolean _xifexpression = false;
    if ((this == o)) {
      _xifexpression = true;
    } else {
      boolean _xifexpression_1 = false;
      if ((o instanceof MetamodelAdapter)) {
        _xifexpression_1 = Objects.equal(this.wrappedEPackage, ((MetamodelAdapter)o).wrappedEPackage);
      } else {
        _xifexpression_1 = false;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  @Override
  public int hashCode() {
    int _xifexpression = (int) 0;
    if ((this.wrappedEPackage == null)) {
      _xifexpression = 0;
    } else {
      _xifexpression = this.wrappedEPackage.hashCode();
    }
    return (5 * _xifexpression);
  }
}
