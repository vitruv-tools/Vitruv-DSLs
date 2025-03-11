package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@SuppressWarnings("all")
public class UnqualifiedMetaclassProposalFactory extends CommonalitiesLanguageProposalFactory {
  @Inject
  private PrefixMatcher.IgnoreCase ignoreCase;

  @Override
  public ICompletionProposal apply(final IEObjectDescription description) {
    return this.completionProposal(description.getName().getSegment(1)).appendInfoText(description.getName().getFirstSegment()).appendInfoText(QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR).appendText(description.getName().getSegment(1)).withImageOf(description.getEObjectOrProxy()).usePrefixMatcher(this.ignoreCase).propose();
  }
}
