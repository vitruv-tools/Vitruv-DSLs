package tools.vitruv.dsls.commonalities.tests.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Provider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.testutils.matchers.ModelMatchers;

@Singleton
@SuppressWarnings("all")
public class CommonalityParseHelper {
  @Inject
  private ParseHelper<CommonalityFile> parseHelper;

  @Inject
  private ValidationTestHelper validator;

  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;

  public CommonalityFile parseInSet(final ResourceSet resourceSet, final CharSequence input) {
    try {
      final CommonalityFile result = this.parseHelper.parse(input, resourceSet);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Cannot parse:");
      _builder.newLine();
      _builder.append(input);
      _builder.newLineIfNotEmpty();
      final String errorMessage = _builder.toString();
      MatcherAssert.<CommonalityFile>assertThat(errorMessage, result, CoreMatchers.<Object>is(CoreMatchers.notNullValue()));
      MatcherAssert.<Resource>assertThat(errorMessage, result.eResource(), ModelMatchers.hasNoErrors());
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public CommonalityFile parse(final CharSequence input) {
    return this.parseInSet(this.resourceSetProvider.get(), input);
  }

  public CommonalityFile parseAndValidateInSet(final ResourceSet resourceSet, final CharSequence input) {
    final CommonalityFile result = this.parseInSet(resourceSet, input);
    this.validator.validate(result);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Validation failed: ");
    _builder.newLine();
    _builder.append(input);
    _builder.newLineIfNotEmpty();
    final String errorMessage = _builder.toString();
    MatcherAssert.<Resource>assertThat(errorMessage, result.eResource(), ModelMatchers.hasNoErrors());
    return result;
  }

  public CommonalityFile parseAndValidate(final CharSequence input) {
    return this.parseAndValidateInSet(this.resourceSetProvider.get(), input);
  }

  public void inSameResourceSet(final Procedure1<? super ResourceSet> action) {
    action.apply(this.resourceSetProvider.get());
  }
}
