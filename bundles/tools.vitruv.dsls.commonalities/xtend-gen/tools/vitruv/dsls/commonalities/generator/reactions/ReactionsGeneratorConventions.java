package tools.vitruv.dsls.commonalities.generator.reactions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;

@Utility
@SuppressWarnings("all")
public final class ReactionsGeneratorConventions {
  public static final String INTERMEDIATE = "intermediate";

  public static final String REFERENCED_INTERMEDIATE = "referencedIntermediate";

  public static final String REFERENCED_INTERMEDIATES = "referencedIntermediates";

  public static final String REFERENCING_INTERMEDIATE = "referencingIntermediate";

  public static final String REFERENCE_ROOT = "referenceRoot";

  public static final String INTERMEDIATE_ROOT = "intermediateRoot";

  public static final String START_OBJECT = "startObject";

  public static final String FOLLOW_ATTRIBUTE_REFERENCES = "followAttributeReferences";

  public static final String FOUND_MATCH_RESULT = "foundMatchResult";

  public static final String PARTICIPATION_OBJECTS = "participationObjects";

  public static final String PARTICIPATION_OBJECT = "participationObject";

  public static final String RESOURCE_BRIDGE = "resourceBridge";

  public static final String SINGLETON = "singleton";

  private static final String EXTERNAL_CLASS_PREFIX = "external_";

  public static String getName(final ParticipationContext.ContextClass contextClass) {
    final ParticipationClass participationClass = contextClass.getParticipationClass();
    boolean _isExternal = contextClass.isExternal();
    if (_isExternal) {
      String _name = participationClass.getName();
      return (ReactionsGeneratorConventions.EXTERNAL_CLASS_PREFIX + _name);
    } else {
      return participationClass.getName();
    }
  }

  public static String correspondingVariableName(final ParticipationContext.ContextClass contextClass) {
    final ParticipationClass participationClass = contextClass.getParticipationClass();
    boolean _isExternal = contextClass.isExternal();
    if (_isExternal) {
      return ReactionsGeneratorConventions.getName(contextClass);
    } else {
      return ReactionsGeneratorConventions.correspondingVariableName(participationClass);
    }
  }

  public static String correspondingVariableName(final ParticipationClass participationClass) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("corresponding_");
    String _name = participationClass.getName();
    _builder.append(_name);
    return _builder.toString();
  }

  public static String getCorrespondenceTag(final ParticipationClass participationClass) {
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participationClass);
    StringConcatenation _builder = new StringConcatenation();
    String _name = CommonalitiesLanguageModelExtensions.getConcept(commonality).getName();
    _builder.append(_name);
    _builder.append(".");
    String _name_1 = commonality.getName();
    _builder.append(_name_1);
    _builder.append("/");
    String _name_2 = CommonalitiesLanguageModelExtensions.getParticipation(participationClass).getName();
    _builder.append(_name_2);
    _builder.append(".");
    String _name_3 = participationClass.getName();
    _builder.append(_name_3);
    return _builder.toString();
  }

  public static String getResourceCorrespondenceTag(final ParticipationClass resourceClass, final ParticipationClass containedClass) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("resource");
    return _builder.toString();
  }

  public static String getReactionName(final Commonality commonality) {
    StringConcatenation _builder = new StringConcatenation();
    String _name = CommonalitiesLanguageModelExtensions.getConcept(commonality).getName();
    _builder.append(_name);
    _builder.append("_");
    String _name_1 = commonality.getName();
    _builder.append(_name_1);
    return _builder.toString();
  }

  public static String getReactionName(final Attribute attribute) {
    String _xblockexpression = null;
    {
      final ClassLike classLike = attribute.getClassLikeContainer();
      final PackageLike packageLike = classLike.getPackageLikeContainer();
      StringConcatenation _builder = new StringConcatenation();
      String _name = packageLike.getName();
      _builder.append(_name);
      _builder.append("_");
      String _shortReactionName = ReactionsGeneratorConventions.getShortReactionName(attribute);
      _builder.append(_shortReactionName);
      _xblockexpression = _builder.toString();
    }
    return _xblockexpression;
  }

  public static String getShortReactionName(final Attribute attribute) {
    String _xblockexpression = null;
    {
      final ClassLike classLike = attribute.getClassLikeContainer();
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(classLike.getName());
      _builder.append(_firstUpper);
      String _firstUpper_1 = StringExtensions.toFirstUpper(attribute.getName());
      _builder.append(_firstUpper_1);
      _xblockexpression = _builder.toString();
    }
    return _xblockexpression;
  }

  public static String getReactionName(final Participation participation) {
    StringConcatenation _builder = new StringConcatenation();
    String _name = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation).getName();
    _builder.append(_name);
    _builder.append("_");
    String _name_1 = participation.getName();
    _builder.append(_name_1);
    return _builder.toString();
  }

  public static String getReactionNameSuffix(final ParticipationContext participationContext) {
    boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
    boolean _not = (!_isForReferenceMapping);
    if (_not) {
      return "";
    }
    final Participation participation = participationContext.getParticipation();
    final CommonalityReference reference = participationContext.getDeclaringReference();
    final Commonality referencingCommonality = participationContext.getReferencingCommonality();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("_forReferenceMapping_");
    String _name = referencingCommonality.getName();
    _builder.append(_name);
    _builder.append("_");
    String _name_1 = reference.getName();
    _builder.append(_name_1);
    _builder.append("_");
    String _name_2 = participation.getName();
    _builder.append(_name_2);
    return _builder.toString();
  }

  public static String getReactionNameSuffix(final CommonalityAttribute commonalityAttribute) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("_forCommonalityAttribute_");
    String _name = commonalityAttribute.getName();
    _builder.append(_name);
    return _builder.toString();
  }

  public static String getReactionsSegmentFromCommonalityToParticipationName(final Commonality commonality, final Participation participation) {
    StringConcatenation _builder = new StringConcatenation();
    String _name = commonality.getName();
    _builder.append(_name);
    _builder.append("To");
    String _name_1 = participation.getName();
    _builder.append(_name_1);
    return _builder.toString();
  }

  public static String getReactionsSegmentFromParticipationToCommonalityName(final Commonality commonality, final Participation participation) {
    StringConcatenation _builder = new StringConcatenation();
    String _name = commonality.getName();
    _builder.append(_name);
    _builder.append("From");
    String _name_1 = participation.getName();
    _builder.append(_name_1);
    return _builder.toString();
  }

  private ReactionsGeneratorConventions() {
    
  }
}
