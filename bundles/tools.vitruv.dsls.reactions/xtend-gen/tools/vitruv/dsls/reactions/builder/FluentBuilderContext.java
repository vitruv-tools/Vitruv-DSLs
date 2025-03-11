package tools.vitruv.dsls.reactions.builder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * Provides access to “the outside” to
 * {@link FluentReactionElementBuilder fluent builders}.
 */
@Singleton
@SuppressWarnings("all")
class FluentBuilderContext {
  @Accessors(AccessorType.PACKAGE_GETTER)
  @Inject
  private JvmModelAssociator jvmModelAssociator;

  @Accessors(AccessorType.PACKAGE_GETTER)
  @Inject
  private TypeReferences typeReferences;

  @Accessors(AccessorType.PACKAGE_GETTER)
  @Inject
  private IJvmTypeProvider.Factory typeProviderFactory;

  @Accessors(AccessorType.PACKAGE_GETTER)
  @Inject
  private JvmTypeReferenceBuilder.Factory referenceBuilderFactory;

  @Pure
  JvmModelAssociator getJvmModelAssociator() {
    return this.jvmModelAssociator;
  }

  @Pure
  TypeReferences getTypeReferences() {
    return this.typeReferences;
  }

  @Pure
  IJvmTypeProvider.Factory getTypeProviderFactory() {
    return this.typeProviderFactory;
  }

  @Pure
  JvmTypeReferenceBuilder.Factory getReferenceBuilderFactory() {
    return this.referenceBuilderFactory;
  }
}
