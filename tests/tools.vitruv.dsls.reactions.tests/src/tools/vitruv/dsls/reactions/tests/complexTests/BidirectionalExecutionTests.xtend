package tools.vitruv.dsls.reactions.tests.complexTests

import allElementTypes.Root
import org.eclipse.emf.ecore.EObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.vitruv.change.atomic.eobject.CreateEObject
import tools.vitruv.change.atomic.eobject.DeleteEObject
import tools.vitruv.change.atomic.feature.reference.RemoveEReference
import tools.vitruv.change.atomic.feature.reference.ReplaceSingleValuedEReference
import tools.vitruv.change.atomic.root.RemoveRootEObject
import tools.vitruv.change.composite.description.CompositeContainerChange
import tools.vitruv.change.composite.description.PropagatedChange
import tools.vitruv.change.composite.description.VitruviusChange
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static tools.vitruv.testutils.matchers.ModelMatchers.*
import static tools.vitruv.testutils.matchers.ModelMatchers.containsModelOf
import static tools.vitruv.testutils.metamodels.AllElementTypesCreators.aet

import static extension tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory.allElementTypes

class BidirectionalExecutionTests extends ReactionsExecutionTest {
	static val SOURCE_MODEL = 'BidirectionalSource'.allElementTypes
	static val TARGET_MODEL = 'BidirectionalTarget'.allElementTypes

	String[] nonContainmentNonRootIds = #["NonRootHelper0", "NonRootHelper1", "NonRootHelper2"]

	override protected createCompiler(TestReactionsCompiler.Factory factory) {
		factory.createCompiler [
			reactions = #["/tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions"]
			changePropagationSegments = #["simpleChangesTests"]
		]
	}

	@BeforeEach
	def setup() {
		resourceAt(SOURCE_MODEL).propagate [
			contents += aet.Root => [
				id = 'EachTestModelSource'
				nonRootObjectContainerHelper = aet.NonRootObjectContainerHelper => [
					id = 'NonRootObjectContainer'
					nonRootObjectsContainment += nonContainmentNonRootIds.map [ nonRootId |
						aet.NonRoot => [id = nonRootId]
					]
				]
			]
		]

		assertThat(resourceAt(TARGET_MODEL), containsModelOf(resourceAt(SOURCE_MODEL)))
	}

	private def VitruviusChange<EObject> getSourceModelChanges(PropagatedChange propagatedChange) {
		return (propagatedChange.consequentialChanges as CompositeContainerChange<EObject>).changes.findFirst [
			changedURIs.exists [lastSegment == SOURCE_MODEL.toString]
		]
	}

	@Test
	def void testBasicBidirectionalApplication() {
		val propagatedChanges = Root.from(TARGET_MODEL).propagate [
			singleValuedContainmentEReference = aet.NonRoot => [
				id = 'bidirectionalId'
			]
		]

		assertThat(propagatedChanges.size, is(2))
		val consequentialSourceModelChange = propagatedChanges.get(0).sourceModelChanges
		assertThat(consequentialSourceModelChange.EChanges.get(0), is(instanceOf(CreateEObject)))
		assertThat(consequentialSourceModelChange.EChanges.get(1), is(instanceOf(ReplaceSingleValuedEReference)))
		assertThat(resourceAt(SOURCE_MODEL), containsModelOf(resourceAt(TARGET_MODEL)))
		assertThat(Root.from(SOURCE_MODEL).singleValuedContainmentEReference.id, is('bidirectionalId'))
		assertThat(Root.from(TARGET_MODEL).singleValuedContainmentEReference.id, is('bidirectionalId'))
		assertThat(propagatedChanges.get(1).consequentialChanges.EChanges, is(emptyList))
	}

	@Test
	def void testApplyRemoveInTargetModel() {
		val propagatedChanges = Root.from(TARGET_MODEL).propagate [
			nonRootObjectContainerHelper.nonRootObjectsContainment.remove(0)
		]

		assertThat(propagatedChanges.size, is(2))
		val consequentialSourceModelChange = propagatedChanges.get(0).sourceModelChanges
		assertThat(consequentialSourceModelChange.EChanges.get(0), is(instanceOf(RemoveEReference)))
		assertThat(consequentialSourceModelChange.EChanges.get(1), is(instanceOf(DeleteEObject)))
		assertThat(resourceAt(SOURCE_MODEL), containsModelOf(resourceAt(TARGET_MODEL)))
		assertThat(Root.from(SOURCE_MODEL).nonRootObjectContainerHelper.nonRootObjectsContainment.size, is(2))
		assertThat(Root.from(TARGET_MODEL).nonRootObjectContainerHelper.nonRootObjectsContainment.size, is(2))
		assertThat(propagatedChanges.get(1).consequentialChanges.EChanges, is(emptyList))
	}

	@Test
	def void testApplyRemoveRootInTargetModel() {
		val propagatedChanges = resourceAt(TARGET_MODEL).propagate [delete(emptyMap)]

		assertThat(propagatedChanges.size, is(2))
		val consequentialSourceModelChange = propagatedChanges.get(0).sourceModelChanges
		assertThat(consequentialSourceModelChange.EChanges.get(0), is(instanceOf(RemoveRootEObject)))
		assertThat(consequentialSourceModelChange.EChanges.get(1), is(instanceOf(DeleteEObject)))
		assertThat(resourceAt(SOURCE_MODEL), doesNotExist())
		assertThat(resourceAt(TARGET_MODEL), doesNotExist())
		assertThat(propagatedChanges.get(1).consequentialChanges.EChanges, is(emptyList))
	}
}
