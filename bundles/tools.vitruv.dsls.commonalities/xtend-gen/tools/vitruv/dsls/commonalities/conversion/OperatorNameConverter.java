package tools.vitruv.dsls.commonalities.conversion;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;

/**
 * Converts operator names from their language notation (like ‘=’) to their type names
 * (like ‘equals_’). Doing this so early provides a consistent representation in the
 * AST: operators will always be represented by the expected type name. The language
 * notation can only be found in the concrete syntax.
 */
@SuppressWarnings("all")
public class OperatorNameConverter implements IValueConverter<String> {
  @Override
  public String toString(final String typeName) throws ValueConverterException {
    String _xifexpression = null;
    boolean _contains = typeName.contains(".");
    if (_contains) {
      String _xifexpression_1 = null;
      boolean _isPotentialOperator = CommonalitiesOperatorConventions.isPotentialOperator(typeName);
      if (_isPotentialOperator) {
        _xifexpression_1 = CommonalitiesOperatorConventions.toOperatorLanguageQualifiedName(typeName);
      } else {
        _xifexpression_1 = typeName;
      }
      _xifexpression = _xifexpression_1;
    } else {
      _xifexpression = CommonalitiesOperatorConventions.toOperatorLanguageName(typeName);
    }
    return _xifexpression;
  }

  @Override
  public String toValue(final String languageNotation, final INode node) throws ValueConverterException {
    String _xifexpression = null;
    boolean _contains = languageNotation.contains(".");
    if (_contains) {
      _xifexpression = CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(languageNotation);
    } else {
      _xifexpression = CommonalitiesOperatorConventions.toOperatorTypeName(languageNotation);
    }
    return _xifexpression;
  }
}
