package tools.vitruv.dsls.reactions.builder;

import allElementTypes.AllElementTypesPackage;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@SuppressWarnings("all")
class ComplexRoutineLogicTests extends FluentReactionsBuilderTest {
  @Test
  void testCase() {
    FluentReactionsFileBuilder reactionsFile = this.create.reactionsFile("createRootTest");
    FluentReactionsSegmentBuilder _executeActionsIn = this.create.
        reactionsSegment("simpleChangesRootTests")
        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
        .executeActionsIn(AllElementTypesPackage.eINSTANCE);

    final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function = (FluentRoutineBuilder.RoutineStartBuilder it) -> {
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_1 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> updateBody = (TypeProvider provider) -> {
          JvmFormalParameter loopVariable = formalParameter("b");
          XForLoopExpression forLoopExpression = forLoop(
              loopVariable,
              xtendListOf(number("10")),
              block(
                  assign(loopVariable, number("20"))
              )
          );
          return forLoopExpression;

        };
        it_1.execute(updateBody);
      };
      it.update(_function_1);
    };
    FluentReactionBuilder _call = this.create.reaction("CreateRootTest").afterElement(FluentReactionsBuilderTest.Root).created().call(_function);
    FluentReactionsSegmentBuilder _add = _executeActionsIn.operator_add(_call);
    final FluentReactionsFileBuilder builder = reactionsFile.operator_add(_add);

    String reactionResult = """
        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
        
        reactions: simpleChangesRootTests in reaction to changes in allElementTypes
        execute actions in allElementTypes
  
        reaction CreateRootTest {
          after element allElementTypes::Root created
          call createRootTestRepair()
        }
  
        routine createRootTestRepair() {
          update {
            for ( b : # [ 10 ] ) { b = 20 }	
          }
        }
        """;

    MatcherAssert.<FluentReactionsFileBuilder>assertThat(builder, this.builds(reactionResult));
  }

}
