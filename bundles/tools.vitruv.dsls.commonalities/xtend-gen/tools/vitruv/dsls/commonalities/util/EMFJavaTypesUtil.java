package tools.vitruv.dsls.commonalities.util;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;

@Utility
@SuppressWarnings("all")
public final class EMFJavaTypesUtil {
  /**
   * If the given classifier corresponds to a Java primitive type we return
   * the classifier of the corresponding wrapper type instead. Otherwise we
   * return the given classifier without changes.
   */
  public static EClassifier wrapJavaPrimitiveTypes(final EClassifier eClassifier) {
    EDataType _xblockexpression = null;
    {
      if ((eClassifier == null)) {
        return null;
      }
      final Class<?> instanceClass = eClassifier.getInstanceClass();
      if (((instanceClass == null) || (!instanceClass.isPrimitive()))) {
        return eClassifier;
      }
      EDataType _switchResult = null;
      boolean _matched = false;
      if (Objects.equal(instanceClass, Boolean.TYPE)) {
        _matched=true;
        _switchResult = EcorePackage.eINSTANCE.getEBooleanObject();
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Character.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getECharacterObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Byte.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getEByteObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Short.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getEShortObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Integer.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getEIntegerObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Long.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getELongObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Float.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getEFloatObject();
        }
      }
      if (!_matched) {
        if (Objects.equal(instanceClass, Double.TYPE)) {
          _matched=true;
          _switchResult = EcorePackage.eINSTANCE.getEDoubleObject();
        }
      }
      if (!_matched) {
        return eClassifier;
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }

  private EMFJavaTypesUtil() {
    
  }
}
