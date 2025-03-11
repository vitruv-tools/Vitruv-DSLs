package tools.vitruv.dsls.commonalities.generator.util.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import java.util.HashMap;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * Scopes a single generator execution for one input resource.
 * <p>
 * There can only be a single generation scope active per thread at any given
 * time.
 */
@SuppressWarnings("all")
public final class GenerationScope {
  /**
   * Guice scope handler.
   * <p>
   * Used in combination with the {@link GenerationScoped} annotation.
   */
  public static class GuiceScope implements Scope {
    public static final GenerationScope.GuiceScope INSTANCE = new GenerationScope.GuiceScope();

    private GuiceScope() {
    }

    @Override
    public <T extends Object> Provider<T> scope(final Key<T> key, final Provider<T> unscopedProvider) {
      final Provider<T> _function = () -> {
        final GenerationScope currentScope = GenerationScope.getCurrentScope();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Not within a GenerationScope!");
        Preconditions.checkState((currentScope != null), _builder);
        Object value = currentScope.values.get(key);
        if ((value == null)) {
          value = unscopedProvider.get();
          boolean _containsKey = currentScope.values.containsKey(key);
          if (_containsKey) {
            String _name = value.getClass().getName();
            String _plus = ("Detected cyclic Guice dependency for " + _name);
            throw new IllegalStateException(_plus);
          }
          currentScope.values.put(key, value);
        }
        return ((T) value);
      };
      return _function;
    }
  }

  private static ThreadLocal<GenerationScope> CURRENT_SCOPE = new ThreadLocal<GenerationScope>();

  public static GenerationScope getCurrentScope() {
    return GenerationScope.CURRENT_SCOPE.get();
  }

  private static void setCurrentScope(final GenerationScope scope) {
    if ((scope == null)) {
      GenerationScope.CURRENT_SCOPE.remove();
    } else {
      GenerationScope.CURRENT_SCOPE.set(scope);
    }
  }

  private final HashMap<Key<?>, Object> values = new HashMap<Key<?>, Object>();

  public GenerationScope() {
  }

  public <T extends Object> Object seed(final Key<T> key, final T value) {
    Object _xblockexpression = null;
    {
      boolean _containsKey = this.values.containsKey(key);
      boolean _not = (!_containsKey);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The scope already contains a value for the key ");
      _builder.append(key);
      _builder.append("!");
      Preconditions.checkState(_not, _builder);
      _xblockexpression = this.values.put(key, value);
    }
    return _xblockexpression;
  }

  public <T extends Object> void seed(final Class<T> clazz, final T value) {
    this.<T>seed(Key.<T>get(clazz), value);
  }

  public void enter() {
    GenerationScope _currentScope = GenerationScope.getCurrentScope();
    boolean _tripleEquals = (_currentScope == null);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Already inside a GenerationScope!");
    Preconditions.checkState(_tripleEquals, _builder);
    GenerationScope.setCurrentScope(this);
  }

  public void leave() {
    GenerationScope _currentScope = GenerationScope.getCurrentScope();
    boolean _tripleNotEquals = (_currentScope != null);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Already not within a GenerationScope!");
    Preconditions.checkState(_tripleNotEquals, _builder);
    GenerationScope.setCurrentScope(null);
  }
}
