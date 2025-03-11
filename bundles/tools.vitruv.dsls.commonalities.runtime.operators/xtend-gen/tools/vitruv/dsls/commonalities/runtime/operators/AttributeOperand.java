package tools.vitruv.dsls.commonalities.runtime.operators;

import com.google.common.base.Preconditions;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

@SuppressWarnings("all")
public class AttributeOperand {
  private final EObject object;

  private final EStructuralFeature feature;

  public AttributeOperand(final EObject object, final EStructuralFeature feature) {
    Preconditions.<EObject>checkNotNull(object, "Object is null!");
    Preconditions.<EStructuralFeature>checkNotNull(feature, "Feature is null!");
    this.object = object;
    this.feature = feature;
  }

  public EObject getObject() {
    return this.object;
  }

  public EStructuralFeature getFeature() {
    return this.feature;
  }
}
