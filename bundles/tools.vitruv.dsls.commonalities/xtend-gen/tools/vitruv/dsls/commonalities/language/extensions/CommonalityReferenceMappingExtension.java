package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.SimpleReferenceMapping;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;

@Utility
@SuppressWarnings("all")
final class CommonalityReferenceMappingExtension {
  public static boolean isSimpleMapping(final CommonalityReferenceMapping mapping) {
    return (mapping instanceof SimpleReferenceMapping);
  }

  public static boolean isOperatorMapping(final CommonalityReferenceMapping mapping) {
    return (mapping instanceof OperatorReferenceMapping);
  }

  protected static boolean _isMultiValued(final SimpleReferenceMapping mapping) {
    return mapping.getReference().isMultiValued();
  }

  protected static boolean _isMultiValued(final OperatorReferenceMapping mapping) {
    return ReferenceMappingExtension.isMultiValued(mapping);
  }

  protected static ParticipationClass _getParticipationClass(final SimpleReferenceMapping mapping) {
    return mapping.getReference().getParticipationClass();
  }

  protected static ParticipationClass _getParticipationClass(final OperatorReferenceMapping mapping) {
    return mapping.getParticipationClass();
  }

  public static Participation getParticipation(final CommonalityReferenceMapping mapping) {
    return ParticipationClassExtension.getParticipation(CommonalityReferenceMappingExtension.getParticipationClass(mapping));
  }

  public static CommonalityReference getDeclaringReference(final CommonalityReferenceMapping mapping) {
    return CommonalitiesLanguageElementExtension.<CommonalityReference>getDirectEContainer(mapping, CommonalityReference.class);
  }

  public static Commonality getReferencedCommonality(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getDeclaringReference(mapping).getReferenceType();
  }

  public static Participation getReferencedParticipation(final CommonalityReferenceMapping mapping) {
    final String domainName = CommonalityReferenceMappingExtension.getParticipation(mapping).getDomainName();
    final Commonality referencedCommonality = CommonalityReferenceMappingExtension.getReferencedCommonality(mapping);
    final Function1<Participation, Boolean> _function = (Participation it) -> {
      String _domainName = it.getDomainName();
      return Boolean.valueOf(Objects.equal(_domainName, domainName));
    };
    final Participation participation = IterableExtensions.<Participation>findFirst(referencedCommonality.getParticipations(), _function);
    if ((participation == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find referenced participation \'");
      _builder.append(domainName);
      _builder.append("\' in commonality \'");
      _builder.append(referencedCommonality);
      _builder.append("\' for mapping of reference \'");
      CommonalityReference _declaringReference = CommonalityReferenceMappingExtension.getDeclaringReference(mapping);
      _builder.append(_declaringReference);
      _builder.append("\'. ");
      throw new RuntimeException(_builder.toString());
    }
    return participation;
  }

  protected static boolean _isAssignmentCompatible(final SimpleReferenceMapping mapping, final ParticipationClass referencedClass) {
    final Classifier referenceType = mapping.getReference().getType();
    return referenceType.isSuperTypeOf(referencedClass.getSuperMetaclass());
  }

  protected static boolean _isAssignmentCompatible(final OperatorReferenceMapping mapping, final ParticipationClass referencedClass) {
    return true;
  }

  public static boolean isMultiValued(final CommonalityReferenceMapping mapping) {
    if (mapping instanceof OperatorReferenceMapping) {
      return _isMultiValued((OperatorReferenceMapping)mapping);
    } else if (mapping instanceof SimpleReferenceMapping) {
      return _isMultiValued((SimpleReferenceMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static ParticipationClass getParticipationClass(final CommonalityReferenceMapping mapping) {
    if (mapping instanceof OperatorReferenceMapping) {
      return _getParticipationClass((OperatorReferenceMapping)mapping);
    } else if (mapping instanceof SimpleReferenceMapping) {
      return _getParticipationClass((SimpleReferenceMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static boolean isAssignmentCompatible(final CommonalityReferenceMapping mapping, final ParticipationClass referencedClass) {
    if (mapping instanceof OperatorReferenceMapping) {
      return _isAssignmentCompatible((OperatorReferenceMapping)mapping, referencedClass);
    } else if (mapping instanceof SimpleReferenceMapping) {
      return _isAssignmentCompatible((SimpleReferenceMapping)mapping, referencedClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping, referencedClass).toString());
    }
  }

  private CommonalityReferenceMappingExtension() {
    
  }
}
