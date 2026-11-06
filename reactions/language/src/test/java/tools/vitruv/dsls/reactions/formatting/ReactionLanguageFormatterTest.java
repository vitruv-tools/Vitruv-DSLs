package tools.vitruv.dsls.reactions.formatting;

import com.google.inject.Inject;
import java.io.File;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.runtime.Platform;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)

class ReactionLanguageFormatterTest {
  @Inject
  private ParseHelper<ReactionsFile> parseHelper;

  @Inject
  private ISerializer iSerializer;

  @DisplayName("reformat existing test classes")
  @ParameterizedTest(name = "{index} file: {1}")
  @ValueSource(strings = {
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
  })
  void testExistingReactionsFilesFormatting(String filePathInSourceFolder) {
    String content = loadJavaFileContents(filePathInSourceFolder);
    MatcherAssert.assertThat(format(destroyFormatting(content)), MultilineTextMatcher.hasEachLineEqualTo(content));
  }

  private String loadJavaFileContents(String filePathInSourceFolder) {
    try {
      ClassLoader classLoader = this.getClass().getClassLoader();
      File file =
          (Platform.isRunning()) ?
          new File(("src/" + filePathInSourceFolder)) :
          new File(classLoader.getResource(filePathInSourceFolder).getFile());
      return new Scanner(file).useDelimiter("\\Z").next();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @DisplayName("reformat file with all kinds of Reactions statements")
  @Test
  void testAllStatementTypes() {
    String expected = loadJavaFileContents(
        "tools/vitruv/dsls/reactions/tests/AllStatementTypes.reactions");
    MatcherAssert.assertThat(format(destroyFormatting(expected)), MultilineTextMatcher.hasEachLineEqualTo(expected));
  }

  private String format(String reactionsCodeToFormat) {
    try {
      return iSerializer.serialize(
          parseHelper.parse(reactionsCodeToFormat), SaveOptions.newBuilder().format().getOptions());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private String destroyFormatting(String textToUnformat) {
    return preserveEmptyLines(textToUnformat,
        text -> applyToEachLineRemovingLinebreaks(text, line -> {
      if (isCommentLine(line)) {
        return preserveCommentLine(line);
      } else {
        return mixUpSpacingTerminals(line);
      }
    }));
  }

  private String preserveEmptyLines(String text, Function<String, String> applicator) {
    String replacement = System.lineSeparator().repeat(2);
    return this.replace(text, replacement, replacement, applicator);
  }

  private String applyToEachLineRemovingLinebreaks(String text,
                                                   Function<String, String> applicator) {
    return Stream.of(text.split(System.lineSeparator()))
        .map(applicator)
        .collect(Collectors.joining());
  }

  private boolean isCommentLine(String line) {
    return isFirstCommentLine(line) || isInnerCommentLine(line);
  }

  private boolean isFirstCommentLine(String line) {
    return line.contains("//") || line.contains("/*");
  }

  private boolean isInnerCommentLine(String line) {
    return line.contains(" *");
  }

  private String preserveCommentLine(String line) {
    if (isFirstCommentLine(line)) {
      return System.lineSeparator() + line + System.lineSeparator();
    }
    if (isInnerCommentLine(line)) {
      return line + System.lineSeparator();
    }
    throw new IllegalArgumentException(("line is no comment: " + line));
  }

  private String mixUpSpacingTerminals(String text) {
    String result = replace(text, " ",
        System.lineSeparator().repeat(2),
        all ->
           replace(all, "::", "  ::  ", line -> line.replace("\t", "  ")
           )
    );
    if (!result.equals("}")) {
      return (result + " ");
    } else {
      return result;
    }
  }

  private String replace(String text, String original, String replacement,
                         Function<String, String> applyOnIntermediatePlaceholderString) {
    return modifyTextUsingPlaceholderChar(text, (inputText, placeholder) -> {
      String placeholderString = ("" + placeholder);
      String textWithPlaceholder = inputText.replace(original, placeholderString);
      String modifiedTextWithPlaceholder = applyOnIntermediatePlaceholderString.apply(textWithPlaceholder);
      return modifiedTextWithPlaceholder.replace(placeholderString, replacement);
    });
  }

  private char nextPlaceholderChar;

  @BeforeEach
  void restorePlaceholderChars() {
    this.nextPlaceholderChar = ((char) 14);
  }

  /**
   * We temporarily replace formatting sequences (such as whitespaces, tabs, newlines etc.)
   * with placeholders to restore different formatting sequences later on.
   * To this end, we use usually unused ASCII symbols as placeholders, specifically
   * the ASCII symbols 14 to 31.
   */
  private String modifyTextUsingPlaceholderChar(String text,
                                                BiFunction<? super String, ? super Character, ? extends String> modifier) {
    String result = modifier.apply(text, retrievePlaceholderChar());
    this.returnPlaceholderChar();
    return result;
  }

  private char retrievePlaceholderChar() {
    if (this.nextPlaceholderChar > 31) {
      throw new IllegalStateException("Too many placeholder chars are used");
    }
    return this.nextPlaceholderChar++;
  }

  private void returnPlaceholderChar() {
    this.nextPlaceholderChar--;
  }
}
