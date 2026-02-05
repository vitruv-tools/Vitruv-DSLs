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
}