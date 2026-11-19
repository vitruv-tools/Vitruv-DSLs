package tools.vitruv.dsls.reactions.tests.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import allElementTypes.AllElementTypesFactory;
import allElementTypes.AllElementTypesPackage;
import allElementTypes.Root;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.TypeInferringAtomicEChangeFactory;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.model.CorrespondenceModelFactory;
import tools.vitruv.change.correspondence.view.CorrespondenceModelViewFactory;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

/**
 * Runs one instance of a compiled reactions specification from several threads at once, each
 * thread on its own model element. A {@code ChangePropagationSpecification} is instantiated once
 * per virtual model, so parallel change propagations share it; its reactions must therefore not
 * keep the change they are executed for in instance state, and setting the user interactor (which
 * the change propagator does before each propagation step) must not disturb running reactions.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
class ConcurrentReactionExecutionTest {
  private static final int THREADS = 8;
  private static final int ITERATIONS = 20_000;
  private static final TypeInferringAtomicEChangeFactory CHANGES =
      TypeInferringAtomicEChangeFactory.getInstance();

  @Inject TestReactionsCompiler.Factory compilerFactory;
  @TempDir Path compilationDir;

  private ChangePropagationSpecification specification;
  private EditableCorrespondenceModelView<Correspondence> correspondenceModel;

  @BeforeEach
  void compileReactions() {
    var compiler = compilerFactory.createCompiler(parameters -> {
      parameters.setReactionsOwner(this);
      parameters.setCompilationProjectDir(compilationDir);
      parameters.setReactions(List.of(
          "/tools/vitruv/dsls/reactions/tests/concurrency/ConcurrentExecution.reactions"));
      parameters.setChangePropagationSegments(List.of("concurrentExecutionTests"));
    });
    List<ChangePropagationSpecification> specifications = new ArrayList<>();
    compiler.getChangePropagationSpecifications().forEach(specifications::add);
    assertEquals(1, specifications.size());
    specification = specifications.get(0);
    correspondenceModel = CorrespondenceModelViewFactory.createEditableCorrespondenceModelView(
        CorrespondenceModelFactory.createPersistableCorrespondenceModel(null));
  }

  @Test
  void reactionsWorkOnTheirOwnChange() throws Exception {
    assertEquals(0, runConcurrently(false), "reactions applied to the change of another thread");
  }

  @Test
  void settingTheUserInteractorDoesNotDisturbRunningReactions() throws Exception {
    assertEquals(0, runConcurrently(true), "reactions applied to the change of another thread");
  }

  /**
   * Lets every thread replace the attribute of its own root many times and propagate each change.
   * Returns the number of propagations after which the root of the thread did not have the id the
   * reaction derives from the change.
   */
  private int runConcurrently(boolean setUserInteractorBeforeEachChange) throws Exception {
    AtomicInteger wrongResults = new AtomicInteger();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    CyclicBarrier start = new CyclicBarrier(THREADS);
    List<Thread> threads = new ArrayList<>();
    for (int t = 0; t < THREADS; t++) {
      Thread thread = new Thread(() -> {
        Root root = AllElementTypesFactory.eINSTANCE.createRoot();
        try {
          start.await();
          for (int i = 1; i <= ITERATIONS; i++) {
            if (setUserInteractorBeforeEachChange) {
              specification.setUserInteractor(null);
            }
            specification.propagateChange(replaceAttribute(root, i - 1, i), correspondenceModel,
                null);
            if (!("value" + i).equals(root.getId())) {
              wrongResults.incrementAndGet();
            }
          }
        } catch (Throwable e) {
          failure.compareAndSet(null, e);
        }
      }, "propagation-" + t);
      threads.add(thread);
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }
    assertNull(failure.get(), () -> "a propagation failed: " + failure.get());
    return wrongResults.get();
  }

  @SuppressWarnings("unchecked")
  private static EChange<EObject> replaceAttribute(Root root, int oldValue, int newValue) {
    return (EChange<EObject>) (EChange<?>) CHANGES.createReplaceSingleAttributeChange(root,
        AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_EATTRIBUTE, oldValue, newValue);
  }
}
