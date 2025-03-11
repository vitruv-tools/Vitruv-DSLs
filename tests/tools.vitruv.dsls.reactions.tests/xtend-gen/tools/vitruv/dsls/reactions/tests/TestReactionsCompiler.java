package tools.vitruv.dsls.reactions.tests;

import com.google.common.base.Predicate;
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
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.testutils.InMemoryClassesCompiler;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class TestReactionsCompiler {
  @Accessors
  public static class TestReactionsCompilerParameters {
    private Object reactionsOwner;

    private Path compilationProjectDir;

    private Iterable<String> reactions = null;

    private Iterable<String> changePropagationSegments = null;

    @Pure
    public Object getReactionsOwner() {
      return this.reactionsOwner;
    }

    public void setReactionsOwner(final Object reactionsOwner) {
      this.reactionsOwner = reactionsOwner;
    }

    @Pure
    public Path getCompilationProjectDir() {
      return this.compilationProjectDir;
    }

    public void setCompilationProjectDir(final Path compilationProjectDir) {
      this.compilationProjectDir = compilationProjectDir;
    }

    @Pure
    public Iterable<String> getReactions() {
      return this.reactions;
    }

    public void setReactions(final Iterable<String> reactions) {
      this.reactions = reactions;
    }

    @Pure
    public Iterable<String> getChangePropagationSegments() {
      return this.changePropagationSegments;
    }

    public void setChangePropagationSegments(final Iterable<String> changePropagationSegments) {
      this.changePropagationSegments = changePropagationSegments;
    }
  }

  public static class Factory {
    @Inject
    private ParseHelper<ReactionsFile> parseHelper;

    @Inject
    private Provider<IReactionsGenerator> generatorProvider;

    @Inject
    private Provider<XtextResourceSet> resourceSetProvider;

    @Inject
    private Provider<JavaIoFileSystemAccess> fsaProvider;

    private TestReactionsCompiler.TestReactionsCompilerParameters parameters = new TestReactionsCompiler.TestReactionsCompilerParameters();

    public TestReactionsCompiler.Factory setParameters(final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
      TestReactionsCompiler.Factory _xblockexpression = null;
      {
        configurer.accept(this.parameters);
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    public TestReactionsCompiler createCompiler(final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> configurer) {
      this.setParameters(configurer);
      Class<?> _class = this.parameters.reactionsOwner.getClass();
      return new TestReactionsCompiler(_class, 
        this.parameters.compilationProjectDir, 
        this.parameters.reactions, 
        this.parameters.changePropagationSegments, 
        this.parseHelper, 
        this.generatorProvider, 
        this.resourceSetProvider, 
        this.fsaProvider);
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
      final Path sourceFolder = Files.createDirectories(this.compilationProjectFolder.resolve(IProjectUtil.JAVA_SOURCE_FOLDER));
      final Path generatedSourceFolder = Files.createDirectories(this.compilationProjectFolder.resolve(IProjectUtil.SOURCE_GEN_FOLDER));
      final JavaIoFileSystemAccess fsa = this.fsaProvider.get();
      fsa.setOutputPath(generatedSourceFolder.toString());
      final IReactionsGenerator generator = this.generatorProvider.get();
      final XtextResourceSet resultResourceSet = this.resourceSetProvider.get();
      for (final String inputReactionFile : this.inputReactionFiles) {
        {
          final InputStream reactionFileStream = this.reactionsOwner.getResourceAsStream(inputReactionFile);
          final Path srcFile = sourceFolder.resolve(Path.of(inputReactionFile).getFileName());
          ByteStreams.copy(reactionFileStream, Files.newOutputStream(srcFile, StandardOpenOption.CREATE_NEW));
          final String reactionFileContent = Files.readString(srcFile);
          final URI reactionFileUri = URI.createFileURI(srcFile.toString());
          this.parseHelper.parse(reactionFileContent, reactionFileUri, resultResourceSet);
        }
      }
      generator.addReactionsFiles(resultResourceSet);
      generator.generate(fsa);
      final Function1<ChangePropagationSpecification, Boolean> _function = (ChangePropagationSpecification specification) -> {
        final Function1<String, Boolean> _function_1 = (String it) -> {
          return Boolean.valueOf(specification.getClass().getName().contains(it));
        };
        return Boolean.valueOf(IterableExtensions.<String>exists(this.changePropagationSegments, _function_1));
      };
      final Function1<ChangePropagationSpecification, Boolean> specificationNameFilter = _function;
      return new InMemoryClassesCompiler(generatedSourceFolder).compile().<ChangePropagationSpecification>filterAndInstantiateClasses(
        ChangePropagationSpecification.class, new Predicate<ChangePropagationSpecification>() {
          public boolean apply(ChangePropagationSpecification arg0) {
            return specificationNameFilter.apply(arg0);
          }
      });
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public static String getBatchCompilerString(final Path path) {
    return path.toString().replace("[", "\\[").replace("]", "\\]");
  }

  public Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    if ((!this.compiled)) {
      this.compiled = true;
      this.changePropagationSpecifications = this.compileReactions();
    }
    return this.changePropagationSpecifications;
  }

  public TestReactionsCompiler(final Class<?> reactionsOwner, final Path compilationProjectFolder, final Iterable<String> inputReactionFiles, final Iterable<String> changePropagationSegments, final ParseHelper<ReactionsFile> parseHelper, final Provider<IReactionsGenerator> generatorProvider, final Provider<XtextResourceSet> resourceSetProvider, final Provider<JavaIoFileSystemAccess> fsaProvider) {
    super();
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
