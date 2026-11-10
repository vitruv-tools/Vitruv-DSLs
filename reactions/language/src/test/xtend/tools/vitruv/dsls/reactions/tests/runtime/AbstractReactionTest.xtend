package tools.vitruv.dsls.reactions.tests.runtime

import java.util.concurrent.atomic.AtomicInteger
import org.eclipse.emf.ecore.EObject
import org.junit.jupiter.api.Test
import tools.vitruv.change.atomic.EChange
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.runtime.structure.Loggable

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.sameInstance
import static org.hamcrest.MatcherAssert.assertThat

class AbstractReactionTest {
	static class TestReaction extends AbstractReaction {
		val boolean matching
		var executions = 0

		new(boolean matching, ()=>RoutinesFacade routinesFacadeSupplier) {
			super([routinesFacadeSupplier.apply])
			this.matching = matching
		}

		override protected isCurrentChangeMatchingTrigger(EChange<EObject> change) {
			matching
		}

		override protected executeReaction(EChange<EObject> change, ReactionExecutionState executionState,
			RoutinesFacade routinesFacade) {
			executions++
		}
	}

	static class TestLoggable extends Loggable {
		def logger() {
			getLogger()
		}
	}

	@Test
	def void doesNotCreateRoutinesFacadeForNonMatchingChange() {
		val createdFacades = new AtomicInteger
		val reaction = new TestReaction(false) [
			createdFacades.incrementAndGet
			null
		]
		reaction.execute(null, null)

		assertThat(createdFacades.get, is(0))
		assertThat(reaction.executions, is(0))
	}

	@Test
	def void executesWithRoutinesFacadeForMatchingChange() {
		val facade = new CountingRoutinesFacade
		val reaction = new TestReaction(true) [facade]
		reaction.execute(null, null)

		assertThat(reaction.executions, is(1))
		assertThat(facade.pushedCallers, is(1))
		assertThat(facade.droppedCallers, is(1))
	}

	@Test
	def void sharesLoggerBetweenInstancesOfSameClass() {
		val first = new TestLoggable
		val second = new TestLoggable

		assertThat(first.logger, sameInstance(second.logger))
		assertThat(first.logger.name, is(TestLoggable.canonicalName))
	}

	static class CountingRoutinesFacade implements RoutinesFacade {
		var pushedCallers = 0
		var droppedCallers = 0

		override pushCaller(CallHierarchyHaving caller) {
			pushedCallers++
		}

		override getCurrentCaller() {
			null
		}

		override dropLastCaller() {
			droppedCallers++
		}

		override setExecutionState(ReactionExecutionState executionState) {
		}

		override getExecutionState() {
			null
		}
	}
}
