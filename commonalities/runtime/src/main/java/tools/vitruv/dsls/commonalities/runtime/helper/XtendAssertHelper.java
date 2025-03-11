package tools.vitruv.dsls.commonalities.runtime.helper;

/** Helper class for Xtend assert statements. */
public class XtendAssertHelper {
  /**
   * Asserts that the given boolean expression is true.
   *
   * @param booleanExpression the boolean expression to check
   */
  public static void assertTrue(boolean booleanExpression) {
    assert booleanExpression;
  }
}
