package tools.vitruv.dsls.reactions.tests.annotationtests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import allElementTypes.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;
import tools.vitruv.change.composite.propagation.ChangeableModelRepository;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecificationRepository;
import tools.vitruv.change.propagation.impl.DefaultChangeRecordingModelRepository;
import tools.vitruv.change.testutils.TestModelRepositoryFactory;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.change.testutils.views.ChangePublishingTestView;
import tools.vitruv.change.testutils.views.TestView;
import tools.vitruv.change.testutils.views.UriMode;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

/**
 * Tests that annotations of a propagated change are available in reactions and routines.
 */
class AnnotationTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL =
      TestMetamodelsPathFactory.allElementTypes("AnnotationTestsSource");

  private AnnotatingModelRepository annotatingModelRepository;

  @Override
  protected TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory) {
    return factory.createCompiler(compilerParameters -> {
      compilerParameters.setReactions(List.of("AnnotationTests.reactions"));
      compilerParameters.setChangePropagationSegments(List.of("annotationTests"));
    });
  }

  @Override
  protected TestView createTestView(
      Path testProjectPath,
      Iterable<ChangePropagationSpecification> changePropagationSpecifications) {
    try {
      TestUserInteraction userInteraction = new TestUserInteraction();
      DefaultChangeRecordingModelRepository modelRepository =
          new DefaultChangeRecordingModelRepository(null, Files.createTempDirectory(null));
      annotatingModelRepository = new AnnotatingModelRepository(
          TestModelRepositoryFactory.createTestChangeableModelRepository(
              modelRepository,
              new ChangePropagationSpecificationRepository(changePropagationSpecifications),
              userInteraction));
      return new ChangePublishingTestView(
          testProjectPath,
          userInteraction,
          UriMode.FILE_URIS,
          annotatingModelRepository,
          modelRepository.getUuidResolver(),
          modelRepository::getModelResource);
    } catch (IOException e) {
      throw Exceptions.sneakyThrow(e);
    }
  }

  @BeforeEach
  void createRoot() {
    propagate(resourceAt(SOURCE_MODEL), resource -> {
      Root root = AllElementTypesCreators.aet.Root();
      root.setId("AnnotationTestsSource");
      resource.getContents().add(root);
    });
    getMonitor().reset();
  }

  private static AnnotationTestsMonitor getMonitor() {
    return AnnotationTestsMonitor.instance;
  }

  private void changeRootId(String id) {
    propagate(from(Root.class, SOURCE_MODEL), root -> root.setId(id));
  }

  @Test
  void annotationIsAvailableInReactionAndRoutine() {
    Author author = new Author("alice");
    annotatingModelRepository.setNextAuthor(author);

    changeRootId("changedByAlice");

    assertThat(getMonitor().getAuthorInReaction(), is(Optional.of(author)));
    assertThat(getMonitor().getAuthorInRoutine(), is(Optional.of(author)));
  }

  @Test
  void missingAnnotationIsEmptyInReactionAndRoutine() {
    annotatingModelRepository.setNextAuthor(null);

    changeRootId("changedWithoutAuthor");

    assertThat(getMonitor().getAuthorInReaction(), is(Optional.<Author>empty()));
    assertThat(getMonitor().getAuthorInRoutine(), is(Optional.<Author>empty()));
  }

  @Test
  void annotationsAreTakenFromTheCurrentChange() {
    annotatingModelRepository.setNextAuthor(new Author("alice"));
    changeRootId("changedByAlice");
    Author bob = new Author("bob");
    annotatingModelRepository.setNextAuthor(bob);

    changeRootId("changedByBob");

    assertThat(getMonitor().getAuthorInReaction(), is(Optional.of(bob)));
    assertThat(getMonitor().getAuthorInRoutine(), is(Optional.of(bob)));
  }

  @Test
  void userInteractorIsAvailableInRoutine() {
    changeRootId("changed");

    assertThat(getMonitor().getUserInteractorInRoutine(), is(notNullValue()));
  }

  /**
   * Annotates every change with the configured {@link Author} before propagating it.
   */
  private static final class AnnotatingModelRepository implements ChangeableModelRepository {
    private final ChangeableModelRepository delegate;
    private Author nextAuthor;

    AnnotatingModelRepository(ChangeableModelRepository delegate) {
      this.delegate = delegate;
    }

    void setNextAuthor(Author nextAuthor) {
      this.nextAuthor = nextAuthor;
    }

    @Override
    public List<PropagatedChange> propagateChange(VitruviusChange<Uuid> change) {
      if (nextAuthor != null) {
        change.setAnnotation(Author.class, nextAuthor);
      }
      return delegate.propagateChange(change);
    }

    @Override
    public void addChangePropagationListener(ChangePropagationListener listener) {
      delegate.addChangePropagationListener(listener);
    }

    @Override
    public void removeChangePropagationListener(ChangePropagationListener listener) {
      delegate.removeChangePropagationListener(listener);
    }
  }
}
