package tools.vitruv.dsls.reactions.validation;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.validation.Issue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

/**
 * Tests the validation that warns about routines that are never called.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
class RoutineCallersValidationTest {
  @Inject
  private ParseHelper<ReactionsFile> parseHelper;
  @Inject
  private ValidationTestHelper validationHelper;

  @Test
  void warnsOnlyAboutRoutinesThatAreNeverCalled() throws Exception {
    ReactionsFile reactionsFile = parseHelper.parse("""
        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as minimal

        reactions: routineCallersTest
        in reaction to changes in minimal
        execute actions in minimal

        reaction RootCreated {
          after element minimal::Root created
          call calledRoutine()
        }

        routine calledRoutine() {
          update {
            calledFromRoutine()
          }
        }

        routine calledFromRoutine() {
          update {
          }
        }

        routine uncalledRoutine() {
          update {
          }
        }
        """);

    List<String> warnings = validationHelper.validate(reactionsFile).stream()
        .map(Issue::getMessage)
        .filter(message -> message.endsWith("is never called."))
        .toList();

    assertThat(warnings, hasItem("Routine 'uncalledRoutine' is never called."));
    assertThat(warnings, not(hasItem("Routine 'calledRoutine' is never called.")));
    assertThat(warnings, not(hasItem("Routine 'calledFromRoutine' is never called.")));
    validationHelper.assertWarning(
        reactionsFile,
        TopLevelElementsPackage.Literals.ROUTINE,
        null,
        "Routine 'uncalledRoutine' is never called.");
  }
}
