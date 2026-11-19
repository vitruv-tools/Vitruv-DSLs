package tools.vitruv.dsls.reactions.tests;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestLogging;
import tools.vitruv.change.testutils.TestProject;
import tools.vitruv.change.testutils.TestProjectManager;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.change.testutils.views.ChangePublishingTestView;
import tools.vitruv.change.testutils.views.TestView;

/**
 * Base class for all Reactions test cases.
 *
 * <p>Provides methods to compile the reactions for tests, and extends {@link TestView}
 * so that the compiled reactions can be executed on some set of resources.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({ TestLogging.class, TestProjectManager.class })
public abstract class ReactionsExecutionTest implements TestView {
  /**
   * Test view used as delegate.
   */
  private TestView testView;
  private Path compilationDir;
  private TestReactionsCompiler.Factory factory;

  /**
   * Lazily initialized compiler (see {@code compilerIsInitialized}).
   */
  private TestReactionsCompiler compiler;
  /**
   * Whether the compiler was attempted to initialize.
   * If compiler initialization fails for the first time,
   * assume a broken configuration and do not retry.
   */
  private boolean compilerIsInitialised = false;

  /**
   * Returns the {@link TestReactionsCompiler} used for this test.
   * If not configured, calls
   * {@link ReactionsExecutionTest#createCompiler(TestReactionsCompiler.Factory)} first.
   *
   * @return compiler {@link TestReactionsCompiler}
   */
  public TestReactionsCompiler getCompiler() {
    if (!compilerIsInitialised) {
      try {
        Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer = parameters -> {
          parameters.setReactionsOwner(this);
          parameters.setCompilationProjectDir(
              checkNotNull(compilationDir, "The compilation directory was not acquired yet!"));
        };
        compiler = createCompiler(
            checkNotNull(factory, "The compiler factory was not injected yet!")
                .setParameters(configurer)
        );
      } finally {
        compilerIsInitialised = true;
      }
    }
    return compiler;
  }

  /**
   * Sets up the {@code compiler} using the provided {@code factory}.
   *
   * <p>Involves the setting of Reactions files and segments to compile.
   *
   * @param factory {@link tools.vitruv.dsls.reactions.tests.TestReactionsCompiler.Factory}
   * @return {@link TestReactionsCompiler}
   */
  protected abstract TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory);

  /**
   * Sets the target directory for Reactions compilation.
   *
   * @param compilationDir {@link Path}
   */
  @BeforeAll
  public void acquireCompilationTargetDir(
      @TestProject(variant = "reactions compilation") Path compilationDir) {
    this.compilationDir = compilationDir;
  }

  /**
   * Creates a new test view that stores resources under {@code testProjectPath}
   * for each test case.
   * For the first test case, also compiles the reactions that are executed.
   *
   * @param testProjectPath {@link Path}
   */
  @BeforeEach
  public void prepareTestView(@TestProject Path testProjectPath) {
    this.testView = createTestView(testProjectPath, getChangePropagationSpecifications());
  }

  /**
   * Creates the {@link TestView} that is used for a test case. Subclasses can override this method
   * to use a customized view, for example to change how changes are propagated.
   *
   * @param testProjectPath {@link Path} under which the view stores its resources
   * @param changePropagationSpecifications the compiled {@link ChangePropagationSpecification}s
   * @return {@link TestView}
   */
  protected TestView createTestView(
      Path testProjectPath,
      Iterable<ChangePropagationSpecification> changePropagationSpecifications) {
    return ChangePublishingTestView.createDefaultChangePublishingTestView(
        testProjectPath,
        changePropagationSpecifications);
  }

  /**
   * Assigns and returns the {@link ReactionsExecutionTest#factory}.
   *
   * @param factory {@link tools.vitruv.dsls.reactions.tests.TestReactionsCompiler.Factory}
   * @return factory
   */
  @Inject
  public TestReactionsCompiler.Factory setCompilerFactory(
      final TestReactionsCompiler.Factory factory) {
    return this.factory = factory;
  }

  /**
   * Closes the {@link ReactionsExecutionTest#testView} at the end of each test case.
   */
  @AfterEach
  public void closeTestView() {
    try {
      testView.close();
    } catch (Throwable e) {
      throw Exceptions.sneakyThrow(e);
    }
  }

  private Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return getCompiler().getChangePropagationSpecifications();
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws Exception {
    testView.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends EObject> T from(Class<T> clazz, Path path) {
    return testView.from(clazz, path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends EObject> T from(Class<T> clazz, Resource resource) {
    return testView.from(clazz, resource);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends EObject> T from(Class<T> clazz, URI uri) {
    return testView.from(clazz, uri);
  }

  /**
   * {@inheritDoc}
   */
  public URI getUri(Path path) {
    return testView.getUri(path);
  }

  /**
   * {@inheritDoc}
   * Delegates to {@link ReactionsExecutionTest#testView}.
   */
  public TestUserInteraction getUserInteraction() {
    return testView.getUserInteraction();
  }

  /**
   * {@inheritDoc}
   * Delegates to {@link ReactionsExecutionTest#testView}.
   */
  public void moveTo(Resource resource, Path newViewRelativePath) {
    testView.moveTo(resource, newViewRelativePath);
  }

  /**
   * {@inheritDoc}
   */
  public void moveTo(Resource resource, URI newUri) {
    testView.moveTo(resource, newUri);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends Notifier> List<PropagatedChange> propagate(T notifier, Consumer<T> consumer) {
    return testView.propagate(notifier, consumer);
  }

  /**
   * {@inheritDoc}
   */
  public <T extends Notifier> T record(T notifier, Consumer<T> consumer) {
    return testView.record(notifier, consumer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Resource resourceAt(Path viewRelativePath) {
    return testView.resourceAt(viewRelativePath);
  }

  /**
   * {@inheritDoc}
   */
  public Resource resourceAt(URI modelUri) {
    return testView.resourceAt(modelUri);
  }
}
