package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class RemapEcoreModel {
  @Accessors
  private String from;

  public Object setTo(final String uri) {
    return EPackage.Registry.INSTANCE.put(uri, EPackage.Registry.INSTANCE.get(this.from));
  }

  @Pure
  public String getFrom() {
    return this.from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }
}
