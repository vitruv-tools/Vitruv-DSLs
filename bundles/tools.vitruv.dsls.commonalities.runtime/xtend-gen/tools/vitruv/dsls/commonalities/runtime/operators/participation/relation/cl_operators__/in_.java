package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.cl_operators__;

import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ContainmentOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.IParticipationRelationOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ParticipationRelationOperator;

@ParticipationRelationOperator(name = "in")
@SuppressWarnings("all")
public class in_ implements IParticipationRelationOperator {
  private final ContainmentOperator delegate;

  public in_(final EObject[] leftObjects, final EObject[] rightObjects) {
    this.delegate = new ContainmentOperator(leftObjects, rightObjects);
  }

  public void enforce() {
     this.delegate.enforce();
  }

  public boolean check() {
    return  this.delegate.check();
  }
}
