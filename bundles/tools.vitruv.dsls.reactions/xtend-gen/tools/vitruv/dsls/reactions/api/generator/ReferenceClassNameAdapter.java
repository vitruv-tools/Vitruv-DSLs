package tools.vitruv.dsls.reactions.api.generator;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;

/**
 * Can be added to an EMF object to tell the reactions generator how to
 * reference that object’s class. When generating reactions for dynamically
 * created EMF metamodels, the generator code needs a way to know how to
 * reference classes of the metamodel, as the default mechanisms will usually
 * point to the wrong classes. If the generator sees this adapter, it will use
 * its information.
 */
@FinalFieldsConstructor
@SuppressWarnings("all")
public final class ReferenceClassNameAdapter implements Adapter {
  private final String classFullyQualifiedName;

  public String getQualifiedNameForReference() {
    return this.classFullyQualifiedName;
  }

  @Override
  public Notifier getTarget() {
    return null;
  }

  @Override
  public boolean isAdapterForType(final Object type) {
    return (type == ReferenceClassNameAdapter.class);
  }

  @Override
  public void notifyChanged(final Notification notification) {
  }

  @Override
  public void setTarget(final Notifier newTarget) {
  }

  public ReferenceClassNameAdapter(final String classFullyQualifiedName) {
    super();
    this.classFullyQualifiedName = classFullyQualifiedName;
  }
}
