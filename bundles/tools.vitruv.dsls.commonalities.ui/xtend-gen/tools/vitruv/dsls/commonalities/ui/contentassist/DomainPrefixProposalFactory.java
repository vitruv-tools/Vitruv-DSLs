package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@SuppressWarnings("all")
public class DomainPrefixProposalFactory extends CommonalitiesLanguageProposalFactory {
  @Inject
  private PrefixMatcher.IgnoreCase ignoreCase;

  @Override
  public ICompletionProposal apply(final IEObjectDescription description) {
    String _firstSegment = description.getName().getFirstSegment();
    String _plus = (_firstSegment + QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR);
    String _plus_1 = (_plus + "(");
    return this.completionProposal(_plus_1).appendText(description.getName().getFirstSegment()).withImageOf(description.getEObjectOrProxy()).usePrefixMatcher(this.ignoreCase).propose();
  }
}
