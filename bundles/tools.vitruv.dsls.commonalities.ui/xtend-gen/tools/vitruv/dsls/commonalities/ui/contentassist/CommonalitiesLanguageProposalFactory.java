package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.AbstractContentProposalProvider;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class CommonalitiesLanguageProposalFactory implements Function<IEObjectDescription, ICompletionProposal>, Function1<IEObjectDescription, ICompletionProposal>, com.google.common.base.Function<IEObjectDescription, ICompletionProposal> {
  protected static class CompletionProposalBuilder {
    @Extension
    private final CommonalitiesLanguageProposalFactory factory;

    private String completion;

    private StyledString text = new StyledString();

    private Image image;

    private CompletionProposalBuilder(final CommonalitiesLanguageProposalFactory factory) {
      this.factory = factory;
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder forCompletion(final String completion) {
      CommonalitiesLanguageProposalFactory.CompletionProposalBuilder _xblockexpression = null;
      {
        this.completion = completion;
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder withImage(final Image image) {
      CommonalitiesLanguageProposalFactory.CompletionProposalBuilder _xblockexpression = null;
      {
        this.image = image;
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder withImageOf(final EObject object) {
      return this.withImage(this.factory.labelProvider.getImage(object));
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder appendText(final String text) {
      CommonalitiesLanguageProposalFactory.CompletionProposalBuilder _xblockexpression = null;
      {
        this.text.append(text);
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder usePrefixMatcher(final PrefixMatcher prefixMatcher) {
      CommonalitiesLanguageProposalFactory.CompletionProposalBuilder _xblockexpression = null;
      {
        ContentAssistContext.Builder _copy = this.factory.getContext().copy();
        final Procedure1<ContentAssistContext.Builder> _function = (ContentAssistContext.Builder it) -> {
          it.setMatcher(prefixMatcher);
        };
        this.factory.setContext(ObjectExtensions.<ContentAssistContext.Builder>operator_doubleArrow(_copy, _function).toContext());
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder appendInfoText(final String text) {
      CommonalitiesLanguageProposalFactory.CompletionProposalBuilder _xblockexpression = null;
      {
        this.text.append(text, StyledString.QUALIFIER_STYLER);
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    protected ICompletionProposal propose() {
      return this.factory.proposalProvider.createCompletionProposal(this.completion, this.text, this.image, this.factory.getContext());
    }
  }

  @Inject
  private IQualifiedNameConverter descriptionConverter;

  @Inject
  private ILabelProvider labelProvider;

  protected ContentAssistContext contentAssistContext;

  protected AbstractContentProposalProvider proposalProvider;

  public AbstractContentProposalProvider setProposalProvider(final AbstractContentProposalProvider proposalProvider) {
    return this.proposalProvider = proposalProvider;
  }

  public ContentAssistContext setContext(final ContentAssistContext context) {
    return this.contentAssistContext = context;
  }

  protected ContentAssistContext getContext() {
    return this.contentAssistContext;
  }

  protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder completionProposal(final QualifiedName completion) {
    return this.completionProposal(this.descriptionConverter.toString(completion));
  }

  protected CommonalitiesLanguageProposalFactory.CompletionProposalBuilder completionProposal(final String completion) {
    return new CommonalitiesLanguageProposalFactory.CompletionProposalBuilder(this).forCompletion(completion);
  }

  /**
   * Needed because Xtend’s type inference cannot handle any case that’s not
   * completely obvious.
   */
  public Function1<IEObjectDescription, ICompletionProposal> fun() {
    return this;
  }
}
