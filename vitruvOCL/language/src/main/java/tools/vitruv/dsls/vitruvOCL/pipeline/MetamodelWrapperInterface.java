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

import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Constraint specification providing metamodel and instance access. Similar to Reactions'
 * ChangePropagationSpecification pattern.
 */
public interface MetamodelWrapperInterface {

  /**
   * Resolves an EClass by qualified name.
   *
   * @param metamodelName The metamodel name (e.g., "UML")
   * @param className The class name (e.g., "Class")
   * @return The resolved EClass, or null if not found
   */
  EClass resolveEClass(String metamodelName, String className);

  /**
   * Returns all instances of the given EClass.
   *
   * @param eClass The EClass to query
   * @return List of all instances
   */
  List<EObject> getAllInstances(EClass eClass);

  /**
   * Returns names of all available metamodels.
   *
   * @return Set of metamodel names
   */
  Set<String> getAvailableMetamodels();

  /**
   * Returns the source filename for an instance at the given index.
   *
   * @param index The instance index (0-based)
   * @return The filename (e.g., "spacecraft-atlas.spacemission"), or null if index out of bounds
   */
  String getInstanceNameByIndex(int index);

  /**
   * Returns all root objects from all loaded model resources.
   *
   * <p>This includes root objects from all metamodels and instances currently loaded in the
   * resource set. Used for accessing correspondence models and other cross-cutting model elements.
   *
   * @return List of all root EObjects from all loaded resources
   */
  List<EObject> getAllRootObjects();

  /**
   * Returns the context EObject at the given evaluation index.
   *
   * @param index The evaluation index (0-based, one per root context object)
   * @return The EObject at that index, or null if out of bounds
   */
  EObject getContextObjectByIndex(int index);

  /**
   * Resolves an EClass by its unqualified short name, searching across all loaded metamodels.
   *
   * <p>Used as a fallback when a type annotation in an OCL constraint uses only the class name
   * without the metamodel qualifier (e.g., {@code Coordinate} instead of {@code cad::Coordinate}).
   * The first match across all registered packages is returned; if the same short name exists in
   * multiple metamodels, the result is unspecified and a qualified name should be used instead.
   *
   * @param shortName unqualified class name (e.g., {@code "Coordinate"})
   * @return the matching EClass, or {@code null} if not found in any loaded metamodel
   */
  EClass resolveEClassByShortName(String shortName);

  /**
   * Returns the source filename for a specific EObject instance (by identity). More reliable than
   * index-based lookup.
   */
  public String getSourceFileForInstance(EObject instance);
}