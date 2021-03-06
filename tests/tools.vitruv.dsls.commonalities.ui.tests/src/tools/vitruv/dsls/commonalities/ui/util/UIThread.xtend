package tools.vitruv.dsls.commonalities.ui.util

import edu.kit.ipd.sdq.activextendannotations.Utility
import org.eclipse.swt.widgets.Display
import tools.vitruv.testutils.Capture
import static extension tools.vitruv.testutils.Capture.*
import java.util.concurrent.Callable

@Utility
class UIThread {
	def static runSync(Runnable runnable) {
		val error = new Capture<Throwable>
		Display.^default.syncExec [
			try {
				runnable.run()
			} catch (Throwable t) {
				error += t				
			}
		]
		if (error.isSet) {
			throw -error
		}
	}
	
	def static <T> T runSync(Callable<T> callable) {
		val result = new Capture<T>
		runSync([
			callable.call() >> result
		] as Runnable)
		return -result
	}
}