package tools.vitruv.dsls.reactions.runtime.routines;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.Loggable;

/**
 * AbstractRoutine provides a base implementation of a {@link Routine}.
 */
public abstract class AbstractRoutine extends CallHierarchyHaving implements Routine {
  private final AbstractRoutinesFacade routinesFacade;

  @Extension
  @Getter(AccessLevel.PROTECTED) private final ReactionExecutionState executionState;

  /**
   * Creates a new AbstractRoutine.
   *
   * @param routinesFacade - {@link AbstractRoutinesFacade}
   * @param executionState - {@link ReactionExecutionState}
   * @param calledBy - {@link CallHierarchyHaving}
   */
  protected AbstractRoutine(
      AbstractRoutinesFacade routinesFacade,
      ReactionExecutionState executionState,
      CallHierarchyHaving calledBy) {
    super(calledBy);
    this.routinesFacade = routinesFacade;
    this.executionState = executionState;
  }

  /**
   * Generic return type for convenience.
   * The requested type has to match the type of the facade provided during construction.
   */
  protected <T extends AbstractRoutinesFacade> T getRoutinesFacade() {
    return (T) routinesFacade;
  }

  @Override
  public boolean execute() {
    routinesFacade.pushCaller(this);
    var success = false;

    try {
      success = executeRoutine();
    } catch (Exception e) {
      throw Exceptions.sneakyThrow(e);
    } finally {
      routinesFacade.dropLastCaller();
    }
    return success;
  }

  /**
   * Executes the routine and modifies the underlying models. May also persist the modified models.
   *
   * @return true if executing the routine succeeds
   * @throws IOException should saving the modified models fail
   */
  protected abstract boolean executeRoutine() throws IOException;

  /**
   * Routines use CorrespondenceRetriever objects to look up correspondences 
   * between two model elements.
   */
  protected static class CorrespondenceRetriever extends Loggable {
    private EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel;

    protected CorrespondenceRetriever(
        EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel) {
      this.correspondenceModel = correspondenceModel;
    }

    protected <T extends EObject> boolean hasCorrespondingElements(
        EObject correspondenceSource,
        Class<T> elementClass,
        Predicate<T> correspondencePreconditionMethod,
        String tag
    ) {
      return !getCorrespondingElements(
        correspondenceSource, elementClass, correspondencePreconditionMethod, tag).isEmpty();
    }

    protected <T extends EObject> Collection<T> getCorrespondingElements(
        EObject correspondenceSource,
        Class<T> expectedType,
        Predicate<T> correspondencePreconditionMethod,
        String expectedTag
    ) {
      final Predicate<T> preconditionMethod =
          correspondencePreconditionMethod != null
          ? correspondencePreconditionMethod 
          : anything -> true;

      return correspondenceModel
        .getCorrespondingEObjects(correspondenceSource, expectedTag)
        .stream()
        .filter(e -> e != null)
        .filter(expectedType::isInstance)
        .map(expectedType::cast)
        .filter(preconditionMethod::test)
        .toList();
    }

    protected <T extends EObject> T getCorrespondingElement(
        EObject correspondenceSource,
        Class<T> elementClass,
        Predicate<T> correspondencePreconditionMethod,
        String tag,
        boolean asserted
    ) {
      var retrievedElements = getCorrespondingElements(correspondenceSource, elementClass,
          correspondencePreconditionMethod, tag);
      checkState(retrievedElements.size() <= 1 && (!asserted || retrievedElements.size() == 1),
          "There were (%s) corresponding elements of type %s for: %s, which are: %s",
          retrievedElements.size(),
          elementClass.getSimpleName(), 
          correspondenceSource, 
          retrievedElements);

      return retrievedElements.isEmpty() ? null : retrievedElements.iterator().next();
    }

  }

  /**
   * Match directly implements {@link CorrespondenceRetriever} for finding matching model elements.
   * <code>match</code> blocks in routines are translated to implementations of Match.
   */
  public static class Match extends CorrespondenceRetriever {
    @Extension
    protected final ReactionExecutionState executionState;

    /**
     * Creates a new Match statement.
     *
     * @param executionState - {@link ReactionExecutionState}
     */
    protected Match(ReactionExecutionState executionState) {
      super(executionState.getCorrespondenceModel());
      this.executionState = executionState;
    }
  }

  /**
   * <code>Create</code> blocks in routines are translated to implementations of the Create class.
   */
  public static class Create extends Loggable {
    @Extension
    protected final ReactionExecutionState executionState;

    /**
     * Creates a new {@link Create} object.
     *
     * @param executionState - {@link ReactionExecutionState}
     */
    protected Create(ReactionExecutionState executionState) {
      this.executionState = executionState;
    }

    /**
     * Creates a new model element according to <code>creator</code>.
     *
     * @param <T> - The type of model element.
     * @param creator - {@link Supplier}
     * @return new T
     */
    protected <T extends EObject> T createObject(Supplier<T> creator) {
      var createdObject = creator.get();
      notifyObjectCreated(createdObject);
      return createdObject;
    }

    private void notifyObjectCreated(EObject createdObject) {
      executionState.changePropagationObservable().notifyObjectCreated(createdObject);
    }
  }

  /**
   * Update extends {@link CorrespondenceRetriever} for managing correspondences, 
   * i.e. adding and removing them.
   * Also, within Update blocks, model elements can be persisted or deleted.
   * 
   * <p><code>update</code> blocks in routines are translated to implementations of Update.
   */
  public static class Update extends CorrespondenceRetriever {
    @Extension
    protected final ReactionExecutionState executionState;

    /**
     * Creates a new Update statement.
     *
     * @param executionState - {@link ReactionExecutionState}
     */
    protected Update(ReactionExecutionState executionState) {
      super(executionState.getCorrespondenceModel());
      this.executionState = executionState;
    }

    /**
     * Persists a given {@link EObject} as root object in the {@link Resource} 
     * at the specified path, relative to the project root folder.
     *
     * @param alreadyPersistedObject - An object that was already persisted within the project
     *      (necessary for retrieving the project folder).
     * @param elementToPersist - The element to be persisted.
     * @param persistencePath -
     *      The path relative to the project root folder at which the element shall be persisted,
     *      using "/" as separator char and including the file name with extension.
     */
    protected void persistProjectRelative(EObject alreadyPersistedObject, EObject elementToPersist,
        String persistencePath) {
      if (alreadyPersistedObject == null || elementToPersist == null || persistencePath == null) {
        throw new IllegalArgumentException(
            "correspondenceSource, element and persistancePath must be specified");
      }

      final URI resourceURI = PersistenceHelper
          .getURIFromSourceProjectFolder(alreadyPersistedObject, persistencePath);
      persistAsRoot(elementToPersist, resourceURI);
    }

    private void persistAsRoot(EObject rootObject, URI uri) {
      getLogger().trace("Registered to persist root %s in %s", rootObject, uri);
      val existingURI = rootObject.eContainer() != null ? rootObject.eResource().getURI() : null;
      if (existingURI != uri) {
        EcoreUtil.remove(rootObject);
        this.executionState.getResourceAccess().persistAsRoot(rootObject, uri);
      }
    }

    /**
     * Adds a correspondence between firstElement and secondElement 
     * with the empty tag <code>""</code>.
     *
     * @param firstElement - {@link EObject}
     * @param secondElement - {@link EObject}
     * @param tag - String
     */
    protected void addCorrespondenceBetween(EObject firstElement, EObject secondElement) {
      addCorrespondenceBetween(firstElement, secondElement, null);
    }

    /**
     * Adds a correspondence between firstElement and secondElement 
     * with the given <code>tag</code>.
     *
     * @param firstElement - {@link EObject}
     * @param secondElement - {@link EObject}
     * @param tag - String
     */
    protected void addCorrespondenceBetween(
        EObject firstElement, EObject secondElement, String tag) {
      executionState
          .getCorrespondenceModel()
          .addCorrespondenceBetween(firstElement, secondElement, tag == null ? "" : tag);
    }

    /**
     * Removes <code>element</code> from its container.
     *
     * @param element - {@link EObject}
     */
    protected void removeObject(EObject element) {
      if (element == null) {
        return;
      }
      var logger = getLogger();
      if (logger.isDebugEnabled()) {
        logger.debug("Removing object %s from %s", element, element.eContainer());
      }
      EcoreUtil.remove(element);
    }

    /**
     * Removes all correspondences between firstElement and secondElement.
     *
     * @param firstElement - {@link EObject}
     * @param secondElement - {@link EObject}
     */
    protected void removeCorrespondenceBetween(EObject firstElement, EObject secondElement) {
      removeCorrespondenceBetween(firstElement, secondElement, null);
    }

    /**
     * Removes the correspondence between firstElement and secondElement 
     * with the given <code>tag</code>.
     *
     * @param firstElement - {@link EObject}
     * @param secondElement - {@link EObject}
     * @param tag - String
     */
    protected void removeCorrespondenceBetween(
        EObject firstElement, EObject secondElement, String tag) {
      executionState
          .getCorrespondenceModel()
          .removeCorrespondencesBetween(firstElement, secondElement, tag);
    }
  }
}
