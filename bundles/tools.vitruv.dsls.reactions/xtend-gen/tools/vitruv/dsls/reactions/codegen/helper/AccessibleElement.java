package tools.vitruv.dsls.reactions.codegen.helper;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.util.List;
import java.util.Optional;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUnknownTypeReference;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class AccessibleElement {
  private static final String UNKNOWN_TYPEREF_NAME = "unknown";

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final String name;

  private final String fullyQualifiedTypeName;

  private final List<String> typeParameters;

  public AccessibleElement(final String name, final String fullyQualifiedTypeName) {
    this.name = name;
    this.fullyQualifiedTypeName = fullyQualifiedTypeName;
    this.typeParameters = CollectionLiterals.<String>newArrayList();
  }

  public AccessibleElement(final String name, final String fullyQualifiedTypeName, final String... typeParameters) {
    this(name, fullyQualifiedTypeName);
    Iterables.<String>addAll(this.typeParameters, ((Iterable<? extends String>)Conversions.doWrapArray(typeParameters)));
  }

  public AccessibleElement(final String name, final Class<?> type) {
    this(name, type.getName());
  }

  public boolean isOptional() {
    return Optional.class.getName().equals(this.fullyQualifiedTypeName);
  }

  public JvmTypeReference generateTypeRef(@Extension final JvmTypeReferenceBuilder typeReferenceBuilder) {
    JvmTypeReference _xblockexpression = null;
    {
      if ((this.fullyQualifiedTypeName == null)) {
        return typeReferenceBuilder.typeRef(AccessibleElement.UNKNOWN_TYPEREF_NAME);
      }
      final Function1<String, String> _function = (String it) -> {
        String _xifexpression = null;
        if ((it == null)) {
          _xifexpression = AccessibleElement.UNKNOWN_TYPEREF_NAME;
        } else {
          _xifexpression = it;
        }
        return _xifexpression;
      };
      final Function1<String, JvmTypeReference> _function_1 = (String it) -> {
        return typeReferenceBuilder.typeRef(it);
      };
      final List<JvmTypeReference> typeParameterReferences = IterableUtil.<String, JvmTypeReference>mapFixed(ListExtensions.<String, String>map(this.typeParameters, _function), _function_1);
      JvmTypeReference _xifexpression = null;
      final Function1<JvmTypeReference, Boolean> _function_2 = (JvmTypeReference it) -> {
        return Boolean.valueOf((it instanceof JvmUnknownTypeReference));
      };
      boolean _exists = IterableExtensions.<JvmTypeReference>exists(typeParameterReferences, _function_2);
      if (_exists) {
        _xifexpression = typeReferenceBuilder.typeRef(this.toJavaCode());
      } else {
        _xifexpression = typeReferenceBuilder.typeRef(this.fullyQualifiedTypeName, ((JvmTypeReference[])Conversions.unwrapArray(typeParameterReferences, JvmTypeReference.class)));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private String toJavaCode() {
    String _xifexpression = null;
    boolean _isEmpty = this.typeParameters.isEmpty();
    if (_isEmpty) {
      _xifexpression = this.fullyQualifiedTypeName;
    } else {
      final Function1<String, CharSequence> _function = (String it) -> {
        return it;
      };
      String _join = IterableExtensions.<String>join(this.typeParameters, "<", ", ", ">", _function);
      _xifexpression = (this.fullyQualifiedTypeName + _join);
    }
    return _xifexpression;
  }

  @Override
  public String toString() {
    return this.toJavaCode();
  }

  @Pure
  public String getName() {
    return this.name;
  }
}
