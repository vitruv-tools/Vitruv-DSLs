package tools.vitruv.dsls.reactions.codegen.changetyperepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;

import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*;
import java.util.List;

/**
 * This class is responsible for representing the relevant change information for generating reactions
 * code for changes. 
 * The information for the changes are extracted by the {@link ChangeTypeRepresentationExtractor} from 
 * a {@link tools.vitruv.dsls.reactions.language.toplevelelements.Trigger} of the reactions language.
 */
public class ChangeTypeRepresentation {
  public static final String TEMPORARY_TYPED_CHANGE_NAME = "_localTypedChange";
  private static final Map<String, String> primitveToWrapperTypesMap = new HashMap<>();
  static {
    primitveToWrapperTypesMap.put("short", "Short");
    primitveToWrapperTypesMap.put("int", "Integer");
    primitveToWrapperTypesMap.put("long", "Long");
    primitveToWrapperTypesMap.put("double", "Double");
    primitveToWrapperTypesMap.put("float", "Float");
    primitveToWrapperTypesMap.put("boolean", "Boolean");
    primitveToWrapperTypesMap.put("char", "Char");
    primitveToWrapperTypesMap.put("byte", "Byte");
    primitveToWrapperTypesMap.put("void", "Void");
  }


  private static String mapToNonPrimitiveType(String potentiallyPrimitiveTypeCName) {
    return primitveToWrapperTypesMap.getOrDefault(
        potentiallyPrimitiveTypeCName,
        potentiallyPrimitiveTypeCName);
  }

  private final Class<?> changeType;
  private final String affectedElementClassCanonicalName;
  private final String affectedValueClassCanonicalName;
  private final boolean hasOldValue;
  private final boolean hasNewValue;
  private final boolean hasIndex;
  private final EStructuralFeature affectedFeature;
  private final String name;

  public String getName() {
    return name;
  }

  private final List<String> explicitGenericTypeParameters;

  protected ChangeTypeRepresentation(String name, Class<?> changeType, String affectedElementClassCanonicalName, String affectedValueClassCanonicalName, boolean hasOldValue,
                                     boolean hasNewValue, EStructuralFeature affectedFeature, boolean hasIndex) {
    this(
        name,
        changeType,
        affectedElementClassCanonicalName,
        affectedValueClassCanonicalName,
        hasOldValue,
        hasNewValue,
        affectedFeature,
        hasIndex,
        null
    );
  }

  protected ChangeTypeRepresentation(String name, Class<?> changeType, String affectedElementClassCanonicalName, String affectedValueClassCanonicalName, boolean hasOldValue,
                                     boolean hasNewValue, EStructuralFeature affectedFeature, boolean hasIndex, List<String> explicitGenericTypeParameters) {
    this.name = name;
    this.changeType = changeType;
    this.affectedElementClassCanonicalName = mapToNonPrimitiveType(affectedElementClassCanonicalName);
    this.affectedValueClassCanonicalName = mapToNonPrimitiveType(affectedValueClassCanonicalName);
    this.affectedFeature = affectedFeature;
    this.hasOldValue = hasOldValue;
    this.hasNewValue = hasNewValue;
    this.hasIndex = hasIndex;
    this.explicitGenericTypeParameters = explicitGenericTypeParameters;
  }

  public String getAffectedElementClass() {
    return affectedElementClassCanonicalName;
  }

  public String getAffectedValueClass() {
    return affectedValueClassCanonicalName;
  }

  public boolean hasAffectedElement() {
    return getAffectedElementClass() != null;
  }

  public boolean hasAffectedFeature() {
    return affectedFeature != null;
  }

  public List<String> getGenericTypeParameters() {
    if (explicitGenericTypeParameters != null) {
      return explicitGenericTypeParameters;
    }
    return Stream.of(affectedElementClassCanonicalName, affectedValueClassCanonicalName)
        .filter(Objects::nonNull)
        .toList();
  }

  public StringConcatenationClient getSetFieldCode(String parameterName) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append("this.");
        target.append(name);
        target.append(" = ");
        target.append("(");
        target.append(getTypedChangeTypeRepresentation());
        target.append(") ");
        target.append(parameterName);
        target.append(";");
      }
    };
  }

  public AccessibleElement getAccessibleElement() {
    return new AccessibleElement(name, changeType.getName(), getGenericTypeParameters().toArray(new String[]{}));
  }

  public StringConcatenationClient generateCheckMethodBody(String parameterName) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append("if (!("+parameterName+" instanceof ");
        target.append(getTypedChangeTypeRepresentationWithWildcards());
        target.append(")) {");
        target.newLine();
        target.append("\t return false;");
        target.newLine();
        target.append("}");

        target.newLine();
        target.newLine();
        target.append(getTypedChangeTypeRepresentation());
        target.append(" " + TEMPORARY_TYPED_CHANGE_NAME + " = (");
        target.append(getTypedChangeTypeRepresentation());
        target.append(") " +parameterName  +";");
        target.newLine();
        target.append(generateElementChecks(TEMPORARY_TYPED_CHANGE_NAME));
        target.newLine();
        target.append(getSetFieldCode(parameterName));
        target.newLine();
        target.append("return true;");
        target.newLine();
      }
    };
  }

  private StringConcatenationClient generateElementChecks(final String parameterName) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        if (ChangeTypeRepresentation.this.hasAffectedElement()) {
          target.append("if (!(");
          target.append(parameterName);
          target.append(".getAffectedElement() instanceof ");
          target.append(ChangeTypeRepresentation.this.getAffectedElementClass());
          target.append(")) {");
          target.newLineIfNotEmpty();
          target.append("\t");
          target.append("return false;");
          target.newLine();
          target.append("}");
          target.newLine();
        }

        if (ChangeTypeRepresentation.this.hasAffectedFeature()) {
          target.append("if (!");
          target.append(parameterName);
          target.append(".getAffectedFeature().getName().equals(\"");
          target.append(ChangeTypeRepresentation.this.affectedFeature.getName());
          target.append("\")) {");
          target.newLineIfNotEmpty();
          target.append("\t");
          target.append("return false;");
          target.newLine();
          target.append("}");
          target.newLine();
        }

        if (ChangeTypeRepresentation.this.hasOldValue) {
          target.append("if (");
          {
            if (ReplaceSingleValuedFeatureEChange.class.isAssignableFrom(ChangeTypeRepresentation.this.changeType)) {
              target.append(parameterName);
              target.append(".isFromNonDefaultValue() && ");
            }
          }
          target.append("!(");
          target.append(parameterName);
          target.append(".getOldValue() instanceof ");
          target.append(ChangeTypeRepresentation.this.getAffectedValueClass());
          target.append(")) {");
          target.newLineIfNotEmpty();
          target.append("\t");
          target.append("return false;");
          target.newLine();
          target.append("}");
          target.newLine();
      }

      if (ChangeTypeRepresentation.this.hasNewValue) {
        target.append("if (");
        {
          if (ReplaceSingleValuedFeatureEChange.class.isAssignableFrom(ChangeTypeRepresentation.this.changeType)) {
            target.append(parameterName);
            target.append(".isToNonDefaultValue() && ");
          }
        }
        target.append("!(");
        target.append(parameterName);
        target.append(".getNewValue() instanceof ");
        target.append(ChangeTypeRepresentation.this.getAffectedValueClass());
        target.append(")) {");
        target.newLineIfNotEmpty();
        target.append("\t");
        target.append("return false;");
        target.newLine();
        target.append("}");
        target.newLine();
      }

      }
    };
  }

  public List <AccessibleElement> generatePropertiesParameterList() {
    List<AccessibleElement> result = new ArrayList<>();
    result.add(new AccessibleElement(name, changeType));
    if (getAffectedElementClass() != null) {
      result.add(new AccessibleElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE, getAffectedElementClass()));
    }
    if (affectedFeature != null) {
      result.add(new AccessibleElement(CHANGE_AFFECTED_FEATURE_ATTRIBUTE, affectedFeature.eClass().getInstanceClass()));
    }
    if (hasOldValue) {
      result.add(new AccessibleElement(CHANGE_OLD_VALUE_ATTRIBUTE, getAffectedValueClass()));
    }
    if (hasNewValue) {
      result.add(new AccessibleElement(CHANGE_NEW_VALUE_ATTRIBUTE, getAffectedValueClass()));
    }
    if (hasIndex) {
      result.add(new AccessibleElement(CHANGE_INDEX_ATTRIBUTE, int.class));
    }
    return result;
  }

  public StringConcatenationClient generatePropertiesAssignmentCode() {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        String affectedElementClass = ChangeTypeRepresentation.this.getAffectedElementClass();
        if (affectedElementClass != null) {
          target.append(affectedElementClass);
          target.append(" ");
          target.append(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE);
          target.append(" = (");
          target.append(affectedElementClass);
          target.append(") ");
          target.append(ChangeTypeRepresentation.this.name);
          target.append(".");
          target.append(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ACCESSOR);
          target.append(";");
          target.newLineIfNotEmpty();
        }

        if ((ChangeTypeRepresentation.this.affectedFeature != null)) {
          target.append(ChangeTypeRepresentation.this.affectedFeature.eClass().getInstanceClass());
          target.append(" ");
          target.append(ReactionsLanguageConstants.CHANGE_AFFECTED_FEATURE_ATTRIBUTE);
          target.append(" = ");
          target.append(ChangeTypeRepresentation.this.name);
          target.append(".get");
          target.append(StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_AFFECTED_FEATURE_ATTRIBUTE));
          target.append("();");
          target.newLineIfNotEmpty();
        }

        if (ChangeTypeRepresentation.this.hasOldValue) {
          String affectedValueClass = ChangeTypeRepresentation.this.getAffectedValueClass();
          target.append(affectedValueClass);
          target.append(" ");
          target.append(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE);
          target.append(" = (");
          target.append(affectedValueClass);
          target.append(") ");
          target.append(ChangeTypeRepresentation.this.name);
          target.append(".get");
          target.append(StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE));
          target.append("();");
          target.newLineIfNotEmpty();
        }

        if (ChangeTypeRepresentation.this.hasNewValue) {
          String affectedValueClass = ChangeTypeRepresentation.this.getAffectedValueClass();
          target.append(affectedValueClass);
          target.append(" ");
          target.append(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE);
          target.append(" = (");
          target.append(affectedValueClass);
          target.append(") ");
          target.append(ChangeTypeRepresentation.this.name);
          target.append(".get");
          target.append(StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE));
          target.append("();");
          target.newLineIfNotEmpty();
        }

        if (ChangeTypeRepresentation.this.hasIndex) {
          target.append("int ");
          target.append(ReactionsLanguageConstants.CHANGE_INDEX_ATTRIBUTE);
          target.append(" = ");
          target.append(ChangeTypeRepresentation.this.name);
          target.append(".get");
          target.append(StringExtensions.toFirstUpper(ReactionsLanguageConstants.CHANGE_INDEX_ATTRIBUTE));
          target.append("();");
          target.newLineIfNotEmpty();
        }

      }
    };
  }

  public StringConcatenationClient getUntypedChangeTypeRepresentation() {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append(ChangeTypeRepresentation.this.changeType);
      }
    };
  }

  public StringConcatenationClient getTypedChangeTypeRepresentation() {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append(ChangeTypeRepresentation.this.changeType);

        var genericTypeParameters = getGenericTypeParameters();
        if (genericTypeParameters.isEmpty()) {
          return;
        }
        target.append("<");
        for (int i = 0; i < genericTypeParameters.size(); i++) {
          target.append(genericTypeParameters.get(i));
          if (i < genericTypeParameters.size() - 1) {
            target.append(", ");
          }
        }
        target.append(">");
      }
    };
  }

  public StringConcatenationClient getTypedChangeTypeRepresentationWithWildcards() {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append(ChangeTypeRepresentation.this.changeType);

        var genericTypeParameters = getGenericTypeParameters();
        if (genericTypeParameters.isEmpty()) {
          return;
        }
        target.append("<");
        for (int i = 0; i < genericTypeParameters.size(); i++) {
          target.append("?");
          if (i < genericTypeParameters.size() - 1) {
            target.append(", ");
          }
        }
        target.append(">");
      }
    };
  }

}
