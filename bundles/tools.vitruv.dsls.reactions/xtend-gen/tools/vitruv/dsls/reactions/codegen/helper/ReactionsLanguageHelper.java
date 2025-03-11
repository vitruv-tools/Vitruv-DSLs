package tools.vitruv.dsls.reactions.codegen.helper;

import com.google.common.base.Preconditions;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.api.generator.ReferenceClassNameAdapter;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;

@Utility
@SuppressWarnings("all")
public final class ReactionsLanguageHelper {
  protected static String _getXBlockExpressionText(final XExpression expression) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("{");
    _builder.newLine();
    _builder.append("\t");
    String _text = NodeModelUtils.getNode(expression).getText();
    _builder.append(_text, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    return _builder.toString();
  }

  protected static String _getXBlockExpressionText(final XBlockExpression expression) {
    return NodeModelUtils.getNode(expression).getText();
  }

  private static ReferenceClassNameAdapter getOptionalReferenceAdapter(final EObject element) {
    final Function1<Adapter, Boolean> _function = (Adapter it) -> {
      return Boolean.valueOf(it.isAdapterForType(ReferenceClassNameAdapter.class));
    };
    Adapter _findFirst = IterableExtensions.<Adapter>findFirst(element.eAdapters(), _function);
    return ((ReferenceClassNameAdapter) _findFirst);
  }

  public static String getJavaClassName(final EClassifier eClassifier) {
    String _elvis = null;
    ReferenceClassNameAdapter _optionalReferenceAdapter = ReactionsLanguageHelper.getOptionalReferenceAdapter(eClassifier);
    String _qualifiedNameForReference = null;
    if (_optionalReferenceAdapter!=null) {
      _qualifiedNameForReference=_optionalReferenceAdapter.getQualifiedNameForReference();
    }
    if (_qualifiedNameForReference != null) {
      _elvis = _qualifiedNameForReference;
    } else {
      String _instanceClassName = eClassifier.getInstanceClassName();
      _elvis = _instanceClassName;
    }
    return _elvis;
  }

  public static String getRuntimeClassName(final EObject element) {
    String _elvis = null;
    ReferenceClassNameAdapter _optionalReferenceAdapter = ReactionsLanguageHelper.getOptionalReferenceAdapter(element);
    String _qualifiedNameForReference = null;
    if (_optionalReferenceAdapter!=null) {
      _qualifiedNameForReference=_optionalReferenceAdapter.getQualifiedNameForReference();
    }
    if (_qualifiedNameForReference != null) {
      _elvis = _qualifiedNameForReference;
    } else {
      String _canonicalName = element.getClass().getCanonicalName();
      _elvis = _canonicalName;
    }
    return _elvis;
  }

  public static String getJavaClassName(final MetaclassReference metaclassReference) {
    EClassifier _metaclass = metaclassReference.getMetaclass();
    String _javaClassName = null;
    if (_metaclass!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(_metaclass);
    }
    return _javaClassName;
  }

  public static ReactionsFile getReactionsFile(final Resource resource) {
    EList<EObject> _contents = null;
    if (resource!=null) {
      _contents=resource.getContents();
    }
    EObject _head = null;
    if (_contents!=null) {
      _head=IterableExtensions.<EObject>head(_contents);
    }
    final EObject firstContentElement = _head;
    Class<? extends EObject> _class = null;
    if (firstContentElement!=null) {
      _class=firstContentElement.getClass();
    }
    String _simpleName = null;
    if (_class!=null) {
      _simpleName=_class.getSimpleName();
    }
    Preconditions.checkArgument((firstContentElement instanceof ReactionsFile), 
      "The given resource %s was expected to contain a ReactionsFile element! (was %s)", resource, _simpleName);
    return ((ReactionsFile) firstContentElement);
  }

  public static ReactionsFile getOptionalReactionsFile(final Resource resource) {
    ReactionsFile _xblockexpression = null;
    {
      EList<EObject> _contents = null;
      if (resource!=null) {
        _contents=resource.getContents();
      }
      EObject _head = null;
      if (_contents!=null) {
        _head=IterableExtensions.<EObject>head(_contents);
      }
      final EObject firstContentElement = _head;
      ReactionsFile _xifexpression = null;
      if ((firstContentElement instanceof ReactionsFile)) {
        _xifexpression = ((ReactionsFile)firstContentElement);
      } else {
        _xifexpression = null;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public static boolean containsReactionsFile(final Resource resource) {
    ReactionsFile _optionalReactionsFile = ReactionsLanguageHelper.getOptionalReactionsFile(resource);
    return (_optionalReactionsFile != null);
  }

  public static String getXBlockExpressionText(final XExpression expression) {
    if (expression instanceof XBlockExpression) {
      return _getXBlockExpressionText((XBlockExpression)expression);
    } else if (expression != null) {
      return _getXBlockExpressionText(expression);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(expression).toString());
    }
  }

  private ReactionsLanguageHelper() {
    
  }
}
