package tools.vitruv.dsls.reactions.tests;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import edu.kit.ipd.sdq.activextendannotations.Lazy;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Delegate;
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
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.TestView;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public abstract class ReactionsExecutionTest implements TestView {
  @Delegate
  private TestView testView;

  private Path compilationDir;

  private TestReactionsCompiler.Factory factory;

  @Lazy
  private TestReactionsCompiler _compiler;

  protected abstract TestReactionsCompiler createCompiler(final TestReactionsCompiler.Factory factory);

  @BeforeAll
  public void acquireCompilationTargetDir(@TestProject(variant = "reactions compilation") final Path compilationDir) {
    this.compilationDir = compilationDir;
  }

  @BeforeEach
  public void prepareTestView(@TestProject final Path testProjectPath) {
    this.testView = ChangePublishingTestView.createDefaultChangePublishingTestView(testProjectPath, this.getChangePropagationSpecifications());
  }

  @Inject
  public TestReactionsCompiler.Factory setCompilerFactory(final TestReactionsCompiler.Factory factory) {
    return this.factory = factory;
  }

  @AfterEach
  public void closeTestView() {
    try {
      this.testView.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return this.getCompiler().getChangePropagationSpecifications();
  }

  public void close() throws Exception {
    this.testView.close();
  }

  public <T extends EObject> T from(final Class<T> arg0, final Path arg1) {
    return this.testView.from(arg0, arg1);
  }

  public <T extends EObject> T from(final Class<T> arg0, final Resource arg1) {
    return this.testView.from(arg0, arg1);
  }

  public <T extends EObject> T from(final Class<T> arg0, final URI arg1) {
    return this.testView.from(arg0, arg1);
  }

  public URI getUri(final Path arg0) {
    return this.testView.getUri(arg0);
  }

  public TestUserInteraction getUserInteraction() {
    return this.testView.getUserInteraction();
  }

  public void moveTo(final Resource arg0, final Path arg1) {
    this.testView.moveTo(arg0, arg1);
  }

  public void moveTo(final Resource arg0, final URI arg1) {
    this.testView.moveTo(arg0, arg1);
  }

  public <T extends Notifier> List<PropagatedChange> propagate(final T arg0, final Consumer<T> arg1) {
    return this.testView.propagate(arg0, arg1);
  }

  public <T extends Notifier> T record(final T arg0, final Consumer<T> arg1) {
    return this.testView.record(arg0, arg1);
  }

  public Resource resourceAt(final Path arg0) {
    return this.testView.resourceAt(arg0);
  }

  public Resource resourceAt(final URI arg0) {
    return this.testView.resourceAt(arg0);
  }

  private boolean _compiler_isInitialised = false;

  private TestReactionsCompiler _compiler_initialise() {
    final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> _function = (TestReactionsCompiler.TestReactionsCompilerParameters it) -> {
      it.setReactionsOwner(this);
      it.setCompilationProjectDir(Preconditions.<Path>checkNotNull(this.compilationDir, "The compilation directory was not acquired yet!"));
    };
    TestReactionsCompiler _createCompiler = this.createCompiler(
      Preconditions.<TestReactionsCompiler.Factory>checkNotNull(this.factory, "The compiler factory was not injected yet!").setParameters(_function));
    return _createCompiler;
  }

  public TestReactionsCompiler getCompiler() {
    if (!_compiler_isInitialised) {
    	try {
    		_compiler = _compiler_initialise();
    	} finally {
    		_compiler_isInitialised = true;
    	}
    }
    return _compiler;
  }
}
