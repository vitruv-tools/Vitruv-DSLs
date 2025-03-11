package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

@SuppressWarnings("all")
public interface IParticipationConditionOperator {
  void enforce();

  boolean check();
}
