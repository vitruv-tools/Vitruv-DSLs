package tools.vitruv.dsls.commonalities.generator;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import tools.vitruv.dsls.commonalities.generator.changepropagationspecification.ChangePropagationSpecificationGenerator;
import tools.vitruv.dsls.commonalities.generator.intermediatemodel.IntermediateMetamodelCodeGenerator;
import tools.vitruv.dsls.commonalities.generator.intermediatemodel.IntermediateMetamodelGenerator;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScope;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;

@SuppressWarnings("all")
public class CommonalitiesLanguageGenerator implements IGenerator2 {
  @Inject
  private GenerationContext.Factory generationContextFactory;

  @Inject
  private Provider<IntermediateMetamodelGenerator> intermediateMetamodelGenerator;

  @Inject
  private Provider<IntermediateMetamodelCodeGenerator> intermediateMetamodelCodeGenerator;

  @Inject
  private Provider<ReactionsGenerator> reactionsGenerator;

  @Inject
  private Provider<ChangePropagationSpecificationGenerator> changePropagationSpecificationGenerator;

  private final HashMap<Resource, GenerationScope> generationScopes = new HashMap<Resource, GenerationScope>();

  private List<? extends SubGenerator> getSubGenerators() {
    IntermediateMetamodelGenerator _get = this.intermediateMetamodelGenerator.get();
    IntermediateMetamodelCodeGenerator _get_1 = this.intermediateMetamodelCodeGenerator.get();
    ReactionsGenerator _get_2 = this.reactionsGenerator.get();
    ChangePropagationSpecificationGenerator _get_3 = this.changePropagationSpecificationGenerator.get();
    return Collections.<SubGenerator>unmodifiableList(CollectionLiterals.<SubGenerator>newArrayList(_get, _get_1, _get_2, _get_3));
  }

  @Override
  public void beforeGenerate(final Resource input, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    final GenerationScope generationScope = new GenerationScope();
    final CommonalityFile commonalityFile = CommonalitiesLanguageGenerator.containedCommonalityFile(input);
    generationScope.<GenerationContext>seed(GenerationContext.class, this.generationContextFactory.create(fsa, commonalityFile));
    this.generationScopes.put(input, generationScope);
    final Runnable _function = () -> {
      final Consumer<SubGenerator> _function_1 = (SubGenerator it) -> {
        it.beforeGenerate();
      };
      this.getSubGenerators().forEach(_function_1);
    };
    this.runInGenerationScope(input, _function);
  }

  @Override
  public void doGenerate(final Resource input, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    final Runnable _function = () -> {
      final Consumer<SubGenerator> _function_1 = (SubGenerator it) -> {
        it.generate();
      };
      this.getSubGenerators().forEach(_function_1);
    };
    this.runInGenerationScope(input, _function);
  }

  @Override
  public void afterGenerate(final Resource input, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    final Runnable _function = () -> {
      final Consumer<SubGenerator> _function_1 = (SubGenerator it) -> {
        it.afterGenerate();
      };
      this.getSubGenerators().forEach(_function_1);
    };
    this.runInGenerationScope(input, _function);
    this.generationScopes.remove(input);
  }

  private void runInGenerationScope(final Resource input, final Runnable runnable) {
    final GenerationScope generationScope = this.generationScopes.get(input);
    try {
      generationScope.enter();
      runnable.run();
    } finally {
      generationScope.leave();
    }
  }

  private static CommonalityFile containedCommonalityFile(final Resource input) {
    CommonalityFile _xblockexpression = null;
    {
      int _length = ((Object[])Conversions.unwrapArray(input.getContents(), Object.class)).length;
      boolean _equals = (_length == 0);
      if (_equals) {
        throw new GeneratorException("Input resource is empty.");
      }
      int _length_1 = ((Object[])Conversions.unwrapArray(input.getContents(), Object.class)).length;
      boolean _greaterThan = (_length_1 > 1);
      if (_greaterThan) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("The input resource may only contain one element (found ");
        int _length_2 = ((Object[])Conversions.unwrapArray(input.getContents(), Object.class)).length;
        _builder.append(_length_2);
        _builder.append(")");
        throw new GeneratorException(_builder.toString());
      }
      final EObject inputObject = input.getContents().get(0);
      if ((!(inputObject instanceof CommonalityFile))) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The input resource does not contain a Commonality file (but a ");
        String _simpleName = inputObject.getClass().getSimpleName();
        _builder_1.append(_simpleName);
        throw new GeneratorException(_builder_1.toString());
      }
      _xblockexpression = ((CommonalityFile) inputObject);
    }
    return _xblockexpression;
  }
}
