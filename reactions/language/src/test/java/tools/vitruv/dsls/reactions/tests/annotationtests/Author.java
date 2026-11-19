package tools.vitruv.dsls.reactions.tests.annotationtests;

/**
 * Change annotation that names the author of a change. It is attached to changes in the
 * {@link AnnotationTests} and read back by the reactions and routines.
 *
 * @param name name of the author
 */
public record Author(String name) {
}
