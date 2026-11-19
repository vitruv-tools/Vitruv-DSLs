package tools.vitruv.dsls.reactions.locking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import allElementTypes.AllElementTypesFactory;
import allElementTypes.AllElementTypesPackage;
import allElementTypes.NonRoot;
import allElementTypes.Root;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.TypeInferringAtomicEChangeFactory;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.locking.FootprintReport;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;
import tools.vitruv.dsls.reactions.runtime.locking.PropagationFootprint;
import tools.vitruv.dsls.reactions.runtime.locking.ReactionsPropagationFootprints;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

/**
 * Runs the runtime access analysis on the compiled reactions of the execution tests
 * ({@code AllElementTypesRedundancy.reactions}) and checks the footprints of some changes.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
class ReactionsPropagationFootprintsTest {
  private static final String MM = AllElementTypesPackage.eNS_URI;
  private static final TypeInferringAtomicEChangeFactory CHANGES =
      TypeInferringAtomicEChangeFactory.getInstance();

  @Inject TestReactionsCompiler.Factory compilerFactory;
  @TempDir static Path compilationDir;

  /**
   * The compiled reactions and their analysis, shared by all tests of this class: compiling and
   * analyzing the bytecode take a few seconds each, and the tests only read them (the cache test
   * uses footprints of its own).
   */
  private static List<ChangePropagationSpecification> specifications;
  private static ReactionsPropagationFootprints footprints;

  @BeforeEach
  void compileAndAnalyzeReactionsOnce() {
    if (specifications == null) {
      var compiler = compilerFactory.createCompiler(parameters -> {
        parameters.setReactionsOwner(this);
        parameters.setCompilationProjectDir(compilationDir);
        parameters.setReactions(
            List.of("/tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions"));
        parameters.setChangePropagationSegments(List.of("simpleChangesTests"));
      });
      List<ChangePropagationSpecification> compiled = new ArrayList<>();
      compiler.getChangePropagationSpecifications().forEach(compiled::add);
      specifications = List.copyOf(compiled);
      footprints = new ReactionsPropagationFootprints(specifications);
    }
  }

  @SuppressWarnings("unchecked")
  private static EChange<EObject> replaceAttribute(Root root) {
    return (EChange<EObject>) (EChange<?>) CHANGES.createReplaceSingleAttributeChange(root,
        AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_EATTRIBUTE, 1, 2);
  }

  @Test
  void footprintOfAttributeReplacement() {
    Root root = AllElementTypesFactory.eINSTANCE.createRoot();
    PropagationFootprint footprint = footprints.footprintOf(replaceAttribute(root));

    assertThat(footprint.reactions().stream().anyMatch(name ->
        name.endsWith("ReplacedSingleValuedEAttributeReaction")), is(true));
    assertThat(footprint.writeKeys(), hasItem("f:" + MM + "#Root.singleValuedEAttribute"));
    assertThat(footprint.readKeys(), hasItem("corr:*|" + MM + "#Root|" + MM + "#Root"));
    // the helper SimpleChangesTestsExecutionMonitor is analyzed, not treated as opaque
    assertThat(footprint.summary().getOpaqueCalls().toString(),
        footprint.summary().isUnknownWrite(), is(false));
    assertThat(footprint.writeKeys(), not(hasItem("mm:" + MM)));
  }

  @Test
  void footprintIsCachedPerChangeKind() {
    var fresh = new ReactionsPropagationFootprints(specifications);
    fresh.footprintOf(replaceAttribute(AllElementTypesFactory.eINSTANCE.createRoot()));
    fresh.footprintOf(replaceAttribute(AllElementTypesFactory.eINSTANCE.createRoot()));
    assertEquals(1, fresh.cacheMisses());
    assertEquals(1, fresh.cacheHits());
  }

  @Test
  @SuppressWarnings("unchecked")
  void disjointChangesDoNotConflictButSameKindDoes() {
    Root root = AllElementTypesFactory.eINSTANCE.createRoot();
    NonRoot nonRoot = AllElementTypesFactory.eINSTANCE.createNonRoot();
    var attributeChange = footprints.footprintOf(replaceAttribute(root));
    var insertChange = footprints.footprintOf((EChange<EObject>) (EChange<?>)
        CHANGES.createInsertReferenceChange(root,
            AllElementTypesPackage.Literals.ROOT__MULTI_VALUED_CONTAINMENT_EREFERENCE,
            nonRoot, 0));
    var conflict = footprints.keyConflict();
    // the same kind of change always conflicts with itself on type level (instance-level locks
    // of the transaction manager have to tell two such changes apart)
    assertTrue(attributeChange.conflictsWith(attributeChange, conflict));
    assertThat(insertChange.writeKeys(),
        hasItem("f:" + MM + "#Root.multiValuedContainmentEReference"));
    // correspondence keys carry the end types: Root|Root lookups vs NonRoot|NonRoot creation
    assertThat(attributeChange.readKeys(), hasItem("corr:*|" + MM + "#Root|" + MM + "#Root"));
  }

  @Test
  void ecoreMetamodelAccessIsNoModelAccess() {
    var footprint = footprints.footprintOf(
        replaceAttribute(AllElementTypesFactory.eINSTANCE.createRoot()));
    assertThat(footprint.readKeys().stream().anyMatch(k -> k.contains("emf/2002/Ecore")),
        is(false));
  }

  @Test
  void stepFootprintIsPartOfTransitiveFootprint() {
    var change = replaceAttribute(AllElementTypesFactory.eINSTANCE.createRoot());
    var step = footprints.stepFootprintOf(change);
    var transitive = footprints.footprintOf(change);
    assertTrue(transitive.reactions().containsAll(step.reactions()));
    assertTrue(transitive.writeKeys().containsAll(step.writeKeys()));
    assertThat(step.writeKeys(), hasItem("f:" + MM + "#Root.singleValuedEAttribute"));
    assertThat(step.reactions().isEmpty(), is(false));
  }

  private static PropagationFootprint footprint(Set<String> reads, Set<String> writes) {
    return new PropagationFootprint(reads, writes, List.of(), null);
  }

  private static String featureKey(EStructuralFeature feature) {
    return "f:" + feature.getEContainingClass().getEPackage().getNsURI() + "#"
        + feature.getEContainingClass().getName() + "." + feature.getName();
  }

  @Test
  void typeLevelConflictsOfClassesAndCorrespondences() {
    var nonRootFeature =
        AllElementTypesPackage.Literals.NON_ROOT.getEAllStructuralFeatures().get(0);
    var rootFeature = AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_EATTRIBUTE;
    var writesAllNonRoots = footprint(Set.of(), Set.of("c:" + MM + "#NonRoot"));
    var readsNonRootFeature = footprint(Set.of(featureKey(nonRootFeature)), Set.of());
    var readsRootFeature = footprint(Set.of(featureKey(rootFeature)), Set.of());
    var writesCorrespondence = footprint(Set.of(),
        Set.of("corr:t|" + MM + "#Root|" + MM + "#NonRoot"));
    var readsSwapped = footprint(Set.of("corr:t|" + MM + "#NonRoot|" + MM + "#Root"), Set.of());
    var readsOtherTag = footprint(Set.of("corr:u|" + MM + "#NonRoot|" + MM + "#Root"), Set.of());
    var readsAnyTag = footprint(Set.of("corr:*|" + MM + "#NonRoot|" + MM + "#Root"), Set.of());

    assertKeyConflict(true, writesAllNonRoots, readsNonRootFeature);
    assertKeyConflict(false, writesAllNonRoots, readsRootFeature);
    assertKeyConflict(true, writesCorrespondence, readsSwapped);
    assertKeyConflict(false, writesCorrespondence, readsOtherTag);
    assertKeyConflict(true, writesCorrespondence, readsAnyTag);
    assertKeyConflict(false, readsNonRootFeature, readsSwapped);
  }

  private void assertKeyConflict(boolean expected, PropagationFootprint a,
      PropagationFootprint b) {
    assertEquals(expected, a.conflictsWith(b, footprints.keyConflict()), "keyConflict");
  }

  /** Not an assertion test: writes the report to target/, to inspect precision. */
  @Test
  void report() throws IOException {
    FootprintReport.Result result = FootprintReport.analyze(specifications);
    Path out = Path.of("target", "access-analysis");
    Files.createDirectories(out);
    List<String> lines = new ArrayList<>();
    lines.add(result.toString());
    lines.add("opaque calls: " + result.opaqueCalls());
    lines.addAll(result.details());
    Files.write(out.resolve("AllElementTypesRedundancy.report.txt"), lines);
    System.out.println("FOOTPRINT REPORT " + result);
  }
}
