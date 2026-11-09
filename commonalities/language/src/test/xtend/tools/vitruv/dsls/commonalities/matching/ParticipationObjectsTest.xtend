package tools.vitruv.dsls.commonalities.tests.matching

import org.eclipse.emf.ecore.impl.EObjectImpl
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

@DisplayName('participation objects')
class ParticipationObjectsTest {
	static class ToStringCountingEObject extends EObjectImpl {
		var toStringCalls = 0

		override toString() {
			toStringCalls++
			return "countingObject"
		}
	}

	@Test
	@DisplayName('does not render objects when adding, copying and merging them')
	def void doesNotRenderObjectsOnSuccess() {
		val first = new ToStringCountingEObject
		val second = new ToStringCountingEObject
		val objects = new ParticipationObjects
		objects.addObject('first', first)
		val other = new ParticipationObjects
		other.addObject('second', second)
		objects.copy.merge(other)

		assertThat(first.toStringCalls, is(0))
		assertThat(second.toStringCalls, is(0))
	}

	@Test
	@DisplayName('names the object that is already present')
	def void namesDuplicateObject() {
		val object = new ToStringCountingEObject
		val objects = new ParticipationObjects
		objects.addObject('first', object)

		val error = assertThrows(IllegalArgumentException) [objects.addObject('second', object)]
		assertThat(error.message, containsString('countingObject'))
	}

	@Test
	@DisplayName('names the name that is already present')
	def void namesDuplicateName() {
		val objects = new ParticipationObjects
		objects.addObject('first', new ToStringCountingEObject)

		val error = assertThrows(IllegalArgumentException) [objects.addObject('first', new ToStringCountingEObject)]
		assertThat(error.message, containsString('first'))
	}
}
