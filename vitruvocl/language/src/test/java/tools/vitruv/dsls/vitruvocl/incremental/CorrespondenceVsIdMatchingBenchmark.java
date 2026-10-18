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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Manual benchmark: the same consistency rule expressed once with the correspondence operator and
 * once with ID-based matching ({@code o.label == self.name}), over the real Vitruvius
 * correspondence model. Model: {@code n} {@code SubA} and {@code n} {@code Other}; the first half
 * of each corresponds 1:1 <em>and</em> shares a key, so both formulations have the same results.
 * Measured: full evaluation (as in the batch evaluation) and an incremental session at level 3
 * (a transaction changing one {@code Other.amount}, and a "re-link" transaction: a new
 * correspondence for {@code ~}, a key change for ID matching). Disabled by default; run with
 * {@code -Dvitruvocl.bench.corrVsId=true} (optionally {@code -Dvitruvocl.bench.corrSizes=...}).
 */
class CorrespondenceVsIdMatchingBenchmark {

  private static final int WARMUP = 1;
  private static final int RUNS = 3;
  private static final int TRANSACTIONS = 5;

  private record Variant(String name, String correspondence, String idMatching) {}

  private static final List<Variant> VARIANTS =
      List.of(
          new Variant(
              "select",
              "context edge::SubA inv: edge::Other.allInstances().select(~).size() <= 1",
              "context edge::SubA inv: edge::Other.allInstances().select(o | o.label == self.name).size() <= 1"),
          new Variant(
              "forAll-guard",
              "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)",
              "context edge::SubA inv: edge::Other.allInstances().forAll(o | o.label == self.name implies o.amount > 0)"),
          new Variant(
              "exists",
              "context edge::SubA inv: self.value >= 0 or edge::Other.allInstances().exists(o | self ~ o)",
              "context edge::SubA inv: self.value >= 0 or edge::Other.allInstances().exists(o | o.label == self.name)"));

  @Test
  void measure() {
    assumeTrue(Boolean.getBoolean("vitruvocl.bench.corrVsId"), "benchmark disabled");
    String sizes = System.getProperty("vitruvocl.bench.corrSizes", "500,1000,2000");

    System.out.println("Full evaluation [ms]");
    System.out.println("| Variante | n | Korrespondenzen | ~ | ID-Matching | ~ / ID |");
    System.out.println("|---|---:|---:|---:|---:|---:|");
    for (String size : sizes.split(",")) {
      int n = Integer.parseInt(size.trim());
      Model model = new Model(n);
      for (Variant v : VARIANTS) {
        double corr = timeFull(v.correspondence(), model.wrapper);
        double id = timeFull(v.idMatching(), model.wrapper);
        System.out.printf("| %s | %d | %d | %.0f | %.0f | %.2f |%n", v.name(), n, n / 2, corr, id, corr / id);
      }
    }

    System.out.println();
    System.out.println("Incremental session, level 3 [ms per transaction]");
    System.out.println(
        "| Variante | n | init ~ | init ID | Modell-Tx ~ | Modell-Tx ID | Relink-Tx ~ | Relink-Tx ID |");
    System.out.println("|---|---:|---:|---:|---:|---:|---:|---:|");
    for (String size : sizes.split(",")) {
      int n = Integer.parseInt(size.trim());
      for (Variant v : VARIANTS) {
        double[] corr = timeSession(v.correspondence(), n, true);
        double[] id = timeSession(v.idMatching(), n, false);
        System.out.printf(
            "| %s | %d | %.0f | %.0f | %.1f | %.1f | %.1f | %.1f |%n",
            v.name(), n, corr[0], id[0], corr[1], id[1], corr[2], id[2]);
      }
    }
  }

  private static double timeFull(String constraint, VsumWrapper wrapper) {
    double total = 0;
    for (int run = 0; run < WARMUP + RUNS; run++) {
      long t0 = System.nanoTime();
      EvaluationOutcome outcome =
          ConstraintEvaluations.evaluateFresh(constraint, wrapper, List.of(), false);
      long t1 = System.nanoTime();
      assertFalse(outcome.failed(), () -> "failed: " + constraint);
      assertEquals(0, outcome.records().size(), constraint);
      if (run >= WARMUP) {
        total += (t1 - t0) / 1e6;
      }
    }
    return total / RUNS;
  }

  /** {init, model transaction, re-link transaction} in ms. */
  private static double[] timeSession(String constraint, int n, boolean correspondence) {
    Model model = new Model(n);
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(constraint), model.wrapper, IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES))) {
      long t0 = System.nanoTime();
      session.initialize();
      double init = (System.nanoTime() - t0) / 1e6;
      double modelTx = 0;
      double relinkTx = 0;
      for (int i = 0; i < TRANSACTIONS; i++) {
        EObject other = model.others.get(i);
        int amount = 2 + i;
        List<EChange<EObject>> changes =
            model.record(() -> other.eSet(EdgeFixture.feature(other, "amount"), amount));
        long t1 = System.nanoTime();
        session.onTransaction(changes);
        modelTx += (System.nanoTime() - t1) / 1e6;

        // link an unmatched SubA to an unmatched Other
        EObject a = model.items.get(n - 1 - i);
        EObject o = model.others.get(n - 1 - i);
        List<EChange<EObject>> relink;
        if (correspondence) {
          model.view.addCorrespondenceBetween(a, o, null);
          relink = List.of();
        } else {
          relink =
              model.record(
                  () -> o.eSet(EdgeFixture.feature(o, "label"), a.eGet(EdgeFixture.feature(a, "name"))));
        }
        long t2 = System.nanoTime();
        session.onTransaction(relink);
        relinkTx += (System.nanoTime() - t2) / 1e6;
      }
      return new double[] {init, modelTx / TRANSACTIONS, relinkTx / TRANSACTIONS};
    }
  }

  /** The benchmark model: corresponding pairs also share a key. */
  private static final class Model {
    final ResourceSet resourceSet = new ResourceSetImpl();
    final Resource resource;
    final EditableCorrespondenceModelView<Correspondence> view;
    final VsumWrapper wrapper;
    final List<EObject> items = new ArrayList<>();
    final List<EObject> others = new ArrayList<>();

    Model(int n) {
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
      resource = resourceSet.createResource(URI.createURI("memory:/vsid.edge"));
      EObject root = EdgeFixture.create("Root");
      resource.getContents().add(root);
      view =
          CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
              CorrespondenceModelFactory.createPersistableCorrespondenceModel(
                  URI.createURI("memory:/vsid.correspondence")));
      for (int i = 0; i < n; i++) {
        EObject a = EdgeFixture.item("SubA", "k" + i, i);
        EObject o = EdgeFixture.create("Other");
        o.eSet(EdgeFixture.feature(o, "amount"), 1);
        o.eSet(EdgeFixture.feature(o, "label"), i < n / 2 ? "k" + i : "u" + i);
        EdgeFixture.list(root, "items").add(a);
        EdgeFixture.list(root, "others").add(o);
        items.add(a);
        others.add(o);
        if (i < n / 2) {
          view.addCorrespondenceBetween(a, o, null);
        }
      }
      InternalVirtualModel vsum =
          mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));
      when(((ViewSource) vsum).getViewSourceModels()).thenReturn(List.of(resource));
      when(vsum.getCorrespondenceModel()).thenReturn(view);
      wrapper = new VsumWrapper(vsum);
    }

    List<EChange<EObject>> record(Runnable edit) {
      try (ChangeRecorder recorder = new ChangeRecorder(resourceSet)) {
        recorder.addToRecording(resource);
        recorder.beginRecording();
        edit.run();
        return new ArrayList<>(recorder.endRecording().getEChanges());
      }
    }
  }
}
