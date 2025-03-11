package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.ConceptImpl;

@SuppressWarnings("all")
class ConceptI extends ConceptImpl {
  @Override
  public String toString() {
    return this.name;
  }
}
