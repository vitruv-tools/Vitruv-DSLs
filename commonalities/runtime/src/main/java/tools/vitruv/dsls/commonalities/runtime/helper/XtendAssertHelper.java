package tools.vitruv.dsls.commonalities.runtime.helper;

/** Helper class for Xtend assert statements. */
public class XtendAssertHelper {

  private XtendAssertHelper() {
  }

  /**
   * Asserts that the given boolean expression is true.
   *
   * @param booleanExpression the boolean expression to check
   */
  public static void assertTrue(boolean booleanExpression) {
    if (!booleanExpression) {
      throw new IllegalStateException("Assertion failed: expected true but was false");
    }
  }
}
