package tools.vitruv.dsls.common.elements;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

/** An {@link IScope} that provides all registered EPackages in the global EPackage registry. */
@Singleton
public class EPackageRegistryScope implements IScope {
  @Inject private IQualifiedNameConverter qualifiedNameConverter;

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    List<IEObjectDescription> all = getAllDescriptions();
    List<IEObjectDescription> result = new ArrayList<>(all.size());
    for (IEObjectDescription d : all) {
      if (exists(d)) {
        result.add(d);
      }
    }
    return result;
  }

  private Collection<String> getAvailableEPackageUris() {
    return EPackage.Registry.INSTANCE.keySet();
  }

  private List<IEObjectDescription> getAllDescriptions() {
    List<IEObjectDescription> list = new ArrayList<>();
    for (String uri : getAvailableEPackageUris()) {
      list.add(getDescription(uri));
    }
    return list;
  }

  private IEObjectDescription getDescription(String uri) {
    QualifiedName qn = qualifiedNameConverter.toQualifiedName(uri);
    return new EPackageDescription(qn);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(QualifiedName name) {
    IEObjectDescription el = getSingleElement(name);
    if (el == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(el);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(EObject object) {
    IEObjectDescription el = getSingleElement(object);
    if (el == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(el);
  }

  @Override
  public IEObjectDescription getSingleElement(QualifiedName name) {
    if (!EPackage.Registry.INSTANCE.containsKey(name.toString())) {
      return null;
    }
    IEObjectDescription pkg = new EPackageDescription(name);
    return exists(pkg) ? pkg : null;
  }

  @Override
  public IEObjectDescription getSingleElement(EObject object) {
    if (object instanceof EPackage) {
      String nsURI = ((EPackage) object).getNsURI();
      QualifiedName qn = qualifiedNameConverter.toQualifiedName(nsURI);
      return EObjectDescription.create(qn, object);
    }
    return null;
  }

  private static boolean exists(IEObjectDescription description) {
    return description.getEObjectOrProxy() != null;
  }

  private static final class EPackageDescription implements IEObjectDescription {
    private final QualifiedName uriName;
    private volatile EPackage ePackage; // lazy

    private EPackageDescription(QualifiedName name) {
      this.uriName = name;
    }

    private EPackage getEPackage() {
      EPackage local = ePackage;
      if (local == null) {
        local = EPackage.Registry.INSTANCE.getEPackage(uriName.toString());
        ePackage = local;
      }
      return local;
    }

    @Override
    public org.eclipse.emf.ecore.EClass getEClass() {
      EPackage pkg = getEPackage();
      return pkg == null ? null : pkg.eClass();
    }

    @Override
    public EObject getEObjectOrProxy() {
      return getEPackage();
    }

    @Override
    public URI getEObjectURI() {
      EPackage pkg = getEPackage();
      return pkg == null ? null : URI.createURI(pkg.getNsURI());
    }

    @Override
    public QualifiedName getQualifiedName() {
      return uriName;
    }

    @Override
    public String getUserData(String key) {
      return null;
    }

    @Override
    public String[] getUserDataKeys() {
      return new String[0];
    }

    @Override
    public QualifiedName getName() {
      return uriName;
    }
  }
}
