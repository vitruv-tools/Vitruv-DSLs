package tools.vitruv.dsls.commonalities.language;

import tools.vitruv.dsls.commonalities.language.impl.SimpleAttributeMappingImpl;

@SuppressWarnings("all")
class SimpleAttributeMappingI extends SimpleAttributeMappingImpl {
  @Override
  public boolean isRead() {
    return (super.isRead() || super.isReadAndWrite());
  }

  @Override
  public boolean isWrite() {
    return (super.isWrite() || super.isReadAndWrite());
  }
}
