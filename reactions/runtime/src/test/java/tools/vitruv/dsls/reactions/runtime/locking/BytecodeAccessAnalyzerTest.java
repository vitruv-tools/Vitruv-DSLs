package tools.vitruv.dsls.reactions.runtime.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tools.vitruv.dsls.reactions.runtime.locking.EmfModelIndexTestSupport.assertContains;

import allElementTypes.AllElementTypesPackage;
import java.util.List;
import mir.reactions.locking.sample.SampleReaction;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The access analysis on bytecode: a hand-written stand-in for a reaction ({@link SampleReaction})
 * with feature reads and writes, a list mutation, a creation, a deletion, a lambda, a helper
 * method and reflective access.
 */
class BytecodeAccessAnalyzerTest {
  private static final AllElementTypesPackage MM = AllElementTypesPackage.eINSTANCE;
  private static AccessSummary summary;

  @BeforeAll
  static void analyze() {
    MM.eClass();
    var analyzer = new BytecodeAccessAnalyzer(new EmfModelIndex(),
        BytecodeAccessAnalyzerTest.class.getClassLoader());
    summary = analyzer.analyzeReaction(SampleReaction.class);
  }

  @Test
  void featureReadsAndWrites() {
    assertContains(summary.getReadFeatures(), MM.getRoot_SingleValuedPrimitiveTypeEAttribute());
    assertContains(summary.getWrittenFeatures(), MM.getRoot_SingleValuedEAttribute());
    assertFalse(summary.getWrittenFeatures().contains(
        MM.getRoot_SingleValuedPrimitiveTypeEAttribute()), "only read");
  }

  @Test
  void helperMethodsAndListMutations() {
    assertContains(summary.getWrittenFeatures(), MM.getRoot_MultiValuedContainmentEReference());
    assertContains(summary.getCreatedClasses(), MM.getNonRoot());
  }

  @Test
  void lambdaBodies() {
    assertContains(summary.getWrittenFeatures(), MM.getIdentified_Id());
  }

  @Test
  void deletionsAndReflectiveAccess() {
    assertContains(summary.getDeletedClasses(), MM.getNonRoot());
    assertContains(summary.getReadClasses(), MM.getRoot());
  }

  @Test
  void writesKeepTheirReceiverClass() {
    assertTrue(summary.featureWrites.stream().anyMatch(write ->
        write.feature() == MM.getIdentified_Id() && write.receiver() == MM.getRoot()),
        () -> "receiver of setId: " + summary.featureWrites);
  }

  @Test
  void keysAndConflicts() {
    String feature = AccessSummary.featureKey(MM.getRoot_SingleValuedEAttribute());
    String otherFeature =
        AccessSummary.featureKey(MM.getRoot_SingleValuedPrimitiveTypeEAttribute());
    var writer = new PropagationFootprint(Set.of(), Set.of(feature), List.of(), null);
    var reader = new PropagationFootprint(Set.of(feature), Set.of(), List.of(), null);
    var unrelated = new PropagationFootprint(Set.of(otherFeature), Set.of(), List.of(), null);
    PropagationFootprint.KeyConflict equalKeys = String::equals;
    assertTrue(writer.conflictsWith(reader, equalKeys));
    assertTrue(reader.conflictsWith(writer, equalKeys));
    assertFalse(reader.conflictsWith(reader, equalKeys), "two readers do not conflict");
    assertFalse(writer.conflictsWith(unrelated, equalKeys));
    assertEquals(feature, AccessSummary.featureKey(MM.getRoot_SingleValuedEAttribute()));
  }
}
