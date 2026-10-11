/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.incremental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ReadSet;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/** Unit tests of the static read-set analysis (level 1) on the {@code edge} metamodel. */
class ReadSetAnalyzerTest {

  private static final EdgeFixture FX = EdgeFixture.create();

  private static ReadSet readSet(String constraint) {
    CompiledConstraint compiled = CompiledConstraint.compile(constraint, FX.model().wrapper());
    assertTrue(compiled.isCompiled(), "must compile: " + constraint);
    return compiled.analysis().readSet();
  }

  private static EClass cls(String name) {
    return (EClass) EdgeFixture.PACKAGE.getEClassifier(name);
  }

  @Test
  void contextTypeAndSelfFeature() {
    ReadSet rs = readSet("context edge::Item inv: self.value >= 0");
    assertEquals(Set.of(cls("Item")), rs.extensionTypes());
    assertEquals(Set.of(cls("Item")), rs.typedFeatureReads().get("value"));
    assertTrue(rs.untypedFeatureReads().isEmpty());
    assertFalse(rs.requiresFullEvaluation());
  }

  @Test
  void navigationChainUsesStaticReceiverTypes() {
    ReadSet rs = readSet("context edge::Root inv: self.items.select(i | i.next.value > 0).size() >= 0");
    assertEquals(Set.of(cls("Root")), rs.typedFeatureReads().get("items"));
    assertEquals(Set.of(cls("Item")), rs.typedFeatureReads().get("next"));
    assertEquals(Set.of(cls("Item")), rs.typedFeatureReads().get("value"));
  }

  @Test
  void castTargetTypeIsTheReceiverOfTheFollowingFeature() {
    ReadSet rs = readSet("context edge::Item inv: self.oclAsType(edge::SubA).extra >= 0");
    assertEquals(Set.of(cls("SubA")), rs.typedFeatureReads().get("extra"));
  }

  @Test
  void allInstancesTypeIsAnExtensionType() {
    ReadSet rs = readSet("context edge::Root inv: edge::Other.allInstances().size() >= 0");
    assertEquals(Set.of(cls("Root"), cls("Other")), rs.extensionTypes());
  }

  @Test
  void messageTemplateAttributesAreRead() {
    ReadSet rs = readSet("context edge::Item inv: @message \"{self.name}\" self.value >= 0");
    assertEquals(Set.of(cls("Item")), rs.typedFeatureReads().get("name"));
  }

  @Test
  void flagsForTransactionDependentConstructs() {
    assertTrue(readSet("context edge::Item post: self.value >= self.value@pre").usesPre());
    assertTrue(readSet("context edge::Item post: self.OCLisNew").usesLifecycle());
    assertTrue(readSet("context edge::Item post: self.value >= 0").isPrePost());
    assertTrue(
        readSet("context edge::Item inv: edge::Other.allInstances().select(~).size() >= 0")
            .usesCorrespondence());
  }

  @Test
  void transientFeatureForcesFullEvaluation() {
    ReadSet rs = readSet("context edge::Item inv: self.cache >= 0");
    assertTrue(rs.readsUnrecordedFeature());
    assertTrue(rs.requiresFullEvaluation());
  }

  @Test
  void containerFeatureMakesTheOwnerClassAnExtensionType() {
    ReadSet rs = readSet("context edge::Root inv: self.items.forAll(i | i.owner.tags.size() <= 2)");
    assertFalse(rs.readsUnrecordedFeature());
    assertTrue(rs.extensionTypes().contains(cls("Item")), rs.extensionTypes().toString());
  }

  @Test
  void appendMakesAllReadsUntyped() {
    ReadSet rs =
        readSet(
            "context edge::Root inv: self.items.append(self.items.first()).forAll(x | x.value >= 0)");
    assertTrue(rs.typedFeatureReads().isEmpty(), rs.typedFeatureReads().toString());
    assertTrue(rs.untypedFeatureReads().contains("value"));
  }

  @Test
  void referenceInMessageTemplateForcesFullEvaluation() {
    assertTrue(readSet("context edge::Item inv: @message \"{self.next}\" self.value >= 0").requiresFullEvaluation());
    assertFalse(readSet("context edge::Item inv: @message \"{self.name}\" self.value >= 0").requiresFullEvaluation());
  }

  @Test
  void uncompilableConstraintHasNoReadSet() {
    CompiledConstraint compiled =
        CompiledConstraint.compile("context edge::Item inv: self.nope > 0", FX.model().wrapper());
    assertFalse(compiled.isCompiled());
    assertNull(compiled.analysis().readSet());
  }
}
