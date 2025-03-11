package tools.vitruv.dsls.commonalities.language.elements;

import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

@Singleton
@SuppressWarnings("all")
public class MetamodelProvider {
  /**
   * In order to be referenced from a Xtext language, EObjects must be
   * contained in a resource. So we create a fake resource to put our domain
   * adapters in. This resource is never serialized and has no other purpose.
   */
  private static final URI CONTAINER_RESOURCE_URI = URI.createURI("synthetic:/commonalities/metamodelAdapters");

  private final Resource container = this.createContainerResource();

  private Map<String, Metamodel> allMetamodelsByName = new HashMap<String, Metamodel>();

  MetamodelProvider() {
  }

  public boolean registerReferencedMetamodel(final String name, final EPackage ePackage) {
    boolean _xifexpression = false;
    boolean _containsKey = this.allMetamodelsByName.containsKey(name);
    boolean _not = (!_containsKey);
    if (_not) {
      boolean _xblockexpression = false;
      {
        final Metamodel metamodel = LanguageElementsFactory.eINSTANCE.createMetamodel().withClassifierProvider(
          ClassifierProvider.INSTANCE).forEPackage(ePackage);
        this.allMetamodelsByName.put(name, metamodel);
        EList<EObject> _contents = this.container.getContents();
        _xblockexpression = _contents.add(metamodel);
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }

  private Resource createContainerResource() {
    final ResourceSetImpl resourceSet = new ResourceSetImpl();
    return resourceSet.createResource(MetamodelProvider.CONTAINER_RESOURCE_URI);
  }

  public Metamodel getMetamodelByName(final String name) {
    return this.allMetamodelsByName.get(name);
  }

  public Collection<Metamodel> getAllMetamodels() {
    return this.allMetamodelsByName.values();
  }
}
