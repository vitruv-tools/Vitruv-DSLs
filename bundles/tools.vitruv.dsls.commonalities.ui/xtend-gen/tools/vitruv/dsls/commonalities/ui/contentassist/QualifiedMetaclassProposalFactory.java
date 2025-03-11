package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.resource.IEObjectDescription;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@SuppressWarnings("all")
public class QualifiedMetaclassProposalFactory extends CommonalitiesLanguageProposalFactory {
  @Inject
  private MetaclassPrefixMatcher metaclassPrefixMatcher;

  @Override
  public ICompletionProposal apply(final IEObjectDescription description) {
    return this.completionProposal(description.getName()).appendText(description.getName().getFirstSegment()).appendText(QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR).appendText(description.getName().getSegment(1)).withImageOf(description.getEObjectOrProxy()).usePrefixMatcher(this.metaclassPrefixMatcher).propose();
  }
}
