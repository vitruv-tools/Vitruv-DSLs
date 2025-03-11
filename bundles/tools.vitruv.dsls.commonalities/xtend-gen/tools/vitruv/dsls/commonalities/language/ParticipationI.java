package tools.vitruv.dsls.commonalities.language;

import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.impl.ParticipationImpl;

@SuppressWarnings("all")
class ParticipationI extends ParticipationImpl {
  @Override
  public String getName() {
    String _elvis = null;
    if (this.domainAlias != null) {
      _elvis = this.domainAlias;
    } else {
      _elvis = this.domainName;
    }
    return _elvis;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    String _name = this.getName();
    _builder.append(_name);
    return _builder.toString();
  }
}
