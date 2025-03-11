package tools.vitruv.dsls.common.ui.quickfix;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.internal.ui.wizards.tools.ConvertProjectToPluginOperation;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;
import tools.vitruv.dsls.common.ui.PluginProjectExtensions;
import tools.vitruv.dsls.common.ui.ProjectAccess;
import tools.vitruv.dsls.common.ui.validation.ProjectValidation;

@Utility
@SuppressWarnings("all")
public final class ProjectQuickfix {
  /**
   * Adds a missing bundle to the project classpath. Applicable for {@link ProjectValidation.ErrorCodes#BUNDLE_MISSING_ON_CLASSPATH}.
   */
  public static void addBundleToProject(final Issue issue, final IssueResolutionAcceptor acceptor) {
    ProjectQuickfix.checkCode(issue, ProjectValidation.ErrorCodes.BUNDLE_MISSING_ON_CLASSPATH);
    final String requiredBundle = issue.getData()[0];
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Add dependency on ‹");
    _builder.append(requiredBundle);
    _builder.append("›");
    final IModification _function = (IModificationContext context) -> {
      final IBundleProjectDescription pluginProject = ProjectAccess.getPluginProject(ProjectAccess.getEclipseProject(context));
      PluginProjectExtensions.addRequiredBundle(pluginProject, requiredBundle);
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      pluginProject.apply(_nullProgressMonitor);
      Thread.sleep(200);
    };
    acceptor.accept(issue, _builder.toString(), null, null, _function);
  }

  /**
   * Configures the project to be a plugin project. Applicable for {@link ProjectValidation.ErrorCodes#NOT_A_PLUGIN_PROJECT}
   */
  public static void convertToPluginProject(final Issue issue, final IssueResolutionAcceptor acceptor) {
    ProjectQuickfix.checkCode(issue, ProjectValidation.ErrorCodes.NOT_A_PLUGIN_PROJECT);
    final IModification _function = (IModificationContext context) -> {
      IProject _eclipseProject = ProjectAccess.getEclipseProject(context);
      ConvertProjectToPluginOperation _convertProjectToPluginOperation = new ConvertProjectToPluginOperation(new IProject[] { _eclipseProject }, false);
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      _convertProjectToPluginOperation.run(_nullProgressMonitor);
    };
    acceptor.accept(issue, "Convert the project to a plugin project", null, null, _function);
  }

  /**
   * Configures the project to be a Java project. Applicable for {@link ProjectValidation.ErrorCodes#NOT_A_JAVA_PROJECT}
   */
  public static void convertToJavaProject(final Issue issue, final IssueResolutionAcceptor acceptor) {
    ProjectQuickfix.checkCode(issue, ProjectValidation.ErrorCodes.NOT_A_JAVA_PROJECT);
    final IModification _function = (IModificationContext context) -> {
      IProjectUtil.configureSrcGenFolder(IProjectUtil.configureAsJavaProject(ProjectAccess.getEclipseProject(context)));
    };
    acceptor.accept(issue, "Convert the project to a Java project", null, null, _function);
  }

  private static void checkCode(final Issue issue, final String expectedCode) {
    String _code = issue.getCode();
    boolean _equals = Objects.equal(_code, expectedCode);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("This quickfix is only applicable for issues with code ");
    _builder.append(expectedCode);
    _builder.append("!");
    Preconditions.checkArgument(_equals, _builder);
  }

  private ProjectQuickfix() {
    
  }
}
