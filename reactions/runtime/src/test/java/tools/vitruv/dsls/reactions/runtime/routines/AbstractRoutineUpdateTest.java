package tools.vitruv.dsls.reactions.runtime.routines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import allElementTypes.AllElementTypesFactory;
import allElementTypes.AllElementTypesPackage;
import allElementTypes.NonRoot;
import allElementTypes.Root;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.description.AnnotationSource;
import tools.vitruv.change.interaction.UserInteractionFactory;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.interaction.impl.PredefinedInteractionResultProviderImpl;
import tools.vitruv.change.propagation.impl.DefaultChangeRecordingModelRepository;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

class AbstractRoutineUpdateTest {
  private record Tag(String value) {}

  @Test
  void updateExposesExecutionStateAnnotationsAndUserInteractor() {
    var tag = new Tag("hello");
    AnnotationSource annotations =
        AnnotationSource.of(type -> type == Tag.class ? Optional.of(tag) : Optional.empty());
    UserInteractor userInteractor = UserInteractionFactory.instance
        .createUserInteractor(new PredefinedInteractionResultProviderImpl(null));
    var executionState = new ReactionExecutionState(userInteractor, null, null, null, annotations);

    var update = new AbstractRoutine.Update(executionState);

    assertSame(annotations, update.changeAnnotations());
    assertSame(tag, update.changeAnnotations().getAnnotation(Tag.class).orElseThrow());
    assertSame(userInteractor, update.userInteractor());
  }

  /**
   * {@link AbstractRoutine.Update} that records every change that is passed to
   * {@code issueEChange} before it is applied, so that tests can check that changes were issued.
   */
  private static final class RecordingUpdate extends AbstractRoutine.Update {
    private final List<EChange<EObject>> issuedChanges = new ArrayList<>();

    RecordingUpdate(ReactionExecutionState executionState) {
      super(executionState);
    }

    @Override
    protected void issueEChange(EChange<EObject> change) {
      issuedChanges.add(change);
      super.issueEChange(change);
    }

    /**
     * Checks that exactly {@code expectedCount} changes were issued since the last call of this
     * method, and forgets them.
     *
     * @param expectedCount number of expected changes
     */
    void assertIssuedChanges(int expectedCount) {
      assertEquals(expectedCount, issuedChanges.size());
      issuedChanges.clear();
    }
  }

  /**
   * Exercises {@code issueEChange} through every method of {@link AbstractRoutine.Update} that
   * calls it. Each step checks that the changes were issued and, where they have an effect,
   * the resulting state of the model.
   *
   * @param tempDir {@link Path} of a temporary directory
   */
  @Test
  void issueEChangeIsCalledByAllChangeIssuingMethods(@TempDir Path tempDir) {
    // Set up a model repository to apply changes on
    var modelRepository = new DefaultChangeRecordingModelRepository(null, tempDir);
    var executionState = new ReactionExecutionState(
        null,
        null,
        modelRepository,
        null,
        AnnotationSource.of(type -> Optional.empty()));
    RecordingUpdate update = new RecordingUpdate(executionState);

    // create, delete
    NonRoot nonRootToDelete = AllElementTypesFactory.eINSTANCE.createNonRoot();
    update.create(nonRootToDelete);
    update.assertIssuedChanges(1);
    update.delete(nonRootToDelete);
    update.assertIssuedChanges(1);

    Root root = AllElementTypesFactory.eINSTANCE.createRoot();
    // setAttribute
    root.setSingleValuedEAttribute(1);
    update.setAttribute(AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_EATTRIBUTE, root, 2);
    update.assertIssuedChanges(1);
    assertEquals(2, root.getSingleValuedEAttribute());

    // insertAttributeValue, removeAttributeValue
    var multiAttribute = AllElementTypesPackage.Literals.ROOT__MULTI_VALUED_EATTRIBUTE;
    update.insertAttributeValue(multiAttribute, root, 1);
    update.insertAttributeValue(multiAttribute, root, 2);
    update.insertAttributeValue(multiAttribute, root, 3);
    update.assertIssuedChanges(3);
    assertEquals(List.of(1, 2, 3), root.getMultiValuedEAttribute());
    update.removeAttributeValue(multiAttribute, root, 2);
    update.assertIssuedChanges(1);
    assertEquals(List.of(1, 3), root.getMultiValuedEAttribute());

    NonRoot first = AllElementTypesFactory.eINSTANCE.createNonRoot();
    NonRoot second = AllElementTypesFactory.eINSTANCE.createNonRoot();
    // setReference
    var singleReference =
        AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_NON_CONTAINMENT_EREFERENCE;
    update.setReference(singleReference, root, first);
    update.setReference(singleReference, root, second);
    update.assertIssuedChanges(2);
    assertSame(second, root.getSingleValuedNonContainmentEReference());

    // addReference, removeReference
    var multiReference =
        AllElementTypesPackage.Literals.ROOT__MULTI_VALUED_NON_CONTAINMENT_EREFERENCE;
    update.addReference(multiReference, root, first);
    update.addReference(multiReference, root, second);
    update.assertIssuedChanges(2);
    assertEquals(List.of(first, second), root.getMultiValuedNonContainmentEReference());
    update.removeReference(multiReference, root, first);
    update.assertIssuedChanges(1);
    assertEquals(List.of(second), root.getMultiValuedNonContainmentEReference());

    // insertRoot, removeRoot
    URI rootUri = URI.createFileURI(tempDir.resolve("root.xmi").toString());
    update.insertRoot(root, rootUri);
    update.assertIssuedChanges(1);
    Resource resource = root.eResource();
    assertEquals(rootUri, resource.getURI());
    assertEquals(List.of(root), resource.getContents());
    update.removeRoot(root);
    update.assertIssuedChanges(1);
    assertTrue(resource.getContents().isEmpty());
  }
}
