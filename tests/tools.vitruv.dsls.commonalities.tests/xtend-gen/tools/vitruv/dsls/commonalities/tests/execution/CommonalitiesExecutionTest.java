package tools.vitruv.dsls.commonalities.tests.execution;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.TestView;

@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public abstract class CommonalitiesExecutionTest implements TestView {
  @Delegate
  private TestView testView;

  protected abstract Iterable<ChangePropagationSpecification> getChangePropagationSpecifications();

  @BeforeEach
  public void prepareTestView(@TestProject final Path testProjectPath) {
    this.testView = ChangePublishingTestView.createDefaultChangePublishingTestView(testProjectPath, this.getChangePropagationSpecifications());
  }

  @AfterEach
  public void closeTestView() {
    try {
      this.testView.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
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
}
