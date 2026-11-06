package tools.vitruv.dsls.reactions.tests.importTests;

import static org.hamcrest.MatcherAssert.assertThat;

import allElementTypes.Root;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

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
    return factory.createCompiler((TestReactionsCompiler.TestReactionsCompilerParameters compilerParameters) -> {
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
      EnumSet.of(ImportTestsExecutionMonitor.ExecutionType.RootReaction,
          ImportTestsExecutionMonitor.ExecutionType.DirectSNReaction,
          ImportTestsExecutionMonitor.ExecutionType.Direct2SNReaction,
          ImportTestsExecutionMonitor.ExecutionType.TransitiveSNReaction,
          ImportTestsExecutionMonitor.ExecutionType.Transitive3SNReaction);

  /**
   * Import hierarchy:
   * <p>
   * root -> directSN, direct2SN, directRoutinesQN r qn
   * directSN -> transitiveSN, transitiveRoutinesQN r qn
   * direct2SN -> transitive3SN
   * directRoutinesQN -> transitive2SN, transitiveRoutinesSN r, transitiveRoutinesQN r qn, commonRoutines r qn
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
  public static final String TAG_TEST_TRANSITIVE_REACTION_OVERRIDE = "testTransitiveReactionOverride";
  public static final String TAG_CALL_OVERRIDDEN_ROUTINE = "callOverriddenRoutine";
  public static final String TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE = "callOverriddenTransitiveRoutine";
  public static final String TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY = "callOverriddenTransitiveRoutineWithSeparateOverrideHierarchy";
  public static final String TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE = "callAlreadyOverriddenTransitiveRoutine";
  public static final String TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT = "callTransitiveRoutineOverriddenByImportedSegment";
  public static final String TAG_FROM_ROOT = "fromRoot";
  public static final String TAG_FROM_OVERRIDDEN_SEGMENT = "fromOverriddenSegment";
  public static final String TAG_FROM_SEGMENT_IN_BETWEEN = "fromSegmentInBetween";
  public static final String TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_1 = "testMultipleImportsOfSameRoutinesImportPath1";
  public static final String TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_2 = "testMultipleImportsOfSameRoutinesImportPath2";

  @Test
  void testRootReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(), CoreMatchers.hasItem(ImportTestsExecutionMonitor.ExecutionType.RootReaction));
  }

  @Test
  void testImportedReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(), CoreMatchers.hasItem(ImportTestsExecutionMonitor.ExecutionType.DirectSNReaction));
  }

  @Test
  void testTransitiveImportedReaction() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor().getObservedExecutions(), CoreMatchers.hasItem(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNReaction));
  }

  private void assertObservedExecutions(EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions) {
    MatcherAssert.assertThat(getExecutionMonitor(), ExecutionMonitor.observedExecutions(expectedExecutions));
  }

  @Test
  void testMultipleImportedReactions() {
    triggerSetRootIdReaction();
    assertThat(getExecutionMonitor(), ExecutionMonitor.observedExecutions(rootReactions));
  }

  @Test
  void testReactionCallsRootRoutine() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_ROOT_ROUTINE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsImportedRoutine_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_DIRECT_ROUTINE_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsImportedRoutine_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_DIRECT_ROUTINE_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutine_SN_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutine_SN_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_SN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutine_QN_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_QN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testReactionCallsTransitiveImportedRoutine_QN_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_TRANSITIVE_ROUTINE_QN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsRootRoutine() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_ROOT_ROUTINE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsImportedRoutine_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_DIRECT_ROUTINE_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsImportedRoutine_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_DIRECT_ROUTINE_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutine_SN_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutine_SN_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_SN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutine_QN_SN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_QN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutineCallsTransitiveImportedRoutine_QN_QN() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_ROUTINE, TAG_TRANSITIVE_ROUTINE_QN_QN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testMultipleImportedRoutines() {
    triggerSetRootIdReaction(TAG_CALL_ROUTINE_FROM_REACTION, TAG_ROOT_ROUTINE, TAG_DIRECT_ROUTINE_SN,
        TAG_DIRECT2_ROUTINE_SN, TAG_TRANSITIVE_ROUTINE_SN_SN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.Direct2SNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testRoutinesOnlyReactions() {
    triggerSetRootIdReaction(TAG_TEST_ROUTINES_ONLY_REACTIONS);
    MatcherAssert.assertThat(getExecutionMonitor(), ExecutionMonitor.observedExecutions(rootReactions));
  }

  @Test
  void testRoutinesOnlyRoutines() {
    triggerSetRootIdReaction(TAG_TEST_ROUTINES_ONLY_ROUTINES);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.Transitive2SNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testImportedSegmentsWorking() {
    triggerSetRootIdReaction(TAG_TEST_IMPORTED_SEGMENTS_WORKING);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNInnerRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNInnerRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testOverrideReaction() {
    triggerSetRootIdReaction(TAG_TEST_REACTION_OVERRIDE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenReaction);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testOverrideTransitiveReaction() {
    triggerSetRootIdReaction(TAG_TEST_TRANSITIVE_REACTION_OVERRIDE);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenReaction);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenReaction2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenReaction3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_ROUTINE, TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY,
        TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY,
        TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallAlreadyOverriddenTransitiveRoutineFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromRoot() {
    triggerSetRootIdReaction(TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT, TAG_FROM_ROOT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromOverriddenSegment() {
    triggerSetRootIdReaction(TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT,
        TAG_FROM_OVERRIDDEN_SEGMENT);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testCallTransitiveRoutineOverriddenByImportedSegmentFromSegmentInBetween() {
    triggerSetRootIdReaction(TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT,
        TAG_FROM_SEGMENT_IN_BETWEEN);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine);
    assertObservedExecutions(expectedExecutions);
  }

  @Test
  void testMultipleImportsOfSameRoutines() {
    triggerSetRootIdReaction(TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_1);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions = rootReactions.clone();
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine1);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine2);
    expectedExecutions.add(ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine3);
    assertObservedExecutions(expectedExecutions);

    getExecutionMonitor().reset();

    triggerSetRootIdReaction(TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_2);
    EnumSet<ImportTestsExecutionMonitor.ExecutionType> expectedExecutions2 = rootReactions.clone();
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType.CommonRoutinesRoutine1);
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType.CommonRoutinesRoutine2);
    expectedExecutions2.add(ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutines2Routine3);
    MatcherAssert.assertThat(getExecutionMonitor(),
        ExecutionMonitor.observedExecutions(expectedExecutions2));
  }
}
