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

  /**
   * Checks whether {@code testDataString} contains the data tag {@code dataTag}. The strings are
   * created by {@link #toTestDataString(String, String...)}.
   *
   * <p>Called from the reactions in the {@code import}, so it wrongly appears as unused in
   * Java.
   *
   * @param testDataString {@link String}
   * @param dataTag {@link String} tag without prefix and separator
   * @return {@code true} if testDataString contains dataTag, {@code false} otherwise
   */
  public static boolean containsDataTag(String testDataString, String dataTag) {
    return testDataString != null
        && testDataString.contains(
            DATA_TAG_SEPARATOR + DATA_TAG_PREFIX + dataTag + DATA_TAG_SEPARATOR);
  }

  private ImportTestsUtils() {}
}
