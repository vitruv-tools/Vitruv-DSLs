package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;

@FinalFieldsConstructor
@SuppressWarnings("all")
public abstract class AbstractParticipationRelationOperator implements IParticipationRelationOperator {
  protected final EObject[] leftObjects;

  protected final EObject[] rightObjects;

  public AbstractParticipationRelationOperator(final EObject[] leftObjects, final EObject[] rightObjects) {
    super();
    this.leftObjects = leftObjects;
    this.rightObjects = rightObjects;
  }
}
