package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;

@Utility
@SuppressWarnings("all")
final class CommonalityReferenceExtension {
  public static List<CommonalityReferenceMapping> getMappings(final CommonalityReference reference, final String domainName) {
    final Function1<CommonalityReferenceMapping, Boolean> _function = (CommonalityReferenceMapping it) -> {
      String _domainName = CommonalityReferenceMappingExtension.getParticipation(it).getDomainName();
      return Boolean.valueOf(Objects.equal(_domainName, domainName));
    };
    return IterableExtensions.<CommonalityReferenceMapping>toList(IterableExtensions.<CommonalityReferenceMapping>filter(reference.getMappings(), _function));
  }

  public static Commonality getDeclaringCommonality(final CommonalityReference reference) {
    return CommonalitiesLanguageElementExtension.<Commonality>getDirectEContainer(reference, Commonality.class);
  }

  private CommonalityReferenceExtension() {
    
  }
}
