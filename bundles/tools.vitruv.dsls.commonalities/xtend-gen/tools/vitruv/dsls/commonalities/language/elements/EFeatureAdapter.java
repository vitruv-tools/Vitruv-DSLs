package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl;

@SuppressWarnings("all")
public class EFeatureAdapter extends EFeatureAttributeImpl implements Wrapper<EStructuralFeature> {
  private EStructuralFeature wrappedEFeature;

  private Classifier adaptedType;

  private ClassifierProvider classifierProvider;

  private Metaclass containingMetaclass;

  @Override
  public EFeatureAttribute withClassifierProvider(final ClassifierProvider classifierProvider) {
    this.classifierProvider = Preconditions.<ClassifierProvider>checkNotNull(classifierProvider);
    if (((this.wrappedEFeature != null) && (this.containingMetaclass != null))) {
      this.readAdaptedType();
    }
    return this;
  }

  private void checkAdaptedTypeRead() {
    if ((this.adaptedType == null)) {
      Preconditions.checkState((this.adaptedType != null), "No classifier provider was set on this element!");
      Preconditions.checkState((this.containingMetaclass != null), "No containing metaclass was set on this attribute!");
    }
  }

  private void checkEFeatureSet() {
    Preconditions.checkState((this.wrappedEFeature != null), "No EStructualFeature was set on this adapter!");
  }

  private void checkMetaclassSet() {
    Preconditions.checkState((this.containingMetaclass != null), "No metaclass was set on this attribute!");
  }

  private ClassifierProvider readAdaptedType() {
    ClassifierProvider _xblockexpression = null;
    {
      this.adaptedType = this.classifierProvider.toClassifier(this.wrappedEFeature.getEType(), this.containingMetaclass.getDomain());
      _xblockexpression = this.classifierProvider = null;
    }
    return _xblockexpression;
  }

  @Override
  public EFeatureAttribute forEFeature(final EStructuralFeature eFeature) {
    this.wrappedEFeature = Preconditions.<EStructuralFeature>checkNotNull(eFeature);
    if (((this.classifierProvider != null) && (this.containingMetaclass != null))) {
      this.readAdaptedType();
    }
    return this;
  }

  @Override
  public EFeatureAttribute fromMetaclass(final Metaclass metaclass) {
    this.containingMetaclass = Preconditions.<Metaclass>checkNotNull(metaclass);
    if (((this.wrappedEFeature != null) && (this.classifierProvider != null))) {
      this.readAdaptedType();
    }
    return this;
  }

  @Override
  public ClassLike basicGetClassLikeContainer() {
    Metaclass _xblockexpression = null;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      this.checkMetaclassSet();
      _xblockexpression = this.containingMetaclass;
    }
    return _xblockexpression;
  }

  @Override
  public String getName() {
    String _xblockexpression = null;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      this.checkEFeatureSet();
      _xblockexpression = this.wrappedEFeature.getName();
    }
    return _xblockexpression;
  }

  @Override
  public Classifier getType() {
    Classifier _xblockexpression = null;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      this.checkAdaptedTypeRead();
      _xblockexpression = this.adaptedType;
    }
    return _xblockexpression;
  }

  @Override
  public EStructuralFeature getWrapped() {
    return this.wrappedEFeature;
  }

  @Override
  public boolean isMultiValued() {
    boolean _xblockexpression = false;
    {
      boolean _eIsProxy = this.eIsProxy();
      if (_eIsProxy) {
        return false;
      }
      this.checkEFeatureSet();
      _xblockexpression = this.wrappedEFeature.isMany();
    }
    return _xblockexpression;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(this.containingMetaclass);
    _builder.append(".");
    String _name = null;
    if (this.wrappedEFeature!=null) {
      _name=this.wrappedEFeature.getName();
    }
    _builder.append(_name);
    return _builder.toString();
  }

  @Override
  public boolean equals(final Object o) {
    boolean _xifexpression = false;
    if ((this == o)) {
      _xifexpression = true;
    } else {
      boolean _xifexpression_1 = false;
      if ((o == null)) {
        _xifexpression_1 = false;
      } else {
        boolean _xifexpression_2 = false;
        if ((o instanceof EFeatureAdapter)) {
          _xifexpression_2 = (Objects.equal(this.containingMetaclass, ((EFeatureAdapter)o).containingMetaclass) && Objects.equal(this.wrappedEFeature, ((EFeatureAdapter)o).wrappedEFeature));
        } else {
          _xifexpression_2 = false;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }

  @Override
  public int hashCode() {
    final int prime = 109;
    int _xifexpression = (int) 0;
    if ((this.containingMetaclass == null)) {
      _xifexpression = 0;
    } else {
      _xifexpression = this.containingMetaclass.hashCode();
    }
    int _plus = (prime + _xifexpression);
    int _multiply = (_plus * prime);
    int _xifexpression_1 = (int) 0;
    if ((this.wrappedEFeature == null)) {
      _xifexpression_1 = 0;
    } else {
      _xifexpression_1 = this.wrappedEFeature.hashCode();
    }
    return (_multiply + _xifexpression_1);
  }
}
