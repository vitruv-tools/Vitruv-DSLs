package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Test constraint specification - loads .ecore and .xmi files from disk. Configure model instance
 * paths via static TEST_MODELS_PATH.
 */
public class MetamodelWrapper implements MetamodelWrapperInterface {

  /** Base path for test model instances (configure before use). */
  public static Path TEST_MODELS_PATH = Path.of("test-models");

  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();
  private final Map<EClass, List<EObject>> instances = new HashMap<>();
  private final ResourceSet resourceSet;

  public MetamodelWrapper() {
    this.resourceSet = new ResourceSetImpl();
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("ecore", new EcoreResourceFactoryImpl());
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("xmi", new XMIResourceFactoryImpl());
  }

  /** Loads an .ecore metamodel file. */
  public void loadMetamodel(Path ecoreFile) throws IOException {
    Resource resource =
        resourceSet.getResource(URI.createFileURI(ecoreFile.toAbsolutePath().toString()), true);

    if (resource.getContents().isEmpty()) {
      throw new IOException("Empty .ecore file: " + ecoreFile);
    }

    EPackage ePackage = (EPackage) resource.getContents().get(0);
    String name = ePackage.getName();
    metamodelRegistry.put(name, ePackage);

    if (ePackage.getNsURI() != null) {
      EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
    }
  }

  /**
   * Loads a .xmi model instance file from TEST_MODELS_PATH.
   *
   * @param xmiFileName File name (e.g., "MyModel.xmi")
   */
  public void loadModelInstance(String xmiFileName) throws IOException {
    Path xmiPath = TEST_MODELS_PATH.resolve(xmiFileName);

    ResourceSet resourceSet = new ResourceSetImpl();

    // Get file extension
    String extension = xmiPath.getFileName().toString();
    int dotIndex = extension.lastIndexOf('.');
    if (dotIndex > 0) {
      extension = extension.substring(dotIndex + 1);
    }

    // Register factory for this extension
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put(extension, new XMIResourceFactoryImpl());

    Resource resource =
        resourceSet.getResource(URI.createFileURI(xmiPath.toAbsolutePath().toString()), true);

    for (EObject root : resource.getContents()) {
      addInstanceRecursive(root);
    }
  }

  /** Recursively adds an instance and its contained objects. */
  private void addInstanceRecursive(EObject instance) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);

    for (EObject child : instance.eContents()) {
      addInstanceRecursive(child);
    }
  }

  @Override
  public EClass resolveEClass(String metamodelName, String className) {
    EPackage ePackage = metamodelRegistry.get(metamodelName);
    if (ePackage == null) {
      System.err.println("MetaModelRegistry: " + metamodelRegistry);
      return null;
    }

    EClassifier classifier = ePackage.getEClassifier(className);
    return (classifier instanceof EClass) ? (EClass) classifier : null;
  }

  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    List<EObject> result = new ArrayList<>();

    // Include exact matches
    result.addAll(instances.getOrDefault(eClass, Collections.emptyList()));

    // Include subtype instances
    for (Map.Entry<EClass, List<EObject>> entry : instances.entrySet()) {
      if (eClass.isSuperTypeOf(entry.getKey()) && !eClass.equals(entry.getKey())) {
        result.addAll(entry.getValue());
      }
    }

    return result;
  }

  @Override
  public Set<String> getAvailableMetamodels() {
    return Collections.unmodifiableSet(metamodelRegistry.keySet());
  }

  /** Adds a model instance for constraint validation. */
  public void addInstance(EObject instance) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);
  }
}