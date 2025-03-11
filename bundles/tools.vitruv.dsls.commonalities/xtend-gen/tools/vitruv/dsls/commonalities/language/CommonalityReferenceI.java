package tools.vitruv.dsls.commonalities.language;

import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.util.function.Predicate;
import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.language.impl.CommonalityReferenceImpl;

@SuppressWarnings("all")
class CommonalityReferenceI extends CommonalityReferenceImpl {
  @Override
  public ClassLike basicGetClassLikeContainer() {
    return CommonalitiesLanguageModelExtensions.<Commonality>getOptionalDirectEContainer(this, Commonality.class);
  }

  @Override
  public boolean isMultiValued() {
    final Predicate<CommonalityReferenceMapping> _function = (CommonalityReferenceMapping it) -> {
      return CommonalitiesLanguageModelExtensions.isMultiValued(it);
    };
    return IterableUtil.<CommonalityReferenceMapping>containsAny(this.getMappings(), _function);
  }

  @Override
  public Classifier getType() {
    return this.getReferenceType();
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    ClassLike _classLikeContainer = this.getClassLikeContainer();
    _builder.append(_classLikeContainer);
    _builder.append(".");
    _builder.append(this.name);
    return _builder.toString();
  }
}
