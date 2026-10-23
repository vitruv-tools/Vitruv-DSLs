package tools.vitruv.dsls.reactions.codegen.helper;

import java.sql.Ref;
import java.util.Optional;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XBlockExpression;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import org.eclipse.emf.ecore.resource.Resource;
import static com.google.common.base.Preconditions.*;
import tools.vitruv.dsls.reactions.api.generator.ReferenceClassNameAdapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EClassifier;
import edu.kit.ipd.sdq.activextendannotations.Utility;

@Utility
public final class ReactionsLanguageHelper {
	private ReactionsLanguageHelper() {
	}
	
	public static String getXBlockExpressionText(XExpression expression) {
		if (expression instanceof XBlockExpression blockExpression) {
			return NodeModelUtils.getNode(blockExpression).getText();
		}
		return NodeModelUtils.getNode(expression).getText();
	}

	private static Optional<ReferenceClassNameAdapter>
		getOptionalReferenceAdapter(EObject element) {
		return element.eAdapters().stream()
				.filter(e -> e.isAdapterForType(ReferenceClassNameAdapter.class))
				.map(e -> (ReferenceClassNameAdapter) e)
				.findFirst();
	}

	public static String getJavaClassName(EClassifier eClassifier) {
		Optional<ReferenceClassNameAdapter> adapter = getOptionalReferenceAdapter(eClassifier);
		if (adapter.isPresent() && adapter.get().getQualifiedNameForReference() != null) {
      return adapter.get().getQualifiedNameForReference();
    }
		return eClassifier.getInstanceClassName();
	}

	public static String getRuntimeClassName(EObject element) {
		Optional<ReferenceClassNameAdapter> adapter = getOptionalReferenceAdapter(element);
		if (adapter.isPresent() && adapter.get().getQualifiedNameForReference() != null) {
			return adapter.get().getQualifiedNameForReference();
		}
		return element.getClass().getCanonicalName();
	}

	public static String getJavaClassName(MetaclassReference metaclassReference) {
		if (metaclassReference.getMetaclass() != null) {
			return getJavaClassName(metaclassReference.getMetaclass());
		}
		return null;
	}
	public static ReactionsFile getReactionsFile(Resource resource) {
		EObject firstContentElement = null;
		if (resource != null && resource.getContents() != null) {
			var contents = resource.getContents();
			if (!contents.isEmpty()) {
				firstContentElement = contents.getFirst();
			}
		}
		checkArgument(firstContentElement instanceof ReactionsFile,
				"The given resource %s was expected to contain a ReactionsFile element! (was %s)",
				resource,
				firstContentElement != null ? firstContentElement.getClass().getSimpleName() : "null");
		return (ReactionsFile) firstContentElement;
	}
	
	public static ReactionsFile getOptionalReactionsFile(Resource resource) {
		EObject firstContentElement = null;
		if (resource != null && resource.getContents() != null) {
			var contents = resource.getContents();
			if (!contents.isEmpty()) {
				firstContentElement = contents.getFirst();
			}
		}
		if (firstContentElement instanceof ReactionsFile) {
			return (ReactionsFile) firstContentElement;
		}
		return null;
	}
	
	public static boolean containsReactionsFile(Resource resource) {
		return getOptionalReactionsFile(resource) != null;
	}
	
}
