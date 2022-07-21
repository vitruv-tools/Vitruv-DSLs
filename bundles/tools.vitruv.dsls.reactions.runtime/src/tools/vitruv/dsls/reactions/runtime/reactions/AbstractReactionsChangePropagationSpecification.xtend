package tools.vitruv.dsls.reactions.runtime.reactions

import tools.vitruv.change.composite.MetamodelDescriptor
import org.apache.log4j.Logger
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import tools.vitruv.change.atomic.EChange
import tools.vitruv.change.correspondence.CorrespondenceModel
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.propagation.ResourceAccess
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

/**
 * A {@link ChangePropagationSpecification} that executes {@link Reaction}s.
 */
abstract class AbstractReactionsChangePropagationSpecification extends AbstractChangePropagationSpecification {
	static val LOGGER = Logger.getLogger(AbstractReactionsChangePropagationSpecification)

	@Accessors(PROTECTED_GETTER)
	val RoutinesFacadesProvider routinesFacadesProvider
	val List<Reaction> reactions

	new(MetamodelDescriptor sourceMetamodelDescriptor, MetamodelDescriptor targetMetamodelDescriptor) {
		super(sourceMetamodelDescriptor, targetMetamodelDescriptor)
		this.reactions = newArrayList
		this.routinesFacadesProvider = createRoutinesFacadesProvider()
		this.setup()
	}

	protected def getRoutinesFacadesProvider() {
		return routinesFacadesProvider
	}

	protected def void addReaction(Reaction reaction) {
		this.reactions += reaction
	}

	override doesHandleChange(EChange change, CorrespondenceModel correspondenceModel) {
		return true
	}

	override propagateChange(EChange change, CorrespondenceModel correspondenceModel, ResourceAccess resourceAccess) {
		LOGGER.trace("Call relevant reactions from " + sourceMetamodelDescriptor + " to " + targetMetamodelDescriptor)
		for (reaction : reactions) {
			LOGGER.trace("Calling reaction: " + reaction.class.simpleName + " with change: " + change)
			val executionState = new ReactionExecutionState(userInteractor, correspondenceModel, resourceAccess, this)
			reaction.execute(change, executionState)
		}
	}

	override setUserInteractor(UserInteractor userInteractor) {
		super.setUserInteractor(userInteractor)
		reactions.clear()
		setup();
	}

	protected abstract def RoutinesFacadesProvider createRoutinesFacadesProvider()

	protected abstract def void setup()
}
