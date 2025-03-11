package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Preconditions;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.IScope;

/**
 * Queries for objects for a given name are prefixed with the specified name.
 * The prefix is removed from the name of results.
 */
@SuppressWarnings("all")
public class PrefixedScope extends NameTransformingScope {
  private final QualifiedName prefix;

  public PrefixedScope(final IScope delegate, final QualifiedName prefix) {
    super(delegate);
    this.prefix = Preconditions.<QualifiedName>checkNotNull(prefix, "prefix was null!");
  }

  @Override
  protected QualifiedName transformQuery(final QualifiedName name) {
    return this.prefix.append(name);
  }

  @Override
  protected QualifiedName transformResult(final QualifiedName name) {
    QualifiedName _xifexpression = null;
    boolean _startsWith = name.startsWith(this.prefix);
    if (_startsWith) {
      _xifexpression = name.skipFirst(this.prefix.getSegmentCount());
    } else {
      _xifexpression = name;
    }
    return _xifexpression;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("prefixed with ‹");
    _builder.append(this.prefix);
    _builder.append("›: ");
    _builder.append(this.delegate);
    return _builder.toString();
  }
}
