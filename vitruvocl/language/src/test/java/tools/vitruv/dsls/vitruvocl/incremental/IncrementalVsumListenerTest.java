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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.incremental.support.CorrespondingElementsSpecification;
import tools.vitruv.dsls.vitruvocl.incremental.support.RealVsumFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * End-to-end: a session attached to a real VSUM via {@link
 * VitruvOCL#attachIncrementalSession}, one transaction per {@code commitChanges()}, with
 * consistency preservation ({@link CorrespondingElementsSpecification}) changing the second model
 * and the correspondence model — including a correspondence created without any event on its
 * partner. Every transaction is checked by the oracle.
 */
class IncrementalVsumListenerTest {

  private static final List<String> CONSTRAINTS =
      List.of(
          "context incrb::BElem inv bName: self.name != \"bad\"",
          "context incra::AElem inv oneCorrespondent: incrb::BElem.allInstances().select(~).size() <= 1",
          "context incra::AElem inv smallTrigger: self.trigger < 5",
          "context incrb::BRoot inv fewElements: self.elements.size() <= 3");

  @TempDir Path storage;

  private InternalVirtualModel vsum;
  private IncrementalSession session;

  @AfterEach
  void tearDown() {
    if (session != null) {
      session.close();
    }
    VitruvOCL.clearVSUM();
    if (vsum != null) {
      vsum.dispose();
    }
  }

  static Stream<IncrementalLevel> levels() {
    return IncrementalOracleTest.levels().stream();
  }

  private void commit(Consumer<CommittableView> edit) throws Exception {
    try (CommittableView view = RealVsumFixture.openView(vsum)) {
      edit.accept(view);
      view.commitChanges();
    }
  }

  private static EObject rootA(CommittableView view) {
    return view.getRootObjects().stream()
        .filter(r -> r.eClass() == RealVsumFixture.rootA())
        .findFirst()
        .orElseThrow();
  }

  @SuppressWarnings("unchecked")
  private static List<EObject> elements(EObject root) {
    return (List<EObject>) root.eGet(root.eClass().getEStructuralFeature("elements"));
  }

  private static EObject newElem(String name) {
    EObject e = EcoreUtil.create(RealVsumFixture.elemA());
    e.eSet(RealVsumFixture.elemA().getEStructuralFeature("name"), name);
    return e;
  }

  private static boolean violated(IncrementalResult result, String name) {
    for (ConstraintResult r : result.result().getResults()) {
      if (r.getConstraint().contains(" " + name + ":")) {
        return r.isSuccess() && !r.isSatisfied();
      }
    }
    throw new IllegalArgumentException(name);
  }

  @ParameterizedTest
  @MethodSource("levels")
  void everyCommitIsOneOracleCheckedTransaction(IncrementalLevel level) throws Exception {
    vsum = RealVsumFixture.build(storage, new CorrespondingElementsSpecification());
    VitruvOCL.registerVSUM(vsum);
    List<IncrementalResult> results = new ArrayList<>();
    List<RuntimeException> failures = new ArrayList<>();
    session =
        VitruvOCL.attachIncrementalSession(
            CONSTRAINTS, IncrementalConfig.of(level).withVerify(true), results::add, failures::add);

    commit(
        view ->
            view.registerRoot(
                EcoreUtil.create(RealVsumFixture.rootA()),
                URI.createFileURI(storage.resolve("m." + RealVsumFixture.EXT_A).toString())));
    commit(view -> elements(rootA(view)).addAll(List.of(newElem("x"), newElem("y"))));
    assertFalse(violated(results.get(results.size() - 1), "fewElements"), "spare + x + y");

    // late correspondence x ~ spare: no event on any BElem, only the trigger update on x
    commit(
        view ->
            elements(rootA(view)).get(0).eSet(RealVsumFixture.elemA().getEStructuralFeature("trigger"), 1));
    assertTrue(violated(results.get(results.size() - 1), "oneCorrespondent"));

    commit(view -> elements(rootA(view)).add(newElem("bad")));
    IncrementalResult afterBad = results.get(results.size() - 1);
    assertTrue(violated(afterBad, "bName"));
    assertTrue(violated(afterBad, "fewElements"));

    commit(
        view ->
            elements(rootA(view)).get(0).eSet(RealVsumFixture.elemA().getEStructuralFeature("trigger"), 2));
    assertFalse(violated(results.get(results.size() - 1), "oneCorrespondent"));

    commit(view -> elements(rootA(view)).remove(elements(rootA(view)).size() - 1));
    IncrementalResult last = results.get(results.size() - 1);
    assertFalse(violated(last, "bName"));
    assertFalse(violated(last, "fewElements"));

    assertTrue(failures.isEmpty(), "oracle mismatches or errors: " + failures);
    assertEquals(6, results.size(), "one result per commit");

    session.close();
    commit(view -> elements(rootA(view)).add(newElem("z")));
    assertEquals(6, results.size(), "closing the session removes the listener");
  }
}
