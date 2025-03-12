package tools.vitruv.dsls.commonalities.runtime.helper;

public class XtendAssertHelper {

	private XtendAssertHelper() {}

	public static void assertTrue(boolean booleanExpression) {
		assert booleanExpression;
	}
}
