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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.EObjectExistenceEChange;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.feature.reference.UpdateReferenceEChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.atomic.root.RemoveRootEObject;
import tools.vitruv.change.atomic.feature.list.InsertInListEChange;
import tools.vitruv.change.atomic.feature.list.RemoveFromListEChange;
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.dsls.vitruvocl.incremental.support.CorrespondingElementsSpecification;
import tools.vitruv.dsls.vitruvocl.incremental.support.RealVsumFixture;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Experiment for decision 3 of the incremental-evaluation plan: are correspondence-model changes
 * performed by consistency-preservation code (here: {@link CorrespondingElementsSpecification}, a
 * hand-written stand-in for generated Reactions code) visible in the {@code EChange} stream that
 * {@code finishedChangePropagation} delivers, and can a correspondence appear or disappear without
 * any observed event on its partner objects?
 *
 * <p>The assertions pin down the <em>observed</em> behaviour of Vitruvius {@code 4.0.1-SNAPSHOT}
 * so that a framework change that alters it makes this test fail (and the conservative
 * correspondence handling in the incremental evaluator must then be revisited). Results are
 * documented in {@code docs/incremental-implementation-report.md}.
 */
class CorrespondenceVisibilityExperimentTest {

  @TempDir Path storage;

  private InternalVirtualModel vsum;
  private final List<List<EChange<EObject>>> transactions = new ArrayList<>();

  @AfterEach
  void tearDown() {
    if (vsum != null) {
      vsum.dispose();
    }
  }

  private void startVsum() throws Exception {
    vsum = RealVsumFixture.build(storage, new CorrespondingElementsSpecification());
    vsum.addChangePropagationListener(
        new ChangePropagationListener() {
          @Override
          public void startedChangePropagation(VitruviusChange<Uuid> changeToPropagate) {
            // nothing to do: only the propagated result is of interest
          }

          @Override
          public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
            List<EChange<EObject>> all = new ArrayList<>();
            for (PropagatedChange pc : propagatedChanges) {
              all.addAll(pc.getOriginalChange().getEChanges());
              all.addAll(pc.getConsequentialChanges().getEChanges());
            }
            transactions.add(all);
          }
        });
  }

  /** Every object an EChange directly mentions (affected element and old/new values). */
  private static List<Object> mentionedObjects(EChange<EObject> change) {
    List<Object> result = new ArrayList<>();
    if (change instanceof FeatureEChange<?, ?> fc) {
      result.add(fc.getAffectedElement());
    }
    if (change instanceof EObjectExistenceEChange<?> ec) {
      result.add(ec.getAffectedElement());
    }
    if (change instanceof InsertRootEObject<?> ir) {
      result.add(ir.getNewValue());
    }
    if (change instanceof RemoveRootEObject<?> rr) {
      result.add(rr.getOldValue());
    }
    if (change instanceof UpdateReferenceEChange<?>) {
      if (change instanceof InsertInListEChange<?, ?, ?> il) {
        result.add(il.getNewValue());
      }
      if (change instanceof RemoveFromListEChange<?, ?, ?> rl) {
        result.add(rl.getOldValue());
      }
      if (change instanceof ReplaceSingleValuedFeatureEChange<?, ?, ?> rs) {
        result.add(rs.getOldValue());
        result.add(rs.getNewValue());
      }
    }
    return result;
  }

  private static boolean touchesCorrespondenceModel(List<EChange<EObject>> changes) {
    for (EChange<EObject> change : changes) {
      for (Object o : mentionedObjects(change)) {
        if (o instanceof Correspondence) {
          return true;
        }
        if (o instanceof EObject eo
            && eo.eClass().getEPackage().getNsURI() != null
            && eo.eClass().getEPackage().getNsURI().toLowerCase().contains("correspondence")) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean mentions(List<EChange<EObject>> changes, EObject object) {
    return changes.stream().flatMap(c -> mentionedObjects(c).stream()).anyMatch(o -> o == object);
  }

  private Resource vsumResource(String extension) {
    return vsum.getViewSourceModels().stream()
        .filter(r -> extension.equals(r.getURI().fileExtension()))
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

  private void commit(java.util.function.Consumer<CommittableView> edit) throws Exception {
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

  @Test
  void correspondenceChangesAreNotPartOfTheEChangeStream_andCanOccurWithoutPartnerEvents()
      throws Exception {
    startVsum();

    // T1: new ARoot -> spec creates BRoot (+ spare BElem) and ARoot ~ BRoot
    commit(
        view ->
            view.registerRoot(
                EcoreUtil.create(RealVsumFixture.rootA()),
                URI.createFileURI(storage.resolve("m." + RealVsumFixture.EXT_A).toString())));
    EObject rootA = vsumResource(RealVsumFixture.EXT_A).getContents().get(0);
    EObject rootB = vsumResource(RealVsumFixture.EXT_B).getContents().get(0);
    assertTrue(
        vsum.getCorrespondenceModel().getCorrespondingEObjects(rootA).contains(rootB),
        "the specification must have created ARoot ~ BRoot");

    // T2: insert AElem 'x' -> spec creates BElem 'x' and x ~ x
    commit(
        view -> {
          EObject x = EcoreUtil.create(RealVsumFixture.elemA());
          x.eSet(RealVsumFixture.elemA().getEStructuralFeature("name"), "x");
          elements(viewRootA(view)).add(x);
        });
    EObject xa = named(elements(rootA), "x");
    EObject xb = named(elements(rootB), "x");
    EObject spare = named(elements(rootB), CorrespondingElementsSpecification.SPARE_NAME);
    assertTrue(vsum.getCorrespondenceModel().getCorrespondingEObjects(xa).contains(xb));

    // T3: trigger := 1 -> spec adds xa ~ spare between two pre-existing objects
    commit(
        view ->
            named(elements(viewRootA(view)), "x")
                .eSet(RealVsumFixture.elemA().getEStructuralFeature("trigger"), 1));
    assertTrue(
        vsum.getCorrespondenceModel().getCorrespondingEObjects(xa).contains(spare),
        "late correspondence xa ~ spare must exist after T3");
    List<EChange<EObject>> t3 = transactions.get(transactions.size() - 1);

    // T4: trigger := 2 -> spec removes xa ~ spare again
    commit(
        view ->
            named(elements(viewRootA(view)), "x")
                .eSet(RealVsumFixture.elemA().getEStructuralFeature("trigger"), 2));
    assertFalse(vsum.getCorrespondenceModel().getCorrespondingEObjects(xa).contains(spare));
    List<EChange<EObject>> t4 = transactions.get(transactions.size() - 1);

    // T5: remove AElem 'x' -> spec removes x ~ x and deletes BElem 'x'
    commit(view -> elements(viewRootA(view)).remove(named(elements(viewRootA(view)), "x")));
    assertTrue(vsum.getCorrespondenceModel().getCorrespondingEObjects(xa).isEmpty());

    assertEquals(5, transactions.size(), "one finishedChangePropagation callback per commit");

    // Finding 1: no transaction mentions any object of the correspondence model.
    for (List<EChange<EObject>> t : transactions) {
      assertFalse(touchesCorrespondenceModel(t), "correspondence model changes visible: " + t);
    }

    // Finding 2: in T3/T4 the correspondence xa ~ spare appeared/disappeared while the stream
    // contains no event on the partner 'spare' (only the trigger update on xa).
    assertFalse(mentions(t3, spare), "T3 must not mention the partner 'spare'");
    assertFalse(mentions(t4, spare), "T4 must not mention the partner 'spare'");
    assertTrue(mentions(t3, xa));
    assertTrue(mentions(t4, xa));
    assertEquals(1, t3.size(), "T3 consists of exactly the trigger update: " + t3);
    assertEquals(1, t4.size(), "T4 consists of exactly the trigger update: " + t4);
  }
}
