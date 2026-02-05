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
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.VirtualModel;

/**
 * VSUM-based implementation of the metamodel wrapper interface for VitruvOCL constraint evaluation.
 *
 * <p>This wrapper provides access to metamodels and model instances through the Vitruvius Virtual
 * Single Underlying Model (VSUM). It automatically discovers and registers all metamodels from the
 * VSUM's view source models, enabling constraint evaluation across multiple metamodels without
 * explicit import declarations.
 *
 * <p>The wrapper serves as the bridge between VitruvOCL's compilation pipeline and the Vitruvius
 * framework, allowing constraints to resolve fully qualified metaclass names (e.g., {@code
 * spacemission::Spacecraft}) and retrieve all instances of a given metaclass from the VSUM.
 *
 * <p><b>Metamodel Discovery:</b> During initialization, the wrapper extracts all {@link EPackage}s
 * from the VSUM's resources and registers them both internally and in the global EMF package
 * registry. This enables automatic resolution of metamodel names used in VitruvOCL constraints.
 *
 * <p><b>Instance Retrieval:</b> The wrapper provides recursive traversal of all model resources in
 * the VSUM to collect instances of a given metaclass, including instances of subclasses (following
 * EMF's {@link EClass#isSuperTypeOf(EClass)} semantics).
 *
 * @see MetamodelWrapperInterface
 * @see VirtualModel
 * @see ViewSource
 */
public class VSUMWrapper implements MetamodelWrapperInterface {

  /** The Vitruvius virtual model providing access to all registered model resources. */
  private final VirtualModel vsum;

  /**
   * Internal registry mapping metamodel names (EPackage names) to their corresponding {@link
   * EPackage} instances.
   */
  private final Map<String, EPackage> metamodelRegistry = new HashMap<>();

  /**
   * Creates a new VSUM wrapper and initializes the metamodel registry.
   *
   * <p>During construction, the wrapper automatically discovers all metamodels from the VSUM's view
   * source models and registers them for constraint evaluation.
   *
   * @param vsum the Vitruvius virtual model to wrap
   * @throws NullPointerException if {@code vsum} is null
   */
  public VSUMWrapper(VirtualModel vsum) {
    this.vsum = vsum;
    loadMetamodelsFromVSUM();
  }

  /**
   * Discovers and registers all metamodels from the VSUM's view source models.
   *
   * <p>This method extracts all {@link EPackage}s from the VSUM's resources by:
   *
   * <ol>
   *   <li>Accessing the VSUM's view source models (resources)
   *   <li>Extracting the {@link EClass} of each root model element
   *   <li>Retrieving the containing {@link EPackage} for each {@link EClass}
   *   <li>Registering each unique package in both the internal registry and the global EMF package
   *       registry
   * </ol>
   *
   * <p>This automatic discovery eliminates the need for explicit metamodel import declarations in
   * VitruvOCL constraints.
   */
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

  /**
   * Resolves a fully qualified metaclass name to its corresponding {@link EClass}.
   *
   * <p>This method enables VitruvOCL constraints to reference metaclasses using fully qualified
   * names like {@code spacemission::Spacecraft} or {@code pfandmodel::Dose}.
   *
   * @param metamodelName the name of the metamodel (EPackage name), e.g., "spacemission"
   * @param className the name of the metaclass within the metamodel, e.g., "Spacecraft"
   * @return the resolved {@link EClass}, or {@code null} if the metamodel is not registered or the
   *     classifier does not exist or is not an {@link EClass}
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
   * <p>This method performs a recursive traversal of all model resources in the VSUM to collect
   * instances of the specified {@link EClass}. The method follows EMF's subtyping semantics: if the
   * requested {@link EClass} has subclasses, instances of those subclasses are also included in the
   * result.
   *
   * <p>This operation supports VitruvOCL's implicit allInstances semantics where a fully qualified
   * metaclass name like {@code spacemission::Spacecraft} automatically resolves to all spacecraft
   * instances in the VSUM.
   *
   * @param eClass the metaclass whose instances should be retrieved
   * @return an immutable list of all model elements that are instances of {@code eClass} or any of
   *     its subclasses; returns an empty list if no instances are found
   * @throws NullPointerException if {@code eClass} is null
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
   * Recursively collects all model elements contained within a root element.
   *
   * <p>This helper method traverses the containment hierarchy of an {@link EObject} to collect the
   * root element itself and all its direct and indirect children.
   *
   * @param root the root element to traverse
   * @return a mutable list containing {@code root} and all elements reachable via {@link
   *     EObject#eAllContents()}
   */
  private List<EObject> getAllContentsRecursive(EObject root) {
    List<EObject> result = new ArrayList<>();
    result.add(root);
    root.eAllContents().forEachRemaining(result::add);
    return result;
  }

  /**
   * Returns the names of all metamodels currently registered in this wrapper.
   *
   * <p>The returned set contains the EPackage names of all metamodels discovered during
   * initialization. These names can be used in fully qualified metaclass references in VitruvOCL
   * constraints.
   *
   * @return an unmodifiable set of metamodel names (EPackage names)
   */
  @Override
  public Set<String> getAvailableMetamodels() {
    return Collections.unmodifiableSet(metamodelRegistry.keySet());
  }

  @Override
  public String getInstanceNameByIndex(int index) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getInstanceNameByIndex'");
  }
}