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

import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Manual benchmark for steps A3/A4 of {@code docs/correspondence-incremental-plan.md}: full
 * evaluation of {@code ~} constraints over the real Vitruvius correspondence model, with and
 * without the correspondence fast paths. The model has {@code n} {@code SubA} and {@code n} {@code
 * Other} objects; the first half of each corresponds 1:1, the second half takes part in no
 * correspondence. Disabled by default; run with {@code -Dvitruvocl.bench.corrFastPaths=true}
 * (optionally {@code -Dvitruvocl.bench.corrSizes=250,500,1000}).
 */
class CorrespondenceFastPathBenchmark {

  private static final int WARMUP = 1;
  private static final int RUNS = 3;
  private static final int MAX_NESTED_SIZE = 250;

  private static final List<String> CONSTRAINTS =
      List.of(
          "context edge::SubA inv: edge::Other.allInstances().select(~).size() <= 1",
          "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)",
          "context edge::SubA inv: edge::Other.allInstances()"
              + ".forAll(o | self ~ o implies edge::Other.allInstances().select(p | p.amount > o.amount).size() >= 0)");

  @AfterEach
  void resetProperty() {
    System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
  }

  @Test
  void measureFastPaths() {
    assumeTrue(Boolean.getBoolean("vitruvocl.bench.corrFastPaths"), "benchmark disabled");
    String sizes = System.getProperty("vitruvocl.bench.corrSizes", "250,500,1000");
    System.out.println("| Constraint | n | Korrespondenzen | ohne Fast-Paths [ms] | mit Fast-Paths [ms] | Faktor |");
    System.out.println("|---|---:|---:|---:|---:|---:|");
    for (String size : sizes.split(",")) {
      int n = Integer.parseInt(size.trim());
      VsumWrapper wrapper = model(n);
      for (int c = 0; c < CONSTRAINTS.size(); c++) {
        if (c == 2 && n > MAX_NESTED_SIZE) {
          continue; // C3 is cubic without the fast path
        }
        String constraint = CONSTRAINTS.get(c);
        double plain = time(constraint, wrapper, true);
        double fast = time(constraint, wrapper, false);
        System.out.printf(
            "| C%d | %d | %d | %.0f | %.0f | %.1f |%n", c + 1, n, n / 2, plain, fast, plain / fast);
      }
    }
  }

  private double time(String constraint, VsumWrapper wrapper, boolean plain) {
    if (plain) {
      System.setProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS, "true");
    } else {
      System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
    }
    double total = 0;
    for (int run = 0; run < WARMUP + RUNS; run++) {
      long t0 = System.nanoTime();
      EvaluationOutcome outcome =
          ConstraintEvaluations.evaluateFresh(constraint, wrapper, List.of(), false);
      long t1 = System.nanoTime();
      assertFalse(outcome.failed(), () -> "failed: " + outcome.errors());
      assertEquals(0, outcome.records().size(), constraint);
      if (run >= WARMUP) {
        total += (t1 - t0) / 1e6;
      }
    }
    return total / RUNS;
  }

  private static VsumWrapper model(int n) {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    Resource resource = rs.createResource(URI.createURI("memory:/bench.edge"));
    EObject root = EdgeFixture.create("Root");
    resource.getContents().add(root);
    EditableCorrespondenceModelView<Correspondence> view =
        CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
            CorrespondenceModelFactory.createPersistableCorrespondenceModel(
                URI.createURI("memory:/bench.correspondence")));
    for (int i = 0; i < n; i++) {
      EObject a = EdgeFixture.item("SubA", "a" + i, i);
      EObject o = EdgeFixture.create("Other");
      o.eSet(EdgeFixture.feature(o, "amount"), 1);
      EdgeFixture.list(root, "items").add(a);
      EdgeFixture.list(root, "others").add(o);
      if (i < n / 2) {
        view.addCorrespondenceBetween(a, o, null);
      }
    }
    InternalVirtualModel vsum =
        mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));
    when(((ViewSource) vsum).getViewSourceModels()).thenReturn(List.of(resource));
    when(vsum.getCorrespondenceModel()).thenReturn(view);
    return new VsumWrapper(vsum);
  }
}
