package tools.vitruv.dsls.reactions.linking;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.linking.impl.IllegalNodeException;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;

/**
 * Used to enable refering to packages by their metamodel namespace URI.
 * <p>
 * Uses <code>EPackage.Registry</code> to deresolve URIs.
 */
@SuppressWarnings("all")
public class ReactionsLinkingService extends DefaultLinkingService {
  private static final Logger log = Logger.getLogger(ReactionsLinkingService.class);

  @Inject
  private IValueConverterService valueConverterService;

  @Override
  public List<EObject> getLinkedObjects(final EObject context, final EReference ref, final INode node) throws IllegalNodeException {
    List<EObject> _xblockexpression = null;
    {
      boolean _equals = ref.getEType().equals(EcorePackage.Literals.EPACKAGE);
      if (_equals) {
        return this.getEPackage(((ILeafNode) node));
      }
      _xblockexpression = super.getLinkedObjects(context, ref, node);
    }
    return _xblockexpression;
  }

  /**
   * from org.eclipse.xtext.xtext.XtextLinkingService.getPackage(ReferencedMetamodel, ILeafNode)
   */
  public List<EObject> getEPackage(final ILeafNode text) {
    final String nsUri = this.getMetamodelNsURI(text);
    if ((nsUri == null)) {
      return Collections.<EObject>emptyList();
    }
    EPackage _ePackage = EPackage.Registry.INSTANCE.getEPackage(nsUri);
    final EObject resolvedEPackage = ((EObject) _ePackage);
    if ((resolvedEPackage == null)) {
      return Collections.<EObject>emptyList();
    }
    return Collections.<EObject>unmodifiableList(CollectionLiterals.<EObject>newArrayList(resolvedEPackage));
  }

  /**
   * from org.eclipse.xtext.xtext.XtextLinkingService.getMetamodelNsURI(ILeafNode)
   */
  private String getMetamodelNsURI(final ILeafNode text) {
    try {
      Object _value = this.valueConverterService.toValue(text.getText(), this.getLinkingHelper().getRuleNameFrom(
        text.getGrammarElement()), text);
      return ((String) _value);
    } catch (final Throwable _t) {
      if (_t instanceof ValueConverterException) {
        final ValueConverterException e = (ValueConverterException)_t;
        String _text = text.getText();
        String _plus = ("Exception on leaf \'" + _text);
        String _plus_1 = (_plus + "\'");
        ReactionsLinkingService.log.debug(_plus_1, e);
        return null;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
