package tools.vitruv.dsls.commonalities.export;

import com.google.inject.Inject;
import java.util.Arrays;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;

@SuppressWarnings("all")
public class CommonalityFileResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
  @Inject
  @Extension
  private IEObjectDescriptionProvider eObjectDescriptionProvider;

  protected boolean _createEObjectDescriptions(final EObject eObject, final IAcceptor<IEObjectDescription> acceptor) {
    return true;
  }

  protected boolean _createEObjectDescriptions(final Commonality commonality, final IAcceptor<IEObjectDescription> acceptor) {
    acceptor.accept(this.eObjectDescriptionProvider.describe(commonality));
    return false;
  }

  public boolean createEObjectDescriptions(final EObject commonality, final IAcceptor<IEObjectDescription> acceptor) {
    if (commonality instanceof Commonality) {
      return _createEObjectDescriptions((Commonality)commonality, acceptor);
    } else if (commonality != null) {
      return _createEObjectDescriptions(commonality, acceptor);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(commonality, acceptor).toString());
    }
  }
}
