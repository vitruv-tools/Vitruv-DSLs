package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.VirtualModel;

/** Vitruvius VSUM-based constraint specification. Accesses models via ViewSource interface. */
public class VSUMWrapper implements MetamodelWrapperInterface {

  private final VirtualModel vsum;
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  public VSUMWrapper(VirtualModel vsum) {
    this.vsum = vsum;
    loadMetamodelsFromVSUM();
  }

  private void loadMetamodelsFromVSUM() {
    // VirtualModel extends ViewSource - use getViewSourceModels()
    Collection<Resource> resources = ((ViewSource) vsum).getViewSourceModels();

    // Extract and register all EPackages from resources
    resources.stream()
        .flatMap(r -> r.getContents().stream())
        .map(EObject::eClass)
        .map(EClass::getEPackage)
        .distinct()
        .forEach(
            pkg -> {
              metamodelRegistry.put(pkg.getName(), pkg);
              EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
            });
  }

  @Override
  public EClass resolveEClass(String metamodelName, String className) {
    EPackage ePackage = metamodelRegistry.get(metamodelName);
    if (ePackage == null) return null;

    EClassifier classifier = ePackage.getEClassifier(className);
    return (classifier instanceof EClass) ? (EClass) classifier : null;
  }

  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    Collection<Resource> resources = ((ViewSource) vsum).getViewSourceModels();

    return resources.stream()
        .flatMap(r -> r.getContents().stream())
        .flatMap(root -> getAllContentsRecursive(root).stream())
        .filter(obj -> eClass.isSuperTypeOf(obj.eClass()))
        .toList();
  }

  private List<EObject> getAllContentsRecursive(EObject root) {
    List<EObject> result = new ArrayList<>();
    result.add(root);
    root.eAllContents().forEachRemaining(result::add);
    return result;
  }

  @Override
  public Set<String> getAvailableMetamodels() {
    return Collections.unmodifiableSet(metamodelRegistry.keySet());
  }
}