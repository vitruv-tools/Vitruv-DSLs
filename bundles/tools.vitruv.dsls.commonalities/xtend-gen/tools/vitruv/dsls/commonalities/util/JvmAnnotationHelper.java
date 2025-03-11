package tools.vitruv.dsls.commonalities.util;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmAnnotationAnnotationValue;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmBooleanAnnotationValue;
import org.eclipse.xtext.common.types.JvmStringAnnotationValue;
import org.eclipse.xtext.common.types.JvmTypeAnnotationValue;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@Utility
@SuppressWarnings("all")
public final class JvmAnnotationHelper {
  public static String getStringAnnotationValue(final JvmAnnotationReference annotation, final String valueName) {
    final Function1<JvmAnnotationValue, Boolean> _function = (JvmAnnotationValue it) -> {
      String _valueName = it.getValueName();
      return Boolean.valueOf(Objects.equal(_valueName, valueName));
    };
    JvmStringAnnotationValue _head = IterableExtensions.<JvmStringAnnotationValue>head(Iterables.<JvmStringAnnotationValue>filter(IterableExtensions.<JvmAnnotationValue>filter(annotation.getValues(), _function), JvmStringAnnotationValue.class));
    EList<String> _values = null;
    if (_head!=null) {
      _values=_head.getValues();
    }
    String _head_1 = null;
    if (_values!=null) {
      _head_1=IterableExtensions.<String>head(_values);
    }
    return _head_1;
  }

  public static boolean getBooleanAnnotationValue(final JvmAnnotationReference annotation, final String valueName) {
    final Function1<JvmAnnotationValue, Boolean> _function = (JvmAnnotationValue it) -> {
      String _valueName = it.getValueName();
      return Boolean.valueOf(Objects.equal(_valueName, valueName));
    };
    JvmBooleanAnnotationValue _head = IterableExtensions.<JvmBooleanAnnotationValue>head(Iterables.<JvmBooleanAnnotationValue>filter(IterableExtensions.<JvmAnnotationValue>filter(annotation.getValues(), _function), JvmBooleanAnnotationValue.class));
    EList<Boolean> _values = null;
    if (_head!=null) {
      _values=_head.getValues();
    }
    Boolean _head_1 = null;
    if (_values!=null) {
      _head_1=IterableExtensions.<Boolean>head(_values);
    }
    return (boolean) _head_1;
  }

  public static JvmTypeReference getTypeAnnotationValue(final JvmAnnotationReference annotation, final String valueName) {
    final Function1<JvmAnnotationValue, Boolean> _function = (JvmAnnotationValue it) -> {
      String _valueName = it.getValueName();
      return Boolean.valueOf(Objects.equal(_valueName, valueName));
    };
    JvmTypeAnnotationValue _head = IterableExtensions.<JvmTypeAnnotationValue>head(Iterables.<JvmTypeAnnotationValue>filter(IterableExtensions.<JvmAnnotationValue>filter(annotation.getValues(), _function), JvmTypeAnnotationValue.class));
    EList<JvmTypeReference> _values = null;
    if (_head!=null) {
      _values=_head.getValues();
    }
    JvmTypeReference _head_1 = null;
    if (_values!=null) {
      _head_1=IterableExtensions.<JvmTypeReference>head(_values);
    }
    return _head_1;
  }

  public static JvmAnnotationReference getAnnotationAnnotationValue(final JvmAnnotationReference annotation, final String valueName) {
    final Function1<JvmAnnotationValue, Boolean> _function = (JvmAnnotationValue it) -> {
      String _valueName = it.getValueName();
      return Boolean.valueOf(Objects.equal(_valueName, valueName));
    };
    JvmAnnotationAnnotationValue _head = IterableExtensions.<JvmAnnotationAnnotationValue>head(Iterables.<JvmAnnotationAnnotationValue>filter(IterableExtensions.<JvmAnnotationValue>filter(annotation.getValues(), _function), JvmAnnotationAnnotationValue.class));
    EList<JvmAnnotationReference> _values = null;
    if (_head!=null) {
      _values=_head.getValues();
    }
    JvmAnnotationReference _head_1 = null;
    if (_values!=null) {
      _head_1=IterableExtensions.<JvmAnnotationReference>head(_values);
    }
    return _head_1;
  }

  private JvmAnnotationHelper() {
    
  }
}
