package tools.vitruv.dsls.reactions.runtime.routines;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.function.Function;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.Loggable;

@SuppressWarnings("all")
public abstract class AbstractRoutine extends CallHierarchyHaving implements Routine {
  private static class CorrespondenceRetriever extends Loggable {
    private EditableCorrespondenceModelView<ReactionsCorrespondence> _correspondenceModel;

    public CorrespondenceRetriever(final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel) {
      this._correspondenceModel = correspondenceModel;
    }

    protected <T extends EObject> boolean hasCorrespondingElements(final EObject correspondenceSource, final Class<T> elementClass, final Function<T, Boolean> correspondencePreconditionMethod, final String tag) {
      boolean _isEmpty = IterableExtensions.isEmpty(this.<T>getCorrespondingElements(correspondenceSource, elementClass, correspondencePreconditionMethod, tag));
      return (!_isEmpty);
    }

    protected <T extends EObject> Iterable<T> getCorrespondingElements(final EObject correspondenceSource, final Class<T> expectedType, final Function<T, Boolean> correspondencePreconditionMethod, final String expectedTag) {
      Function<T, Boolean> _elvis = null;
      if (correspondencePreconditionMethod != null) {
        _elvis = correspondencePreconditionMethod;
      } else {
        final Function<T, Boolean> _function = (T it) -> {
          return Boolean.valueOf(true);
        };
        _elvis = _function;
      }
      final Function<T, Boolean> preconditionMethod = _elvis;
      final Iterable<T> correspondingObjects = Iterables.<T>filter(this._correspondenceModel.getCorrespondingEObjects(correspondenceSource, expectedTag), expectedType);
      return IterableExtensions.<T>filter(IterableExtensions.<T>filterNull(correspondingObjects), new Function1<T, Boolean>() {
          public Boolean apply(T arg0) {
            return preconditionMethod.apply(arg0);
          }
      });
    }

    protected <T extends EObject> T getCorrespondingElement(final EObject correspondenceSource, final Class<T> elementClass, final Function<T, Boolean> correspondencePreconditionMethod, final String tag, final boolean asserted) {
      final Iterable<T> retrievedElements = this.<T>getCorrespondingElements(correspondenceSource, elementClass, correspondencePreconditionMethod, tag);
      Preconditions.checkState(((IterableExtensions.size(retrievedElements) <= 1) && ((!asserted) || (IterableExtensions.size(retrievedElements) == 1))), 
        "There were (%s) corresponding elements of type %s for: %s, which are: %s", Integer.valueOf(IterableExtensions.size(retrievedElements)), 
        elementClass.getSimpleName(), correspondenceSource, retrievedElements);
      final T retrievedElement = IterableExtensions.<T>head(retrievedElements);
      return retrievedElement;
    }
  }

  public static class Match extends AbstractRoutine.CorrespondenceRetriever {
    @Extension
    protected final ReactionExecutionState executionState;

    public Match(final ReactionExecutionState executionState) {
      super(executionState.getCorrespondenceModel());
      this.executionState = executionState;
    }
  }

  public static class Create extends Loggable {
    @Extension
    protected final ReactionExecutionState executionState;

    public Create(final ReactionExecutionState executionState) {
      this.executionState = executionState;
    }

    protected <T extends EObject> T createObject(final Function0<? extends T> creator) {
      final T createdObject = creator.apply();
      this.notifyObjectCreated(createdObject);
      return createdObject;
    }

    private void notifyObjectCreated(final EObject createdObject) {
      this.executionState.getChangePropagationObservable().notifyObjectCreated(createdObject);
    }
  }

  public static class Update extends AbstractRoutine.CorrespondenceRetriever {
    @Extension
    protected final ReactionExecutionState executionState;

    public Update(final ReactionExecutionState executionState) {
      super(executionState.getCorrespondenceModel());
      this.executionState = executionState;
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
    protected void persistProjectRelative(final EObject alreadyPersistedObject, final EObject elementToPersist, final String persistencePath) {
      if ((((alreadyPersistedObject == null) || (elementToPersist == null)) || (persistencePath == null))) {
        throw new IllegalArgumentException(
          "correspondenceSource, element and persistancePath must be specified");
      }
      final URI _resourceURI = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedObject, persistencePath);
      this.persistAsRoot(elementToPersist, _resourceURI);
    }

    private void persistAsRoot(final EObject rootObject, final URI uri) {
      this.getLogger().trace(((("Registered to persist root " + rootObject) + " in: ") + uri));
      Resource _eResource = rootObject.eResource();
      URI _uRI = null;
      if (_eResource!=null) {
        _uRI=_eResource.getURI();
      }
      boolean _tripleNotEquals = (_uRI != uri);
      if (_tripleNotEquals) {
        EcoreUtil.remove(rootObject);
        this.executionState.getResourceAccess().persistAsRoot(rootObject, uri);
      }
    }

    protected void addCorrespondenceBetween(final EObject firstElement, final EObject secondElement) {
      this.addCorrespondenceBetween(firstElement, secondElement, null);
    }

    protected void addCorrespondenceBetween(final EObject firstElement, final EObject secondElement, final String tag) {
      String _elvis = null;
      if (tag != null) {
        _elvis = tag;
      } else {
        _elvis = "";
      }
      this.executionState.getCorrespondenceModel().addCorrespondenceBetween(firstElement, secondElement, _elvis);
    }

    public void removeObject(final EObject element) {
      if ((element == null)) {
        return;
      }
      boolean _isDebugEnabled = this.getLogger().isDebugEnabled();
      if (_isDebugEnabled) {
        Logger _logger = this.getLogger();
        EObject _eContainer = element.eContainer();
        String _plus = ((("Removing object " + element) + " from container ") + _eContainer);
        _logger.debug(_plus);
      }
      EcoreUtil.remove(element);
    }

    protected void removeCorrespondenceBetween(final EObject firstElement, final EObject secondElement) {
      this.removeCorrespondenceBetween(firstElement, secondElement, null);
    }

    protected void removeCorrespondenceBetween(final EObject firstElement, final EObject secondElement, final String tag) {
      this.executionState.getCorrespondenceModel().removeCorrespondencesBetween(firstElement, secondElement, tag);
    }
  }

  private final AbstractRoutinesFacade routinesFacade;

  @Accessors(AccessorType.PROTECTED_GETTER)
  @Extension
  private final ReactionExecutionState executionState;

  public AbstractRoutine(final AbstractRoutinesFacade routinesFacade, final ReactionExecutionState executionState, final CallHierarchyHaving calledBy) {
    super(calledBy);
    this.routinesFacade = routinesFacade;
    this.executionState = executionState;
  }

  protected <T extends AbstractRoutinesFacade> T getRoutinesFacade() {
    return ((T) this.routinesFacade);
  }

  @Override
  public boolean execute() {
    try {
      this.routinesFacade._pushCaller(this);
      try {
        return this.executeRoutine();
      } finally {
        this.routinesFacade._dropLastCaller();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected abstract boolean executeRoutine() throws IOException;

  @Pure
  protected ReactionExecutionState getExecutionState() {
    return this.executionState;
  }
}
