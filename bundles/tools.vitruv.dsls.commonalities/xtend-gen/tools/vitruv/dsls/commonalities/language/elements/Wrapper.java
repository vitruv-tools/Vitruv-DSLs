package tools.vitruv.dsls.commonalities.language.elements;

@SuppressWarnings("all")
public interface Wrapper<WrappedType extends Object> {
  WrappedType getWrapped();
}
