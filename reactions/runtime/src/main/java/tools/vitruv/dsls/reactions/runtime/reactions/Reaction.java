package tools.vitruv.dsls.reactions.runtime.reactions;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * A {@link Reaction} can be executed in response to an {@link EChange} to 
 * maintain the consistency of a V-SUM.
 *
 * @see {@link ReactionsClassGenerator}
 * for the generation of Reaction instances from .reactions files
 */
public interface Reaction {
  /**
   * Executes the reaction on the V-SUM, as represented by execution state.
   *
   * @param change - {@link EChange} The change which triggered the reaction.
   * @param executionState - {@link ReactionExecutionState}
   */
  void execute(EChange<EObject> change, ReactionExecutionState executionState);

  /**
   * Returns the type of {@link EChange} that would trigger this reaction.
   *
   * @return {@link Class}
   */
  Class<?> getMatchingChangeType();

  /**
   * Returns the type of metamodel element 
   * ({@link EStructuralFeature} for feature changes, otherwise {@link EClass})
   * that would trigger this reaction.
   *
   * @return {@link ENamedElement}
   */
  // ENamedElement getMatchingMetamodelElement();
}
