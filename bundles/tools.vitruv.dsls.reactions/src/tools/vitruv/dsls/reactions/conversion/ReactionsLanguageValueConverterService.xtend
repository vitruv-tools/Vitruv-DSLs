package tools.vitruv.dsls.reactions.conversion

import com.google.inject.Singleton
import org.eclipse.xtext.xbase.conversion.XbaseValueConverterService
import com.google.inject.Inject
import org.eclipse.xtext.conversion.impl.KeywordAlternativeConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.IValueConverter

@Singleton
class ReactionsLanguageValueConverterService extends XbaseValueConverterService {

	@Inject KeywordAlternativeConverter validIDConverter;

	@ValueConverter(rule = "EAttributeReference")
	def IValueConverter<String> getEAttributeReferenceConverter() {
		validIDConverter
	}

}
