/* ******************************************************************************
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

package tools.vitruv.dsls.vitruvocl.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tools.vitruv.dsls.vitruvocl.allinstances.AllInstancesEngine;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite;

/**
 * Manages metamodel and model instance loading for OCL constraint evaluation.
 *
 * <p>Provides metamodel-to-instance mapping required for constraint evaluation, supporting:
 *
 * <ul>
 *   <li>Loading Ecore metamodels with package name registration
 *   <li>Loading XMI model instances
 *   <li>Resolving qualified names like {@code spacecraft::Spacecraft} to EClass
 *   <li>Querying all instances of a given EClass (including subtypes), via an {@link
 *       tools.vitruv.dsls.vitruvocl.allinstances.AllInstancesEngine} — see {@link
 *       #getAllInstances} for details
 * </ul>
 *
 * <p>Implements {@link MetamodelWrapperInterface} for use across compilation phases, particularly
 * in the evaluation visitor where constraints access model elements.
 */
public class MetamodelWrapper implements MetamodelWrapperInterface {

  private static final String FEAT_LEFT_EOBJECTS = "leftEObjects";
  private static final String FEAT_RIGHT_EOBJECTS = "rightEObjects";

  private static final String REACTIONS_NS_URI =
      "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0";

  /** Default directory for test model files (legacy support). */
  private static Path testModelsPath = Path.of("test-models");

  /** Returns the current test models path. */
  public static Path getTestModelsPath() {
    return testModelsPath;
  }

  /** Sets the test models path (for test setup). */
  public static void setTestModelsPath(Path path) {
    testModelsPath = path;
  }

  /** Maps package names to loaded EPackages. */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /**
   * Every {@link EClass} ever passed to {@link #getAllInstances}, in first-seen order.
   *
   * <p>This is the runtime-discovered "call site" registration mentioned in the allInstances()
   * computation strategy of Wei &amp; Kolovos (BigMDE 2015): rather than statically pre-scanning
   * every constraint's AST for {@code allInstances()} calls, each type is registered the moment a
   * constraint actually asks for it (from {@link
   * tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor}, the sole caller of {@link
   * #getAllInstances}). This is exact, not an approximation: the set of types ever queried against
   * this wrapper equals the set of types constraints actually need, since there is no other way to
   * ask for instances than through this method.
   *
   * <p><b>Semantics note:</b> every prior call site is treated as {@code ALL_OF_KIND}
   * (subtype-inclusive), matching the old greedy implementation's behavior exactly — see {@link
   * #getAllInstances} for the detailed semantic-equivalence analysis.
   */
  private final Set<EClass> queriedAllInstancesTypes = new LinkedHashSet<>();

  /**
   * Lazily (re)built {@link AllInstancesEngine}; {@code null} means stale/not-yet-built.
   *
   * <p>Invalidated whenever {@link #queriedAllInstancesTypes} gains a new entry or a new metamodel
   * package is (un)registered — both change the metamodel-level pruning decision the engine's
   * cache configuration is based on. Per {@link AllInstancesEngine}'s own contract, this cached
   * engine may be reused across many {@link #getAllInstances} calls even as the instance model
   * changes, since {@code compute()} always performs a fresh traversal; only the pruning analysis
   * itself (query analysis + containment reachability) is cached here.
   */
  private AllInstancesEngine allInstancesEngine;

  /**
   * Maps each registered EObject to its source filename. Uses identity (not equals) so different.
   * objects from same file are tracked separately.
   */
  private final Map<EObject, String> instanceSourceFile = new IdentityHashMap<>();

  /**
   * Correspondences loaded from .correspondence files via DOM (not EMF). Maps an absolute EMF URI
   * string to the set of corresponding absolute URI strings.
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
   *
   * <ol>
   *   <li>Build a {@code filename → file URI} map from {@code ecorePaths} (e.g. {@code
   *       "stoex.ecore" → file:/C:/…/stoex.ecore}).
   *   <li>For each ecore file, grep its raw text for {@code platform:/plugin/…/name.ecore}.
   *   <li>If the referenced filename is found in the workspace, add the exact mapping to the EMF
   *       {@link org.eclipse.emf.ecore.resource.URIConverter} of this resource set.
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
          if (localUri != null) {
            uriMap.computeIfAbsent(platformUri, k -> localUri);
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
   * <p>The correspondence files produced by Vitruvius use {@code
   * xsi:type="correspondence_1:ReactionsCorrespondence"} (namespace URI {@code
   * http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0}). When the reactions
   * runtime is absent, EMF cannot resolve this type and silently drops all correspondence entries,
   * causing every {@code ~} operator to evaluate to {@code false}. Registering a minimal dynamic
   * EPackage containing {@code ReactionsCorrespondence} as a subtype of the known {@code
   * Correspondence} class is sufficient to let EMF load the entries correctly; the inherited {@code
   * leftEObjects} / {@code rightEObjects} features are resolved via the base class.
   */
  private static void ensureReactionsCorrespondenceRegistered() {
    if (EPackage.Registry.INSTANCE.containsKey(REACTIONS_NS_URI)) {
      return;
    }

    final String CORR_NS_URI = "http://vitruv.tools/metamodels/change/correspondence/1.0";
    EPackage corrPackage = (EPackage) EPackage.Registry.INSTANCE.get(CORR_NS_URI);
    if (corrPackage == null) {
      corrPackage = forceInitCorrespondencePackage();
    }
    if (corrPackage == null) {
      buildBaseCorrespondencePackage(CORR_NS_URI);
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
   * concrete subtype can be held without type-compatibility issues) and a concrete {@code
   * Correspondence} class carrying {@code leftEObjects}, {@code rightEObjects}, and {@code tag}.
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
    leftRef.setName(FEAT_LEFT_EOBJECTS);
    leftRef.setEType(EcorePackage.Literals.EOBJECT);
    leftRef.setUpperBound(-1);
    cls.getEStructuralFeatures().add(leftRef);

    EReference rightRef = EcoreFactory.eINSTANCE.createEReference();
    rightRef.setName(FEAT_RIGHT_EOBJECTS);
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
   * triggers {@code CorrespondencePackageImpl.init()}, which registers the package in {@link
   * EPackage.Registry#INSTANCE}.
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
        if (pkg != null) {
          return pkg;
        }
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
    // packageName may refer to a nested eSubpackage (e.g. "repository" inside pcm.ecore), so
    // don't blindly bind it to the root package here — registerPackageRecursively() resolves
    // the correct (sub-)package for each name found in the file.
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
   * Registers an {@link EPackage} and all its sub-packages recursively in both {@link
   * EPackage.Registry#INSTANCE} (by nsURI, for EMF XMI loading) and {@link #metamodelRegistry} (by
   * package name, for constraint type resolution).
   *
   * <p>Without this, model instance files that reference sub-package types (e.g. {@code
   * xmlns:tires="tires"}) cause a {@code PackageNotFoundException} during loading because only the
   * root package's nsURI is known to EMF.
   */
  private void registerPackageRecursively(EPackage pkg) {
    if (pkg.getNsURI() != null) {
      EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
    }
    // Also register by name so resolveEClass("tires", "Tire") works.
    // A genuinely new package changes what computeTraversableReferences() can see, so it
    // invalidates the cached engine (putIfAbsent returns null exactly when newly inserted).
    if (pkg.getName() != null && metamodelRegistry.putIfAbsent(pkg.getName(), pkg) == null) {
      allInstancesEngine = null;
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

    if (toRemove == null) {
      return; // file was never loaded — nothing to do
    }

    // Remove the EPackage from our registry and from the global EMF registry.
    if (!toRemove.getContents().isEmpty()
        && toRemove.getContents().get(0) instanceof EPackage pkg) {
      metamodelRegistry.remove(pkg.getName());
      if (pkg.getNsURI() != null) {
        EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
      }
      // The known metamodel package set shrank, invalidating any cached engine built from it.
      allInstancesEngine = null;
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

    for (EObject root : resource.getContents()) {
      registerSourceFileRecursively(root, filename);
      // Register root as context candidate (one entry per root EObject per file)
      contextObjects.add(root);
      instanceFilenames.add(filename);
    }
  }

  /**
   * Loads model instance from TEST_MODELS_PATH directory (legacy method).
   *
   * @param xmiFileName Filename relative to TEST_MODELS_PATH
   * @throws IOException If file cannot be read
   */
  public void loadModelInstance(String xmiFileName) throws IOException {
    loadModelInstance(testModelsPath.resolve(xmiFileName));
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
   * Internal recursive helper. All levels add to {@link #instanceSourceFile}. Only top-level roots
   * are registered in contextObjects/instanceFilenames.
   */
  private void registerSourceFileRecursively(EObject instance, String sourceFile) {
    instanceSourceFile.put(instance, sourceFile);

    for (EObject child : instance.eContents()) {
      registerSourceFileRecursively(child, sourceFile);
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
    return MetamodelWrapperInterface.resolveEClassInRegistry(
        metamodelRegistry, metamodelName, className);
  }

  /**
   * Returns all instances of given EClass, including subtype instances.
   *
   * <p><b>Implementation:</b> delegates to an {@link AllInstancesEngine} (Wei &amp; Kolovos,
   * "An Efficient Computation Strategy for allInstances()", BigMDE 2015) instead of indexing every
   * loaded object by its exact type up front. The engine's query analysis is built lazily from
   * {@link #queriedAllInstancesTypes} — the set of EClasses ever passed to this method — since this
   * method is the only place in the codebase constraints ever ask for instances through (see {@link
   * tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor}, its sole caller); the set of types
   * this method has ever been called with is therefore exactly the set of types constraints need,
   * with no separate AST pre-scan required.
   *
   * <p><b>Semantic equivalence with the previous greedy implementation:</b> the old code indexed
   * every loaded object by its exact {@link EClass} (via an unconditional {@code eContents()}
   * walk at load time) and, on every call, returned {@code index[eClass]} unioned with
   * {@code index[T]} for every recorded type {@code T} with {@code eClass.isSuperTypeOf(T)} — i.e.
   * subtype-inclusive ("allInstances()"/allOfKind semantics), never exact-type-only. This method
   * reproduces that exactly: every call site is registered as {@code ALL_OF_KIND}, and {@link
   * tools.vitruv.dsls.vitruvocl.allinstances.collect.SinglePassInstanceCollector} matches instances
   * via the same {@code kindType.isSuperTypeOf(actualType)} relation. The traversal root set is
   * {@link #contextObjects} — precisely the roots the old {@code instances} index was ever
   * populated from (via {@link #loadModelInstance}) — not {@link #getAllRootObjects()}, which would
   * also include metamodel (.ecore) and correspondence resources the old index never touched.
   *
   * <p><b>Engine lifecycle:</b> the engine's cache configuration and pruned containment-reference
   * set depend only on the metamodel and the accumulated query call sites, so they are cached and
   * reused across calls; only a newly-seen type or a newly-(un)registered metamodel package
   * invalidates and rebuilds them (see {@link #queriedAllInstancesTypes} and {@link
   * #allInstancesEngine}). The instance collection itself ({@code engine.compute(...)}) is always
   * performed fresh on every call, so changes to the loaded instance model between calls are always
   * reflected.
   *
   * @param eClass EClass to query
   * @return List of all direct and indirect instances
   */
  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    // Only the cheap call-site bookkeeping + lazy engine (re)build is synchronized; the
    // potentially expensive compute() traversal below runs lock-free so parallel constraint
    // evaluation (see ConstraintListEvaluator) isn't serialized on this call. This is safe because
    // AllInstancesEngine is immutable after construction (see its class doc) and obtainEngine()
    // guarantees the returned engine's call-site set already includes eClass — even if another
    // thread concurrently swaps in a newer engine for a different, newly-seen type.
    AllInstancesEngine engine = obtainEngine(eClass);
    Map<EClass, List<EObject>> instancesByType = engine.compute(contextObjects);
    return instancesByType.getOrDefault(eClass, Collections.emptyList());
  }

  /**
   * Registers {@code eClass} as a call site (if not already known) and returns an {@link
   * AllInstancesEngine} whose cache configuration covers it, rebuilding the cached engine first if
   * necessary.
   *
   * <p>Synchronized because {@link #queriedAllInstancesTypes} and {@link #allInstancesEngine} are
   * shared, mutable state read and written by every {@link #getAllInstances} call — including
   * concurrently from multiple threads when constraints are evaluated in parallel.
   */
  private synchronized AllInstancesEngine obtainEngine(EClass eClass) {
    if (queriedAllInstancesTypes.add(eClass)) {
      allInstancesEngine = null; // newly-seen call site -> cache configuration is stale
    }
    if (allInstancesEngine == null) {
      allInstancesEngine = buildAllInstancesEngine();
    }
    return allInstancesEngine;
  }

  /** Builds a fresh {@link AllInstancesEngine} from the currently known metamodels and call sites. */
  private AllInstancesEngine buildAllInstancesEngine() {
    List<AllInstancesCallSite> callSites = new ArrayList<>(queriedAllInstancesTypes.size());
    for (EClass type : queriedAllInstancesTypes) {
      callSites.add(new AllInstancesCallSite(type, AllInstancesCallSite.Kind.ALL_OF_KIND));
    }
    return new AllInstancesEngine(new LinkedHashSet<>(metamodelRegistry.values()), callSites);
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
    return MetamodelWrapperInterface.resolveEClassByShortNameInRegistry(
        metamodelRegistry, shortName);
  }

  @Override
  public EEnum resolveEEnum(String enumName) {
    return MetamodelWrapperInterface.resolveEEnumInRegistry(metamodelRegistry, enumName);
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
   * Loads a Vitruvius {@code .correspondence} file via DOM (not EMF) and populates {@link
   * #correspondenceUriMap}.
   *
   * <p>EMF cannot reliably load correspondence XMI files in standalone mode because the {@code
   * ReactionsCorrespondence} type is only available as a dynamic EClass with no Java backing class,
   * which triggers {@code IllegalValueException} during containment validation. Parsing the XML
   * directly with a DOM parser is simpler and avoids all type-checking issues.
   *
   * <p>Each {@code <correspondences>} element's {@code leftEObjects} and {@code rightEObjects}
   * child hrefs are resolved to absolute EMF URIs relative to the correspondence file location and
   * stored bidirectionally in {@link #correspondenceUriMap}.
   */
  @SuppressWarnings("java:S3776")
  private void loadCorrespondenceViaDOM(Path corrPath) {
    URI baseUri = URI.createFileURI(corrPath.toAbsolutePath().toString());
    try {
      DocumentBuilder builder = newSecureDocumentBuilder();
      builder.setErrorHandler(null); // suppress SAX warnings
      Document doc = builder.parse(corrPath.toFile());

      NodeList corrNodes = doc.getElementsByTagName("correspondences");
      for (int i = 0; i < corrNodes.getLength(); i++) {
        Element corrEl = (Element) corrNodes.item(i);
        String tag = corrEl.getAttribute("tag");
        // Support two XMI serialisation styles:
        //   (a) inline attributes:  <correspondences leftEObjects="..." rightEObjects="..."/>
        //   (b) child elements:     <correspondences><leftEObjects href="..."/></correspondences>
        List<String> lefts = collectHrefsFromAttr(corrEl, FEAT_LEFT_EOBJECTS, baseUri);
        if (lefts.isEmpty()) {
          lefts = collectHrefsFromChildElements(corrEl, FEAT_LEFT_EOBJECTS, baseUri);
        }
        List<String> rights = collectHrefsFromAttr(corrEl, FEAT_RIGHT_EOBJECTS, baseUri);
        if (rights.isEmpty()) {
          rights = collectHrefsFromChildElements(corrEl, FEAT_RIGHT_EOBJECTS, baseUri);
        }
        for (String l : lefts) {
          for (String r : rights) {
            correspondenceUriMap.computeIfAbsent(l, k -> new LinkedHashSet<>()).add(r);
            correspondenceUriMap.computeIfAbsent(r, k -> new LinkedHashSet<>()).add(l);
            if (tag != null && !tag.isEmpty()) {
              correspondenceTagMap
                  .computeIfAbsent(l + "|" + r, k -> new LinkedHashSet<>())
                  .add(tag);
              correspondenceTagMap
                  .computeIfAbsent(r + "|" + l, k -> new LinkedHashSet<>())
                  .add(tag);
            }
          }
        }
      }
    } catch (Exception e) {
      // malformed or unreadable correspondence file — skip silently
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
    if (sourceUri == null) {
      return Collections.emptySet();
    }
    String sourceUriStr = sourceUri.toString();

    Set<String> correspondingUris = correspondenceUriMap.get(sourceUriStr);
    if (correspondingUris == null || correspondingUris.isEmpty()) {
      return Collections.emptySet();
    }

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
    if (uri1 == null || uri2 == null) {
      return false;
    }
    String key = uri1.toString() + "|" + uri2.toString();
    Set<String> tags = correspondenceTagMap.get(key);
    return tags != null && tags.contains(tag);
  }

  private static DocumentBuilder newSecureDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    factory.setExpandEntityReferences(false);
    return factory.newDocumentBuilder();
  }
}
