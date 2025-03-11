package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.language.elements.impl.EDataTypeClassifierImpl;

@SuppressWarnings("all")
public class EDataTypeAdapter extends EDataTypeClassifierImpl implements Wrapper<EDataType> {
  private EDataType wrappedDataType;

  @Override
  public EDataTypeClassifier forEDataType(final EDataType eDataType) {
    this.wrappedDataType = Preconditions.<EDataType>checkNotNull(eDataType);
    return this;
  }

  private void checkEDataTypeSet() {
    Preconditions.checkState((this.wrappedDataType != null), "No EDataType was set on this adapter!");
  }

  @Override
  public EDataType getWrapped() {
    return this.wrappedDataType;
  }

  protected boolean _isSuperTypeOf(final Classifier subType) {
    return false;
  }

  protected boolean _isSuperTypeOf(final EDataTypeAdapter dataTypeAdapter) {
    boolean _xblockexpression = false;
    {
      this.checkEDataTypeSet();
      if ((dataTypeAdapter == this)) {
        return true;
      }
      _xblockexpression = this.wrappedDataType.getInstanceClass().isAssignableFrom(dataTypeAdapter.wrappedDataType.getInstanceClass());
    }
    return _xblockexpression;
  }

  protected boolean _isSuperTypeOf(final MostSpecificType mostSpecificType) {
    return true;
  }

  protected boolean _isSuperTypeOf(final LeastSpecificType leastSpecificType) {
    return false;
  }

  @Override
  public String getName() {
    return this.wrappedDataType.getName();
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("{{");
    String _name = null;
    if (this.wrappedDataType!=null) {
      _name=this.wrappedDataType.getName();
    }
    _builder.append(_name);
    _builder.append("}}");
    return _builder.toString();
  }

  public boolean isSuperTypeOf(final Classifier dataTypeAdapter) {
    if (dataTypeAdapter instanceof EDataTypeAdapter) {
      return _isSuperTypeOf((EDataTypeAdapter)dataTypeAdapter);
    } else if (dataTypeAdapter instanceof LeastSpecificType) {
      return _isSuperTypeOf((LeastSpecificType)dataTypeAdapter);
    } else if (dataTypeAdapter instanceof MostSpecificType) {
      return _isSuperTypeOf((MostSpecificType)dataTypeAdapter);
    } else if (dataTypeAdapter != null) {
      return _isSuperTypeOf(dataTypeAdapter);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(dataTypeAdapter).toString());
    }
  }
}
