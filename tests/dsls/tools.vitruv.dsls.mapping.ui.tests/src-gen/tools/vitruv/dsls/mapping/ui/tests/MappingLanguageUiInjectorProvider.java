/*
 * generated by Xtext 2.10.0
 */
package tools.vitruv.dsls.mapping.ui.tests;

import com.google.inject.Injector;
import org.eclipse.xtext.junit4.IInjectorProvider;
import tools.vitruv.dsls.mapping.ui.internal.MappingActivator;

public class MappingLanguageUiInjectorProvider implements IInjectorProvider {

	@Override
	public Injector getInjector() {
		return MappingActivator.getInstance().getInjector("tools.vitruv.dsls.mapping.MappingLanguage");
	}

}