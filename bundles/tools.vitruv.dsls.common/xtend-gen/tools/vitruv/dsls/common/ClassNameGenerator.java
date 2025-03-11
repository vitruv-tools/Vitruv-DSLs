package tools.vitruv.dsls.common;

@SuppressWarnings("all")
public interface ClassNameGenerator {
  static final String QNAME_SEPARATOR = ".";

  default String getQualifiedName() {
    String _packageName = this.getPackageName();
    String _plus = (_packageName + ClassNameGenerator.QNAME_SEPARATOR);
    String _simpleName = this.getSimpleName();
    return (_plus + _simpleName);
  }

  String getSimpleName();

  String getPackageName();

  static GenericClassNameGenerator fromQualifiedName(final String qualifiedName) {
    GenericClassNameGenerator _xblockexpression = null;
    {
      final int lastIndex = qualifiedName.lastIndexOf(ClassNameGenerator.QNAME_SEPARATOR);
      GenericClassNameGenerator _xifexpression = null;
      if ((lastIndex == (-1))) {
        return new GenericClassNameGenerator("", qualifiedName);
      } else {
        String _substring = qualifiedName.substring(0, lastIndex);
        int _length = ClassNameGenerator.QNAME_SEPARATOR.length();
        int _plus = (lastIndex + _length);
        String _substring_1 = qualifiedName.substring(_plus);
        _xifexpression = new GenericClassNameGenerator(_substring, _substring_1);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
}
