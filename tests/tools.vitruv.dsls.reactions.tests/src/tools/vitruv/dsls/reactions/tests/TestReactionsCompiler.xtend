package tools.vitruv.dsls.reactions.tests

import com.google.common.io.ByteStreams
import com.google.inject.Inject
import com.google.inject.Provider
import java.nio.file.Path
import java.util.function.Consumer
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.generator.JavaIoFileSystemAccess
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.testing.util.ParseHelper
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile
import tools.vitruv.change.propagation.ChangePropagationSpecification

import static java.nio.file.Files.createDirectories
import static java.nio.file.Files.newOutputStream
import static java.nio.file.StandardOpenOption.CREATE_NEW
import static extension java.nio.file.Files.readString
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil
import tools.vitruv.dsls.testutils.InMemoryClassesCompiler

@FinalFieldsConstructor
class TestReactionsCompiler {
	val Class<?> reactionsOwner
	val Path compilationProjectFolder
	val Iterable<String> inputReactionFiles
	val Iterable<String> changePropagationSegments

	val ParseHelper<ReactionsFile> parseHelper
	val Provider<IReactionsGenerator> generatorProvider
	val Provider<XtextResourceSet> resourceSetProvider
	val Provider<JavaIoFileSystemAccess> fsaProvider

	var compiled = false
	var Iterable<ChangePropagationSpecification> changePropagationSpecifications

	def private compileReactions() {
		val sourceFolder = createDirectories(compilationProjectFolder.resolve(IProjectUtil.JAVA_SOURCE_FOLDER))
		val generatedSourceFolder = createDirectories(compilationProjectFolder.resolve(IProjectUtil.SOURCE_GEN_FOLDER))
		val fsa = fsaProvider.get()
		fsa.outputPath = generatedSourceFolder.toString

		val generator = generatorProvider.get()
		val resultResourceSet = resourceSetProvider.get()

		for (inputReactionFile : inputReactionFiles) {
			val reactionFileStream = reactionsOwner.getResourceAsStream(inputReactionFile)
			val srcFile = sourceFolder.resolve(Path.of(inputReactionFile).fileName)
			ByteStreams.copy(reactionFileStream, newOutputStream(srcFile, CREATE_NEW))

			val reactionFileContent = readString(srcFile)
			val reactionFileUri = URI.createFileURI(srcFile.toString)
			parseHelper.parse(reactionFileContent, reactionFileUri, resultResourceSet)
		}

		generator.addReactionsFiles(resultResourceSet)
		generator.generate(fsa)

		val specificationNameFilter = [ ChangePropagationSpecification specification |
			changePropagationSegments.exists[specification.class.name.contains(it)]
		]
		return new InMemoryClassesCompiler(generatedSourceFolder).compile().filterAndInstantiateClasses(
			ChangePropagationSpecification, specificationNameFilter)
	}

	def static getBatchCompilerString(Path path) {
		path.toString().replace('[', '\\[').replace(']', '\\]');
	}

	def Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
		if (!compiled) {
			compiled = true
			changePropagationSpecifications = compileReactions()
		}
		return changePropagationSpecifications
	}

	@Accessors
	static class TestReactionsCompilerParameters {
		var Object reactionsOwner
		var Path compilationProjectDir
		var Iterable<String> reactions = null
		var Iterable<String> changePropagationSegments = null
	}

	static class Factory {
		@Inject ParseHelper<ReactionsFile> parseHelper
		@Inject Provider<IReactionsGenerator> generatorProvider
		@Inject Provider<XtextResourceSet> resourceSetProvider
		@Inject Provider<JavaIoFileSystemAccess> fsaProvider
		var parameters = new TestReactionsCompilerParameters

		def setParameters(Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
			configurer.accept(parameters)
			this
		}

		def createCompiler(Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
			setParameters(configurer)
			return new TestReactionsCompiler(
				parameters.reactionsOwner.class,
				parameters.compilationProjectDir,
				parameters.reactions,
				parameters.changePropagationSegments,
				parseHelper,
				generatorProvider,
				resourceSetProvider,
				fsaProvider
			)
		}
	}

}
