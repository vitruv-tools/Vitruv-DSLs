package tools.vitruv.dsls.commonalities.generator.reactions.util;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.commonalities.runtime.EmfAccess;
import tools.vitruv.dsls.commonalities.util.EMFJavaTypesUtil;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@Utility
@SuppressWarnings("all")
public final class EmfAccessExpressions {
  private static XMemberFeatureCall eGetFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(object);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, EObject.class, "eGet", 1, EStructuralFeature.class));
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XMemberFeatureCall _eFeature = EmfAccessExpressions.getEFeature(typeProvider, XbaseHelper.<XExpression>copy(object), eFeature);
      _memberCallArguments.add(_eFeature);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  private static XMemberFeatureCall eSetFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(object);
      it.setFeature(typeProvider.findMethod(EObject.class, "eSet"));
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XMemberFeatureCall _eFeature = EmfAccessExpressions.getEFeature(typeProvider, XbaseHelper.<XExpression>copy(object), eFeature);
      _memberCallArguments.add(_eFeature);
      EList<XExpression> _memberCallArguments_1 = it.getMemberCallArguments();
      _memberCallArguments_1.add(newValue);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  private static XCastedExpression eGetListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    XCastedExpression _createXCastedExpression = XbaseFactory.eINSTANCE.createXCastedExpression();
    final Procedure1<XCastedExpression> _function = (XCastedExpression it) -> {
      JvmParameterizedTypeReference _createJvmParameterizedTypeReference = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference();
      final Procedure1<JvmParameterizedTypeReference> _function_1 = (JvmParameterizedTypeReference it_1) -> {
        it_1.setType(JvmTypeProviderHelper.findType(typeProvider, List.class));
        EList<JvmTypeReference> _arguments = it_1.getArguments();
        JvmTypeReference _typeRef = typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(eFeature.getEType()));
        _arguments.add(_typeRef);
      };
      JvmParameterizedTypeReference _doubleArrow = ObjectExtensions.<JvmParameterizedTypeReference>operator_doubleArrow(_createJvmParameterizedTypeReference, _function_1);
      it.setType(_doubleArrow);
      it.setTarget(EmfAccessExpressions.eGetFeatureValue(typeProvider, object, eFeature));
    };
    return ObjectExtensions.<XCastedExpression>operator_doubleArrow(_createXCastedExpression, _function);
  }

  private static XBinaryOperation eAddListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    XBinaryOperation _xblockexpression = null;
    {
      final XCastedExpression getList = EmfAccessExpressions.eGetListFeatureValue(typeProvider, object, eFeature);
      _xblockexpression = XbaseCollectionHelper.addToCollection(typeProvider, getList, newValue);
    }
    return _xblockexpression;
  }

  private static XBinaryOperation eRemoveListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    XBinaryOperation _xblockexpression = null;
    {
      final XCastedExpression getList = EmfAccessExpressions.eGetListFeatureValue(typeProvider, object, eFeature);
      _xblockexpression = XbaseCollectionHelper.removeFromCollection(typeProvider, getList, newValue);
    }
    return _xblockexpression;
  }

  private static XIfExpression eSetListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValues) {
    final XCastedExpression getList = EmfAccessExpressions.eGetListFeatureValue(typeProvider, object, eFeature);
    XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
    final Procedure1<XIfExpression> _function = (XIfExpression it) -> {
      it.setIf(XbaseHelper.notEquals(getList, newValues, typeProvider));
      XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
      final Procedure1<XBlockExpression> _function_1 = (XBlockExpression it_1) -> {
        EList<XExpression> _expressions = it_1.getExpressions();
        List<XExpression> _expressions_1 = XbaseHelper.expressions(
          XbaseCollectionHelper.clearCollection(typeProvider, XbaseHelper.<XCastedExpression>copy(getList)), 
          XbaseCollectionHelper.addAllToCollection(typeProvider, XbaseHelper.<XCastedExpression>copy(getList), XbaseHelper.<XExpression>copy(newValues)));
        Iterables.<XExpression>addAll(_expressions, _expressions_1);
      };
      XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_1);
      it.setThen(_doubleArrow);
    };
    return ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function);
  }

  public static XExpression getFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    try {
      return EmfAccessExpressions.getEFeatureValue(typeProvider, object, eFeature);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        XCastedExpression _createXCastedExpression = XbaseFactory.eINSTANCE.createXCastedExpression();
        final Procedure1<XCastedExpression> _function = (XCastedExpression it) -> {
          final EClassifier eType = EMFJavaTypesUtil.wrapJavaPrimitiveTypes(eFeature.getEType());
          it.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(eType)));
          it.setTarget(EmfAccessExpressions.eGetFeatureValue(typeProvider, object, eFeature));
        };
        return ObjectExtensions.<XCastedExpression>operator_doubleArrow(_createXCastedExpression, _function);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static XAbstractFeatureCall setFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    try {
      final String containingInstanceClassName = ReactionsHelper.getJavaClassName(eFeature.getEContainingClass());
      if ((containingInstanceClassName == null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Containing instance class name is null!");
        throw new RuntimeException(_builder.toString());
      }
      XAssignment _createXAssignment = XbaseFactory.eINSTANCE.createXAssignment();
      final Procedure1<XAssignment> _function = (XAssignment it) -> {
        it.setAssignable(object);
        String _firstUpper = StringExtensions.toFirstUpper(eFeature.getName());
        String _plus = ("set" + _firstUpper);
        it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, containingInstanceClassName, _plus));
        it.setValue(newValue);
      };
      return ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment, _function);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        return EmfAccessExpressions.eSetFeatureValue(typeProvider, object, eFeature, newValue);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static XExpression getListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    try {
      return EmfAccessExpressions.getEFeatureValue(typeProvider, object, eFeature);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        return EmfAccessExpressions.eGetListFeatureValue(typeProvider, object, eFeature);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static XBinaryOperation addListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    try {
      final XMemberFeatureCall getList = EmfAccessExpressions.getEFeatureValue(typeProvider, object, eFeature);
      return XbaseCollectionHelper.addToCollection(typeProvider, getList, newValue);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        return EmfAccessExpressions.eAddListFeatureValue(typeProvider, object, eFeature, newValue);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static XBinaryOperation removeListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    try {
      final XMemberFeatureCall getList = EmfAccessExpressions.getEFeatureValue(typeProvider, object, eFeature);
      return XbaseCollectionHelper.removeFromCollection(typeProvider, getList, newValue);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        return EmfAccessExpressions.eRemoveListFeatureValue(typeProvider, object, eFeature, newValue);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  public static XIfExpression setListFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValues) {
    try {
      final XMemberFeatureCall getList = EmfAccessExpressions.getEFeatureValue(typeProvider, object, eFeature);
      XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
      final Procedure1<XIfExpression> _function = (XIfExpression it) -> {
        it.setIf(XbaseHelper.notEquals(getList, newValues, typeProvider));
        XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
        final Procedure1<XBlockExpression> _function_1 = (XBlockExpression it_1) -> {
          EList<XExpression> _expressions = it_1.getExpressions();
          List<XExpression> _expressions_1 = XbaseHelper.expressions(
            XbaseCollectionHelper.clearCollection(typeProvider, XbaseHelper.<XMemberFeatureCall>copy(getList)), 
            XbaseCollectionHelper.addAllToCollection(typeProvider, XbaseHelper.<XMemberFeatureCall>copy(getList), XbaseHelper.<XExpression>copy(newValues)));
          Iterables.<XExpression>addAll(_expressions, _expressions_1);
        };
        XBlockExpression _doubleArrow = ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_1);
        it.setThen(_doubleArrow);
      };
      return ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function);
    } catch (final Throwable _t) {
      if (_t instanceof NoSuchJvmElementException) {
        return EmfAccessExpressions.eSetListFeatureValue(typeProvider, object, eFeature, newValues);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }

  private static XMemberFeatureCall getEFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    final String containingInstanceClassName = ReactionsHelper.getJavaClassName(eFeature.getEContainingClass());
    if ((containingInstanceClassName == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Containing instance class name is null!");
      throw new RuntimeException(_builder.toString());
    }
    final String instanceClassName = eFeature.getEType().getInstanceClassName();
    final boolean isBooleanType = (Objects.equal(instanceClassName, boolean.class.getName()) || Objects.equal(instanceClassName, Boolean.class.getName()));
    List<String> candidateAccessorPrefixes = null;
    if (isBooleanType) {
      candidateAccessorPrefixes = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("is", ""));
    } else {
      candidateAccessorPrefixes = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("get"));
    }
    final List<String> candidateAccessorSuffixes = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("", "s"));
    final Function1<String, List<String>> _function = (String accessorPrefix) -> {
      String _xifexpression = null;
      boolean _isEmpty = accessorPrefix.isEmpty();
      if (_isEmpty) {
        _xifexpression = eFeature.getName();
      } else {
        _xifexpression = StringExtensions.toFirstUpper(eFeature.getName());
      }
      final String prefixedAccessorName = (accessorPrefix + _xifexpression);
      final Function1<String, String> _function_1 = (String accessorSuffix) -> {
        final String accessorName = (prefixedAccessorName + accessorSuffix);
        return accessorName;
      };
      return ListExtensions.<String, String>map(candidateAccessorSuffixes, _function_1);
    };
    final Iterable<String> candidateAccessors = IterableExtensions.<String, String>flatMap(candidateAccessorPrefixes, _function);
    final Function1<String, JvmOperation> _function_1 = (String accessorName) -> {
      try {
        return JvmTypeProviderHelper.findMethod(typeProvider, containingInstanceClassName, accessorName, 0);
      } catch (final Throwable _t) {
        if (_t instanceof NoSuchJvmElementException) {
          return null;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    };
    final JvmOperation accessorMethod = IterableExtensions.<JvmOperation>head(IterableExtensions.<JvmOperation>filterNull(IterableExtensions.<String, JvmOperation>map(candidateAccessors, _function_1)));
    if ((accessorMethod == null)) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Could not guess accessor for feature \'");
      String _name = eFeature.getName();
      _builder_1.append(_name);
      _builder_1.append("\'.");
      throw new NoSuchJvmElementException(_builder_1.toString());
    }
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function_2 = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(object);
      it.setFeature(accessorMethod);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function_2);
  }

  /**
   * Adaptive feature operations, depending on whether the feature is multi-valued or not:
   */
  public static XExpression retrieveFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    XExpression _xifexpression = null;
    boolean _isMany = eFeature.isMany();
    if (_isMany) {
      _xifexpression = EmfAccessExpressions.getListFeatureValue(typeProvider, object, eFeature);
    } else {
      _xifexpression = EmfAccessExpressions.getFeatureValue(typeProvider, object, eFeature);
    }
    return _xifexpression;
  }

  public static XExpression replaceFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    XExpression _xifexpression = null;
    boolean _isMany = eFeature.isMany();
    if (_isMany) {
      _xifexpression = EmfAccessExpressions.setListFeatureValue(typeProvider, object, eFeature, newValue);
    } else {
      _xifexpression = EmfAccessExpressions.setFeatureValue(typeProvider, object, eFeature, newValue);
    }
    return _xifexpression;
  }

  public static XAbstractFeatureCall insertFeatureValue(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature, final XExpression newValue) {
    XAbstractFeatureCall _xifexpression = null;
    boolean _isMany = eFeature.isMany();
    if (_isMany) {
      _xifexpression = EmfAccessExpressions.addListFeatureValue(typeProvider, object, eFeature, newValue);
    } else {
      _xifexpression = EmfAccessExpressions.setFeatureValue(typeProvider, object, eFeature, newValue);
    }
    return _xifexpression;
  }

  /**
   * Reflective retrieval of EMF meta objects:
   */
  public static XMemberFeatureCall getEFeature(@Extension final TypeProvider typeProvider, final XExpression object, final EStructuralFeature eFeature) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      XMemberFeatureCall _createXMemberFeatureCall_1 = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
      final Procedure1<XMemberFeatureCall> _function_1 = (XMemberFeatureCall it_1) -> {
        it_1.setMemberCallTarget(object);
        it_1.setFeature(typeProvider.findMethod(EObject.class, "eClass"));
      };
      XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall_1, _function_1);
      it.setMemberCallTarget(_doubleArrow);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, EClass.class, "getEStructuralFeature", String.class));
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
      final Procedure1<XStringLiteral> _function_2 = (XStringLiteral it_1) -> {
        it_1.setValue(eFeature.getName());
      };
      XStringLiteral _doubleArrow_1 = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function_2);
      _memberCallArguments.add(_doubleArrow_1);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEPackage(@Extension final TypeProvider typeProvider, final EPackage ePackage) {
    final JvmDeclaredType emfAccessType = JvmTypeProviderHelper.findDeclaredType(typeProvider, EmfAccess.class);
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(emfAccessType);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(emfAccessType, "getEPackage", String.class));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XExpression _stringLiteral = XbaseHelper.stringLiteral(ePackage.getNsURI());
      _memberCallArguments.add(_stringLiteral);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEClass(@Extension final TypeProvider typeProvider, final EClass eClass) {
    final JvmDeclaredType emfAccessType = JvmTypeProviderHelper.findDeclaredType(typeProvider, EmfAccess.class);
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(emfAccessType);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(emfAccessType, "getEClass", String.class, String.class));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(eClass.getEPackage().getNsURI()), 
        XbaseHelper.stringLiteral(eClass.getName()));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEFeature(@Extension final TypeProvider typeProvider, final EStructuralFeature eFeature) {
    final EClass containingEClass = eFeature.getEContainingClass();
    final JvmDeclaredType emfAccessType = JvmTypeProviderHelper.findDeclaredType(typeProvider, EmfAccess.class);
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(emfAccessType);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(emfAccessType, "getEFeature", String.class, String.class, String.class));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containingEClass.getEPackage().getNsURI()), 
        XbaseHelper.stringLiteral(containingEClass.getName()), 
        XbaseHelper.stringLiteral(eFeature.getName()));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEReference(@Extension final TypeProvider typeProvider, final EReference eReference) {
    final EClass containingEClass = eReference.getEContainingClass();
    final JvmDeclaredType emfAccessType = JvmTypeProviderHelper.findDeclaredType(typeProvider, EmfAccess.class);
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(emfAccessType);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(emfAccessType, "getEReference", String.class, String.class, String.class));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containingEClass.getEPackage().getNsURI()), 
        XbaseHelper.stringLiteral(containingEClass.getName()), 
        XbaseHelper.stringLiteral(eReference.getName()));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEAttribute(@Extension final TypeProvider typeProvider, final EAttribute eAttribute) {
    final EClass containingEClass = eAttribute.getEContainingClass();
    final JvmDeclaredType emfAccessType = JvmTypeProviderHelper.findDeclaredType(typeProvider, EmfAccess.class);
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(emfAccessType);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(emfAccessType, "getEAttribute", String.class, String.class, String.class));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containingEClass.getEPackage().getNsURI()), 
        XbaseHelper.stringLiteral(containingEClass.getName()), 
        XbaseHelper.stringLiteral(eAttribute.getName()));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  /**
   * Containment operations:
   */
  public static XMemberFeatureCall getEContainer(@Extension final TypeProvider typeProvider, final XExpression object) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(object);
      it.setExplicitOperationCall(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, EObject.class, "eContainer", 0));
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public static XMemberFeatureCall getEContainmentFeature(@Extension final TypeProvider typeProvider, final XExpression object) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(object);
      it.setExplicitOperationCall(true);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, EObject.class, "eContainmentFeature", 0));
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  private EmfAccessExpressions() {
    
  }
}
