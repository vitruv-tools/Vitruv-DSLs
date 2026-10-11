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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/** Unit tests of the EChange → event mapping (report §4) on changes recorded by Vitruvius. */
class EChangeClassifierTest {

  private static EClass cls(String name) {
    return (EClass) EdgeFixture.PACKAGE.getEClassifier(name);
  }

  private static List<ModelEvent.Kind> kinds(TransactionDelta delta) {
    return delta.events().stream().map(ModelEvent::kind).toList();
  }

  @Test
  void attributeReplaceIsUpdateAttribute() {
    EdgeFixture fx = EdgeFixture.create();
    EObject a1 = fx.find("a1");
    TransactionDelta delta =
        EChangeClassifier.classify(fx.model().record(() -> a1.eSet(EdgeFixture.feature(a1, "value"), 8)));
    assertEquals(List.of(ModelEvent.Kind.UPDATE_ATTRIBUTE), kinds(delta));
    assertTrue(delta.touchedFeatures().get("value").contains(cls("SubA")));
    assertTrue(delta.structurallyAffectedClasses().isEmpty());
  }

  @Test
  void containmentInsertIsStructuralForTheNewObject() {
    EdgeFixture fx = EdgeFixture.create();
    TransactionDelta delta =
        EChangeClassifier.classify(
            fx.model().record(() -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.item("SubB", "n", 1))));
    assertTrue(kinds(delta).contains(ModelEvent.Kind.INSERT_REFERENCE), kinds(delta).toString());
    assertTrue(delta.touchedFeatures().get("items").contains(cls("Root")));
    assertTrue(delta.structurallyAffectedClasses().contains(cls("SubB")));
    assertTrue(delta.affectsExtensionOf(cls("Item")));
    assertFalse(delta.affectsExtensionOf(cls("Other")));
  }

  @Test
  void containmentMoveIsStructuralWithoutCreateOrDelete() {
    EdgeFixture fx = EdgeFixture.create();
    TransactionDelta delta =
        EChangeClassifier.classify(
            fx.model().record(() -> EdgeFixture.list(fx.root(1), "items").add(fx.find("b2"))));
    assertFalse(kinds(delta).contains(ModelEvent.Kind.CREATE));
    assertFalse(kinds(delta).contains(ModelEvent.Kind.DELETE));
    assertTrue(delta.affectsExtensionOf(cls("SubB")));
  }

  @Test
  void deleteIncludesTheRemovedSubtree() {
    EdgeFixture fx = EdgeFixture.create();
    TransactionDelta delta =
        EChangeClassifier.classify(fx.model().record(() -> EcoreUtil.delete(fx.root(1), true)));
    assertTrue(delta.affectsExtensionOf(cls("Root")));
    assertTrue(delta.affectsExtensionOf(cls("SubA")), "contents of the deleted root count as well");
    assertTrue(delta.affectsExtensionOf(cls("Other")));
  }

  @Test
  void duplicateEventsAreKeptOnceButLinkAndUnlinkAreNotCancelled() {
    EdgeFixture fx = EdgeFixture.create();
    EObject b1 = fx.find("b1");
    EObject a1 = fx.find("a1");
    TransactionDelta delta =
        EChangeClassifier.classify(
            fx.model().record(
                () -> {
                  List<Object> refs = EdgeFixture.list(b1, "refs");
                  refs.add(a1);
                  refs.remove(refs.size() - 1);
                }));
    assertEquals(
        List.of(ModelEvent.Kind.INSERT_REFERENCE, ModelEvent.Kind.REMOVE_REFERENCE), kinds(delta));
  }

  @Test
  void multiValuedAttributeEvents() {
    EdgeFixture fx = EdgeFixture.create();
    TransactionDelta delta =
        EChangeClassifier.classify(fx.model().record(() -> EdgeFixture.list(fx.root(1), "tags").add("t")));
    assertEquals(List.of(ModelEvent.Kind.INSERT_ATTRIBUTE_VALUE), kinds(delta));
  }
}
