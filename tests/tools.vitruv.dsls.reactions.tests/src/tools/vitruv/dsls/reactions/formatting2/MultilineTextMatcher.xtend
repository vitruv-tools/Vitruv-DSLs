package tools.vitruv.dsls.reactions.formatting2

import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description
import java.util.function.Consumer

class MultilineTextMatcher extends TypeSafeMatcher<String> {
	val String expectedText
	var Consumer<Description> mismatch

	new(String expectedText) {
		this.expectedText = expectedText
	}
	
	override protected matchesSafely(String item) {
		val firstMismatchingLines = firstMismatchingLines(item, expectedText)
		if (firstMismatchingLines !== null) {
			mismatch = [
				appendText('text has wrong content').
					appendText('.\nExpected was:\n\n').appendText(expectedText)
					.appendText('\n\n But got:\n\n').appendText(item)
					.appendText('\n\nFirst mismatching line:\n\n')
					.appendText("\tActual: ").appendValue(firstMismatchingLines.key).appendText("\n")
					.appendText("\tExpected: ").appendValue(firstMismatchingLines.value)
			]
			return false
		}
		return true
	}
		
	override describeTo(Description description) {
		description.appendText("the following text: \n\n").appendText(expectedText)
	}

	override protected describeMismatchSafely(String item, Description mismatchDescription) {
		mismatch?.accept(mismatchDescription)
	}
	
	def private static firstMismatchingLines(String firstText, String secondText) {
		val firstTextLines = firstText.split(System.lineSeparator)
		val secondTextLines = secondText.split(System.lineSeparator)
		val numberOfCommonLines = Math.min(firstTextLines.size, secondTextLines.size)
		for (var lineNumber = 0; lineNumber < numberOfCommonLines; lineNumber++) {
			val comparedFirstTextLine = firstTextLines.get(lineNumber)
			val comparedSecondTextLine = secondTextLines.get(lineNumber)
			if (comparedFirstTextLine != comparedSecondTextLine) {
				return comparedFirstTextLine -> comparedSecondTextLine
			}
		}
		return if (firstTextLines.size > secondTextLines.size) {
			firstTextLines.get(numberOfCommonLines) -> ""
		} else if (secondTextLines.size > firstTextLines.size) {
			"" -> secondTextLines.get(numberOfCommonLines)
		} else {
			null
		}
	}
	
	static def hasEachLineEqualTo(String expected) {
		return new MultilineTextMatcher(expected)
	}
}