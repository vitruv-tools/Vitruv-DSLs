package tools.vitruv.dsls.reactions.codegen.typesbuilder;

import org.eclipse.xtext.xbase.jvmmodel.JvmAnnotationReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Extension;

@SuppressWarnings("all")
public class TypesBuilderExtensionProvider {
  @Extension
  protected JvmTypesBuilderWithoutAssociations _typesBuilder;

  @Extension
  protected JvmTypeReferenceBuilder _typeReferenceBuilder;

  @Extension
  protected JvmAnnotationReferenceBuilder _annotationTypesBuilder;

  @Extension
  protected ParameterGenerator _parameterGenerator;

  public void setBuilders(final JvmTypesBuilderWithoutAssociations typesBuilder, final JvmTypeReferenceBuilder typeReferenceBuilder, final JvmAnnotationReferenceBuilder annotationReferenceBuilder) {
    this._typesBuilder = typesBuilder;
    this._typeReferenceBuilder = typeReferenceBuilder;
    this._annotationTypesBuilder = annotationReferenceBuilder;
    ParameterGenerator _parameterGenerator = new ParameterGenerator(typeReferenceBuilder, typesBuilder);
    this._parameterGenerator = _parameterGenerator;
  }

  public ParameterGenerator copyBuildersTo(final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    ParameterGenerator _xblockexpression = null;
    {
      typesBuilderExtensionProvider._typesBuilder = this._typesBuilder;
      typesBuilderExtensionProvider._typeReferenceBuilder = this._typeReferenceBuilder;
      typesBuilderExtensionProvider._annotationTypesBuilder = this._annotationTypesBuilder;
      _xblockexpression = typesBuilderExtensionProvider._parameterGenerator = this._parameterGenerator;
    }
    return _xblockexpression;
  }
}
