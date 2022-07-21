package tools.vitruv.dsls.reactions.runtime.routines

import java.io.IOException
import java.util.List
import java.util.function.Function
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper
import tools.vitruv.dsls.reactions.runtime.helper.ReactionsCorrespondenceHelper
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.runtime.structure.Loggable
import tools.vitruv.change.correspondence.CorrespondenceModel
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.propagation.ResourceAccess
import org.eclipse.emf.common.util.URI
import static com.google.common.base.Preconditions.checkState
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

abstract class AbstractRoutine extends CallHierarchyHaving implements Routine {
	val AbstractRoutinesFacade routinesFacade;
	extension val ReactionExecutionState executionState;

	new(AbstractRoutinesFacade routinesFacade, ReactionExecutionState executionState, CallHierarchyHaving calledBy) {
		super(calledBy);
		this.routinesFacade = routinesFacade;
		this.executionState = executionState;
	}

	// generic return type for convenience; the requested type has to match the type of the facade provided during construction
	protected def <T extends AbstractRoutinesFacade> T getRoutinesFacade() {
		return routinesFacade as T;
	}

	protected def ReactionExecutionState getExecutionState() {
		return executionState;
	}

	protected def UserInteractor getUserInteractor() {
		return executionState.userInteractor;
	}

	protected def CorrespondenceModel getCorrespondenceModel() {
		return executionState.correspondenceModel;
	}
	
	override boolean execute() {
		// capture the current routines facade execution state:
		val facadeExecutionState = routinesFacade._getExecutionState().capture();
		// set the reaction execution state and caller to use for all following routine calls:
		routinesFacade._getExecutionState.setExecutionState(executionState, this);

		try {
			// Exception handling could be added here when productively used
			return executeRoutine();
		} finally {
			// restore the previously captured execution state of the facade:
			routinesFacade._getExecutionState().restore(facadeExecutionState);
		}
	}

	protected abstract def boolean executeRoutine() throws IOException;

	static class Match extends Loggable {
		protected final extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
			this.executionState = executionState
		}
		
		protected def <T extends EObject> List<T> getCorrespondingElements(
			EObject correspondenceSource,
			Class<T> elementClass,
			Function<T, Boolean> correspondencePreconditionMethod,
			String tag
		) {
			val retrievedElements = ReactionsCorrespondenceHelper.getCorrespondingModelElements(correspondenceSource,
				elementClass, tag, correspondencePreconditionMethod, correspondenceModel);
			return retrievedElements;
		}
	
		protected def <T extends EObject> T getCorrespondingElement(
			EObject correspondenceSource,
			Class<T> elementClass,
			Function<T, Boolean> correspondencePreconditionMethod,
			String tag,
			boolean asserted
		) {
			val retrievedElements = getCorrespondingElements(correspondenceSource,
				elementClass, correspondencePreconditionMethod, tag);
			checkState(retrievedElements.size <= 1 && (!asserted || retrievedElements.size == 1),
				"There were (%s) corresponding elements of type %s for: %s, which are: %s", 
				retrievedElements.size, elementClass.simpleName, correspondenceSource, retrievedElements)
			val retrievedElement = if (!retrievedElements.empty) retrievedElements.get(0) else null;
			return retrievedElement;
		}
	}
	
	static class Create extends Loggable {
		protected final extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
			this.executionState = executionState
		}

		protected def void notifyObjectCreated(EObject createdObject) {
			executionState.changePropagationObservable.notifyObjectCreated(createdObject);
		}	
	}
	
	static class Update extends Loggable {
		protected final extension ReactionExecutionState executionState

		new(ReactionExecutionState executionState) {
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
					"correspondenceSource, element and persistancePath must be specified");
			}

			val _resourceURI = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedObject, persistencePath);
			persistAsRoot(elementToPersist, _resourceURI)
		}

		/**
		 * Persists the given object as root of the metadata model specified by
		 * the given metadata key.
		 * 
		 * @param rootObject The root object, not <code>null</code>.
		 * @param metadataKey The key uniquely identifying the metadata model.
		 * 		See {@link ResourceAccess#getMetadataModelURI}.
		 */
		protected def persistAsMetadataRoot(EObject rootObject, String... metadataKey) {
			if (rootObject === null) {
				throw new IllegalArgumentException("rootObject is null!");
			}
			val modelURI = resourceAccess.getMetadataModelURI(metadataKey)
			persistAsRoot(rootObject, modelURI)
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
			ReactionsCorrespondenceHelper.addCorrespondence(correspondenceModel, firstElement, secondElement, tag);
		}
		
		def void removeObject(EObject element) {
			if (element === null) {
				return
			}
			if (logger.debugEnabled) {
				logger.debug("Removing object " + element + " from container " + element.eContainer())
			}
			EcoreUtil.delete(element)
		}
		
		protected def void removeCorrespondenceBetween(EObject firstElement, EObject secondElement) {
			removeCorrespondenceBetween(firstElement, secondElement, null)
		}
		
		protected def void removeCorrespondenceBetween(EObject firstElement, EObject secondElement, String tag) {
			ReactionsCorrespondenceHelper.removeCorrespondencesBetweenElements(correspondenceModel, 
				firstElement, secondElement, tag)
		}
	}
}
