package tools.vitruv.dsls.commonalities.runtime.helper

import edu.kit.ipd.sdq.activextendannotations.Utility
import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate
import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge
import tools.vitruv.change.correspondence.CorrespondenceModel

import static com.google.common.base.Preconditions.*

import static extension tools.vitruv.dsls.reactions.runtime.helper.ReactionsCorrespondenceHelper.getCorrespondingElements

@Utility
class IntermediateModelHelper {

	static def getMetadataModelKey(String conceptDomainName) {
		return #[
			'commonalities',
			conceptDomainName + '.' + conceptDomainName.toFirstLower
		]
	}

	static def Intermediate getCorrespondingIntermediate(CorrespondenceModel correspondenceModel, EObject object) {
		return correspondenceModel.getCorrespondingIntermediate(object, Intermediate)
	}

	// This explicitly checks that the corresponding intermediate is of the specified type and otherwise returns null:
	static def <I extends Intermediate> I getCorrespondingIntermediate(CorrespondenceModel correspondenceModel,
		EObject object, Class<I> intermediateType) {
		// Assumption: Each object has at most one Intermediate correspondence.
		return correspondenceModel.getCorrespondingElements(object, intermediateType, null).head
	}

	static def IntermediateResourceBridge getCorrespondingResourceBridge(CorrespondenceModel correspondenceModel,
		EObject object) {
		checkArgument(!(object instanceof Intermediate), "object cannot be of type Intermediate")
		// Assumption: Each object has at most one Resource correspondence.
		return correspondenceModel.getCorrespondingElements(object, IntermediateResourceBridge, null).head
	}
}
