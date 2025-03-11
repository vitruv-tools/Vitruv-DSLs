package tools.vitruv.dsls.common;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtend2.lib.StringConcatenation;

@Utility
@SuppressWarnings("all")
public final class JavaFileGenerator {
  private static final String JAVA_FILE_EXTENSION = ".java";

  public static final String FSA_SEPARATOR = "/";

  public static CharSequence generateClass(final CharSequence classImplementation, final String packageName, final JavaImportHelper importHelper) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    _builder.append(packageName);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    CharSequence _generateImportCode = importHelper.generateImportCode();
    _builder.append(_generateImportCode);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append(classImplementation);
    _builder.newLineIfNotEmpty();
    return _builder;
  }

  public static String getJavaFilePath(final String qualifiedClassName) {
    String _replace = qualifiedClassName.replace(".", JavaFileGenerator.FSA_SEPARATOR);
    return (_replace + JavaFileGenerator.JAVA_FILE_EXTENSION);
  }

  public static String getJavaFilePath(final ClassNameGenerator className) {
    return JavaFileGenerator.getJavaFilePath(className.getQualifiedName());
  }

  private JavaFileGenerator() {
    
  }
}
