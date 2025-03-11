package tools.vitruv.dsls.commonalities.language;

import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.impl.ParticipationAttributeImpl;

@SuppressWarnings("all")
class ParticipationAttributeI extends ParticipationAttributeImpl {
  @Override
  public Classifier getType() {
    Attribute _attribute = this.getAttribute();
    Classifier _type = null;
    if (_attribute!=null) {
      _type=_attribute.getType();
    }
    return _type;
  }

  @Override
  public ClassLike basicGetClassLikeContainer() {
    return this.getParticipationClass();
  }

  @Override
  public boolean isMultiValued() {
    return ((this.getAttribute() != null) && this.getAttribute().isMultiValued());
  }

  @Override
  public String getName() {
    Attribute _attribute = this.getAttribute();
    String _name = null;
    if (_attribute!=null) {
      _name=_attribute.getName();
    }
    return _name;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(this.participationClass);
    _builder.append(".");
    String _name = this.getName();
    _builder.append(_name);
    return _builder.toString();
  }
}
