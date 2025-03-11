package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.runtime.IntermediateModelManagement;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@Utility
@SuppressWarnings("all")
public final class IntermediateModelHelper {
  public static XMemberFeatureCall claimIntermediateId(@Extension final TypeProvider typeProvider, final XFeatureCall element) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(element);
      it.setFeature(typeProvider.staticExtensionWildcardImported(typeProvider.findMethod(IntermediateModelManagement.class, "claimIntermediateId")));
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public static XMemberFeatureCall callGetMetadataModelURI(@Extension final TypeProvider typeProvider, final Concept concept) {
    return ReactionsHelper.callGetMetadataModelURI(typeProvider, IntermediateModelHelper.getMetadataModelKey(concept));
  }

  private static List<String> getMetadataModelKey(final Concept concept) {
    return tools.vitruv.dsls.commonalities.runtime.helper.IntermediateModelHelper.getMetadataModelKey(concept.getName());
  }

  private IntermediateModelHelper() {
    
  }
}
