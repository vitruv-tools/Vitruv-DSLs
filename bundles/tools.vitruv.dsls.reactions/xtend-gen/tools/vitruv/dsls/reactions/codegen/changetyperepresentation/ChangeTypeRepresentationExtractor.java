package tools.vitruv.dsls.reactions.codegen.changetyperepresentation;

import java.util.Arrays;
import java.util.Collections;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.EobjectPackage;
import tools.vitruv.change.atomic.feature.attribute.AttributePackage;
import tools.vitruv.change.atomic.feature.reference.ReferencePackage;
import tools.vitruv.change.atomic.root.RootPackage;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.language.ElementChangeType;
import tools.vitruv.dsls.reactions.language.ElementCreationChangeType;
import tools.vitruv.dsls.reactions.language.ElementDeletionChangeType;
import tools.vitruv.dsls.reactions.language.ElementExistenceChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionAsRootChangeType;
import tools.vitruv.dsls.reactions.language.ElementInsertionInListChangeType;
import tools.vitruv.dsls.reactions.language.ElementReferenceChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalAsRootChangeType;
import tools.vitruv.dsls.reactions.language.ElementRemovalFromListChangeType;
import tools.vitruv.dsls.reactions.language.ElementReplacementChangeType;
import tools.vitruv.dsls.reactions.language.ElementRootChangeType;
import tools.vitruv.dsls.reactions.language.ModelAttributeChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeInsertedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeRemovedChange;
import tools.vitruv.dsls.reactions.language.ModelAttributeReplacedChange;
import tools.vitruv.dsls.reactions.language.ModelElementChange;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;

@SuppressWarnings("all")
public final class ChangeTypeRepresentationExtractor {
  private static final String CREATE_CHANGE_NAME = "createChange";

  private static final String DELET_CHANGE_NAME = "deleteChange";

  private static final String INSERT_CHANGE_NAME = "insertChange";

  private static final String REMOVE_CHANGE_NAME = "removeChange";

  private static final String REPLACE_CHANGE_NAME = "replaceChange";

  private static final String GENERAL_CHANGE_NAME = "change";

  protected static ChangeTypeRepresentation _extractChangeType(final Trigger trigger) {
    final ChangeTypeRepresentation atomicChange = new ChangeTypeRepresentation(ChangeTypeRepresentationExtractor.GENERAL_CHANGE_NAME, EChange.class, null, null, false, false, null, false);
    return atomicChange;
  }

  protected static ChangeTypeRepresentation _extractChangeType(final ModelAttributeChange modelAttributeChange) {
    boolean hasOldValue = false;
    boolean hasNewValue = false;
    boolean hasIndex = false;
    EClass clazz = null;
    String name = "";
    boolean _matched = false;
    if (modelAttributeChange instanceof ModelAttributeInsertedChange) {
      _matched=true;
      clazz = AttributePackage.Literals.INSERT_EATTRIBUTE_VALUE;
      hasNewValue = true;
      hasIndex = true;
      name = ChangeTypeRepresentationExtractor.INSERT_CHANGE_NAME;
    }
    if (!_matched) {
      if (modelAttributeChange instanceof ModelAttributeRemovedChange) {
        _matched=true;
        clazz = AttributePackage.Literals.REMOVE_EATTRIBUTE_VALUE;
        hasOldValue = true;
        hasIndex = true;
        name = ChangeTypeRepresentationExtractor.REMOVE_CHANGE_NAME;
      }
    }
    if (!_matched) {
      if (modelAttributeChange instanceof ModelAttributeReplacedChange) {
        _matched=true;
        clazz = AttributePackage.Literals.REPLACE_SINGLE_VALUED_EATTRIBUTE;
        hasOldValue = true;
        hasNewValue = true;
        name = ChangeTypeRepresentationExtractor.REPLACE_CHANGE_NAME;
      }
    }
    final String affectedEObject = ReactionsLanguageHelper.getJavaClassName(modelAttributeChange.getFeature().getMetaclass());
    final String affectedValue = ReactionsLanguageHelper.getJavaClassName(modelAttributeChange.getFeature().getFeature().getEType());
    final EAttribute affectedFeature = modelAttributeChange.getFeature().getFeature();
    Class<?> _instanceClass = clazz.getInstanceClass();
    final ChangeTypeRepresentation atomicChange = new ChangeTypeRepresentation(name, _instanceClass, affectedEObject, affectedValue, hasOldValue, hasNewValue, affectedFeature, hasIndex);
    return atomicChange;
  }

  protected static ChangeTypeRepresentation _extractChangeType(final ModelElementChange modelElementChange) {
    ElementChangeType _changeType = null;
    if (modelElementChange!=null) {
      _changeType=modelElementChange.getChangeType();
    }
    boolean _tripleEquals = (_changeType == null);
    if (_tripleEquals) {
      return new ChangeTypeRepresentation(ChangeTypeRepresentationExtractor.GENERAL_CHANGE_NAME, EChange.class, null, null, false, false, null, false);
    } else {
      MetaclassReference _elementType = modelElementChange.getElementType();
      EClassifier _metaclass = null;
      if (_elementType!=null) {
        _metaclass=_elementType.getMetaclass();
      }
      return ChangeTypeRepresentationExtractor.generateChangeTypeRepresentation(modelElementChange.getChangeType(), ((EClass) _metaclass));
    }
  }

  private static ChangeTypeRepresentation _generateChangeTypeRepresentation(final ElementRootChangeType modelElementChange, final EClass elementClass) {
    EClass clazz = null;
    boolean hasNewValue = false;
    String name = "";
    boolean _matched = false;
    if (modelElementChange instanceof ElementInsertionAsRootChangeType) {
      _matched=true;
      clazz = RootPackage.Literals.INSERT_ROOT_EOBJECT;
      hasNewValue = true;
      name = ChangeTypeRepresentationExtractor.INSERT_CHANGE_NAME;
    }
    if (!_matched) {
      if (modelElementChange instanceof ElementRemovalAsRootChangeType) {
        _matched=true;
        clazz = RootPackage.Literals.REMOVE_ROOT_EOBJECT;
        name = ChangeTypeRepresentationExtractor.REMOVE_CHANGE_NAME;
      }
    }
    final Object affectedEObject = null;
    String _xifexpression = null;
    if ((elementClass != null)) {
      _xifexpression = ReactionsLanguageHelper.getJavaClassName(elementClass);
    } else {
      _xifexpression = EObject.class.getCanonicalName();
    }
    final String affectedValue = _xifexpression;
    Class<?> _instanceClass = clazz.getInstanceClass();
    return new ChangeTypeRepresentation(name, _instanceClass, ((String)affectedEObject), affectedValue, (!hasNewValue), hasNewValue, null, true);
  }

  private static ChangeTypeRepresentation _generateChangeTypeRepresentation(final ElementReferenceChangeType modelElementChange, final EClass elementClass) {
    boolean hasOldValue = false;
    boolean hasNewValue = false;
    boolean hasIndex = false;
    EClass clazz = null;
    String name = "";
    boolean _matched = false;
    if (modelElementChange instanceof ElementInsertionInListChangeType) {
      _matched=true;
      clazz = ReferencePackage.Literals.INSERT_EREFERENCE;
      hasNewValue = true;
      hasIndex = true;
      name = ChangeTypeRepresentationExtractor.INSERT_CHANGE_NAME;
    }
    if (!_matched) {
      if (modelElementChange instanceof ElementRemovalFromListChangeType) {
        _matched=true;
        clazz = ReferencePackage.Literals.REMOVE_EREFERENCE;
        hasOldValue = true;
        hasIndex = true;
        name = ChangeTypeRepresentationExtractor.REMOVE_CHANGE_NAME;
      }
    }
    if (!_matched) {
      if (modelElementChange instanceof ElementReplacementChangeType) {
        _matched=true;
        clazz = ReferencePackage.Literals.REPLACE_SINGLE_VALUED_EREFERENCE;
        hasOldValue = true;
        hasNewValue = true;
        name = ChangeTypeRepresentationExtractor.REPLACE_CHANGE_NAME;
      }
    }
    MetaclassEReferenceReference _feature = modelElementChange.getFeature();
    EClassifier _metaclass = null;
    if (_feature!=null) {
      _metaclass=_feature.getMetaclass();
    }
    String _javaClassName = null;
    if (_metaclass!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(_metaclass);
    }
    final String affectedEObject = _javaClassName;
    String _xifexpression = null;
    if ((elementClass != null)) {
      _xifexpression = ReactionsLanguageHelper.getJavaClassName(elementClass);
    } else {
      MetaclassEReferenceReference _feature_1 = modelElementChange.getFeature();
      EReference _feature_2 = null;
      if (_feature_1!=null) {
        _feature_2=_feature_1.getFeature();
      }
      EClassifier _eType = null;
      if (_feature_2!=null) {
        _eType=_feature_2.getEType();
      }
      String _javaClassName_1 = null;
      if (_eType!=null) {
        _javaClassName_1=ReactionsLanguageHelper.getJavaClassName(_eType);
      }
      _xifexpression = _javaClassName_1;
    }
    final String affectedValue = _xifexpression;
    MetaclassEReferenceReference _feature_3 = modelElementChange.getFeature();
    EReference _feature_4 = null;
    if (_feature_3!=null) {
      _feature_4=_feature_3.getFeature();
    }
    final EReference affectedFeature = _feature_4;
    Class<?> _instanceClass = clazz.getInstanceClass();
    String _canonicalName = EObject.class.getCanonicalName();
    return new ChangeTypeRepresentation(name, _instanceClass, affectedEObject, affectedValue, hasOldValue, hasNewValue, affectedFeature, hasIndex, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_canonicalName)));
  }

  private static ChangeTypeRepresentation _generateChangeTypeRepresentation(final ElementExistenceChangeType modelElementChange, final EClass elementClass) {
    EClass clazz = null;
    String name = "";
    boolean _matched = false;
    if (modelElementChange instanceof ElementCreationChangeType) {
      _matched=true;
      clazz = EobjectPackage.Literals.CREATE_EOBJECT;
      name = ChangeTypeRepresentationExtractor.CREATE_CHANGE_NAME;
    }
    if (!_matched) {
      if (modelElementChange instanceof ElementDeletionChangeType) {
        _matched=true;
        clazz = EobjectPackage.Literals.DELETE_EOBJECT;
        name = ChangeTypeRepresentationExtractor.DELET_CHANGE_NAME;
      }
    }
    String _xifexpression = null;
    if ((elementClass != null)) {
      _xifexpression = ReactionsLanguageHelper.getJavaClassName(elementClass);
    } else {
      _xifexpression = ReactionsLanguageHelper.getJavaClassName(EcorePackage.eINSTANCE.getEObject());
    }
    final String affectedEObject = _xifexpression;
    final Object affectedValue = null;
    Class<?> _instanceClass = clazz.getInstanceClass();
    return new ChangeTypeRepresentation(name, _instanceClass, affectedEObject, ((String)affectedValue), false, false, null, false);
  }

  public static ChangeTypeRepresentation extractChangeType(final Trigger modelAttributeChange) {
    if (modelAttributeChange instanceof ModelAttributeChange) {
      return _extractChangeType((ModelAttributeChange)modelAttributeChange);
    } else if (modelAttributeChange instanceof ModelElementChange) {
      return _extractChangeType((ModelElementChange)modelAttributeChange);
    } else if (modelAttributeChange != null) {
      return _extractChangeType(modelAttributeChange);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(modelAttributeChange).toString());
    }
  }

  private static ChangeTypeRepresentation generateChangeTypeRepresentation(final EObject modelElementChange, final EClass elementClass) {
    if (modelElementChange instanceof ElementExistenceChangeType) {
      return _generateChangeTypeRepresentation((ElementExistenceChangeType)modelElementChange, elementClass);
    } else if (modelElementChange instanceof ElementReferenceChangeType) {
      return _generateChangeTypeRepresentation((ElementReferenceChangeType)modelElementChange, elementClass);
    } else if (modelElementChange instanceof ElementRootChangeType) {
      return _generateChangeTypeRepresentation((ElementRootChangeType)modelElementChange, elementClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(modelElementChange, elementClass).toString());
    }
  }
}
