package tools.vitruv.dsls.reactions.tests.annotationTests

import allElementTypes.Root
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Delegate
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.vitruv.change.atomic.uuid.Uuid
import tools.vitruv.change.composite.description.VitruviusChange
import tools.vitruv.change.composite.propagation.ChangeableModelRepository
import tools.vitruv.change.propagation.ChangePropagationSpecification
import tools.vitruv.change.propagation.ChangePropagationSpecificationRepository
import tools.vitruv.change.propagation.impl.DefaultChangeRecordingModelRepository
import tools.vitruv.change.testutils.TestModelRepositoryFactory
import tools.vitruv.change.testutils.TestUserInteraction
import tools.vitruv.change.testutils.views.ChangePublishingTestView
import tools.vitruv.change.testutils.views.TestView
import tools.vitruv.change.testutils.views.UriMode
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static tools.vitruv.change.testutils.metamodels.AllElementTypesCreators.aet

import static extension tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory.allElementTypes

/**
 * Tests that annotations of a propagated change are available in reactions and routines.
 */
class AnnotationTests extends ReactionsExecutionTest {
	static val SOURCE_MODEL = 'AnnotationTestsSource'.allElementTypes

	var AnnotatingModelRepository annotatingModelRepository

	override protected createCompiler(TestReactionsCompiler.Factory factory) {
		factory.createCompiler [
			reactions = #["AnnotationTests.reactions"]
			changePropagationSegments = #["annotationTests"]
		]
	}

	override protected createTestView(Path testProjectPath,
		Iterable<ChangePropagationSpecification> changePropagationSpecifications) {
		val userInteraction = new TestUserInteraction
		val modelRepository = new DefaultChangeRecordingModelRepository(null, Files.createTempDirectory(null))
		annotatingModelRepository = new AnnotatingModelRepository(
			TestModelRepositoryFactory.createTestChangeableModelRepository(modelRepository,
				new ChangePropagationSpecificationRepository(changePropagationSpecifications), userInteraction))
		new ChangePublishingTestView(testProjectPath, userInteraction, UriMode.FILE_URIS, annotatingModelRepository,
			modelRepository.uuidResolver, [URI uri|modelRepository.getModelResource(uri)])
	}

	@BeforeEach
	def void createRoot() {
		resourceAt(SOURCE_MODEL).propagate [
			contents += aet.Root => [id = 'AnnotationTestsSource']
		]
		monitor.reset()
	}

	private static def getMonitor() {
		AnnotationTestsMonitor.instance
	}

	@Test
	def void annotationIsAvailableInReactionAndRoutine() {
		val author = new Author("alice")
		annotatingModelRepository.nextAuthor = author

		Root.from(SOURCE_MODEL).propagate [
			id = 'changedByAlice'
		]

		assertThat(monitor.authorInReaction, is(Optional.of(author)))
		assertThat(monitor.authorInRoutine, is(Optional.of(author)))
	}

	@Test
	def void missingAnnotationIsEmptyInReactionAndRoutine() {
		annotatingModelRepository.nextAuthor = null

		Root.from(SOURCE_MODEL).propagate [
			id = 'changedWithoutAuthor'
		]

		assertThat(monitor.authorInReaction, is(Optional.empty))
		assertThat(monitor.authorInRoutine, is(Optional.empty))
	}

	@Test
	def void annotationsAreTakenFromTheCurrentChange() {
		annotatingModelRepository.nextAuthor = new Author("alice")
		Root.from(SOURCE_MODEL).propagate [
			id = 'changedByAlice'
		]
		val bob = new Author("bob")
		annotatingModelRepository.nextAuthor = bob

		Root.from(SOURCE_MODEL).propagate [
			id = 'changedByBob'
		]

		assertThat(monitor.authorInReaction, is(Optional.of(bob)))
		assertThat(monitor.authorInRoutine, is(Optional.of(bob)))
	}

	@Test
	def void userInteractorIsAvailableInRoutine() {
		Root.from(SOURCE_MODEL).propagate [
			id = 'changed'
		]

		assertThat(monitor.userInteractorInRoutine, is(notNullValue))
	}

	/**
	 * Annotates every change with the configured {@link Author} before propagating it.
	 */
	@FinalFieldsConstructor
	private static class AnnotatingModelRepository implements ChangeableModelRepository {
		@Delegate val ChangeableModelRepository delegate
		@Accessors(PUBLIC_SETTER) var Author nextAuthor

		override propagateChange(VitruviusChange<Uuid> change) {
			if (nextAuthor !== null) {
				change.setAnnotation(Author, nextAuthor)
			}
			delegate.propagateChange(change)
		}
	}
}
