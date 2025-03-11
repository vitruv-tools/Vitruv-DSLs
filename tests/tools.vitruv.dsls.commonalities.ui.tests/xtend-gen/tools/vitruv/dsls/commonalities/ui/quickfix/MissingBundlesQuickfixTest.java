package tools.vitruv.dsls.commonalities.ui.quickfix;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolution;
import org.eclipse.xtext.ui.testing.AbstractQuickfixTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.common.ui.ProjectAccess;
import tools.vitruv.dsls.common.ui.validation.ProjectValidation;
import tools.vitruv.dsls.commonalities.ui.setup.CommonalitiesProjectSetup;
import tools.vitruv.dsls.commonalities.ui.tests.CommonalitiesLanguageUiInjectorProvider;
import tools.vitruv.dsls.commonalities.ui.util.BugFixedAbstractQuickfixTest;
import tools.vitruv.dsls.commonalities.util.CommonalitiesLanguageConstants;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;

@DisplayName("quick fixes for missing bundles")
@ExtendWith({ InjectionExtension.class, TestProjectManager.class })
@InjectWith(CommonalitiesLanguageUiInjectorProvider.class)
@SuppressWarnings("all")
public class MissingBundlesQuickfixTest extends BugFixedAbstractQuickfixTest {
  @Inject
  @Extension
  private XtextAssertions xtextAssertions;

  private Path projectLocation;

  @BeforeEach
  public void captureProjectLocation(@TestProject final Path projectLocation) {
    this.projectLocation = projectLocation;
  }

  @Test
  @DisplayName("fixes a missing runtime bundle dependency")
  public void fixMissingRuntimeBundle() {
    IProject _setupProject = this.setupProject();
    final Procedure1<IProject> _function = (IProject it) -> {
      IBundleProjectDescription _pluginProject = ProjectAccess.getPluginProject(it);
      final Procedure1<IBundleProjectDescription> _function_1 = (IBundleProjectDescription it_1) -> {
        try {
          CommonalitiesProjectSetup.removeRequiredBundle(it_1, CommonalitiesLanguageConstants.RUNTIME_BUNDLE);
          NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
          it_1.apply(_nullProgressMonitor);
          Thread.sleep(200);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      ObjectExtensions.<IBundleProjectDescription>operator_doubleArrow(_pluginProject, _function_1);
    };
    final IProject testProject = ObjectExtensions.<IProject>operator_doubleArrow(_setupProject, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {}");
    _builder.newLine();
    final String testCommonality = _builder.toString();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Add dependency on ‹");
    _builder_1.append(CommonalitiesLanguageConstants.RUNTIME_BUNDLE);
    _builder_1.append("›");
    AbstractQuickfixTest.Quickfix _quickfix = new AbstractQuickfixTest.Quickfix(_builder_1.toString(), null, testCommonality);
    this.testQuickfixesOn(testCommonality, 
      ProjectValidation.ErrorCodes.BUNDLE_MISSING_ON_CLASSPATH, _quickfix);
    List<IRequiredBundleDescription> _elvis = null;
    IRequiredBundleDescription[] _requiredBundles = ProjectAccess.getPluginProject(testProject).getRequiredBundles();
    List<IRequiredBundleDescription> _list = null;
    if (((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(_requiredBundles))!=null) {
      _list=IterableExtensions.<IRequiredBundleDescription>toList(((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(_requiredBundles)));
    }
    if (_list != null) {
      _elvis = _list;
    } else {
      List<IRequiredBundleDescription> _emptyList = CollectionLiterals.<IRequiredBundleDescription>emptyList();
      _elvis = _emptyList;
    }
    final Function1<IRequiredBundleDescription, String> _function_1 = (IRequiredBundleDescription it) -> {
      return it.getName();
    };
    final Set<String> requiredBundles = IterableExtensions.<String>toSet(ListExtensions.<IRequiredBundleDescription, String>map(_elvis, _function_1));
    MatcherAssert.<Set<String>>assertThat(requiredBundles, CoreMatchers.<Set<String>>is(Set.<String>of(CommonalitiesLanguageConstants.RUNTIME_BUNDLE)));
    IResourcesSetupUtil.waitForBuild();
    MatcherAssert.<IXtextDocument>assertThat(XtextAssertions.getCurrentlyOpenedXtextDocument(), this.xtextAssertions.hasNoValidationIssues());
  }

  @Test
  @DisplayName("fixes a missing domain bundle dependency")
  public void fixMissingDomainBundle() {
    final IProject testProject = this.setupProject();
    final String missingBundle = "tools.vitruv.testutils.metamodels";
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:Root");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String testCommonality = _builder.toString();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Add dependency on ‹");
    _builder_1.append(missingBundle);
    _builder_1.append("›");
    AbstractQuickfixTest.Quickfix _quickfix = new AbstractQuickfixTest.Quickfix(_builder_1.toString(), null, testCommonality);
    this.testQuickfixesOn(testCommonality, 
      ProjectValidation.ErrorCodes.BUNDLE_MISSING_ON_CLASSPATH, _quickfix);
    List<IRequiredBundleDescription> _elvis = null;
    IRequiredBundleDescription[] _requiredBundles = ProjectAccess.getPluginProject(testProject).getRequiredBundles();
    List<IRequiredBundleDescription> _list = null;
    if (((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(_requiredBundles))!=null) {
      _list=IterableExtensions.<IRequiredBundleDescription>toList(((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(_requiredBundles)));
    }
    if (_list != null) {
      _elvis = _list;
    } else {
      List<IRequiredBundleDescription> _emptyList = CollectionLiterals.<IRequiredBundleDescription>emptyList();
      _elvis = _emptyList;
    }
    final Function1<IRequiredBundleDescription, String> _function = (IRequiredBundleDescription it) -> {
      return it.getName();
    };
    final Set<String> requiredBundles = IterableExtensions.<String>toSet(ListExtensions.<IRequiredBundleDescription, String>map(_elvis, _function));
    MatcherAssert.<Set<String>>assertThat(requiredBundles, CoreMatchers.<Set<String>>is(Set.<String>of(CommonalitiesLanguageConstants.RUNTIME_BUNDLE, missingBundle)));
    IResourcesSetupUtil.waitForBuild();
    MatcherAssert.<IXtextDocument>assertThat(XtextAssertions.getCurrentlyOpenedXtextDocument(), this.xtextAssertions.hasNoValidationIssues());
  }

  @Test
  @DisplayName("does not offer a quick fix to add a bundle when the project is not a plugin project")
  public void notPluginProject() {
    IProject _createProjectAt = IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation);
    final Procedure1<IProject> _function = (IProject it) -> {
      IProjectUtil.configureAsJavaProject(it);
    };
    ObjectExtensions.<IProject>operator_doubleArrow(_createProjectAt, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:Root");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String testCommonality = _builder.toString();
    final Function1<IssueResolution, Boolean> _function_1 = (IssueResolution it) -> {
      String _label = it.getLabel();
      return Boolean.valueOf((!Objects.equal(_label, "Convert the project to a plugin project")));
    };
    MatcherAssert.<List<IssueResolution>>assertThat(IterableExtensions.<IssueResolution>toList(IterableExtensions.<IssueResolution>filter(this.getAllQuickfixes(testCommonality), _function_1)), CoreMatchers.<List<IssueResolution>>is(CollectionLiterals.<IssueResolution>emptyList()));
    MatcherAssert.<IXtextDocument>assertThat(XtextAssertions.getCurrentlyOpenedXtextDocument(), this.xtextAssertions.hasIssues(
      ProjectValidation.ErrorCodes.CANNOT_ACCESS_TYPE, 
      ProjectValidation.ErrorCodes.CANNOT_ACCESS_TYPE));
  }

  private Iterable<IssueResolution> getAllQuickfixes(final CharSequence code) {
    final IFile dslFile = this.dslFile(this.getProjectName(), this.getFileName(), this.getFileExtension(), code);
    this.project = dslFile.getProject();
    final XtextEditor editor = this.openInEditor(dslFile);
    final List<Issue> issues = this.getAllValidationIssues(editor.getDocument());
    final Function1<Issue, List<IssueResolution>> _function = (Issue it) -> {
      return this.issueResolutionProvider.getResolutions(it);
    };
    return IterableExtensions.<Issue, IssueResolution>flatMap(issues, _function);
  }

  private IProject setupProject() {
    return CommonalitiesProjectSetup.setupAsCommonalitiesProject(IProjectUtil.createProjectAt(this.getProjectName(), this.projectLocation));
  }

  @Override
  public String getProjectName() {
    return this.projectLocation.getFileName().toString();
  }
}
