package tools.vitruv.dsls.reactions.tests.importtests;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static tools.vitruv.dsls.reactions.tests.ExecutionMonitor.observedExecutions;

import allElementTypes.Root;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

/**
 * Generated reactions code static-imports the TAG_* constants of this test class,
 * so it is made public.
 */
public class ImportTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL =
      TestMetamodelsPathFactory.allElementTypes("ImportTestsModelSource");

  private String testName;
  private final List<String> reactionNames = List.of(
      "CommonRoutines.reactions",
      "TransitiveRoutinesQN.reactions",
      "TransitiveRoutinesSN.reactions",
      "Transitive2SN.reactions",
      "Transitive3SN.reactions",
      "TransitiveSN.reactions",
      "DirectRoutinesQN.reactions",
      "Direct2SN.reactions",
      "DirectSN.reactions",
      "Root.reactions");

  @Override
  protected TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory) {
    return factory.createCompiler(compilerParameters -> {
      compilerParameters.setReactions(reactionNames);
      compilerParameters.setChangePropagationSegments(List.of("importTestsRoot"));
    });
  }

  private static ImportTestsExecutionMonitor getExecutionMonitor() {
    return ImportTestsExecutionMonitor.getInstance();
  }

  @BeforeEach
  void configureTests(TestInfo testInfo) {
    // Reset Execution Monitor
    getExecutionMonitor().reset();
    testName = testInfo.getDisplayName();
    this.propagate(this.resourceAt(SOURCE_MODEL),
        // Create Root in resource
        (Resource resource) -> {
          Root root = AllElementTypesCreators.aet.Root();
          root.setId("ImportTestsModelSource");
          resource.getContents().add(root);
        });
  }

  private List<PropagatedChange> triggerSetRootIdReaction(final String... dataTags) {
    return propagate(from(Root.class, SOURCE_MODEL),
        (Root root) -> {
          root.setId(ImportTestsUtils.toTestDataString(testName, dataTags));
        });
  }

  private static final EnumSet<ImportTestsExecutionMonitor.ExecutionType> rootReactions =
      EnumSet.of(ImportTestsExecutionMonitor.ExecutionType.ROOT_REACTION,
          ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_REACTION,
          ImportTestsExecutionMonitor.ExecutionType.DIRECT_2_SN_REACTION,
          ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_REACTION,
          ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_3_SN_REACTION);

  /**
   * Import hierarchy.
   *
   * <p>root -> directSN, direct2SN, directRoutinesQN r qn
   * directSN -> transitiveSN, transitiveRoutinesQN r qn
   * direct2SN -> transitive3SN
   * directRoutinesQN -> transitive2SN, transitiveRoutinesSN r,
   *                     transitiveRoutinesQN r qn, commonRoutines r qn
   * transitiveSN -> commonRoutines r qn
   * transitive2SN
   * transitive3SN
   * transitiveRoutinesSN
   * transitiveRoutinesQN
   * commonRoutines
   */
  public static final String TAG_CALL_ROUTINE_FROM_REACTION = "callRoutineFromReaction";
  public static final String TAG_CALL_ROUTINE_FROM_ROUTINE = "callRoutineFromRoutine";
  public static final String TAG_ROOT_ROUTINE = "rootRoutine";
  public static final String TAG_DIRECT_ROUTINE_SN = "directRoutine_SN";
  public static final String TAG_DIRECT2_ROUTINE_SN = "direct2Routine_SN";
  public static final String TAG_DIRECT_ROUTINE_QN = "directRoutine_QN";
  public static final String TAG_TRANSITIVE_ROUTINE_SN_SN = "transitiveRoutine_SN_SN";
  public static final String TAG_TRANSITIVE_ROUTINE_SN_QN = "transitiveRoutine_SN_QN";
  public static final String TAG_TRANSITIVE_ROUTINE_QN_SN = "transitiveRoutine_QN_SN";
  public static final String TAG_TRANSITIVE_ROUTINE_QN_QN = "transitiveRoutine_QN_QN";
  public static final String TAG_TEST_ROUTINES_ONLY_REACTIONS = "testRoutinesOnlyReactions";
  public static final String TAG_TEST_ROUTINES_ONLY_ROUTINES = "testRoutinesOnlyRoutines";
  public static final String TAG_TEST_IMPORTED_SEGMENTS_WORKING = "testImportedSegmentsWorking";
  public static final String TAG_TEST_REACTION_OVERRIDE = "testReactionOverride";
  public static final String TAG_TEST_TRANSITIVE_REACTION_OVERRIDE
      = "testTransitiveReactionOverride";
  public static final String TAG_CALL_OVERRIDDEN_ROUTINE = "callOverriddenRoutine";
  public static final String TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE
      = "callOverriddenTransitiveRoutine";
  public static final String TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY
      = "callOverriddenTransitiveRoutineWithSeparateOverrideHierarchy";
  public static final String TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE
      = "callAlreadyOverriddenTransitiveRoutine";
  public static final String TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT
      = "callTransitiveRoutineOverriddenByImportedSegment";
  public static final String TAG_FROM_ROOT = "fromRoot";
  public static final String TAG_FROM_OVERRIDDEN_SEGMENT = "fromOverriddenSegment";
  public static final String TAG_FROM_SEGMENT_IN_BETWEEN = "fromSegmentInBetween";
  public static final String TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_1
      = "testMultipleImportsOfSameRoutinesImportPath1";
  public static final String TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_2
      = "testMultipleImportsOfSameRoutinesImportPath2";

  @Test
  void testRootReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(),
        hasItem(ImportTestsExecutionMonitor.ExecutionType.ROOT_REACTION));
  }

  @Test
  void testImportedReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(),
        hasItem(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_REACTION));
  }

  @Test
  void testTransitiveImportedReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(),
        hasItem(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_REACTION));
  }

  private void assertObservedExecutions(
      EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions) {
    MatcherAssert.assertThat(getExecutionMonitor(),
        observedExecutions(expectedExecutions));
  }

  @Test
  void testMultipleImportedReactions() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor(), observedExecutions(rootReactions));
  }

  @Test
  void testReactionCallsRootRoutine() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_ROOT_ROUTINE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsImportedRoutineSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_DIRECT_ROUTINE_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsImportedRoutineQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_DIRECT_ROUTINE_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutineSNSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutineSNQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_SN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutineQNSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_QN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutineQNQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_QN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsRootRoutine() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_ROOT_ROUTINE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsImportedRoutineSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_DIRECT_ROUTINE_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsImportedRoutineQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_DIRECT_ROUTINE_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.DIRECT_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutineSNSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutineSNQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_SN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutineQNSN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_QN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutineQNQN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_QN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(
        ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_ROUTINES_QN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testMultipleImportedRoutines() {
    triggerSetRootIdReaction(
        TAG_CALL_ROUTINE_FROM_REACTION, TAG_ROOT_ROUTINE, TAG_DIRECT_ROUTINE_SN,
        TAG_DIRECT2_ROUTINE_SN, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_2_SN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutinesOnlyReactions() {
    triggerSetRootIdReaction(TAG_TEST_ROUTINES_ONLY_REACTIONS);
    MatcherAssert.assertThat(getExecutionMonitor(), observedExecutions(rootReactions));
  }

  @Test
  void testRoutinesOnlyRoutines() {
    triggerSetRootIdReaction(TAG_TEST_ROUTINES_ONLY_ROUTINES);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_ROUTINES_QN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .TRANSITIVE_ROUTINES_QN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .TRANSITIVE_ROUTINES_SN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .TRANSITIVE_2_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testImportedSegmentsWorking() {
    triggerSetRootIdReaction(TAG_TEST_IMPORTED_SEGMENTS_WORKING);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_INNER_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .TRANSITIVE_ROUTINES_QN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TRANSITIVE_SN_INNER_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testOverrideReaction() {
    triggerSetRootIdReaction(TAG_TEST_REACTION_OVERRIDE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_DIRECT_SN_OVERRIDDEN_REACTION);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testOverrideTransitiveReaction() {
    triggerSetRootIdReaction(TAG_TEST_TRANSITIVE_REACTION_OVERRIDE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_REACTION);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_REACTION_2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_REACTION_3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_DIRECT_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_ROUTINE, TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_DIRECT_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromRoot() {
    triggerSetRootIdReaction(
        TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY,
        TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_3_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromOverriddenSegment() {
    triggerSetRootIdReaction(
        TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY,
        TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_3_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromSegmentInBetween() {
    triggerSetRootIdReaction(
        TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY,
        TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_3_SN_OVERRIDDEN_ROUTINE);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(
        TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE,
        TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromSegmentInBetween() {
    triggerSetRootIdReaction(
        TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE,
        TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.ROOT_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromRoot() {
    triggerSetRootIdReaction(
        TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT,
        TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT,
        TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT,
        TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DIRECT_SN_ROUTINE);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testMultipleImportsOfSameRoutines() {
    triggerSetRootIdReaction(TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_1);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_COMMON_ROUTINES_ROUTINE_1);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_COMMON_ROUTINES_ROUTINE_2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_COMMON_ROUTINES_ROUTINE_3);
    assertObservedExecutions(expectedExecutions);

    getExecutionMonitor().reset();

    triggerSetRootIdReaction(TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_2);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions2 = rootReactions.clone();
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType.COMMON_ROUTINES_ROUTINE_1);
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType.COMMON_ROUTINES_ROUTINE_2);
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType
        .ROOT_COMMON_ROUTINES_2_ROUTINE_3);
    assertThat(getExecutionMonitor(),
        observedExecutions(expectedExecutions2));
  }
}
