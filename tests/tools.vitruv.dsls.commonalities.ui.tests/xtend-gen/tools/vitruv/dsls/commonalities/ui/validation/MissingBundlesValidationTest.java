package tools.vitruv.dsls.commonalities.ui.validation;

import com.google.inject.Inject;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import java.nio.file.Path;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.xbase.lib.Extension;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.commonalities.ui.quickfix.XtextAssertions;
import tools.vitruv.dsls.commonalities.ui.setup.CommonalitiesProjectSetup;
import tools.vitruv.dsls.commonalities.ui.tests.CommonalitiesLanguageUiInjectorProvider;
import tools.vitruv.dsls.commonalities.ui.util.BugFixedAbstractEditorTest;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;

@ExtendWith({ InjectionExtension.class, TestProjectManager.class })
@InjectWith(CommonalitiesLanguageUiInjectorProvider.class)
@DisplayName("missing bundles validation")
@SuppressWarnings("all")
public class MissingBundlesValidationTest extends BugFixedAbstractEditorTest {
  @Inject
  @Extension
  private XtextAssertions xtextAssertions;

  @Inject
  private FileExtensionProvider fileExtensionProvider;

  private Path projectLocation;

  @BeforeEach
  public void captureProjectLocation(@TestProject final Path projectLocation) {
    this.projectLocation = projectLocation;
  }

  @Test
  @DisplayName("does not add issues for missing bundles for commonality participations")
  public void commonalityParticipation() {
    final IProject project = CommonalitiesProjectSetup.setupAsCommonalitiesProject(IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation));
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("referenced.");
    String _fileExtension = this.getFileExtension();
    _builder.append(_fileExtension);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("concept test");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("commonality Referenced {}");
    _builder_1.newLine();
    CommonalitiesProjectSetup.createFile(project, _builder.toString(), _builder_1.toString());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("test.");
    String _fileExtension_1 = this.getFileExtension();
    _builder_2.append(_fileExtension_1);
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append("concept test");
    _builder_3.newLine();
    _builder_3.newLine();
    _builder_3.append("commonality Test {");
    _builder_3.newLine();
    _builder_3.append("\t");
    _builder_3.append("with test:Referenced");
    _builder_3.newLine();
    _builder_3.append("}");
    _builder_3.newLine();
    final IFile testFile = CommonalitiesProjectSetup.createFile(project, _builder_2.toString(), _builder_3.toString());
    IResourcesSetupUtil.waitForBuild();
    MatcherAssert.<IXtextDocument>assertThat(this.openEditor(testFile).getDocument(), this.xtextAssertions.hasNoValidationIssues());
  }

  public String getFileExtension() {
    return this.fileExtensionProvider.getPrimaryFileExtension();
  }

  public String getProjectName() {
    return this.projectLocation.getFileName().toString();
  }
}
