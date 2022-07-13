package tools.vitruv.dsls.reactions.runtime

import org.eclipse.emf.ecore.EObject
import tools.vitruv.change.interaction.UserInteractor

interface CorrespondenceFailHandler {
	/** 
	 * Returns whether the execution shall be continued or not 
	 */
	def boolean handle(Iterable<? extends EObject> foundObjects, EObject sourceElement, Class<?> expectedType, UserInteractor userInteractor);
}