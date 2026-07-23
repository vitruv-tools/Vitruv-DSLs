package tools.vitruv.dsls.vitruvocl.allinstances.fixtures;

import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/** Small shared helper for wiring up test instance models via dynamic EMF containment features. */
public final class ModelBuilding {

  private ModelBuilding() {}

  /** Appends {@code children} to the many-valued containment feature {@code ref} of {@code owner}. */
  @SuppressWarnings("unchecked")
  public static void addMany(EObject owner, EReference ref, EObject... children) {
    List<EObject> list = (List<EObject>) owner.eGet(ref);
    list.addAll(List.of(children));
  }
}
