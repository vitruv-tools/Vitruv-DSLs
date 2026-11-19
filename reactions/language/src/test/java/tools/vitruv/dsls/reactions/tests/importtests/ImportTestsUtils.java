package tools.vitruv.dsls.reactions.tests.importtests;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Contains utility methods for {@link ImportTests}.
 */
@Utility
public final class ImportTestsUtils {
  public static final String TEST_NAME_PREFIX = "test:";
  public static final String DATA_TAG_PREFIX = "data:";
  public static final String DATA_TAG_SEPARATOR = ";";

  /**
   * Concatenates {@code testName} and {@code dataTags} to form a string of the form
   * {@code "test:$testName;data:$dataTags[0];$dataTags[1]..."}.
   *
   * @param testName {@link String}
   * @param dataTags {@link String} array, possibly empty
   * @return {@link String}
   */
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

  private ImportTestsUtils() {}
}
