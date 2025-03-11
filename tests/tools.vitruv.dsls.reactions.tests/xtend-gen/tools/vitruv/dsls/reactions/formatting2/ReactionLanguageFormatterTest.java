package tools.vitruv.dsls.reactions.formatting2;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import org.eclipse.core.runtime.Platform;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
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
@SuppressWarnings("all")
public class ReactionLanguageFormatterTest {
  @Inject
  @Extension
  private ParseHelper<ReactionsFile> _parseHelper;

  @Inject
  @Extension
  private ISerializer _iSerializer;

  @DisplayName("reformat existing test classes")
  @ParameterizedTest(name = "{index} file: {1}")
  @ValueSource(strings = { "tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions", "tools/vitruv/dsls/reactions/tests/importTests/CommonRoutines.reactions", "tools/vitruv/dsls/reactions/tests/importTests/Direct2SN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/DirectRoutinesQN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/DirectSN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/Root.reactions", "tools/vitruv/dsls/reactions/tests/importTests/Transitive2SN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/Transitive3SN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/TransitiveRoutinesQN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/TransitiveRoutinesSN.reactions", "tools/vitruv/dsls/reactions/tests/importTests/TransitiveSN.reactions" })
  public void testExistingReactionsFilesFormatting(final String filePathInSourceFolder) {
    final String content = this.loadJavaFileContents(filePathInSourceFolder);
    MatcherAssert.<String>assertThat(this.format(this.destroyFormatting(content)), MultilineTextMatcher.hasEachLineEqualTo(content));
  }

  private String loadJavaFileContents(final String filePathInSourceFolder) {
    try {
      final ClassLoader classLoader = this.getClass().getClassLoader();
      File _xifexpression = null;
      boolean _isRunning = Platform.isRunning();
      if (_isRunning) {
        _xifexpression = new File(("src/" + filePathInSourceFolder));
      } else {
        String _file = classLoader.getResource(filePathInSourceFolder).getFile();
        _xifexpression = new File(_file);
      }
      final File file = _xifexpression;
      return new Scanner(file).useDelimiter("\\Z").next();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @DisplayName("reformat file with all kinds of Reactions statements")
  @Test
  public void testAllStatementTypes() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static tools.vitruv.dsls.reactions.tests.simpleChangesTests.SimpleChangesTestsExecutionMonitor.ChangeType.*;");
    _builder.newLine();
    _builder.append("import tools.vitruv.change.atomic.eobject.EObjectExistenceEChange");
    _builder.newLine();
    _builder.append("import static extension tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory.allElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as minimal");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as minimal2");
    _builder.newLine();
    _builder.newLine();
    _builder.append("reactions: simpleChangesTests");
    _builder.newLine();
    _builder.append("in reaction to changes in minimal");
    _builder.newLine();
    _builder.append("execute actions in minimal");
    _builder.newLine();
    _builder.newLine();
    _builder.append("// A test reaction");
    _builder.newLine();
    _builder.append("reaction InsertedNonRoot {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("after element minimal::NonRoot inserted in minimal::NonRootObjectContainerHelper[nonRootObjectsContainment]");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("call insertNonRoot(newValue)");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("/*");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* This is a routine.");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* The only one.");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("routine insertNonRoot(minimal::NonRoot nonRootElement) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("match {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("require absence of minimal::NonRoot corresponding to nonRootElement");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val correspondingContainer = retrieve EObject corresponding to nonRootElement.eContainer tagged \"test\" with true");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val optionalCorresponding = retrieve optional minimal::NonRoot corresponding to {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("nonRootElement");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("} tagged null");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val correspondingAsIterable = retrieve many minimal::NonRoot corresponding to {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("return nonRootElement;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("} with {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("correspondingAsIterable !== null");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("create {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val newNonRoot = new minimal::NonRoot");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("update {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("if (optionalCorresponding.empty) {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("// This can never happen due to matcher checks");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("newNonRoot.id = nonRootElement.id");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("{");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("{");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("newNonRoot.value = \"new\"");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("insertNonRoot(newNonRoot)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final String expected = _builder.toString();
    MatcherAssert.<String>assertThat(this.format(this.destroyFormatting(expected)), MultilineTextMatcher.hasEachLineEqualTo(expected));
  }

  private String format(final String reactionsCodeToFormat) {
    try {
      return this._iSerializer.serialize(this._parseHelper.parse(reactionsCodeToFormat), SaveOptions.newBuilder().format().getOptions());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private String destroyFormatting(final String textToUnformat) {
    final Function1<String, String> _function = (String it) -> {
      final Function1<String, String> _function_1 = (String it_1) -> {
        String _xifexpression = null;
        boolean _isCommentLine = this.isCommentLine(it_1);
        if (_isCommentLine) {
          _xifexpression = this.preserveCommentLine(it_1);
        } else {
          _xifexpression = this.mixUpSpacingTerminals(it_1);
        }
        return _xifexpression;
      };
      return this.applyToEachLineRemovingLinebreaks(it, _function_1);
    };
    return this.preserveEmptyLines(textToUnformat, _function);
  }

  private String preserveEmptyLines(final String text, final Function1<? super String, ? extends String> applicator) {
    String _lineSeparator = System.lineSeparator();
    String _lineSeparator_1 = System.lineSeparator();
    String _plus = (_lineSeparator + _lineSeparator_1);
    String _lineSeparator_2 = System.lineSeparator();
    String _lineSeparator_3 = System.lineSeparator();
    String _plus_1 = (_lineSeparator_2 + _lineSeparator_3);
    return this.replace(text, _plus, _plus_1, applicator);
  }

  private String applyToEachLineRemovingLinebreaks(final String text, final Function1<? super String, ? extends String> applicator) {
    final Function1<String, String> _function = (String it) -> {
      return applicator.apply(it);
    };
    return IterableExtensions.join(ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(text.split(System.lineSeparator()))), _function));
  }

  private boolean isCommentLine(final String line) {
    return (this.isFirstCommentLine(line) || this.isInnerCommentLine(line));
  }

  private boolean isFirstCommentLine(@Extension final String line) {
    return (line.contains("//") || line.contains("/*"));
  }

  private boolean isInnerCommentLine(@Extension final String line) {
    return line.contains(" *");
  }

  private String preserveCommentLine(@Extension final String line) {
    boolean _isFirstCommentLine = this.isFirstCommentLine(line);
    if (_isFirstCommentLine) {
      String _lineSeparator = System.lineSeparator();
      String _plus = (_lineSeparator + line);
      String _lineSeparator_1 = System.lineSeparator();
      return (_plus + _lineSeparator_1);
    } else {
      boolean _isInnerCommentLine = this.isInnerCommentLine(line);
      if (_isInnerCommentLine) {
        String _lineSeparator_2 = System.lineSeparator();
        return (line + _lineSeparator_2);
      } else {
        throw new IllegalArgumentException(("line is no comment: " + line));
      }
    }
  }

  private String mixUpSpacingTerminals(final String text) {
    String _lineSeparator = System.lineSeparator();
    String _lineSeparator_1 = System.lineSeparator();
    String _plus = (_lineSeparator + _lineSeparator_1);
    final Function1<String, String> _function = (String it) -> {
      final Function1<String, String> _function_1 = (String it_1) -> {
        return it_1.replace("\t", "  ");
      };
      return this.replace(it, "::", "  ::  ", _function_1);
    };
    final String result = this.replace(text, " ", _plus, _function);
    boolean _notEquals = (!Objects.equal(result, "}"));
    if (_notEquals) {
      return (result + " ");
    } else {
      return result;
    }
  }

  private String replace(final String text, final String original, final String replacement, final Function1<? super String, ? extends String> applyOnIntermediatePlaceholderString) {
    final Function2<String, Character, String> _function = (String inputText, Character placeholder) -> {
      final String placeholderString = ("" + placeholder);
      final String textWithPlaceholder = inputText.replace(original, placeholderString);
      final String modifiedTextWithPlaceholder = applyOnIntermediatePlaceholderString.apply(textWithPlaceholder);
      final String textWithReplacement = modifiedTextWithPlaceholder.replace(placeholderString, replacement);
      return textWithReplacement;
    };
    return this.modifyTextUsingPlaceholderChar(text, _function);
  }

  private char nextPlaceholderChar;

  @BeforeEach
  public void restorePlaceholderChars() {
    this.nextPlaceholderChar = ((char) 14);
  }

  /**
   * We temporarily replace formatting sequences (such as whitespaces, tabs, newlines etc.)
   * with placeholders to restore different formatting sequences later on.
   * To this end, we use usually unused ASCII symbols as placeholders, specifically
   * the ASCII symbols 14 to 31.
   */
  private String modifyTextUsingPlaceholderChar(final String text, final Function2<? super String, ? super Character, ? extends String> modifier) {
    final String result = modifier.apply(text, Character.valueOf(this.retrievePlaceholderChar()));
    this.returnPlaceholderChar();
    return result;
  }

  private char retrievePlaceholderChar() {
    if ((this.nextPlaceholderChar > 31)) {
      throw new IllegalStateException("Too many placeholder chars are used");
    }
    return this.nextPlaceholderChar++;
  }

  private void returnPlaceholderChar() {
    this.nextPlaceholderChar--;
  }
}
