package tools.vitruv.dsls.commonalities.ui.util;

import java.util.concurrent.Callable;
import org.eclipse.core.resources.IFile;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.testing.AbstractEditorTest;
import org.eclipse.xtext.ui.testing.AbstractWorkbenchTest;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.BeforeAll;

/**
 * Fixes shortcomings of {@link AbstractEditorTest}.
 */
@SuppressWarnings("all")
public class BugFixedAbstractEditorTest extends AbstractEditorTest {
  @BeforeAll
  public static void prepareWorkbench() {
    final Runnable _function = () -> {
      try {
        AbstractWorkbenchTest.prepareWorkbench();
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    UIThread.runSync(_function);
  }

  @Override
  public void setUp() {
    final Runnable _function = () -> {
      try {
        super.setUp();
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    UIThread.runSync(_function);
  }

  @Override
  public void tearDown() {
    final Runnable _function = () -> {
      try {
        super.tearDown();
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    UIThread.runSync(_function);
  }

  @Override
  public XtextEditor openEditor(final IFile file) {
    final Callable<XtextEditor> _function = () -> {
      return super.openEditor(file);
    };
    return UIThread.<XtextEditor>runSync(_function);
  }

  @Override
  public void waitForEventProcessing() {
    final Runnable _function = () -> {
      super.waitForEventProcessing();
    };
    UIThread.runSync(_function);
  }
}
