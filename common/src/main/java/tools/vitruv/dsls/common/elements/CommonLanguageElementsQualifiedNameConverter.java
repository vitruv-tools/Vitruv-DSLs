package tools.vitruv.dsls.common.elements;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.xbase.XbaseQualifiedNameConverter;

/** A qualified name converter that handles qualified names starting with "http://" as a single */
public class CommonLanguageElementsQualifiedNameConverter extends XbaseQualifiedNameConverter {

  @Override
  public QualifiedName toQualifiedName(String qualifiedNameAsString) {
    if (qualifiedNameAsString.startsWith("http://")) {
      return QualifiedName.create(qualifiedNameAsString);
    }
    return super.toQualifiedName(qualifiedNameAsString);
  }
}
