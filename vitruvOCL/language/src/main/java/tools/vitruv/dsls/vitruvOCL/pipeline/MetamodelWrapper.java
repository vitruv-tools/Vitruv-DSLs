/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
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

  /** Default directory for test model files (legacy support). */
  public static Path TEST_MODELS_PATH = Path.of("test-models");

  /** Maps package names to loaded EPackages. */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /** Maps EClasses to all instances (including subtype instances). */
  private final Map<EClass, List<EObject>> instances = new HashMap<>();

  /**
   * Maps each registered EObject to its source filename. Uses identity (not equals) so different.
   * objects from same file are tracked separately.
   */
  private final Map<EObject, String> instanceSourceFile = new IdentityHashMap<>();

  /** Ordered list of context-level (root) EObjects for index-based lookup from evaluator. */
  private final List<EObject> contextObjects = new ArrayList<>();

  /** Maps instance index to source filename for error reporting (index matches contextObjects). */
  private final List<String> instanceFilenames = new ArrayList<>();

  /** EMF resource set for loading metamodels. */
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
    ResourceSet instanceResourceSet = this.resourceSet;

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

    String filename = xmiPath.getFileName().toString();

    for (EObject root : resource.getContents()) {
      addInstanceRecursiveInternal(root, filename);
      // Register root as context candidate (one entry per root EObject per file)
      contextObjects.add(root);
      instanceFilenames.add(filename);
    }
  }

  /**
   * Returns the context EObject at the given evaluation index.
   *
   * @param index The evaluation index (0-based)
   * @return The EObject at that index, or null if out of bounds
   */
  @Override
  public EObject getContextObjectByIndex(int index) {
    if (index >= 0 && index < contextObjects.size()) {
      return contextObjects.get(index);
    }
    return null;
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

  /**
   * Internal recursive helper. All levels add to instances map and instanceSourceFile. Only
   * top-level roots are registered in contextObjects/instanceFilenames.
   */
  private void addInstanceRecursiveInternal(EObject instance, String sourceFile) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);
    instanceSourceFile.put(instance, sourceFile);

    for (EObject child : instance.eContents()) {
      addInstanceRecursiveInternal(child, sourceFile);
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
   * Returns source filename for the context object at the given evaluation index. Index corresponds
   * to the i-th root EObject loaded (one per root per XMI file).
   *
   * @param index The evaluation index (0-based, one per root context object)
   * @return The filename (e.g., "spacecraft-atlas.spacemission"), or null if out of bounds
   */
  @Override
  public String getInstanceNameByIndex(int index) {
    if (index >= 0 && index < instanceFilenames.size()) {
      return instanceFilenames.get(index);
    }
    return null;
  }

  /**
   * Returns the source filename for a specific EObject instance (by identity). More reliable than
   * index-based lookup.
   */
  public String getSourceFileForInstance(EObject instance) {
    return instanceSourceFile.get(instance);
  }

  /** Returns all registered context (root) objects in load order. */
  public List<EObject> getContextObjects() {
    return Collections.unmodifiableList(contextObjects);
  }

  /**
   * Manually adds instance to index.
   *
   * @param instance EObject to register
   */
  public void addInstance(EObject instance) {
    instances.computeIfAbsent(instance.eClass(), k -> new ArrayList<>()).add(instance);
    instanceFilenames.add("manually-added");
  }

  /**
   * Returns all root objects from all loaded model resources.
   *
   * <p>Iterates through all resources in the resource set and collects their root contents. This
   * includes metamodel packages, model instances, and correspondence models.
   *
   * @return List of all root EObjects from all loaded resources
   */
  @Override
  public List<EObject> getAllRootObjects() {
    List<EObject> roots = new ArrayList<>();

    // Iterate through all resources in the resource set
    for (Resource resource : resourceSet.getResources()) {
      // Add all root contents from this resource
      roots.addAll(resource.getContents());
    }
    return roots;
  }

  /**
   * Resolves an EClass by its unqualified short name across all registered metamodel packages.
   *
   * <p>Iterates all loaded {@link EPackage}s and returns the first {@link EClassifier} whose name
   * equals {@code shortName} and which is an {@link EClass}. Subpackages are also searched one
   * level deep.
   *
   * @param shortName the unqualified class name (e.g., {@code "Coordinate"})
   * @return the first matching {@link EClass}, or {@code null} if not found
   */
  @Override
  public EClass resolveEClassByShortName(String shortName) {
    for (EPackage ePackage : metamodelRegistry.values()) {
      EClass found = resolveEClassInPackage(ePackage, shortName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Searches {@code ePackage} and its direct subpackages for an {@link EClass} with the given short
   * name.
   *
   * @param ePackage the package to search
   * @param shortName the unqualified class name
   * @return the matching {@link EClass}, or {@code null}
   */
  private EClass resolveEClassInPackage(EPackage ePackage, String shortName) {
    // Search classifiers in this package
    EClassifier classifier = ePackage.getEClassifier(shortName);
    if (classifier instanceof EClass eClass) {
      return eClass;
    }
    // Recurse into sub-packages (one level — sufficient for standard Ecore layouts)
    for (EPackage subPkg : ePackage.getESubpackages()) {
      EClassifier subClassifier = subPkg.getEClassifier(shortName);
      if (subClassifier instanceof EClass eClass) {
        return eClass;
      }
    }
    return null;
  }
}
