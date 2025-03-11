package tools.vitruv.dsls.commonalities.language.elements;

import tools.vitruv.dsls.commonalities.language.elements.impl.MostSpecificTypeImpl;

@SuppressWarnings("all")
public class MostSpecificTypeI extends MostSpecificTypeImpl {
  @Override
  public boolean isSuperTypeOf(final Classifier subType) {
    return (this == subType);
  }

  @Override
  public String getName() {
    return "MostSpecificType";
  }

  @Override
  public String toString() {
    return "MostSpecificType";
  }
}
