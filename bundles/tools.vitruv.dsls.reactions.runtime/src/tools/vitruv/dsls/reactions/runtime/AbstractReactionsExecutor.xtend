package tools.vitruv.dsls.reactions.runtime

import org.apache.log4j.Logger
import tools.vitruv.dsls.reactions.runtime.IReactionRealization
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.atomic.EChange
import tools.vitruv.change.correspondence.CorrespondenceModel
import tools.vitruv.change.propagation.ResourceAccess
import java.util.List
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification
import tools.vitruv.change.composite.MetamodelDescriptor

abstract class AbstractReactionsExecutor extends AbstractChangePropagationSpecification {
	static val LOGGER = Logger.getLogger(AbstractReactionsExecutor);

	val RoutinesFacadesProvider routinesFacadesProvider;
	List<IReactionRealization> reactions;

	new(MetamodelDescriptor sourceMetamodelDescriptor, MetamodelDescriptor targetMetamodelDescriptor) {
		super(sourceMetamodelDescriptor, targetMetamodelDescriptor);
		this.reactions = newArrayList;
		this.routinesFacadesProvider = this.createRoutinesFacadesProvider();
		this.setup();
	}

	protected def getRoutinesFacadesProvider() {
		return routinesFacadesProvider;
	}

	protected def void addReaction(IReactionRealization reaction) {
		this.reactions += reaction;
	}

	override doesHandleChange(EChange change, CorrespondenceModel correspondenceModel) {
		return true
	}

	override propagateChange(EChange change, CorrespondenceModel correspondenceModel,
		ResourceAccess resourceAccess) {
		LOGGER.trace("Call relevant reactions from " + sourceMetamodelDescriptor + " to " + targetMetamodelDescriptor);
		for (reaction : reactions) {
			LOGGER.trace("Calling reaction: " + reaction.class.simpleName + " with change: " + change);
			val executionState = new ReactionExecutionState(userInteractor, correspondenceModel, resourceAccess, this);
			reaction.applyEvent(change, executionState)
		}
	}

	override setUserInteractor(UserInteractor userInteractor) {
		super.setUserInteractor(userInteractor);
		reactions = newArrayList;
		setup();
	}

	protected abstract def RoutinesFacadesProvider createRoutinesFacadesProvider();

	protected abstract def void setup();

}
