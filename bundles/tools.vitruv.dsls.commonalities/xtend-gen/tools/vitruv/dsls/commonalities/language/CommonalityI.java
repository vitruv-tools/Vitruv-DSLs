package tools.vitruv.dsls.commonalities.language;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.List;
import org.eclipse.emf.common.util.DelegatingEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.MetaclassMember;
import tools.vitruv.dsls.commonalities.language.elements.MostSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.language.impl.CommonalityImpl;

@SuppressWarnings("all")
class CommonalityI extends CommonalityImpl {
  @Override
  public PackageLike basicGetPackageLikeContainer() {
    CommonalityFile _optionalDirectEContainer = CommonalitiesLanguageModelExtensions.<CommonalityFile>getOptionalDirectEContainer(this, CommonalityFile.class);
    Concept _concept = null;
    if (_optionalDirectEContainer!=null) {
      _concept=_optionalDirectEContainer.getConcept();
    }
    return _concept;
  }

  @Override
  public EList<MetaclassMember> getAllMembers() {
    EList<CommonalityAttribute> _attributes = this.getAttributes();
    EList<CommonalityReference> _references = this.getReferences();
    List<Attribute> _list = IterableExtensions.<Attribute>toList(Iterables.<Attribute>concat(_attributes, _references));
    return new DelegatingEList.UnmodifiableEList(_list);
  }

  protected boolean _isSuperTypeOf(final Classifier classifier) {
    return Objects.equal(classifier, this);
  }

  protected boolean _isSuperTypeOf(final MostSpecificType mostSpecificType) {
    return true;
  }

  protected boolean _isSuperTypeOf(final LeastSpecificType leastSpecificType) {
    return false;
  }

  @Override
  public Domain basicGetDomain() {
    return CommonalitiesLanguageModelExtensions.getConcept(this);
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    PackageLike _packageLikeContainer = this.getPackageLikeContainer();
    _builder.append(_packageLikeContainer);
    _builder.append(":");
    _builder.append(this.name);
    return _builder.toString();
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  public boolean isSuperTypeOf(final Classifier leastSpecificType) {
    if (leastSpecificType instanceof LeastSpecificType) {
      return _isSuperTypeOf((LeastSpecificType)leastSpecificType);
    } else if (leastSpecificType instanceof MostSpecificType) {
      return _isSuperTypeOf((MostSpecificType)leastSpecificType);
    } else if (leastSpecificType != null) {
      return _isSuperTypeOf(leastSpecificType);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(leastSpecificType).toString());
    }
  }
}
