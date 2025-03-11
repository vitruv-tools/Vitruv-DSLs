package tools.vitruv.dsls.commonalities.names;

import com.google.inject.Singleton;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;

@Singleton
@SuppressWarnings("all")
public class CommonalitiesLanguageQualifiedNameConverter extends IQualifiedNameConverter.DefaultImpl {
  private static final int NOT_FOUND = (-1);

  /**
   * In order to retain backwards compatibility with java-like qualified
   * names and still be able to identify the domain portion of qualified
   * names, we insert a separator segment into qualified names that include a
   * domain name.
   * 
   * 'a:b.c' -> #['a', ':', 'b', 'c']
   * 'a:b' -> #['a', ':', 'b']
   * 'b.c' -> #['b', 'c']
   * 'a' -> #['a']
   * If 'a' is a domain name, then the qualified name is #['a', ':']
   * 'a.b.c.d' -> #['a', 'b', 'c', 'd'] (java-like)
   */
  @Override
  public QualifiedName toQualifiedName(final String qualifiedNameAsText) {
    boolean _startsWith = qualifiedNameAsText.startsWith("http://");
    if (_startsWith) {
      return QualifiedName.create(qualifiedNameAsText);
    }
    final int domainSeparatorIndex = qualifiedNameAsText.indexOf(QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR);
    if ((domainSeparatorIndex == CommonalitiesLanguageQualifiedNameConverter.NOT_FOUND)) {
      return super.toQualifiedName(qualifiedNameAsText);
    }
    final String domainName = qualifiedNameAsText.substring(0, domainSeparatorIndex);
    final String classAndAttributePart = qualifiedNameAsText.substring((domainSeparatorIndex + 1));
    return QualifiedName.create(domainName, QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR_SEGMENT).append(super.toQualifiedName(classAndAttributePart));
  }

  @Override
  public String toString(final QualifiedName name) {
    final String domainName = QualifiedNameHelper.getMetamodelName(name);
    if ((domainName != null)) {
      String _string = super.toString(name.skipFirst(2));
      return ((domainName + QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR) + _string);
    } else {
      return super.toString(name);
    }
  }
}
