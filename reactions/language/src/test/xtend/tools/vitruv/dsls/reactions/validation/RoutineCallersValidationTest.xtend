package tools.vitruv.dsls.reactions.validation

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider

import static org.hamcrest.CoreMatchers.hasItem
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.MatcherAssert.assertThat

@ExtendWith(InjectionExtension)
@InjectWith(ReactionsLanguageInjectorProvider)
class RoutineCallersValidationTest {
	@Inject ParseHelper<ReactionsFile> parseHelper
	@Inject ValidationTestHelper validationHelper

	@Test
	def void warnsOnlyAboutRoutinesThatAreNeverCalled() {
		val reactionsFile = parseHelper.parse('''
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as minimal

			reactions: routineCallersTest
			in reaction to changes in minimal
			execute actions in minimal

			reaction RootCreated {
				after element minimal::Root created
				call calledRoutine()
			}

			routine calledRoutine() {
				update {
					calledFromRoutine()
				}
			}

			routine calledFromRoutine() {
				update {
				}
			}

			routine uncalledRoutine() {
				update {
				}
			}
		''')

		val warnings = validationHelper.validate(reactionsFile)
			.filter[message.endsWith("is never called.")].map[message].toList
		assertThat(warnings, hasItem("Routine 'uncalledRoutine' is never called."))
		assertThat(warnings, not(hasItem("Routine 'calledRoutine' is never called.")))
		assertThat(warnings, not(hasItem("Routine 'calledFromRoutine' is never called.")))
		validationHelper.assertWarning(reactionsFile, TopLevelElementsPackage.Literals.ROUTINE, null,
			"Routine 'uncalledRoutine' is never called.")
	}
}
