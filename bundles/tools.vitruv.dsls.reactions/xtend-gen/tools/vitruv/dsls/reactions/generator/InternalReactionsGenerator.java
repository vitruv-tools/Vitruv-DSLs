package tools.vitruv.dsls.reactions.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import edu.kit.ipd.sdq.activextendannotations.CloseResource;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.RuntimeIOException;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.builder.FluentReactionsFileBuilder;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

@SuppressWarnings("all")
public class InternalReactionsGenerator implements IReactionsGenerator {
  private static final URI SYNTHETIC_RESOURCES = URI.createHierarchicalURI("synthetic", null, null, new String[] {}, null, null);

  private boolean used = false;

  @Inject
  private IGenerator generator;

  @Inject
  private IResourceFactory resourceFactory;

  private ResourceSet artificialReactionsResourceSet;

  private final ArrayList<ResourceSet> reactionFileResourceSets = new ArrayList<ResourceSet>();

  private final ArrayList<Resource> resourcesToGenerate = new ArrayList<Resource>();

  private boolean addReaction(final String sourceFileName, final Reaction reaction) {
    boolean _xblockexpression = false;
    {
      Preconditions.<Reaction>checkNotNull(reaction, "Reaction must not be null!");
      Preconditions.checkState((this.artificialReactionsResourceSet != null), 
        "A resource set must be provided in order to add artificial reactions!");
      final ReactionsSegment reactionsSegment = this.getCorrespondingReactionsSegmentInTempResource(sourceFileName, 
        reaction.getReactionsSegment());
      EList<Reaction> _reactions = reactionsSegment.getReactions();
      _xblockexpression = _reactions.add(reaction);
    }
    return _xblockexpression;
  }

  private ReactionsSegment getCorrespondingReactionsSegmentInTempResource(final String sourceFileName, final ReactionsSegment reactionsSegment) {
    EList<Resource> _resources = this.artificialReactionsResourceSet.getResources();
    for (final Resource res : _resources) {
      boolean _equals = IterableExtensions.<String>last(res.getURI().segmentsList()).equals((sourceFileName + ".reactions"));
      if (_equals) {
        final ReactionsFile reactionsFile = ReactionsLanguageHelper.getReactionsFile(res);
        ReactionsSegment foundSegment = null;
        EList<ReactionsSegment> _reactionsSegments = reactionsFile.getReactionsSegments();
        for (final ReactionsSegment segment : _reactionsSegments) {
          if ((ListExtensions.<MetamodelImport, EPackage>map(segment.getFromMetamodels(), ((Function1<MetamodelImport, EPackage>) (MetamodelImport it) -> {
            return it.getPackage();
          })).equals(ListExtensions.<MetamodelImport, EPackage>map(reactionsSegment.getFromMetamodels(), ((Function1<MetamodelImport, EPackage>) (MetamodelImport it) -> {
            return it.getPackage();
          }))) && 
            ListExtensions.<MetamodelImport, EPackage>map(segment.getToMetamodels(), ((Function1<MetamodelImport, EPackage>) (MetamodelImport it) -> {
              return it.getPackage();
            })).equals(ListExtensions.<MetamodelImport, EPackage>map(reactionsSegment.getToMetamodels(), ((Function1<MetamodelImport, EPackage>) (MetamodelImport it) -> {
              return it.getPackage();
            }))))) {
            foundSegment = segment;
          }
        }
        if ((foundSegment == null)) {
          foundSegment = this.addReactionsSegment(reactionsFile, reactionsSegment, sourceFileName);
        }
        return foundSegment;
      }
    }
    final ReactionsFile newFile = this.createSyntheticResourceWithReactionsFile(sourceFileName);
    return this.addReactionsSegment(newFile, reactionsSegment, sourceFileName);
  }

  private ReactionsSegment addReactionsSegment(final ReactionsFile fileToAddTo, final ReactionsSegment originalSegment, final String segmentName) {
    ReactionsSegment _createReactionsSegment = TopLevelElementsFactory.eINSTANCE.createReactionsSegment();
    final Procedure1<ReactionsSegment> _function = (ReactionsSegment it) -> {
      EList<MetamodelImport> _fromMetamodels = it.getFromMetamodels();
      EList<MetamodelImport> _fromMetamodels_1 = originalSegment.getFromMetamodels();
      Iterables.<MetamodelImport>addAll(_fromMetamodels, _fromMetamodels_1);
      EList<MetamodelImport> _toMetamodels = it.getToMetamodels();
      EList<MetamodelImport> _toMetamodels_1 = originalSegment.getToMetamodels();
      Iterables.<MetamodelImport>addAll(_toMetamodels, _toMetamodels_1);
      it.setName(segmentName);
    };
    final ReactionsSegment newSegment = ObjectExtensions.<ReactionsSegment>operator_doubleArrow(_createReactionsSegment, _function);
    EList<ReactionsSegment> _reactionsSegments = fileToAddTo.getReactionsSegments();
    _reactionsSegments.add(newSegment);
    return newSegment;
  }

  @Override
  public void addReaction(final String sourceFileName, final Reaction... reactions) {
    this.addReaction(sourceFileName, IterableExtensions.<Reaction>toList(((Iterable<Reaction>)Conversions.doWrapArray(reactions))));
  }

  @Override
  public void addReaction(final String sourceFileName, final Iterable<? extends Reaction> reactions) {
    final Consumer<Reaction> _function = (Reaction it) -> {
      this.addReaction(sourceFileName, it);
    };
    reactions.forEach(_function);
  }

  @Override
  public void generate(final IFileSystemAccess2 fsa) {
    Preconditions.checkState((!this.used), "This generator was already used to generate reactions!");
    this.used = true;
    final Consumer<Resource> _function = (Resource it) -> {
      this.generateReactions(it, fsa);
    };
    this.resourcesToGenerate.forEach(_function);
  }

  private Resource createSyntheticResource(final String sourceFileName) {
    int uriAppendix = 1;
    URI resourceUri = InternalReactionsGenerator.SYNTHETIC_RESOURCES.appendSegment(sourceFileName).appendFileExtension("reactions");
    while ((this.artificialReactionsResourceSet.getResource(resourceUri, false) != null)) {
      {
        resourceUri = InternalReactionsGenerator.SYNTHETIC_RESOURCES.appendSegment((sourceFileName + Integer.valueOf(uriAppendix))).appendFileExtension("reactions");
        uriAppendix++;
      }
    }
    final Resource resource = this.resourceFactory.createResource(resourceUri);
    EList<Resource> _resources = this.artificialReactionsResourceSet.getResources();
    _resources.add(resource);
    this.resourcesToGenerate.add(resource);
    return resource;
  }

  private ReactionsFile createSyntheticResourceWithReactionsFile(final String sourceFileName) {
    final Resource singleReactionResource = this.createSyntheticResource(sourceFileName);
    final ReactionsFile reactionsFile = TopLevelElementsFactory.eINSTANCE.createReactionsFile();
    singleReactionResource.getContents().add(reactionsFile);
    return reactionsFile;
  }

  private void generateReactions(final Resource reactionsResource, final IFileSystemAccess fsa) {
    this.generator.doGenerate(reactionsResource, fsa);
  }

  @Override
  public void addReactionsFiles(final XtextResourceSet resourceSet) {
    this.reactionFileResourceSets.add(resourceSet);
    final Function1<Resource, Boolean> _function = (Resource it) -> {
      return Boolean.valueOf(ReactionsLanguageHelper.containsReactionsFile(it));
    };
    Iterable<Resource> _filter = IterableExtensions.<Resource>filter(resourceSet.getResources(), _function);
    Iterables.<Resource>addAll(this.resourcesToGenerate, _filter);
  }

  @Override
  public void addReactionsFile(final FluentReactionsFileBuilder reactionBuilder) {
    Preconditions.checkState((this.artificialReactionsResourceSet != null), 
      "A resource set must be provided in order to add artificial reactions files!");
    final Resource resource = this.createSyntheticResource(reactionBuilder.getFileName());
    reactionBuilder.attachTo(resource);
  }

  @Override
  public void addReactionsFile(final String sourceFileName, final ReactionsFile reactionsFile) {
    Preconditions.checkState((this.artificialReactionsResourceSet != null), 
      "A resource set must be provided in order to add artificial reactions files!");
    final Resource reactionsFileResource = this.createSyntheticResource(sourceFileName);
    reactionsFileResource.getContents().add(reactionsFile);
    this.resourcesToGenerate.add(reactionsFileResource);
  }

  @Override
  public void writeReactions(final IFileSystemAccess2 fsa) throws IOException {
    this.writeReactions(fsa, null);
  }

  @Override
  public void writeReactions(final IFileSystemAccess2 fsa, final String subPath) throws IOException {
    try {
      String _xifexpression = null;
      if ((subPath == null)) {
        _xifexpression = "";
      } else {
        String _xifexpression_1 = null;
        boolean _endsWith = subPath.endsWith("/");
        if (_endsWith) {
          _xifexpression_1 = subPath;
        } else {
          _xifexpression_1 = (subPath + "/");
        }
        _xifexpression = _xifexpression_1;
      }
      final String pathPrefix = _xifexpression;
      final ThreadFactory _function = (Runnable r) -> {
        return new Thread(r, "Reactions Serializer");
      };
      final ExecutorService serializationExecutor = Executors.newCachedThreadPool(_function);
      try {
        final Function1<Resource, Pair<Resource, PipedInputStream>> _function_1 = (Resource resource) -> {
          try {
            Pair<Resource, PipedInputStream> _xblockexpression = null;
            {
              final PipedOutputStream serializationInput = new PipedOutputStream();
              final PipedInputStream serializationOutput = new PipedInputStream(serializationInput, 102400);
              final Callable<Void> _function_2 = () -> {
                return InternalReactionsGenerator.writeTo(resource, serializationInput);
              };
              serializationExecutor.<Void>submit(_function_2);
              _xblockexpression = Pair.<Resource, PipedInputStream>of(resource, serializationOutput);
            }
            return _xblockexpression;
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        };
        final Consumer<Pair<Resource, PipedInputStream>> _function_2 = (Pair<Resource, PipedInputStream> it) -> {
          try {
            final Resource resource = it.getKey();
            final PipedInputStream output = it.getValue();
            String _lastSegment = resource.getURI().lastSegment();
            String _plus = (pathPrefix + _lastSegment);
            InternalReactionsGenerator.writeTo(output, fsa, _plus);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        };
        IterableUtil.<Resource, Pair<Resource, PipedInputStream>>mapFixed(this.resourcesToGenerate, _function_1).forEach(_function_2);
        serializationExecutor.shutdown();
        serializationExecutor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (final Throwable _t) {
        if (_t instanceof RuntimeIOException) {
          final RuntimeIOException runtimeIoError = (RuntimeIOException)_t;
          throw this.mapRuntimeIoException(runtimeIoError);
        } else if (_t instanceof ExecutionException) {
          final ExecutionException writerError = (ExecutionException)_t;
          throw this.mapWriteException(writerError.getCause());
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      } finally {
        serializationExecutor.shutdownNow();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private static Void writeTo(final Resource resource, @CloseResource final OutputStream outputStream) throws IOException {
    try (OutputStream r_outputStream = outputStream) {
    	return _writeTo_with_safe_resources(resource, r_outputStream);
    }			
  }

  private static Void writeTo(@CloseResource final InputStream inputStream, final IFileSystemAccess2 fsa, final String path) throws RuntimeIOException, IOException {
    try (InputStream r_inputStream = inputStream) {
    	return _writeTo_with_safe_resources(r_inputStream, fsa, path);
    }			
  }

  private IOException mapWriteException(final Throwable executionException) {
    try {
      IOException _switchResult = null;
      boolean _matched = false;
      if (executionException instanceof IOException) {
        _matched=true;
        throw ((IOException)executionException);
      }
      if (!_matched) {
        if (executionException instanceof RuntimeIOException) {
          _matched=true;
          _switchResult = this.mapRuntimeIoException(((RuntimeIOException)executionException));
        }
      }
      if (!_matched) {
        throw new IOException(executionException);
      }
      return _switchResult;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private IOException mapRuntimeIoException(final RuntimeIOException runtimeIOException) {
    try {
      final Throwable realException = runtimeIOException.getCause();
      boolean _matched = false;
      if (realException instanceof IOException) {
        _matched=true;
        throw ((IOException)realException);
      }
      throw new IOException(realException);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Override
  public void useResourceSet(final ResourceSet resourceSet) {
    Preconditions.<ResourceSet>checkNotNull(resourceSet);
    this.artificialReactionsResourceSet = resourceSet;
    this.reactionFileResourceSets.add(resourceSet);
  }

  private static Void _writeTo_with_safe_resources(final Resource resource, final OutputStream outputStream) throws IOException {
    Object _xblockexpression = null;
    {
      resource.save(outputStream, CollectionLiterals.<Object, Object>emptyMap());
      _xblockexpression = null;
    }
    return ((Void)_xblockexpression);
  }

  private static Void _writeTo_with_safe_resources(final InputStream inputStream, final IFileSystemAccess2 fsa, final String path) throws RuntimeIOException {
    Object _xblockexpression = null;
    {
      fsa.generateFile(path, inputStream);
      _xblockexpression = null;
    }
    return ((Void)_xblockexpression);
  }
}
