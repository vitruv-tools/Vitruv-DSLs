package tools.vitruv.dsls.reactions.runtime.locking;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.AdditiveEChange;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.SubtractiveEChange;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.atomic.root.RemoveRootEObject;
import tools.vitruv.dsls.reactions.runtime.locking.AccessSummary.FeatureWrite;
import tools.vitruv.dsls.reactions.runtime.locking.AccessSummary.ValueSide;

/**
 * The trigger of a generated reaction, extracted from the bytecode of its
 * {@code isCurrentChangeMatchingTrigger} method: the EChange type it checks, the class of the
 * affected element, the name of the affected feature and the class the new or old value must have.
 * The user-defined precondition ({@code with ...}) is not part of it; it runs inside the reaction
 * and its reads are part of the reaction's access summary.
 *
 * @param changeType the checked EChange interface, {@link EChange} for {@code anychange}
 * @param affectedClass the class the affected element must be an instance of, or {@code null}
 * @param featureName the name the affected feature must have, or {@code null}
 * @param valueClass the class the checked value must be an instance of, or {@code null}
 * @param valueSide which value {@code valueClass} refers to
 */
public record ReactionTrigger(Class<?> changeType, EClass affectedClass, String featureName,
    EClass valueClass, ValueSide valueSide) {

  /** Whether a concrete change matches (without the user-defined precondition). */
  public boolean matches(EChange<EObject> change) {
    if (!changeType.isInstance(change)) {
      return false;
    }
    if (affectedClass != null) {
      EObject affected = affectedElement(change);
      if (affected == null || !affectedClass.isSuperTypeOf(affected.eClass())) {
        return false;
      }
    }
    if (featureName != null && !(change instanceof FeatureEChange<?, ?> featureChange
        && featureName.equals(featureChange.getAffectedFeature().getName()))) {
      return false;
    }
    if (valueClass != null) {
      // as the generated check: a missing (default) value passes, a present one must fit
      Object value = valueSide == ValueSide.NEW ? newValue(change) : oldValue(change);
      return !(value instanceof EObject eObject) || valueClass.isSuperTypeOf(eObject.eClass());
    }
    return true;
  }

  /** Whether a feature write of a reaction may produce a change that triggers this reaction. */
  boolean mayBeTriggeredBy(FeatureWrite write, EmfModelIndex index) {
    if (!changeType.isAssignableFrom(FeatureEChange.class)
        && !FeatureEChange.class.isAssignableFrom(changeType)) {
      return false;
    }
    if (featureName != null && !featureName.equals(write.feature().getName())) {
      return false;
    }
    EClass receiver = write.receiver() != null ? write.receiver()
        : write.feature().getEContainingClass();
    if (affectedClass != null && !index.overlap(affectedClass, receiver)) {
      return false;
    }
    // a value check only excludes the write if the write's value on that side is known
    if (valueClass != null && write.value() != null && write.side() == valueSide) {
      return index.overlap(valueClass, write.value());
    }
    return true;
  }

  /** Whether creating (or deleting, or inserting/removing as root) an instance may trigger it. */
  boolean mayBeTriggeredByExistenceChange(EClass eClass, Class<?> existenceChangeType,
      EmfModelIndex index) {
    if (!changeType.isAssignableFrom(existenceChangeType)) {
      return false;
    }
    return affectedClass == null || index.overlap(affectedClass, eClass);
  }

  private static EObject affectedElement(EChange<EObject> change) {
    if (change instanceof FeatureEChange<?, ?> featureChange) {
      return (EObject) featureChange.getAffectedElement();
    }
    if (change instanceof CreateEObject<EObject> create) {
      return create.getAffectedElement();
    }
    if (change instanceof DeleteEObject<EObject> delete) {
      return delete.getAffectedElement();
    }
    if (change instanceof InsertRootEObject<EObject> insert) {
      return insert.getNewValue();
    }
    if (change instanceof RemoveRootEObject<EObject> remove) {
      return remove.getOldValue();
    }
    return null;
  }

  static Object newValue(EChange<EObject> change) {
    return change instanceof AdditiveEChange<?, ?> additive ? additive.getNewValue() : null;
  }

  static Object oldValue(EChange<EObject> change) {
    return change instanceof SubtractiveEChange<?, ?> subtractive ? subtractive.getOldValue()
        : null;
  }
}
