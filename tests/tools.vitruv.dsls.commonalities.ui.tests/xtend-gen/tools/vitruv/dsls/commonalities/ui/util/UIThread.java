package tools.vitruv.dsls.commonalities.ui.util;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.concurrent.Callable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import tools.vitruv.testutils.Capture;

@Utility
@SuppressWarnings("all")
public final class UIThread {
  public static void runSync(final Runnable runnable) {
    try {
      final Capture<Throwable> error = new Capture<Throwable>();
      final Runnable _function = () -> {
        try {
          runnable.run();
        } catch (final Throwable _t) {
          if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            error.operator_add(t);
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      };
      Display.getDefault().syncExec(_function);
      boolean _isSet = error.getIsSet();
      if (_isSet) {
        throw error.operator_minus();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static <T extends Object> T runSync(final Callable<T> callable) {
    final Capture<T> result = new Capture<T>();
    final Function1<Object, T> _function = (Object it) -> {
      try {
        T _call = callable.call();
        return Capture.<T>operator_doubleGreaterThan(_call, result);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    UIThread.runSync(
      ((Runnable) new Runnable() {
          public void run() {
            _function.apply(null);
          }
      }));
    return result.operator_minus();
  }

  private UIThread() {
    
  }
}
