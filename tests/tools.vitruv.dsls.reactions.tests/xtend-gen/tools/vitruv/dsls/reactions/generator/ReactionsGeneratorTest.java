package tools.vitruv.dsls.reactions.generator;

import allElementTypes.AllElementTypesPackage;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsFileBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsLanguageBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@SuppressWarnings("all")
public class ReactionsGeneratorTest {
  @Inject
  private Provider<InMemoryFileSystemAccess> fsaProvider;

  @Inject
  private Provider<IReactionsGenerator> generatorProvider;

  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;

  private static final String CHANGE_PROPAGATION_SPEC_NAME_SUFFIX = "ChangePropagationSpecification";

  private static final String REACTION_NAME = "TestReaction";

  private static final String FIRST_SEGMENT = "firstTestReaction";

  private static final String SECOND_SEGMENT = "secondTestReaction";

  private static final String THIRD_SEGMENT = "thirdTestReaction";

  private static final String FOURTH_SEGMENT = "fourthTestReaction";

  private FluentReactionsFileBuilder createReaction(final String reactionName, final String reactionsFileName) {
    final FluentReactionsLanguageBuilder create = new FluentReactionsLanguageBuilder();
    final FluentReactionsFileBuilder fileBuilder = create.reactionsFile(reactionsFileName);
    FluentReactionsSegmentBuilder _executeActionsIn = create.reactionsSegment(reactionsFileName).inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).executeActionsIn(
      AllElementTypesPackage.eINSTANCE);
    final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function = (FluentRoutineBuilder.RoutineStartBuilder it) -> {
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_1 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_2) -> {
          return this.createPrintlnStatement(it_2);
        };
        it_1.execute(_function_2);
      };
      it.update(_function_1);
    };
    FluentReactionBuilder _call = create.reaction(reactionName).afterAnyChange().call(_function);
    FluentReactionsSegmentBuilder _add = _executeActionsIn.operator_add(_call);
    fileBuilder.operator_add(_add);
    return fileBuilder;
  }

  private XFeatureCall createPrintlnStatement(final TypeProvider typeProvider) {
    XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
      JvmType _findTypeByName = typeProvider.findTypeByName(InputOutput.class.getName());
      final JvmGenericType type = ((JvmGenericType) _findTypeByName);
      final Function1<JvmOperation, Boolean> _function_1 = (JvmOperation it_1) -> {
        String _simpleName = it_1.getSimpleName();
        return Boolean.valueOf(Objects.equal(_simpleName, "println"));
      };
      it.setFeature(IterableExtensions.<JvmOperation>head(IterableExtensions.<JvmOperation>filter(Iterables.<JvmOperation>filter(type.getMembers(), JvmOperation.class), _function_1)));
      it.setExplicitOperationCall(true);
      EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
      XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
      final Procedure1<XStringLiteral> _function_2 = (XStringLiteral it_1) -> {
        it_1.setValue("That\'s it");
      };
      XStringLiteral _doubleArrow = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function_2);
      _featureCallArguments.add(_doubleArrow);
    };
    return ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
  }

  private static void assertFilesForReaction(final InMemoryFileSystemAccess fsa, final String segmentName, final String reactionName) {
    ReactionsGeneratorTest.assertFilesForReactionWithoutChangePropagationSpecification(fsa, segmentName, reactionName);
    String _firstUpper = StringExtensions.toFirstUpper(segmentName);
    String _plus = ((segmentName + "/") + _firstUpper);
    String _plus_1 = (_plus + ReactionsGeneratorTest.CHANGE_PROPAGATION_SPEC_NAME_SUFFIX);
    String _plus_2 = (_plus_1 + ".java");
    MatcherAssert.<Set<String>>assertThat(fsa.getAllFiles().keySet(), 
      CoreMatchers.<String>hasItem(CoreMatchers.endsWith(_plus_2)));
  }

  private static void assertFilesForReactionWithoutChangePropagationSpecification(final InMemoryFileSystemAccess fsa, final String segmentName, final String reactionName) {
    MatcherAssert.<Set<String>>assertThat(fsa.getAllFiles().keySet(), 
      CoreMatchers.<String>hasItem(CoreMatchers.endsWith((((segmentName + "/") + reactionName) + "Reaction.java"))));
    MatcherAssert.<Set<String>>assertThat(fsa.getAllFiles().keySet(), 
      CoreMatchers.<String>hasItem(CoreMatchers.endsWith((((segmentName + "/") + reactionName) + "RepairRoutine.java"))));
  }

  @Test
  public void testGenerateReactionsEnvironment() {
    IReactionsGenerator generator = this.generatorProvider.get();
    generator.useResourceSet(this.resourceSetProvider.get());
    InMemoryFileSystemAccess _get = this.fsaProvider.get();
    final Procedure1<InMemoryFileSystemAccess> _function = (InMemoryFileSystemAccess it) -> {
      it.setCurrentSource("src");
      it.setOutputPath("src-gen");
    };
    final InMemoryFileSystemAccess fsa = ObjectExtensions.<InMemoryFileSystemAccess>operator_doubleArrow(_get, _function);
    generator.addReactionsFile(this.createReaction(ReactionsGeneratorTest.REACTION_NAME, ReactionsGeneratorTest.FIRST_SEGMENT));
    generator.addReactionsFile(this.createReaction(ReactionsGeneratorTest.REACTION_NAME, ReactionsGeneratorTest.SECOND_SEGMENT));
    generator.addReactionsFile(this.createReaction(ReactionsGeneratorTest.REACTION_NAME, ReactionsGeneratorTest.THIRD_SEGMENT));
    generator.generate(fsa);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.FIRST_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.SECOND_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.THIRD_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    final Function1<Map.Entry<String, Object>, Boolean> _function_1 = (Map.Entry<String, Object> it) -> {
      String _key = it.getKey();
      String _firstUpper = StringExtensions.toFirstUpper(ReactionsGeneratorTest.SECOND_SEGMENT);
      String _plus = ((ReactionsGeneratorTest.SECOND_SEGMENT + "/") + _firstUpper);
      String _plus_1 = (_plus + ReactionsGeneratorTest.CHANGE_PROPAGATION_SPEC_NAME_SUFFIX);
      String _plus_2 = (_plus_1 + ".java");
      return Boolean.valueOf(_key.endsWith(_plus_2));
    };
    final String secondChangePropagationSpecificationFileName = IterableExtensions.<Map.Entry<String, Object>>findFirst(fsa.getAllFiles().entrySet(), _function_1).getKey();
    fsa.deleteFile(secondChangePropagationSpecificationFileName, "");
    generator = this.generatorProvider.get();
    generator.useResourceSet(this.resourceSetProvider.get());
    generator.addReactionsFile(this.createReaction(ReactionsGeneratorTest.REACTION_NAME, ReactionsGeneratorTest.FOURTH_SEGMENT));
    generator.generate(fsa);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.FIRST_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    ReactionsGeneratorTest.assertFilesForReactionWithoutChangePropagationSpecification(fsa, ReactionsGeneratorTest.SECOND_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.THIRD_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
    ReactionsGeneratorTest.assertFilesForReaction(fsa, ReactionsGeneratorTest.FOURTH_SEGMENT, ReactionsGeneratorTest.REACTION_NAME);
  }
}
