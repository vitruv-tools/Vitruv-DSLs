package tools.vitruv.dsls.testutils;

import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * A specific {@link TestModel} that can be used with {@link
 * tools.vitruv.change.testutils.views.TestView}s, which require existing root elements to be
 * registered upon their creation via change propagation.
 *
 * @param <T> the root element type
 */
public interface TestViewBasedTestModel<T extends EObject> extends TestModel<T> {
  /**
   * Registers an existing root element with its resource URI. It can be retrieved from {@link
   * #getRootObjects} afterwards.
   *
   * @param rootResourceURI the URI of the root element resource to add
   */
  void registerExistingRoot(URI rootResourceURI);

  /**
   * Applies the given modification function to this test model. Updates the other given test model
   * with additions of root elements to that model.
   *
   * @param modelModificationFunction the function with modifications to apply
   * @param otherTestModelToUpdate a test model to update upon creation of new root objects of that
   *     model
   */
  void applyChanges(
      Consumer<TestModel<T>> modelModificationFunction,
      TestViewBasedTestModel<?> otherTestModelToUpdate);
}
