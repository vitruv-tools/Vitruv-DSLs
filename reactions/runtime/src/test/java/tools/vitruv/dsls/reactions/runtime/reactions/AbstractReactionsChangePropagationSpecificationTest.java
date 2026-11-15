package tools.vitruv.dsls.reactions.runtime.reactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.EobjectFactory;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.composite.description.AnnotationSource;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.interaction.UserInteractionFactory;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.interaction.impl.PredefinedInteractionResultProviderImpl;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

class AbstractReactionsChangePropagationSpecificationTest {
  private record Tag(String value) {}

  private final EChange<EObject> change = EobjectFactory.eINSTANCE.createCreateEObject();
  private final UserInteractor userInteractor = UserInteractionFactory.instance
      .createUserInteractor(new PredefinedInteractionResultProviderImpl(null));
  private EditableCorrespondenceModelView<Correspondence> correspondenceModel;
  private RecordingSpecification specification;

  @BeforeEach
  void setUp() {
    correspondenceModel = CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
        CorrespondenceModelFactory.createPersistableCorrespondenceModel(
            URI.createURI("test:/correspondences")));
    specification = new RecordingSpecification();
    specification.setUserInteractor(userInteractor);
  }

  @Test
  void annotationsArePassedToAllReactions() {
    var tag = new Tag("hello");
    AnnotationSource annotations =
        AnnotationSource.of(type -> type == Tag.class ? Optional.of(tag) : Optional.empty());

    specification.propagateChange(change, annotations, correspondenceModel, null);

    assertEquals(2, specification.executionStates.size());
    for (var executionState : specification.executionStates) {
      assertSame(annotations, executionState.changeAnnotations());
      assertSame(tag, executionState.changeAnnotations().getAnnotation(Tag.class).orElseThrow());
    }
  }

  @Test
  void propagationWithoutAnnotationsUsesEmptyAnnotations() {
    specification.propagateChange(change, correspondenceModel, null);

    assertEquals(2, specification.executionStates.size());
    for (var executionState : specification.executionStates) {
      assertSame(AnnotationSource.EMPTY, executionState.changeAnnotations());
      assertTrue(executionState.changeAnnotations().getAnnotation(Tag.class).isEmpty());
    }
  }

  @Test
  void executionStateProvidesPropagationContext() {
    specification.propagateChange(change, AnnotationSource.EMPTY, correspondenceModel, null);

    assertEquals(List.of(change, change), specification.changes);
    for (var executionState : specification.executionStates) {
      assertSame(userInteractor, executionState.userInteractor());
      assertSame(specification, executionState.getChangePropagationObservable());
    }
  }

  /** A specification with two reactions that record the state they are executed with. */
  private static class RecordingSpecification
      extends AbstractReactionsChangePropagationSpecification {
    private final List<EChange<EObject>> changes = new ArrayList<>();
    private final List<ReactionExecutionState> executionStates = new ArrayList<>();

    RecordingSpecification() {
      super(MetamodelDescriptor.with("modelsA"), MetamodelDescriptor.with("modelsB"));
    }

    @Override
    protected void setup() {
      Reaction recordingReaction = (change, executionState) -> {
        changes.add(change);
        executionStates.add(executionState);
      };
      addReaction(recordingReaction);
      addReaction(recordingReaction);
    }
  }
}
