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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * If processing a transaction fails, its delta is lost; the next transaction must therefore
 * re-evaluate every constraint, otherwise constraints touched only by the lost transaction would
 * keep stale outcomes.
 */
class SessionRecoveryTest {

  /** Delegating wrapper whose instance access can be made to fail. */
  private static final class FailingWrapper implements MetamodelWrapperInterface {
    private final MetamodelWrapperInterface delegate;
    private boolean fail;

    FailingWrapper(MetamodelWrapperInterface delegate) {
      this.delegate = delegate;
    }

    @Override
    public EClass resolveEClass(String metamodelName, String className) {
      return delegate.resolveEClass(metamodelName, className);
    }

    @Override
    public List<EObject> getAllInstances(EClass eClass) {
      if (fail) {
        throw new IllegalStateException("simulated failure");
      }
      return delegate.getAllInstances(eClass);
    }

    @Override
    public EEnum resolveEEnum(String enumName) {
      return delegate.resolveEEnum(enumName);
    }

    @Override
    public Set<String> getAvailableMetamodels() {
      return delegate.getAvailableMetamodels();
    }

    @Override
    public String getInstanceNameByIndex(int index) {
      return delegate.getInstanceNameByIndex(index);
    }

    @Override
    public List<EObject> getAllRootObjects() {
      return delegate.getAllRootObjects();
    }

    @Override
    public EObject getContextObjectByIndex(int index) {
      return delegate.getContextObjectByIndex(index);
    }

    @Override
    public EClass resolveEClassByShortName(String shortName) {
      return delegate.resolveEClassByShortName(shortName);
    }

    @Override
    public String getSourceFileForInstance(EObject instance) {
      return delegate.getSourceFileForInstance(instance);
    }

    @Override
    public Set<EObject> getCorrespondingObjects(EObject source) {
      return delegate.getCorrespondingObjects(source);
    }

    @Override
    public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
      return delegate.correspondenceHasTag(obj1, obj2, tag);
    }
  }

  @Test
  void transactionAfterAFailureReEvaluatesEverything() {
    EdgeFixture fx = EdgeFixture.create();
    FailingWrapper wrapper = new FailingWrapper(fx.model().wrapper());
    List<String> constraints =
        List.of(
            "context edge::Item inv: self.value >= 0",
            "context edge::Other inv: self.label != \"z\"");
    try (IncrementalSession session =
        new IncrementalSession(
            constraints, wrapper, IncrementalConfig.of(IncrementalLevel.READ_SET).withVerify(true))) {
      session.initialize();
      EObject a1 = fx.find("a1");
      wrapper.fail = true;
      List<EChange<EObject>> lost = fx.model().record(() -> a1.eSet(EdgeFixture.feature(a1, "value"), -1));
      assertThrows(IllegalStateException.class, () -> session.onTransaction(lost));
      wrapper.fail = false;

      // touches only Other.label, but the Item constraint missed the lost transaction
      EObject x = fx.find("x");
      IncrementalStats stats =
          session.onTransaction(fx.model().record(() -> x.eSet(EdgeFixture.feature(x, "label"), "q"))).stats();
      assertEquals(2, stats.constraintsEvaluated(), stats.toString());
      assertEquals(2, stats.forcedEvaluations(), stats.toString());

      // afterwards, filtering resumes
      IncrementalStats next =
          session.onTransaction(fx.model().record(() -> x.eSet(EdgeFixture.feature(x, "label"), "r"))).stats();
      assertEquals(1, next.constraintsSkipped(), next.toString());
    }
  }
}
