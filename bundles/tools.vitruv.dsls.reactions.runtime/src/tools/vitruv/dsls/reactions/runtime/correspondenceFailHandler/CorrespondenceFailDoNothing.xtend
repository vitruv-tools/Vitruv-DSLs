package tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler

import org.eclipse.emf.ecore.EObject
import tools.vitruv.change.interaction.UserInteractor

class CorrespondenceFailDoNothing extends AbstractCorrespondenceFailHandler {
	final boolean abortEffect;
	
	new(boolean abortEffect) {
		this.abortEffect = abortEffect;
	}
	
	override handle(Iterable<? extends EObject> foundObjects, EObject sourceElement, Class<?> expectedType, UserInteractor userInteractor) {
		logFail(foundObjects, sourceElement, expectedType);
		logger.debug("And nothing ist done");
		return abortEffect;
	}
}