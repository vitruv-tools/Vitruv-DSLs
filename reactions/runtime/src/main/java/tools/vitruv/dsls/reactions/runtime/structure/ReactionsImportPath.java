package tools.vitruv.dsls.reactions.runtime.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Describes paths between reactions segments inside the reactions import hierarchy.
 */
public record ReactionsImportPath(List<String> segments) {

  private static final ReactionsImportPath EMPTY_PATH = new ReactionsImportPath(List.of());

  /**
   * The separator used between path segments in the String representation of
   * {@link ReactionsImportPath ReactionsImportPaths}.
   */
  public static final String PATH_STRING_SEPARATOR = ".";

  private static final Pattern PATH_STRING_SEPARATOR_PATTERN =
      Pattern.compile(Pattern.quote(PATH_STRING_SEPARATOR));

  /**
   * Creates a {@link ReactionsImportPath} from the given path String.
   *
   * @param pathString A path string, with segments separated by '.'.
   * @return the reactions import path.
   */
  public static ReactionsImportPath fromPathString(String pathString) {
    if (pathString == null || pathString.isEmpty()) {
      return EMPTY_PATH;
    }
    var pathSegments = PATH_STRING_SEPARATOR_PATTERN.split(pathString, -1);
    return create(List.of(pathSegments));
  }

  /**
   * Creates a new ReactionsImportPath from one or more path segments.
   *
   * @param pathSegments - {@link Iterable}
   * @return new ReactionsImportPath
   */
  public static ReactionsImportPath create(Iterable<String> pathSegments) {
    if (pathSegments == null || !pathSegments.iterator().hasNext()) {
      return EMPTY_PATH;
    }
    var segmentList = new LinkedList<String>();
    pathSegments.forEach(segmentList::add);
    return new ReactionsImportPath(segmentList);
  }

  /**
   * Creates a new ReactionsImportPath that joins parentPath with pathSegments.
   *
   * @param parentPath - {@link Iterable}
   * @param pathSegments - {@link Iterable}
   * @return new ReactionsImportPath
   */
  public static ReactionsImportPath create(
      Iterable<String> parentPath, Iterable<String> pathSegments) {
    var actualParentPath = parentPath != null ? parentPath : List.<String>of();
    var actualPathSegments = pathSegments != null ? pathSegments : List.<String>of();
    return create(Iterables.concat(actualParentPath, actualPathSegments));
  }

  /**
   * Creates a new ReactionsImportPath whose segments are stored in segments.
   *
   * @param segments - {@link List}, must not be null.
   */
  public ReactionsImportPath(List<String> segments) {
    checkNotNull(segments, "pathSegments is null");
    this.segments = ImmutableList.copyOf(segments);
  }

  /**
   * Gets the number of segments this import path consists of.
   *
   * @return int
   */
  public int getLength() {
    return segments.size();
  }

  /**
   * Checks whether this import path is empty.
   *
   * @return <code>true</code> if there are no segments.
   */
  public boolean isEmpty() {
    return (this.getLength() == 0);
  }

  /**
   * Gets the path segment at the specified index.
   *
   * @param index int
   * @return the path segment at the specified index
   */
  public String getSegment(int index) {
    return segments.get(index);
  }

  /**
   * Gets the last path segment.
   *
   * @return the last path segment.
   */
  public String getLastSegment() {
    return segments.get(segments.size() - 1);
  }

  /**
   * Gets the first path segment.
   *
   * @return The first path segment.
   */
  public String getFirstSegment() {
    return segments.get(0);
  }

  /**
   * Creates a reactions import path with the given segment appended.
   *
   * @param pathSegment The path segment to append. Can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(String pathSegment) {
    return this.append(List.of(pathSegment));
  }

  /**
   * Creates a reactions import path with the given segments appended.
   *
   * @param pathSegments The path segments to append. Can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(Iterable<String> pathSegments) {
    return ReactionsImportPath.create(this.segments, pathSegments);
  }

  /**
   * Creates a reactions import path with the given path appended.
   *
   * @param path The path to append. Can be <code>null</code>.
   * @return the resulting reactions import path
   */
  public ReactionsImportPath append(ReactionsImportPath path) {
    return ReactionsImportPath.create(this.segments, path != null ? path.segments : null);
  }

  /**
   * Creates a reactions import path with the given segments prepended.
   *
   * @param pathSegments The path segments to prepend. Can be <code>null</code>
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(String pathSegments) {
    return this.prepend(List.of(pathSegments));
  }

  /**
   * Creates a reactions import path with the given segments prepended.
   *
   * @param pathSegments The path segments to prepend. Can be <code>null</code>.
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(Iterable<String> pathSegments) {
    return ReactionsImportPath.create(pathSegments, this.segments);
  }

  /**
   * Creates a reactions import path with the given path prepended.
   *
   * @param path The path to prepend. Can be <code>null</code>.
   * @return the resulting reactions import path
   */
  public ReactionsImportPath prepend(ReactionsImportPath path) {
    return ReactionsImportPath.create(path != null ? path.segments : null, this.segments);
  }

  /**
   * Gets the reactions import path with the last segment omitted.
   *
   * @return the resulting reactions import path. Can be empty.
   */
  public ReactionsImportPath getParent() {
    var parentPath = segments.subList(0, getLength() - 1);
    return ReactionsImportPath.create(parentPath);
  }

  /**
   * Gets the reactions import path with the first segment omitted.
   *
   * @return The resulting reactions import path. Can be empty.
   */
  public ReactionsImportPath relativeToRoot() {
    var tailPath = segments.subList(1, getLength());
    return ReactionsImportPath.create(tailPath);
  }

  /**
   * Creates a reactions import path that is the sub-path of this path,
   * starting with the segment following <code>pathSegment</code>.
   *
   * @param pathSegment - {@link String}
   * @return The resulting reactions import path, or an empty path
   *      if the specified segment is not contained or the last segment of this path.
   */
  public ReactionsImportPath relativeTo(String pathSegment) {
    var index = segments.indexOf(pathSegment);
    if (index == -1 || index == getLength() - 1) {
      // segment is not contained or the last segment of the path:
      return EMPTY_PATH;
    }
    var relativePathSegments = segments.subList(index + 1, getLength());
    return ReactionsImportPath.create(relativePathSegments);
  }

  /**
   * Creates a reactions import path that is the sub-path of this path,
   * ending with <code>pathSegment</code>.
   *
   * @param pathSegment - {@link String}
   * @return the resulting reactions import path, or an empty path
   *      if the specified segment is not contained in this path
   */
  public ReactionsImportPath subPathTo(String pathSegment) {
    var index = segments.indexOf(pathSegment);
    if (index == -1) {
      // segment is not contained in this path:
      return EMPTY_PATH;
    }
    var subPathSegments = segments.subList(0, index + 1);
    return ReactionsImportPath.create(subPathSegments);
  }

  /**
   * Gets the String representation of this import path.
   *
   * @return all segments concatenated by '.'
   */
  public String getPathString() {
    return String.join(PATH_STRING_SEPARATOR, segments);
  }
}
