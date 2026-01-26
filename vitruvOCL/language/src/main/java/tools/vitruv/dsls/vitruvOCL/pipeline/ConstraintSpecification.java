package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Constraint specification providing metamodel and instance access. Similar to Reactions'
 * ChangePropagationSpecification pattern.
 */
public interface ConstraintSpecification {

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
}