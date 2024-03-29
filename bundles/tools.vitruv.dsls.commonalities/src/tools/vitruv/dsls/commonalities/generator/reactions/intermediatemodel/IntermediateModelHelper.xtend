package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel

import edu.kit.ipd.sdq.activextendannotations.Utility
import org.eclipse.xtext.xbase.XFeatureCall
import org.eclipse.xtext.xbase.XbaseFactory
import tools.vitruv.dsls.commonalities.language.Concept
import tools.vitruv.dsls.reactions.builder.TypeProvider
import tools.vitruv.dsls.commonalities.runtime.IntermediateModelManagement

import static tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper.*

@Utility
class IntermediateModelHelper {

	static def claimIntermediateId(extension TypeProvider typeProvider, XFeatureCall element) {
		XbaseFactory.eINSTANCE.createXMemberFeatureCall => [
			memberCallTarget = element
			feature = typeProvider.findMethod(IntermediateModelManagement, 'claimIntermediateId')
				.staticExtensionWildcardImported
			explicitOperationCall = true
		]
	}

	static def callGetMetadataModelURI(extension TypeProvider typeProvider, Concept concept) {
		callGetMetadataModelURI(typeProvider, concept.metadataModelKey)
	}

	private static def getMetadataModelKey(Concept concept) {
		return tools.vitruv.dsls.commonalities.runtime.helper.IntermediateModelHelper.getMetadataModelKey(concept.name)
	}
}
