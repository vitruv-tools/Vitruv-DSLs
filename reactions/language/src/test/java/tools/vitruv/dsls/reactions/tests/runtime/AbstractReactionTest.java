package tools.vitruv.dsls.reactions.tests.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.Loggable;

/**
 * Tests the runtime classes {@link AbstractReaction} and {@link Loggable}.
 */
class AbstractReactionTest {
  /**
   * Reaction that matches either all or no changes and counts its executions.
   */
  private static class TestReaction extends AbstractReaction {
    private final boolean matching;
    private int executions = 0;

    TestReaction(boolean matching, Supplier<RoutinesFacade> routinesFacadeSupplier) {
      super(executionState -> routinesFacadeSupplier.get());
      this.matching = matching;
    }

    @Override
    protected boolean isCurrentChangeMatchingTrigger(EChange<EObject> change) {
      return matching;
    }

    @Override
    protected void executeReaction(
        EChange<EObject> change,
        ReactionExecutionState executionState,
        RoutinesFacade routinesFacade) {
      executions++;
    }

    int getExecutions() {
      return executions;
    }
  }

  /**
   * Makes the protected logger of {@link Loggable} accessible.
   */
  private static class TestLoggable extends Loggable {
    Logger logger() {
      return getLogger();
    }
  }

  /**
   * Routines facade that counts how often callers were pushed and dropped.
   */
  private static class CountingRoutinesFacade implements RoutinesFacade {
    private int pushedCallers = 0;
    private int droppedCallers = 0;

    @Override
    public void pushCaller(CallHierarchyHaving caller) {
      pushedCallers++;
    }

    @Override
    public CallHierarchyHaving getCurrentCaller() {
      return null;
    }

    @Override
    public void dropLastCaller() {
      droppedCallers++;
    }

    @Override
    public void setExecutionState(ReactionExecutionState executionState) {
      // The execution state is not needed in these tests.
    }

    @Override
    public ReactionExecutionState getExecutionState() {
      return null;
    }

    int getPushedCallers() {
      return pushedCallers;
    }

    int getDroppedCallers() {
      return droppedCallers;
    }
  }

  @Test
  void doesNotCreateRoutinesFacadeForNonMatchingChange() {
    AtomicInteger createdFacades = new AtomicInteger();
    TestReaction reaction = new TestReaction(false, () -> {
      createdFacades.incrementAndGet();
      return null;
    });

    reaction.execute(null, null);

    assertThat(createdFacades.get(), is(0));
    assertThat(reaction.getExecutions(), is(0));
  }

  @Test
  void executesWithRoutinesFacadeForMatchingChange() {
    CountingRoutinesFacade facade = new CountingRoutinesFacade();
    TestReaction reaction = new TestReaction(true, () -> facade);

    reaction.execute(null, null);

    assertThat(reaction.getExecutions(), is(1));
    assertThat(facade.getPushedCallers(), is(1));
    assertThat(facade.getDroppedCallers(), is(1));
  }

  @Test
  void sharesLoggerBetweenInstancesOfSameClass() {
    TestLoggable first = new TestLoggable();
    TestLoggable second = new TestLoggable();

    assertThat(first.logger(), sameInstance(second.logger()));
    assertThat(first.logger().getName(), is(TestLoggable.class.getCanonicalName()));
  }
}
