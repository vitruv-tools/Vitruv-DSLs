package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.StaticDelegate;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.lib.Inline;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.BidirectionalParticipationCondition;
import tools.vitruv.dsls.commonalities.language.CheckedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.EnforcedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.LiteralOperand;
import tools.vitruv.dsls.commonalities.language.Operand;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationClassOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.language.ReferenceMappingOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.SimpleAttributeMapping;
import tools.vitruv.dsls.commonalities.language.SimpleReferenceMapping;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.participation.Containment;

@StaticDelegate({ AttributeMappingExtension.class, AttributeMappingOperandExtension.class, CommonalityExtension.class, CommonalitiesLanguageElementExtension.class, CommonalityAttributeExtension.class, CommonalityAttributeMappingExtension.class, CommonalityReferenceExtension.class, CommonalityReferenceMappingExtension.class, OperandExtension.class, OperatorAttributeMappingExtension.class, OperatorReferenceMappingExtension.class, ParticipationExtension.class, ParticipationPartExtension.class, ParticipationClassExtension.class, ParticipationRelationExtension.class, ParticipationConditionExtension.class, ParticipationConditionOperandExtension.class, ReferenceMappingOperandExtension.class, ReferenceMappingExtension.class })
@Utility
@SuppressWarnings("all")
public final class CommonalitiesLanguageModelExtensions {
  public static ParticipationRelation getOptionalParticipationRelation(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getOptionalParticipationRelation(participationClass);
  }

  public static Domain getDomain(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getDomain(participationClass);
  }

  public static Participation getParticipation(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getParticipation(participationClass);
  }

  public static Commonality getParticipatingCommonality(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getParticipatingCommonality(participationClass);
  }

  /**
   * Returns the participation class that contains the given participation
   * class according to the specified containment relationships.
   * <p>
   * Returns <code>null</code> if no container class is found.
   */
  public static ParticipationClass getDeclaredContainerClass(final ParticipationClass contained) {
    return ParticipationClassExtension.getDeclaredContainerClass(contained);
  }

  /**
   * Gets the root participation class that (transitively) contains the given
   * participation class.
   * <p>
   * Returns the given participation class itself if it has no container
   * class.
   */
  public static ParticipationClass getRootDeclaredContainerClass(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getRootDeclaredContainerClass(participationClass);
  }

  /**
   * Gets all participation classes along the chain of container classes of the given participation class.
   * <p>
   * This includes the direct and transitive container classes.
   * <p>
   * Empty if the given participation class has no container class.
   */
  public static Iterable<ParticipationClass> getTransitiveContainerClasses(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getTransitiveContainerClasses(participationClass);
  }

  /**
   * Gets the participation classes that are (directly) contained by the given participation class.
   * <p>
   * Empty if there are no contained classes.
   */
  public static Iterable<ParticipationClass> getContainedClasses(final ParticipationClass container) {
    return ParticipationClassExtension.getContainedClasses(container);
  }

  /**
   * Starting at the given class, this finds the leaf participation classes
   * that are (transitively) contained by the given class and don't contain
   * any other participation classes themselves.
   * <p>
   * This returns the given participation class itself if it contains no
   * other participation classes.
   */
  public static Iterable<ParticipationClass> getLeafClasses(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getLeafClasses(participationClass);
  }

  /**
   * Gets all participation classes that are directly and transitively contained by the given participation class.
   * <p>
   * Empty if the given participation class contains no other classes.
   */
  public static Iterable<ParticipationClass> getTransitiveContainedClasses(final ParticipationClass participationClass) {
    return ParticipationClassExtension.getTransitiveContainedClasses(participationClass);
  }

  public static boolean isForResource(final ParticipationClass participationClass) {
    return ParticipationClassExtension.isForResource(participationClass);
  }

  public static boolean isInSingletonRoot(final ParticipationClass participationClass) {
    return ParticipationClassExtension.isInSingletonRoot(participationClass);
  }

  @Inline(value = "$2.getDeclaringCommonality($1)", imported = CommonalityAttributeExtension.class)
  public static Commonality getDeclaringCommonality(final CommonalityAttribute attribute) {
    return CommonalityAttributeExtension.getDeclaringCommonality(attribute);
  }

  public static boolean isInReferenceMappingContext(final ReferenceMappingOperand operand) {
    return ReferenceMappingOperandExtension.isInReferenceMappingContext(operand);
  }

  public static OperatorReferenceMapping getReferenceMapping(final ReferenceMappingOperand operand) {
    return ReferenceMappingOperandExtension.getReferenceMapping(operand);
  }

  public static String getParticipationConditionOperatorName(final JvmDeclaredType operatorType) {
    return ParticipationConditionExtension.getParticipationConditionOperatorName(operatorType);
  }

  public static String getName(final ParticipationCondition condition) {
    return ParticipationConditionExtension.getName(condition);
  }

  public static boolean isContainment(final ParticipationCondition condition) {
    return ParticipationConditionExtension.isContainment(condition);
  }

  public static Participation getParticipation(final ParticipationCondition participationCondition) {
    return ParticipationConditionExtension.getParticipation(participationCondition);
  }

  protected static boolean _isEnforced(final BidirectionalParticipationCondition condition) {
    return ParticipationConditionExtension._isEnforced(condition);
  }

  protected static boolean _isEnforced(final EnforcedParticipationCondition condition) {
    return ParticipationConditionExtension._isEnforced(condition);
  }

  protected static boolean _isEnforced(final CheckedParticipationCondition condition) {
    return ParticipationConditionExtension._isEnforced(condition);
  }

  protected static boolean _isChecked(final BidirectionalParticipationCondition condition) {
    return ParticipationConditionExtension._isChecked(condition);
  }

  protected static boolean _isChecked(final EnforcedParticipationCondition condition) {
    return ParticipationConditionExtension._isChecked(condition);
  }

  protected static boolean _isChecked(final CheckedParticipationCondition condition) {
    return ParticipationConditionExtension._isChecked(condition);
  }

  public static Containment getContainment(final ParticipationCondition condition) {
    return ParticipationConditionExtension.getContainment(condition);
  }

  public static boolean isEnforced(final ParticipationCondition condition) {
    return ParticipationConditionExtension.isEnforced(condition);
  }

  public static boolean isChecked(final ParticipationCondition condition) {
    return ParticipationConditionExtension.isChecked(condition);
  }

  public static Participation getParticipation(final Operand operand) {
    return OperandExtension.getParticipation(operand);
  }

  protected static ParticipationClass _getParticipationClass(final ParticipationClassOperand operand) {
    return OperandExtension._getParticipationClass(operand);
  }

  protected static ParticipationClass _getParticipationClass(final ParticipationAttributeOperand operand) {
    return OperandExtension._getParticipationClass(operand);
  }

  protected static ParticipationClass _getParticipationClass(final ReferencedParticipationAttributeOperand operand) {
    return OperandExtension._getParticipationClass(operand);
  }

  protected static ParticipationClass _getParticipationClass(final Operand operand) {
    return OperandExtension._getParticipationClass(operand);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final ParticipationAttributeOperand operand) {
    return OperandExtension._getParticipationAttribute(operand);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final ReferencedParticipationAttributeOperand operand) {
    return OperandExtension._getParticipationAttribute(operand);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final Operand operand) {
    return OperandExtension._getParticipationAttribute(operand);
  }

  public static ParticipationClass getParticipationClass(final Operand operand) {
    return OperandExtension.getParticipationClass(operand);
  }

  public static ParticipationAttribute getParticipationAttribute(final Operand operand) {
    return OperandExtension.getParticipationAttribute(operand);
  }

  public static ParticipationAttributeOperand getParticipationAttributeOperand(final OperatorAttributeMapping mapping) {
    return OperatorAttributeMappingExtension.getParticipationAttributeOperand(mapping);
  }

  public static Iterable<ParticipationClass> getParticipationClassOperands(final OperatorAttributeMapping mapping) {
    return OperatorAttributeMappingExtension.getParticipationClassOperands(mapping);
  }

  /**
   * Gets all operands that are common for both application directions (from participation to commonality and from
   * commonality to participation).
   * <p>
   * I.e. this omits the participation attribute operand (if it is present).
   */
  public static Iterable<AttributeMappingOperand> getCommonOperands(final OperatorAttributeMapping mapping) {
    return OperatorAttributeMappingExtension.getCommonOperands(mapping);
  }

  public static Concept getConcept(final Commonality commonality) {
    return CommonalityExtension.getConcept(commonality);
  }

  public static Iterable<ParticipationAttribute> getReferencedParticipationAttributes(final OperatorReferenceMapping mapping) {
    return OperatorReferenceMappingExtension.getReferencedParticipationAttributes(mapping);
  }

  public static Set<ParticipationClass> getReferencedParticipationClasses(final OperatorReferenceMapping mapping) {
    return OperatorReferenceMappingExtension.getReferencedParticipationClasses(mapping);
  }

  public static Iterable<LiteralOperand> getPassedOperands(final OperatorReferenceMapping mapping) {
    return OperatorReferenceMappingExtension.getPassedOperands(mapping);
  }

  public static boolean isSimpleMapping(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.isSimpleMapping(mapping);
  }

  public static boolean isOperatorMapping(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.isOperatorMapping(mapping);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipationAttribute(mapping);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipationAttribute(mapping);
  }

  protected static Collection<ParticipationClass> _getInvolvedParticipationClasses(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getInvolvedParticipationClasses(mapping);
  }

  protected static Collection<ParticipationClass> _getInvolvedParticipationClasses(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getInvolvedParticipationClasses(mapping);
  }

  protected static Participation _getParticipation(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipation(mapping);
  }

  protected static Participation _getParticipation(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipation(mapping);
  }

  public static CommonalityAttribute getDeclaringAttribute(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getDeclaringAttribute(mapping);
  }

  /**
   * Returns <code>true<code> if the commonality side of the mapping is
   * multi-valued.
   */
  protected static boolean _isMultiValuedRead(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._isMultiValuedRead(mapping);
  }

  protected static boolean _isMultiValuedRead(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._isMultiValuedRead(mapping);
  }

  /**
   * Returns <code>true<code> if the participation side of the mapping is
   * multi-valued.
   */
  protected static boolean _isMultiValuedWrite(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._isMultiValuedWrite(mapping);
  }

  protected static boolean _isMultiValuedWrite(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._isMultiValuedWrite(mapping);
  }

  protected static Classifier _getCommonalityAttributeType(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getCommonalityAttributeType(mapping);
  }

  protected static Classifier _getCommonalityAttributeType(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getCommonalityAttributeType(mapping);
  }

  protected static Classifier _getParticipationAttributeType(final SimpleAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipationAttributeType(mapping);
  }

  protected static Classifier _getParticipationAttributeType(final OperatorAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension._getParticipationAttributeType(mapping);
  }

  public static Classifier getProvidedType(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getProvidedType(mapping);
  }

  public static Classifier getRequiredType(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getRequiredType(mapping);
  }

  public static ParticipationAttribute getParticipationAttribute(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getParticipationAttribute(mapping);
  }

  public static Collection<ParticipationClass> getInvolvedParticipationClasses(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getInvolvedParticipationClasses(mapping);
  }

  public static Participation getParticipation(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getParticipation(mapping);
  }

  public static boolean isMultiValuedRead(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.isMultiValuedRead(mapping);
  }

  public static boolean isMultiValuedWrite(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.isMultiValuedWrite(mapping);
  }

  public static Classifier getCommonalityAttributeType(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getCommonalityAttributeType(mapping);
  }

  public static Classifier getParticipationAttributeType(final CommonalityAttributeMapping mapping) {
    return CommonalityAttributeMappingExtension.getParticipationAttributeType(mapping);
  }

  public static String getAttributeMappingOperatorName(final JvmDeclaredType operatorType) {
    return AttributeMappingExtension.getAttributeMappingOperatorName(operatorType);
  }

  public static String getName(final OperatorAttributeMapping mapping) {
    return AttributeMappingExtension.getName(mapping);
  }

  public static AttributeTypeDescription getCommonalityAttributeTypeDescription(final OperatorAttributeMapping mapping) {
    return AttributeMappingExtension.getCommonalityAttributeTypeDescription(mapping);
  }

  public static AttributeTypeDescription getParticipationAttributeTypeDescription(final OperatorAttributeMapping mapping) {
    return AttributeMappingExtension.getParticipationAttributeTypeDescription(mapping);
  }

  public static Commonality getDeclaringCommonality(final Participation participation) {
    return ParticipationExtension.getDeclaringCommonality(participation);
  }

  public static Iterable<ParticipationClass> getAllClasses(final Participation participation) {
    return ParticipationExtension.getAllClasses(participation);
  }

  public static Domain getDomain(final Participation participation) {
    return ParticipationExtension.getDomain(participation);
  }

  public static boolean isCommonalityParticipation(final Participation participation) {
    return ParticipationExtension.isCommonalityParticipation(participation);
  }

  public static Concept getParticipationConcept(final Participation participation) {
    return ParticipationExtension.getParticipationConcept(participation);
  }

  public static Iterable<ParticipationRelation> getAllRelations(final Participation participation) {
    return ParticipationExtension.getAllRelations(participation);
  }

  public static Iterable<ParticipationRelation> getAllContainmentRelations(final Participation participation) {
    return ParticipationExtension.getAllContainmentRelations(participation);
  }

  public static Iterable<ParticipationRelation> getAllNonContainmentRelations(final Participation participation) {
    return ParticipationExtension.getAllNonContainmentRelations(participation);
  }

  public static Iterable<ParticipationCondition> getAllContainmentConditions(final Participation participation) {
    return ParticipationExtension.getAllContainmentConditions(participation);
  }

  public static Iterable<ParticipationCondition> getAllNonContainmentConditions(final Participation participation) {
    return ParticipationExtension.getAllNonContainmentConditions(participation);
  }

  public static Iterable<Containment> getContainments(final Participation participation) {
    return ParticipationExtension.getContainments(participation);
  }

  public static Set<ParticipationClass> getRootContainerClasses(final Participation participation) {
    return ParticipationExtension.getRootContainerClasses(participation);
  }

  public static boolean hasResourceClass(final Participation participation) {
    return ParticipationExtension.hasResourceClass(participation);
  }

  public static ParticipationClass getResourceClass(final Participation participation) {
    return ParticipationExtension.getResourceClass(participation);
  }

  public static Iterable<Containment> getResourceContainments(final Participation participation) {
    return ParticipationExtension.getResourceContainments(participation);
  }

  public static boolean hasSingletonClass(final Participation participation) {
    return ParticipationExtension.hasSingletonClass(participation);
  }

  public static ParticipationClass getSingletonClass(final Participation participation) {
    return ParticipationExtension.getSingletonClass(participation);
  }

  /**
   * A class marked as singleton and its containers also act as root of the
   * participation. This returns these classes.
   */
  public static Iterable<ParticipationClass> getSingletonRootClasses(final Participation participation) {
    return ParticipationExtension.getSingletonRootClasses(participation);
  }

  public static boolean isInAttributeMappingContext(final AttributeMappingOperand operand) {
    return AttributeMappingOperandExtension.isInAttributeMappingContext(operand);
  }

  public static OperatorAttributeMapping getAttributeMapping(final AttributeMappingOperand operand) {
    return AttributeMappingOperandExtension.getAttributeMapping(operand);
  }

  /**
   * @return the container of the given type
   * @throws RuntimeException if no container of the given type is found
   */
  public static <T extends Object> T getEContainer(final EObject object, final Class<T> containerType) {
    return CommonalitiesLanguageElementExtension.getEContainer(object, containerType);
  }

  /**
   * @return the container of the given type, or <code>null</code> if no such container is found
   */
  public static <T extends Object> T getOptionalEContainer(final EObject object, final Class<T> containerType) {
    return CommonalitiesLanguageElementExtension.getOptionalEContainer(object, containerType);
  }

  public static boolean hasEContainer(final EObject object, final Class<? extends EObject> containerType) {
    return CommonalitiesLanguageElementExtension.hasEContainer(object, containerType);
  }

  public static <T extends Object> T getDirectEContainer(final EObject object, final Class<T> containerType) {
    return CommonalitiesLanguageElementExtension.getDirectEContainer(object, containerType);
  }

  public static <T extends Object> T getOptionalDirectEContainer(final EObject object, final Class<T> containerType) {
    return CommonalitiesLanguageElementExtension.getOptionalDirectEContainer(object, containerType);
  }

  public static boolean hasDirectEContainer(final EObject object, final Class<? extends EObject> containerType) {
    return CommonalitiesLanguageElementExtension.hasDirectEContainer(object, containerType);
  }

  public static CommonalityFile getContainedCommonalityFile(final Resource resource) {
    return CommonalitiesLanguageElementExtension.getContainedCommonalityFile(resource);
  }

  public static CommonalityFile getOptionalContainedCommonalityFile(final Resource resource) {
    return CommonalitiesLanguageElementExtension.getOptionalContainedCommonalityFile(resource);
  }

  public static boolean isContainment(final ParticipationRelation relation) {
    return ParticipationRelationExtension.isContainment(relation);
  }

  public static Iterable<Containment> getContainments(final ParticipationRelation relation) {
    return ParticipationRelationExtension.getContainments(relation);
  }

  public static String getParticipationRelationOperatorName(final JvmDeclaredType operatorType) {
    return ParticipationRelationExtension.getParticipationRelationOperatorName(operatorType);
  }

  public static String getOperatorName(final ParticipationRelation relation) {
    return ParticipationRelationExtension.getOperatorName(relation);
  }

  public static boolean isSimpleMapping(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.isSimpleMapping(mapping);
  }

  public static boolean isOperatorMapping(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.isOperatorMapping(mapping);
  }

  protected static boolean _isMultiValued(final SimpleReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension._isMultiValued(mapping);
  }

  protected static boolean _isMultiValued(final OperatorReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension._isMultiValued(mapping);
  }

  protected static ParticipationClass _getParticipationClass(final SimpleReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension._getParticipationClass(mapping);
  }

  protected static ParticipationClass _getParticipationClass(final OperatorReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension._getParticipationClass(mapping);
  }

  public static Participation getParticipation(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getParticipation(mapping);
  }

  public static CommonalityReference getDeclaringReference(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getDeclaringReference(mapping);
  }

  public static Commonality getReferencedCommonality(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getReferencedCommonality(mapping);
  }

  public static Participation getReferencedParticipation(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getReferencedParticipation(mapping);
  }

  protected static boolean _isAssignmentCompatible(final SimpleReferenceMapping mapping, final ParticipationClass referencedClass) {
    return CommonalityReferenceMappingExtension._isAssignmentCompatible(mapping, referencedClass);
  }

  protected static boolean _isAssignmentCompatible(final OperatorReferenceMapping mapping, final ParticipationClass referencedClass) {
    return CommonalityReferenceMappingExtension._isAssignmentCompatible(mapping, referencedClass);
  }

  public static boolean isMultiValued(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.isMultiValued(mapping);
  }

  public static ParticipationClass getParticipationClass(final CommonalityReferenceMapping mapping) {
    return CommonalityReferenceMappingExtension.getParticipationClass(mapping);
  }

  public static boolean isAssignmentCompatible(final CommonalityReferenceMapping mapping, final ParticipationClass referencedClass) {
    return CommonalityReferenceMappingExtension.isAssignmentCompatible(mapping, referencedClass);
  }

  public static boolean isInParticipationConditionContext(final ParticipationConditionOperand operand) {
    return ParticipationConditionOperandExtension.isInParticipationConditionContext(operand);
  }

  public static ParticipationCondition getParticipationCondition(final ParticipationConditionOperand operand) {
    return ParticipationConditionOperandExtension.getParticipationCondition(operand);
  }

  public static Participation getParticipation(final ParticipationConditionOperand operand) {
    return ParticipationConditionOperandExtension.getParticipation(operand);
  }

  public static Participation getDeclaringParticipation(final ParticipationPart participationPart) {
    return ParticipationPartExtension.getDeclaringParticipation(participationPart);
  }

  public static Commonality getDeclaringCommonality(final ParticipationPart participationPart) {
    return ParticipationPartExtension.getDeclaringCommonality(participationPart);
  }

  protected static Iterable<ParticipationClass> _getAllParticipationClasses(final ParticipationClass pClass) {
    return ParticipationPartExtension._getAllParticipationClasses(pClass);
  }

  protected static Iterable<ParticipationClass> _getAllParticipationClasses(final ParticipationRelation relation) {
    return ParticipationPartExtension._getAllParticipationClasses(relation);
  }

  protected static Iterable<ParticipationRelation> _getAllParticipationRelations(final ParticipationClass pClass) {
    return ParticipationPartExtension._getAllParticipationRelations(pClass);
  }

  protected static Iterable<ParticipationRelation> _getAllParticipationRelations(final ParticipationRelation relation) {
    return ParticipationPartExtension._getAllParticipationRelations(relation);
  }

  protected static ParticipationClass _getDeclaredContainerClass(final ParticipationClass pClass) {
    return ParticipationPartExtension._getDeclaredContainerClass(pClass);
  }

  protected static ParticipationClass _getDeclaredContainerClass(final ParticipationRelation relation) {
    return ParticipationPartExtension._getDeclaredContainerClass(relation);
  }

  public static Iterable<ParticipationClass> getAllParticipationClasses(final ParticipationPart pClass) {
    return ParticipationPartExtension.getAllParticipationClasses(pClass);
  }

  public static Iterable<ParticipationRelation> getAllParticipationRelations(final ParticipationPart pClass) {
    return ParticipationPartExtension.getAllParticipationRelations(pClass);
  }

  public static ParticipationClass getDeclaredContainerClass(final ParticipationPart pClass) {
    return ParticipationPartExtension.getDeclaredContainerClass(pClass);
  }

  public static String getReferenceMappingOperatorName(final JvmDeclaredType operatorType) {
    return ReferenceMappingExtension.getReferenceMappingOperatorName(operatorType);
  }

  public static String getName(final OperatorReferenceMapping mapping) {
    return ReferenceMappingExtension.getName(mapping);
  }

  public static boolean isMultiValued(final OperatorReferenceMapping mapping) {
    return ReferenceMappingExtension.isMultiValued(mapping);
  }

  public static boolean isAttributeReference(final OperatorReferenceMapping mapping) {
    return ReferenceMappingExtension.isAttributeReference(mapping);
  }

  public static List<CommonalityReferenceMapping> getMappings(final CommonalityReference reference, final String domainName) {
    return CommonalityReferenceExtension.getMappings(reference, domainName);
  }

  public static Commonality getDeclaringCommonality(final CommonalityReference reference) {
    return CommonalityReferenceExtension.getDeclaringCommonality(reference);
  }

  private CommonalitiesLanguageModelExtensions() {
    
  }
}
