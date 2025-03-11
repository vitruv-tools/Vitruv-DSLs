package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.OperatorAttributeMappingImpl;

@SuppressWarnings("all")
class OperatorAttributeMappingI extends OperatorAttributeMappingImpl {
  @Override
  public boolean isRead() {
    return (super.isRead() || super.isReadAndWrite());
  }

  @Override
  public boolean isWrite() {
    return (super.isWrite() || super.isReadAndWrite());
  }
}
