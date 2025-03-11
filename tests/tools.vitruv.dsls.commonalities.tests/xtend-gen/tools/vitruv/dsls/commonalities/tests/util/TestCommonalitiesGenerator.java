package tools.vitruv.dsls.commonalities.tests.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Provider;
import edu.kit.ipd.sdq.commons.util.org.eclipse.core.resources.IProjectUtil;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.URIBasedFileSystemAccess;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.hamcrest.MatcherAssert;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.commonalities.generator.changepropagationspecification.ChangePropagationSpecificationConstants;
import tools.vitruv.dsls.testutils.InMemoryClassesCompiler;
import tools.vitruv.testutils.matchers.ModelMatchers;

/**
 * Xtext’s {@link CompilationTestHelper} is bug-ridden and does not work with the Ecore generator.
 * So we roll our own.
 */
@SuppressWarnings("all")
public class TestCommonalitiesGenerator {
  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;

  @Inject
  private IResourceServiceProvider.Registry resourceServiceRegistry;

  private InMemoryClassesCompiler classesCompiler;

  public void generate(final Path testProject, final Pair<String, CharSequence>... code) {
    boolean _isRunning = Platform.isRunning();
    boolean _not = (!_isRunning);
    StringConcatenation _builder = new StringConcatenation();
    String _simpleName = TestCommonalitiesGenerator.class.getSimpleName();
    _builder.append(_simpleName);
    _builder.append(" can only be used in standalone mode!");
    Preconditions.checkState(_not, _builder);
    this.writeTo(((Iterable<Pair<String, CharSequence>>)Conversions.doWrapArray(code)), testProject);
    final Function1<Pair<String, CharSequence>, String> _function = (Pair<String, CharSequence> it) -> {
      return it.getKey();
    };
    XtextResourceSet _loadResourceSet = this.loadResourceSet(testProject, ListExtensions.<Pair<String, CharSequence>, String>map(((List<Pair<String, CharSequence>>)Conversions.doWrapArray(code)), _function));
    final Procedure1<XtextResourceSet> _function_1 = (XtextResourceSet it) -> {
      this.index(it);
      this.validate(it);
      this.generateInto(it, testProject);
    };
    ObjectExtensions.<XtextResourceSet>operator_doubleArrow(_loadResourceSet, _function_1);
    this.compileGeneratedJava(testProject);
  }

  public Set<ChangePropagationSpecification> createChangePropagationSpecifications() {
    final Predicate<ChangePropagationSpecification> _function = (ChangePropagationSpecification it) -> {
      String _packageName = it.getClass().getPackageName();
      String _changePropagationSpecificationPackageName = ChangePropagationSpecificationConstants.getChangePropagationSpecificationPackageName();
      return Objects.equal(_packageName, _changePropagationSpecificationPackageName);
    };
    return this.classesCompiler.<ChangePropagationSpecification>filterAndInstantiateClasses(ChangePropagationSpecification.class, _function);
  }

  private void writeTo(final Iterable<Pair<String, CharSequence>> code, final Path testProject) {
    try {
      for (final Pair<String, CharSequence> sourceCode : code) {
        Files.writeString(TestCommonalitiesGenerator.getSourcePath(testProject, sourceCode.getKey()), sourceCode.getValue(), StandardCharsets.UTF_8);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private XtextResourceSet loadResourceSet(final Path testProject, final Iterable<String> paths) {
    XtextResourceSet _xblockexpression = null;
    {
      final XtextResourceSet resourceSet = this.resourceSetProvider.get();
      for (final String path : paths) {
        resourceSet.createResource(URI.createFileURI(TestCommonalitiesGenerator.getSourcePath(testProject, path).toString()));
      }
      final Consumer<Resource> _function = (Resource it) -> {
        try {
          it.load(CollectionLiterals.<Object, Object>emptyMap());
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      resourceSet.getResources().forEach(_function);
      _xblockexpression = resourceSet;
    }
    return _xblockexpression;
  }

  private void index(final ResourceSet resourceSet) {
    final Function1<Resource, IResourceDescription> _function = (Resource resource) -> {
      return this.resourceServiceRegistry.getResourceServiceProvider(resource.getURI()).getResourceDescriptionManager().getResourceDescription(resource);
    };
    final List<IResourceDescription> resourceDescriptions = ListExtensions.<Resource, IResourceDescription>map(resourceSet.getResources(), _function);
    final ResourceDescriptionsData index = new ResourceDescriptionsData(resourceDescriptions);
    ResourceDescriptionsData.ResourceSetAdapter.installResourceDescriptionsData(resourceSet, index);
  }

  private void validate(final ResourceSet resourceSet) {
    List<XtextResource> _list = IterableExtensions.<XtextResource>toList(Iterables.<XtextResource>filter(resourceSet.getResources(), XtextResource.class));
    for (final XtextResource sourceResource : _list) {
      {
        sourceResource.getResourceServiceProvider().getResourceValidator().validate(sourceResource, CheckMode.ALL, 
          CancelIndicator.NullImpl);
        MatcherAssert.<XtextResource>assertThat(sourceResource, ModelMatchers.hasNoErrors());
      }
    }
  }

  private void generateInto(final ResourceSet resourceSet, final Path testProject) {
    URIBasedFileSystemAccess _uRIBasedFileSystemAccess = new URIBasedFileSystemAccess();
    final Procedure1<URIBasedFileSystemAccess> _function = (URIBasedFileSystemAccess it) -> {
      it.setConverter(resourceSet.getURIConverter());
      it.setOutputPath(testProject.resolve(IProjectUtil.SOURCE_GEN_FOLDER).toString());
    };
    final URIBasedFileSystemAccess fsa = ObjectExtensions.<URIBasedFileSystemAccess>operator_doubleArrow(_uRIBasedFileSystemAccess, _function);
    GeneratorContext _generatorContext = new GeneratorContext();
    final Procedure1<GeneratorContext> _function_1 = (GeneratorContext it) -> {
      it.setCancelIndicator(CancelIndicator.NullImpl);
    };
    final GeneratorContext context = ObjectExtensions.<GeneratorContext>operator_doubleArrow(_generatorContext, _function_1);
    List<XtextResource> _list = IterableExtensions.<XtextResource>toList(Iterables.<XtextResource>filter(resourceSet.getResources(), XtextResource.class));
    for (final XtextResource sourceResource : _list) {
      sourceResource.getResourceServiceProvider().<GeneratorDelegate>get(GeneratorDelegate.class).generate(sourceResource, fsa, context);
    }
  }

  private void compileGeneratedJava(final Path testProject) {
    try {
      final Path generatedSourcesDir = testProject.resolve(IProjectUtil.SOURCE_GEN_FOLDER);
      InMemoryClassesCompiler _inMemoryClassesCompiler = new InMemoryClassesCompiler(generatedSourcesDir);
      this.classesCompiler = _inMemoryClassesCompiler;
      this.classesCompiler.compile();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private static Path getSourcePath(final Path testProject, final String fileName) {
    return testProject.resolve(fileName);
  }
}
