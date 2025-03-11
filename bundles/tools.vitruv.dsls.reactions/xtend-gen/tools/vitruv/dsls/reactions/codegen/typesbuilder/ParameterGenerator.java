package tools.vitruv.dsls.reactions.codegen.typesbuilder;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.language.inputTypes.InputTypesPackage;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;

@SuppressWarnings("all")
public class ParameterGenerator {
  private static final String MISSING_PARAMETER_NAME = "/* Name missing */";

  @Extension
  protected final JvmTypeReferenceBuilder _typeReferenceBuilder;

  @Extension
  protected final JvmTypesBuilderWithoutAssociations _typesBuilder;

  public ParameterGenerator(final JvmTypeReferenceBuilder typeReferenceBuilder, final JvmTypesBuilderWithoutAssociations typesBuilder) {
    this._typeReferenceBuilder = typeReferenceBuilder;
    this._typesBuilder = typesBuilder;
  }

  public JvmFormalParameter generateParameter(final EObject contextObject, final AccessibleElement element) {
    return this._typesBuilder.toParameter(contextObject, element.getName(), element.generateTypeRef(this._typeReferenceBuilder));
  }

  public Iterable<JvmFormalParameter> generateParameters(final EObject contextObject, final Iterable<AccessibleElement> elements) {
    final Function1<AccessibleElement, JvmFormalParameter> _function = (AccessibleElement it) -> {
      return this._typesBuilder.toParameter(contextObject, it.getName(), it.generateTypeRef(this._typeReferenceBuilder));
    };
    return IterableExtensions.<AccessibleElement, JvmFormalParameter>map(elements, _function);
  }

  public Iterable<AccessibleElement> getInputElements(final Iterable<NamedMetaclassReference> metaclassReferences, final Iterable<NamedJavaElementReference> javaElements) {
    final Function1<NamedMetaclassReference, AccessibleElement> _function = (NamedMetaclassReference it) -> {
      String _elvis = null;
      String _name = it.getName();
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = ParameterGenerator.MISSING_PARAMETER_NAME;
      }
      EClassifier _metaclass = it.getMetaclass();
      String _mappedInstanceClassCanonicalName = null;
      if (_metaclass!=null) {
        _mappedInstanceClassCanonicalName=this.getMappedInstanceClassCanonicalName(_metaclass);
      }
      return new AccessibleElement(_elvis, _mappedInstanceClassCanonicalName);
    };
    Iterable<AccessibleElement> _map = IterableExtensions.<NamedMetaclassReference, AccessibleElement>map(metaclassReferences, _function);
    final Function1<NamedJavaElementReference, AccessibleElement> _function_1 = (NamedJavaElementReference it) -> {
      String _elvis = null;
      String _name = it.getName();
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = ParameterGenerator.MISSING_PARAMETER_NAME;
      }
      JvmTypeReference _type = it.getType();
      String _qualifiedName = null;
      if (_type!=null) {
        _qualifiedName=_type.getQualifiedName();
      }
      return new AccessibleElement(_elvis, _qualifiedName);
    };
    Iterable<AccessibleElement> _map_1 = IterableExtensions.<NamedJavaElementReference, AccessibleElement>map(javaElements, _function_1);
    return Iterables.<AccessibleElement>concat(_map, _map_1);
  }

  private String _getMappedInstanceClassCanonicalName(final EClass eClass) {
    String _switchResult = null;
    boolean _matched = false;
    if (Objects.equal(eClass, InputTypesPackage.Literals.STRING)) {
      _matched=true;
      _switchResult = String.class.getName();
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.INTEGER)) {
        _matched=true;
        _switchResult = Integer.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.LONG)) {
        _matched=true;
        _switchResult = Long.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.SHORT)) {
        _matched=true;
        _switchResult = Short.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.BOOLEAN)) {
        _matched=true;
        _switchResult = Boolean.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.CHARACTER)) {
        _matched=true;
        _switchResult = Character.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.BYTE)) {
        _matched=true;
        _switchResult = Byte.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.FLOAT)) {
        _matched=true;
        _switchResult = Float.class.getName();
      }
    }
    if (!_matched) {
      if (Objects.equal(eClass, InputTypesPackage.Literals.DOUBLE)) {
        _matched=true;
        _switchResult = Double.class.getName();
      }
    }
    if (!_matched) {
      _switchResult = ReactionsLanguageHelper.getJavaClassName(eClass);
    }
    return _switchResult;
  }

  private String _getMappedInstanceClassCanonicalName(final EEnum eEnum) {
    return ReactionsLanguageHelper.getJavaClassName(eEnum);
  }

  private String getMappedInstanceClassCanonicalName(final EClassifier eEnum) {
    if (eEnum instanceof EEnum) {
      return _getMappedInstanceClassCanonicalName((EEnum)eEnum);
    } else if (eEnum instanceof EClass) {
      return _getMappedInstanceClassCanonicalName((EClass)eEnum);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(eEnum).toString());
    }
  }
}
