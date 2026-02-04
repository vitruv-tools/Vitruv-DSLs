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
 * Manages metamodel and model instance loading for VitruvOCL constraint evaluation.
 *
 * <p>Provides metamodel-to-instance mapping required for constraint evaluation, supporting:
 *
 * <ul>
 *   <li>Loading Ecore metamodels with package name registration
 *   <li>Loading XMI model instances and organizing by EClass
 *   <li>Resolving qualified names like {@code spacecraft::Spacecraft} to EClass
 *   <li>Querying all instances of a given EClass (including subtypes)
 * </ul>
 *
 * <p>Implements {@link MetamodelWrapperInterface} for use across compilation phases, particularly
 * in the evaluation visitor where constraints access model elements.
 */
public class MetamodelWrapper implements MetamodelWrapperInterface {

  /** Default directory for test model files (legacy support) */
  public static Path TEST_MODELS_PATH = Path.of("test-models");

  /** Maps package names to loaded EPackages */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /** Maps EClasses to all instances (including subtype instances) */
  private final Map<EClass, List<EObject>> instances = new HashMap<>();

  /** EMF resource set for loading metamodels */
  private final ResourceSet resourceSet;

  /** Creates metamodel wrapper with EMF resource factories configured. */
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

  /**
   * Loads metamodel with explicit package name override.
   *
   * @param packageName Name to register metamodel under (for qualified name resolution)
   * @param ecoreFile Path to .ecore metamodel file
   * @throws IOException If file cannot be read or is empty
   */
  public void loadMetamodel(String packageName, Path ecoreFile) throws IOException {
    Resource resource =
        resourceSet.getResource(URI.createFileURI(ecoreFile.toAbsolutePath().toString()), true);

    if (resource.getContents().isEmpty()) {
      throw new IOException("Empty .ecore file: " + ecoreFile);
    }

    EPackage ePackage = (EPackage) resource.getContents().get(0);
    metamodelRegistry.put(packageName, ePackage);

    if (ePackage.getNsURI() != null) {
      EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
    }
  }

  /**
   * Loads metamodel using its intrinsic package name from file.
   *
   * @param ecoreFile Path to .ecore metamodel file
   * @throws IOException If file cannot be read or is empty
   */
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
   * Loads model instance from absolute path, indexing all objects by EClass.
   *
   * @param xmiPath Absolute path to XMI model file
   * @throws IOException If file cannot be read
   */
  public void loadModelInstance(Path xmiPath) throws IOException {
    ResourceSet instanceResourceSet = new ResourceSetImpl();

    String extension = xmiPath.getFileName().toString();
    int dotIndex = extension.lastIndexOf('.');
    if (dotIndex > 0) {
      extension = extension.substring(dotIndex + 1);
    }

    instanceResourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put(extension, new XMIResourceFactoryImpl());

    Resource resource =
        instanceResourceSet.getResource(
            URI.createFileURI(xmiPath.toAbsolutePath().toString()), true);

    for (EObject root : resource.getContents()) {
      addInstanceRecursive(root);
    }
  }

  /**
   * Loads model instance from TEST_MODELS_PATH directory (legacy method).
   *
   * @param xmiFileName Filename relative to TEST_MODELS_PATH
   * @throws IOException If file cannot be read
   */
  public void loadModelInstance(String xmiFileName) throws IOException {
    loadModelInstance(TEST_MODELS_PATH.resolve(xmiFileName));
  }

  /** Recursively indexes instance and all contained objects by EClass. */
  private void addInstanceRecursive(EObject instance) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);

    for (EObject child : instance.eContents()) {
      addInstanceRecursive(child);
    }
  }

  /**
   * Resolves fully qualified name to EClass.
   *
   * @param metamodelName Package name (e.g., "spacecraft")
   * @param className Class name (e.g., "Spacecraft")
   * @return Resolved EClass, or null if not found
   */
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

  /**
   * Returns all instances of given EClass, including subtype instances.
   *
   * @param eClass EClass to query
   * @return List of all direct and indirect instances
   */
  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    List<EObject> result = new ArrayList<>();

    result.addAll(instances.getOrDefault(eClass, Collections.emptyList()));

    for (Map.Entry<EClass, List<EObject>> entry : instances.entrySet()) {
      if (eClass.isSuperTypeOf(entry.getKey()) && !eClass.equals(entry.getKey())) {
        result.addAll(entry.getValue());
      }
    }

    return result;
  }

  /**
   * Returns all registered metamodel package names.
   *
   * @return Unmodifiable set of package names
   */
  @Override
  public Set<String> getAvailableMetamodels() {
    return Collections.unmodifiableSet(metamodelRegistry.keySet());
  }

  /**
   * Manually adds instance to index.
   *
   * @param instance EObject to register
   */
  public void addInstance(EObject instance) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);
  }
}