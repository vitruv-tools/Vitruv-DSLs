package tools.vitruv.dsls.commonalities.language;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.WellKnownClassifiers;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.language.impl.CommonalityAttributeImpl;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;

@SuppressWarnings("all")
class CommonalityAttributeI extends CommonalityAttributeImpl {
  @Override
  public ClassLike basicGetClassLikeContainer() {
    return CommonalitiesLanguageModelExtensions.<Commonality>getOptionalDirectEContainer(this, Commonality.class);
  }

  @Override
  public boolean isMultiValued() {
    final Function1<CommonalityAttributeMapping, Boolean> _function = (CommonalityAttributeMapping it) -> {
      return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isMultiValuedRead(it));
    };
    return IterableExtensions.<CommonalityAttributeMapping>exists(this.getMappings(), _function);
  }

  @Override
  public Classifier getType() {
    boolean _isEmpty = this.getMappings().isEmpty();
    if (_isEmpty) {
      return WellKnownClassifiers.JAVA_OBJECT;
    }
    Classifier requiredType = WellKnownClassifiers.LEAST_SPECIFIC_TYPE;
    Classifier providedType = WellKnownClassifiers.MOST_SPECIFIC_TYPE;
    EList<CommonalityAttributeMapping> _mappings = this.getMappings();
    for (final CommonalityAttributeMapping mapping : _mappings) {
      {
        Classifier _elvis = null;
        Classifier _requiredType = CommonalitiesLanguageModelExtensions.getRequiredType(mapping);
        if (_requiredType != null) {
          _elvis = _requiredType;
        } else {
          _elvis = WellKnownClassifiers.LEAST_SPECIFIC_TYPE;
        }
        final Classifier mappingRequiredType = _elvis;
        Classifier _elvis_1 = null;
        Classifier _providedType = CommonalitiesLanguageModelExtensions.getProvidedType(mapping);
        if (_providedType != null) {
          _elvis_1 = _providedType;
        } else {
          _elvis_1 = WellKnownClassifiers.MOST_SPECIFIC_TYPE;
        }
        final Classifier mappingProvidedType = _elvis_1;
        XtendAssertHelper.assertTrue(mappingRequiredType.isSuperTypeOf(mappingProvidedType));
        boolean _isSuperTypeOf = requiredType.isSuperTypeOf(mappingRequiredType);
        if (_isSuperTypeOf) {
          requiredType = mappingRequiredType;
        } else {
          boolean _isSuperTypeOf_1 = mappingRequiredType.isSuperTypeOf(requiredType);
          boolean _not = (!_isSuperTypeOf_1);
          if (_not) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Incompatible mappings for commonality attribute ");
            _builder.append(this);
            throw new RuntimeException(_builder.toString());
          }
        }
        boolean _isSuperTypeOf_2 = mappingProvidedType.isSuperTypeOf(providedType);
        if (_isSuperTypeOf_2) {
          providedType = mappingProvidedType;
        } else {
          boolean _isSuperTypeOf_3 = providedType.isSuperTypeOf(mappingProvidedType);
          boolean _not_1 = (!_isSuperTypeOf_3);
          if (_not_1) {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("Incompatible mappings for commonality attribute ");
            _builder_1.append(this);
            throw new RuntimeException(_builder_1.toString());
          }
        }
      }
    }
    boolean _isSuperTypeOf = requiredType.isSuperTypeOf(providedType);
    boolean _not = (!_isSuperTypeOf);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Incompatible mappings for commonality attribute ");
      _builder.append(this);
      throw new RuntimeException(_builder.toString());
    }
    if ((providedType == WellKnownClassifiers.MOST_SPECIFIC_TYPE)) {
      XtendAssertHelper.assertTrue((requiredType != WellKnownClassifiers.LEAST_SPECIFIC_TYPE));
      return requiredType;
    } else {
      return providedType;
    }
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    ClassLike _classLikeContainer = this.getClassLikeContainer();
    _builder.append(_classLikeContainer);
    _builder.append(".");
    _builder.append(this.name);
    return _builder.toString();
  }
}
