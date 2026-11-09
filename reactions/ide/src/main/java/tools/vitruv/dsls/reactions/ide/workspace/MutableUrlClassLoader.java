package tools.vitruv.dsls.reactions.ide.workspace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * A {@link URLClassLoader} that can have entries added after construction, so that model
 * projects discovered later in the session (see {@link ReactionsClasspathRegistrar}) become
 * visible to Xbase's JVM type resolution without restarting the language server.
 * <p>
 * Besides plain classpath entries, it can serve classes from a directory only for the class names
 * a filter accepts (see {@link #addFilteredClassesDirectory(File, Predicate)}). Plain entries win.
 */
public class MutableUrlClassLoader extends URLClassLoader {

  private static final String CLASS_SUFFIX = ".class";

  private final List<FilteredClassesDirectory> filteredDirectories = new CopyOnWriteArrayList<>();

  /**
   * Creates a new, empty {@link MutableUrlClassLoader} delegating to {@code parent}.
   *
   * @param parent - the parent {@link ClassLoader}
   */
  public MutableUrlClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  /**
   * Adds {@code url} to the set of URLs this class loader resolves classes and resources from.
   *
   * @param url - the {@link URL} to add
   */
  public void addClasspathUrl(URL url) {
    addURL(url);
  }

  /**
   * Adds {@code classesDirectory}, but only for the classes whose binary name {@code
   * classNameFilter} accepts. Other classes and non-class resources in it stay invisible.
   *
   * @param classesDirectory - a directory of compiled classes, e.g. {@code target/classes}
   * @param classNameFilter - accepts the binary class names (e.g. {@code a.b.C$D}) to serve
   */
  public void addFilteredClassesDirectory(File classesDirectory,
      Predicate<String> classNameFilter) {
    filteredDirectories.add(new FilteredClassesDirectory(classesDirectory, classNameFilter));
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    try {
      return super.findClass(name);
    } catch (ClassNotFoundException e) {
      File classFile = findFilteredClassFile(name);
      if (classFile == null) {
        throw e;
      }
      try {
        byte[] bytes = Files.readAllBytes(classFile.toPath());
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException readFailure) {
        throw new ClassNotFoundException(name, readFailure);
      }
    }
  }

  @Override
  public URL findResource(String name) {
    URL url = super.findResource(name);
    if (url != null || !name.endsWith(CLASS_SUFFIX)) {
      return url;
    }
    String className = name.substring(0, name.length() - CLASS_SUFFIX.length()).replace('/', '.');
    File classFile = findFilteredClassFile(className);
    try {
      return classFile == null ? null : classFile.toURI().toURL();
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private File findFilteredClassFile(String className) {
    for (FilteredClassesDirectory filtered : filteredDirectories) {
      if (filtered.classNameFilter().test(className)) {
        File classFile = new File(filtered.directory(), className.replace('.', '/') + CLASS_SUFFIX);
        if (classFile.isFile()) {
          return classFile;
        }
      }
    }
    return null;
  }

  private record FilteredClassesDirectory(File directory, Predicate<String> classNameFilter) {
  }

}
