package tools.vitruv.dsls.commonalities.ui.quickfix;

import com.google.common.base.Objects;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import tools.vitruv.testutils.Capture;

@Singleton
@SuppressWarnings("all")
public class XtextAssertions {
  @FinalFieldsConstructor
  private static class NoValidationIssues extends TypeSafeMatcher<IXtextDocument> {
    private final IResourceValidator resourceValidator;

    private List<Issue> issues;

    @Override
    protected boolean matchesSafely(final IXtextDocument document) {
      final IUnitOfWork<List<Issue>, XtextResource> _function = (XtextResource state) -> {
        return this.resourceValidator.validate(state, CheckMode.ALL, null);
      };
      this.issues = document.<List<Issue>>readOnly(_function);
      return this.issues.isEmpty();
    }

    @Override
    public void describeTo(final Description description) {
      description.appendText("an Xtext document with no validation issues");
    }

    @Override
    protected void describeMismatchSafely(final IXtextDocument document, final Description mismatchDescription) {
      XtextAssertions.printIssueList(mismatchDescription.appendText("the document ").appendValue(document.getResourceURI()).appendText(" had the following issues:"), this.issues, false);
    }

    public NoValidationIssues(final IResourceValidator resourceValidator) {
      super();
      this.resourceValidator = resourceValidator;
    }
  }

  @FinalFieldsConstructor
  private static class HasIssuesWithCode extends TypeSafeMatcher<IXtextDocument> {
    private final IResourceValidator resourceValidator;

    private final List<String> expectedIssueCodes;

    private final List<String> missingIssueCodes = new ArrayList<String>();

    private List<Issue> issues;

    @Override
    public void describeTo(final Description description) {
      description.appendText("an Xtext document with at least these issues: ").<String>appendValueList("[", ", ", "]", 
        this.expectedIssueCodes);
    }

    @Override
    protected boolean matchesSafely(final IXtextDocument document) {
      final IUnitOfWork<List<Issue>, XtextResource> _function = (XtextResource state) -> {
        return this.resourceValidator.validate(state, CheckMode.ALL, null);
      };
      this.issues = document.<List<Issue>>readOnly(_function);
      ArrayList<Issue> issuesCopy = new ArrayList<Issue>(this.issues);
      for (final String issueCode : this.expectedIssueCodes) {
        {
          boolean found = false;
          for (final Iterator<Issue> iter = issuesCopy.iterator(); (iter.hasNext() && (!found));) {
            String _code = iter.next().getCode();
            boolean _equals = Objects.equal(_code, issueCode);
            if (_equals) {
              found = true;
              iter.remove();
            }
          }
          if ((!found)) {
            this.missingIssueCodes.add(issueCode);
          }
        }
      }
      return this.missingIssueCodes.isEmpty();
    }

    @Override
    protected void describeMismatchSafely(final IXtextDocument document, final Description mismatchDescription) {
      XtextAssertions.printIssueList(mismatchDescription.appendText("the document ").appendValue(document.getResourceURI()).appendText(" lacked these issues: ").<String>appendValueList("[", ",", "]", this.missingIssueCodes).appendText(
        System.lineSeparator()).appendText("    all found issues were:"), this.issues, true);
    }

    public HasIssuesWithCode(final IResourceValidator resourceValidator, final List<String> expectedIssueCodes) {
      super();
      this.resourceValidator = resourceValidator;
      this.expectedIssueCodes = expectedIssueCodes;
    }
  }

  @Inject
  protected IResourceValidator resourceValidator;

  public Matcher<? super IXtextDocument> hasNoValidationIssues() {
    return new XtextAssertions.NoValidationIssues(this.resourceValidator);
  }

  public Matcher<? super IXtextDocument> hasIssues(final String... codes) {
    return new XtextAssertions.HasIssuesWithCode(this.resourceValidator, (List<String>)Conversions.doWrapArray(codes));
  }

  public static IXtextDocument getCurrentlyOpenedXtextDocument() {
    final Capture<IXtextDocument> document = new Capture<IXtextDocument>();
    final Runnable _function = () -> {
      IXtextDocument _document = EditorUtils.getActiveXtextEditor().getDocument();
      Capture.<IXtextDocument>operator_doubleGreaterThan(_document, document);
    };
    Display.getDefault().syncExec(_function);
    return document.operator_minus();
  }

  private static void printIssueList(final Description description, final List<Issue> issues, final boolean printCode) {
    final Consumer<Issue> _function = (Issue it) -> {
      description.appendText(System.lineSeparator()).appendText("      • ").appendText(it.getSeverity().toString()).appendText(": ").appendText(it.getMessage()).appendText(" (").appendText("@lines ").appendText(it.getLineNumber().toString()).appendText(":").appendText(it.getColumn().toString()).appendText("–").appendText(it.getLineNumberEnd().toString()).appendText(":").appendText(it.getColumnEnd().toString()).appendText(")");
      if (printCode) {
        description.appendText(" (code: ").appendValue(it.getCode()).appendText(")");
      }
    };
    issues.forEach(_function);
  }
}
