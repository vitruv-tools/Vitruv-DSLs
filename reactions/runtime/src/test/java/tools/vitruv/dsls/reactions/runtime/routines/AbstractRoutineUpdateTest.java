package tools.vitruv.dsls.reactions.runtime.routines;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.composite.description.AnnotationSource;
import tools.vitruv.change.interaction.UserInteractionFactory;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.interaction.impl.PredefinedInteractionResultProviderImpl;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

class AbstractRoutineUpdateTest {
  private record Tag(String value) {}

  @Test
  void updateExposesExecutionStateAnnotationsAndUserInteractor() {
    var tag = new Tag("hello");
    AnnotationSource annotations =
        AnnotationSource.of(type -> type == Tag.class ? Optional.of(tag) : Optional.empty());
    UserInteractor userInteractor = UserInteractionFactory.instance
        .createUserInteractor(new PredefinedInteractionResultProviderImpl(null));
    var executionState = new ReactionExecutionState(userInteractor, null, null, null, annotations);

    var update = new AbstractRoutine.Update(executionState);

    assertSame(annotations, update.changeAnnotations());
    assertSame(tag, update.changeAnnotations().getAnnotation(Tag.class).orElseThrow());
    assertSame(userInteractor, update.userInteractor());
  }
}
