package tools.vitruv.dsls.reactions.builder;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;

import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME;

abstract class FluentReactionsSegmentChildBuilder extends FluentReactionElementBuilder {

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private FluentReactionsSegmentBuilder segmentBuilder;

	protected FluentReactionsSegmentChildBuilder(FluentBuilderContext context) {
		super(context);
	}

	@Override
	public boolean willGenerateCode() {
		return true;
	}

	protected void transferReactionsSegmentTo(FluentReactionsSegmentChildBuilder infector,
		FluentReactionsSegmentChildBuilder infected) {
		beforeAttached(infector, infectorX -> infect(infector, infected));
		beforeAttached(infected, infectedX -> checkReactionsSegmentIsCompatibleTo(infector, infected));
	}

	private void infect(FluentReactionsSegmentChildBuilder infector, FluentReactionsSegmentChildBuilder infected) {
		checkReactionsSegmentIsCompatibleTo(infected, infected);
		if (infector.segmentBuilder != null && infected.segmentBuilder == null) {
			infector.segmentBuilder.add(infected);
		}
	}

	private void checkReactionsSegmentIsCompatibleTo(FluentReactionsSegmentChildBuilder a,
		FluentReactionsSegmentChildBuilder b) {
		if (a.getAttachedReactionsFile() != null && b.getAttachedReactionsFile() != null &&
			!a.getAttachedReactionsFile().equals(b.getAttachedReactionsFile())) {
			throw new RuntimeException(a + "is in a different reaction file than" + b + "!");
		}
	}

	protected abstract String getCreatedElementName();

	protected abstract String getCreatedElementType();

	protected JvmFormalParameter correspondingMethodParameter(XExpression correpondingExpression, String parameterName) {
		JvmOperation method = getCorrespondingMethod(correpondingExpression);
		Optional<JvmFormalParameter> parameter =
				method.getParameters()
						.stream()
						.filter(param -> param.getName().equals(parameterName))
						.findFirst();
		if (parameter.isEmpty()) {
			// most likely an error by the client
			throw new IllegalStateException("Could not find the variable or parameter " + parameterName + " in the " + getCreatedElementType() + " " + getCreatedElementName());
		}
		return parameter.get();
	}

	protected JvmOperation getCorrespondingMethod(XExpression correpondingExpression) {
		// Takes parent containers into account in order to work in both routine and reaction contexts:
		JvmIdentifiableElement element = context.getJvmModelAssociator().getNearestLogicalContainer(correpondingExpression);
		if (element instanceof JvmOperation method) {
			return method;
		}
		throw new IllegalStateException("Could not find the method corresponding to " + correpondingExpression + " in the " + getCreatedElementType() + " " + getCreatedElementName());
	}

	protected static List<XExpression> extractExpressions(XExpression expression) {
		if (expression == null) {
			return List.of();
		}
		if (expression instanceof XBlockExpression block) {
			return block.getExpressions();
		}
		return List.of(expression);

	}

	protected XFeatureCall featureCall(JvmIdentifiableElement element) {
		if (element == null) {
			return null;
		}
		XFeatureCall featureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
		featureCall.setFeature(element);
		return featureCall;
	}

	protected TypeProvider getTypeProvider(XExpression scopeExpression) {
		return new TypeProvider(delegateTypeProvider(), referenceBuilderFactory(), this, scopeExpression);
	}

	public XFeatureCall getJvmOperationRoutineFacade(XExpression codeBlock) {
		return this.featureCall(this.correspondingMethodParameter(codeBlock, ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME));
	}
}
