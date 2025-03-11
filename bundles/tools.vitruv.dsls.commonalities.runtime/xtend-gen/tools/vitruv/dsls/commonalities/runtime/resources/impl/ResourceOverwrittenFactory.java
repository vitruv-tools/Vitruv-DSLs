package tools.vitruv.dsls.commonalities.runtime.resources.impl;

import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;

@SuppressWarnings("all")
public class ResourceOverwrittenFactory extends ResourcesFactoryImpl {
  @Override
  public IntermediateResourceBridge createIntermediateResourceBridge() {
    return new IntermediateResourceBridgeI();
  }
}
