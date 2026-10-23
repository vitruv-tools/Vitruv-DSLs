package tools.vitruv.dsls.reactions.codegen.changetyperepresentation;

import java.util.List;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.reference.ReferencePackage;
import tools.vitruv.change.atomic.root.RootPackage;
import tools.vitruv.dsls.reactions.language.ModelElementChange;
import tools.vitruv.change.atomic.eobject.EobjectPackage;
import tools.vitruv.dsls.reactions.language.ModelAttributeChange;
import tools.vitruv.change.atomic.feature.attribute.AttributePackage;
import tools.vitruv.dsls.reactions.language.ModelAttributeRemovedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeReplacedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeInsertedChange;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.reactions.language.ElementCreationChangeType;
import tools.vitruv.dsls.reactions.language.ElementDeletionChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionAsRootChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalAsRootChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionInListChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalFromListChangeType;
import tools.vitruv.dsls.reactions.language.ElementReplacementChangeType;
import tools.vitruv.dsls.reactions.language.ElementRootChangeType;
import tools.vitruv.dsls.reactions.language.ElementExistenceChangeType;
import tools.vitruv.dsls.reactions.language.ElementReferenceChangeType;
import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.getJavaClassName;
import org.eclipse.emf.ecore.EcorePackage;

public final class ChangeTypeRepresentationExtractor {
  public static final String CREATE_CHANGE_NAME = "createChange";
  public static final String DELETE_CHANGE_NAME = "deleteChange";
  public static final String INSERT_CHANGE_NAME = "insertChange";
  public static final String REMOVE_CHANGE_NAME = "removeChange";
  public static final String REPLACE_CHANGE_NAME = "replaceChange";
  public static final String GENERAL_CHANGE_NAME = "change";

  public static ChangeTypeRepresentation extractChangeType(Trigger trigger) {
    if (trigger instanceof ModelElementChange elementChange) {
      return extractChangeType(elementChange);
    }
    if (trigger instanceof ModelAttributeChange attributeChange) {
      return extractChangeType(attributeChange);
    }
    return new ChangeTypeRepresentation(
        GENERAL_CHANGE_NAME, EChange.class, null, null, false, false, null, false);
  }

  public static ChangeTypeRepresentation extractChangeType(ModelAttributeChange modelAttributeChange) {
    boolean hasOldValue = false;
    boolean hasNewValue = false;
    boolean hasIndex = false;
    EClass clazz = null;
    var name = "";

    if (modelAttributeChange instanceof ModelAttributeInsertedChange) {
      clazz = AttributePackage.Literals.INSERT_EATTRIBUTE_VALUE;
      hasNewValue = true;
      hasIndex = true;
      name = INSERT_CHANGE_NAME;
    }
    else if (modelAttributeChange instanceof ModelAttributeRemovedChange) {
      clazz = AttributePackage.Literals.REMOVE_EATTRIBUTE_VALUE;
      hasOldValue = true;
      hasIndex = true;
      name = REMOVE_CHANGE_NAME;
    }
    else if (modelAttributeChange instanceof ModelAttributeReplacedChange) {
      clazz = AttributePackage.Literals.REPLACE_SINGLE_VALUED_EATTRIBUTE;
      hasOldValue = true;
      hasNewValue = true;
      name = REPLACE_CHANGE_NAME;
    }

    String affectedEObjectClassName = getJavaClassName(modelAttributeChange.getFeature().getMetaclass());
    EAttribute affectedFeature = modelAttributeChange.getFeature().getFeature();
    String affectedValue = getJavaClassName(affectedFeature.getEType());
    return new ChangeTypeRepresentation(name, clazz.getInstanceClass(), affectedEObjectClassName, affectedValue, hasOldValue, hasNewValue, affectedFeature, hasIndex);
  }

  public static ChangeTypeRepresentation extractChangeType(ModelElementChange modelElementChange) {
    if (modelElementChange == null || modelElementChange.getChangeType() == null) {
      return new ChangeTypeRepresentation(GENERAL_CHANGE_NAME, EChange.class, null, null, false, false, null, false);
    } else {
      EClass eClass = modelElementChange.getElementType() != null ? (EClass) modelElementChange.getElementType().getMetaclass() : null;
      return generateChangeTypeRepresentation(modelElementChange, eClass);
    }
  }

  private static ChangeTypeRepresentation
      generateChangeTypeRepresentation(ModelElementChange elementChange, EClass elementClass) {
    var changeType = elementChange.getChangeType();
    if (changeType instanceof ElementRootChangeType rootChangeType) {
      return generateRootChangeTypeRepresentation(rootChangeType, elementClass);
    }
    if (changeType instanceof ElementReferenceChangeType referenceChangeType) {
      return generateReferenceChangeTypeRepresentation(referenceChangeType, elementClass);
    }
    if (changeType instanceof ElementExistenceChangeType existenceChangeType) {
      return generateExistenceChangeTypeRepresentation(existenceChangeType, elementClass);
    }
    throw new IllegalArgumentException("Cannot generate change type representation for "+changeType);
  }

  private static ChangeTypeRepresentation generateRootChangeTypeRepresentation(ElementRootChangeType modelElementChange, EClass elementClass) {
    EClass clazz = null;
    var hasNewValue = false;
    var name = "";
    if (modelElementChange instanceof ElementInsertionAsRootChangeType) {
      clazz = RootPackage.Literals.INSERT_ROOT_EOBJECT;
      hasNewValue = true;
      name = INSERT_CHANGE_NAME;
    } else if (modelElementChange instanceof ElementRemovalAsRootChangeType) {
      clazz = RootPackage.Literals.REMOVE_ROOT_EOBJECT;
      name = REMOVE_CHANGE_NAME;
    }
    String affectedValueClassName =
        (elementClass != null)
          ? getJavaClassName(elementClass)
          : EObject.class.getCanonicalName();

    return new ChangeTypeRepresentation(name, clazz.getInstanceClass(), null, affectedValueClassName, !hasNewValue, hasNewValue, null, true);
  }

  private static ChangeTypeRepresentation generateReferenceChangeTypeRepresentation(ElementReferenceChangeType modelElementChange, EClass elementClass) {
    var hasOldValue = false;
    var hasNewValue = false;
    var hasIndex = false;
    EClass clazz = null;
    var name = "";

    if (modelElementChange instanceof ElementInsertionInListChangeType) {
      clazz = ReferencePackage.Literals.INSERT_EREFERENCE;
      hasNewValue = true;
      hasIndex = true;
      name = INSERT_CHANGE_NAME;
    } else if (modelElementChange instanceof ElementRemovalFromListChangeType) {
      clazz = ReferencePackage.Literals.REMOVE_EREFERENCE;
      hasOldValue = true;
      hasIndex = true;
      name = REMOVE_CHANGE_NAME;
    } else if (modelElementChange instanceof ElementReplacementChangeType) {
      clazz = ReferencePackage.Literals.REPLACE_SINGLE_VALUED_EREFERENCE;
      hasOldValue = true;
      hasNewValue = true;
      name = REPLACE_CHANGE_NAME;
    }

    MetaclassEReferenceReference feature = modelElementChange.getFeature();
    String affectedEObjectName = null;
    if (feature != null && feature.getMetaclass() != null) {
      affectedEObjectName = getJavaClassName(feature.getMetaclass());
    }
    EReference affectedFeature = null;
    if (feature != null && feature.getFeature() != null) {
      affectedFeature = feature.getFeature();
    }
    String affectedValueClassName = null;
    if (elementClass != null) {
      affectedValueClassName = getJavaClassName(elementClass);
    } else {
      if (affectedFeature != null && affectedFeature.getEType() != null) {
        affectedValueClassName = getJavaClassName(affectedFeature.getEType());
      }
    }

    return new ChangeTypeRepresentation(
        name,
        clazz.getInstanceClass(),
        affectedEObjectName,
        affectedValueClassName,
        hasOldValue,
        hasNewValue,
        affectedFeature,
        hasIndex,
        List.of(EObject.class.getCanonicalName())
    );
  }

  private static ChangeTypeRepresentation generateExistenceChangeTypeRepresentation(ElementExistenceChangeType modelElementChange, EClass elementClass) {
    EClass clazz = null;
    var name = "";
    if (modelElementChange instanceof ElementCreationChangeType) {
      clazz = EobjectPackage.Literals.CREATE_EOBJECT;
      name = CREATE_CHANGE_NAME;
    } else if (modelElementChange instanceof ElementDeletionChangeType) {
      clazz = EobjectPackage.Literals.DELETE_EOBJECT;
      name = DELETE_CHANGE_NAME;
    }
    String affectedEObjectClassName =
        (elementClass != null)
            ? getJavaClassName(elementClass)
            : getJavaClassName(EcorePackage.eINSTANCE.getEObject());
    return new ChangeTypeRepresentation(
        name,
        clazz.getInstanceClass(),
        affectedEObjectClassName,
        null,
        false,
        false,
        null,
        false
    );
  }
}
