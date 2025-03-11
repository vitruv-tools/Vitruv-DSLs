package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.SimpleReferenceMappingImpl;

@SuppressWarnings("all")
class SimpleReferenceMappingI extends SimpleReferenceMappingImpl {
  @Override
  public boolean isRead() {
    return (super.isRead() || super.isReadAndWrite());
  }

  @Override
  public boolean isWrite() {
    return (super.isWrite() || super.isReadAndWrite());
  }
}
