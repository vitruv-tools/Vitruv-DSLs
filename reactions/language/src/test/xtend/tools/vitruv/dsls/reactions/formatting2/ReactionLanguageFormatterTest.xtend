package tools.vitruv.dsls.reactions.formatting2

import org.junit.jupiter.api.Test
import java.io.File
import com.google.inject.Inject
import org.eclipse.xtext.testing.util.ParseHelper
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile
import java.util.Scanner
import org.eclipse.xtext.serializer.ISerializer
import org.eclipse.xtext.resource.SaveOptions
import org.eclipse.xtext.testing.extensions.InjectionExtension
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.junit.jupiter.api.^extension.ExtendWith
import static tools.vitruv.dsls.reactions.formatting2.MultilineTextMatcher.hasEachLineEqualTo
import static org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.eclipse.core.runtime.Platform
import org.junit.jupiter.api.BeforeEach
import static java.lang.System.lineSeparator
import org.junit.jupiter.api.DisplayName

@ExtendWith(InjectionExtension)
@InjectWith(ReactionsLanguageInjectorProvider)
class ReactionLanguageFormatterTest {
	@Inject var extension ParseHelper<ReactionsFile>
	@Inject var extension ISerializer

	@DisplayName("reformat existing test classes")
	@ParameterizedTest(name = "{index} file: {1}")
	@ValueSource(strings=#[
		"tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/CommonRoutines.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/Direct2SN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/DirectRoutinesQN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/DirectSN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/Root.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/Transitive2SN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/Transitive3SN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/TransitiveRoutinesQN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/TransitiveRoutinesSN.reactions",
		"tools/vitruv/dsls/reactions/tests/importTests/TransitiveSN.reactions"
	])
	def void testExistingReactionsFilesFormatting(String filePathInSourceFolder) {
		val content = filePathInSourceFolder.loadJavaFileContents()
		assertThat(content.destroyFormatting().format(), hasEachLineEqualTo(content))
	}
	
	private def String loadJavaFileContents(String filePathInSourceFolder) {
		val classLoader = this.getClass().getClassLoader()
		val file = if (Platform.isRunning) {
				new File("src/" + filePathInSourceFolder)
			} else {
				new File(classLoader.getResource(filePathInSourceFolder).getFile())
			}
		return new Scanner(file).useDelimiter("\\Z").next()
	}

	@DisplayName("reformat file with all kinds of Reactions statements")
	@Test
	def void testAllStatementTypes() {
		val expected = '''
		import static tools.vitruv.dsls.^reactions.tests.simpleChangesTests.SimpleChangesTestsExecutionMonitor.ChangeType.*;
		import tools.vitruv.change.atomic.eobject.EObjectExistenceEChange
		import static extension tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory.allElementTypes
		
		import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as minimal
		import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as minimal2
		
		reactions: simpleChangesTests
		in reaction to changes in minimal
		execute actions in minimal
		
		// A test reaction
		reaction InsertedNonRoot {
			after element minimal::NonRoot inserted in minimal::NonRootObjectContainerHelper[nonRootObjectsContainment]
			call insertNonRoot(newValue)
		}
		
		/*
		 * This is a routine.
		 * The only one.
		 */
		routine insertNonRoot(minimal::NonRoot nonRootElement) {
			match {
				require absence of minimal::NonRoot corresponding to nonRootElement
				val correspondingContainer = retrieve EObject corresponding to nonRootElement.eContainer tagged "test" with true
				val optionalCorresponding = retrieve optional minimal::NonRoot corresponding to {
					nonRootElement
				} tagged null
				val correspondingAsIterable = retrieve many minimal::NonRoot corresponding to {
					return nonRootElement;
				} with {
					correspondingAsIterable !== null
				}
			}
			create {
				val newNonRoot = new minimal::NonRoot
			}
			update {
				if (optionalCorresponding.empty) {
					// This can never happen due to matcher checks
				}
				newNonRoot.id = nonRootElement.id
				{
					{
						newNonRoot.value = "new"
					}
				}
				insertNonRoot(newNonRoot)
			}
		}'''
		assertThat(expected.destroyFormatting().format(), hasEachLineEqualTo(expected))
	}

	private def String format(String reactionsCodeToFormat) {
		return reactionsCodeToFormat.parse.serialize(SaveOptions::newBuilder.format().getOptions())
	}

	private def String destroyFormatting(String textToUnformat) {
		return textToUnformat.preserveEmptyLines [
			applyToEachLineRemovingLinebreaks [
				if (it.isCommentLine) {
					it.preserveCommentLine()
				} else {
					it.mixUpSpacingTerminals()
				}
			]
		]
	}

	private def String preserveEmptyLines(String text, (String)=>String applicator) {
		text.replace(lineSeparator + lineSeparator, lineSeparator + lineSeparator, applicator)
	}

	private def String applyToEachLineRemovingLinebreaks(String text, (String)=>String applicator) {
		text.split(lineSeparator).map[applicator.apply(it)].join()
	}

	private def boolean isCommentLine(String line) {
		return line.isFirstCommentLine || line.isInnerCommentLine
	}

	private def boolean isFirstCommentLine(extension String line) {
		return contains("//") || contains("/*")
	}

	private def boolean isInnerCommentLine(extension String line) {
		return contains(" *")
	}

	private def String preserveCommentLine(extension String line) {
		if (line.isFirstCommentLine) {
			return lineSeparator + line + lineSeparator
		} else if (line.isInnerCommentLine) {
			return line + lineSeparator
		} else {
			throw new IllegalArgumentException("line is no comment: " + line)
		}
	}

	private def String mixUpSpacingTerminals(String text) {
		val result = text.replace(" ", lineSeparator + lineSeparator) [
			replace("::", "  ::  ") [
				replace('\t', '  ')
			]
		]
		if (result != "}") {
			return result + " "
		} else {
			return result
		}
	}

	private def String replace(String text, String original, String replacement,
		(String)=>String applyOnIntermediatePlaceholderString) {
		return text.modifyTextUsingPlaceholderChar [inputText, placeholder|
			val placeholderString = "" + placeholder
			val textWithPlaceholder = inputText.replace(original, placeholderString)
			val modifiedTextWithPlaceholder = applyOnIntermediatePlaceholderString.apply(textWithPlaceholder)
			val textWithReplacement = modifiedTextWithPlaceholder.replace(placeholderString, replacement)
			return textWithReplacement	
		]
	}

	var char nextPlaceholderChar

	@BeforeEach
	def void restorePlaceholderChars() {
		nextPlaceholderChar = 14 as char
	}

	/*
	 * We temporarily replace formatting sequences (such as whitespaces, tabs, newlines etc.)
	 * with placeholders to restore different formatting sequences later on.
	 * To this end, we use usually unused ASCII symbols as placeholders, specifically
	 * the ASCII symbols 14 to 31.
	 */
	private def String modifyTextUsingPlaceholderChar(String text, (String, char)=>String modifier) {
		val result = modifier.apply(text, retrievePlaceholderChar())
		returnPlaceholderChar()
		return result
	}

	private def char retrievePlaceholderChar() {
		if (nextPlaceholderChar > 31) {
			throw new IllegalStateException("Too many placeholder chars are used")
		}
		return nextPlaceholderChar++
	}

	private def void returnPlaceholderChar() {
		nextPlaceholderChar--
	}
}
