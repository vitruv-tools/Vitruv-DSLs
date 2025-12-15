package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EPackage;

/**
 * Remaps an Ecore model from one URI to another in the global EPackage
 * registry.
 */
public class RemapEcoreModel {
  private String from;

  /**
   * Gets the source URI from which to remap.
   *
   * @return the source URI
   */
  public String getFrom() {
    return from;
  }

  /**
   * Sets the source URI from which to remap.
   *
   * @param from the source URI
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Sets the target URI to which to remap.
   *
   * @param uri the target URI
   */
  public void setTo(String uri) {
    EPackage.Registry.INSTANCE.put(uri, EPackage.Registry.INSTANCE.get(from));
  }
}
