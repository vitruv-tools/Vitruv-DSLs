package tools.vitruv.dsls.commonalities.ui.util;

import java.util.concurrent.Callable;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.testing.AbstractQuickfixTest;
import org.eclipse.xtext.ui.testing.AbstractWorkbenchTest;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.BeforeAll;

/**
 * Fixes shortcomings of {@link AbstractQuickfixTest}.
 */
@SuppressWarnings("all")
public class BugFixedAbstractQuickfixTest extends AbstractQuickfixTest {
  @Override
  public XtextResource getXtextResource(final String model) {
    final XtextResource xtextResource = super.getXtextResource(model);
    StringConcatenation _builder = new StringConcatenation();
    String _projectName = this.getProjectName();
    _builder.append(_projectName);
    _builder.append("/");
    String _fileName = this.getFileName();
    _builder.append(_fileName);
    _builder.append(".");
    String _fileExtension = this.getFileExtension();
    _builder.append(_fileExtension);
    xtextResource.setURI(URI.createPlatformResourceURI(_builder.toString(), true));
    return xtextResource;
  }

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
}
