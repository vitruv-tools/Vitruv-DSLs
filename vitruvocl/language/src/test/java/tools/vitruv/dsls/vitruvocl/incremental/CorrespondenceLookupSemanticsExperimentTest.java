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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.incremental.support.CorrespondingElementsSpecification;
import tools.vitruv.dsls.vitruvocl.incremental.support.RealVsumFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Step 0 of {@code docs/correspondence-incremental-plan.md}: pins down the lookup semantics of the
 * Vitruvius correspondence model that the {@code ~} optimizations (part A) and the correspondence
 * snapshot (part B) rely on, against a real VSUM:
 *
 * <ol>
 *   <li>lookups of single-object correspondences are symmetric, untagged and tagged, but
 *       multi-element correspondences are visible only from their single-object side, so the
 *       evaluator must keep checking both directions;
 *   <li>tagged correspondences appear in the untagged lookup, and tagged lookups match exactly;
 *   <li>concurrent read-only lookups return the sequential result;
 *   <li>what a lookup returns after a partner was deleted without removing its correspondence.
 * </ol>
 *
 * <p>If a Vitruvius update changes one of these, this test fails and the assumptions in the plan
 * (and in {@code CorrespondenceLookup}) must be revisited.
 */
class CorrespondenceLookupSemanticsExperimentTest {

  @TempDir Path storage;

  private InternalVirtualModel vsum;
  private CorrespondenceModelView<Correspondence> correspondences;
  private EObject rootA;
  private EObject rootB;
  private EObject xa;
  private EObject xb;
  private EObject spare;

  @BeforeEach
  void setUp() throws Exception {
    vsum = RealVsumFixture.build(storage, new CorrespondingElementsSpecification());
    correspondences = vsum.getCorrespondenceModel();

    // T1: ARoot -> BRoot + spare BElem, ARoot ~ BRoot (tag root)
    commit(
        view ->
            view.registerRoot(
                EcoreUtil.create(RealVsumFixture.rootA()),
                URI.createFileURI(storage.resolve("m." + RealVsumFixture.EXT_A).toString())));
    // T2: AElem 'x' -> BElem 'x', x ~ x (tag elem)
    commit(
        view -> {
          EObject x = EcoreUtil.create(RealVsumFixture.elemA());
          x.eSet(RealVsumFixture.elemA().getEStructuralFeature("name"), "x");
          elements(viewRootA(view)).add(x);
        });
    // T3: trigger := 1 -> xa ~ spare (tag late)
    setTrigger(1);

    rootA = vsumResource(RealVsumFixture.EXT_A).getContents().get(0);
    rootB = vsumResource(RealVsumFixture.EXT_B).getContents().get(0);
    xa = named(elements(rootA), "x");
    xb = named(elements(rootB), "x");
    spare = named(elements(rootB), CorrespondingElementsSpecification.SPARE_NAME);
  }

  @AfterEach
  void tearDown() {
    if (vsum != null) {
      vsum.dispose();
    }
  }

  @Test
  void singleObjectCorrespondences_lookUpSymmetrically_untaggedAndTagged() {
    assertEquals(Set.of(rootB), correspondences.getCorrespondingEObjects(rootA));
    assertEquals(Set.of(rootA), correspondences.getCorrespondingEObjects(rootB));
    assertEquals(Set.of(xa), correspondences.getCorrespondingEObjects(xb));
    assertEquals(Set.of(xa), correspondences.getCorrespondingEObjects(spare));

    assertEquals(Set.of(xb), correspondences.getCorrespondingEObjects(xa, "elem"));
    assertEquals(Set.of(xa), correspondences.getCorrespondingEObjects(xb, "elem"));
    assertEquals(Set.of(xa), correspondences.getCorrespondingEObjects(spare, "late"));

    // The wrapper the evaluator uses inherits the symmetry for single-object correspondences.
    VsumWrapper wrapper = new VsumWrapper(vsum);
    for (EObject o : List.of(rootA, rootB, xa, xb, spare)) {
      for (EObject partner : wrapper.getCorrespondingObjects(o)) {
        assertTrue(
            wrapper.getCorrespondingObjects(partner).contains(o),
            "asymmetric correspondence " + o + " -> " + partner);
      }
    }
  }

  @Test
  void taggedCorrespondencesAppearInUntaggedLookup_andTagLookupsAreExact() {
    assertEquals(Set.of(xb, spare), correspondences.getCorrespondingEObjects(xa));
    assertEquals(Set.of(spare), correspondences.getCorrespondingEObjects(xa, "late"));
    assertEquals(Set.of(), correspondences.getCorrespondingEObjects(xa, "root"));
    assertEquals(Set.of(), correspondences.getCorrespondingEObjects(xa, "LATE"));
    assertEquals(
        Map.of("elem", Set.of(xb), "late", Set.of(spare)),
        correspondences.getCorrespondingEObjectsWithTag(xa));
    assertEquals(Set.of("root", "elem", "late"), correspondences.getAllTags());
    assertEquals(
        Set.of(rootA, rootB, xa, xb, spare), correspondences.getAllEObjectsInACorrespondence());
  }

  @Test
  void concurrentReadOnlyLookupsReturnTheSequentialResult() throws Exception {
    List<EObject> objects = List.of(rootA, rootB, xa, xb, spare);
    Map<EObject, Set<EObject>> expected = new HashMap<>();
    for (EObject o : objects) {
      expected.put(o, correspondences.getCorrespondingEObjects(o));
    }

    ExecutorService pool = Executors.newFixedThreadPool(8);
    try {
      List<Callable<Boolean>> tasks = new ArrayList<>();
      for (int t = 0; t < 8; t++) {
        tasks.add(
            () -> {
              for (int i = 0; i < 2_000; i++) {
                EObject o = objects.get(i % objects.size());
                if (!expected.get(o).equals(correspondences.getCorrespondingEObjects(o))) {
                  return false;
                }
              }
              return true;
            });
      }
      for (Future<Boolean> result : pool.invokeAll(tasks)) {
        assertTrue(result.get(), "a concurrent lookup differed from the sequential one");
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void deletingAPartnerWithoutRemovingItsCorrespondence_dropsItFromLookups() throws Exception {
    // T4: trigger := 3 -> spare is deleted, xa ~ spare (late) is not removed by the specification
    setTrigger(3);

    assertTrue(
        elements(rootB).stream().noneMatch(e -> e == spare), "spare must be gone from its model");
    assertEquals(
        Set.of(xb),
        correspondences.getCorrespondingEObjects(xa),
        "Vitruvius removes correspondences of deleted elements at the end of the propagation");
    assertEquals(Set.of(), correspondences.getCorrespondingEObjects(xa, "late"));
  }

  @Test
  void multiElementCorrespondences_areOnlyVisibleFromTheSingleObjectSide() throws Exception {
    setTrigger(2); // remove the single-object xa ~ spare (late)
    setTrigger(4); // add [xa, rootA] ~ [spare] (multi)

    // A single-object lookup matches a side only if that side is exactly [object].
    assertEquals(Set.of(xb), correspondences.getCorrespondingEObjects(xa));
    assertEquals(Set.of(rootB), correspondences.getCorrespondingEObjects(rootA));
    assertEquals(Set.of(xa, rootA), correspondences.getCorrespondingEObjects(spare));
    assertEquals(Set.of(), correspondences.getCorrespondingEObjects(xa, "multi"));
    assertEquals(Set.of(xa, rootA), correspondences.getCorrespondingEObjects(spare, "multi"));

    // So the evaluator must check both directions: xa ~ spare holds only via spare's lookup.
    VsumWrapper wrapper = new VsumWrapper(vsum);
    assertTrue(wrapper.correspondenceHasTag(xa, spare, "multi"));
    assertTrue(wrapper.correspondenceHasTag(spare, xa, "multi"));
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

  private Resource vsumResource(String extension) {
    return vsum.getViewSourceModels().stream()
        .filter(r -> extension.equals(r.getURI().fileExtension()))
        .findFirst()
        .orElseThrow();
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
