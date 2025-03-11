package tools.vitruv.dsls.commonalities.generator.reactions.helper;

import com.google.inject.Inject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGenerationContext;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;

/**
 * Base for helper classes that need to be aware of the current reactions
 * generation context.
 */
@GenerationScoped
@SuppressWarnings("all")
public abstract class ReactionsGenerationHelper {
  @Inject
  @Extension
  protected GenerationContext _generationContext;

  @Inject
  @Extension
  protected ReactionsGenerationContext _reactionsGenerationContext;
}
