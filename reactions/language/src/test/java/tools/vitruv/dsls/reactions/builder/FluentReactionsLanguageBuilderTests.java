package tools.vitruv.dsls.reactions.builder;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import allElementTypes.AllElementTypesPackage;
import allElementTypes2.AllElementTypes2Package;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FluentReactionsLanguageBuilderTests extends FluentReactionsBuilderTest {
  @Test
  void createRoot() {
    FluentReactionsFileBuilder builder =
        create
            .reactionsFile("createRootTest")
            .operator_add(
                create
                    .reactionsSegment("simpleChangesRootTests")
                    .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                    .executeActionsIn(AllElementTypesPackage.eINSTANCE)
                    .operator_add(
                        create
                            .reaction("CreateRootTest")
                            .afterElement(Root)
                            .created()
                            .call(
                                routine ->
                                    routine
                                        .create(creates -> creates.vall("newRoot").create(Root))
                                        .update(
                                            updates ->
                                                updates
                                                    .addCorrespondenceBetween("newRoot")
                                                    .and()
                                                    .affectedEObject()))));

    String createRootReactionText = readReactions("CreateRoot.reactions");

    assertThat(builder, builds(createRootReactionText));
  }

  @Test
  void removeRoot() {
    FluentReactionsFileBuilder builder =
        create
            .reactionsFile("deleteRootTest")
            .operator_add(
                create
                    .reactionsSegment("simpleChangesRootTests")
                    .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                    .executeActionsIn(AllElementTypesPackage.eINSTANCE)
                    .operator_add(
                        create
                            .reaction("DeleteRootTest")
                            .afterElement(Root)
                            .deleted()
                            .call(
                                routine ->
                                    routine
                                        .match(
                                            matches ->
                                                matches
                                                    .vall("toDelete")
                                                    .retrieve(Root)
                                                    .correspondingTo()
                                                    .affectedEObject())
                                        .update(updates -> updates.delete("toDelete")))));

    String removeRootReactionText = readReactions("RemoveRoot.reactions");

    assertThat(builder, builds(removeRootReactionText));
  }

  @Test
  void importSegments() {
    FluentReactionsFileBuilder builder = create.reactionsFile("importTest");

    FluentReactionsSegmentBuilder baseSegment =
        create
            .reactionsSegment("baseSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE);
    baseSegment.operator_add(
        create
            .reaction("CreateRootTest")
            .afterElement(Root)
            .created()
            .call(
                routine ->
                    routine
                        .create(creates -> creates.vall("newRoot").create(Root))
                        .update(
                            updates ->
                                updates.addCorrespondenceBetween("newRoot")
                                    .and().affectedEObject())));

    FluentReactionsSegmentBuilder baseSegment2 =
        create
            .reactionsSegment("baseSegment2")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE);
    baseSegment2.operator_add(
        create
            .reaction("DeleteRootTest")
            .afterElement(Root)
            .deleted()
            .call(
                routine ->
                    routine
                        .match(
                            matches ->
                                matches
                                    .vall("toDelete")
                                    .retrieve(Root)
                                    .correspondingTo()
                                    .affectedEObject())
                        .update(updates -> updates.delete("toDelete"))));

    FluentReactionsSegmentBuilder extendedSegment =
        create
            .reactionsSegment("extendedSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .importSegment(baseSegment)
            .usingSimpleRoutineNames()
            .importSegment(baseSegment2)
            .routinesOnly()
            .usingQualifiedRoutineNames();

    builder.operator_add(baseSegment);
    builder.operator_add(baseSegment2);
    builder.operator_add(extendedSegment);

    String importSegmentsReactionText = readReactions("ImportSegments.reactions");

    assertThat(builder, builds(importSegmentsReactionText));
  }

  @Test
  void overrideReaction() {
    FluentReactionsFileBuilder builder = create.reactionsFile("overrideReactionTest");

    FluentReactionsSegmentBuilder baseSegment =
        create
            .reactionsSegment("baseSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE);
    baseSegment.operator_add(
        create
            .reaction("CreateRootTest")
            .afterElement(Root)
            .created()
            .call(
                routine ->
                    routine
                        .create(creates -> creates.vall("newRoot").create(Root))
                        .update(
                            updates ->
                                updates.addCorrespondenceBetween("newRoot")
                                    .and().affectedEObject())));

    FluentReactionsSegmentBuilder extendedSegment =
        create
            .reactionsSegment("extendedSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .importSegment(baseSegment)
            .usingQualifiedRoutineNames();
    extendedSegment.operator_add(
        create
            .reaction("CreateRootTest")
            .overrideSegment(baseSegment)
            .afterElement(Root)
            .created()
            .call(
                routine ->
                    routine
                        .create(creates -> creates.vall("newRootInOverride").create(Root))
                        .update(
                            updates ->
                                updates
                                    .addCorrespondenceBetween("newRootInOverride")
                                    .and()
                                    .affectedEObject())));

    builder.operator_add(baseSegment);
    builder.operator_add(extendedSegment);

    String overrideReactionText = readReactions("OverrideReaction.reactions");

    assertThat(builder, builds(overrideReactionText));
  }

  @Test
  void overrideRoutines() {
    FluentReactionsSegmentBuilder baseSegment =
        create
            .reactionsSegment("baseSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE);
    baseSegment.operator_add(
        create
            .reaction("CreateRootTest")
            .afterElement(Root)
            .created()
            .call(
                routine ->
                    routine
                        .create(creates -> creates.vall("newRoot").create(Root))
                        .update(
                            updates ->
                                updates.addCorrespondenceBetween("newRoot")
                                    .and().affectedEObject())));

    FluentReactionsSegmentBuilder extendedSegment =
        create
            .reactionsSegment("extendedSegment")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .importSegment(baseSegment)
            .usingQualifiedRoutineNames();
    extendedSegment.operator_add(
        create
            .routine("createRootTestRepair") // TODO this is not working yet
            .overrideAlongImportPath(baseSegment)
            .input(inputs -> inputs.model(Root, "affectedEObject"))
            .create(creates -> creates.vall("newRoot2").create(Root))
            .update(
                updates -> updates.addCorrespondenceBetween("newRoot2").and().affectedEObject()));

    FluentReactionsSegmentBuilder extendedSegment2 =
        create
            .reactionsSegment("extendedSegment2")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .importSegment(extendedSegment)
            .usingQualifiedRoutineNames();
    extendedSegment2.operator_add(
        create
            .routine("createRootTestRepair") // TODO this is not working yet
            .overrideAlongImportPath(extendedSegment, baseSegment)
            .input(inputs -> inputs.model(Root, "affectedEObject"))
            .create(creates -> creates.vall("newRoot3").create(Root))
            .update(
                updates -> updates.addCorrespondenceBetween("newRoot3").and().affectedEObject()));

    FluentReactionsFileBuilder builder = create.reactionsFile("overrideRoutinesTest");
    builder.operator_add(baseSegment);
    builder.operator_add(extendedSegment);
    builder.operator_add(extendedSegment2);

    String overrideRoutinesReactionText = readReactions("OverrideRoutines.reactions");

    assertThat(builder, builds(overrideRoutinesReactionText));
  }

  @Test
  void noEmptyReactionsFile() {
    FluentReactionsFileBuilder builder = create.reactionsFile("empty");
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> matcher.build(builder));
    assertThat(exception.getMessage(), containsString("No reactions segments"));
  }

  @Test
  void noEmptyReactionsSegment() {
    FluentReactionsFileBuilder builder =
        create
            .reactionsFile("Test")
            .operator_add(
                create
                    .reactionsSegment("empty")
                    .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                    .executeActionsIn(AllElementTypesPackage.eINSTANCE));
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> matcher.build(builder));
    assertThat(
        exception.getMessage(), containsString("Neither routines, nor reactions, nor imports"));
  }

  @Test
  void routineArgument() {
    FluentReactionsFileBuilder builder =
        create
            .reactionsFile("createRootTest")
            .operator_add(
                create
                    .reactionsSegment("simpleChangesRootTests")
                    .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                    .executeActionsIn(AllElementTypesPackage.eINSTANCE)
                    .operator_add(
                        create
                            .routine("withArguments")
                            .input(
                                inputs -> {
                                  inputs.model(Root, "rootParameter");
                                  inputs.model(NonRoot, "nonRootParameter");
                                })
                            .update(
                                updates ->
                                    updates
                                        .addCorrespondenceBetween("rootParameter")
                                        .and("nonRootParameter"))));

    String routineArgumentReactionText = readReactions("RoutineArgument.reactions");

    assertThat(builder, builds(routineArgumentReactionText));
  }

  @Test
  void routineForTwoReactionsImplicitlyAdded() {
    FluentRoutineBuilder commonRoutine = commonRootCreateRoutine();

    FluentReactionsFileBuilder reactionsFile = create.reactionsFile("createRootTest");
    FluentReactionsSegmentBuilder reactionsSegment =
        create
            .reactionsSegment("simpleChangesRootTests")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                new FluentReactionBuilder[] {
                  create
                      .reaction("CreateRootTest")
                      .afterElement(Root)
                      .created()
                      .call(commonRoutine),
                  create
                      .reaction("CreateNonRootTest")
                      .afterElement(NonRoot)
                      .created()
                      .call(commonRoutine)
                });

    reactionsFile.operator_add(reactionsSegment);

    String routineForTwoReactionsImplicitlyAddedReactionText =
        readReactions("RoutineForTwoReactionsImplicitlyAdded.reactions");

    assertThat(reactionsFile, builds(routineForTwoReactionsImplicitlyAddedReactionText));
  }

  @Test
  void routineForTwoReactionsExplicitlyAdded() {
    FluentRoutineBuilder commonRoutine = commonRootCreateRoutine();
    FluentReactionsSegmentBuilder reactionsSegment =
        create
            .reactionsSegment("simpleChangesRootTests")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE);

    reactionsSegment.operator_add(commonRoutine);
    reactionsSegment.operator_add(
        create.reaction("CreateRootTest").afterElement(Root).created().call(commonRoutine));
    reactionsSegment.operator_add(
        create.reaction("CreateNonRootTest").afterElement(NonRoot).created().call(commonRoutine));

    FluentReactionsFileBuilder reactionsFile = create.reactionsFile("createRootTest");
    reactionsFile.operator_add(reactionsSegment);

    String routineForTwoReactionsExplicitlyAddedReactionText =
        readReactions("RoutineForTwoReactionsExplicitlyAdded.reactions");

    assertThat(reactionsFile, builds(routineForTwoReactionsExplicitlyAddedReactionText));
  }

  @Test
  void routineWithMatch() {
    FluentRoutineBuilder routineWithMatch =
        create
            .routine("withMatch")
            .match(
                matches ->
                    matches.check(
                        typeProvider ->
                            binaryOperation(
                                stringLiteral("test"),
                                typeProvider.findMethod(
                                    ObjectExtensions.class, "operator_tripleEquals"),
                                stringLiteral("test"))))
            .create(creates -> creates.vall("newRoot").create(Root))
            .withoutUpdate();

    FluentReactionsFileBuilder reactionsFile =
        create
            .reactionsFile("routineWithMatchTest")
            .operator_add(
                create
                    .reactionsSegment("routineWithMatchTest")
                    .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                    .executeActionsIn(AllElementTypesPackage.eINSTANCE)
                    .operator_add(routineWithMatch));

    String routineWithMatchReactionText =
        readReactions("RoutineWithMatch.reactions")
            .formatted(ObjectExtensions.class.getName());
    assertThat(reactionsFile, builds(routineWithMatchReactionText));
  }

  @Test
  void routineWithAffectedEObjectInMatch() {
    FluentRoutineBuilder routineWithMatch =
        create
            .routine("withMatch")
            .match(
                matches ->
                    matches.check(
                        typeProvider ->
                            binaryOperation(
                                stringLiteral("test"),
                                typeProvider.findMethod(
                                    ObjectExtensions.class, "operator_tripleEquals"),
                                typeProvider.affectedEObject())))
            .create(creates -> creates.vall("newRoot").create(Root))
            .update(
                updates -> updates.addCorrespondenceBetween("newRoot").and().affectedEObject());

    FluentReactionBuilder reaction =
        create.reaction("CreateRoot").afterElement(Root).created().call(routineWithMatch);

    FluentReactionsSegmentBuilder segment =
        create
            .reactionsSegment("routineWithMatchTest")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(reaction);
    segment.operator_add(routineWithMatch);

    FluentReactionsFileBuilder reactionsFile =
        create.reactionsFile("routineWithMatchTest").operator_add(segment);

    String routineWithAffectedEObjectInMatchReactionText =
        readReactions("RoutineWithAffectedEObjectInMatch.reactions")
            .formatted(ObjectExtensions.class.getName());
    assertThat(reactionsFile, builds(routineWithAffectedEObjectInMatchReactionText));
  }

  @Test
  @Disabled("not supported from text right now")
  void routineFromDifferentSegmentWithImplicitSegment() {
    FluentRoutineBuilder commonRoutine = commonRootCreateRoutine();

    FluentReactionsFileBuilder reactionsFile = create.reactionsFile("createRootTest");

    reactionsFile.operator_add(
        create
            .reactionsSegment("simpleChangesRootTests")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                create
                    .reaction("CreateRootTest")
                    .afterElement(Root)
                    .created()
                    .call(commonRoutine)));

    reactionsFile.operator_add(
        create
            .reactionsSegment("simpleChangesRoot2Tests")
            .inReactionToChangesIn(AllElementTypes2Package.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                create
                    .reaction("CreateRoot2Test")
                    .afterElement(Root2)
                    .created()
                    .call(commonRoutine)));

    String routineFromDifferentSegmentWithImplicitSegmentReactionText =
        readReactions("RoutineFromDifferentSegmentWithImplicitSegment.reactions");

    assertThat(reactionsFile, builds(routineFromDifferentSegmentWithImplicitSegmentReactionText));
  }

  @Test
  @Disabled("not supported from text right now")
  void routineFromDifferentSegmentWithExplicitSegment() {
    FluentRoutineBuilder commonRoutine = commonRootCreateRoutine();

    FluentReactionsFileBuilder reactionsFile = create.reactionsFile("createRootTest");

    reactionsFile.operator_add(
        create
            .reactionsSegment("simpleChangesRootTests")
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                create
                    .reaction("CreateRootTest")
                    .afterElement(Root)
                    .created()
                    .call(commonRoutine)));

    FluentReactionsSegmentBuilder secondSegment =
        create
            .reactionsSegment("simpleChangesRoot2Tests")
            .inReactionToChangesIn(AllElementTypes2Package.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            .operator_add(
                create
                    .reaction("CreateRoot2Test")
                    .afterElement(Root2)
                    .created()
                    .call(commonRoutine));

    secondSegment.operator_add(commonRoutine);

    reactionsFile.operator_add(secondSegment);

    String routineFromDifferentSegmentWithExplicitSegmentReactionText =
        readReactions("RoutineFromDifferentSegmentWithExplicitSegment.reactions");

    assertThat(reactionsFile, builds(routineFromDifferentSegmentWithExplicitSegmentReactionText));
  }

  private FluentRoutineBuilder commonRootCreateRoutine() {
    return create
        .routine("commonRootCreate")
        .input(inputs -> inputs.model(EObject, "affectedEObject"))
        .create(creates -> creates.vall("newRoot").create(Root))
        .update(updates -> updates.addCorrespondenceBetween("newRoot").and().affectedEObject());
  }
}
