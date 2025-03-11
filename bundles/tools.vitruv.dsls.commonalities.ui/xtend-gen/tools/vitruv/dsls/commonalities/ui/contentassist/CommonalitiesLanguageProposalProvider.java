package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
@SuppressWarnings("all")
public class CommonalitiesLanguageProposalProvider extends AbstractCommonalitiesLanguageProposalProvider {
  @Inject
  private Provider<QualifiedMetaclassProposalFactory> qMetaclassProposalFactory;

  @Inject
  private Provider<UnqualifiedMetaclassProposalFactory> uMetaclassProposalFactory;

  @Inject
  private Provider<DomainPrefixProposalFactory> domainPrefixProposalFactory;

  @Override
  public Function<IEObjectDescription, ICompletionProposal> getProposalFactory(final String ruleName, final ContentAssistContext contentAssistContext) {
    Function<IEObjectDescription, ICompletionProposal> _switchResult = null;
    if (ruleName != null) {
      switch (ruleName) {
        case "QualifiedMetaclass":
          _switchResult = this.<QualifiedMetaclassProposalFactory>init(this.qMetaclassProposalFactory, contentAssistContext);
          break;
        case "UnqualifiedMetaclass":
          _switchResult = this.<UnqualifiedMetaclassProposalFactory>init(this.uMetaclassProposalFactory, contentAssistContext);
          break;
        case "DomainReference":
          _switchResult = this.<DomainPrefixProposalFactory>init(this.domainPrefixProposalFactory, contentAssistContext);
          break;
        default:
          _switchResult = super.getProposalFactory(ruleName, contentAssistContext);
          break;
      }
    } else {
      _switchResult = super.getProposalFactory(ruleName, contentAssistContext);
    }
    return _switchResult;
  }

  private <T extends CommonalitiesLanguageProposalFactory> T init(final Provider<T> factory, final ContentAssistContext contentAssistContext) {
    T _get = factory.get();
    final Procedure1<T> _function = (T it) -> {
      it.setContext(contentAssistContext);
      it.proposalProvider = this;
    };
    return ObjectExtensions.<T>operator_doubleArrow(_get, _function);
  }
}
