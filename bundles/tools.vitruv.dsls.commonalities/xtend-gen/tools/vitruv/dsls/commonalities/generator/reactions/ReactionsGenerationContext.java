package tools.vitruv.dsls.commonalities.generator.reactions;

import javax.inject.Inject;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.reactions.builder.FluentReactionsLanguageBuilder;

@GenerationScoped
@SuppressWarnings("all")
public class ReactionsGenerationContext {
  @Accessors(AccessorType.PUBLIC_GETTER)
  @Inject
  private FluentReactionsLanguageBuilder create;

  @Pure
  public FluentReactionsLanguageBuilder getCreate() {
    return this.create;
  }
}
