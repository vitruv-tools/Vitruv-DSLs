package tools.vitruv.dsls.reactions.runtime.helper

import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.xbase.lib.Functions.Function1
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
import tools.vitruv.change.correspondence.CorrespondenceModel
import edu.kit.ipd.sdq.activextendannotations.Utility

@Utility
class ReactionsCorrespondenceHelper {
	static val Logger logger = Logger.getLogger(ReactionsCorrespondenceHelper)

	private static def getReactionsView(CorrespondenceModel correspondenceModel) {
		return correspondenceModel.getEditableView(ReactionsCorrespondenceModelViewFactory.instance)
	}

	static def removeCorrespondences(CorrespondenceModel correspondenceModel, EObject source, EObject target,
		String tag) {
		logger.trace("Removing correspondence between " + source + " and " + target + " with tag: " + tag)
		val correspondenceModelView = correspondenceModel.reactionsView
		correspondenceModelView.removeCorrespondencesBetween(#[source], #[target], tag)
	}

	static def ReactionsCorrespondence addCorrespondence(CorrespondenceModel correspondenceModel, EObject source,
		EObject target, String tag) {
		logger.trace("Adding correspondence between " + source + " and " + target + " with tag: " + tag)
		val correspondence = correspondenceModel.reactionsView.createAndAddCorrespondence(#[source], #[target])
		correspondence.tag = tag ?: ""
		return correspondence
	}

	static def <T> Iterable<T> getCorrespondingElements(CorrespondenceModel correspondenceModel, EObject sourceElement,
		Class<T> expectedType, String expectedTag) {
		if (sourceElement === null) {
			return #[]
		}
		return correspondenceModel.reactionsView.getCorrespondingEObjects(#[sourceElement], expectedTag).flatten.filter(
			expectedType)
	}

	static def <T> Iterable<T> getCorrespondingElements(CorrespondenceModel correspondenceModel, EObject sourceElement,
		Class<T> expectedType, String expectedTag, Function1<T, Boolean> preconditionMethod) {
		val correspondingObjects = correspondenceModel.getCorrespondingElements(sourceElement, expectedType,
			expectedTag)
		val nonNullPreconditionMethod = if(preconditionMethod !== null) preconditionMethod else [T input|true]
		return correspondingObjects.filterNull.filter(nonNullPreconditionMethod)
	}
}
