package tools.vitruv.dsls.testutils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.Files.walk;
import static com.google.common.collect.FluentIterable.from;

import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.testing.InMemoryJavaCompiler;
import org.eclipse.xtext.xbase.testing.InMemoryJavaCompiler.Result;
import org.eclipse.xtext.xbase.testing.JavaSource;
import static java.lang.reflect.Modifier.isPublic;
import com.google.common.base.Predicate;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.join;

import static com.google.common.base.Preconditions.checkState;

/**
 * Allows to compile all Java source files in a given folder at runtime and provides the compiled classes to use
 * afterwards. {@link #compile()} has to be called first to compiles the classes. Afterwards, classes can be retrieved
 * by calling {@link #getCompiledClasses()} or even instantiated calling
 * {@link #filterAndInstantiateClasses(Class, Predicate)}.
 */
public class InMemoryClassesCompiler {
	private final Path javaSourcesFolder;
	private Set<? extends Class<?>> compiledClasses;

	/**
	 * Instantiates a compiler for the source files in the given directory.
	 * @param javaSourcesFolder the directory to compile Java files in, must not be {@code null}
	 */
	public InMemoryClassesCompiler(Path javaSourcesFolder) {
		this.javaSourcesFolder = javaSourcesFolder;
	}

	/**
	 * Compiles the sources files in the directory passed to the constructor.
	 * @return itself (i.e. {@code this}) to allow retrieval of compiled classes or their instances by calling the according
	 * methods right after compilation
	 * @throws IOException if the directory does not exist or cannot be traversed successfully
	 */
	public InMemoryClassesCompiler compile() throws IOException {
		checkState(compiledClasses == null, "classes have already been compiled");
		this.compiledClasses = compileJavaFiles(from(walk(javaSourcesFolder).collect(Collectors.toList()))
				.filter(path -> path.toString().endsWith(".java")).transform(path -> new RelativeAndAbsolutePath(javaSourcesFolder, path)).toList());
		return this;
	}

	private Set<? extends Class<?>> compileJavaFiles(Iterable<RelativeAndAbsolutePath> sourceFilePaths) {
		InMemoryJavaCompiler compiler = new InMemoryJavaCompiler(getClass().getClassLoader(), JavaVersion.JAVA17);
		Result result = compiler.compile(from(sourceFilePaths)
				.transform(path -> new JavaSource(path.getRelative().toString(), readFile(path.getAbsolute()))).toArray(JavaSource.class));
		// use the same class loader for all classes!
		ClassLoader classLoader = result.getClassLoader();
		if (from(result.getCompilationProblems()).anyMatch(problem -> problem.isError())) {
			throw new AssertionError("compiling the generated code failed with these errors:" + lineSeparator()
					+ join(from(result.getCompilationProblems()).filter(problem -> problem.isError()), lineSeparator(),
							(problem -> "    • " + format(problem))));
		}
		return from(sourceFilePaths).transform(path -> loadClass(classLoader, getClassName(path.getRelative()))).toSet();
	}

	private static Class<?> loadClass(ClassLoader classLoader, String className) {
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static String readFile(Path path) {
		try {
			return readString(path);
		} catch (IOException e) {
			return "";
		}
	}

	private String format(CategorizedProblem problem) {
		return problem.getMessage() + " (" + new String(problem.getOriginatingFileName()) + ":" + problem.getSourceLineNumber();
	}

	private static String getClassName(Path path) {
		return join(path, ".").replaceFirst("\\.java$", "");
	}

	/**
	 * Returns the compiled classes. {@link #compile()} must be called first.
	 * @return the compiled classes
	 */
	public Set<? extends Class<?>> getCompiledClasses() {
		checkState(compiledClasses != null, "classes must have been compiled");
		return compiledClasses;
	}

	/**
	 * Filters the compiled classes by the given type and condition and returns instances of them. {@link #compile()} must
	 * be called first.
	 * @param <T> the type of classes to filter
	 * @param typesToInstantiate the type of classes to filter
	 * @param conditionToInstantiate a condition that must evaluate to {@code true} for a class to instantiate it
	 * @return instances of the filtered classes
	 */
	public <T> Set<T> filterAndInstantiateClasses(Class<T> typesToInstantiate, Predicate<T> conditionToInstantiate) {
		checkState(compiledClasses != null, "classes must have been compiled");
		return from(compiledClasses).filter(InMemoryClassesCompiler::isPublicAndHasPublicConstructor).transform(InMemoryClassesCompiler::instantiate)
				.filter(typesToInstantiate).filter(conditionToInstantiate).toSet();
	}

	private static boolean isPublicAndHasPublicConstructor(Class<?> clazz) {
		try {
			return isPublic(clazz.getModifiers()) && isPublic(clazz.getDeclaredConstructor().getModifiers());
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}

	private static Object instantiate(Class<?> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			return null;
		}
	}

	private static class RelativeAndAbsolutePath {
		private final Path baseDir;
		private final Path path;

		RelativeAndAbsolutePath(final Path baseDir, final Path path) {
			this.baseDir = baseDir;
			this.path = path.isAbsolute() ? baseDir.relativize(path) : path;
		}

		public Path getRelative() {
			return path;
		}

		public Path getAbsolute() {
			return baseDir.resolve(path);
		}
	}
}
