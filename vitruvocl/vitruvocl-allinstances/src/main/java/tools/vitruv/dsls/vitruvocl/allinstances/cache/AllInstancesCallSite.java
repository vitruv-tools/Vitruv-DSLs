package tools.vitruv.dsls.vitruvocl.allinstances.cache;

import org.eclipse.emf.ecore.EClass;

/**
 * Represents one statically-discovered {@code allInstances()}-style call site in a query (e.g. an
 * OCL expression), as used by the static analysis of Wei &amp; Kolovos (BigMDE 2015, Section 3.1).
 *
 * <p>Callers building this list are expected to walk their own query AST (e.g. a parsed OCL
 * expression tree) and emit one {@code AllInstancesCallSite} per {@code allInstances()},
 * {@code getAllOfType()}, or {@code getAllOfKind()} invocation found, classifying it by {@link
 * Kind}.
 *
 * @param targetType the metaclass the call site queries
 * @param kind whether subtypes should be included ({@link Kind#ALL_OF_KIND}) or not ({@link
 *     Kind#ALL_OF_TYPE})
 */
public record AllInstancesCallSite(EClass targetType, Kind kind) {

  /** Distinguishes OCL's {@code allInstances()} semantics from an exact-type-only query. */
  public enum Kind {
    /** Include instances of {@code targetType} and all of its subtypes (OCL {@code allInstances()}). */
    ALL_OF_KIND,
    /** Include only instances whose exact metaclass is {@code targetType}. */
    ALL_OF_TYPE
  }
}
