package tools.vitruv.dsls.common.elements;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.kit.ipd.sdq.activextendannotations.Lazy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@Singleton
@SuppressWarnings("all")
public class EPackageRegistryScope implements IScope {
  private static class EPackageDescription implements IEObjectDescription {
    private final QualifiedName uriName;

    @Lazy
    private EPackage _ePackage;

    public EPackageDescription(final QualifiedName name) {
      this.uriName = name;
    }

    @Override
    public EClass getEClass() {
      return this.getEPackage().eClass();
    }

    @Override
    public EObject getEObjectOrProxy() {
      return this.getEPackage();
    }

    @Override
    public URI getEObjectURI() {
      return URI.createURI(this.getEPackage().getNsURI());
    }

    @Override
    public QualifiedName getQualifiedName() {
      return this.uriName;
    }

    @Override
    public String getUserData(final String key) {
      return null;
    }

    @Override
    public String[] getUserDataKeys() {
      return new String[] {};
    }

    @Override
    public QualifiedName getName() {
      return this.uriName;
    }

    private boolean _ePackage_isInitialised = false;

    private EPackage _ePackage_initialise() {
      EPackage _ePackage = EPackage.Registry.INSTANCE.getEPackage(this.uriName.toString());
      return _ePackage;
    }

    public EPackage getEPackage() {
      if (!_ePackage_isInitialised) {
      	try {
      		_ePackage = _ePackage_initialise();
      	} finally {
      		_ePackage_isInitialised = true;
      	}
      }
      return _ePackage;
    }
  }

  @Inject
  @Extension
  private IQualifiedNameConverter qualifiedNameConverter;

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    final Function1<IEObjectDescription, Boolean> _function = (IEObjectDescription it) -> {
      return Boolean.valueOf(EPackageRegistryScope.exists(it));
    };
    return IterableExtensions.<IEObjectDescription>filter(this.getAllDescriptions(), _function);
  }

  public Iterable<EObject> allEPackages() {
    final Function1<IEObjectDescription, Boolean> _function = (IEObjectDescription it) -> {
      return Boolean.valueOf(EPackageRegistryScope.exists(it));
    };
    final Function1<IEObjectDescription, EObject> _function_1 = (IEObjectDescription it) -> {
      return it.getEObjectOrProxy();
    };
    return IterableExtensions.<IEObjectDescription, EObject>map(IterableExtensions.<IEObjectDescription>filter(this.getAllDescriptions(), _function), _function_1);
  }

  private Set<String> getAvailableEPackageUris() {
    return EPackage.Registry.INSTANCE.keySet();
  }

  private Iterable<IEObjectDescription> getAllDescriptions() {
    final Function1<String, IEObjectDescription> _function = (String it) -> {
      return this.getDescription(it);
    };
    return IterableExtensions.<String, IEObjectDescription>map(this.getAvailableEPackageUris(), _function);
  }

  private IEObjectDescription getDescription(final String uri) {
    QualifiedName _qualifiedName = this.qualifiedNameConverter.toQualifiedName(uri);
    return new EPackageRegistryScope.EPackageDescription(_qualifiedName);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
    List<IEObjectDescription> _xblockexpression = null;
    {
      final IEObjectDescription el = this.getSingleElement(name);
      List<IEObjectDescription> _xifexpression = null;
      if ((el == null)) {
        _xifexpression = Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList());
      } else {
        _xifexpression = Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList(el));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    IEObjectDescription _singleElement = this.getSingleElement(object);
    return IterableExtensions.<IEObjectDescription>filterNull(Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList(_singleElement)));
  }

  @Override
  public IEObjectDescription getSingleElement(final QualifiedName name) {
    EPackageRegistryScope.EPackageDescription _xblockexpression = null;
    {
      boolean _containsKey = EPackage.Registry.INSTANCE.containsKey(name.toString());
      boolean _not = (!_containsKey);
      if (_not) {
        return null;
      }
      final EPackageRegistryScope.EPackageDescription package_ = new EPackageRegistryScope.EPackageDescription(name);
      EPackageRegistryScope.EPackageDescription _xifexpression = null;
      boolean _exists = EPackageRegistryScope.exists(package_);
      if (_exists) {
        _xifexpression = package_;
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  @Override
  public IEObjectDescription getSingleElement(final EObject object) {
    if ((object instanceof EPackage)) {
      return EObjectDescription.create(this.qualifiedNameConverter.toQualifiedName(((EPackage)object).getNsURI()), object);
    }
    return null;
  }

  private static boolean exists(final IEObjectDescription description) {
    EObject _eObjectOrProxy = description.getEObjectOrProxy();
    return (_eObjectOrProxy != null);
  }
}
