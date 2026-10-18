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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Manual benchmark for step C4 of {@code docs/correspondence-incremental-plan.md}: an incremental
 * session over the real Vitruvius correspondence model, with and without the shared
 * correspondence lookup. Model: {@code n} {@code SubA} and {@code n} {@code Other}, the first half
 * of each corresponding 1:1. Measured: {@code initialize()}, a transaction changing one {@code
 * Other.amount} (read by the {@code ~} constraints), and a transaction that only adds a
 * correspondence silently. Disabled by default; run with {@code
 * -Dvitruvocl.bench.corrSession=true} (optionally {@code -Dvitruvocl.bench.corrSizes=500,1000}).
 */
class CorrespondenceSessionBenchmark {

  private static final int TRANSACTIONS = 5;

  private static final List<String> CONSTRAINTS =
      List.of(
          "context edge::SubA inv: edge::Other.allInstances().select(~).size() <= 1",
          "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)",
          "context edge::SubA inv: edge::Other.allInstances().reject(~).size() >= 0",
          "context edge::Other inv: edge::SubA.allInstances().exists(a | a ~ self) or self.amount > 0",
          "context edge::Item inv: self.value >= 0");

  @AfterEach
  void resetProperty() {
    System.clearProperty(IncrementalSession.PROP_NO_SHARED_CORRESPONDENCE_LOOKUP);
  }

  @Test
  void measureSharedLookup() {
    assumeTrue(Boolean.getBoolean("vitruvocl.bench.corrSession"), "benchmark disabled");
    String sizes = System.getProperty("vitruvocl.bench.corrSizes", "500,1000");
    System.out.println(
        "| Level | n | Korrespondenzen | gemeinsamer Lookup | initialize [ms] | Modell-Tx [ms] | Korrespondenz-Tx [ms] |");
    System.out.println("|---|---:|---:|---|---:|---:|---:|");
    for (String size : sizes.split(",")) {
      int n = Integer.parseInt(size.trim());
      for (IncrementalLevel level : List.of(IncrementalLevel.READ_SET, IncrementalLevel.RELEVANT_INSTANCES)) {
        for (boolean shared : List.of(false, true)) {
          measure(n, level, shared);
        }
      }
    }
  }

  private void measure(int n, IncrementalLevel level, boolean shared) {
    if (shared) {
      System.clearProperty(IncrementalSession.PROP_NO_SHARED_CORRESPONDENCE_LOOKUP);
    } else {
      System.setProperty(IncrementalSession.PROP_NO_SHARED_CORRESPONDENCE_LOOKUP, "true");
    }
    Model model = new Model(n);
    try (IncrementalSession session =
        new IncrementalSession(CONSTRAINTS, model.wrapper, IncrementalConfig.of(level))) {
      long t0 = System.nanoTime();
      session.initialize();
      double init = (System.nanoTime() - t0) / 1e6;

      double modelTx = 0;
      double corrTx = 0;
      for (int i = 0; i < TRANSACTIONS; i++) {
        EObject other = model.others.get(n - 1 - i);
        int amount = 2 + i;
        List<EChange<EObject>> changes =
            model.record(() -> other.eSet(EdgeFixture.feature(other, "amount"), amount));
        long t1 = System.nanoTime();
        session.onTransaction(changes);
        modelTx += (System.nanoTime() - t1) / 1e6;

        model.view.addCorrespondenceBetween(model.items.get(n - 1 - i), other, null);
        long t2 = System.nanoTime();
        session.onTransaction(List.of());
        corrTx += (System.nanoTime() - t2) / 1e6;
      }
      System.out.printf(
          "| %d | %d | %d | %s | %.0f | %.0f | %.0f |%n",
          level.number(), n, n / 2, shared ? "ja" : "nein", init, modelTx / TRANSACTIONS, corrTx / TRANSACTIONS);
    }
  }

  /** The benchmark model over a real Vitruvius correspondence model. */
  private static final class Model {
    final ResourceSet resourceSet = new ResourceSetImpl();
    final Resource resource;
    final EditableCorrespondenceModelView<Correspondence> view;
    final VsumWrapper wrapper;
    final List<EObject> items = new ArrayList<>();
    final List<EObject> others = new ArrayList<>();

    Model(int n) {
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
      resource = resourceSet.createResource(URI.createURI("memory:/session.edge"));
      EObject root = EdgeFixture.create("Root");
      resource.getContents().add(root);
      view =
          CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
              CorrespondenceModelFactory.createPersistableCorrespondenceModel(
                  URI.createURI("memory:/session.correspondence")));
      for (int i = 0; i < n; i++) {
        EObject a = EdgeFixture.item("SubA", "a" + i, i);
        EObject o = EdgeFixture.create("Other");
        o.eSet(EdgeFixture.feature(o, "amount"), 1);
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
