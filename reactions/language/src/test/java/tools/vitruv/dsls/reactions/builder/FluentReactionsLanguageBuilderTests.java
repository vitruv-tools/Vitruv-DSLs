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

    String reactionResult =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair(affectedEObject)
        }

        routine createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }
        """;

    assertThat(builder, builds(reactionResult));
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

    String reactionResult =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction DeleteRootTest {
          after element allElementTypes::Root deleted
          call deleteRootTestRepair(affectedEObject)
        }

        routine deleteRootTestRepair(allElementTypes::Root affectedEObject) {
          match {
            val toDelete = retrieve allElementTypes::Root corresponding to affectedEObject
          }
          update {
            removeObject(toDelete)
          }
        }
        """;

    assertThat(builder, builds(reactionResult));
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
                                updates.addCorrespondenceBetween("newRoot").and().affectedEObject())));

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

    String reactionResult =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: baseSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair(affectedEObject)
        }

        routine createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }



        reactions: baseSegment2
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction DeleteRootTest {
          after element allElementTypes::Root deleted
          call deleteRootTestRepair(affectedEObject)
        }

        routine deleteRootTestRepair(allElementTypes::Root affectedEObject) {
          match {
            val toDelete = retrieve allElementTypes::Root corresponding to affectedEObject
          }
          update {
            removeObject(toDelete)
          }
        }



        reactions: extendedSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        import baseSegment
        import routines baseSegment2 using qualified names
        """;

    assertThat(builder, builds(reactionResult));
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
                                updates.addCorrespondenceBetween("newRoot").and().affectedEObject())));

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

    String reactionResult =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: baseSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair(affectedEObject)
        }

        routine createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }



        reactions: extendedSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        import baseSegment using qualified names

        reaction baseSegment::CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair(affectedEObject)
        }

        routine createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRootInOverride = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRootInOverride, affectedEObject)
          }
        }
        """;

    assertThat(builder, builds(reactionResult));
  }

  @Test
  void overrideRoutines() {
    FluentReactionsFileBuilder builder = create.reactionsFile("overrideRoutinesTest");

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
                                updates.addCorrespondenceBetween("newRoot").and().affectedEObject())));

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

    builder.operator_add(baseSegment);
    builder.operator_add(extendedSegment);
    builder.operator_add(extendedSegment2);

    String reactionResult =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: baseSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair(affectedEObject)
        }

        routine createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }



        reactions: extendedSegment
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        import baseSegment using qualified names

        routine baseSegment::createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot2 = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot2, affectedEObject)
          }
        }



        reactions: extendedSegment2
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        import extendedSegment using qualified names

        routine extendedSegment.baseSegment::createRootTestRepair(allElementTypes::Root affectedEObject) {
          create {
            val newRoot3 = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot3, affectedEObject)
          }
        }
        """;

    assertThat(builder, builds(reactionResult));
  }

  @Test
  void noEmptyReactionsFile() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              FluentReactionsFileBuilder builder = create.reactionsFile("empty");
              matcher.build(builder);
            });
    assertThat(exception.getMessage(), containsString("No reactions segments"));
  }

  @Test
  void noEmptyReactionsSegment() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              FluentReactionsFileBuilder builder =
                  create
                      .reactionsFile("Test")
                      .operator_add(
                          create
                              .reactionsSegment("empty")
                              .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                              .executeActionsIn(AllElementTypesPackage.eINSTANCE));
              matcher.build(builder);
            });
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

    String expectedReaction =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        routine withArguments(allElementTypes::Root rootParameter, allElementTypes::NonRoot nonRootParameter) {
          update {
            addCorrespondenceBetween(rootParameter, nonRootParameter)
          }
        }
        """;

    assertThat(builder, builds(expectedReaction));
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
                  create.reaction("CreateRootTest").afterElement(Root).created().call(commonRoutine),
                  create
                      .reaction("CreateNonRootTest")
                      .afterElement(NonRoot)
                      .created()
                      .call(commonRoutine)
                });

    reactionsFile.operator_add(reactionsSegment);

    String expectedReaction =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
        import "http://www.eclipse.org/emf/2002/Ecore" as ecore

        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call commonRootCreate(affectedEObject)
        }

        reaction CreateNonRootTest {
          after element allElementTypes::NonRoot created
          call commonRootCreate(affectedEObject)
        }

        routine commonRootCreate(ecore::EObject affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }
        """;

    assertThat(reactionsFile, builds(expectedReaction));
  }

  @Test
  void routineForTwoReactionsExplicitlyAdded() {
    FluentRoutineBuilder commonRoutine = commonRootCreateRoutine();

    FluentReactionsFileBuilder reactionsFile = create.reactionsFile("createRootTest");
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

    reactionsFile.operator_add(reactionsSegment);

    String expectedReaction =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://www.eclipse.org/emf/2002/Ecore" as ecore
        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call commonRootCreate(affectedEObject)
        }

        reaction CreateNonRootTest {
          after element allElementTypes::NonRoot created
          call commonRootCreate(affectedEObject)
        }

        routine commonRootCreate(ecore::EObject affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }
        """;

    assertThat(reactionsFile, builds(expectedReaction));
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

    String expectedReaction =
        """
        import %s

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: routineWithMatchTest
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        routine withMatch() {
          match {
            check {
              "test" === "test"
            }
          }
          create {
            val newRoot = new allElementTypes::Root
          }
        }
        """
            .formatted(ObjectExtensions.class.getName());
    assertThat(reactionsFile, builds(expectedReaction));
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

    String expectedReaction =
        """
        import %s
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

        reactions: routineWithMatchTest
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRoot {
          after element allElementTypes::Root created
          call withMatch(affectedEObject)
        }

        routine withMatch(allElementTypes::Root affectedEObject) {
          match {
            check {
              "test" === affectedEObject
            }
          }
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }
        """
            .formatted(ObjectExtensions.class.getName());
    assertThat(reactionsFile, builds(expectedReaction));
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
                create.reaction("CreateRootTest").afterElement(Root).created().call(commonRoutine)));

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

    String expectedReaction =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
        import "http://www.eclipse.org/emf/2002/Ecore" as ecore
        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes2" as allElementTypes2


        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call commonRootCreate(affectedEObject)
        }

        routine commonRootCreate(ecore::EObject affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }


        reactions: simpleChangesRoot2Tests
        in reaction to changes in allElementTypes2
        execute actions in allElementTypes

        reaction CreateRoot2Test {
          after element allElementTypes2::Root2 created
          call commonRootCreate(affectedEObject)
        }
        """;

    assertThat(reactionsFile, builds(expectedReaction));
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
                create.reaction("CreateRootTest").afterElement(Root).created().call(commonRoutine)));

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

    String expectedReaction =
        """
        import tools.vitruv.dsls.^reactions.runtime.^routines.AbstractRoutine.Update

        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes2" as allElementTypes2
        import "http://www.eclipse.org/emf/2002/Ecore" as ecore


        reactions: simpleChangesRootTests
        in reaction to changes in allElementTypes
        execute actions in allElementTypes

        reaction CreateRootTest {
          after element allElementTypes::Root created
          call commonRootCreate(affectedEObject)
        }


        reactions: simpleChangesRoot2Tests
        in reaction to changes in allElementTypes2
        execute actions in allElementTypes

        reaction CreateRoot2Test {
          after element allElementTypes2::Root2 created
          call commonRootCreate(affectedEObject)
        }

        routine commonRootCreate(ecore::EObject affectedEObject) {
          create {
            val newRoot = new allElementTypes::Root
          }
          update {
            addCorrespondenceBetween(newRoot, affectedEObject)
          }
        }
        """;

    assertThat(reactionsFile, builds(expectedReaction));
  }

  private FluentRoutineBuilder commonRootCreateRoutine() {
    return create
        .routine("commonRootCreate")
        .input(inputs -> inputs.model(EObject, "affectedEObject"))
        .create(creates -> creates.vall("newRoot").create(Root))
        .update(updates -> updates.addCorrespondenceBetween("newRoot").and().affectedEObject());
  }
}
