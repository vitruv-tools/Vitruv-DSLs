package tools.vitruv.dsls.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;

@SuppressWarnings("all")
public class JavaImportHelper {
  public static final char FQN_SEPARATOR = '.';

  private static final Set<String> NO_IMPORT_NEEDED = Collections.<String>singleton("java.lang");

  private final Map<String, String> imports = CollectionLiterals.<String, String>newHashMap();

  private final Map<String, String> staticImports = CollectionLiterals.<String, String>newHashMap();

  public CharSequence generateImportCode() {
    StringConcatenation _builder = new StringConcatenation();
    {
      Collection<String> _values = this.imports.values();
      for(final String i : _values) {
        _builder.append("import ");
        _builder.append(i);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      Collection<String> _values_1 = this.staticImports.values();
      for(final String i_1 : _values_1) {
        _builder.append("import static ");
        _builder.append(i_1);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }

  public String staticRef(final Class<?> javaClass, final String methodName) {
    boolean _containsKey = this.staticImports.containsKey(methodName);
    boolean _not = (!_containsKey);
    if (_not) {
      this.staticImports.put(methodName, javaClass.getName());
      return methodName;
    }
    String _name = javaClass.getName();
    String _plus = (_name + Character.valueOf(JavaImportHelper.FQN_SEPARATOR));
    return (_plus + methodName);
  }

  public String typeRef(final ClassNameGenerator nameGenerator) {
    return this.typeRef(nameGenerator.getQualifiedName());
  }

  public String typeRef(final Class<?> javaClass) {
    return this.typeRef(javaClass.getName());
  }

  public String typeRef(final EClassifier eClassifier) {
    return this.typeRef(eClassifier.getInstanceTypeName());
  }

  public String typeRef(final CharSequence fullyQualifiedJVMName) {
    final String fullyQualifiedJVMNameString = fullyQualifiedJVMName.toString();
    boolean _isSimpleName = JavaImportHelper.isSimpleName(fullyQualifiedJVMNameString);
    if (_isSimpleName) {
      return fullyQualifiedJVMNameString;
    }
    final GenericClassNameGenerator className = ClassNameGenerator.fromQualifiedName(fullyQualifiedJVMNameString);
    boolean _contains = JavaImportHelper.NO_IMPORT_NEEDED.contains(className.getPackageName());
    if (_contains) {
      return className.getSimpleName();
    }
    boolean _containsKey = this.imports.containsKey(className.getSimpleName());
    boolean _not = (!_containsKey);
    if (_not) {
      this.imports.put(className.getSimpleName(), className.getQualifiedName());
    } else {
      boolean _equals = this.imports.get(className.getSimpleName()).equals(fullyQualifiedJVMNameString);
      boolean _not_1 = (!_equals);
      if (_not_1) {
        return className.getQualifiedName();
      }
    }
    return className.getSimpleName();
  }

  private static boolean isSimpleName(final String fqn) {
    final int lastSeparatorPos = fqn.lastIndexOf(JavaImportHelper.FQN_SEPARATOR);
    return (lastSeparatorPos == (-1));
  }
}
