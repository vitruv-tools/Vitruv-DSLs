package tools.vitruv.dsls.commonalities.language.elements;

import tools.vitruv.dsls.commonalities.language.elements.impl.LeastSpecificTypeImpl;

@SuppressWarnings("all")
public class LeastSpecificTypeI extends LeastSpecificTypeImpl {
  @Override
  public boolean isSuperTypeOf(final Classifier subType) {
    return true;
  }

  @Override
  public String getName() {
    return "LeastSpecificType";
  }

  @Override
  public String toString() {
    return "LeastSpecificType";
  }
}
