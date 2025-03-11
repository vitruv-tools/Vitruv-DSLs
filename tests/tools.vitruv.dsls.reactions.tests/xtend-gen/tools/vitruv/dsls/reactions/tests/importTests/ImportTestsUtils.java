package tools.vitruv.dsls.reactions.tests.importTests;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collections;
import java.util.List;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@Utility
@SuppressWarnings("all")
public final class ImportTestsUtils {
  public static final String TEST_NAME_PREFIX = "test:";

  public static final String DATA_TAG_PREFIX = "data:";

  public static final String DATA_TAG_SEPARATOR = ";";

  public static String toTestDataString(final String testName, final String... dataTags) {
    List<String> _elvis = null;
    if (((List<String>) Conversions.doWrapArray(dataTags)) != null) {
      _elvis = ((List<String>) Conversions.doWrapArray(dataTags));
    } else {
      _elvis = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
    }
    final List<String> dataTagsList = _elvis;
    final Function1<String, CharSequence> _function = (String it) -> {
      return (ImportTestsUtils.DATA_TAG_PREFIX + it);
    };
    String _join = IterableExtensions.<String>join(dataTagsList, ImportTestsUtils.DATA_TAG_SEPARATOR, ImportTestsUtils.DATA_TAG_SEPARATOR, ImportTestsUtils.DATA_TAG_SEPARATOR, _function);
    return ((ImportTestsUtils.TEST_NAME_PREFIX + testName) + _join);
  }

  public static boolean containsDataTag(final String testDataString, final String dataTag) {
    return ((testDataString != null) && testDataString.contains((((ImportTestsUtils.DATA_TAG_SEPARATOR + ImportTestsUtils.DATA_TAG_PREFIX) + dataTag) + ImportTestsUtils.DATA_TAG_SEPARATOR)));
  }

  private ImportTestsUtils() {
    
  }
}
