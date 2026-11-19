package tools.vitruv.dsls.reactions.tests;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.testutils.InMemoryClassesCompiler;

/**
 * Compiles reactions files selected by {@code inputReactionFiles}
 * and {@code changePropagationSegments}. These are then made available to the various
 * {@link ReactionsExecutionTest}s through {@code getChangePropagationSpecifications}.
 */
public class TestReactionsCompiler {
  /**
   * Various parameters to configure the {@link Factory} with.
   */
  @Accessors
  public static class TestReactionsCompilerParameters {
    private Object reactionsOwner;
    private Path compilationProjectDir;
    private Iterable<String> reactions = null;
    private Iterable<String> changePropagationSegments = null;

    public void setReactionsOwner(Object reactionsOwner) {
      this.reactionsOwner = reactionsOwner;
    }

    public void setCompilationProjectDir(Path compilationProjectDir) {
      this.compilationProjectDir = compilationProjectDir;
    }

    public Iterable<String> getReactions() {
      return reactions;
    }

    public void setReactions(final Iterable<String> reactions) {
      this.reactions = reactions;
    }

    public void setChangePropagationSegments(final Iterable<String> changePropagationSegments) {
      this.changePropagationSegments = changePropagationSegments;
    }
  }

  /**
   * Factory for new {@link TestReactionsCompiler}s.
   */
  public static class Factory {
    @Inject
    private ParseHelper<ReactionsFile> parseHelper;
    @Inject
    private Provider<IReactionsGenerator> generatorProvider;
    @Inject
    private Provider<XtextResourceSet> resourceSetProvider;
    @Inject
    private Provider<JavaIoFileSystemAccess> fsaProvider;
    private final TestReactionsCompiler.TestReactionsCompilerParameters parameters
        = new TestReactionsCompiler.TestReactionsCompilerParameters();

    /**
     * Configures this factory using the parameters provided in {@code configurer}.
     *
     * @param configurer {@link TestReactionsCompilerParameters}
     * @return this
     */
    public TestReactionsCompiler.Factory setParameters(
        Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
      configurer.accept(parameters);
      return this;
    }

    /**
     * Configures this factory using the parameters provided in {@code configurer},
     * and then creates a new compiler for reactions.
     *
     * @param configurer {@link TestReactionsCompilerParameters}
     * @return new {@link TestReactionsCompiler}
     */
    public TestReactionsCompiler createCompiler(
        final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
      this.setParameters(configurer);
      Class<?> clazz = parameters.reactionsOwner.getClass();
      return new TestReactionsCompiler(clazz,
        parameters.compilationProjectDir,
        parameters.reactions,
        parameters.changePropagationSegments,
        parseHelper,
        generatorProvider,
        resourceSetProvider,
        fsaProvider);
    }
  }

  private final Class<?> reactionsOwner;
  private final Path compilationProjectFolder;
  private final Iterable<String> inputReactionFiles;
  private final Iterable<String> changePropagationSegments;
  private final ParseHelper<ReactionsFile> parseHelper;
  private final Provider<IReactionsGenerator> generatorProvider;
  private final Provider<XtextResourceSet> resourceSetProvider;
  private final Provider<JavaIoFileSystemAccess> fsaProvider;
  private boolean compiled = false;
  private Iterable<ChangePropagationSpecification> changePropagationSpecifications;

  private Set<ChangePropagationSpecification> compileReactions() {
    try {
      Path sourceFolder =
          Files.createDirectories(
              compilationProjectFolder.resolve(IProjectUtil.JAVA_SOURCE_FOLDER));
      Path generatedSourceFolder =
          Files.createDirectories(
              compilationProjectFolder.resolve(IProjectUtil.SOURCE_GEN_FOLDER));
      JavaIoFileSystemAccess fsa = fsaProvider.get();
      fsa.setOutputPath(generatedSourceFolder.toString());

      final IReactionsGenerator generator = generatorProvider.get();
      final XtextResourceSet resultResourceSet = resourceSetProvider.get();
      for (String inputReactionFile : inputReactionFiles) {
        {
          InputStream reactionFileStream = reactionsOwner.getResourceAsStream(inputReactionFile);
          Path srcFile = sourceFolder.resolve(Path.of(inputReactionFile).getFileName());
          ByteStreams.copy(reactionFileStream,
              Files.newOutputStream(srcFile, StandardOpenOption.CREATE_NEW));
          String reactionFileContent = Files.readString(srcFile);
          URI reactionFileUri = URI.createFileURI(srcFile.toString());
          parseHelper.parse(reactionFileContent, reactionFileUri, resultResourceSet);
        }
      }
      generator.addReactionsFiles(resultResourceSet);
      generator.generate(fsa);

      final Function1<ChangePropagationSpecification, Boolean> specificationNameFilter
          = specification ->
          IterableExtensions.exists(changePropagationSegments, it ->
              specification.getClass().getName().contains(it));

      InMemoryClassesCompiler inMemoryClassesCompiler =
          new InMemoryClassesCompiler(generatedSourceFolder);
      inMemoryClassesCompiler.compile();
      return inMemoryClassesCompiler.filterAndInstantiateClasses(
        ChangePropagationSpecification.class, specificationNameFilter::apply);
    } catch (Throwable e) {
      throw Exceptions.sneakyThrow(e);
    }
  }


  /**
   * Returns the {@link ChangePropagationSpecification}s that are
   * implemented by the provided reaction files.
   *
   * <p>Compiles all reactions selected by {@code inputReactionFiles}
   * and {@code changePropagationSegments}, if required beforehand.
   *
   * @return {@link Iterable}
   */
  public Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    if (!compiled) {
      changePropagationSpecifications = compileReactions();
      compiled = true;
    }
    return changePropagationSpecifications;
  }

  /**
   * Creates a new {@link TestReactionsCompiler}.
   *
   * @param reactionsOwner {@link Class}
   * @param compilationProjectFolder {@link Path}
   * @param inputReactionFiles {@link Iterable} of {@link String}s
   * @param changePropagationSegments {@link Iterable} of {@link String}s
   * @param parseHelper {@link ParseHelper}
   * @param generatorProvider {@link Provider}
   * @param resourceSetProvider {@link Provider}
   * @param fsaProvider {@link Provider}
   */
  public TestReactionsCompiler(
      Class<?> reactionsOwner,
      Path compilationProjectFolder,
      Iterable<String> inputReactionFiles,
      Iterable<String> changePropagationSegments,
      ParseHelper<ReactionsFile> parseHelper,
      Provider<IReactionsGenerator> generatorProvider,
      Provider<XtextResourceSet> resourceSetProvider,
      Provider<JavaIoFileSystemAccess> fsaProvider) {
    this.reactionsOwner = reactionsOwner;
    this.compilationProjectFolder = compilationProjectFolder;
    this.inputReactionFiles = inputReactionFiles;
    this.changePropagationSegments = changePropagationSegments;
    this.parseHelper = parseHelper;
    this.generatorProvider = generatorProvider;
    this.resourceSetProvider = resourceSetProvider;
    this.fsaProvider = fsaProvider;
  }
}
