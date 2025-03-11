package tools.vitruv.dsls.reactions.builder;

import allElementTypes.AllElementTypesPackage;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.XNumberLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
@SuppressWarnings("all")
public class ComplexRoutineLogicTests extends FluentReactionsBuilderTest {
  @Test
  public void ts() {
    FluentReactionsFileBuilder _reactionsFile = this.create.reactionsFile("createRootTest");
    FluentReactionsSegmentBuilder _executeActionsIn = this.create.reactionsSegment("simpleChangesRootTests").inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).executeActionsIn(AllElementTypesPackage.eINSTANCE);
    final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function = (FluentRoutineBuilder.RoutineStartBuilder it) -> {
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_1 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider provider) -> {
          XForLoopExpression _xblockexpression = null;
          {
            JvmFormalParameter _createJvmFormalParameter = TypesFactory.eINSTANCE.createJvmFormalParameter();
            final Procedure1<JvmFormalParameter> _function_3 = (JvmFormalParameter it_2) -> {
              it_2.setName("b");
            };
            final JvmFormalParameter loopVariable = ObjectExtensions.<JvmFormalParameter>operator_doubleArrow(_createJvmFormalParameter, _function_3);
            XForLoopExpression _createXForLoopExpression = XbaseFactory.eINSTANCE.createXForLoopExpression();
            final Procedure1<XForLoopExpression> _function_4 = (XForLoopExpression it_2) -> {
              it_2.setDeclaredParam(loopVariable);
              XListLiteral _createXListLiteral = XbaseFactory.eINSTANCE.createXListLiteral();
              final Procedure1<XListLiteral> _function_5 = (XListLiteral it_3) -> {
                EList<XExpression> _elements = it_3.getElements();
                XNumberLiteral _createXNumberLiteral = XbaseFactory.eINSTANCE.createXNumberLiteral();
                final Procedure1<XNumberLiteral> _function_6 = (XNumberLiteral it_4) -> {
                  it_4.setValue("10");
                };
                XNumberLiteral _doubleArrow = ObjectExtensions.<XNumberLiteral>operator_doubleArrow(_createXNumberLiteral, _function_6);
                _elements.add(_doubleArrow);
              };
              XListLiteral _doubleArrow = ObjectExtensions.<XListLiteral>operator_doubleArrow(_createXListLiteral, _function_5);
              it_2.setForExpression(_doubleArrow);
              XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
              final Procedure1<XBlockExpression> _function_6 = (XBlockExpression it_3) -> {
                EList<XExpression> _expressions = it_3.getExpressions();
                XAssignment _createXAssignment = XbaseFactory.eINSTANCE.createXAssignment();
                final Procedure1<XAssignment> _function_7 = (XAssignment it_4) -> {
                  it_4.setFeature(loopVariable);
                  XNumberLiteral _createXNumberLiteral = XbaseFactory.eINSTANCE.createXNumberLiteral();
                  final Procedure1<XNumberLiteral> _function_8 = (XNumberLiteral it_5) -> {
                    it_5.setValue("20");
                  };
                  XNumberLiteral _doubleArrow_1 = ObjectExtensions.<XNumberLiteral>operator_doubleArrow(_createXNumberLiteral, _function_8);
                  it_4.setValue(_doubleArrow_1);
                };
                XAssignment _doubleArrow_1 = ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment, _function_7);
                _expressions.add(_doubleArrow_1);
              };
              XBlockExpression _doubleArrow_1 = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_6);
              it_2.setEachExpression(_doubleArrow_1);
            };
            _xblockexpression = ObjectExtensions.<XForLoopExpression>operator_doubleArrow(_createXForLoopExpression, _function_4);
          }
          return _xblockexpression;
        };
        it_1.execute(_function_2);
      };
      it.update(_function_1);
    };
    FluentReactionBuilder _call = this.create.reaction("CreateRootTest").afterElement(FluentReactionsBuilderTest.Root).created().call(_function);
    FluentReactionsSegmentBuilder _add = _executeActionsIn.operator_add(_call);
    final FluentReactionsFileBuilder builder = _reactionsFile.operator_add(_add);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as allElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("reactions: simpleChangesRootTests in reaction to changes in allElementTypes");
    _builder.newLine();
    _builder.append("execute actions in allElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("reaction CreateRootTest {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("after element allElementTypes::Root created");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("call createRootTestRepair()");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("routine createRootTestRepair() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("update {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("for ( b : # [ 10 ] ) { b = 20 }\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String reactionResult = _builder.toString();
    MatcherAssert.<FluentReactionsFileBuilder>assertThat(builder, this.builds(reactionResult));
  }
}
