package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference

import edu.kit.ipd.sdq.activextendannotations.Utility
import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate

import static extension tools.vitruv.dsls.commonalities.runtime.helper.IntermediateModelHelper.*
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence

@Utility
class AttributeReferenceHelper {

	static def <I extends Intermediate> Iterable<I> getPotentiallyContainedIntermediates(
		IReferenceMappingOperator operator, EObject containerObject, EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel,
		Class<I> intermediateType) {
		val containedObjects = operator.getContainedObjects(containerObject)
		return containedObjects.map[correspondenceModel.getCorrespondingIntermediate(it, intermediateType)]
			.filterNull.toSet
	}

	static def <I extends Intermediate> I getPotentialContainerIntermediate(IReferenceMappingOperator operator,
		EObject containedObject, EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel, Class<I> intermediateType) {
		val containerObject = operator.getContainer(containedObject)
		if (containerObject === null) return null
		return correspondenceModel.getCorrespondingIntermediate(containerObject, intermediateType)
	}
}
