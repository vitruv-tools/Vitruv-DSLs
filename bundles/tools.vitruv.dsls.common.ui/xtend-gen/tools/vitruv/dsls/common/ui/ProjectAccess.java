package tools.vitruv.dsls.common.ui;

import edu.kit.ipd.sdq.activextendannotations.Lazy;
import edu.kit.ipd.sdq.activextendannotations.Visibility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class ProjectAccess {
  @Lazy(Visibility.PACKAGE)
  private static IBundleProjectService _projectBundleService;

  private ProjectAccess() {
  }

  public static IProject getEclipseProject(final IModificationContext context) {
    return ProjectAccess.getEclipseProject(context.getXtextDocument().getResourceURI());
  }

  public static IProject getEclipseProject(final EObject eObject) {
    return ProjectAccess.getEclipseProject(eObject.eResource().getURI());
  }

  public static IProject getEclipseProject(final URI uri) {
    String _platformString = uri.toPlatformString(true);
    return ProjectAccess.getEclipseProject(new Path(_platformString));
  }

  public static IProject getEclipseProject(final Path path) {
    return ResourcesPlugin.getWorkspace().getRoot().getFile(path).getProject();
  }

  /**
   * Returns the project as a Java project. There is no check on whether the project is actually a Java project,
   * use {@link #isJavaProject} for that!
   */
  public static IJavaProject getJavaProject(final IProject project) {
    return JavaCore.create(project);
  }

  /**
   * Returns the project as a plugin project. There is no check on whether the project is actually a plugin project,
   * use {@link #isPluginProject} for that!
   */
  public static IBundleProjectDescription getPluginProject(final IProject project) {
    try {
      return ProjectAccess.getProjectBundleService().getDescription(project);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static boolean getIsPluginProject(final IProject project) {
    try {
      return project.isNatureEnabled(IBundleProjectDescription.PLUGIN_NATURE);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static boolean getIsJavaProject(final IProject project) {
    try {
      return project.isNatureEnabled(JavaCore.NATURE_ID);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private static boolean _projectBundleService_isInitialised = false;

  private static IBundleProjectService _projectBundleService_initialise() {
    IBundleProjectService _acquireService = PDECore.getDefault().<IBundleProjectService>acquireService(IBundleProjectService.class);
    return _acquireService;
  }

  static IBundleProjectService getProjectBundleService() {
    if (!_projectBundleService_isInitialised) {
    	try {
    		_projectBundleService = _projectBundleService_initialise();
    	} finally {
    		_projectBundleService_isInitialised = true;
    	}
    }
    return _projectBundleService;
  }
}
