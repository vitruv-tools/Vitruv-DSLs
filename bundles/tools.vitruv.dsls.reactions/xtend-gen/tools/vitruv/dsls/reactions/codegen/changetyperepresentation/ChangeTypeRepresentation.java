package tools.vitruv.dsls.reactions.codegen.changetyperepresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;

/**
 * This class is responsible for representing the relevant change information for generating reactions
 * code for changes.
 * The information for the changes are extracted by the {@link ChangeTypeRepresentationExtractor} from
 * a {@link Trigger} of the reactions language.
 */
@SuppressWarnings("all")
public class ChangeTypeRepresentation {
  private static final String TEMPORARY_TYPED_CHANGE_NAME = "_localTypedChange";

  private static final HashMap<String, String> primitveToWrapperTypesMap = CollectionLiterals.<String, String>newHashMap(
    ((Pair<? extends String, ? extends String>[])Conversions.unwrapArray(ListExtensions.map(Collections.<Pair<? extends Class<?>, ? extends Class<?>>>unmodifiableList(CollectionLiterals.<Pair<? extends Class<?>, ? extends Class<?>>>newArrayList(Pair.<Class<Short>, Class<Short>>of(short.class, Short.class), Pair.<Class<Integer>, Class<Integer>>of(int.class, Integer.class), Pair.<Class<Long>, Class<Long>>of(long.class, Long.class), Pair.<Class<Double>, Class<Double>>of(double.class, Double.class), Pair.<Class<Float>, Class<Float>>of(float.class, Float.class), Pair.<Class<Boolean>, Class<Boolean>>of(boolean.class, Boolean.class), Pair.<Class<Character>, Class<Character>>of(char.class, Character.class), Pair.<Class<Byte>, Class<Byte>>of(byte.class, Byte.class), Pair.<Class<Void>, Class<Void>>of(void.class, Void.class))), ((Function1<Pair<? extends Class<?>, ? extends Class<?>>, Pair<String, String>>) (Pair<? extends Class<?>, ? extends Class<?>> it) -> {
      String _canonicalName = it.getKey().getCanonicalName();
      String _canonicalName_1 = it.getValue().getCanonicalName();
      return Pair.<String, String>of(_canonicalName, _canonicalName_1);
    })), Pair.class)));

  private static String mapToNonPrimitiveType(final String potentiallyPrimitiveTypeCName) {
    return ChangeTypeRepresentation.primitveToWrapperTypesMap.getOrDefault(potentiallyPrimitiveTypeCName, potentiallyPrimitiveTypeCName);
  }

  private final Class<?> changeType;

  private final String affectedElementClassCanonicalName;

  private final String affectedValueClassCanonicalName;

  private final boolean hasOldValue;

  private final boolean hasNewValue;

  private final boolean hasIndex;

  private final EStructuralFeature affectedFeature;

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final String name;

  private final List<String> explicitGenericTypeParameters;

  protected ChangeTypeRepresentation(final String name, final Class<?> changeType, final String affectedElementClassCanonicalName, final String affectedValueClassCanonicalName, final boolean hasOldValue, final boolean hasNewValue, final EStructuralFeature affectedFeature, final boolean hasIndex) {
    this(name, changeType, affectedElementClassCanonicalName, affectedValueClassCanonicalName, hasOldValue, hasNewValue, affectedFeature, hasIndex, null);
  }

  protected ChangeTypeRepresentation(final String name, final Class<?> changeType, final String affectedElementClassCanonicalName, final String affectedValueClassCanonicalName, final boolean hasOldValue, final boolean hasNewValue, final EStructuralFeature affectedFeature, final boolean hasIndex, final List<String> explicitGenericTypeParameters) {
    this.name = name;
    this.changeType = changeType;
    this.affectedElementClassCanonicalName = ChangeTypeRepresentation.mapToNonPrimitiveType(affectedElementClassCanonicalName);
    this.affectedValueClassCanonicalName = ChangeTypeRepresentation.mapToNonPrimitiveType(affectedValueClassCanonicalName);
    this.affectedFeature = affectedFeature;
    this.hasOldValue = hasOldValue;
    this.hasNewValue = hasNewValue;
    this.hasIndex = hasIndex;
    this.explicitGenericTypeParameters = explicitGenericTypeParameters;
  }

  public String getAffectedElementClass() {
    return this.affectedElementClassCanonicalName;
  }

  public String getAffectedValueClass() {
    return this.affectedValueClassCanonicalName;
  }

  public boolean hasAffectedElement() {
    String _affectedElementClass = this.getAffectedElementClass();
    return (_affectedElementClass != null);
  }

  public boolean hasAffectedFeature() {
    return (this.affectedFeature != null);
  }

  public Iterable<String> getGenericTypeParameters() {
    Iterable<String> _xblockexpression = null;
    {
      if ((this.explicitGenericTypeParameters != null)) {
        return this.explicitGenericTypeParameters;
      }
      _xblockexpression = IterableExtensions.<String>filterNull(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(this.affectedElementClassCanonicalName, this.affectedValueClassCanonicalName)));
    }
    return _xblockexpression;
  }

  public StringConcatenationClient getSetFieldCode(final String parameterName) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("this.");
        _builder.append(ChangeTypeRepresentation.this.name);
        _builder.append(" = (");
        StringConcatenationClient _typedChangeTypeRepresentation = ChangeTypeRepresentation.this.getTypedChangeTypeRepresentation();
        _builder.append(_typedChangeTypeRepresentation);
        _builder.append(") ");
        _builder.append(parameterName);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    };
    return _client;
  }

  public AccessibleElement getAccessibleElement() {
    String _name = this.changeType.getName();
    Iterable<String> _genericTypeParameters = this.getGenericTypeParameters();
    return new AccessibleElement(this.name, _name, ((String[])Conversions.unwrapArray(_genericTypeParameters, String.class)));
  }

  public StringConcatenationClient generateCheckMethodBody(final String parameterName) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (!(");
        _builder.append(parameterName);
        _builder.append(" instanceof ");
        StringConcatenationClient _changeTypeRepresentationWithWildcards = ChangeTypeRepresentation.this.getChangeTypeRepresentationWithWildcards();
        _builder.append(_changeTypeRepresentationWithWildcards);
        _builder.append(")) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("return false;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        StringConcatenationClient _typedChangeTypeRepresentation = ChangeTypeRepresentation.this.getTypedChangeTypeRepresentation();
        _builder.append(_typedChangeTypeRepresentation);
        _builder.append(" ");
        _builder.append(ChangeTypeRepresentation.TEMPORARY_TYPED_CHANGE_NAME);
        _builder.append(" = (");
        StringConcatenationClient _typedChangeTypeRepresentation_1 = ChangeTypeRepresentation.this.getTypedChangeTypeRepresentation();
        _builder.append(_typedChangeTypeRepresentation_1);
        _builder.append(") ");
        _builder.append(parameterName);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        StringConcatenationClient _generateElementChecks = ChangeTypeRepresentation.this.generateElementChecks(ChangeTypeRepresentation.TEMPORARY_TYPED_CHANGE_NAME);
        _builder.append(_generateElementChecks);
        _builder.newLineIfNotEmpty();
        StringConcatenationClient _setFieldCode = ChangeTypeRepresentation.this.getSetFieldCode(parameterName);
        _builder.append(_setFieldCode);
        _builder.newLineIfNotEmpty();
        _builder.append("return true;");
        _builder.newLine();
      }
    };
    return _client;
  }

  private StringConcatenationClient generateElementChecks(final String parameterName) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          boolean _hasAffectedElement = ChangeTypeRepresentation.this.hasAffectedElement();
          if (_hasAffectedElement) {
            _builder.append("if (!(");
            _builder.append(parameterName);
            _builder.append(".getAffectedElement() instanceof ");
            String _affectedElementClass = ChangeTypeRepresentation.this.getAffectedElementClass();
            _builder.append(_affectedElementClass);
            _builder.append(")) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("return false;");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
          }
        }
        {
          boolean _hasAffectedFeature = ChangeTypeRepresentation.this.hasAffectedFeature();
          if (_hasAffectedFeature) {
            _builder.append("if (!");
            _builder.append(parameterName);
            _builder.append(".getAffectedFeature().getName().equals(\"");
            String _name = ChangeTypeRepresentation.this.affectedFeature.getName();
            _builder.append(_name);
            _builder.append("\")) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("return false;");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
          }
        }
        {
          if (ChangeTypeRepresentation.this.hasOldValue) {
            _builder.append("if (");
            {
              boolean _isAssignableFrom = ReplaceSingleValuedFeatureEChange.class.isAssignableFrom(ChangeTypeRepresentation.this.changeType);
              if (_isAssignableFrom) {
                _builder.append(parameterName);
                _builder.append(".isFromNonDefaultValue() && ");
              }
            }
            _builder.append("!(");
            _builder.append(parameterName);
            _builder.append(".getOldValue() instanceof ");
            String _affectedValueClass = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass);
            _builder.append(")) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("return false;");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
          }
        }
        {
          if (ChangeTypeRepresentation.this.hasNewValue) {
            _builder.append("if (");
            {
              boolean _isAssignableFrom_1 = ReplaceSingleValuedFeatureEChange.class.isAssignableFrom(ChangeTypeRepresentation.this.changeType);
              if (_isAssignableFrom_1) {
                _builder.append(parameterName);
                _builder.append(".isToNonDefaultValue() && ");
              }
            }
            _builder.append("!(");
            _builder.append(parameterName);
            _builder.append(".getNewValue() instanceof ");
            String _affectedValueClass_1 = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass_1);
            _builder.append(")) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("return false;");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
          }
        }
      }
    };
    return _client;
  }

  public Iterable<AccessibleElement> generatePropertiesParameterList() {
    final ArrayList<AccessibleElement> result = CollectionLiterals.<AccessibleElement>newArrayList();
    AccessibleElement _accessibleElement = new AccessibleElement(this.name, this.changeType);
    result.add(_accessibleElement);
    String _affectedElementClass = this.getAffectedElementClass();
    boolean _tripleNotEquals = (_affectedElementClass != null);
    if (_tripleNotEquals) {
      String _affectedElementClass_1 = this.getAffectedElementClass();
      AccessibleElement _accessibleElement_1 = new AccessibleElement(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE, _affectedElementClass_1);
      result.add(_accessibleElement_1);
    }
    if ((this.affectedFeature != null)) {
      Class<?> _instanceClass = this.affectedFeature.eClass().getInstanceClass();
      AccessibleElement _accessibleElement_2 = new AccessibleElement(ReactionsLanguageConstants.CHANGE_AFFECTED_FEATURE_ATTRIBUTE, _instanceClass);
      result.add(_accessibleElement_2);
    }
    if (this.hasOldValue) {
      String _affectedValueClass = this.getAffectedValueClass();
      AccessibleElement _accessibleElement_3 = new AccessibleElement(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE, _affectedValueClass);
      result.add(_accessibleElement_3);
    }
    if (this.hasNewValue) {
      String _affectedValueClass_1 = this.getAffectedValueClass();
      AccessibleElement _accessibleElement_4 = new AccessibleElement(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE, _affectedValueClass_1);
      result.add(_accessibleElement_4);
    }
    if (this.hasIndex) {
      AccessibleElement _accessibleElement_5 = new AccessibleElement(ReactionsLanguageConstants.CHANGE_INDEX_ATTRIBUTE, int.class);
      result.add(_accessibleElement_5);
    }
    return result;
  }

  public StringConcatenationClient generatePropertiesAssignmentCode() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          String _affectedElementClass = ChangeTypeRepresentation.this.getAffectedElementClass();
          boolean _tripleNotEquals = (_affectedElementClass != null);
          if (_tripleNotEquals) {
            String _affectedElementClass_1 = ChangeTypeRepresentation.this.getAffectedElementClass();
            _builder.append(_affectedElementClass_1);
            _builder.append(" ");
            _builder.append(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE);
            _builder.append(" = (");
            String _affectedElementClass_2 = ChangeTypeRepresentation.this.getAffectedElementClass();
            _builder.append(_affectedElementClass_2);
            _builder.append(")");
            _builder.append(ChangeTypeRepresentation.this.name);
            _builder.append(".");
            _builder.append(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ACCESSOR);
            _builder.append(";");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          if ((ChangeTypeRepresentation.this.affectedFeature != null)) {
            Class<?> _instanceClass = ChangeTypeRepresentation.this.affectedFeature.eClass().getInstanceClass();
            _builder.append(_instanceClass);
            _builder.append(" ");
            _builder.append(ReactionsLanguageConstants.CHANGE_AFFECTED_FEATURE_ATTRIBUTE);
            _builder.append(" = ");
            _builder.append(ChangeTypeRepresentation.this.name);
            _builder.append(".get");
            String _firstUpper = StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_AFFECTED_FEATURE_ATTRIBUTE);
            _builder.append(_firstUpper);
            _builder.append("();");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          if (ChangeTypeRepresentation.this.hasOldValue) {
            String _affectedValueClass = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass);
            _builder.append(" ");
            _builder.append(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE);
            _builder.append(" = (");
            String _affectedValueClass_1 = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass_1);
            _builder.append(")");
            _builder.append(ChangeTypeRepresentation.this.name);
            _builder.append(".get");
            String _firstUpper_1 = StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE);
            _builder.append(_firstUpper_1);
            _builder.append("();");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          if (ChangeTypeRepresentation.this.hasNewValue) {
            String _affectedValueClass_2 = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass_2);
            _builder.append(" ");
            _builder.append(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE);
            _builder.append(" = (");
            String _affectedValueClass_3 = ChangeTypeRepresentation.this.getAffectedValueClass();
            _builder.append(_affectedValueClass_3);
            _builder.append(")");
            _builder.append(ChangeTypeRepresentation.this.name);
            _builder.append(".get");
            String _firstUpper_2 = StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE);
            _builder.append(_firstUpper_2);
            _builder.append("();");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          if (ChangeTypeRepresentation.this.hasIndex) {
            _builder.append("int ");
            _builder.append(ReactionsLanguageConstants.CHANGE_INDEX_ATTRIBUTE);
            _builder.append(" = ");
            _builder.append(ChangeTypeRepresentation.this.name);
            _builder.append(".get");
            String _firstUpper_3 = StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_INDEX_ATTRIBUTE);
            _builder.append(_firstUpper_3);
            _builder.append("();");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    };
    return _client;
  }

  public StringConcatenationClient getUntypedChangeTypeRepresentation() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(ChangeTypeRepresentation.this.changeType);
      }
    };
    return _client;
  }

  public StringConcatenationClient getTypedChangeTypeRepresentation() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(ChangeTypeRepresentation.this.changeType);
        {
          Iterable<String> _genericTypeParameters = ChangeTypeRepresentation.this.getGenericTypeParameters();
          boolean _hasElements = false;
          for(final String param : _genericTypeParameters) {
            if (!_hasElements) {
              _hasElements = true;
              _builder.append("<");
            } else {
              _builder.appendImmediate(", ", "");
            }
            _builder.append(param);
          }
          if (_hasElements) {
            _builder.append(">");
          }
        }
      }
    };
    return _client;
  }

  public StringConcatenationClient getChangeTypeRepresentationWithWildcards() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(ChangeTypeRepresentation.this.changeType);
        {
          Iterable<String> _genericTypeParameters = ChangeTypeRepresentation.this.getGenericTypeParameters();
          boolean _hasElements = false;
          for(final String param : _genericTypeParameters) {
            if (!_hasElements) {
              _hasElements = true;
              _builder.append("<");
            } else {
              _builder.appendImmediate(", ", "");
            }
            _builder.append("?");
          }
          if (_hasElements) {
            _builder.append(">");
          }
        }
      }
    };
    return _client;
  }

  @Pure
  public String getName() {
    return this.name;
  }
}
