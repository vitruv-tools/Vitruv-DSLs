package tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler

import tools.vitruv.dsls.reactions.runtime.structure.Loggable
import tools.vitruv.dsls.reactions.runtime.CorrespondenceFailHandler
import org.eclipse.emf.ecore.EObject

abstract class AbstractCorrespondenceFailHandler extends Loggable implements CorrespondenceFailHandler {
	def logFail(Iterable<? extends EObject> foundObjects, EObject sourceElement, Class<?> expectedType) {
		logger.debug("There were (" + foundObjects.size + ") corresponding elements of type " +
				expectedType.getSimpleName() + " for: " + sourceElement)
		for (obj : foundObjects) {
			logger.debug("    " + obj);
		}
	}
}