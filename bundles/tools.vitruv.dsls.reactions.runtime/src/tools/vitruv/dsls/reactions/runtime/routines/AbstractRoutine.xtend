package tools.vitruv.dsls.reactions.runtime.routines

import java.io.IOException
import java.util.function.Function
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.runtime.structure.Loggable
import org.eclipse.emf.common.util.URI
import static com.google.common.base.Preconditions.checkState
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import org.eclipse.xtend.lib.annotations.Accessors
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence

abstract class AbstractRoutine<T extends AbstractRoutinesFacade> extends CallHierarchyHaving implements Routine {
	val T routinesFacade
	@Accessors(PROTECTED_GETTER)
	extension val ReactionExecutionState executionState

	new(T routinesFacade, ReactionExecutionState executionState, CallHierarchyHaving calledBy) {
		super(calledBy)
		this.routinesFacade = routinesFacade
		this.executionState = executionState
	}

	// generic return type for convenience; the requested type has to match the type of the facade provided during construction
	protected def T getRoutinesFacade() {
		return routinesFacade
	}

	override boolean execute() {
		routinesFacade._pushCaller(this)

		try {
			return executeRoutine()
		} finally {
			routinesFacade._dropLastCaller()
		}
	}

	protected abstract def boolean executeRoutine() throws IOException

	private static class CorrespondenceRetriever extends Loggable {
		EditableCorrespondenceModelView<ReactionsCorrespondence> _correspondenceModel

		new(EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel) {
			this._correspondenceModel = correspondenceModel
		}

		protected def <T extends EObject> boolean hasCorrespondingElements(
			EObject correspondenceSource,
			Class<T> elementClass,
			Function<T, Boolean> correspondencePreconditionMethod,
			String tag
		) {
			return !getCorrespondingElements(correspondenceSource, elementClass, correspondencePreconditionMethod, tag).
				empty
		}

		protected def <T extends EObject> Iterable<T> getCorrespondingElements(
			EObject correspondenceSource,
			Class<T> expectedType,
			Function<T, Boolean> correspondencePreconditionMethod,
			String expectedTag
		) {
			val Function<T, Boolean> preconditionMethod = correspondencePreconditionMethod ?: [true]
			val correspondingObjects = _correspondenceModel.getCorrespondingEObjects(correspondenceSource, expectedTag).
				filter(expectedType)
			return correspondingObjects.filterNull.filter(preconditionMethod)
		}

		protected def <T extends EObject> T getCorrespondingElement(
			EObject correspondenceSource,
			Class<T> elementClass,
			Function<T, Boolean> correspondencePreconditionMethod,
			String tag,
			boolean asserted
		) {
			val retrievedElements = getCorrespondingElements(correspondenceSource, elementClass,
				correspondencePreconditionMethod, tag)
			checkState(retrievedElements.size <= 1 && (!asserted || retrievedElements.size == 1),
				"There were (%s) corresponding elements of type %s for: %s, which are: %s", retrievedElements.size,
				elementClass.simpleName, correspondenceSource, retrievedElements)
			val retrievedElement = retrievedElements.head
			return retrievedElement
		}

	}

	static class Match extends CorrespondenceRetriever {
		protected val extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
			super(executionState.correspondenceModel)
			this.executionState = executionState
		}

	}

	static class Create extends Loggable {
		protected val extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
			this.executionState = executionState
		}

		protected def <T extends EObject> T createObject(()=>T creator) {
			val createdObject = creator.apply
			createdObject.notifyObjectCreated
			return createdObject
		}

		private def void notifyObjectCreated(EObject createdObject) {
			executionState.changePropagationObservable.notifyObjectCreated(createdObject)
		}
	}

	static class Update extends CorrespondenceRetriever {
		protected val extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
			super(executionState.correspondenceModel)
			this.executionState = executionState
		}

		/**
		 * Persists a given {@link EObject} as root object in the {@link Resource} at the specified path, 
		 * relative to the project root folder.
		 * 
		 * @param alreadyPersistedObject -
		 * 		An object that was already persisted within the project (necessary for retrieving the project folder)
		 * @param elementToPersist -
		 * 		The element to be persisted
		 * @param persistencePath -
		 * 		The path relative to the project root folder at which the element shall be persisted,
		 * 		using "/" as separator char and including the file name with extension
		 */
		protected def persistProjectRelative(EObject alreadyPersistedObject, EObject elementToPersist,
			String persistencePath) {
			if (alreadyPersistedObject === null || elementToPersist === null || persistencePath === null) {
				throw new IllegalArgumentException(
					"correspondenceSource, element and persistancePath must be specified")
			}

			val _resourceURI = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedObject, persistencePath)
			persistAsRoot(elementToPersist, _resourceURI)
		}

		private def persistAsRoot(EObject rootObject, URI uri) {
			logger.trace("Registered to persist root " + rootObject + " in: " + uri)
			if (rootObject.eResource?.URI !== uri) {
				EcoreUtil.remove(rootObject)
				resourceAccess.persistAsRoot(rootObject, uri)
			}
		}

		protected def void addCorrespondenceBetween(EObject firstElement, EObject secondElement) {
			addCorrespondenceBetween(firstElement, secondElement, null)
		}

		protected def void addCorrespondenceBetween(EObject firstElement, EObject secondElement, String tag) {
			correspondenceModel.addCorrespondenceBetween(firstElement, secondElement, tag ?: "")
		}

		def void removeObject(EObject element) {
			if (element === null) {
				return
			}
			if (logger.debugEnabled) {
				logger.debug("Removing object " + element + " from container " + element.eContainer())
			}
			EcoreUtil.remove(element)
		}

		protected def void removeCorrespondenceBetween(EObject firstElement, EObject secondElement) {
			removeCorrespondenceBetween(firstElement, secondElement, null)
		}

		protected def void removeCorrespondenceBetween(EObject firstElement, EObject secondElement, String tag) {
			correspondenceModel.removeCorrespondencesBetween(firstElement, secondElement, tag)
		}
	}
}
