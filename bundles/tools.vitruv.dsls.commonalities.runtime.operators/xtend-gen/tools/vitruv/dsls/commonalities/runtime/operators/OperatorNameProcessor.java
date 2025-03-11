package tools.vitruv.dsls.commonalities.runtime.operators;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.TypeCopier;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;
import javax.lang.model.SourceVersion;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend.lib.macro.AbstractClassProcessor;
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.InterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableConstructorDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ResolvedMethod;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@FinalFieldsConstructor
@SuppressWarnings("all")
public final class OperatorNameProcessor extends AbstractClassProcessor implements ClassProcessor {
  private final Class<? extends Annotation> annotationType;

  @Override
  public void doRegisterGlobals(final ClassDeclaration annotatedClass, @Extension final RegisterGlobalsContext context) {
    final Type annotationTypeReference = context.findUpstreamType(this.annotationType);
    final String operatorLanguageName = this.getOperatorName(annotatedClass, annotationTypeReference);
    boolean _isEmpty = IterableExtensions.isEmpty(this.getNameProblems(operatorLanguageName));
    if (_isEmpty) {
      context.registerClass(this.getTargetQualifedName(annotatedClass, annotationTypeReference));
    }
  }

  @Override
  public void doTransform(final MutableClassDeclaration annotatedClass, @Extension final TransformationContext context) {
    final Type annotationTypeReference = context.findTypeGlobally(this.annotationType);
    final String operatorLanguageName = this.getOperatorName(annotatedClass, annotationTypeReference);
    final Iterable<String> nameProblems = this.getNameProblems(operatorLanguageName);
    boolean _isEmpty = IterableExtensions.isEmpty(nameProblems);
    boolean _not = (!_isEmpty);
    if (_not) {
      final Consumer<String> _function = (String problem) -> {
        context.addError(annotatedClass, problem);
      };
      nameProblems.forEach(_function);
      return;
    }
    MutableClassDeclaration _findClass = context.findClass(this.getTargetQualifedName(annotatedClass, annotationTypeReference));
    final Procedure1<MutableClassDeclaration> _function_1 = (MutableClassDeclaration it) -> {
      @Extension
      final TypeCopier classTypeCopier = new TypeCopier(context);
      final Consumer<TypeReference> _function_2 = (TypeReference parentInterface) -> {
        classTypeCopier.copyTypeParametersFrom(it, parentInterface);
      };
      this.getAllImplementedInterfaces(annotatedClass).forEach(_function_2);
      final Function1<TypeReference, TypeReference> _function_3 = (TypeReference it_1) -> {
        return classTypeCopier.replaceTypeParameters(it_1);
      };
      it.setImplementedInterfaces(IterableExtensions.<TypeReference, TypeReference>map(this.getAllImplementedInterfaces(annotatedClass), _function_3));
      final Consumer<AnnotationReference> _function_4 = (AnnotationReference sourceAnnotation) -> {
        it.addAnnotation(context.newAnnotationReference(sourceAnnotation));
      };
      annotatedClass.getAnnotations().forEach(_function_4);
      context.setPrimarySourceElement(it, annotatedClass);
      final Procedure1<MutableFieldDeclaration> _function_5 = (MutableFieldDeclaration it_1) -> {
        final Function1<MutableTypeParameterDeclaration, TypeReference> _function_6 = (MutableTypeParameterDeclaration it_2) -> {
          return classTypeCopier.replaceTypeParameters(context.newTypeReference(it_2));
        };
        it_1.setType(context.newTypeReference(annotatedClass, ((TypeReference[])Conversions.unwrapArray(IterableExtensions.map(annotatedClass.getTypeParameters(), _function_6), TypeReference.class))));
        it_1.setFinal(true);
        it_1.setVisibility(Visibility.PRIVATE);
      };
      it.addField("delegate", _function_5);
      final Function1<MutableConstructorDeclaration, Boolean> _function_6 = (MutableConstructorDeclaration it_1) -> {
        Visibility _visibility = it_1.getVisibility();
        return Boolean.valueOf(Objects.equal(_visibility, Visibility.PUBLIC));
      };
      final Consumer<MutableConstructorDeclaration> _function_7 = (MutableConstructorDeclaration sourceConstructor) -> {
        final Procedure1<MutableConstructorDeclaration> _function_8 = (MutableConstructorDeclaration it_1) -> {
          final Consumer<MutableParameterDeclaration> _function_9 = (MutableParameterDeclaration source) -> {
            it_1.addParameter(source.getSimpleName(), source.getType());
          };
          sourceConstructor.getParameters().forEach(_function_9);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("this.delegate = new ");
              _builder.append(annotatedClass);
              _builder.append("(");
              {
                Iterable<? extends MutableParameterDeclaration> _parameters = sourceConstructor.getParameters();
                boolean _hasElements = false;
                for(final MutableParameterDeclaration p : _parameters) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(", ", "");
                  }
                  String _simpleName = p.getSimpleName();
                  _builder.append(_simpleName);
                }
              }
              _builder.append(");");
              _builder.newLineIfNotEmpty();
            }
          };
          it_1.setBody(_client);
        };
        it.addConstructor(_function_8);
      };
      IterableExtensions.filter(annotatedClass.getDeclaredConstructors(), _function_6).forEach(_function_7);
      final Function1<TypeReference, Iterable<ResolvedMethod>> _function_8 = (TypeReference it_1) -> {
        Iterable<? extends ResolvedMethod> _allResolvedMethods = it_1.getAllResolvedMethods();
        return ((Iterable<ResolvedMethod>) _allResolvedMethods);
      };
      final Function1<ResolvedMethod, Boolean> _function_9 = (ResolvedMethod it_1) -> {
        TypeDeclaration _declaringType = it_1.getDeclaration().getDeclaringType();
        return Boolean.valueOf((_declaringType instanceof InterfaceDeclaration));
      };
      final Function1<ResolvedMethod, Object> _function_10 = (ResolvedMethod it_1) -> {
        return this.getIdentifier(it_1);
      };
      final Consumer<ResolvedMethod> _function_11 = (ResolvedMethod interfaceMethod) -> {
        @Extension
        final TypeCopier methodTypeCopier = new TypeCopier(classTypeCopier, context);
        final Procedure1<MutableMethodDeclaration> _function_12 = (MutableMethodDeclaration it_1) -> {
          methodTypeCopier.copyTypeParametersFrom(it_1, interfaceMethod);
          it_1.setReturnType(methodTypeCopier.replaceTypeParameters(interfaceMethod.getDeclaration().getReturnType()));
          final Consumer<ParameterDeclaration> _function_13 = (ParameterDeclaration source) -> {
            it_1.addParameter(source.getSimpleName(), methodTypeCopier.replaceTypeParameters(source.getType()));
          };
          interfaceMethod.getDeclaration().getParameters().forEach(_function_13);
          it_1.setVisibility(Visibility.PUBLIC);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              {
                boolean _isVoid = interfaceMethod.getDeclaration().getReturnType().isVoid();
                boolean _not = (!_isVoid);
                if (_not) {
                  _builder.append("return ");
                }
              }
              _builder.append(" this.delegate.");
              String _simpleName = interfaceMethod.getDeclaration().getSimpleName();
              _builder.append(_simpleName);
              _builder.append("(");
              {
                Iterable<? extends ParameterDeclaration> _parameters = interfaceMethod.getDeclaration().getParameters();
                boolean _hasElements = false;
                for(final ParameterDeclaration p : _parameters) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(", ", "");
                  }
                  String _simpleName_1 = p.getSimpleName();
                  _builder.append(_simpleName_1);
                }
              }
              _builder.append(");");
              _builder.newLineIfNotEmpty();
            }
          };
          it_1.setBody(_client);
        };
        it.addMethod(interfaceMethod.getDeclaration().getSimpleName(), _function_12);
      };
      this.<ResolvedMethod>uniqueBy(IterableExtensions.<ResolvedMethod>filter(IterableExtensions.<TypeReference, ResolvedMethod>flatMap(this.getAllImplementedInterfaces(annotatedClass), _function_8), _function_9), _function_10).forEach(_function_11);
    };
    ObjectExtensions.<MutableClassDeclaration>operator_doubleArrow(_findClass, _function_1);
  }

  private Iterable<String> getNameProblems(final String name) {
    Iterable<String> _xblockexpression = null;
    {
      final String convertedName = CommonalitiesOperatorConventions.toOperatorTypeName(name);
      boolean _endsWith = name.endsWith("_");
      boolean _not = (!_endsWith);
      List<String> _check = OperatorNameProcessor.check(_not, "Operator names must not end with underscores!");
      boolean _isName = SourceVersion.isName(convertedName);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("‹");
      _builder.append(convertedName);
      _builder.append("› is not a legal Java type name!");
      List<String> _check_1 = OperatorNameProcessor.check(_isName, _builder.toString());
      _xblockexpression = Iterables.<String>concat(_check, _check_1);
    }
    return _xblockexpression;
  }

  private String getTargetQualifedName(final ClassDeclaration annotatedClass, final Type annotationType) {
    String _xblockexpression = null;
    {
      final int lastDot = annotatedClass.getQualifiedName().lastIndexOf(".");
      _xblockexpression = CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(
        annotatedClass.getQualifiedName().substring(0, lastDot), 
        this.getOperatorName(annotatedClass, annotationType));
    }
    return _xblockexpression;
  }

  private String getOperatorName(final ClassDeclaration annotatedClass, final Type annotationType) {
    String _elvis = null;
    AnnotationReference _findAnnotation = annotatedClass.findAnnotation(annotationType);
    String _stringValue = null;
    if (_findAnnotation!=null) {
      _stringValue=_findAnnotation.getStringValue("name");
    }
    if (_stringValue != null) {
      _elvis = _stringValue;
    } else {
      String _firstLower = StringExtensions.toFirstLower(annotatedClass.getSimpleName());
      _elvis = _firstLower;
    }
    return _elvis;
  }

  private Iterable<TypeReference> getAllImplementedInterfaces(final MutableClassDeclaration annotatedClass) {
    Iterable<? extends TypeReference> _implementedInterfaces = annotatedClass.getImplementedInterfaces();
    Iterable<TypeReference> _allImplementedInterfaces = this.getAllImplementedInterfaces(annotatedClass.getExtendedClass());
    return Iterables.<TypeReference>concat(_implementedInterfaces, _allImplementedInterfaces);
  }

  private Iterable<TypeReference> getAllImplementedInterfaces(final TypeReference reference) {
    final Function1<TypeReference, Boolean> _function = (TypeReference it) -> {
      Type _type = it.getType();
      return Boolean.valueOf((_type instanceof InterfaceDeclaration));
    };
    Iterable<? extends TypeReference> _filter = IterableExtensions.filter(reference.getDeclaredSuperTypes(), _function);
    final Function1<TypeReference, Boolean> _function_1 = (TypeReference it) -> {
      Type _type = it.getType();
      return Boolean.valueOf((_type instanceof ClassDeclaration));
    };
    final Function1<TypeReference, Iterable<TypeReference>> _function_2 = (TypeReference it) -> {
      return this.getAllImplementedInterfaces(it);
    };
    Iterable<TypeReference> _flatMap = IterableExtensions.flatMap(IterableExtensions.filter(reference.getDeclaredSuperTypes(), _function_1), _function_2);
    return Iterables.<TypeReference>concat(_filter, _flatMap);
  }

  private String getIdentifier(final ResolvedMethod method) {
    String _simpleName = method.getDeclaration().getSimpleName();
    String _plus = (_simpleName + ",");
    final Function1<ParameterDeclaration, String> _function = (ParameterDeclaration it) -> {
      return it.getType().getName();
    };
    String _join = IterableExtensions.join(IterableExtensions.map(method.getDeclaration().getParameters(), _function), ",");
    return (_plus + _join);
  }

  private <T extends Object> Iterable<T> uniqueBy(final Iterable<T> elements, final Function1<? super T, ?> identifier) {
    final Function1<List<T>, T> _function = (List<T> it) -> {
      return it.get(0);
    };
    return IterableExtensions.<List<T>, T>map(IterableExtensions.<Object, T>groupBy(elements, identifier).values(), _function);
  }

  private static List<String> check(final boolean condition, final String message) {
    List<String> _xifexpression = null;
    if ((!condition)) {
      _xifexpression = List.<String>of(message);
    } else {
      _xifexpression = CollectionLiterals.<String>emptyList();
    }
    return _xifexpression;
  }

  public OperatorNameProcessor(final Class<? extends Annotation> annotationType) {
    super();
    this.annotationType = annotationType;
  }
}
