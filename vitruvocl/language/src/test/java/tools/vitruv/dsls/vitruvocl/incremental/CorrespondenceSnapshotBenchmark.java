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
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Manual benchmark for step B1 of {@code docs/correspondence-incremental-plan.md}: the cost of
 * capturing and diffing a {@link CorrespondenceSnapshot} over the real Vitruvius correspondence
 * model ({@code PersistableCorrespondenceModelImpl}) through {@link VsumWrapper}, for growing
 * numbers of 1:1 correspondences.
 *
 * <p>For comparison it also measures one lookup per object, which is what a full evaluation of a
 * single {@code Y.allInstances()->select(~)} constraint costs after step A1. Disabled by default;
 * run with {@code -Dvitruvocl.bench.corrSnapshot=true} (optionally {@code
 * -Dvitruvocl.bench.corrSizes=500,1000,2000,4000}).
 */
class CorrespondenceSnapshotBenchmark {

  private static final int WARMUP = 2;
  private static final int RUNS = 5;

  @Test
  void measureCaptureAndDiff() {
    assumeTrue(Boolean.getBoolean("vitruvocl.bench.corrSnapshot"), "benchmark disabled");
    String sizes = System.getProperty("vitruvocl.bench.corrSizes", "500,1000,2000,4000");

    System.out.println(
        "| Korrespondenzen | Objekte | Capture [ms] | Diff [ms] | Capture+Diff [ms] | Lookups allein [ms] |");
    System.out.println("|---:|---:|---:|---:|---:|---:|");
    for (String size : sizes.split(",")) {
      measure(Integer.parseInt(size.trim()));
    }
  }

  private void measure(int pairs) {
    var resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    Resource resource = resourceSet.createResource(URI.createURI("memory:/bench.xmi"));
    EditableCorrespondenceModelView<Correspondence> view =
        CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
            CorrespondenceModelFactory.createPersistableCorrespondenceModel(
                URI.createURI("memory:/bench.correspondence")));
    List<EObject> objects = new ArrayList<>();
    for (int i = 0; i < pairs; i++) {
      EObject left = EcoreFactory.eINSTANCE.createEObject();
      EObject right = EcoreFactory.eINSTANCE.createEObject();
      resource.getContents().add(left);
      resource.getContents().add(right);
      objects.add(left);
      objects.add(right);
      view.addCorrespondenceBetween(left, right, i % 2 == 0 ? "even" : "odd");
    }
    InternalVirtualModel vsum = mock(InternalVirtualModel.class);
    when(vsum.getCorrespondenceModel()).thenReturn(view);
    VsumWrapper wrapper = new VsumWrapper(vsum);

    double capture = 0;
    double diff = 0;
    double lookups = 0;
    for (int run = 0; run < WARMUP + RUNS; run++) {
      long t0 = System.nanoTime();
      CorrespondenceSnapshot before = CorrespondenceSnapshot.capture(wrapper).orElseThrow();
      long t1 = System.nanoTime();
      // one silent change, as a consistency-preservation rule would make
      view.addCorrespondenceBetween(objects.get(0), objects.get(3), "late");
      CorrespondenceSnapshot after = CorrespondenceSnapshot.capture(wrapper).orElseThrow();
      long t2 = System.nanoTime();
      CorrespondenceDelta delta = before.diff(after);
      long t3 = System.nanoTime();
      view.removeCorrespondencesBetween(objects.get(0), objects.get(3), "late");
      long t4 = System.nanoTime();
      int found = 0;
      for (EObject o : objects) {
        found += wrapper.getCorrespondingObjects(o).size();
      }
      long t5 = System.nanoTime();

      assertEquals(1, delta.added().size(), "exactly the silent change");
      assertEquals(2 * pairs, found);
      if (run >= WARMUP) {
        capture += ((t1 - t0) + (t2 - t1)) / 2.0 / 1e6;
        diff += (t3 - t2) / 1e6;
        lookups += (t5 - t4) / 1e6;
      }
    }
    capture /= RUNS;
    diff /= RUNS;
    lookups /= RUNS;
    System.out.printf(
        "| %d | %d | %.1f | %.2f | %.1f | %.1f |%n",
        pairs, 2 * pairs, capture, diff, capture + diff, lookups);
  }
}
