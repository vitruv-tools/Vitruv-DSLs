package tools.vitruv.dsls.reactions.builder;

import allElementTypes.AllElementTypesPackage;
import allElementTypes2.AllElementTypes2Package;
import java.util.Arrays;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.XNumberLiteral;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.hamcrest.Matcher;

@ExtendWith(InjectionExtension.class)
@InjectWith(ReactionsLanguageInjectorProvider.class)
public abstract class FluentReactionsBuilderTest {
	protected static final EClass Root = AllElementTypesPackage.eINSTANCE.getRoot();
	protected static final EClass NonRoot = AllElementTypesPackage.eINSTANCE.getNonRoot();
	protected static final EClass Root2 = AllElementTypes2Package.eINSTANCE.getRoot2();
	protected static final EClass EObject = EcorePackage.eINSTANCE.getEObject();

	@Inject
	protected GeneratedReactionsMatcherBuilder matcher;
	@Inject
	protected FluentReactionsLanguageBuilder create;

	protected static XAssignment assign(JvmIdentifiableElement feature, XExpression value) {
		XAssignment xAssignment = XbaseFactory.eINSTANCE.createXAssignment();
		xAssignment.setFeature(feature);
		xAssignment.setValue(value);
		return xAssignment;
	}

	protected static XNumberLiteral number(String number) {
		XNumberLiteral numberLiteral = XbaseFactory.eINSTANCE.createXNumberLiteral();
		numberLiteral.setValue(number);
		return numberLiteral;
	}

	protected static XStringLiteral stringLiteral(String value) {
		XStringLiteral stringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
		stringLiteral.setValue(value);
		return stringLiteral;
	}

	protected static XBinaryOperation binaryOperation(
			XExpression leftOperand,
			JvmIdentifiableElement operator,
			XExpression rightOperand
	) {
		XBinaryOperation binaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
		binaryOperation.setLeftOperand(leftOperand);
		binaryOperation.setFeature(operator);
		binaryOperation.setRightOperand(rightOperand);
		return binaryOperation;
	}

	protected static JvmFormalParameter formalParameter(String name) {
		JvmFormalParameter formalParameter = TypesFactory.eINSTANCE.createJvmFormalParameter();
		formalParameter.setName(name);
		return formalParameter;
	}

	protected static XBlockExpression block(XExpression... expressions) {
		XBlockExpression blockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
		EList<XExpression> blockExpressions = blockExpression.getExpressions();
    blockExpressions.addAll(Arrays.asList(expressions));
		return blockExpression;
	}

	protected static XListLiteral xtendListOf(XExpression... expressions) {
		XListLiteral list = XbaseFactory.eINSTANCE.createXListLiteral();
		EList<XExpression> blockExpressions = list.getElements();
    blockExpressions.addAll(Arrays.asList(expressions));
		return list;
	}

	protected static XForLoopExpression forLoop(
			JvmFormalParameter loopParameter,
			XExpression elementSource,
			XExpression body
	) {
		XForLoopExpression forLoop = XbaseFactory.eINSTANCE.createXForLoopExpression();
		forLoop.setDeclaredParam(loopParameter);
		forLoop.setForExpression(elementSource);
		forLoop.setEachExpression(body);
		return forLoop;
	}

	protected Matcher<? super FluentReactionsFileBuilder> builds(String reactionsTest) {
		return matcher.builds(reactionsTest);
	}
}
