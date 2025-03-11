package tools.vitruv.dsls.reactions.runtime.structure;

import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class CallHierarchyHaving extends Loggable {
  private final CallHierarchyHaving calledBy;

  public CallHierarchyHaving() {
    this.calledBy = null;
  }

  public CallHierarchyHaving(final CallHierarchyHaving calledBy) {
    this.calledBy = calledBy;
  }

  public CallHierarchyHaving getCalledBy() {
    return this.calledBy;
  }

  public String getCalledByString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(");
    String _simpleName = this.getClass().getSimpleName();
    _builder.append(_simpleName);
    _builder.append(")");
    {
      if ((this.calledBy != null)) {
        _builder.append(" called by ");
        String _calledByString = this.calledBy.getCalledByString();
        _builder.append(_calledByString);
      }
    }
    return _builder.toString();
  }
}
