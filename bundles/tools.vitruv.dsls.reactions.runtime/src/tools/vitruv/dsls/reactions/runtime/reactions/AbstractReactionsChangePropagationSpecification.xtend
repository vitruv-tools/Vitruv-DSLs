package tools.vitruv.dsls.reactions.runtime.reactions

import tools.vitruv.change.composite.MetamodelDescriptor
import org.apache.log4j.Logger
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification
import java.util.List
import tools.vitruv.change.atomic.EChange
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.propagation.ResourceAccess
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
import tools.vitruv.change.correspondence.Correspondence
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory

/**
 * A {@link ChangePropagationSpecification} that executes {@link Reaction}s.
 */
abstract class AbstractReactionsChangePropagationSpecification extends AbstractChangePropagationSpecification {
	static val LOGGER = Logger.getLogger(AbstractReactionsChangePropagationSpecification)

	val List<Reaction> reactions

	new(MetamodelDescriptor sourceMetamodelDescriptor, MetamodelDescriptor targetMetamodelDescriptor) {
		super(sourceMetamodelDescriptor, targetMetamodelDescriptor)
		this.reactions = newArrayList
		this.setup()
	}

	protected def void addReaction(Reaction reaction) {
		this.reactions += reaction
	}

	override doesHandleChange(EChange change, EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
		return true
	}

	override propagateChange(EChange change, EditableCorrespondenceModelView<Correspondence> correspondenceModel, ResourceAccess resourceAccess) {
		LOGGER.trace("Call relevant reactions from " + sourceMetamodelDescriptor + " to " + targetMetamodelDescriptor)
		for (reaction : reactions) {
			LOGGER.trace("Calling reaction: " + reaction.class.simpleName + " with change: " + change)
			val executionState = new ReactionExecutionState(userInteractor, correspondenceModel.reactionsView, resourceAccess, this)
			reaction.execute(change, executionState)
		}
	}
	
	private static def getReactionsView(EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
		return correspondenceModel.getEditableView(ReactionsCorrespondence, [CorrespondenceFactory.eINSTANCE.createReactionsCorrespondence])
	}

	override setUserInteractor(UserInteractor userInteractor) {
		super.setUserInteractor(userInteractor)
		reactions.clear()
		setup()
	}

	protected abstract def void setup()
}
