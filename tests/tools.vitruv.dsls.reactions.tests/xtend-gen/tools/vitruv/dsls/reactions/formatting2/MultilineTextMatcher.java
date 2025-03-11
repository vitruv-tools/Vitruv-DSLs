package tools.vitruv.dsls.reactions.formatting2;

import com.google.common.base.Objects;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@SuppressWarnings("all")
public class MultilineTextMatcher extends TypeSafeMatcher<String> {
  private final String expectedText;

  private Consumer<Description> mismatch;

  public MultilineTextMatcher(final String expectedText) {
    this.expectedText = expectedText;
  }

  @Override
  protected boolean matchesSafely(final String item) {
    final Pair<String, String> firstMismatchingLines = MultilineTextMatcher.firstMismatchingLines(item, this.expectedText);
    if ((firstMismatchingLines != null)) {
      final Consumer<Description> _function = (Description it) -> {
        it.appendText("text has wrong content").appendText(".\nExpected was:\n\n").appendText(this.expectedText).appendText("\n\n But got:\n\n").appendText(item).appendText("\n\nFirst mismatching line:\n\n").appendText("\tActual: ").appendValue(firstMismatchingLines.getKey()).appendText("\n").appendText("\tExpected: ").appendValue(firstMismatchingLines.getValue());
      };
      this.mismatch = _function;
      return false;
    }
    return true;
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText("the following text: \n\n").appendText(this.expectedText);
  }

  @Override
  protected void describeMismatchSafely(final String item, final Description mismatchDescription) {
    if (this.mismatch!=null) {
      this.mismatch.accept(mismatchDescription);
    }
  }

  private static Pair<String, String> firstMismatchingLines(final String firstText, final String secondText) {
    final String[] firstTextLines = firstText.split(System.lineSeparator());
    final String[] secondTextLines = secondText.split(System.lineSeparator());
    final int numberOfCommonLines = Math.min(((List<String>)Conversions.doWrapArray(firstTextLines)).size(), ((List<String>)Conversions.doWrapArray(secondTextLines)).size());
    for (int lineNumber = 0; (lineNumber < numberOfCommonLines); lineNumber++) {
      {
        final String comparedFirstTextLine = firstTextLines[lineNumber];
        final String comparedSecondTextLine = secondTextLines[lineNumber];
        boolean _notEquals = (!Objects.equal(comparedFirstTextLine, comparedSecondTextLine));
        if (_notEquals) {
          return Pair.<String, String>of(comparedFirstTextLine, comparedSecondTextLine);
        }
      }
    }
    Pair<String, String> _xifexpression = null;
    int _size = ((List<String>)Conversions.doWrapArray(firstTextLines)).size();
    int _size_1 = ((List<String>)Conversions.doWrapArray(secondTextLines)).size();
    boolean _greaterThan = (_size > _size_1);
    if (_greaterThan) {
      String _get = firstTextLines[numberOfCommonLines];
      _xifexpression = Pair.<String, String>of(_get, "");
    } else {
      Pair<String, String> _xifexpression_1 = null;
      int _size_2 = ((List<String>)Conversions.doWrapArray(secondTextLines)).size();
      int _size_3 = ((List<String>)Conversions.doWrapArray(firstTextLines)).size();
      boolean _greaterThan_1 = (_size_2 > _size_3);
      if (_greaterThan_1) {
        String _get_1 = secondTextLines[numberOfCommonLines];
        _xifexpression_1 = Pair.<String, String>of("", _get_1);
      } else {
        _xifexpression_1 = null;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  public static MultilineTextMatcher hasEachLineEqualTo(final String expected) {
    return new MultilineTextMatcher(expected);
  }
}
