package tools.vitruv.dsls.reactions.runtime.locking;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

/** Assertions shared by the tests of the access analysis. */
final class EmfModelIndexTestSupport {
  private EmfModelIndexTestSupport() {
  }

  static <T> void assertContains(Collection<? extends T> collection, T element) {
    assertTrue(collection.contains(element), () -> collection + " does not contain " + element);
  }
}
