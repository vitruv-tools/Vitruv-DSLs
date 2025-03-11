package tools.vitruv.dsls.commonalities.generator.reactions.util;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@Utility
@SuppressWarnings("all")
public final class JvmTypeProviderHelper {
  private static class ArgumentRestrictionChecker {
    private final Class<?>[] parameterTypeRestrictions;

    private final int parameterCount;

    private ArgumentRestrictionChecker(final Class<?>[] parameterTypeRestrictions, final int parameterCount) {
      this.parameterTypeRestrictions = parameterTypeRestrictions;
      this.parameterCount = parameterCount;
    }

    private boolean confirmsToArgumentRestrictions(final JvmExecutable executable) {
      if (((this.parameterCount != (-1)) && (executable.getParameters().size() != this.parameterCount))) {
        return false;
      }
      final Iterator<JvmFormalParameter> parameterIter = executable.getParameters().iterator();
      for (final Class<?> restriction : this.parameterTypeRestrictions) {
        {
          boolean _hasNext = parameterIter.hasNext();
          boolean _not = (!_hasNext);
          if (_not) {
            return false;
          }
          final JvmType parameterType = parameterIter.next().getParameterType().getType();
          if (((!Objects.equal(parameterType.getQualifiedName(), this.getQualifiedName(restriction))) && 
            (!((parameterType instanceof JvmTypeParameter) && Objects.equal(restriction, JvmTypeProviderHelper.TypeVariable.class))))) {
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Gets a name for the given class to use for the comparison with
     * {@link JvmIdentifiableElement#getQualifiedName qualified names} of
     * JVM types.
     * <p>
     * Unlike {@link Class#getCanonicalName} this uses <code>$</code> as
     * delimiter for inner classes. And unlike {@link Class#getName} this
     * appends <code>[]</code> to the component type name of array types.
     */
    private String getQualifiedName(final Class<?> clazz) {
      boolean _isArray = clazz.isArray();
      if (_isArray) {
        String _qualifiedName = this.getQualifiedName(clazz.getComponentType());
        return (_qualifiedName + "[]");
      } else {
        return clazz.getName();
      }
    }
  }

  private static class TypeVariable {
  }

  public static JvmType findType(final IJvmTypeProvider typeProvider, final Class<?> clazz) {
    final JvmType result = typeProvider.findTypeByName(clazz.getName());
    if ((result != null)) {
      return result;
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Could not find type “");
    String _name = clazz.getName();
    _builder.append(_name);
    _builder.append("”!");
    throw new NoSuchJvmElementException(_builder.toString());
  }

  public static JvmDeclaredType findDeclaredType(final IJvmTypeProvider typeProvider, final Class<?> clazz) {
    final JvmType result = JvmTypeProviderHelper.findType(typeProvider, clazz);
    if ((result instanceof JvmDeclaredType)) {
      return ((JvmDeclaredType)result);
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Could not find declared type “");
    String _name = clazz.getName();
    _builder.append(_name);
    _builder.append("”!");
    throw new NoSuchJvmElementException(_builder.toString());
  }

  private static JvmGenericType checkGenericType(final JvmType type) {
    if ((type instanceof JvmGenericType)) {
      return ((JvmGenericType)type);
    }
    if ((type == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Type not found!");
      throw new NoSuchJvmElementException(_builder.toString());
    }
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Expected ‹");
    String _qualifiedName = type.getQualifiedName();
    _builder_1.append(_qualifiedName);
    _builder_1.append("› to resolve to a JvmGenericType!");
    throw new NoSuchJvmElementException(_builder_1.toString());
  }

  public static JvmField findAttribute(final JvmDeclaredType declaredType, final String attributeName) {
    final Function1<JvmMember, Boolean> _function = (JvmMember it) -> {
      String _simpleName = it.getSimpleName();
      return Boolean.valueOf(Objects.equal(_simpleName, attributeName));
    };
    Iterable<JvmField> _filter = Iterables.<JvmField>filter(IterableExtensions.<JvmMember>filter(declaredType.getMembers(), _function), JvmField.class);
    final Function1<JvmTypeReference, JvmType> _function_1 = (JvmTypeReference it) -> {
      return it.getType();
    };
    final Function1<JvmDeclaredType, JvmField> _function_2 = (JvmDeclaredType it) -> {
      return JvmTypeProviderHelper.findAttribute(it, attributeName);
    };
    Iterable<JvmField> _map = IterableExtensions.<JvmDeclaredType, JvmField>map(Iterables.<JvmDeclaredType>filter(ListExtensions.<JvmTypeReference, JvmType>map(declaredType.getSuperTypes(), _function_1), JvmDeclaredType.class), _function_2);
    final JvmField result = IterableExtensions.<JvmField>head(Iterables.<JvmField>concat(_filter, _map));
    if ((result != null)) {
      return result;
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Could not find the attribute “");
    _builder.append(attributeName);
    _builder.append("” in ‹");
    String _qualifiedName = declaredType.getQualifiedName();
    _builder.append(_qualifiedName);
    _builder.append("›!");
    throw new NoSuchJvmElementException(_builder.toString());
  }

  public static JvmOperation findMethod(final JvmDeclaredType type, final String methodName, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findMethod(type, methodName, (-1), parameterTypeRestrictions);
  }

  public static JvmOperation findImplementedMethod(final JvmDeclaredType declaredType, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.<JvmOperation>onlyResultFor(JvmTypeProviderHelper.getImplementedMethodList(declaredType, methodName, parameterCount, parameterTypeRestrictions), "implemented method", methodName, parameterTypeRestrictions, parameterCount, declaredType);
  }

  public static JvmOperation findOptionalImplementedMethod(final JvmDeclaredType declaredType, final String methodName, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findOptionalImplementedMethod(declaredType, methodName, (-1), parameterTypeRestrictions);
  }

  public static JvmOperation findOptionalImplementedMethod(final JvmDeclaredType declaredType, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    return IterableExtensions.<JvmOperation>head(JvmTypeProviderHelper.<JvmOperation>maxOneResultFor(JvmTypeProviderHelper.getImplementedMethodList(declaredType, methodName, parameterCount, parameterTypeRestrictions), "implemented method", methodName, parameterTypeRestrictions, parameterCount, declaredType));
  }

  private static List<JvmOperation> getImplementedMethodList(final JvmDeclaredType declaredType, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    List<JvmOperation> _xblockexpression = null;
    {
      @Extension
      final JvmTypeProviderHelper.ArgumentRestrictionChecker argumentChecker = new JvmTypeProviderHelper.ArgumentRestrictionChecker(parameterTypeRestrictions, parameterCount);
      final Function1<JvmOperation, Boolean> _function = (JvmOperation it) -> {
        return Boolean.valueOf((Objects.equal(it.getSimpleName(), methodName) && argumentChecker.confirmsToArgumentRestrictions(it)));
      };
      _xblockexpression = IterableExtensions.<JvmOperation>toList(IterableExtensions.<JvmOperation>filter(Iterables.<JvmOperation>filter(declaredType.getMembers(), JvmOperation.class), _function));
    }
    return _xblockexpression;
  }

  public static JvmOperation findImplementedMethod(final JvmDeclaredType type, final String methodName, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findImplementedMethod(type, methodName, (-1), parameterTypeRestrictions);
  }

  public static JvmOperation findMethod(final JvmDeclaredType type, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    @Extension
    final JvmTypeProviderHelper.ArgumentRestrictionChecker argumentChecker = new JvmTypeProviderHelper.ArgumentRestrictionChecker(parameterTypeRestrictions, parameterCount);
    final Predicate<JvmOperation> _function = (JvmOperation it) -> {
      return (Objects.equal(it.getSimpleName(), methodName) && argumentChecker.confirmsToArgumentRestrictions(it));
    };
    final List<JvmOperation> results = IterableExtensions.<JvmOperation>toList(JvmTypeProviderHelper.findAllMethods(type, _function));
    return JvmTypeProviderHelper.<JvmOperation>onlyResultFor(results, "method", methodName, parameterTypeRestrictions, parameterCount, type);
  }

  private static <T extends JvmMember> T onlyResultFor(final Collection<T> result, final String memberType, final String memberName, final Class<?>[] parameterTypeRestrictions, final int parameterCount, final JvmDeclaredType type) {
    return JvmTypeProviderHelper.<T>atLeastOneResultFor(JvmTypeProviderHelper.<T>maxOneResultFor(result, memberType, memberName, parameterTypeRestrictions, parameterCount, type), memberType, memberName, parameterTypeRestrictions, parameterCount, type);
  }

  private static <T extends JvmMember> T atLeastOneResultFor(final Collection<T> result, final String memberType, final String memberName, final Class<?>[] parameterTypeRestrictions, final int parameterCount, final JvmDeclaredType type) {
    int _size = result.size();
    boolean _tripleEquals = (_size == 0);
    if (_tripleEquals) {
      final CharSequence description = JvmTypeProviderHelper.description(memberType, memberName, parameterTypeRestrictions, parameterCount, type);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find a ");
      _builder.append(description);
      _builder.append("!");
      throw new NoSuchJvmElementException(_builder.toString());
    }
    return ((T[])Conversions.unwrapArray(result, JvmMember.class))[0];
  }

  private static <T extends JvmMember> Collection<T> maxOneResultFor(final Collection<T> result, final String memberType, final String memberName, final Class<?>[] parameterTypeRestrictions, final int parameterCount, final JvmDeclaredType type) {
    int _size = result.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      final CharSequence description = JvmTypeProviderHelper.description(memberType, memberName, parameterTypeRestrictions, parameterCount, type);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Found more than one ");
      _builder.append(description);
      _builder.append(":");
      _builder.newLineIfNotEmpty();
      {
        for(final T resultElement : result) {
          String _identifier = resultElement.getIdentifier();
          _builder.append(_identifier);
          _builder.newLineIfNotEmpty();
        }
      }
      throw new NoSuchJvmElementException(_builder.toString());
    }
    return result;
  }

  private static CharSequence description(final String memberType, final String memberName, final Class<?>[] parameterTypeRestrictions, final int parameterCount, final JvmDeclaredType type) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(memberType);
    {
      if ((memberName != null)) {
        _builder.append(" “");
        _builder.append(memberName);
        _builder.append("”");
      }
    }
    {
      int _length = parameterTypeRestrictions.length;
      boolean _greaterThan = (_length > 0);
      if (_greaterThan) {
        _builder.append(" with the parameters (");
        {
          boolean _hasElements = false;
          for(final Class<?> restriction : parameterTypeRestrictions) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate(", ", "");
            }
            _builder.append("‹");
            {
              boolean _equals = Objects.equal(restriction, JvmTypeProviderHelper.TypeVariable.class);
              if (_equals) {
                _builder.append("Type Variable");
              } else {
                _builder.append(restriction);
              }
            }
            _builder.append("›");
          }
        }
        _builder.append(")");
      }
    }
    {
      if ((parameterCount != (-1))) {
        _builder.append(" having exactly ");
        _builder.append(parameterCount);
        _builder.append(" parameters");
      }
    }
    _builder.append(" in ‹");
    String _qualifiedName = type.getQualifiedName();
    _builder.append(_qualifiedName);
    _builder.append("›!");
    _builder.newLineIfNotEmpty();
    return _builder;
  }

  private static Iterable<JvmOperation> findAllMethods(final JvmDeclaredType declaredType, final Predicate<JvmOperation> inclusionCondition) {
    final List<JvmOperation> thisMethods = IterableExtensions.<JvmOperation>toList(IterableExtensions.<JvmOperation>filter(Iterables.<JvmOperation>filter(declaredType.getMembers(), JvmOperation.class), new Function1<JvmOperation, Boolean>() {
        public Boolean apply(JvmOperation arg0) {
          return inclusionCondition.test(arg0);
        }
    }));
    final Function1<JvmTypeReference, JvmType> _function = (JvmTypeReference it) -> {
      return it.getType();
    };
    final Function1<JvmDeclaredType, Iterable<JvmOperation>> _function_1 = (JvmDeclaredType it) -> {
      return JvmTypeProviderHelper.findAllMethods(it, inclusionCondition);
    };
    final Function1<JvmOperation, Boolean> _function_2 = (JvmOperation superMethod) -> {
      final Function1<JvmOperation, Boolean> _function_3 = (JvmOperation it) -> {
        return Boolean.valueOf(JvmTypeProviderHelper.hasSameSignatureAs(it, superMethod));
      };
      JvmOperation _findFirst = IterableExtensions.<JvmOperation>findFirst(thisMethods, _function_3);
      return Boolean.valueOf((_findFirst == null));
    };
    final Iterable<JvmOperation> superMethods = IterableExtensions.<JvmOperation>filter(IterableExtensions.<JvmDeclaredType, JvmOperation>flatMap(Iterables.<JvmDeclaredType>filter(ListExtensions.<JvmTypeReference, JvmType>map(declaredType.getSuperTypes(), _function), JvmDeclaredType.class), _function_1), _function_2);
    return Iterables.<JvmOperation>concat(thisMethods, superMethods);
  }

  private static boolean hasSameSignatureAs(final JvmOperation a, final JvmOperation b) {
    String _simpleName = a.getSimpleName();
    String _simpleName_1 = b.getSimpleName();
    boolean _notEquals = (!Objects.equal(_simpleName, _simpleName_1));
    if (_notEquals) {
      return false;
    }
    int _size = a.getParameters().size();
    int _size_1 = b.getParameters().size();
    boolean _notEquals_1 = (_size != _size_1);
    if (_notEquals_1) {
      return false;
    }
    boolean _isStatic = a.isStatic();
    boolean _isStatic_1 = b.isStatic();
    boolean _notEquals_2 = (_isStatic != _isStatic_1);
    if (_notEquals_2) {
      return false;
    }
    final Iterator<JvmFormalParameter> bIter = b.getParameters().iterator();
    EList<JvmFormalParameter> _parameters = a.getParameters();
    for (final JvmFormalParameter aParameter : _parameters) {
      String _qualifiedName = aParameter.getParameterType().getQualifiedName();
      String _qualifiedName_1 = bIter.next().getParameterType().getQualifiedName();
      boolean _notEquals_3 = (!Objects.equal(_qualifiedName, _qualifiedName_1));
      if (_notEquals_3) {
        return false;
      }
    }
    return true;
  }

  public static JvmOperation findMethod(final IJvmTypeProvider typeProvider, final String typeQualifiedName, final String methodName, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findMethod(typeProvider, typeQualifiedName, methodName, (-1), parameterTypeRestrictions);
  }

  public static JvmOperation findMethod(final IJvmTypeProvider typeProvider, final String typeQualifiedName, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.checkGenericType(typeProvider.findTypeByName(typeQualifiedName)), methodName, parameterCount, parameterTypeRestrictions);
  }

  public static JvmOperation findMethod(final IJvmTypeProvider typeProvider, final Class<?> clazz, final String methodName, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findMethod(typeProvider, clazz, methodName, (-1), parameterTypeRestrictions);
  }

  public static JvmOperation findMethod(final IJvmTypeProvider typeProvider, final Class<?> clazz, final String methodName, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.checkGenericType(JvmTypeProviderHelper.findType(typeProvider, clazz)), methodName, parameterCount, parameterTypeRestrictions);
  }

  public static JvmField findAttribute(final IJvmTypeProvider typeProvider, final Class<?> clazz, final String attributeName) {
    return JvmTypeProviderHelper.findAttribute(JvmTypeProviderHelper.checkGenericType(JvmTypeProviderHelper.findType(typeProvider, clazz)), attributeName);
  }

  public static JvmConstructor findConstructor(final JvmDeclaredType type, final Class<?>... parameterTypeRestrictions) {
    return JvmTypeProviderHelper.findConstructor(type, (-1), parameterTypeRestrictions);
  }

  public static JvmConstructor findNoArgsConstructor(final JvmDeclaredType type) {
    return JvmTypeProviderHelper.findConstructor(type, 0);
  }

  public static JvmConstructor findConstructor(final JvmDeclaredType type, final int parameterCount, final Class<?>... parameterTypeRestrictions) {
    @Extension
    final JvmTypeProviderHelper.ArgumentRestrictionChecker argumentChecker = new JvmTypeProviderHelper.ArgumentRestrictionChecker(parameterTypeRestrictions, parameterCount);
    final Function1<JvmConstructor, Boolean> _function = (JvmConstructor it) -> {
      return Boolean.valueOf(argumentChecker.confirmsToArgumentRestrictions(it));
    };
    final List<JvmConstructor> results = IterableExtensions.<JvmConstructor>toList(IterableExtensions.<JvmConstructor>filter(type.getDeclaredConstructors(), _function));
    return JvmTypeProviderHelper.<JvmConstructor>onlyResultFor(results, "constructor", null, parameterTypeRestrictions, parameterCount, type);
  }

  public static Class<?> typeVariable() {
    return JvmTypeProviderHelper.TypeVariable.class;
  }

  /**
   * Helper method to get array class references, as you cannot write
   * "Type[].class" in Xtend.
   */
  public static Class<?> getArrayClass(final Class<?> componentClass) {
    try {
      String _switchResult = null;
      boolean _matched = false;
      boolean _isArray = componentClass.isArray();
      if (_isArray) {
        _matched=true;
        String _name = componentClass.getName();
        _switchResult = ("[" + _name);
      }
      if (!_matched) {
        if (Objects.equal(componentClass, boolean.class)) {
          _matched=true;
          _switchResult = "[Z";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, byte.class)) {
          _matched=true;
          _switchResult = "[B";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, char.class)) {
          _matched=true;
          _switchResult = "[C";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, double.class)) {
          _matched=true;
          _switchResult = "[D";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, float.class)) {
          _matched=true;
          _switchResult = "[F";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, int.class)) {
          _matched=true;
          _switchResult = "[I";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, long.class)) {
          _matched=true;
          _switchResult = "[J";
        }
      }
      if (!_matched) {
        if (Objects.equal(componentClass, short.class)) {
          _matched=true;
          _switchResult = "[S";
        }
      }
      if (!_matched) {
        String _name_1 = componentClass.getName();
        String _plus = ("[L" + _name_1);
        _switchResult = (_plus + ";");
      }
      final String className = _switchResult;
      return Class.forName(className, false, componentClass.getClassLoader());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private JvmTypeProviderHelper() {
    
  }
}
