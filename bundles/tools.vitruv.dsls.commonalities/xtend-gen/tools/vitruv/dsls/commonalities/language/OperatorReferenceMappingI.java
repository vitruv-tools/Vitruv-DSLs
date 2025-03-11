package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.OperatorReferenceMappingImpl;

@SuppressWarnings("all")
class OperatorReferenceMappingI extends OperatorReferenceMappingImpl {
  @Override
  public boolean isRead() {
    return (super.isRead() || super.isReadAndWrite());
  }

  @Override
  public boolean isWrite() {
    return (super.isWrite() || super.isReadAndWrite());
  }
}
