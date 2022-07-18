package tools.vitruv.dsls.reactions.runtime

import tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler.CorrespondenceFailException
import tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler.CorrespondenceFailDefaultDialog
import tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler.CorrespondenceFailCustomDialog
import tools.vitruv.dsls.reactions.runtime.correspondenceFailHandler.CorrespondenceFailDoNothing

class CorrespondenceFailHandlerFactory {
	static def CorrespondenceFailHandler createExceptionHandler() {
		return new CorrespondenceFailException();
	}
	
	static def CorrespondenceFailHandler createDefaultUserDialogHandler(boolean abortEffect) {
		return new CorrespondenceFailDefaultDialog(abortEffect);
	}
	
	static def CorrespondenceFailHandler createCustomUserDialogHandler(boolean abortEffect, String message) {
		return new CorrespondenceFailCustomDialog(abortEffect, message);	
	}
	
	static def CorrespondenceFailHandler createDoNothingHandler(boolean abortEffect) {
		return new CorrespondenceFailDoNothing(abortEffect);
	}
}