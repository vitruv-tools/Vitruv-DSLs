package tools.vitruv.dsls.reactions.tests.importTests;

import allElementTypes.Root;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory;

@SuppressWarnings("all")
public class ImportTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("ImportTestsModelSource");

  private String testName;

  @Override
  protected TestReactionsCompiler createCompiler(final TestReactionsCompiler.Factory factory) {
    final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> _function = (TestReactionsCompiler.TestReactionsCompilerParameters it) -> {
      it.setReactions(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("CommonRoutines.reactions", "TransitiveRoutinesQN.reactions", "TransitiveRoutinesSN.reactions", "Transitive2SN.reactions", "Transitive3SN.reactions", "TransitiveSN.reactions", "DirectRoutinesQN.reactions", "Direct2SN.reactions", "DirectSN.reactions", "Root.reactions")));
      it.setChangePropagationSegments(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("importTestsRoot")));
    };
    return factory.createCompiler(_function);
  }

  private static ImportTestsExecutionMonitor getExecutionMonitor() {
    return ImportTestsExecutionMonitor.getInstance();
  }

  @BeforeEach
  public void createRoot() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("ImportTestsModelSource");
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(ImportTests.SOURCE_MODEL), _function);
  }

  @BeforeEach
  public void captureTestName(final TestInfo testInfo) {
    this.testName = testInfo.getDisplayName();
  }

  @BeforeEach
  public void resetExecutionMonitor() {
    ImportTests.getExecutionMonitor().reset();
  }

  private List<PropagatedChange> triggerSetRootIdReaction(final String... dataTags) {
    final Consumer<Root> _function = (Root it) -> {
      it.setId(ImportTestsUtils.toTestDataString(this.testName, dataTags));
    };
    return this.<Root>propagate(this.<Root>from(Root.class, ImportTests.SOURCE_MODEL), _function);
  }

  private static final EnumSet<ImportTestsExecutionMonitor.ExecutionType> rootReactions = EnumSet.<ImportTestsExecutionMonitor.ExecutionType>of(ImportTestsExecutionMonitor.ExecutionType.RootReaction, ImportTestsExecutionMonitor.ExecutionType.DirectSNReaction, ImportTestsExecutionMonitor.ExecutionType.Direct2SNReaction, ImportTestsExecutionMonitor.ExecutionType.TransitiveSNReaction, 
    ImportTestsExecutionMonitor.ExecutionType.Transitive3SNReaction);

  /**
   * Import hierarchy:
   * 
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
  public void testRootReaction() {
    this.triggerSetRootIdReaction();
    MatcherAssert.<Set<ImportTestsExecutionMonitor.ExecutionType>>assertThat(ImportTests.getExecutionMonitor().getObservedExecutions(), CoreMatchers.<ImportTestsExecutionMonitor.ExecutionType>hasItem(ImportTestsExecutionMonitor.ExecutionType.RootReaction));
  }

  @Test
  public void testImportedReaction() {
    this.triggerSetRootIdReaction();
    MatcherAssert.<Set<ImportTestsExecutionMonitor.ExecutionType>>assertThat(ImportTests.getExecutionMonitor().getObservedExecutions(), CoreMatchers.<ImportTestsExecutionMonitor.ExecutionType>hasItem(ImportTestsExecutionMonitor.ExecutionType.DirectSNReaction));
  }

  @Test
  public void testTransitiveImportedReaction() {
    this.triggerSetRootIdReaction();
    MatcherAssert.<Set<ImportTestsExecutionMonitor.ExecutionType>>assertThat(ImportTests.getExecutionMonitor().getObservedExecutions(), CoreMatchers.<ImportTestsExecutionMonitor.ExecutionType>hasItem(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNReaction));
  }

  @Test
  public void testMultipleImportedReactions() {
    this.triggerSetRootIdReaction();
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(ImportTests.rootReactions));
  }

  @Test
  public void testReactionCallsRootRoutine() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_ROOT_ROUTINE);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsImportedRoutine_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_DIRECT_ROUTINE_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsImportedRoutine_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_DIRECT_ROUTINE_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsTransitiveImportedRoutine_SN_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_TRANSITIVE_ROUTINE_SN_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsTransitiveImportedRoutine_SN_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_TRANSITIVE_ROUTINE_SN_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsTransitiveImportedRoutine_QN_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_TRANSITIVE_ROUTINE_QN_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testReactionCallsTransitiveImportedRoutine_QN_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_TRANSITIVE_ROUTINE_QN_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsRootRoutine() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_ROOT_ROUTINE);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsImportedRoutine_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_DIRECT_ROUTINE_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsImportedRoutine_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_DIRECT_ROUTINE_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsTransitiveImportedRoutine_SN_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_TRANSITIVE_ROUTINE_SN_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsTransitiveImportedRoutine_SN_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_TRANSITIVE_ROUTINE_SN_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsTransitiveImportedRoutine_QN_SN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_TRANSITIVE_ROUTINE_QN_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutineCallsTransitiveImportedRoutine_QN_QN() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_ROUTINE, ImportTests.TAG_TRANSITIVE_ROUTINE_QN_QN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testMultipleImportedRoutines() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ROUTINE_FROM_REACTION, ImportTests.TAG_ROOT_ROUTINE, ImportTests.TAG_DIRECT_ROUTINE_SN, 
      ImportTests.TAG_DIRECT2_ROUTINE_SN, ImportTests.TAG_TRANSITIVE_ROUTINE_SN_SN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootRoutine, ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine, ImportTestsExecutionMonitor.ExecutionType.Direct2SNRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testRoutinesOnlyReactions() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_ROUTINES_ONLY_REACTIONS);
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(ImportTests.rootReactions));
  }

  @Test
  public void testRoutinesOnlyRoutines() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_ROUTINES_ONLY_ROUTINES);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectRoutinesQNRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesSNRoutine, ImportTestsExecutionMonitor.ExecutionType.Transitive2SNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testImportedSegmentsWorking() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_IMPORTED_SEGMENTS_WORKING);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNInnerRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveSNRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveRoutinesQNRoutine, ImportTestsExecutionMonitor.ExecutionType.TransitiveSNInnerRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testOverrideReaction() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_REACTION_OVERRIDE);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, 
      Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenReaction, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testOverrideTransitiveReaction() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_TRANSITIVE_REACTION_OVERRIDE);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenReaction, ImportTestsExecutionMonitor.ExecutionType.RootRoutine, ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenReaction2, ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenReaction3, ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenRoutineFromRoot() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_ROUTINE, ImportTests.TAG_FROM_ROOT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenRoutineFromOverriddenSegment() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_ROUTINE, ImportTests.TAG_FROM_OVERRIDDEN_SEGMENT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootDirectSNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineFromRoot() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_ROOT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineFromOverriddenSegment() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_OVERRIDDEN_SEGMENT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineFromSegmentInBetween() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_SEGMENT_IN_BETWEEN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromRoot() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY, ImportTests.TAG_FROM_ROOT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromOverriddenSegment() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY, 
      ImportTests.TAG_FROM_OVERRIDDEN_SEGMENT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallOverriddenTransitiveRoutineWithSeparateOverrideHierarchyFromSegmentInBetween() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_OVERRIDDEN_TRANSITIVE_ROUTINE_WITH_SEPARATE_OVERRIDE_HIERARCHY, 
      ImportTests.TAG_FROM_SEGMENT_IN_BETWEEN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitive3SNOverriddenRoutine, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallAlreadyOverriddenTransitiveRoutineFromRoot() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_ROOT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallAlreadyOverriddenTransitiveRoutineFromOverriddenSegment() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_OVERRIDDEN_SEGMENT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallAlreadyOverriddenTransitiveRoutineFromSegmentInBetween() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_ALREADY_OVERRIDDEN_TRANSITIVE_ROUTINE, ImportTests.TAG_FROM_SEGMENT_IN_BETWEEN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootTransitiveSNOverriddenRoutine2, ImportTestsExecutionMonitor.ExecutionType.RootRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallTransitiveRoutineOverriddenByImportedSegmentFromRoot() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT, ImportTests.TAG_FROM_ROOT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3, ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallTransitiveRoutineOverriddenByImportedSegmentFromOverriddenSegment() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT, 
      ImportTests.TAG_FROM_OVERRIDDEN_SEGMENT);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3, ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testCallTransitiveRoutineOverriddenByImportedSegmentFromSegmentInBetween() {
    this.triggerSetRootIdReaction(ImportTests.TAG_CALL_TRANSITIVE_ROUTINE_OVERRIDDEN_BY_IMPORTED_SEGMENT, 
      ImportTests.TAG_FROM_SEGMENT_IN_BETWEEN);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.DirectSNTransitiveSNOverriddenRoutine3, ImportTestsExecutionMonitor.ExecutionType.DirectSNRoutine)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
  }

  @Test
  public void testMultipleImportsOfSameRoutines() {
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_1);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, 
      Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine1, ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine2, ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutinesRoutine3)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), 
      ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus, ImportTestsExecutionMonitor.ExecutionType.class))));
    ImportTests.getExecutionMonitor().reset();
    this.triggerSetRootIdReaction(ImportTests.TAG_TEST_MULTIPLE_IMPORTS_OF_SAME_ROUTINES_IMPORT_PATH_2);
    Iterable<ImportTestsExecutionMonitor.ExecutionType> _plus_1 = Iterables.<ImportTestsExecutionMonitor.ExecutionType>concat(ImportTests.rootReactions, Collections.<ImportTestsExecutionMonitor.ExecutionType>unmodifiableList(CollectionLiterals.<ImportTestsExecutionMonitor.ExecutionType>newArrayList(ImportTestsExecutionMonitor.ExecutionType.CommonRoutinesRoutine1, ImportTestsExecutionMonitor.ExecutionType.CommonRoutinesRoutine2, ImportTestsExecutionMonitor.ExecutionType.RootCommonRoutines2Routine3)));
    MatcherAssert.<ImportTestsExecutionMonitor>assertThat(ImportTests.getExecutionMonitor(), ExecutionMonitor.<ImportTestsExecutionMonitor.ExecutionType>observedExecutions(((ImportTestsExecutionMonitor.ExecutionType[])Conversions.unwrapArray(_plus_1, ImportTestsExecutionMonitor.ExecutionType.class))));
  }
}
