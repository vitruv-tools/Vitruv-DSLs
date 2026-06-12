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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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

  /**
   * Correspondences loaded from .correspondence files via DOM (not EMF).
   * Maps an absolute EMF URI string to the set of corresponding absolute URI strings.
   */
  private final Map<String, Set<String>> correspondenceUriMap = new HashMap<>();

  /**
   * Maps a bidirectional key {@code "leftUri|rightUri"} to the set of tags on that correspondence.
   * Both directions are stored so lookups are O(1) regardless of order.
   */
  private final Map<String, Set<String>> correspondenceTagMap = new HashMap<>();

  /** Ordered list of context-level (root) EObjects for index-based lookup from evaluator. */
  private final List<EObject> contextObjects = new ArrayList<>();

  /** Maps instance index to source filename for error reporting (index matches contextObjects). */
  private final List<String> instanceFilenames = new ArrayList<>();

  /** EMF resource set for loading metamodels. */
  private final ResourceSet resourceSet;

  /** Matches any {@code platform:/plugin/.../<name>.ecore} URI in an ecore file. */
  private static final Pattern PLATFORM_PLUGIN_ECORE_PATTERN =
      Pattern.compile("platform:/plugin/[^\"#\\s]+\\.ecore");

  /**
   * Scans a set of workspace {@code .ecore} files for {@code platform:/plugin/} cross-references
   * and registers URI mappings so EMF can resolve them to local files without any manual
   * configuration.
   *
   * <p>How it works:
   * <ol>
   *   <li>Build a {@code filename → file URI} map from {@code ecorePaths} (e.g.
   *       {@code "stoex.ecore" → file:/C:/…/stoex.ecore}).
   *   <li>For each ecore file, grep its raw text for {@code platform:/plugin/…/name.ecore}.
   *   <li>If the referenced filename is found in the workspace, add the exact mapping to
   *       the EMF {@link org.eclipse.emf.ecore.resource.URIConverter} of this resource set.
   * </ol>
   *
   * <p>Call this <em>before</em> {@link #loadMetamodel(Path)} so that inherited features from
   * cross-ecore supertypes (e.g. {@code stoex::RandomVariable#specification}) are visible to the
   * type checker without touching any project file.
   *
   * @param ecorePaths all {@code .ecore} files found in the workspace
   */
  public void registerWorkspaceEcoresForPlatformResolution(List<Path> ecorePaths) {
    // filename.ecore -> file URI of the local copy
    Map<String, URI> byFilename = new HashMap<>();
    for (Path p : ecorePaths) {
      String name = p.getFileName().toString();
      byFilename.put(name, URI.createFileURI(p.toAbsolutePath().toString()));
    }

    Map<URI, URI> uriMap = resourceSet.getURIConverter().getURIMap();

    for (Path ecorePath : ecorePaths) {
      try {
        String content = Files.readString(ecorePath);
        Matcher m = PLATFORM_PLUGIN_ECORE_PATTERN.matcher(content);
        while (m.find()) {
          String platformUriStr = m.group();
          URI platformUri = URI.createURI(platformUriStr);
          String filename = platformUri.lastSegment();
          URI localUri = byFilename.get(filename);
          if (localUri != null && !uriMap.containsKey(platformUri)) {
            uriMap.put(platformUri, localUri);
            System.err.println("[OCL-LS] platform:/plugin/ mapped: " + platformUriStr + " -> " + localUri);
          }
        }
      } catch (IOException e) {
        // unreadable file — skip silently
      }
    }
  }

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
    ensureReactionsCorrespondenceRegistered();
  }

  /**
   * Dynamically registers the {@code ReactionsCorrespondence} EClass as a concrete subtype of the
   * base {@code Correspondence} class when the Vitruvius reactions runtime JAR is not on the
   * classpath.
   *
   * <p>The correspondence files produced by Vitruvius use
   * {@code xsi:type="correspondence_1:ReactionsCorrespondence"} (namespace URI
   * {@code http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0}). When the
   * reactions runtime is absent, EMF cannot resolve this type and silently drops all correspondence
   * entries, causing every {@code ~} operator to evaluate to {@code false}. Registering a minimal
   * dynamic EPackage containing {@code ReactionsCorrespondence} as a subtype of the known
   * {@code Correspondence} class is sufficient to let EMF load the entries correctly; the inherited
   * {@code leftEObjects} / {@code rightEObjects} features are resolved via the base class.
   */
  private static void ensureReactionsCorrespondenceRegistered() {
    final String REACTIONS_NS_URI =
        "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0";
    if (EPackage.Registry.INSTANCE.containsKey(REACTIONS_NS_URI)) {
      return;
    }

    final String CORR_NS_URI = "http://vitruv.tools/metamodels/change/correspondence/1.0";
    EPackage corrPackage = (EPackage) EPackage.Registry.INSTANCE.get(CORR_NS_URI);
    if (corrPackage == null) {
      corrPackage = forceInitCorrespondencePackage();
    }
    if (corrPackage == null) {
      corrPackage = buildBaseCorrespondencePackage(CORR_NS_URI);
    }

    // ReactionsCorrespondence is self-contained: no cross-package supertype.
    // Cross-package EClass inheritance in dynamic EPackages causes EMF to reject the class
    // as "not a valid classifier" during XMI loading. All required features are added directly.
    EPackage reactionsPackage = EcoreFactory.eINSTANCE.createEPackage();
    reactionsPackage.setName("correspondence_1");
    reactionsPackage.setNsPrefix("correspondence_1");
    reactionsPackage.setNsURI(REACTIONS_NS_URI);

    EClass reactionsCorr = EcoreFactory.eINSTANCE.createEClass();
    reactionsCorr.setName("ReactionsCorrespondence");
    addCorrespondenceFeatures(reactionsCorr);

    reactionsPackage.getEClassifiers().add(reactionsCorr);
    EPackage.Registry.INSTANCE.put(REACTIONS_NS_URI, reactionsPackage);
  }

  /**
   * Builds the base correspondence EPackage dynamically.
   *
   * <p>Creates a minimal structural copy of the real package: a {@code Correspondences} root class
   * (with {@code correspondences} containment using {@code EObject} as element type so that any
   * concrete subtype can be held without type-compatibility issues) and a concrete
   * {@code Correspondence} class carrying {@code leftEObjects}, {@code rightEObjects}, and
   * {@code tag}.
   */
  private static EPackage buildBaseCorrespondencePackage(String nsUri) {
    EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
    pkg.setName("correspondence");
    pkg.setNsPrefix("correspondence");
    pkg.setNsURI(nsUri);

    // Concrete (non-abstract) Correspondence class with the three needed features.
    EClass corrClass = EcoreFactory.eINSTANCE.createEClass();
    corrClass.setName("Correspondence");
    addCorrespondenceFeatures(corrClass);

    // Correspondences root — holds any EObject so xsi:type subtypes are accepted.
    EClass corrsClass = EcoreFactory.eINSTANCE.createEClass();
    corrsClass.setName("Correspondences");

    EReference corrsRef = EcoreFactory.eINSTANCE.createEReference();
    corrsRef.setName("correspondences");
    corrsRef.setEType(EcorePackage.Literals.EOBJECT); // EObject: accepts any concrete subtype
    corrsRef.setUpperBound(-1);
    corrsRef.setContainment(true);
    corrsClass.getEStructuralFeatures().add(corrsRef);

    pkg.getEClassifiers().add(corrsClass);
    pkg.getEClassifiers().add(corrClass);

    EPackage.Registry.INSTANCE.put(nsUri, pkg);
    return pkg;
  }

  /** Adds leftEObjects, rightEObjects, and tag features to the given EClass. */
  private static void addCorrespondenceFeatures(EClass cls) {
    EReference leftRef = EcoreFactory.eINSTANCE.createEReference();
    leftRef.setName("leftEObjects");
    leftRef.setEType(EcorePackage.Literals.EOBJECT);
    leftRef.setUpperBound(-1);
    cls.getEStructuralFeatures().add(leftRef);

    EReference rightRef = EcoreFactory.eINSTANCE.createEReference();
    rightRef.setName("rightEObjects");
    rightRef.setEType(EcorePackage.Literals.EOBJECT);
    rightRef.setUpperBound(-1);
    cls.getEStructuralFeatures().add(rightRef);

    EAttribute tagAttr = EcoreFactory.eINSTANCE.createEAttribute();
    tagAttr.setName("tag");
    tagAttr.setEType(EcorePackage.Literals.ESTRING);
    cls.getEStructuralFeatures().add(tagAttr);
  }

  /**
   * Forces initialization of the generated {@code CorrespondencePackage} EMF class via reflection.
   *
   * <p>In standalone (non-OSGi) mode the EMF generated classes are in the fat JAR but their static
   * initializers are not automatically called. Accessing {@code CorrespondencePackage.eINSTANCE}
   * triggers {@code CorrespondencePackageImpl.init()}, which registers the package in
   * {@link EPackage.Registry#INSTANCE}.
   *
   * @return the registered {@link EPackage}, or {@code null} if reflection fails
   */
  private static EPackage forceInitCorrespondencePackage() {
    final String CORR_NS_URI = "http://vitruv.tools/metamodels/change/correspondence/1.0";
    // Try generated interface — accessing eINSTANCE triggers static init + package registration.
    String[] candidateClasses = {
      "tools.vitruv.change.correspondence.CorrespondencePackage",
      "tools.vitruv.change.correspondence.impl.CorrespondencePackageImpl"
    };
    for (String className : candidateClasses) {
      try {
        Class<?> cls = Class.forName(className);
        java.lang.reflect.Field f = cls.getField("eINSTANCE");
        f.get(null); // triggers static init
        EPackage pkg = (EPackage) EPackage.Registry.INSTANCE.get(CORR_NS_URI);
        if (pkg != null) return pkg;
      } catch (Exception e) {
        // class not on classpath — expected in standalone mode, fall through to dynamic build
      }
    }
    return null;
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
    registerPackageRecursively(ePackage);
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
    registerPackageRecursively(ePackage);
  }

  /**
   * Registers an {@link EPackage} and all its sub-packages recursively in both
   * {@link EPackage.Registry#INSTANCE} (by nsURI, for EMF XMI loading) and
   * {@link #metamodelRegistry} (by package name, for constraint type resolution).
   *
   * <p>Without this, model instance files that reference sub-package types (e.g.
   * {@code xmlns:tires="tires"}) cause a {@code PackageNotFoundException} during loading
   * because only the root package's nsURI is known to EMF.
   */
  private void registerPackageRecursively(EPackage pkg) {
    if (pkg.getNsURI() != null) {
      EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
    }
    // Also register by name so resolveEClass("tires", "Tire") works
    if (pkg.getName() != null) {
      metamodelRegistry.putIfAbsent(pkg.getName(), pkg);
    }
    for (EPackage subPkg : pkg.getESubpackages()) {
      registerPackageRecursively(subPkg);
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
    // Correspondence files are loaded via DOM to avoid EMF dynamic-EPackage type-validation issues.
    if (xmiPath.getFileName().toString().endsWith(".correspondence")) {
      loadCorrespondenceViaDOM(xmiPath);
      return;
    }

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

    System.err.println("[DBG-MW] Loaded file: " + filename
        + " | contents=" + resource.getContents().size()
        + " | errors=" + resource.getErrors().size());
    if (!resource.getErrors().isEmpty()) {
      resource.getErrors().forEach(e -> System.err.println("[DBG-MW]   load-error: " + e.getMessage()));
    }

    for (EObject root : resource.getContents()) {
      System.err.println("[DBG-MW]   root eClass: " + root.eClass().getName()
          + " (pkg=" + root.eClass().getEPackage().getNsURI() + ")");
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
   * Loads a Vitruvius {@code .correspondence} file via DOM (not EMF) and populates
   * {@link #correspondenceUriMap}.
   *
   * <p>EMF cannot reliably load correspondence XMI files in standalone mode because the
   * {@code ReactionsCorrespondence} type is only available as a dynamic EClass with no Java
   * backing class, which triggers {@code IllegalValueException} during containment validation.
   * Parsing the XML directly with a DOM parser is simpler and avoids all type-checking issues.
   *
   * <p>Each {@code <correspondences>} element's {@code leftEObjects} and {@code rightEObjects}
   * child hrefs are resolved to absolute EMF URIs relative to the correspondence file location
   * and stored bidirectionally in {@link #correspondenceUriMap}.
   */
  private void loadCorrespondenceViaDOM(Path corrPath) {
    URI baseUri = URI.createFileURI(corrPath.toAbsolutePath().toString());
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(null); // suppress SAX warnings
      Document doc = builder.parse(corrPath.toFile());

      NodeList corrNodes = doc.getElementsByTagName("correspondences");
      for (int i = 0; i < corrNodes.getLength(); i++) {
        Element corrEl = (Element) corrNodes.item(i);
        String tag = corrEl.getAttribute("tag");
        // Support two XMI serialisation styles:
        //   (a) inline attributes:  <correspondences leftEObjects="..." rightEObjects="..."/>
        //   (b) child elements:     <correspondences><leftEObjects href="..."/></correspondences>
        List<String> lefts = collectHrefsFromAttr(corrEl, "leftEObjects", baseUri);
        if (lefts.isEmpty()) lefts = collectHrefsFromChildElements(corrEl, "leftEObjects", baseUri);
        List<String> rights = collectHrefsFromAttr(corrEl, "rightEObjects", baseUri);
        if (rights.isEmpty()) rights = collectHrefsFromChildElements(corrEl, "rightEObjects", baseUri);
        for (String l : lefts) {
          for (String r : rights) {
            correspondenceUriMap.computeIfAbsent(l, k -> new LinkedHashSet<>()).add(r);
            correspondenceUriMap.computeIfAbsent(r, k -> new LinkedHashSet<>()).add(l);
            if (tag != null && !tag.isEmpty()) {
              correspondenceTagMap
                  .computeIfAbsent(l + "|" + r, k -> new LinkedHashSet<>()).add(tag);
              correspondenceTagMap
                  .computeIfAbsent(r + "|" + l, k -> new LinkedHashSet<>()).add(tag);
            }
          }
        }
      }
      System.err.println("[DBG-MW] Loaded correspondence (DOM): " + corrPath.getFileName()
          + " | entries=" + correspondenceUriMap.size());
    } catch (Exception e) {
      System.err.println("[DBG-MW] Failed to load correspondence via DOM: " + e.getMessage());
    }
  }

  /** Collects href values from child elements (format: {@code <leftEObjects href="..."/>}). */
  private List<String> collectHrefsFromChildElements(Element parent, String childTag, URI baseUri) {
    List<String> result = new ArrayList<>();
    NodeList children = parent.getElementsByTagName(childTag);
    for (int i = 0; i < children.getLength(); i++) {
      Element child = (Element) children.item(i);
      String href = child.getAttribute("href");
      if (href != null && !href.isEmpty()) {
        URI resolved = URI.createURI(href).resolve(baseUri);
        result.add(resolved.toString());
      }
    }
    return result;
  }

  /**
   * Collects href values from a space-separated attribute on a DOM element.
   *
   * <p>The Vitruvius correspondence XMI format stores {@code leftEObjects} and {@code
   * rightEObjects} as space-separated attribute values (one URI per token), not as child elements.
   */
  private List<String> collectHrefsFromAttr(Element parent, String attrName, URI baseUri) {
    List<String> result = new ArrayList<>();
    String attrValue = parent.getAttribute(attrName);
    if (attrValue == null || attrValue.isEmpty()) {
      return result;
    }
    for (String token : attrValue.trim().split("\\s+")) {
      if (!token.isEmpty()) {
        URI resolved = URI.createURI(token).resolve(baseUri);
        result.add(resolved.toString());
      }
    }
    return result;
  }

  @Override
  public Set<EObject> getCorrespondingObjects(EObject source) {
    // Resolve the source object's absolute URI (file URI + fragment)
    URI sourceUri = EcoreUtil.getURI(source);
    if (sourceUri == null) return Collections.emptySet();
    String sourceUriStr = sourceUri.toString();

    Set<String> correspondingUris = correspondenceUriMap.get(sourceUriStr);
    if (correspondingUris == null || correspondingUris.isEmpty()) return Collections.emptySet();

    Set<EObject> result = new LinkedHashSet<>();
    for (String targetUriStr : correspondingUris) {
      try {
        URI targetUri = URI.createURI(targetUriStr);
        EObject target = resourceSet.getEObject(targetUri, false);
        if (target != null && !target.eIsProxy()) {
          result.add(target);
        }
      } catch (Exception e) {
        // unresolvable reference — skip
      }
    }
    return result;
  }

  @Override
  public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
    URI uri1 = EcoreUtil.getURI(obj1);
    URI uri2 = EcoreUtil.getURI(obj2);
    if (uri1 == null || uri2 == null) return false;
    String key = uri1.toString() + "|" + uri2.toString();
    Set<String> tags = correspondenceTagMap.get(key);
    return tags != null && tags.contains(tag);
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
