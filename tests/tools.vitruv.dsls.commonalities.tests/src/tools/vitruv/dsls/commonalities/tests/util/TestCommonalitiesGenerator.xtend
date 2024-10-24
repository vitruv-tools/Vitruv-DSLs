package tools.vitruv.dsls.commonalities.tests.util

import com.google.inject.Provider
import java.nio.file.Path
import jakarta.inject.Inject
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.xbase.testing.CompilationTestHelper

import static java.nio.charset.StandardCharsets.UTF_8
import static org.eclipse.emf.common.util.URI.*

import static extension java.nio.file.Files.writeString
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil
import org.eclipse.xtext.generator.GeneratorDelegate
import org.eclipse.xtext.generator.GeneratorContext
import org.eclipse.xtext.generator.URIBasedFileSystemAccess
import static com.google.common.base.Preconditions.checkState
import org.eclipse.core.runtime.Platform
import static org.hamcrest.MatcherAssert.assertThat
import static tools.vitruv.testutils.matchers.ModelMatchers.hasNoErrors
import tools.vitruv.change.propagation.ChangePropagationSpecification
import java.util.Set
import tools.vitruv.dsls.commonalities.generator.changepropagationspecification.ChangePropagationSpecificationConstants
import tools.vitruv.dsls.testutils.InMemoryClassesCompiler

/**
 * Xtext’s {@link CompilationTestHelper} is bug-ridden and does not work with the Ecore generator.
 * So we roll our own.
 */
class TestCommonalitiesGenerator {
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject IResourceServiceProvider.Registry resourceServiceRegistry

	var InMemoryClassesCompiler classesCompiler

	def void generate(Path testProject, Pair<String, CharSequence>... code) {
		checkState(
			!Platform.isRunning, '''«TestCommonalitiesGenerator.simpleName» can only be used in standalone mode!''')

		code.writeTo(testProject)

		loadResourceSet(testProject, code.map[key]) => [
			index()
			validate()
			generateInto(testProject)
		]

		compileGeneratedJava(testProject)
	}

	def Set<ChangePropagationSpecification> createChangePropagationSpecifications() {
		return classesCompiler.filterAndInstantiateClasses(ChangePropagationSpecification, [
			it.class.packageName == ChangePropagationSpecificationConstants.changePropagationSpecificationPackageName
		])
	}

	def private writeTo(Iterable<Pair<String, CharSequence>> code, Path testProject) {
		for (sourceCode : code) {
			testProject.getSourcePath(sourceCode.key).writeString(sourceCode.value, UTF_8)
		}
	}

	def private loadResourceSet(Path testProject, Iterable<String> paths) {
		val resourceSet = resourceSetProvider.get()
		for (path : paths) {
			resourceSet.createResource(createFileURI(testProject.getSourcePath(path).toString))
		}
		resourceSet.resources.forEach[load(emptyMap)]
		resourceSet
	}

	def private void index(ResourceSet resourceSet) {
		val resourceDescriptions = resourceSet.resources.map [ resource |
			resourceServiceRegistry.getResourceServiceProvider(resource.URI).resourceDescriptionManager.
				getResourceDescription(resource)
		]
		val index = new ResourceDescriptionsData(resourceDescriptions)
		ResourceDescriptionsData.ResourceSetAdapter.installResourceDescriptionsData(resourceSet, index)
	}

	def private void validate(ResourceSet resourceSet) {
		for (sourceResource : resourceSet.resources.filter(XtextResource).toList) {
			sourceResource.resourceServiceProvider.resourceValidator.validate(sourceResource, CheckMode.ALL,
				CancelIndicator.NullImpl)
			assertThat(sourceResource, hasNoErrors)
		}
	}

	def private void generateInto(ResourceSet resourceSet, Path testProject) {
		val fsa = new URIBasedFileSystemAccess => [
			converter = resourceSet.URIConverter
			outputPath = testProject.resolve(IProjectUtil.SOURCE_GEN_FOLDER).toString
		]
		val context = new GeneratorContext() => [
			cancelIndicator = CancelIndicator.NullImpl
		]

		for (sourceResource : resourceSet.resources.filter(XtextResource).toList) {
			sourceResource.resourceServiceProvider.get(GeneratorDelegate).generate(sourceResource, fsa, context)
		}
	}

	def private void compileGeneratedJava(Path testProject) {
		val generatedSourcesDir = testProject.resolve(IProjectUtil.SOURCE_GEN_FOLDER)
		classesCompiler = new InMemoryClassesCompiler(generatedSourcesDir)
		classesCompiler.compile()
	}

	def private static getSourcePath(Path testProject, String fileName) {
		testProject.resolve(fileName)
	}

}
