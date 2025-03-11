package tools.vitruv.dsls.commonalities.conversion;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.KeywordAlternativeConverter;
import org.eclipse.xtext.xbase.conversion.XbaseValueConverterService;

@Singleton
@SuppressWarnings("all")
public class CommonalitiesLanguageValueConverterService extends XbaseValueConverterService {
  @Inject
  private KeywordAlternativeConverter validIDConverter;

  @Inject
  private QualifiedClassValueConverter qualifiedClassValueConverter;

  @Inject
  private OperatorNameConverter operatorNameConverter;

  @ValueConverter(rule = "DomainName")
  public IValueConverter<String> getDomainNameConverter() {
    return this.validIDConverter;
  }

  @ValueConverter(rule = "UnqualifiedClass")
  public IValueConverter<String> getUnqualifiedClassConverter() {
    return this.validIDConverter;
  }

  @ValueConverter(rule = "QualifiedClass")
  public IValueConverter<String> getQualifiedClassConverter() {
    return this.qualifiedClassValueConverter;
  }

  @ValueConverter(rule = "UnqualifiedAttribute")
  public IValueConverter<String> getUnqualifiedAttributeConverter() {
    return this.validIDConverter;
  }

  @ValueConverter(rule = "OperatorName")
  public IValueConverter<String> getOperatorNameConverter() {
    return this.operatorNameConverter;
  }

  @ValueConverter(rule = "QualifiedOperatorName")
  public IValueConverter<String> getQualifierOperatorNameConverter() {
    return this.operatorNameConverter;
  }

  @ValueConverter(rule = "QualifiedOperatorWildCard")
  public IValueConverter<String> getQualifierOperatorWilCardConverter() {
    return this.operatorNameConverter;
  }
}
