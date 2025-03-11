package tools.vitruv.dsls.reactions.runtime.structure;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * Describes paths between reactions segments inside the reactions import hierarchy.
 */
@SuppressWarnings("all")
public class ReactionsImportPath {
  public static final ReactionsImportPath EMPTY_PATH = new ReactionsImportPath(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));

  /**
   * The separator used between path segments in the String representation of {@link ReactionsImportPath ReactionsImportPaths}.
   */
  public static final String PATH_STRING_SEPARATOR = ".";

  private static final Pattern PATH_STRING_SEPARATOR_PATTERN = Pattern.compile(Pattern.quote(ReactionsImportPath.PATH_STRING_SEPARATOR));

  /**
   * Creates a {@link ReactionsImportPath} from the given path String.
   * 
   * @param pathString the pathString
   * @return the reactions import path
   */
  public static ReactionsImportPath fromPathString(final String pathString) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(pathString);
    if (_isNullOrEmpty) {
      return ReactionsImportPath.EMPTY_PATH;
    }
    final String[] pathSegments = ReactionsImportPath.PATH_STRING_SEPARATOR_PATTERN.split(pathString, (-1));
    return ReactionsImportPath.create(pathSegments);
  }

  public static ReactionsImportPath create(final Iterable<String> pathSegments) {
    boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(pathSegments);
    if (_isNullOrEmpty) {
      return ReactionsImportPath.EMPTY_PATH;
    }
    return new ReactionsImportPath(pathSegments);
  }

  public static ReactionsImportPath create(final String... pathSegments) {
    return ReactionsImportPath.create(((Iterable<String>) Conversions.doWrapArray(pathSegments)));
  }

  public static ReactionsImportPath create(final Iterable<String> parentPath, final String... pathSegments) {
    return ReactionsImportPath.create(parentPath, ((Iterable<String>) Conversions.doWrapArray(pathSegments)));
  }

  public static ReactionsImportPath create(final Iterable<String> parentPath, final Iterable<String> pathSegments) {
    Iterable<String> _elvis = null;
    if (parentPath != null) {
      _elvis = parentPath;
    } else {
      _elvis = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
    }
    Iterable<String> _elvis_1 = null;
    if (pathSegments != null) {
      _elvis_1 = pathSegments;
    } else {
      _elvis_1 = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList());
    }
    Iterable<String> _plus = Iterables.<String>concat(_elvis, _elvis_1);
    return ReactionsImportPath.create(_plus);
  }

  private final List<String> segments;

  private ReactionsImportPath(final Iterable<String> pathSegments) {
    Preconditions.<Iterable<String>>checkNotNull(pathSegments, "pathSegments is null");
    this.segments = ImmutableList.<String>copyOf(pathSegments);
  }

  /**
   * Gets an unmodifiable view on the segments of this import path.
   * 
   * @return an unmodifiable view on the segments of this import path, can be empty
   */
  public List<String> getSegments() {
    return this.segments;
  }

  /**
   * Gets the number of segments this import path consists of.
   * 
   * @return the number of segments this import path consists of
   */
  public int getLength() {
    return this.segments.size();
  }

  /**
   * Checks whether this import path is empty.
   * 
   * @return <code>true</code> if this import path is empty
   */
  public boolean isEmpty() {
    int _length = this.getLength();
    return (_length == 0);
  }

  /**
   * Gets the path segment at the specified index.
   * 
   * @param index the index
   * @return the path segment at the specified index
   */
  public String getSegment(final int index) {
    return this.segments.get(index);
  }

  /**
   * Gets the last path segment.
   * 
   * @return the last path segment
   */
  public String getLastSegment() {
    int _size = this.segments.size();
    int _minus = (_size - 1);
    return this.segments.get(_minus);
  }

  /**
   * Gets the first path segment.
   * 
   * @return the first path segment
   */
  public String getFirstSegment() {
    return this.segments.get(0);
  }

  /**
   * Creates a reactions import path with the given segments appended.
   * 
   * @param pathSegments the path segments to append, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(final String... pathSegments) {
    return this.append(((Iterable<String>) Conversions.doWrapArray(pathSegments)));
  }

  /**
   * Creates a reactions import path with the given segments appended.
   * 
   * @param pathSegments the path segments to append, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(final Iterable<String> pathSegments) {
    return ReactionsImportPath.create(this.segments, pathSegments);
  }

  /**
   * Creates a reactions import path with the given path appended.
   * 
   * @param path the path to append, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(final ReactionsImportPath path) {
    List<String> _segments = null;
    if (path!=null) {
      _segments=path.segments;
    }
    return ReactionsImportPath.create(this.segments, _segments);
  }

  /**
   * Creates a reactions import path with the given segments prepended.
   * 
   * @param pathSegments the path segments to prepend, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(final String... pathSegments) {
    return this.prepend(((Iterable<String>) Conversions.doWrapArray(pathSegments)));
  }

  /**
   * Creates a reactions import path with the given segments prepended.
   * 
   * @param pathSegments the path segments to prepend, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(final Iterable<String> pathSegments) {
    return ReactionsImportPath.create(pathSegments, this.segments);
  }

  /**
   * Creates a reactions import path with the given path prepended.
   * 
   * @param path the path to prepend, can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(final ReactionsImportPath path) {
    List<String> _segments = null;
    if (path!=null) {
      _segments=path.segments;
    }
    return ReactionsImportPath.create(_segments, this.segments);
  }

  /**
   * Gets the reactions import path with the last segment omitted.
   * 
   * @return the resulting reactions import path, can be empty
   */
  public ReactionsImportPath getParent() {
    int _length = this.getLength();
    int _minus = (_length - 1);
    final Iterable<String> parentPath = IterableExtensions.<String>take(this.segments, _minus);
    return ReactionsImportPath.create(parentPath);
  }

  /**
   * Gets the reactions import path with the first segment omitted.
   * 
   * @return the resulting reactions import path, can be empty
   */
  public ReactionsImportPath relativeToRoot() {
    final Iterable<String> tailPath = IterableExtensions.<String>tail(this.segments);
    return ReactionsImportPath.create(tailPath);
  }

  /**
   * Creates a reactions import path that is the sub-path of this path starting with the segment following the specified segment.
   * 
   * @return the resulting reactions import path, or an empty path if the specified segment is not contained or the last segment of this path
   */
  public ReactionsImportPath relativeTo(final String pathSegment) {
    final int index = this.segments.indexOf(pathSegment);
    if (((index == (-1)) || (index == (this.getLength() - 1)))) {
      return ReactionsImportPath.EMPTY_PATH;
    }
    final List<String> relativePathSegments = this.segments.subList((index + 1), this.getLength());
    return ReactionsImportPath.create(relativePathSegments);
  }

  /**
   * Creates a reactions import path that is the sub-path of this path ending with the specified segment.
   * 
   * @return the resulting reactions import path, or an empty path if the specified segment is not contained in this path
   */
  public ReactionsImportPath subPathTo(final String pathSegment) {
    final int index = this.segments.indexOf(pathSegment);
    if ((index == (-1))) {
      return ReactionsImportPath.EMPTY_PATH;
    }
    final List<String> subPathSegments = this.segments.subList(0, (index + 1));
    return ReactionsImportPath.create(subPathSegments);
  }

  /**
   * Gets the String representation of this import path.
   * 
   * @return the String representation of this import path
   */
  public String getPathString() {
    return IterableExtensions.join(this.segments, ReactionsImportPath.PATH_STRING_SEPARATOR);
  }

  @Override
  public String toString() {
    String _pathString = this.getPathString();
    return ("ReactionsImportPath=" + _pathString);
  }

  @Override
  public int hashCode() {
    return this.segments.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if ((this == obj)) {
      return true;
    }
    if ((obj == null)) {
      return false;
    }
    if ((!(obj instanceof ReactionsImportPath))) {
      return false;
    }
    final ReactionsImportPath other = ((ReactionsImportPath) obj);
    boolean _equals = this.segments.equals(other.segments);
    boolean _not = (!_equals);
    if (_not) {
      return false;
    }
    return true;
  }
}
