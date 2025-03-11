package tools.vitruv.dsls.commonalities.generator;

import org.eclipse.xtext.generator.IGenerator2;

/**
 * A generator invoked by the {@link CommonalitiesLanguageGenerator}. Unlike {@link IGenerator2},
 * sub generators do not receive arguments through method parameters, but rather by running in
 * the generation scope and having {@link GenerationContext} injected.
 */
@SuppressWarnings("all")
public interface SubGenerator {
  /**
   * @see IGenerator2#beforeGenerate
   */
  default void beforeGenerate() {
  }

  /**
   * @see IGenerator2#doGenerate
   */
  void generate();

  /**
   * @see IGenerator2#afterGenerate
   */
  default void afterGenerate() {
  }
}
