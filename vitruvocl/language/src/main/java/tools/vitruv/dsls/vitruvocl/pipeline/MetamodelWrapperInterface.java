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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

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
   * * Resolves an EEnum by qualified name.
   *
   * @param enumName The qualified name of the enum (e.g., "UML.VisibilityKind")
   * @return The resolved EEnum, or null if not found
   */
  public EEnum resolveEEnum(String enumName);

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
   * Returns the source filename an instance was loaded from.
   *
   * @param instance the model instance
   * @return the source filename, or {@code null} if unknown
   */
  String getSourceFileForInstance(EObject instance);

  /**
   * Returns all objects corresponding to the given source object.
   *
   * @param source the source object
   * @return set of all corresponding objects; empty if none
   */
  Set<EObject> getCorrespondingObjects(EObject source);

  /**
   * Checks whether a correspondence between obj1 and obj2 carries the given tag.
   *
   * @param obj1 one side of the correspondence
   * @param obj2 other side of the correspondence
   * @param tag the required tag value
   * @return true if a correspondence with that tag exists between obj1 and obj2
   */
  boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag);

  /** Resolves a qualified name to its {@link EClass} using a name-keyed metamodel registry. */
  static EClass resolveEClassInRegistry(
      Map<String, EPackage> registry, String metamodelName, String className) {
    EPackage ePackage = registry.get(metamodelName);
    if (ePackage == null) {
      return null;
    }
    EClassifier classifier = ePackage.getEClassifier(className);
    return (classifier instanceof EClass ec) ? ec : null;
  }

  /** Resolves the first {@link EClass} matching an unqualified short name across a registry. */
  static EClass resolveEClassByShortNameInRegistry(
      Map<String, EPackage> registry, String shortName) {
    for (EPackage ePackage : registry.values()) {
      EClass found = resolveEClassByShortNameInPackage(ePackage, shortName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Searches an {@link EPackage} and its direct subpackages for an {@link EClass} by short name.
   */
  static EClass resolveEClassByShortNameInPackage(EPackage ePackage, String shortName) {
    EClassifier classifier = ePackage.getEClassifier(shortName);
    if (classifier instanceof EClass eClass) {
      return eClass;
    }
    for (EPackage subPkg : ePackage.getESubpackages()) {
      EClassifier subClassifier = subPkg.getEClassifier(shortName);
      if (subClassifier instanceof EClass eClass) {
        return eClass;
      }
    }
    return null;
  }

  /** Resolves the first {@link EEnum} matching a name across a registry and its subpackages. */
  static EEnum resolveEEnumInRegistry(Map<String, EPackage> registry, String enumName) {
    for (EPackage ePackage : registry.values()) {
      EEnum found = resolveEEnumInPackage(ePackage, enumName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /** Resolves an {@link EEnum} by name within an {@link EPackage} and its subpackages. */
  static EEnum resolveEEnumInPackage(EPackage ePackage, String enumName) {
    for (EClassifier classifier : ePackage.getEClassifiers()) {
      if (classifier instanceof EEnum eEnum && eEnum.getName().equals(enumName)) {
        return eEnum;
      }
    }
    for (EPackage subPackage : ePackage.getESubpackages()) {
      EEnum found = resolveEEnumInPackage(subPackage, enumName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
