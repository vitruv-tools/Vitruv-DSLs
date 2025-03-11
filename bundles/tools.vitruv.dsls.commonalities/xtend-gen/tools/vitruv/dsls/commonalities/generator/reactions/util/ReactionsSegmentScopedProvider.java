package tools.vitruv.dsls.commonalities.generator.reactions.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;

/**
 * Base class for providers which create one object per reactions segment
 * builder and then keep returning that same object in subsequent requests.
 */
@GenerationScoped
@SuppressWarnings("all")
public abstract class ReactionsSegmentScopedProvider<T extends Object> extends InjectingFactoryBase {
  private final Map<FluentReactionsSegmentBuilder, T> bySegment = new HashMap<FluentReactionsSegmentBuilder, T>();

  public T getFor(final FluentReactionsSegmentBuilder segment) {
    final Function<FluentReactionsSegmentBuilder, T> _function = (FluentReactionsSegmentBuilder it) -> {
      return this.createFor(segment);
    };
    return this.bySegment.computeIfAbsent(segment, _function);
  }

  protected abstract T createFor(final FluentReactionsSegmentBuilder segment);
}
