package tools.vitruv.dsls.reactions.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;

/**
 * Provides access to “the outside” to 
 * {@link FluentReactionElementBuilder} fluent builders.
 */
@Singleton
class FluentBuilderContext {
	/**
	 * The associator is a singleton, which is why we can have it injected
	 * once and for all.
	 */
	@Getter(AccessLevel.PACKAGE)
	@Inject
	private JvmModelAssociator jvmModelAssociator;

	/**
	 * TypeReferences also is a singleton.
	 */
	@Getter(AccessLevel.PACKAGE)
	@Inject
	private TypeReferences typeReferences;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private JvmTypeReferenceBuilder.Factory referenceBuilderFactory;
}