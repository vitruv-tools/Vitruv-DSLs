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

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({ TestLogging.class, TestProjectManager.class })

public abstract class ReactionsExecutionTest implements TestView {
  /**
   * Test view used as delegate
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

  protected abstract TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory);

  @BeforeAll
  public void acquireCompilationTargetDir(
      @TestProject(variant = "reactions compilation") Path compilationDir) {
    this.compilationDir = compilationDir;
  }

  @BeforeEach
  public void prepareTestView(@TestProject Path testProjectPath) {
    this.testView =
        ChangePublishingTestView.createDefaultChangePublishingTestView(testProjectPath, getChangePropagationSpecifications());
  }

  @Inject
  public TestReactionsCompiler.Factory setCompilerFactory(final TestReactionsCompiler.Factory factory) {
    return this.factory = factory;
  }

  @AfterEach
  public void closeTestView() {
    try {
      testView.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return getCompiler().getChangePropagationSpecifications();
  }

  public void close() throws Exception {
    testView.close();
  }

  @Override
  public <T extends EObject> T from(Class<T> clazz, Path path) {
    return testView.from(clazz, path);
  }

  @Override
  public <T extends EObject> T from(Class<T> clazz, Resource resource) {
    return testView.from(clazz, resource);
  }

  public <T extends EObject> T from(Class<T> clazz, URI uri) {
    return testView.from(clazz, uri);
  }

  public URI getUri(Path path) {
    return testView.getUri(path);
  }

  public TestUserInteraction getUserInteraction() {
    return testView.getUserInteraction();
  }

  public void moveTo(Resource resource, Path newViewRelativePath) {
    testView.moveTo(resource, newViewRelativePath);
  }

  public void moveTo(Resource resource, URI newUri) {
    testView.moveTo(resource, newUri);
  }

  public <T extends Notifier> List<PropagatedChange> propagate(T notifier, Consumer<T> consumer) {
    return testView.propagate(notifier, consumer);
  }

  public <T extends Notifier> T record(T notifier, Consumer<T> consumer) {
    return testView.record(notifier, consumer);
  }

  @Override
  public Resource resourceAt(Path viewRelativePath) {
    return testView.resourceAt(viewRelativePath);
  }

  public Resource resourceAt(URI modelUri) {
    return testView.resourceAt(modelUri);
  }
}
