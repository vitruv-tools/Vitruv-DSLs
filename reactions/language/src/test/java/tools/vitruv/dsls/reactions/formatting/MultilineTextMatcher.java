package tools.vitruv.dsls.reactions.formatting;

import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Pair;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class MultilineTextMatcher extends TypeSafeMatcher<String> {
  private final String expectedText;

  private Consumer<Description> mismatch;

  public MultilineTextMatcher(String expectedText) {
    this.expectedText = expectedText;
  }

  @Override
  protected boolean matchesSafely(String item) {
    Pair<String, String> firstMismatchingLines = firstMismatchingLines(item, this.expectedText);
    if (firstMismatchingLines != null) {
      mismatch = description -> {
        description.appendText("text has wrong content")
            .appendText(".\nExpected was:\n\n")
            .appendText(this.expectedText)
            .appendText("\n\n But got:\n\n")
            .appendText(item)
            .appendText("\n\nFirst mismatching line:\n\n")
            .appendText("\tActual: ")
            .appendValue(firstMismatchingLines.getKey())
            .appendText("\n")
            .appendText("\tExpected: ")
            .appendValue(firstMismatchingLines.getValue());
      };
      return false;
    }
    return true;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("the following text: \n\n").appendText(this.expectedText);
  }

  @Override
  protected void describeMismatchSafely(String item, Description mismatchDescription) {
    if (mismatch != null) {
      mismatch.accept(mismatchDescription);
    }
  }

  private static Pair<String, String> firstMismatchingLines(String firstText, String secondText) {
    String[] firstTextLines = firstText.split(System.lineSeparator());
    String[] secondTextLines = secondText.split(System.lineSeparator());
    int firstTextLinesCount = firstTextLines.length;
    int secondTextLinesCount = secondTextLines.length;
    
    int numberOfCommonLines = Math.min(firstTextLinesCount, secondTextLinesCount);
    for (int lineNumber = 0; lineNumber < numberOfCommonLines; lineNumber++) {
      String comparedFirstTextLine = firstTextLines[lineNumber];
      String comparedSecondTextLine = secondTextLines[lineNumber];
      if (!comparedFirstTextLine.equals(comparedSecondTextLine)) {
        return Pair.of(comparedFirstTextLine, comparedSecondTextLine);
      }
    }

    if (firstTextLinesCount > secondTextLinesCount) {
      return Pair.of(firstTextLines[numberOfCommonLines], "");
    }
    if (secondTextLinesCount > firstTextLinesCount) {
      return Pair.of("", secondTextLines[numberOfCommonLines]);
    }
    return null;
  }

  public static MultilineTextMatcher hasEachLineEqualTo(String expected) {
    return new MultilineTextMatcher(expected);
  }
}
