package tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler

import org.eclipse.emf.ecore.EObject
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality

class CorrespondenceFailCustomDialog extends AbstractCorrespondenceFailHandler {
	final boolean abortEffect;
	final String message;
	
	new(boolean abortEffect, String message) {
		this.abortEffect = abortEffect;
		this.message = message;
	}
	
	override handle(Iterable<? extends EObject> foundObjects, EObject sourceElement, Class<?> expectedType, UserInteractor userInteractor) {
		logFail(foundObjects, sourceElement, expectedType);
		logger.debug("Show user dialog with message: " + message);
		userInteractor.notificationDialogBuilder.message(message).windowModality(WindowModality.MODAL).startInteraction();
		return abortEffect;
	}
}