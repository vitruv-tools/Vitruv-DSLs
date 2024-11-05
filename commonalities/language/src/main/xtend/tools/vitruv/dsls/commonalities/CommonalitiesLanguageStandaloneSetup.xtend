/*
 * generated by Xtext 2.12.0
 */
package tools.vitruv.dsls.commonalities

import com.google.inject.Injector
import tools.vitruv.dsls.reactions.ReactionsLanguageStandaloneSetup
import org.eclipse.emf.ecore.plugin.EcorePlugin

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class CommonalitiesLanguageStandaloneSetup extends CommonalitiesLanguageStandaloneSetupGenerated {
	override Injector createInjectorAndDoEMFRegistration() {
		// Makes ECore register our custom implementations of EPackages
		EcorePlugin.ExtensionProcessor.process(null)
		// Set up Reactions Language dependency:
		ReactionsLanguageStandaloneSetup.doSetup()
		return super.createInjectorAndDoEMFRegistration()
	}

	static def void doSetup() {
		new CommonalitiesLanguageStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}