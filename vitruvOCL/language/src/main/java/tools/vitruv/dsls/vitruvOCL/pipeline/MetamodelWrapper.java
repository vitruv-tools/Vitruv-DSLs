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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Manages metamodel and model instance loading for OCL constraint evaluation.
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
   * Reloads a previously loaded metamodel from disk, replacing the old version in the registry.
   *
   * <p>If the file was not previously loaded this behaves identically to {@link
   * #loadMetamodel(Path)}. Call this when the {@code .ecore} file has been modified on disk (e.g.
   * triggered by a {@code workspace/didChangeWatchedFiles} event from the language client).
   *
   * @param ecoreFile path to the modified {@code .ecore} file
   * @throws IOException if the file cannot be read or is empty after modification
   */
  public void reloadMetamodel(Path ecoreFile) throws IOException {
    unloadMetamodel(ecoreFile);
    loadMetamodel(ecoreFile);
  }

  /**
   * Removes a previously loaded metamodel from the registry and the EMF resource set.
   *
   * <p>The package is also removed from {@link EPackage.Registry#INSTANCE} so that subsequent loads
   * start from a clean state. No-op if the file was never loaded.
   *
   * @param ecoreFile path to the {@code .ecore} file to remove
   */
  public void unloadMetamodel(Path ecoreFile) {
    String targetUri = URI.createFileURI(ecoreFile.toAbsolutePath().toString()).toString();

    // Find the cached Resource in the ResourceSet by URI.
    Resource toRemove = null;
    for (Resource res : new ArrayList<>(resourceSet.getResources())) {
      if (res.getURI().toString().equals(targetUri)) {
        toRemove = res;
        break;
      }
    }

    if (toRemove == null) return; // file was never loaded — nothing to do

    // Remove the EPackage from our registry and from the global EMF registry.
    if (!toRemove.getContents().isEmpty()
        && toRemove.getContents().get(0) instanceof EPackage pkg) {
      metamodelRegistry.remove(pkg.getName());
      if (pkg.getNsURI() != null) {
        EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
      }
    }

    toRemove.unload();
    resourceSet.getResources().remove(toRemove);
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
   * Resolves an {@link EEnum} by name across all registered metamodel packages.
   *
   * <p>Searches each {@link EPackage} in the metamodel registry in iteration order, delegating to
   * {@link #resolveEEnumInPackage} for each package. Returns the first match found, or {@code null}
   * if no {@code EEnum} with the given name exists in any registered package.
   *
   * @param enumName the simple name of the {@code EEnum} to resolve
   * @return the first matching {@link EEnum}, or {@code null} if not found
   */
  @Override
  public EEnum resolveEEnum(String enumName) {
    for (EPackage ePackage : metamodelRegistry.values()) {
      EEnum found = resolveEEnumInPackage(ePackage, enumName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Resolves an {@link EEnum} by name within an {@link EPackage} and its subpackages.
   *
   * <p>Searches the package's classifiers for an {@code EEnum} with the given name, then recurses
   * into subpackages if no match is found.
   *
   * @param ePackage the root package to search in
   * @param enumName the name of the enum to resolve
   * @return the matching {@link EEnum}, or {@code null} if not found
   */
  private EEnum resolveEEnumInPackage(EPackage ePackage, String enumName) {
    for (EClassifier classifier : ePackage.getEClassifiers()) {
      if (classifier instanceof EEnum eEnum && eEnum.getName().equals(enumName)) {
        return eEnum;
      }
    }
    for (EPackage subPackage : ePackage.getESubpackages()) {
      EEnum found = resolveEEnumInPackage(subPackage, enumName);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Returns the EPackage registered under the given metamodel name.
   *
   * <p>Used by the language server completion provider to enumerate all EClass names within a
   * package (e.g., for {@code JavaMM::} prefix completion).
   *
   * @param metamodelName the package name (e.g., {@code "JavaMM"})
   * @return the registered {@link EPackage}, or {@code null} if not found
   */
  public EPackage getEPackage(String metamodelName) {
    return metamodelRegistry.get(metamodelName);
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

  /**
   * Returns all EObjects corresponding to the given source object.
   *
   * <p>Searches all loaded Correspondence objects in the resource set. A Correspondence relates
   * obj1 to obj2 if obj1 appears in leftEObjects and obj2 in rightEObjects, or vice versa
   * (bidirectional).
   *
   * @param source the source object to look up correspondences for
   * @return set of all corresponding objects; empty if none exist
   */
  @Override
  public Set<EObject> getCorrespondingObjects(EObject source) {
    Set<EObject> result = new LinkedHashSet<>();
    for (EObject corrObj : getCorrespondenceObjects()) {
      @SuppressWarnings("unchecked")
      List<EObject> lefts = resolveAll((List<EObject>) safeGet(corrObj, "leftEObjects"));
      @SuppressWarnings("unchecked")
      List<EObject> rights = resolveAll((List<EObject>) safeGet(corrObj, "rightEObjects"));
      if (lefts == null || rights == null) continue;
      if (lefts.contains(source)) result.addAll(rights);
      if (rights.contains(source)) result.addAll(lefts);
    }
    return result;
  }

  @Override
  public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
    for (EObject corrObj : getCorrespondenceObjects()) {
      @SuppressWarnings("unchecked")
      List<EObject> lefts = resolveAll((List<EObject>) safeGet(corrObj, "leftEObjects"));
      @SuppressWarnings("unchecked")
      List<EObject> rights = resolveAll((List<EObject>) safeGet(corrObj, "rightEObjects"));
      String corrTag = (String) safeGet(corrObj, "tag");
      if (lefts == null || rights == null) continue;
      boolean matches =
          (lefts.contains(obj1) && rights.contains(obj2))
              || (lefts.contains(obj2) && rights.contains(obj1));
      if (matches && tag.equals(corrTag)) return true;
    }
    return false;
  }

  /**
   * Resolves all proxy EObjects in the list using the resource set. Returns a new list with
   * resolved objects; unresolvable proxies are dropped.
   */
  private List<EObject> resolveAll(List<EObject> proxies) {
    if (proxies == null) return null;
    List<EObject> resolved = new ArrayList<>(proxies.size());
    for (EObject obj : proxies) {
      EObject r = EcoreUtil.resolve(obj, resourceSet);
      if (r != null && !r.eIsProxy()) {
        resolved.add(r);
      }
    }
    return resolved;
  }

  /**
   * Collects all Correspondence objects from all loaded resources.
   *
   * <p>A Correspondence object is any EObject whose EClass name is "Correspondence" or a subtype
   * (e.g., "ManualCorrespondence"). The container "Correspondences" root object is traversed via
   * its "correspondences" reference.
   *
   * @return flat list of all Correspondence EObjects across all loaded resources
   */
  private List<EObject> getCorrespondenceObjects() {
    List<EObject> result = new ArrayList<>();
    for (Resource resource : resourceSet.getResources()) {

      for (EObject root : resource.getContents()) {

        EStructuralFeature corrFeature = root.eClass().getEStructuralFeature("correspondences");
        if (corrFeature != null) {
          @SuppressWarnings("unchecked")
          List<EObject> corrs = (List<EObject>) root.eGet(corrFeature);

          if (corrs != null) {
            for (EObject corr : corrs) {
              EStructuralFeature tagFeature = corr.eClass().getEStructuralFeature("tag");
              EStructuralFeature leftFeature = corr.eClass().getEStructuralFeature("leftEObjects");
              EStructuralFeature rightFeature =
                  corr.eClass().getEStructuralFeature("rightEObjects");

              @SuppressWarnings("unchecked")
              List<EObject> lefts =
                  leftFeature != null ? (List<EObject>) corr.eGet(leftFeature) : null;
              @SuppressWarnings("unchecked")
              List<EObject> rights =
                  rightFeature != null ? (List<EObject>) corr.eGet(rightFeature) : null;
            }
            result.addAll(corrs);
          }
        }
      }
    }
    return result;
  }

  /**
   * Safely retrieves a structural feature value from an EObject by feature name.
   *
   * @param obj the EObject to read from
   * @param featureName the feature name
   * @return the feature value, or null if the feature does not exist
   */
  private Object safeGet(EObject obj, String featureName) {
    EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureName);
    if (feature == null) return null;
    return obj.eGet(feature);
  }
}

