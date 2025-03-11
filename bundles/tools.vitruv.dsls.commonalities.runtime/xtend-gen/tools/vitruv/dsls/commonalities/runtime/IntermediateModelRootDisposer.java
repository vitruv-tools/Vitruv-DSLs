package tools.vitruv.dsls.commonalities.runtime;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root;

/**
 * Deletes intermediate model root objects
 */
@SuppressWarnings("all")
class IntermediateModelRootDisposer extends AdapterImpl {
  static final IntermediateModelRootDisposer INSTANCE = new IntermediateModelRootDisposer();

  private IntermediateModelRootDisposer() {
  }

  @Override
  public void notifyChanged(@Extension final Notification msg) {
    Object _notifier = msg.getNotifier();
    final Root root = ((Root) _notifier);
    if (((msg.getEventType() == Notification.REMOVE) || (msg.getEventType() == Notification.REMOVE_MANY))) {
      boolean _isEmpty = root.getIntermediates().isEmpty();
      if (_isEmpty) {
      }
    }
  }
}
