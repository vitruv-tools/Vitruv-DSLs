package tools.vitruv.dsls.reactions.generator;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import allElementTypes.AllElementTypesPackage;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Optional;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.builder.FluentReactionsFileBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsLanguageBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
class ReactionsGeneratorTest {

  @Inject
  private Provider<InMemoryFileSystemAccess> fsaProvider;
  @Inject
  private Provider<IReactionsGenerator> generatorProvider;
  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;

  private static final String CHANGE_PROPAGATION_SPEC_NAME_SUFFIX =
      "ChangePropagationSpecification";
  private static final String REACTION_NAME = "TestReaction";
  private static final String FIRST_SEGMENT = "firstTestReaction";
  private static final String SECOND_SEGMENT = "secondTestReaction";
  private static final String THIRD_SEGMENT = "thirdTestReaction";
  private static final String FOURTH_SEGMENT = "fourthTestReaction";

  private FluentReactionsFileBuilder createReaction(String reactionName, String reactionsFileName) {
    FluentReactionsLanguageBuilder create = new FluentReactionsLanguageBuilder();
    FluentReactionsFileBuilder fileBuilder = create.reactionsFile(reactionsFileName);
    fileBuilder.operator_add(
        create
            .reactionsSegment(reactionsFileName)
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                create
                    .reaction(reactionName)
                    .afterAnyChange()
                    .call(
                        routine ->
                            routine.update(
                                updates -> updates.execute(this::createPrintlnStatement)))));
    return fileBuilder;
  }

  private XFeatureCall createPrintlnStatement(TypeProvider typeProvider) {
    XFeatureCall printlnCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    printlnCall.setFeature(typeProvider.findMethod(InputOutput.class, "println"));
    printlnCall.setExplicitOperationCall(true);
    XStringLiteral message = XbaseFactory.eINSTANCE.createXStringLiteral();
    message.setValue("That's it");
    printlnCall.getFeatureCallArguments().add(message);
    return printlnCall;
  }

  private static String getChangePropagationSpecificationFileSuffix(String segmentName) {
    return segmentName
        + "/"
        + StringExtensions.toFirstUpper(segmentName)
        + CHANGE_PROPAGATION_SPEC_NAME_SUFFIX
        + ".java";
  }

  private static void assertFilesForReaction(
      InMemoryFileSystemAccess fsa, String segmentName, String reactionName) {
    assertFilesForReactionWithoutChangePropagationSpecification(fsa, segmentName, reactionName);
    assertThat(
        fsa.getAllFiles().keySet(),
        hasItem(endsWith(getChangePropagationSpecificationFileSuffix(segmentName))));
  }

  private static void assertFilesForReactionWithoutChangePropagationSpecification(
      InMemoryFileSystemAccess fsa, String segmentName, String reactionName) {
    assertThat(
        fsa.getAllFiles().keySet(),
        hasItem(endsWith(segmentName + "/" + reactionName + "Reaction.java")));
    assertThat(
        fsa.getAllFiles().keySet(),
        hasItem(endsWith(segmentName + "/" + reactionName + "RepairRoutine.java")));
  }

  @Test
  void testGenerateReactionsEnvironment() {
    IReactionsGenerator generator = generatorProvider.get();
    generator.useResourceSet(resourceSetProvider.get());

    InMemoryFileSystemAccess fsa = fsaProvider.get();
    fsa.setCurrentSource("src");
    fsa.setOutputPath("src-gen");
    generator.addReactionsFile(createReaction(REACTION_NAME, FIRST_SEGMENT));
    generator.addReactionsFile(createReaction(REACTION_NAME, SECOND_SEGMENT));
    generator.addReactionsFile(createReaction(REACTION_NAME, THIRD_SEGMENT));
    generator.generate(fsa);

    assertFilesForReaction(fsa, FIRST_SEGMENT, REACTION_NAME);
    assertFilesForReaction(fsa, SECOND_SEGMENT, REACTION_NAME);
    assertFilesForReaction(fsa, THIRD_SEGMENT, REACTION_NAME);

    Optional<String> secondChangePropagationSpecificationFileName =
        fsa.getAllFiles().keySet().stream()
            .filter(name -> name.endsWith(getChangePropagationSpecificationFileSuffix(SECOND_SEGMENT)))
            .findFirst();
    assertTrue(secondChangePropagationSpecificationFileName.isPresent());
    fsa.deleteFile(secondChangePropagationSpecificationFileName.get(), "");

    generator = generatorProvider.get();
    generator.useResourceSet(resourceSetProvider.get());
    generator.addReactionsFile(createReaction(REACTION_NAME, FOURTH_SEGMENT));
    generator.generate(fsa);

    assertFilesForReaction(fsa, FIRST_SEGMENT, REACTION_NAME);
    assertFilesForReactionWithoutChangePropagationSpecification(fsa, SECOND_SEGMENT, REACTION_NAME);
    assertFilesForReaction(fsa, THIRD_SEGMENT, REACTION_NAME);
    assertFilesForReaction(fsa, FOURTH_SEGMENT, REACTION_NAME);
  }
}
