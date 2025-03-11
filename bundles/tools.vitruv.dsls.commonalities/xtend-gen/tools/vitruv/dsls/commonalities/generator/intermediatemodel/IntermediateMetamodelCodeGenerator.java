package tools.vitruv.dsls.commonalities.generator.intermediatemodel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.CloseResource;
import edu.kit.ipd.sdq.activextendannotations.Lazy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.SubGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;

@GenerationScoped
@SuppressWarnings("all")
public class IntermediateMetamodelCodeGenerator implements SubGenerator {
  private interface ECoreCodeGenerationTarget extends AutoCloseable {
    URI getUri();
  }

  private static class PlatformResourceTarget implements IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget {
    private final URI uri;

    private PlatformResourceTarget(final URI targetUri) {
      this.uri = targetUri;
    }

    @Override
    public URI getUri() {
      return this.uri;
    }

    @Override
    public void close() {
    }
  }

  private static class GloballyRegisteredFileTarget implements IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget {
    private static final String PLATFORM_RESOURCE_ID = IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.class.getName();

    private GloballyRegisteredFileTarget(final URI targetUri) {
      boolean _isFile = targetUri.isFile();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(targetUri);
      _builder.append(" must be a file URI!");
      Preconditions.checkArgument(_isFile, _builder);
      final URI platformUri = URI.createPlatformResourceURI(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID, true);
      boolean _containsKey = EcorePlugin.getPlatformResourceMap().containsKey(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID);
      boolean _not = (!_containsKey);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("The global URI map already contains a mapping for ‹");
      _builder_1.append(platformUri);
      _builder_1.append("›!");
      Preconditions.checkState(_not, _builder_1);
      EcorePlugin.getPlatformResourceMap().put(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID, targetUri);
    }

    @Override
    public URI getUri() {
      return URI.createPlatformResourceURI(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID, true);
    }

    @Override
    public void close() {
      URI _remove = EcorePlugin.getPlatformResourceMap().remove(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID);
      boolean _tripleNotEquals = (_remove != null);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Failed to unregister ‹");
      _builder.append(IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget.PLATFORM_RESOURCE_ID);
      _builder.append("›!");
      Preconditions.checkState(_tripleNotEquals, _builder);
    }
  }

  private static final Logger log = Logger.getLogger(IntermediateMetamodelCodeGenerator.class);

  private static final GenJDKLevel GENERATED_CODE_COMPLIANCE_LEVEL = GenJDKLevel.JDK80_LITERAL;

  private static final String GENERATED_CODE_FOLDER = ".";

  private static final URI INTERMEDIATEMODELBASE_GENMODEL_URI = IntermediateMetamodelCodeGenerator.ensurePlatformPlugin(EcorePlugin.getEPackageNsURIToGenModelLocationMap(true).get(IntermediateModelBasePackage.eINSTANCE.getNsURI()));

  @Lazy
  private GenModel _intermediateModelBaseGenModel;

  @Inject
  @Extension
  private GenerationContext _generationContext;

  @Override
  public void generate() {
    try {
      boolean _isNewResourceSet = this._generationContext.isNewResourceSet();
      boolean _not = (!_isNewResourceSet);
      if (_not) {
        return;
      }
      final URI fsaTargetUri = this._generationContext.getFsa().getURI(IntermediateMetamodelCodeGenerator.GENERATED_CODE_FOLDER);
      IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget _xifexpression = null;
      boolean _isPlatformResource = fsaTargetUri.isPlatformResource();
      if (_isPlatformResource) {
        _xifexpression = new IntermediateMetamodelCodeGenerator.PlatformResourceTarget(fsaTargetUri);
      } else {
        IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget _xifexpression_1 = null;
        if ((fsaTargetUri.isFile() && (!Platform.isRunning()))) {
          _xifexpression_1 = new IntermediateMetamodelCodeGenerator.GloballyRegisteredFileTarget(fsaTargetUri);
        } else {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Unsupported URI type: ‹");
          _builder.append(fsaTargetUri);
          _builder.append("›");
          throw new IllegalStateException(_builder.toString());
        }
        _xifexpression = _xifexpression_1;
      }
      final IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget target = _xifexpression;
      this.generateInto(target);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private void generateInto(@CloseResource final IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget target) throws Exception {
    try (IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget r_target = target) {
    	_generateInto_with_safe_resources(r_target);
    }			
  }

  private GenModel generateGenModel(final EPackage generatedPackage, final String conceptName, final IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget target) {
    GenModel _createGenModel = GenModelFactory.eINSTANCE.createGenModel();
    final Procedure1<GenModel> _function = (GenModel it) -> {
      it.setComplianceLevel(IntermediateMetamodelCodeGenerator.GENERATED_CODE_COMPLIANCE_LEVEL);
      it.setModelDirectory(target.getUri().toPlatformString(true));
      it.setCanGenerate(true);
      it.setModelName(conceptName);
      EList<GenPackage> _usedGenPackages = it.getUsedGenPackages();
      EList<GenPackage> _genPackages = this.getIntermediateModelBaseGenModel().getGenPackages();
      Iterables.<GenPackage>addAll(_usedGenPackages, _genPackages);
      it.initialize(Collections.<EPackage>singleton(generatedPackage));
      GenPackage _head = IterableExtensions.<GenPackage>head(it.getGenPackages());
      final Procedure1<GenPackage> _function_1 = (GenPackage it_1) -> {
        it_1.setPrefix(IntermediateModelConstants.getIntermediateMetamodelClassesPrefix(conceptName));
        it_1.setBasePackage(IntermediateModelConstants.getIntermediateMetamodelPackagePrefix());
        it_1.setAdapterFactory(false);
      };
      ObjectExtensions.<GenPackage>operator_doubleArrow(_head, _function_1);
    };
    return ObjectExtensions.<GenModel>operator_doubleArrow(_createGenModel, _function);
  }

  private void generateModelCode(final GenModel generatedGenModel) {
    GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI, 
      GenModelGeneratorAdapterFactory.DESCRIPTOR);
    Generator _generator = new Generator();
    final Procedure1<Generator> _function = (Generator it) -> {
      it.setInput(generatedGenModel);
    };
    final Generator generator = ObjectExtensions.<Generator>operator_doubleArrow(_generator, _function);
    BasicMonitor _basicMonitor = new BasicMonitor();
    final Diagnostic result = generator.generate(generatedGenModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, _basicMonitor);
    int _severity = result.getSeverity();
    boolean _notEquals = (_severity != Diagnostic.OK);
    if (_notEquals) {
      String _lineSeparator = System.lineSeparator();
      String _plus = ("Generating the intermediate model failed:" + _lineSeparator);
      String _explanation = this.getExplanation(result);
      String _plus_1 = (_plus + _explanation);
      throw new IllegalStateException(_plus_1);
    }
  }

  private String getExplanation(final Diagnostic diagnostic) {
    final Function1<Diagnostic, String> _function = (Diagnostic it) -> {
      return it.getMessage();
    };
    final Function1<String, CharSequence> _function_1 = (String it) -> {
      return ("  • " + it);
    };
    return IterableExtensions.<String>join(IterableExtensions.<String>toSet(IterableExtensions.<Diagnostic, String>map(this.getNotOkayRootDiagnostics(diagnostic), _function)), System.lineSeparator(), _function_1);
  }

  private Iterable<Diagnostic> getNotOkayRootDiagnostics(final Diagnostic diagnostic) {
    Iterable<Diagnostic> _xifexpression = null;
    int _severity = diagnostic.getSeverity();
    boolean _equals = (_severity == Diagnostic.OK);
    if (_equals) {
      _xifexpression = CollectionLiterals.<Diagnostic>emptyList();
    } else {
      Iterable<Diagnostic> _xifexpression_1 = null;
      boolean _isEmpty = diagnostic.getChildren().isEmpty();
      if (_isEmpty) {
        _xifexpression_1 = List.<Diagnostic>of(diagnostic);
      } else {
        final Function1<Diagnostic, Iterable<Diagnostic>> _function = (Diagnostic it) -> {
          return this.getNotOkayRootDiagnostics(it);
        };
        _xifexpression_1 = IterableExtensions.<Diagnostic, Diagnostic>flatMap(diagnostic.getChildren(), _function);
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  private static URI ensurePlatformPlugin(final URI uri) {
    URI _xifexpression = null;
    boolean _isPlatformResource = uri.isPlatformResource();
    if (_isPlatformResource) {
      _xifexpression = uri.replacePrefix(URI.createPlatformResourceURI("/", false), URI.createPlatformPluginURI("/", false));
    } else {
      URI _xifexpression_1 = null;
      boolean _isPlatformPlugin = uri.isPlatformPlugin();
      boolean _not = (!_isPlatformPlugin);
      if (_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Unsupported URI type, we need a platform plugin URI: ‹");
        _builder.append(uri);
        _builder.append("›");
        throw new IllegalStateException(_builder.toString());
      } else {
        _xifexpression_1 = uri;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  private boolean _intermediateModelBaseGenModel_isInitialised = false;

  private GenModel _intermediateModelBaseGenModel_initialise() {
    EObject _head = IterableExtensions.<EObject>head(this._generationContext.getResourceSet().getResource(IntermediateMetamodelCodeGenerator.INTERMEDIATEMODELBASE_GENMODEL_URI, true).getContents());
    return ((GenModel) _head);
  }

  public GenModel getIntermediateModelBaseGenModel() {
    if (!_intermediateModelBaseGenModel_isInitialised) {
    	try {
    		_intermediateModelBaseGenModel = _intermediateModelBaseGenModel_initialise();
    	} finally {
    		_intermediateModelBaseGenModel_isInitialised = true;
    	}
    }
    return _intermediateModelBaseGenModel;
  }

  private void _generateInto_with_safe_resources(final IntermediateMetamodelCodeGenerator.ECoreCodeGenerationTarget target) {
    Set<String> _generatedConcepts = this._generationContext.getGeneratedConcepts();
    for (final String generatedConcept : _generatedConcepts) {
      {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Generating code for the intermediate metamodel of concept \'");
        _builder.append(generatedConcept);
        _builder.append("\'.");
        IntermediateMetamodelCodeGenerator.log.debug(_builder);
        final EPackage generatedPackage = this._generationContext.getIntermediateMetamodelPackage(generatedConcept);
        final GenModel generatedGenModel = this.generateGenModel(generatedPackage, generatedConcept, target);
        this.generateModelCode(generatedGenModel);
      }
    }
  }
}
