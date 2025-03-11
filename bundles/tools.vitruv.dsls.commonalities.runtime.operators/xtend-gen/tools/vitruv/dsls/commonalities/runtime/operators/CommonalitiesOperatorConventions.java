package tools.vitruv.dsls.commonalities.runtime.operators;

import com.google.common.base.Preconditions;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Map;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@Utility
@SuppressWarnings("all")
public final class CommonalitiesOperatorConventions {
  private static final String OPERATOR_TYPES_PACKAGE_NAME = "cl_operators__";

  private static final Map<String, String> TYPE_NAME_TO_LANGUAGE_IDENTIFIER = CollectionLiterals.<String, String>newImmutableMap(
    Pair.<String, String>of("equals_", "="), 
    Pair.<String, String>of("in_", "in"), 
    Pair.<String, String>of("plusEquals_", "+="), 
    Pair.<String, String>of("minusEquals_", "-="), 
    Pair.<String, String>of("timesEquals_", "*="), 
    Pair.<String, String>of("dividedByEquals_", "/="), 
    Pair.<String, String>of("modEquals_", "%="), 
    Pair.<String, String>of("greaterThanOrEqual_", ">="), 
    Pair.<String, String>of("lessThanOrEqual_", "<="), 
    Pair.<String, String>of("logicalOr_", "||"), 
    Pair.<String, String>of("logicalAnd_", "&&"), 
    Pair.<String, String>of("doubleEquals_", "=="), 
    Pair.<String, String>of("equalsNot_", "!="), 
    Pair.<String, String>of("tripleEquals_", "==="), 
    Pair.<String, String>of("notDoubleEquals_", "!=="), 
    Pair.<String, String>of("implies_", "=>"), 
    Pair.<String, String>of("leftRight_", "<>"), 
    Pair.<String, String>of("elvis_", "?:"), 
    Pair.<String, String>of("doubleTimes_", "**"), 
    Pair.<String, String>of("doubleMinus_", "--"), 
    Pair.<String, String>of("doublePlus_", "++"), 
    Pair.<String, String>of("doubleColon_", "::"), 
    Pair.<String, String>of("not_", "!"), 
    Pair.<String, String>of("mod_", "%"), 
    Pair.<String, String>of("and_", "&"), 
    Pair.<String, String>of("times_", "*"), 
    Pair.<String, String>of("plus_", "+"), 
    Pair.<String, String>of("minus_", "-"), 
    Pair.<String, String>of("dividedBy_", "/"), 
    Pair.<String, String>of("lessThan_", "<"), 
    Pair.<String, String>of("greaterThan_", ">"), 
    Pair.<String, String>of("circumflex_", "^"), 
    Pair.<String, String>of("tilde_", "~"), 
    Pair.<String, String>of("colon_", ":"), 
    Pair.<String, String>of("questionmark_", "?"));

  private static final Map<String, String> LANGUAGE_IDENTIFIER_TO_TYPE_NAME = CollectionLiterals.<String, String>newImmutableMap(((Pair<? extends String, ? extends String>[])Conversions.unwrapArray(IterableExtensions.<Map.Entry<String, String>, Pair<String, String>>map(CommonalitiesOperatorConventions.TYPE_NAME_TO_LANGUAGE_IDENTIFIER.entrySet(), ((Function1<Map.Entry<String, String>, Pair<String, String>>) (Map.Entry<String, String> it) -> {
    String _value = it.getValue();
    String _key = it.getKey();
    return Pair.<String, String>of(_value, _key);
  })), Pair.class)));

  public static String toOperatorLanguageName(final String typeName) {
    String _xifexpression = null;
    if ((typeName == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = CommonalitiesOperatorConventions.TYPE_NAME_TO_LANGUAGE_IDENTIFIER.getOrDefault(typeName, typeName);
    }
    return _xifexpression;
  }

  public static String toOperatorTypeName(final String languageName) {
    String _xifexpression = null;
    if ((languageName == null)) {
      _xifexpression = null;
    } else {
      _xifexpression = CommonalitiesOperatorConventions.LANGUAGE_IDENTIFIER_TO_TYPE_NAME.getOrDefault(languageName, languageName);
    }
    return _xifexpression;
  }

  public static String toOperatorTypeQualifiedName(final String languageQualifiedName) {
    String _xifexpression = null;
    if ((languageQualifiedName == null)) {
      _xifexpression = null;
    } else {
      String _xblockexpression = null;
      {
        final int lastDot = languageQualifiedName.lastIndexOf(".");
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("‹");
        _builder.append(languageQualifiedName);
        _builder.append("› is not a fully qualified name!");
        Preconditions.checkArgument((lastDot != (-1)), _builder);
        _xblockexpression = CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(
          languageQualifiedName.substring(0, lastDot), 
          languageQualifiedName.substring((lastDot + 1)));
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }

  public static String toOperatorTypeQualifiedName(final String languagePackage, final String languageName) {
    String _operatorTypeName = CommonalitiesOperatorConventions.toOperatorTypeName(languageName);
    return ((((languagePackage + ".") + CommonalitiesOperatorConventions.OPERATOR_TYPES_PACKAGE_NAME) + ".") + _operatorTypeName);
  }

  public static boolean isPotentialOperator(final String typeQualifiedName) {
    return typeQualifiedName.contains(CommonalitiesOperatorConventions.OPERATOR_TYPES_PACKAGE_NAME);
  }

  public static String toOperatorLanguageQualifiedName(final String typeQualifiedName) {
    String _xifexpression = null;
    if ((typeQualifiedName == null)) {
      _xifexpression = null;
    } else {
      String _xblockexpression = null;
      {
        final int lastDot = typeQualifiedName.lastIndexOf(".");
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("‹");
        _builder.append(typeQualifiedName);
        _builder.append("› is not a fully qualified name!");
        Preconditions.checkArgument((lastDot != (-1)), _builder);
        _xblockexpression = CommonalitiesOperatorConventions.toOperatorLanguageQualifiedName(
          typeQualifiedName.substring(0, lastDot), 
          typeQualifiedName.substring((lastDot + 1)));
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }

  public static String toOperatorLanguageQualifiedName(final String typePackage, final String typeName) {
    String _xblockexpression = null;
    {
      final int packageEnd = typePackage.lastIndexOf(("." + CommonalitiesOperatorConventions.OPERATOR_TYPES_PACKAGE_NAME));
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The provided package does not contain ");
      _builder.append(CommonalitiesOperatorConventions.OPERATOR_TYPES_PACKAGE_NAME);
      _builder.append(": ‹");
      _builder.append(typePackage);
      _builder.append("›");
      Preconditions.checkArgument((packageEnd != (-1)), _builder);
      String _substring = typePackage.substring(0, (packageEnd - 1));
      String _operatorLanguageName = CommonalitiesOperatorConventions.toOperatorLanguageName(typeName);
      _xblockexpression = (_substring + _operatorLanguageName);
    }
    return _xblockexpression;
  }

  private CommonalitiesOperatorConventions() {
    
  }
}
