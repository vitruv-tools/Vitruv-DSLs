package tools.vitruv.dsls.commonalities;

import com.google.inject.Binder;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import tools.vitruv.dsls.commonalities.conversion.CommonalitiesLanguageValueConverterService;
import tools.vitruv.dsls.commonalities.export.CommonalityFileResourceDescriptionStrategy;
import tools.vitruv.dsls.commonalities.generator.CommonalitiesLanguageGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScope;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.names.CommonalitiesLanguageQualifiedNameConverter;
import tools.vitruv.dsls.commonalities.names.CommonalitiesLanguageQualifiedNameProvider;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameProviderDescriptionProvider;
import tools.vitruv.dsls.commonalities.scoping.CommonalitiesLanguageGlobalScopeProvider;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
@SuppressWarnings("all")
public class CommonalitiesLanguageRuntimeModule extends AbstractCommonalitiesLanguageRuntimeModule {
  @Override
  public Class<? extends IValueConverterService> bindIValueConverterService() {
    return CommonalitiesLanguageValueConverterService.class;
  }

  @Override
  public Class<? extends IQualifiedNameConverter> bindIQualifiedNameConverter() {
    return CommonalitiesLanguageQualifiedNameConverter.class;
  }

  @Override
  public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
    return CommonalitiesLanguageQualifiedNameProvider.class;
  }

  public Class<? extends IEObjectDescriptionProvider> bindIEObjectDescriptionProvider() {
    return QualifiedNameProviderDescriptionProvider.class;
  }

  public Class<? extends IGenerator2> bindIGenerator2() {
    return CommonalitiesLanguageGenerator.class;
  }

  @Override
  public Class<? extends IDefaultResourceDescriptionStrategy> bindIDefaultResourceDescriptionStrategy() {
    return CommonalityFileResourceDescriptionStrategy.class;
  }

  @Override
  public Class<? extends IGlobalScopeProvider> bindIGlobalScopeProvider() {
    return CommonalitiesLanguageGlobalScopeProvider.class;
  }

  @Override
  public void configure(final Binder binder) {
    super.configure(binder);
    binder.bindScope(GenerationScoped.class, GenerationScope.GuiceScope.INSTANCE);
  }
}
