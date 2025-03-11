package tools.vitruv.dsls.common;

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class GenericClassNameGenerator implements ClassNameGenerator {
  private final String packageName;

  private final String className;

  @Override
  public String getSimpleName() {
    return this.className;
  }

  @Override
  public String getPackageName() {
    return this.packageName;
  }

  public GenericClassNameGenerator(final String packageName, final String className) {
    super();
    this.packageName = packageName;
    this.className = className;
  }
}
