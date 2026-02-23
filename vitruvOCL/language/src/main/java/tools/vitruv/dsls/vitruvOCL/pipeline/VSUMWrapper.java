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

import java.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.VirtualModel;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * VSUM-based implementation of the metamodel wrapper interface for VitruvOCL constraint evaluation.
 *
 * <p>This wrapper provides access to metamodels, model instances, and the correspondence model
 * through the Vitruvius Virtual Single Underlying Model (VSUM). It automatically discovers and
 * registers all metamodels from the VSUM's view source models, enabling constraint evaluation
 * across multiple metamodels without explicit import declarations.
 *
 * <p>The correspondence model is accessible via {@link #getCorrespondingObjects(EObject)} and
 * {@link #getCorrespondingObjects(EObject, Class)}, enabling evaluation of VitruvOCL's {@code ~}
 * (correspondence) operator for cross-metamodel consistency checking.
 *
 * @see MetamodelWrapperInterface
 * @see VirtualModel
 */
public class VSUMWrapper implements MetamodelWrapperInterface {

  /** The Vitruvius virtual model providing access to all registered model resources. */
  private final VirtualModel vsum;

  /**
   * The correspondence model view, used to resolve the {@code ~} operator in VitruvOCL constraints.
   */
  private final CorrespondenceModelView<Correspondence> correspondenceModel;

  /**
   * Internal registry mapping metamodel names (EPackage names) to their corresponding {@link
   * EPackage} instances.
   */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /**
   * Flat list of all root objects across all VSUM resources, built once at construction time.
   *
   * <p>Used by {@link #getAllRootObjects()} and {@link #getInstanceNameByIndex(int)}.
   */
  private final List<EObject> allRootObjects = new ArrayList<>();

  /**
   * Creates a new VSUM wrapper and initializes the metamodel registry and correspondence model.
   *
   * <p>The VSUM must be an {@link InternalVirtualModel} to allow access to the correspondence model
   * via {@code getCorrespondenceModel()}.
   *
   * @param vsum the Vitruvius virtual model to wrap, must be an {@link InternalVirtualModel}
   * @throws IllegalArgumentException if {@code vsum} is not an {@link InternalVirtualModel}
   */
  public VSUMWrapper(VirtualModel vsum) {
    if (!(vsum instanceof InternalVirtualModel)) {
      throw new IllegalArgumentException(
          "VSUMWrapper requires an InternalVirtualModel to access the correspondence model");
    }
    this.vsum = vsum;
    this.correspondenceModel = ((InternalVirtualModel) vsum).getCorrespondenceModel();
    loadMetamodelsFromVSUM();
  }

  /**
   * Discovers and registers all metamodels from the VSUM's view source models.
   *
   * <p>Also populates {@link #allRootObjects} for use by {@link #getAllRootObjects()} and {@link
   * #getInstanceNameByIndex(int)}.
   */
  private void loadMetamodelsFromVSUM() {
    Collection<Resource> resources = ((ViewSource) vsum).getViewSourceModels();

    for (Resource resource : resources) {
      for (EObject root : resource.getContents()) {
        allRootObjects.add(root);

        EPackage pkg = root.eClass().getEPackage();
        if (!metamodelRegistry.containsKey(pkg.getName())) {
          metamodelRegistry.put(pkg.getName(), pkg);
          EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
        }
      }
    }
  }

  /**
   * Resolves a fully qualified metaclass name to its corresponding {@link EClass}.
   *
   * @param metamodelName the name of the metamodel (EPackage name), e.g., "spacemission"
   * @param className the name of the metaclass within the metamodel, e.g., "Spacecraft"
   * @return the resolved {@link EClass}, or {@code null} if not found
   */
  @Override
  public EClass resolveEClass(String metamodelName, String className) {
    EPackage ePackage = metamodelRegistry.get(metamodelName);
    if (ePackage == null) return null;

    EClassifier classifier = ePackage.getEClassifier(className);
    return (classifier instanceof EClass) ? (EClass) classifier : null;
  }

  /**
   * Retrieves all instances of a given metaclass from the VSUM.
   *
   * <p>Follows EMF subtyping semantics — instances of subclasses are included.
   *
   * @param eClass the metaclass whose instances should be retrieved
   * @return list of all matching model elements; empty if none found
   */
  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    Collection<Resource> resources = ((ViewSource) vsum).getViewSourceModels();

    return resources.stream()
        .flatMap(r -> r.getContents().stream())
        .flatMap(root -> getAllContentsRecursive(root).stream())
        .filter(obj -> eClass.isSuperTypeOf(obj.eClass()))
        .toList();
  }

  /**
   * Returns the names of all metamodels currently registered in this wrapper.
   *
   * @return unmodifiable set of metamodel names (EPackage names)
   */
  @Override
  public Set<String> getAvailableMetamodels() {
    return Collections.unmodifiableSet(metamodelRegistry.keySet());
  }

  /**
   * Returns the source resource URI for the root object at the given index.
   *
   * @param index 0-based index into the flat list of all root objects
   * @return the last segment of the resource URI (filename), or {@code null} if out of bounds
   */
  @Override
  public String getInstanceNameByIndex(int index) {
    if (index < 0 || index >= allRootObjects.size()) {
      return null;
    }
    EObject root = allRootObjects.get(index);
    Resource resource = root.eResource();
    if (resource == null || resource.getURI() == null) {
      return null;
    }
    return resource.getURI().lastSegment();
  }

  /**
   * Returns all root objects from all loaded model resources in the VSUM.
   *
   * @return unmodifiable list of all root EObjects across all VSUM resources
   */
  @Override
  public List<EObject> getAllRootObjects() {
    return Collections.unmodifiableList(allRootObjects);
  }

  // ---------------------------------------------------------------------------
  // Correspondence model access (for the ~ operator)
  // ---------------------------------------------------------------------------

  /**
   * Returns all objects corresponding to the given source object.
   *
   * <p>Used to evaluate the VitruvOCL {@code ~} (correspondence) operator:
   *
   * <pre>
   * self~model2::Entity
   * </pre>
   *
   * @param source the source object to look up correspondences for
   * @return set of all corresponding objects; empty if none exist
   */
  public Set<EObject> getCorrespondingObjects(EObject source) {
    return correspondenceModel.getCorrespondingEObjects(source);
  }

  /**
   * Returns all objects corresponding to the given source object that are instances of the given
   * target type.
   *
   * <p>Filters the result of {@link #getCorrespondingObjects(EObject)} to only include objects
   * whose {@link EClass} is a subtype of {@code targetType}. This is what the fully qualified
   * {@code ~} operator uses:
   *
   * <pre>
   * self~model2::Entity  -- only returns corresponding Entity instances
   * </pre>
   *
   * @param source the source object to look up correspondences for
   * @param targetType the EClass to filter corresponding objects by
   * @return set of corresponding objects that are instances of {@code targetType}; empty if none
   */
  public Set<EObject> getCorrespondingObjects(EObject source, EClass targetType) {
    return correspondenceModel.getCorrespondingEObjects(source).stream()
        .filter(obj -> targetType.isSuperTypeOf(obj.eClass()))
        .collect(java.util.stream.Collectors.toSet());
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  private List<EObject> getAllContentsRecursive(EObject root) {
    List<EObject> result = new ArrayList<>();
    result.add(root);
    root.eAllContents().forEachRemaining(result::add);
    return result;
  }
}