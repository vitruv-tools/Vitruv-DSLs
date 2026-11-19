package tools.vitruv.dsls.reactions.tests.annotationtests;

import java.util.Optional;
import tools.vitruv.change.interaction.UserInteractor;

/**
 * Records what the reactions and routines of the {@link AnnotationTests} observe about the
 * propagated change. The reactions in {@code AnnotationTests.reactions} report to the singleton
 * {@link #instance}.
 *
 * <p>The monitor is shared by all tests, so tests must call {@link #reset()} before they start.
 * It is not thread-safe.
 */
public final class AnnotationTestsMonitor {
  /** The singleton used by the reactions and by the tests. */
  public static final AnnotationTestsMonitor instance = new AnnotationTestsMonitor();

  private Optional<Author> authorInReaction;
  private Optional<Author> authorInRoutine;
  private UserInteractor userInteractorInRoutine;

  private AnnotationTestsMonitor() {
  }

  /**
   * Forgets all observations.
   */
  public void reset() {
    authorInReaction = null;
    authorInRoutine = null;
    userInteractorInRoutine = null;
  }

  /**
   * Returns the author annotation seen in the reaction.
   *
   * @return {@link Optional} with the {@link Author}; {@code null} if the reaction did not run
   */
  public Optional<Author> getAuthorInReaction() {
    return authorInReaction;
  }

  /**
   * Records the author annotation seen in the reaction.
   *
   * @param authorInReaction {@link Optional} of {@link Author}
   */
  public void setAuthorInReaction(Optional<Author> authorInReaction) {
    this.authorInReaction = authorInReaction;
  }

  /**
   * Returns the author annotation seen in the routine.
   *
   * @return {@link Optional} with the {@link Author}; {@code null} if the routine did not run
   */
  public Optional<Author> getAuthorInRoutine() {
    return authorInRoutine;
  }

  /**
   * Records the author annotation seen in the routine.
   *
   * @param authorInRoutine {@link Optional} of {@link Author}
   */
  public void setAuthorInRoutine(Optional<Author> authorInRoutine) {
    this.authorInRoutine = authorInRoutine;
  }

  /**
   * Returns the {@link UserInteractor} that was available in the routine.
   *
   * @return {@link UserInteractor}; {@code null} if the routine did not run
   */
  public UserInteractor getUserInteractorInRoutine() {
    return userInteractorInRoutine;
  }

  /**
   * Records the {@link UserInteractor} that was available in the routine.
   *
   * @param userInteractorInRoutine {@link UserInteractor}
   */
  public void setUserInteractorInRoutine(UserInteractor userInteractorInRoutine) {
    this.userInteractorInRoutine = userInteractorInRoutine;
  }
}
