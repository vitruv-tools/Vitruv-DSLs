package tools.vitruv.dsls.commonalities.ui.quickfix;

import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.testing.AbstractQuickfixTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.common.ui.ProjectAccess;
import tools.vitruv.dsls.common.ui.validation.ProjectValidation;
import tools.vitruv.dsls.commonalities.ui.tests.CommonalitiesLanguageUiInjectorProvider;
import tools.vitruv.dsls.commonalities.ui.util.BugFixedAbstractQuickfixTest;
import tools.vitruv.testutils.DisableAutoBuild;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;

@DisplayName("quick fixes for project natures")
@ExtendWith({ InjectionExtension.class, TestProjectManager.class, DisableAutoBuild.class })
@InjectWith(CommonalitiesLanguageUiInjectorProvider.class)
@SuppressWarnings("all")
public class ProjectConversionQuickfixTest extends BugFixedAbstractQuickfixTest {
  private Path projectLocation;

  private static final String testCommonality = new Function0<String>() {
    @Override
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("concept test");
      _builder.newLine();
      _builder.newLine();
      _builder.append("commonality Test {}");
      _builder.newLine();
      return _builder.toString();
    }
  }.apply();

  @BeforeEach
  public void captureTestProject(@TestProject final Path projectLocation) {
    this.projectLocation = projectLocation;
  }

  @Test
  @DisplayName("converts the plugin nature to a Java project")
  public void fixNotPluginProject() {
    IProject _createProjectAt = IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation);
    final Procedure1<IProject> _function = (IProject it) -> {
      IProjectUtil.configureAsJavaProject(it);
    };
    final IProject testProject = ObjectExtensions.<IProject>operator_doubleArrow(_createProjectAt, _function);
    AbstractQuickfixTest.Quickfix _quickfix = new AbstractQuickfixTest.Quickfix("Convert the project to a plugin project", null, ProjectConversionQuickfixTest.testCommonality);
    this.testQuickfixesOn(
      ProjectConversionQuickfixTest.testCommonality, 
      ProjectValidation.ErrorCodes.NOT_A_PLUGIN_PROJECT, _quickfix);
    IResourcesSetupUtil.waitForBuild();
    Assertions.assertTrue(ProjectAccess.getIsPluginProject(testProject), "is plugin project");
    Assertions.assertTrue(
      Files.isRegularFile(this.projectLocation.resolve("META-INF").resolve("MANIFEST.MF")), 
      "MANIFEST.MF exists");
    Assertions.assertTrue(
      Files.isRegularFile(this.projectLocation.resolve("build.properties")), 
      "build.properties exists");
  }

  @Test
  @DisplayName("adds the Java nature to a plugin project")
  public void fixNotJavaProject() {
    IProject _createProjectAt = IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation);
    final Procedure1<IProject> _function = (IProject it) -> {
      try {
        it.open(null);
        IProjectDescription _description = it.getDescription();
        final Procedure1<IProjectDescription> _function_1 = (IProjectDescription it_1) -> {
          it_1.setNatureIds(((String[])Conversions.unwrapArray(List.<String>of(IBundleProjectDescription.PLUGIN_NATURE, XtextProjectHelper.NATURE_ID), String.class)));
        };
        IProjectDescription _doubleArrow = ObjectExtensions.<IProjectDescription>operator_doubleArrow(_description, _function_1);
        it.setDescription(_doubleArrow, null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    final IProject testProject = ObjectExtensions.<IProject>operator_doubleArrow(_createProjectAt, _function);
    AbstractQuickfixTest.Quickfix _quickfix = new AbstractQuickfixTest.Quickfix("Convert the project to a Java project", null, ProjectConversionQuickfixTest.testCommonality);
    this.testQuickfixesOn(
      ProjectConversionQuickfixTest.testCommonality, 
      ProjectValidation.ErrorCodes.NOT_A_JAVA_PROJECT, _quickfix);
    IResourcesSetupUtil.waitForBuild();
    Assertions.assertTrue(ProjectAccess.getIsJavaProject(testProject), "is Java project");
  }

  @Test
  @DisplayName("adds the Java and the plugin nature to a plain project")
  public void fixNotPlainProject() {
    IProject _createProjectAt = IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation);
    final Procedure1<IProject> _function = (IProject it) -> {
      try {
        it.open(null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    final IProject testProject = ObjectExtensions.<IProject>operator_doubleArrow(_createProjectAt, _function);
    AbstractQuickfixTest.Quickfix _quickfix = new AbstractQuickfixTest.Quickfix("Convert the project to a Java project", null, ProjectConversionQuickfixTest.testCommonality);
    this.testQuickfixesOn(
      ProjectConversionQuickfixTest.testCommonality, 
      ProjectValidation.ErrorCodes.NOT_A_JAVA_PROJECT, _quickfix);
    IResourcesSetupUtil.waitForBuild();
    AbstractQuickfixTest.Quickfix _quickfix_1 = new AbstractQuickfixTest.Quickfix("Convert the project to a plugin project", null, ProjectConversionQuickfixTest.testCommonality);
    this.testQuickfixesOn(
      ProjectConversionQuickfixTest.testCommonality, 
      ProjectValidation.ErrorCodes.NOT_A_PLUGIN_PROJECT, _quickfix_1);
    IResourcesSetupUtil.waitForBuild();
    Assertions.assertTrue(ProjectAccess.getIsJavaProject(testProject), "is Java project");
    Assertions.assertTrue(ProjectAccess.getIsPluginProject(testProject), "is plugin project");
    Assertions.assertTrue(
      Files.isRegularFile(this.projectLocation.resolve("META-INF").resolve("MANIFEST.MF")), 
      "MANIFEST.MF exists");
    Assertions.assertTrue(
      Files.isRegularFile(this.projectLocation.resolve("build.properties")), 
      "build.properties exists");
  }

  @Override
  public String getProjectName() {
    return this.projectLocation.getFileName().toString();
  }

  @Override
  public XtextEditor openInEditor(final IFile dslFile) {
    try {
      return this.openEditor(dslFile);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        throw new RuntimeException(e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
