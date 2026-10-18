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
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.vitruv.dsls.vitruvocl.incremental.support.CorrespondingElementsSpecification;
import tools.vitruv.dsls.vitruvocl.incremental.support.RealVsumFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * End-to-end check of every form of {@code ~} (binary in both operand orders, {@code
 * select/reject/exists(~)}, with {@code Type=} and {@code Tag=}) through the evaluator and {@link
 * VsumWrapper} over a real VSUM.
 *
 * <p>The model contains a single-object correspondence {@code xa ~ xb} (tag {@code elem}) and a
 * multi-element one {@code [xa, rootA] ~ [spare]} (tag {@code multi}), which Vitruvius returns only
 * when looking up {@code spare}. Every constraint is satisfied iff the evaluator checks both
 * directions; a one-directional check makes the {@code spare} cases fail.
 */
class CorrespondenceFormsEndToEndTest {

  @TempDir Path storage;

  private InternalVirtualModel vsum;

  @BeforeEach
  void setUp() throws Exception {
    vsum = RealVsumFixture.build(storage, new CorrespondingElementsSpecification());
    commit(
        view ->
            view.registerRoot(
                EcoreUtil.create(RealVsumFixture.rootA()),
                URI.createFileURI(storage.resolve("m." + RealVsumFixture.EXT_A).toString())));
    commit(
        view -> {
          EObject x = EcoreUtil.create(RealVsumFixture.elemA());
          x.eSet(RealVsumFixture.elemA().getEStructuralFeature("name"), "x");
          elements(viewRootA(view)).add(x);
        });
    setTrigger(4); // [xa, rootA] ~ [spare] (multi)
  }

  @AfterEach
  void tearDown() {
    if (vsum != null) {
      vsum.dispose();
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // F1: binary, both operand orders, in iterator bodies
        "context incra::AElem inv: incrb::BElem.allInstances()"
            + ".exists(b | self ~ b and b.name == \"spare\")",
        "context incra::AElem inv: incrb::BElem.allInstances()"
            + ".exists(b | b ~ self and b.name == \"spare\")",
        "context incra::AElem inv: incrb::BElem.allInstances()"
            + ".select(b | self ~ b).size() == 2",
        "context incra::AElem inv: incrb::BElem.allInstances()"
            + ".forAll(b | b.name == \"spare\" implies b ~ self)",
        "context incrb::BElem inv: incra::AElem.allInstances().forAll(a | a ~ self)",
        // F2: select(~) with and without options
        "context incra::AElem inv: incrb::BElem.allInstances().select(~).size() == 2",
        "context incra::AElem inv: incrb::BElem.allInstances().select(~, Tag = \"multi\").size() == 1",
        "context incra::AElem inv: incrb::BElem.allInstances().select(~, Tag = \"elem\").size() == 1",
        "context incra::AElem inv: incrb::BElem.allInstances().select(~, Tag = \"late\").isEmpty()",
        "context incra::AElem inv: incrb::BElem.allInstances()"
            + ".select(~, Type = incrb::BElem, Tag = \"multi\").size() == 1",
        "context incra::ARoot inv: incrb::BElem.allInstances().select(~, Tag = \"multi\").size() == 1",
        "context incrb::BElem inv: incra::AElem.allInstances().select(~).size() == 1",
        // F3: reject(~)
        "context incra::AElem inv: incrb::BElem.allInstances().reject(~).isEmpty()",
        "context incra::AElem inv: incrb::BElem.allInstances().reject(~, Tag = \"multi\").size() == 1",
        // F4: exists(~)
        "context incra::AElem inv: incrb::BElem.allInstances().exists(~, Tag = \"multi\")",
        "context incra::ARoot inv: incrb::BElem.allInstances().exists(~)",
        "context incra::AElem inv: not incrb::BElem.allInstances().exists(~, Tag = \"root\")"
      })
  void everyFormSeesCorrespondencesFromBothSides(String constraint) {
    EvaluationOutcome outcome =
        ConstraintEvaluations.evaluateFresh(constraint, new VsumWrapper(vsum), List.of(), false);

    assertFalse(outcome.failed(), () -> "compilation failed: " + outcome.errors());
    assertTrue(outcome.evaluatedInstances() > 0, "no context instance evaluated");
    assertEquals(List.of(), outcome.records(), "violations of: " + constraint);
  }

  // ---------------------------------------------------------------------------

  private void setTrigger(int value) throws Exception {
    commit(
        view ->
            named(elements(viewRootA(view)), "x")
                .eSet(RealVsumFixture.elemA().getEStructuralFeature("trigger"), value));
  }

  private void commit(Consumer<CommittableView> edit) throws Exception {
    try (CommittableView view = RealVsumFixture.openView(vsum)) {
      edit.accept(view);
      view.commitChanges();
    }
  }

  private static EObject viewRootA(CommittableView view) {
    return view.getRootObjects().stream()
        .filter(r -> r.eClass() == RealVsumFixture.rootA())
        .findFirst()
        .orElseThrow();
  }

  @SuppressWarnings("unchecked")
  private static List<EObject> elements(EObject root) {
    return (List<EObject>) root.eGet(root.eClass().getEStructuralFeature("elements"));
  }

  private static EObject named(List<EObject> elements, String name) {
    return elements.stream()
        .filter(e -> name.equals(e.eGet(e.eClass().getEStructuralFeature("name"))))
        .findFirst()
        .orElseThrow();
  }
}
