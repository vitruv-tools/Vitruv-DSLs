package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;

@Utility
@SuppressWarnings("all")
final class CommonalitiesLanguageElementExtension {
  /**
   * @return the container of the given type
   * @throws RuntimeException if no container of the given type is found
   */
  public static <T extends Object> T getEContainer(final EObject object, final Class<T> containerType) {
    final T typedContainer = CommonalitiesLanguageElementExtension.<T>getOptionalEContainer(object, containerType);
    if ((typedContainer != null)) {
      return typedContainer;
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The ");
    String _name = object.eClass().getName();
    _builder.append(_name);
    _builder.append(" ‹");
    _builder.append(object);
    _builder.append("› is not contained inside any ");
    String _simpleName = containerType.getSimpleName();
    _builder.append(_simpleName);
    _builder.append("!");
    throw new IllegalStateException(_builder.toString());
  }

  /**
   * @return the container of the given type, or <code>null</code> if no such container is found
   */
  public static <T extends Object> T getOptionalEContainer(final EObject object, final Class<T> containerType) {
    T _elvis = null;
    T _optionalDirectEContainer = CommonalitiesLanguageElementExtension.<T>getOptionalDirectEContainer(object, containerType);
    if (_optionalDirectEContainer != null) {
      _elvis = _optionalDirectEContainer;
    } else {
      EObject _eContainer = object.eContainer();
      T _optionalEContainer = null;
      if (_eContainer!=null) {
        _optionalEContainer=CommonalitiesLanguageElementExtension.<T>getOptionalEContainer(_eContainer, containerType);
      }
      _elvis = _optionalEContainer;
    }
    return _elvis;
  }

  public static boolean hasEContainer(final EObject object, final Class<? extends EObject> containerType) {
    EObject _optionalEContainer = CommonalitiesLanguageElementExtension.getOptionalEContainer(object, containerType);
    return (_optionalEContainer != null);
  }

  public static <T extends Object> T getDirectEContainer(final EObject object, final Class<T> containerType) {
    final T typedContainer = CommonalitiesLanguageElementExtension.<T>getOptionalDirectEContainer(object, containerType);
    if ((typedContainer != null)) {
      return typedContainer;
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The ");
    String _simpleName = object.getClass().getSimpleName();
    _builder.append(_simpleName);
    _builder.append(" ‹");
    _builder.append(object);
    _builder.append("› parent is not a ");
    String _simpleName_1 = containerType.getSimpleName();
    _builder.append(_simpleName_1);
    _builder.append(", but a ");
    String _name = object.eContainer().eClass().getName();
    _builder.append(_name);
    _builder.append("!");
    throw new RuntimeException(_builder.toString());
  }

  public static <T extends Object> T getOptionalDirectEContainer(final EObject object, final Class<T> containerType) {
    final EObject container = object.eContainer();
    T _xifexpression = null;
    boolean _isInstance = containerType.isInstance(container);
    if (_isInstance) {
      _xifexpression = containerType.cast(container);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static boolean hasDirectEContainer(final EObject object, final Class<? extends EObject> containerType) {
    EObject _optionalDirectEContainer = CommonalitiesLanguageElementExtension.getOptionalDirectEContainer(object, containerType);
    return (_optionalDirectEContainer != null);
  }

  public static CommonalityFile getContainedCommonalityFile(final Resource resource) {
    final CommonalityFile result = CommonalitiesLanguageElementExtension.getOptionalContainedCommonalityFile(resource);
    if ((result != null)) {
      return result;
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The resource ‹");
    _builder.append(resource);
    _builder.append("› is expected to contain only a commonality file, ");
    _builder.append("but it");
    {
      if ((result == null)) {
        _builder.append(" is empty.");
      } else {
        _builder.append(" contains ");
        EList<EObject> _contents = resource.getContents();
        _builder.append(_contents);
        _builder.append(".");
      }
    }
    throw new IllegalStateException(_builder.toString());
  }

  public static CommonalityFile getOptionalContainedCommonalityFile(final Resource resource) {
    final EObject result = IterableExtensions.<EObject>head(resource.getContents());
    if ((result instanceof CommonalityFile)) {
      return ((CommonalityFile)result);
    }
    return null;
  }

  private CommonalitiesLanguageElementExtension() {
    
  }
}
