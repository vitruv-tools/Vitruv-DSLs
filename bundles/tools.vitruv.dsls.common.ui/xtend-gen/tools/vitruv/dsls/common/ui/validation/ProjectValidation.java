package tools.vitruv.dsls.common.ui.validation;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.osgi.framework.FrameworkUtil;
import tools.vitruv.dsls.common.ValidationMessageAcceptorExtensions;
import tools.vitruv.dsls.common.ui.ProjectAccess;

@Utility
@SuppressWarnings("all")
public final class ProjectValidation {
  public static class ErrorCodes {
    public static final String PREFIX = "tools.vitruv.dsls.common.ui.validation.ProjectValidation.";

    public static final String BUNDLE_MISSING_ON_CLASSPATH = (ProjectValidation.ErrorCodes.PREFIX + "BUNDLE_MISSING_ON_CLASSPATH");

    public static final String CANNOT_ACCESS_TYPE = (ProjectValidation.ErrorCodes.PREFIX + "CANNOT_ACCESS_TYPE");

    public static final String NOT_A_JAVA_PROJECT = (ProjectValidation.ErrorCodes.PREFIX + "NOT_A_JAVA_PROJECT");

    public static final String NOT_A_PLUGIN_PROJECT = (ProjectValidation.ErrorCodes.PREFIX + "NOT_A_PLUGIN_PROJECT");
  }

  public static void checkIsJavaPluginProject(final ValidationMessageAcceptor acceptor, final EObject referenceObject) {
    ProjectValidation.checkIsJavaPluginProject(acceptor, referenceObject, null);
  }

  public static void checkIsJavaPluginProject(final ValidationMessageAcceptor acceptor, final EObject referenceObject, final EStructuralFeature messageTargetFeature) {
    final IProject project = ProjectAccess.getEclipseProject(referenceObject);
    boolean _isJavaProject = ProjectAccess.getIsJavaProject(project);
    boolean _not = (!_isJavaProject);
    if (_not) {
      ValidationMessageAcceptorExtensions.warning(acceptor, "The project is not a Java project", referenceObject, messageTargetFeature, 
        ProjectValidation.ErrorCodes.NOT_A_JAVA_PROJECT);
    }
    boolean _isPluginProject = ProjectAccess.getIsPluginProject(project);
    boolean _not_1 = (!_isPluginProject);
    if (_not_1) {
      ValidationMessageAcceptorExtensions.warning(acceptor, "The project is not an Eclipse plugin project", referenceObject, messageTargetFeature, 
        ProjectValidation.ErrorCodes.NOT_A_PLUGIN_PROJECT);
    }
  }

  public static void checkRuntimeProjectIsOnClasspath(final ValidationMessageAcceptor acceptor, final TypeReferences typeReferences, final Class<?> markerType, final EObject referenceObject) {
    ProjectValidation.checkRuntimeProjectIsOnClasspath(acceptor, typeReferences, markerType, referenceObject, null);
  }

  public static void checkRuntimeProjectIsOnClasspath(final ValidationMessageAcceptor acceptor, final TypeReferences typeReferences, final Class<?> markerType, final EObject referenceObject, final EStructuralFeature messageTargetFeature) {
    ProjectValidation.checkOnClasspath(acceptor, typeReferences, markerType, referenceObject, messageTargetFeature, 
      "The runtime bundle is not on the classpath");
  }

  public static void checkMetamodelProjectIsOnClasspath(final ValidationMessageAcceptor acceptor, final TypeReferences typeReferences, final EPackage requiredPackage, final EObject referenceObject) {
    ProjectValidation.checkMetamodelProjectIsOnClasspath(acceptor, typeReferences, requiredPackage, referenceObject, null);
  }

  public static void checkMetamodelProjectIsOnClasspath(final ValidationMessageAcceptor acceptor, final TypeReferences typeReferences, final EPackage requiredPackage, final EObject referenceObject, final EStructuralFeature messageTargetFeature) {
    Class<? extends EPackage> _class = requiredPackage.getClass();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The bundle providing EPackage \"");
    String _nsURI = requiredPackage.getNsURI();
    _builder.append(_nsURI);
    _builder.append("\" is not on the classpath");
    ProjectValidation.checkOnClasspath(acceptor, typeReferences, _class, referenceObject, messageTargetFeature, _builder.toString());
  }

  private static void checkOnClasspath(final ValidationMessageAcceptor acceptor, final TypeReferences typeReferences, final Class<?> searchedClass, final EObject referenceObject, final EStructuralFeature messageTargetFeature, final String message) {
    final IProject project = ProjectAccess.getEclipseProject(referenceObject);
    boolean _isJavaProject = ProjectAccess.getIsJavaProject(project);
    boolean _not = (!_isJavaProject);
    if (_not) {
      return;
    }
    try {
      JvmType _findDeclaredType = typeReferences.findDeclaredType(searchedClass, referenceObject);
      boolean _tripleEquals = (_findDeclaredType == null);
      if (_tripleEquals) {
        boolean _isPluginProject = ProjectAccess.getIsPluginProject(project);
        boolean _not_1 = (!_isPluginProject);
        if (_not_1) {
          ValidationMessageAcceptorExtensions.error(acceptor, message, referenceObject, messageTargetFeature, ProjectValidation.ErrorCodes.CANNOT_ACCESS_TYPE);
        } else {
          final String providingBundle = FrameworkUtil.getBundle(searchedClass).getSymbolicName();
          List<IRequiredBundleDescription> _elvis = null;
          IRequiredBundleDescription[] _requiredBundles = ProjectAccess.getPluginProject(project).getRequiredBundles();
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
          final List<IRequiredBundleDescription> currentlyRequiredBundles = _elvis;
          final Function1<IRequiredBundleDescription, Boolean> _function = (IRequiredBundleDescription it) -> {
            String _name = it.getName();
            return Boolean.valueOf(Objects.equal(_name, providingBundle));
          };
          boolean _exists = IterableExtensions.<IRequiredBundleDescription>exists(currentlyRequiredBundles, _function);
          if (_exists) {
            ValidationMessageAcceptorExtensions.error(acceptor, message, referenceObject, messageTargetFeature, ProjectValidation.ErrorCodes.CANNOT_ACCESS_TYPE);
          } else {
            ValidationMessageAcceptorExtensions.error(acceptor, message, referenceObject, messageTargetFeature, 
              ProjectValidation.ErrorCodes.BUNDLE_MISSING_ON_CLASSPATH, new String[] { providingBundle });
          }
        }
      }
    } catch (final Throwable _t) {
      if (_t instanceof CoreException) {
        final CoreException e = (CoreException)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Failed to search this project’s classpath. Please check that your project is set up correctly: ");
        String _message = e.getMessage();
        _builder.append(_message);
        ValidationMessageAcceptorExtensions.error(acceptor, _builder.toString(), referenceObject, messageTargetFeature);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  private ProjectValidation() {
    
  }
}
