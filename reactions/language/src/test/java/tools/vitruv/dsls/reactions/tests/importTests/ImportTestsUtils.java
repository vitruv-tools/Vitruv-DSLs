package tools.vitruv.dsls.reactions.tests.importTests;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Utility
public final class ImportTestsUtils {
  public static final String TEST_NAME_PREFIX = "test:";
  public static final String DATA_TAG_PREFIX = "data:";
  public static final String DATA_TAG_SEPARATOR = ";";

  public static String toTestDataString(String testName, String... dataTags) {
    List<String> dataTagsList = new ArrayList<>();
    if (dataTags != null) {
      Collections.addAll(dataTagsList, dataTags);
    }
    Function<String, String> prefix = string -> DATA_TAG_PREFIX + string;
    var prefixedTags = dataTagsList.stream()
        .map(prefix)
        .toList();

    String joined = String.join(
        DATA_TAG_SEPARATOR,
        prefixedTags);

    return TEST_NAME_PREFIX + testName + DATA_TAG_SEPARATOR + joined + DATA_TAG_SEPARATOR;
  }

  public static boolean containsDataTag(String testDataString, String dataTag) {
    return testDataString != null
        && testDataString.contains(
            DATA_TAG_SEPARATOR + DATA_TAG_PREFIX + dataTag+ DATA_TAG_SEPARATOR
    );
  }

  private ImportTestsUtils() {}
}
