package tools.vitruv.dsls.commonalities.ui.setup;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IResourceUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.util.CommonalitiesLanguageConstants;

@Utility
@SuppressWarnings("all")
public final class CommonalitiesProjectSetup {
  private static final String COMPLIANCE_LEVEL = "17";

  public static IProject setupAsCommonalitiesProject(final IProject project) {
    try {
      project.open(null);
      IProjectDescription _description = project.getDescription();
      final Procedure1<IProjectDescription> _function = (IProjectDescription it) -> {
        it.setNatureIds(new String[] { JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID, IBundleProjectDescription.PLUGIN_NATURE });
      };
      IProjectDescription _doubleArrow = ObjectExtensions.<IProjectDescription>operator_doubleArrow(_description, _function);
      project.setDescription(_doubleArrow, null);
      CommonalitiesProjectSetup.createManifestMf(project);
      final IFolder sourcesFolder = IResourceUtil.createIfNotExists(project.getFolder(IProjectUtil.JAVA_SOURCE_FOLDER.toString()));
      final IFolder generatedSourcesFolder = IResourceUtil.createIfNotExists(project.getFolder(IProjectUtil.SOURCE_GEN_FOLDER.toString()));
      final IFolder javaProjectBinFolder = project.getFolder(IProjectUtil.JAVA_BIN_FOLDER.toString());
      IClasspathEntry _defaultJREContainerEntry = JREContainerProvider.getDefaultJREContainerEntry();
      IClasspathEntry _newContainerEntry = JavaCore.newContainerEntry(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH);
      IClasspathEntry _newSourceEntry = JavaCore.newSourceEntry(sourcesFolder.getFullPath());
      IClasspathEntry _newSourceEntry_1 = JavaCore.newSourceEntry(generatedSourcesFolder.getFullPath());
      final IClasspathEntry[] projectClasspath = new IClasspathEntry[] { _defaultJREContainerEntry, _newContainerEntry, _newSourceEntry, _newSourceEntry_1 };
      IJavaProject _create = JavaCore.create(project);
      final Procedure1<IJavaProject> _function_1 = (IJavaProject it) -> {
        try {
          Hashtable<String, String> _hashtable = new Hashtable<String, String>();
          final Procedure1<Hashtable<String, String>> _function_2 = (Hashtable<String, String> it_1) -> {
            it_1.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CommonalitiesProjectSetup.COMPLIANCE_LEVEL);
            it_1.put(JavaCore.COMPILER_COMPLIANCE, CommonalitiesProjectSetup.COMPLIANCE_LEVEL);
            it_1.put(JavaCore.COMPILER_SOURCE, CommonalitiesProjectSetup.COMPLIANCE_LEVEL);
          };
          Hashtable<String, String> _doubleArrow_1 = ObjectExtensions.<Hashtable<String, String>>operator_doubleArrow(_hashtable, _function_2);
          it.setOptions(_doubleArrow_1);
          it.setRawClasspath(projectClasspath, javaProjectBinFolder.getFullPath(), true, null);
          it.save(null, true);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      ObjectExtensions.<IJavaProject>operator_doubleArrow(_create, _function_1);
      return project;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private static IFile createManifestMf(final IProject project) {
    IFile _xblockexpression = null;
    {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Manifest-Version: 1.0");
      _builder.newLine();
      _builder.append("Bundle-ManifestVersion: 2");
      _builder.newLine();
      _builder.append("Bundle-Name: Commonalities Language Test Project");
      _builder.newLine();
      _builder.append("Bundle-Vendor: vitruv.tools");
      _builder.newLine();
      _builder.append("Bundle-Version: 3.0.1.qualifier");
      _builder.newLine();
      _builder.append("Bundle-SymbolicName: ");
      String _replace = project.getName().replace(" ", "-");
      _builder.append(_replace);
      _builder.append("; singleton:=true");
      _builder.newLineIfNotEmpty();
      _builder.append("Bundle-ActivationPolicy: lazy");
      _builder.newLine();
      _builder.append("Require-Bundle: ");
      _builder.append(CommonalitiesLanguageConstants.RUNTIME_BUNDLE);
      _builder.newLineIfNotEmpty();
      _builder.append("Bundle-RequiredExecutionEnvironment: JavaSE-");
      _builder.append(CommonalitiesProjectSetup.COMPLIANCE_LEVEL);
      _builder.newLineIfNotEmpty();
      final String mf = _builder.toString();
      _xblockexpression = CommonalitiesProjectSetup.createFile(IResourceUtil.createIfNotExists(project.getFolder("META-INF")), "MANIFEST.MF", mf);
    }
    return _xblockexpression;
  }

  public static void removeRequiredBundle(final IBundleProjectDescription pluginProject, final String requiredBundleName) {
    final IRequiredBundleDescription[] currentlyRequiredBundles = pluginProject.getRequiredBundles();
    if ((currentlyRequiredBundles == null)) {
      return;
    }
    int _length = currentlyRequiredBundles.length;
    boolean _tripleEquals = (_length == 1);
    if (_tripleEquals) {
      pluginProject.setRequiredBundles(null);
    } else {
      final Function1<Pair<Integer, IRequiredBundleDescription>, Boolean> _function = (Pair<Integer, IRequiredBundleDescription> it) -> {
        String _name = it.getValue().getName();
        return Boolean.valueOf(Objects.equal(_name, requiredBundleName));
      };
      Pair<Integer, IRequiredBundleDescription> _findFirst = IterableExtensions.<Pair<Integer, IRequiredBundleDescription>>findFirst(IterableExtensions.<IRequiredBundleDescription>indexed(((Iterable<? extends IRequiredBundleDescription>)Conversions.doWrapArray(currentlyRequiredBundles))), _function);
      Integer _key = null;
      if (_findFirst!=null) {
        _key=_findFirst.getKey();
      }
      Integer removeIndex = _key;
      if ((removeIndex == null)) {
        return;
      }
      int _length_1 = currentlyRequiredBundles.length;
      int _minus = (_length_1 - 1);
      final IRequiredBundleDescription[] newRequiredBundles = new IRequiredBundleDescription[_minus];
      if (((removeIndex).intValue() > 0)) {
        System.arraycopy(currentlyRequiredBundles, 0, newRequiredBundles, 0, (removeIndex).intValue());
      }
      int _length_2 = newRequiredBundles.length;
      boolean _lessThan = ((removeIndex).intValue() < _length_2);
      if (_lessThan) {
        int _length_3 = newRequiredBundles.length;
        int _minus_1 = (_length_3 - (removeIndex).intValue());
        System.arraycopy(currentlyRequiredBundles, ((removeIndex).intValue() + 1), newRequiredBundles, (removeIndex).intValue(), _minus_1);
      }
      pluginProject.setRequiredBundles(newRequiredBundles);
    }
  }

  public static void closeWelcomePage() {
    final Runnable _function = () -> {
      final IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
      final IIntroPart intro = introManager.getIntro();
      if ((intro != null)) {
        introManager.closeIntro(intro);
      }
    };
    Display.getDefault().asyncExec(_function);
  }

  public static void build(final IProject project, final String configName) {
    try {
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      project.build(IncrementalProjectBuilder.FULL_BUILD, configName, null, _nullProgressMonitor);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static void refresh(final IProject project) {
    try {
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      project.refreshLocal(IResource.DEPTH_INFINITE, _nullProgressMonitor);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static IFolder getSourceFolder(final IProject project) {
    return project.getFolder(IProjectUtil.SOURCE_GEN_FOLDER.toString());
  }

  public static IFolder getBinFolder(final IProject project) {
    return project.getFolder(IProjectUtil.JAVA_BIN_FOLDER.toString());
  }

  public static IFile createFile(final IContainer container, final String fileName, final InputStream content) {
    try {
      Path _path = new Path(fileName);
      final IFile file = container.getFile(_path);
      file.create(content, true, null);
      return file;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static IFile createFile(final IContainer container, final String fileName, final String content) {
    byte[] _bytes = content.getBytes();
    ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
    return CommonalitiesProjectSetup.createFile(container, fileName, _byteArrayInputStream);
  }

  public static java.nio.file.Path getPath(final IResource eclipseResource) {
    return eclipseResource.getRawLocation().toFile().toPath();
  }

  private CommonalitiesProjectSetup() {
    
  }
}
