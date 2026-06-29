/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.lsp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OclOperationDocs {

  // S1192 – string-literal constants
  private static final String T_BOOLEAN = "Boolean";
  private static final String PARAM_RIGHT = "right";
  private static final String DESC_SECOND_OPERAND = "The second operand";
  private static final String PARAM_ITERATOR = "iterator";
  private static final String DESC_ITERATOR = "The loop variable bound to each element";
  private static final String PARAM_CONDITION = "condition";
  private static final String DESC_PREDICATE = "The predicate tested per element";
  private static final String PARAM_ELEMENT = "element";
  private static final String PARAM_OTHER = "other";
  private static final String T_COLLECTION = "Collection<T>";
  private static final String T_INTEGER = "Integer";
  private static final String DESC_SCALAR_RECEIVER = "Requires a scalar (singleton) receiver.";
  private static final String T_OCL_TYPE = "OclType";
  private static final String T_STRING = "String";
  private static final String PARAM_PATTERN = "pattern";
  private static final String DESC_NO_STRING_ORDERING =
      "String ordering is NOT supported in OCL#.";
  private static final String ITER_PARAM_PART = ": T | ";
  private static final String RETURNS_COLL = ": T) → ";

  public record OperationDoc(
      String signature, String description, String returnDescription, List<ParamDoc> params) {}

  public record ParamDoc(String name, String type, String description) {}

  public static Optional<OperationDoc> lookup(String tokenText) {
    return Optional.ofNullable(DOCS.get(tokenText));
  }

  private static final Map<String, OperationDoc> DOCS;

  static {
    Map<String, OperationDoc> m = new LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Logical operators
    // ------------------------------------------------------------------

    m.put(
        "implies",
        op(
            T_BOOLEAN + ".implies(" + PARAM_RIGHT + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Logical implication. Evaluates to `true` whenever the left operand is `false`, "
                + "or whenever both operands are `true`. Equivalent to `not self or right`.",
            "`true` unless `self` is `true` and `right` is `false`",
            param(PARAM_RIGHT, T_BOOLEAN, "The consequent expression (right-hand side)")));

    m.put(
        "and",
        op(
            T_BOOLEAN + ".and(" + PARAM_RIGHT + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Logical conjunction. Both operands must be `true` for the result to be `true`.",
            "`true` if and only if both `self` and `right` are `true`",
            param(PARAM_RIGHT, T_BOOLEAN, DESC_SECOND_OPERAND)));

    m.put(
        "or",
        op(
            T_BOOLEAN + ".or(" + PARAM_RIGHT + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Logical disjunction. At least one operand must be `true`.",
            "`true` if `self` or `right` (or both) are `true`",
            param(PARAM_RIGHT, T_BOOLEAN, DESC_SECOND_OPERAND)));

    m.put(
        "xor",
        op(
            T_BOOLEAN + ".xor(" + PARAM_RIGHT + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Exclusive disjunction. Exactly one of the two operands must be `true`.",
            "`true` if exactly one of `self` or `right` is `true`",
            param(PARAM_RIGHT, T_BOOLEAN, DESC_SECOND_OPERAND)));

    m.put(
        "not",
        op(
            "not(operand: " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Logical negation. Inverts the truth value of the operand.",
            "`true` if `operand` is `false`, `false` otherwise",
            param("operand", T_BOOLEAN, "The expression to negate")));

    // ------------------------------------------------------------------
    // Iterator operations
    // ------------------------------------------------------------------

    m.put(
        "select",
        op(
            T_COLLECTION + ".select(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → " + T_COLLECTION,
            "Filters a collection to the subset for which the body expression evaluates to `true`. "
                + "Preserves the collection kind (Set stays Set, Sequence stays Sequence).",
            "A sub-collection containing only the elements that satisfy `condition`",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, "The filter predicate evaluated per element")));

    m.put(
        "reject",
        op(
            T_COLLECTION + ".reject(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → " + T_COLLECTION,
            "Filters a collection to the subset for which the body expression evaluates to `false`."
                + " Complementary to `select`.",
            "A sub-collection of elements for which `condition` is `false`",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, "Elements matching this are excluded")));

    m.put(
        "collect",
        op(
            T_COLLECTION + ".collect(" + PARAM_ITERATOR + ": T | expr: V) → Collection<V>",
            "Transforms each element by evaluating `expr` and collects the results into a new "
                + "collection of the same kind. Equivalent to a map operation.",
            "A new collection of transformed values (type V)",
            param(PARAM_ITERATOR, "T", "The loop variable bound to each source element"),
            param("expr", "V", "The expression that produces the value for each result element")));

    m.put(
        "forAll",
        op(
            T_COLLECTION + ".forAll(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Universal quantifier. Returns `true` when the body evaluates to `true` for every "
                + "element. Returns `true` for empty collections (vacuous truth).",
            "`true` if `condition` holds for all elements; `false` as soon as one element fails",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, "The predicate that must hold for every element")));

    m.put(
        "exists",
        op(
            T_COLLECTION + ".exists(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Existential quantifier. Returns `true` when the body evaluates to `true` for at least "
                + "one element. Returns `false` for empty collections.",
            "`true` if at least one element satisfies `condition`",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, DESC_PREDICATE)));

    m.put(
        "one",
        op(
            T_COLLECTION + ".one(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → " + T_BOOLEAN,
            "Returns `true` if exactly one element satisfies the predicate.",
            "`true` if exactly one element satisfies `condition`",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, DESC_PREDICATE)));

    m.put(
        "any",
        op(
            T_COLLECTION + ".any(" + PARAM_ITERATOR + ITER_PARAM_PART + PARAM_CONDITION + ": " + T_BOOLEAN + ") → T",
            "Returns an arbitrary element satisfying the predicate. "
                + "Result is undefined if no element matches.",
            "A singleton containing one matching element, or empty if none found",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param(PARAM_CONDITION, T_BOOLEAN, DESC_PREDICATE)));

    m.put(
        "isUnique",
        op(
            T_COLLECTION + ".isUnique(" + PARAM_ITERATOR + ": T | expr: V) → " + T_BOOLEAN,
            "Returns `true` if the body expression evaluates to a distinct value for every"
                + " element.",
            "`true` if all body results are distinct",
            param(PARAM_ITERATOR, "T", DESC_ITERATOR),
            param("expr", "V", "Expression whose values must all be distinct")));

    m.put(
        "sortedBy",
        op(
            T_COLLECTION + ".sortedBy(" + PARAM_ITERATOR + ": T | key: Comparable) → OrderedSet<T>",
            "Returns an `OrderedSet` with all elements sorted in ascending order of the key"
                + " expression. Supports numeric keys and enum keys.",
            "An `OrderedSet<T>` ordered by `key` ascending",
            param(PARAM_ITERATOR, "T", "The loop variable"),
            param("key", "Comparable", "Expression whose value determines the sort order")));

    m.put(
        "collectNested",
        op(
            T_COLLECTION + ".collectNested(" + PARAM_ITERATOR + ": T | expr: V) → Collection<Collection<V>>",
            "Like `collect` but does NOT flatten — returns a collection of collections.",
            "A collection of collections (not flattened)",
            param(PARAM_ITERATOR, "T", "The loop variable bound to each source element"),
            param("expr", "V", "The expression evaluated per element")));

    m.put(
        "iterate",
        op(
            "Collection<T>.iterate(elem; acc: A = init | body: A) → A",
            "General-purpose fold/reduce operation. Iterates over the collection accumulating "
                + "a result. The accumulator `acc` starts at `init` and is updated by `body` "
                + "for each element.",
            "The final value of the accumulator",
            param("elem", "T", "Iterator variable bound to each element"),
            param("acc", "A", "Accumulator variable (declared with type and initial value)"),
            param("body", "A", "Expression that computes the new accumulator value")));

    // ------------------------------------------------------------------
    // Collection membership & size operations
    // ------------------------------------------------------------------

    m.put(
        "includes",
        op(
            T_COLLECTION + ".includes(" + PARAM_ELEMENT + RETURNS_COLL + T_BOOLEAN,
            "Tests whether the collection contains the given element (using equality).",
            "`true` if `element` is present in the collection",
            param(PARAM_ELEMENT, "T", "The value to search for")));

    m.put(
        "excludes",
        op(
            T_COLLECTION + ".excludes(" + PARAM_ELEMENT + RETURNS_COLL + T_BOOLEAN,
            "Tests whether the collection does not contain the given element.",
            "`true` if `element` is absent from the collection",
            param(PARAM_ELEMENT, "T", "The value to search for")));

    m.put(
        "includesAll",
        op(
            T_COLLECTION + ".includesAll(" + PARAM_OTHER + ": " + T_COLLECTION + ") → " + T_BOOLEAN,
            "Tests whether this collection contains all elements of `other`.",
            "`true` if every element of `other` is present in this collection",
            param(PARAM_OTHER, T_COLLECTION, "The collection whose elements to look for")));

    m.put(
        "excludesAll",
        op(
            T_COLLECTION + ".excludesAll(" + PARAM_OTHER + ": " + T_COLLECTION + ") → " + T_BOOLEAN,
            "Tests whether this collection contains none of the elements of `other`.",
            "`true` if no element of `other` is present in this collection",
            param(PARAM_OTHER, T_COLLECTION, "The collection whose elements must be absent")));

    m.put(
        "count",
        op(
            T_COLLECTION + ".count(" + PARAM_ELEMENT + RETURNS_COLL + T_INTEGER,
            "Returns how many times `element` occurs in the collection. "
                + "For Sets and OrderedSets this is always 0 or 1.",
            "The number of occurrences of `element`",
            param(PARAM_ELEMENT, "T", "The element to count")));

    m.put(
        "including",
        op(
            T_COLLECTION + ".including(" + PARAM_ELEMENT + RETURNS_COLL + T_COLLECTION,
            "Returns a new collection that contains all elements of the receiver plus `element`. "
                + "For Sets, if the element already exists it is not duplicated.",
            "A new collection with `element` added",
            param(PARAM_ELEMENT, "T", "The element to add")));

    m.put(
        "excluding",
        op(
            T_COLLECTION + ".excluding(" + PARAM_ELEMENT + RETURNS_COLL + T_COLLECTION,
            "Returns a new collection with all occurrences of `element` removed.",
            "A new collection without `element`",
            param(PARAM_ELEMENT, "T", "The element to remove")));

    m.put(
        "isEmpty",
        op(
            T_COLLECTION + ".isEmpty() → " + T_BOOLEAN,
            "Tests whether the collection contains no elements.",
            "`true` if the collection has zero elements"));

    m.put(
        "notEmpty",
        op(
            T_COLLECTION + ".notEmpty() → " + T_BOOLEAN,
            "Tests whether the collection contains at least one element.",
            "`true` if the collection has one or more elements"));

    m.put(
        "size",
        op(
            T_COLLECTION + ".size() → " + T_INTEGER,
            "Returns the number of elements in the collection. "
                + "Also works on String singletons (¡String!) to return the string length.",
            "The cardinality of the collection as a non-negative integer"));

    m.put(
        "flatten",
        op(
            "Collection<Collection<T>>.flatten() → Collection<T>",
            "Flattens a nested collection one level deep into a single-level collection.",
            "A flat collection containing all elements of the nested collections"));

    m.put(
        "union",
        op(
            T_COLLECTION + ".union(" + PARAM_OTHER + ": " + T_COLLECTION + ") → " + T_COLLECTION,
            "Returns a new collection containing all elements from both collections. "
                + "For Sets, duplicates are eliminated.",
            "The union of both collections",
            param(PARAM_OTHER, T_COLLECTION, "The collection to merge with")));

    m.put(
        "intersection",
        op(
            T_COLLECTION + ".intersection(" + PARAM_OTHER + ": " + T_COLLECTION + ") → " + T_COLLECTION,
            "Returns the elements present in both collections. "
                + "Result is unique if either operand is unique (Set/OrderedSet).",
            "A collection containing elements common to both",
            param(PARAM_OTHER, T_COLLECTION, "The collection to intersect with")));

    m.put(
        "symmetricDifference",
        op(
            "Set<T>.symmetricDifference(other: Set<T>) → Set<T>",
            "Returns elements present in exactly one of the two sets (not both). "
                + "Both operands must be Sets (unique collections).",
            "A Set containing elements in either collection but not both",
            param(PARAM_OTHER, "Set<T>", "The Set to compute symmetric difference with")));

    m.put(
        "append",
        op(
            "Sequence<T>.append(element: T) → Sequence<T>",
            "Appends `element` at the end of the sequence.",
            "A new `Sequence<T>` with `element` as the last item",
            param(PARAM_ELEMENT, "T", "The element to append")));

    m.put(
        "prepend",
        op(
            "Sequence<T>.prepend(element: T) → Sequence<T>",
            "Inserts `element` at the beginning of the sequence.",
            "A new `Sequence<T>` with `element` as the first item",
            param(PARAM_ELEMENT, "T", "The element to prepend")));

    m.put(
        "insertAt",
        op(
            "Sequence<T>.insertAt(index: " + T_INTEGER + ", " + PARAM_ELEMENT + ": T) → Sequence<T>",
            "Inserts `element` at the given 1-based `index` in the sequence.",
            "A new `Sequence<T>` with `element` inserted at `index`",
            param("index", T_INTEGER, "1-based insertion position"),
            param(PARAM_ELEMENT, "T", "The element to insert")));

    m.put(
        "subSequence",
        op(
            "Sequence<T>.subSequence(lower: " + T_INTEGER + ", upper: " + T_INTEGER + ") → Sequence<T>",
            "Returns the sub-sequence from index `lower` to `upper` (1-based, inclusive).",
            "A new `Sequence<T>` with elements from `lower` to `upper`",
            param("lower", T_INTEGER, "Start index (1-based, inclusive)"),
            param("upper", T_INTEGER, "End index (1-based, inclusive)")));

    m.put(
        "at",
        op(
            "Sequence<T>.at(index: " + T_INTEGER + ") → T",
            "Returns the element at the given 1-based index. "
                + "Also works on String to return the character at that position.",
            "The element at position `index`",
            param("index", T_INTEGER, "1-based position")));

    m.put(
        "first",
        op(
            "Collection<T>.first() → T",
            "Returns the first element of the collection. For unordered collections (Set, Bag) "
                + "the result is non-deterministic. Undefined for empty collections.",
            "The first (or an arbitrary) element"));

    m.put(
        "last",
        op(
            "Collection<T>.last() → T",
            "Returns the last element of the collection. For unordered collections (Set, Bag) "
                + "the result is non-deterministic. Undefined for empty collections.",
            "The last (or an arbitrary) element"));

    m.put(
        "reverse",
        op(
            "Sequence<T>.reverse() → Sequence<T>",
            "Returns a new sequence with all elements in reversed order.",
            "A `Sequence<T>` with elements in reverse order"));

    // ------------------------------------------------------------------
    // Collection conversion operations
    // ------------------------------------------------------------------

    m.put(
        "asSet",
        op(
            "Collection<T>.asSet() → Set<T>",
            "Converts any collection to a Set, removing duplicates.",
            "A `Set<T>` with the same elements (duplicates removed)"));

    m.put(
        "asBag",
        op(
            "Collection<T>.asBag() → Bag<T>",
            "Converts any collection to a Bag (unordered, duplicates allowed).",
            "A `Bag<T>` with the same elements"));

    m.put(
        "asSequence",
        op(
            "Collection<T>.asSequence() → Sequence<T>",
            "Converts any collection to a Sequence (ordered, duplicates allowed).",
            "A `Sequence<T>` with the same elements"));

    m.put(
        "asOrderedSet",
        op(
            "Collection<T>.asOrderedSet() → OrderedSet<T>",
            "Converts any collection to an OrderedSet (ordered, unique), removing duplicates.",
            "An `OrderedSet<T>` with the same elements (duplicates removed)"));

    // ------------------------------------------------------------------
    // Numeric aggregate operations
    // ------------------------------------------------------------------

    m.put(
        "sum",
        op(
            "Collection<Number>.sum() → Number",
            "Computes the arithmetic sum of all elements. Returns 0 for empty collections.",
            "The sum of all numeric elements"));

    m.put(
        "max",
        op(
            "Collection<Number>.max() → Number",
            "Returns the maximum value in the collection. Undefined for empty collections. "
                + "Supports Integer, Float, and Double element types.",
            "The largest element"));

    m.put(
        "min",
        op(
            "Collection<Number>.min() → Number",
            "Returns the minimum value in the collection. Undefined for empty collections. "
                + "Supports Integer, Float, and Double element types.",
            "The smallest element"));

    m.put(
        "avg",
        op(
            "Collection<Number>.avg() → Real",
            "Computes the arithmetic mean of all elements. Undefined for empty collections.",
            "The arithmetic average as a `Real`"));

    // ------------------------------------------------------------------
    // Numeric scalar operations
    // ------------------------------------------------------------------

    m.put(
        "abs",
        op(
            "Number.abs() → Number",
            "Returns the absolute value. Preserves the numeric type (Integer stays Integer, "
                + "Real stays Real). " + DESC_SCALAR_RECEIVER + " not a collection.",
            "The non-negative magnitude of `self`"));

    m.put(
        "floor",
        op(
            "Real.floor() → " + T_INTEGER,
            "Returns the largest integer less than or equal to `self`. "
                + DESC_SCALAR_RECEIVER,
            "The floor of `self` as an `Integer`"));

    m.put(
        "ceil",
        op(
            "Real.ceil() → " + T_INTEGER,
            "Returns the smallest integer greater than or equal to `self`. "
                + DESC_SCALAR_RECEIVER,
            "The ceiling of `self` as an `Integer`"));

    m.put(
        "ceiling",
        op(
            "Real.ceiling() → " + T_INTEGER,
            "Alias for `ceil`. Returns the smallest integer greater than or equal to `self`.",
            "The ceiling of `self` as an `Integer`"));

    m.put(
        "round",
        op(
            "Real.round() → " + T_INTEGER,
            "Returns `self` rounded to the nearest integer (half rounds up). "
                + DESC_SCALAR_RECEIVER,
            "The rounded value as an `Integer`"));

    m.put(
        "div",
        op(
            T_INTEGER + ".div(divisor: " + T_INTEGER + ") → " + T_INTEGER,
            "Integer division (truncating). Both operands must be Integer.",
            "The integer quotient of `self` divided by `divisor`",
            param("divisor", T_INTEGER, "The divisor")));

    m.put(
        "mod",
        op(
            T_INTEGER + ".mod(divisor: " + T_INTEGER + ") → " + T_INTEGER,
            "Integer remainder. Both operands must be Integer.",
            "The remainder of `self` divided by `divisor`",
            param("divisor", T_INTEGER, "The divisor")));

    m.put(
        "lift",
        op(
            "Collection<T>.lift() → Collection<Collection<T>>",
            "Wraps the collection inside another collection of the same kind. "
                + "Useful before `flatten` to change nesting level.",
            "A collection containing the receiver as its single nested element"));

    // ------------------------------------------------------------------
    // Type-testing & casting operations
    // ------------------------------------------------------------------

    m.put(
        "oclIsKindOf",
        op(
            "OclAny.oclIsKindOf(type: " + T_OCL_TYPE + ") → " + T_BOOLEAN,
            "Tests whether the object is an instance of `type` or any of its subtypes. "
                + "Equivalent to Java's `instanceof`.",
            "`true` if `self` conforms to `type`",
            param("type", T_OCL_TYPE, "The metaclass to test against (e.g. `JavaMM::Class`)")));

    m.put(
        "oclIsTypeOf",
        op(
            "OclAny.oclIsTypeOf(type: " + T_OCL_TYPE + ") → " + T_BOOLEAN,
            "Tests whether the object is an instance of exactly `type` — not a subtype. "
                + "Stricter than `oclIsKindOf`.",
            "`true` if the dynamic type of `self` is exactly `type`",
            param("type", T_OCL_TYPE, "The exact metaclass to test against")));

    m.put(
        "oclAsType",
        op(
            "OclAny.oclAsType(type: " + T_OCL_TYPE + ") → ¡T!",
            "Downcasts `self` to `type`, returning a singleton ¡T!. "
                + "In OCL# everything is a collection, so the result is always a singleton "
                + "with one element. Use `oclIsKindOf` to guard before casting.",
            "A singleton ¡T! containing the cast object",
            param("type", T_OCL_TYPE, "The target metaclass (e.g. `JavaMM::Method`)")));

    // ------------------------------------------------------------------
    // Instance-set operation
    // ------------------------------------------------------------------

    m.put(
        "allInstances",
        op(
            "MetaClass.allInstances() → Set<MetaClass>",
            "Returns the set of all model instances of the given metaclass, including instances of"
                + " all its subclasses. Resolved against the currently loaded model instance files."
                + " Results are cached per EClass for performance.",
            "A `Set` containing every instance of `MetaClass` in the loaded models"));

    // ------------------------------------------------------------------
    // Cross-metamodel correspondence (OCL# extension)
    // ------------------------------------------------------------------

    m.put(
        "~",
        op(
            "Element ~ CorrespondingElement",
            "OCL# correspondence operator. Tests or navigates the correspondence link between "
                + "two elements from different metamodels, as maintained by the Vitruv VSUM. "
                + "The exact semantics depend on the active correspondence model.",
            "The element(s) in the other metamodel that correspond to `self`",
            param(
                "CorrespondingElement",
                "OclAny",
                "The element or type on the right-hand side of the correspondence")));

    // ------------------------------------------------------------------
    // String operations
    // ------------------------------------------------------------------

    m.put(
        "concat",
        op(
            T_STRING + ".concat(suffix: " + T_STRING + ") → " + T_STRING,
            "Concatenates `suffix` to the end of this string.",
            "A new string with `suffix` appended",
            param("suffix", T_STRING, "The string to append")));

    m.put(
        "toUpper",
        op(
            T_STRING + ".toUpper() → " + T_STRING,
            "Converts all characters in the string to upper case.",
            "The upper-case version of `self`"));

    m.put(
        "toLower",
        op(
            T_STRING + ".toLower() → " + T_STRING,
            "Converts all characters in the string to lower case.",
            "The lower-case version of `self`"));

    m.put(
        "substring",
        op(
            T_STRING + ".substring(lower: " + T_INTEGER + ", upper: " + T_INTEGER + ") → " + T_STRING,
            "Extracts the substring between index `lower` and `upper` (1-based, inclusive).",
            "The extracted substring",
            param("lower", T_INTEGER, "Start index (1-based, inclusive)"),
            param("upper", T_INTEGER, "End index (1-based, inclusive)")));

    m.put(
        "indexOf",
        op(
            T_STRING + ".indexOf(sub: " + T_STRING + ") → " + T_INTEGER,
            "Returns the 1-based index of the first occurrence of `sub` within this string, "
                + "or 0 if not found.",
            "1-based position of `sub`, or 0 if absent",
            param("sub", T_STRING, "The substring to search for")));

    m.put(
        "equalsIgnoreCase",
        op(
            T_STRING + ".equalsIgnoreCase(" + PARAM_OTHER + ": " + T_STRING + ") → " + T_BOOLEAN,
            "Tests whether two strings are equal, ignoring case differences.",
            "`true` if both strings are equal when compared case-insensitively",
            param(PARAM_OTHER, T_STRING, "The string to compare against")));

    m.put(
        "length",
        op(
            T_STRING + ".length() → " + T_INTEGER,
            "Returns the number of characters in the string.",
            "The length of the string as a non-negative integer"));

    m.put(
        "toInteger",
        op(
            T_STRING + ".toInteger() → " + T_INTEGER,
            "Parses the string as an integer. Undefined if the string is not a valid integer.",
            "The integer value represented by this string"));

    m.put(
        "toReal",
        op(
            T_STRING + ".toReal() → Real",
            "Parses the string as a real number. Undefined if the string is not a valid number.",
            "The real value represented by this string"));

    m.put(
        "characters",
        op(
            T_STRING + ".characters() → Sequence<" + T_STRING + ">",
            "Splits the string into a Sequence of single-character strings.",
            "A `Sequence<String>` of individual characters"));

    m.put(
        "matches",
        op(
            T_STRING + ".matches(" + PARAM_PATTERN + ": " + T_STRING + ") → " + T_BOOLEAN,
            "Tests whether the string matches the given regular expression pattern.",
            "`true` if `self` matches `pattern`",
            param(PARAM_PATTERN, T_STRING, "A Java-compatible regular expression")));

    m.put(
        "substituteAll",
        op(
            T_STRING + ".substituteAll(" + PARAM_PATTERN + ": " + T_STRING + ", replacement: " + T_STRING + ") → " + T_STRING,
            "Replaces all occurrences of `pattern` (regex) with `replacement`.",
            "A new string with all matches replaced",
            param(PARAM_PATTERN, T_STRING, "The regex pattern to find"),
            param("replacement", T_STRING, "The replacement string")));

    m.put(
        "substituteFirst",
        op(
            T_STRING + ".substituteFirst(" + PARAM_PATTERN + ": " + T_STRING + ", replacement: " + T_STRING + ") → " + T_STRING,
            "Replaces the first occurrence of `pattern` (regex) with `replacement`.",
            "A new string with the first match replaced",
            param(PARAM_PATTERN, T_STRING, "The regex pattern to find"),
            param("replacement", T_STRING, "The replacement string")));

    m.put(
        "tokenize",
        op(
            T_STRING + ".tokenize(delimiter: " + T_STRING + ") → Sequence<" + T_STRING + ">",
            "Splits the string by `delimiter` and returns the parts as a Sequence.",
            "A `Sequence<String>` of the split parts",
            param("delimiter", T_STRING, "The delimiter string or regex to split on")));

    // ------------------------------------------------------------------
    // Ordering comparisons (numeric only in OCL#)
    // ------------------------------------------------------------------

    m.put(
        "<",
        op(
            "Number < Number → " + T_BOOLEAN,
            "Less-than comparison. Only valid for numeric types (Integer, Float, Real). "
                + DESC_NO_STRING_ORDERING,
            "`true` if `self` is strictly less than the right operand"));

    m.put(
        "<=",
        op(
            "Number <= Number → " + T_BOOLEAN,
            "Less-than-or-equal comparison. Only valid for numeric types (Integer, Float, Real). "
                + DESC_NO_STRING_ORDERING,
            "`true` if `self` is less than or equal to the right operand"));

    m.put(
        ">",
        op(
            "Number > Number → " + T_BOOLEAN,
            "Greater-than comparison. Only valid for numeric types (Integer, Float, Real). "
                + DESC_NO_STRING_ORDERING,
            "`true` if `self` is strictly greater than the right operand"));

    m.put(
        ">=",
        op(
            "Number >= Number → " + T_BOOLEAN,
            "Greater-than-or-equal comparison. Only valid for numeric types (Integer, Float, Real)."
                + " " + DESC_NO_STRING_ORDERING,
            "`true` if `self` is greater than or equal to the right operand"));

    m.put(
        "==",
        op(
            "T == T → " + T_BOOLEAN,
            "Equality comparison. Works for all types including enums. "
                + "Enum literals are compared by name within the same enum type.",
            "`true` if both operands are equal"));

    m.put(
        "!=",
        op(
            "T != T → " + T_BOOLEAN,
            "Inequality comparison. Works for all types including enums.",
            "`true` if the operands are not equal"));

    DOCS = Collections.unmodifiableMap(m);
  }

  private static OperationDoc op(
      String signature, String description, String returnDesc, ParamDoc... params) {
    return new OperationDoc(signature, description, returnDesc, List.of(params));
  }

  private static ParamDoc param(String name, String type, String description) {
    return new ParamDoc(name, type, description);
  }

  private OclOperationDocs() {}
}


