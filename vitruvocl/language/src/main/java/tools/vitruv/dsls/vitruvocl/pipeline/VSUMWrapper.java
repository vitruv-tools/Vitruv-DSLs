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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
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
 * through the Vitruvius Virtual Single Underlying Model (VSUM). It discovers metamodels from the
 * global {@link EPackage.Registry}, which is populated by the VSUM during initialization — before
 * any model instances are created.
 *
 * <p>The correspondence model is accessible via {@link #getCorrespondingObjects(EObject)} and
 * {@link #getCorrespondingObjects(EObject, EClass)}, enabling evaluation of VitruvOCL's {@code ~}
 * (correspondence) operator for cross-metamodel consistency checking.
 *
 * @see MetamodelWrapperInterface
 * @see VirtualModel
 */
public class VsumWrapper implements MetamodelWrapperInterface {

  /** The Vitruvius virtual model providing access to all registered model resources. */
  private final VirtualModel vsum;

  /**
   * The correspondence model view, used to resolve the {@code ~} operator in VitruvOCL constraints.
   */
  private final CorrespondenceModelView<Correspondence> correspondenceModel;

  /**
   * Internal registry mapping metamodel names (EPackage names) to their corresponding {@link
   * EPackage} instances.
   *
   * <p>Populated from the global {@link EPackage.Registry} at construction time — the VSUM
   * registers all metamodels during {@code buildAndInitialize()}, before any instances exist.
   */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /**
   * Creates a new VSUM wrapper and initializes the metamodel registry and correspondence model.
   *
   * <p>The VSUM must be an {@link InternalVirtualModel} to allow access to the correspondence model
   * via {@code getCorrespondenceModel()}.
   *
   * <p>Metamodels are loaded from the global {@link EPackage.Registry} rather than from VSUM
   * resources directly, because the registry is already populated by the VSUM during initialization
   * — before any model instances are added.
   *
   * @param vsum the Vitruvius virtual model to wrap, must be an {@link InternalVirtualModel}
   * @throws IllegalArgumentException if {@code vsum} is not an {@link InternalVirtualModel}
   */
  public VsumWrapper(VirtualModel vsum) {
    if (!(vsum instanceof InternalVirtualModel)) {
      throw new IllegalArgumentException(
          "VsumWrapper requires an InternalVirtualModel to access the correspondence model");
    }
    this.vsum = vsum;
    this.correspondenceModel = ((InternalVirtualModel) vsum).getCorrespondenceModel();
    loadMetamodelsFromRegistry();
  }

  /**
   * Discovers and registers all metamodels from the global EMF {@link EPackage.Registry}.
   *
   * <p>The VSUM registers all metamodel packages during {@code buildAndInitialize()} — they are
   * available in the global registry even before any model instances are created. This approach
   * avoids the need to have instances in the VSUM at construction time.
   */
  private void loadMetamodelsFromRegistry() {
    EPackage.Registry.INSTANCE.forEach(
        (nsURI, value) -> {
          if (value instanceof EPackage pkg) {
            metamodelRegistry.put(pkg.getName(), pkg);
          }
        });
  }

  /**
   * Resolves a fully qualified metaclass name to its corresponding {@link EClass}.
   *
   * @param metamodelName the name of the metamodel (EPackage name), e.g., "model"
   * @param className the name of the metaclass within the metamodel, e.g., "Component"
   * @return the resolved {@link EClass}, or {@code null} if not found
   */
  @Override
  public EClass resolveEClass(String metamodelName, String className) {
    return MetamodelWrapperInterface.resolveEClassInRegistry(
        metamodelRegistry, metamodelName, className);
  }

  /**
   * Retrieves all instances of a given metaclass from the VSUM.
   *
   * <p>Always reads live from the VSUM's view source models to reflect the current state, including
   * instances added after construction. Follows EMF subtyping semantics — instances of subclasses
   * are included.
   *
   * @param eClass the metaclass whose instances should be retrieved
   * @return list of all matching model elements; empty if none found
   */
  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    return ((ViewSource) vsum)
        .getViewSourceModels().stream()
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
   * <p>Always reads live from the VSUM to reflect current state.
   *
   * @param index 0-based index into the flat list of all root objects
   * @return the last segment of the resource URI (filename), or {@code null} if out of bounds
   */
  @Override
  public String getInstanceNameByIndex(int index) {
    List<EObject> roots = getAllRootObjects();
    if  (index < 0 || index >= roots.size()) {
      return null;
    }
    EObject root = roots.get(index);
    Resource resource = root.eResource();
    if  (resource == null || resource.getURI() == null) {
      return null;
    }
    return resource.getURI().lastSegment();
  }

  /**
   * Returns all root objects from all loaded model resources in the VSUM.
   *
   * <p>Always reads live from the VSUM to reflect current state.
   *
   * @return list of all root EObjects across all VSUM resources
   */
  @Override
  public List<EObject> getAllRootObjects() {
    return ((ViewSource) vsum)
        .getViewSourceModels().stream().flatMap(r -> r.getContents().stream()).toList();
  }

  // ---------------------------------------------------------------------------
  // Correspondence model access (for the ~ operator)
  // ---------------------------------------------------------------------------

  /**
   * Returns all objects corresponding to the given source object.
   *
   * <p>Used to evaluate the VitruvOCL {@code ~} (correspondence) operator.
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
   * @param source the source object to look up correspondences for
   * @param targetType the EClass to filter corresponding objects by
   * @return set of corresponding objects that are instances of {@code targetType}; empty if none
   */
  public Set<EObject> getCorrespondingObjects(EObject source, EClass targetType) {
    return correspondenceModel.getCorrespondingEObjects(source).stream()
        .filter(obj -> targetType.isSuperTypeOf(obj.eClass()))
        .collect(Collectors.toSet());
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

  @Override
  public String getSourceFileForInstance(EObject instance) {
    if (instance == null) {
      return null;
    }
    Resource resource = instance.eResource();
    if (resource == null || resource.getURI() == null) {
      return null;
    }
    return resource.getURI().lastSegment();
  }

  @Override
  public EObject getContextObjectByIndex(int index) {
    List<EObject> roots = getAllRootObjects();
    if (index < 0 || index >= roots.size()) {
      return null;
    }
    return roots.get(index);
  }

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
   * Checks whether a correspondence between obj1 and obj2 carries the given tag.
   *
   * <p>The Vitruvius CorrespondenceModelView does not expose tags directly — tags are an internal
   * attribute of the underlying Correspondence EMF objects. We therefore iterate the raw
   * correspondences via the correspondence model's internal stream.
   *
   * <p>Bidirectional: obj1 may appear in leftEObjects or rightEObjects.
   *
   * @param obj1 one side of the correspondence
   * @param obj2 other side of the correspondence
   * @param tag the required tag value
   * @return true if a tagged correspondence with that value relates obj1 and obj2
   */
  @Override
  public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
    // getCorrespondingEObjects(obj1, tag) returns only objects linked via a
    // correspondence that carries exactly that tag
    Set<EObject> taggedCorrespondents = correspondenceModel.getCorrespondingEObjects(obj1, tag);
    if  (taggedCorrespondents.contains(obj2)) {
      return true;
    }
    // bidirectional
    return correspondenceModel.getCorrespondingEObjects(obj2, tag).contains(obj1);
  }
}
