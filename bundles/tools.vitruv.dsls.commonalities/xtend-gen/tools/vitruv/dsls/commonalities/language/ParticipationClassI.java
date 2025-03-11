package tools.vitruv.dsls.commonalities.language;

import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.language.impl.ParticipationClassImpl;

@SuppressWarnings("all")
class ParticipationClassI extends ParticipationClassImpl {
  @Override
  public String getName() {
    String _elvis = null;
    if (this.classAlias != null) {
      _elvis = this.classAlias;
    } else {
      Metaclass _superMetaclass = this.getSuperMetaclass();
      String _name = null;
      if (_superMetaclass!=null) {
        _name=_superMetaclass.getName();
      }
      _elvis = _name;
    }
    return _elvis;
  }

  @Override
  public PackageLike basicGetPackageLikeContainer() {
    return CommonalitiesLanguageModelExtensions.<Participation>getOptionalEContainer(this, Participation.class);
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    PackageLike _packageLikeContainer = this.getPackageLikeContainer();
    _builder.append(_packageLikeContainer);
    _builder.append(":");
    String _name = this.getName();
    _builder.append(_name);
    return _builder.toString();
  }
}
