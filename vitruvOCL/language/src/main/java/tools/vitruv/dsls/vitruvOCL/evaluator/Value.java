/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.evaluator;

import java.util.*;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * OCL Value - The unified value representation.
 *
 * <p>Core OCL Principle: "Everything is a collection"
 *
 * <p>All values are represented as lists of elements: - Singleton: [elem] (¡τ! - exactly 1 element)
 * - Empty: [] (no τ - "kein Wert") - Multi-valued: [e1, e2, e3] (collections)
 *
 * <p>The Type's Ctype properties determine how the list is interpreted: - {τ} (Set): Unique,
 * unordered - [τ] (Sequence): Non-unique, ordered - {{τ}} (Bag): Non-unique, unordered - ⟨τ⟩
 * (OrderedSet): Unique, ordered
 */
public class Value {

  /** The actual value - in OCL, this is ALWAYS List<OCLElement> */
  private final List<OCLElement> elements;

  /** The runtime type - includes Ctype information (unique, ordered) */
  private final Type runtimeType;

  // ==================== Constructors ====================

  /** Creates a Value from a list of elements. This is the main constructor for OCL values. */
  public Value(List<OCLElement> elements, Type runtimeType) {
    this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    this.runtimeType = runtimeType;
  }

  /**
   * Legacy constructor for backwards compatibility. If value is already List<OCLElement>, use it
   * directly. Otherwise, wrap it as a singleton OCLElement.
   */
  public Value(Object value, Type runtimeType) {
    if (value instanceof List) {
      @SuppressWarnings("unchecked")
      List<OCLElement> list = (List<OCLElement>) value;
      this.elements = Collections.unmodifiableList(new ArrayList<>(list));
    } else if (value == null) {
      // null becomes empty collection in OCL
      this.elements = List.of();
    } else {
      // Wrap single value as singleton
      this.elements = List.of(wrapAsElement(value));
    }
    this.runtimeType = runtimeType;
  }

  /** Wraps a Java object as an OCLElement (for legacy compatibility). */
  private OCLElement wrapAsElement(Object obj) {
    if (obj instanceof Integer) {
      return new OCLElement.IntValue((Integer) obj);
    } else if (obj instanceof Boolean) {
      return new OCLElement.BoolValue((Boolean) obj);
    } else if (obj instanceof String) {
      return new OCLElement.StringValue((String) obj);
    } else {
      throw new IllegalArgumentException("Cannot wrap " + obj.getClass() + " as OCLElement");
    }
  }

  // ==================== Factory Methods ====================

  /** Creates an empty Value (no τ). */
  public static Value empty(Type type) {
    return new Value(List.of(), type);
  }

  /** Creates a singleton Value (¡τ!). */
  public static Value singleton(OCLElement elem, Type type) {
    return new Value(List.of(elem), type);
  }

  /** Creates a Value from multiple elements. */
  public static Value of(List<OCLElement> elements, Type type) {
    return new Value(elements, type);
  }

  /** Convenience: Creates integer singleton. */
  public static Value intValue(int value) {
    return singleton(new OCLElement.IntValue(value), Type.INTEGER);
  }

  /** Convenience: Creates boolean singleton. */
  public static Value boolValue(boolean value) {
    return singleton(new OCLElement.BoolValue(value), Type.BOOLEAN);
  }

  /** Convenience: Creates string singleton. */
  public static Value stringValue(String value) {
    return singleton(new OCLElement.StringValue(value), Type.STRING);
  }

  // ==================== Accessors ====================

  /** Returns the elements as an immutable list. */
  public List<OCLElement> getElements() {
    return elements;
  }

  /** Legacy getter - returns the elements list for OCL compatibility. */
  public Object getValue() {
    return elements;
  }

  /** Returns the runtime type. */
  public Type getRuntimeType() {
    return runtimeType;
  }

  /** Returns the number of elements in this collection. */
  public int size() {
    return elements.size();
  }

  /** Checks if this collection is empty. */
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  /** Checks if this collection is not empty. */
  public boolean notEmpty() {
    return !elements.isEmpty();
  }

  /**
   * In OCL, null doesn't exist - only empty collections. This method returns true if the collection
   * is empty.
   */
  public boolean isNull() {
    return elements.isEmpty();
  }

  // ==================== Monoid Operations ====================

  /**
   * Merges two Values using OCL monoid semantics (⊕χ).
   *
   * <p>Behavior depends on Type's Ctype properties: - Set (unique, unordered): Union with
   * uniqueness - Bag (non-unique, unordered): Concatenation with duplicates - Sequence (non-unique,
   * ordered): Concatenation preserving order - OrderedSet (unique, ordered): Union preserving order
   */
  public Value merge(Value other) {
    List<OCLElement> result = new ArrayList<>(this.elements);

    boolean isUnique = runtimeType.isUnique();

    if (isUnique) {
      // Set/OrderedSet: Add only unique elements
      for (OCLElement elem : other.elements) {
        if (!containsElement(result, elem)) {
          result.add(elem);
        }
      }
    } else {
      // Bag/Sequence: Add all elements
      result.addAll(other.elements);
    }

    return new Value(result, this.runtimeType);
  }

  // ==================== Collection Operations ====================

  /** Checks if this collection includes an element. */
  public boolean includes(OCLElement elem) {
    for (OCLElement e : elements) {
      if (OCLElement.semanticEquals(e, elem)) {
        return true;
      }
    }
    return false;
  }

  /** Checks if this collection excludes an element. */
  public boolean excludes(OCLElement element) {
    return !includes(element);
  }

  /** Returns a new Value with element included. Immutable - does not modify this Value. */
  public Value including(OCLElement element) {
    return this.merge(singleton(element, runtimeType));
  }

  /**
   * Returns a new Value with element excluded. - Set/OrderedSet: Removes all occurrences -
   * Bag/Sequence: Removes first occurrence only
   */
  public Value excluding(OCLElement element) {
    List<OCLElement> result = new ArrayList<>();
    boolean isUnique = runtimeType.isUnique();
    boolean foundFirst = false;

    for (OCLElement elem : elements) {
      if (OCLElement.semanticEquals(elem, element)) {
        if (isUnique) {
          continue; // Skip all
        } else if (!foundFirst) {
          foundFirst = true;
          continue; // Skip first
        }
      }
      result.add(elem);
    }

    return new Value(result, runtimeType);
  }

  /** Union of two collections. */
  public Value union(Value other) {
    return this.merge(other);
  }

  /** Intersection of two collections. */
  public Value intersection(Value other) {
    List<OCLElement> result = new ArrayList<>();

    for (OCLElement elem : this.elements) {
      if (other.includes(elem) && !containsElement(result, elem)) {
        result.add(elem);
      }
    }

    return new Value(result, this.runtimeType);
  }

  /** Set difference (this - other). */
  public Value minus(Value other) {
    List<OCLElement> result = new ArrayList<>();

    for (OCLElement elem : this.elements) {
      if (!other.includes(elem)) {
        result.add(elem);
      }
    }

    return new Value(result, this.runtimeType);
  }

  /** Symmetric difference (elements in either but not both). */
  public Value symmetricDifference(Value other) {
    List<OCLElement> result = new ArrayList<>();

    for (OCLElement elem : this.elements) {
      if (!other.includes(elem)) {
        result.add(elem);
      }
    }

    for (OCLElement elem : other.elements) {
      if (!this.includes(elem)) {
        result.add(elem);
      }
    }

    return new Value(result, this.runtimeType);
  }

  /** Checks if this collection includes all elements of another. */
  public boolean includesAll(Value other) {
    for (OCLElement elem : other.elements) {
      if (!this.includes(elem)) {
        return false;
      }
    }
    return true;
  }

  /** Checks if this collection excludes all elements of another. */
  public boolean excludesAll(Value other) {
    for (OCLElement elem : other.elements) {
      if (this.includes(elem)) {
        return false;
      }
    }
    return true;
  }

  // ==================== Ordered Collection Operations ====================

  /** Returns the first element. Returns empty if this is empty (OCL semantics). */
  public Value first() {
    if (isEmpty()) {
      return empty(runtimeType);
    }
    return singleton(elements.get(0), runtimeType.getElementType());
  }

  /** Returns the last element. Returns empty if this is empty (OCL semantics). */
  public Value last() {
    if (isEmpty()) {
      return empty(runtimeType);
    }
    return singleton(elements.get(size() - 1), runtimeType.getElementType());
  }

  /** Returns element at index (1-based OCL indexing). Only valid for ordered collections. */
  public Value at(int index) {
    if (!runtimeType.isOrdered()) {
      throw new UnsupportedOperationException("at() requires an ordered collection");
    }

    if (index < 1 || index > size()) {
      throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size());
    }

    return singleton(elements.get(index - 1), runtimeType.getElementType());
  }

  /** Returns the index of first occurrence (1-based). Returns 0 if not found. */
  public int indexOf(OCLElement element) {
    for (int i = 0; i < elements.size(); i++) {
      if (OCLElement.semanticEquals(elements.get(i), element)) {
        return i + 1; // 1-based
      }
    }
    return 0; // Not found
  }

  /** Reverses the order of elements. Only valid for ordered collections. */
  public Value reverse() {
    if (!runtimeType.isOrdered()) {
      throw new UnsupportedOperationException("reverse() requires an ordered collection");
    }

    List<OCLElement> reversed = new ArrayList<>(elements);
    Collections.reverse(reversed);
    return new Value(reversed, runtimeType);
  }

  /** Counts occurrences of an element (for Bags). */
  public int count(OCLElement element) {
    int count = 0;
    for (OCLElement elem : elements) {
      if (OCLElement.semanticEquals(elem, element)) {
        count++;
      }
    }
    return count;
  }

  // ==================== Normalization & Equality ====================

  /**
   * Normalizes this Value according to its Ctype. Unordered collections (Set, Bag) are sorted for
   * canonical form.
   */
  public Value normalize() {
    if (runtimeType.isOrdered()) {
      return this; // Already canonical
    }

    List<OCLElement> sorted = new ArrayList<>(elements);
    sorted.sort(OCLElement::compare);
    return new Value(sorted, runtimeType);
  }

  /** Semantic equality (≡χ₁,χ₂). Compares normalized forms. */
  public static boolean semanticEquals(Value v1, Value v2) {
    if (v1 == null && v2 == null) return true;
    if (v1 == null || v2 == null) return false;

    Value norm1 = v1.normalize();
    Value norm2 = v2.normalize();

    if (norm1.size() != norm2.size()) {
      return false;
    }

    for (int i = 0; i < norm1.size(); i++) {
      if (!OCLElement.semanticEquals(norm1.elements.get(i), norm2.elements.get(i))) {
        return false;
      }
    }

    return true;
  }

  /** Removes duplicate elements (for Set/OrderedSet creation). */
  public Value removeDuplicates() {
    List<OCLElement> unique = new ArrayList<>();

    for (OCLElement elem : elements) {
      if (!containsElement(unique, elem)) {
        unique.add(elem);
      }
    }

    return new Value(unique, runtimeType);
  }

  // ==================== Helper Methods ====================

  /** Checks if a list contains an element using semantic equality. */
  private static boolean containsElement(List<OCLElement> list, OCLElement elem) {
    for (OCLElement e : list) {
      if (OCLElement.semanticEquals(e, elem)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares two Values for ordering (used in nested collections). Required by OCLElement.compare()
   * for NestedCollection comparison.
   */
  public static int compare(Value v1, Value v2) {
    if (v1 == v2) return 0;
    if (v1 == null) return -1;
    if (v2 == null) return 1;

    // First compare by size
    int sizeCompare = Integer.compare(v1.size(), v2.size());
    if (sizeCompare != 0) {
      return sizeCompare;
    }

    // Same size - compare elements pairwise
    for (int i = 0; i < v1.size(); i++) {
      int elemCompare = OCLElement.compare(v1.elements.get(i), v2.elements.get(i));
      if (elemCompare != 0) {
        return elemCompare;
      }
    }

    return 0; // Equal
  }

  // ==================== String Representation ====================

  @Override
  public String toString() {
    if (isEmpty()) {
      return getCollectionKind() + "{}";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(getCollectionKind()).append("{");

    for (int i = 0; i < elements.size(); i++) {
      sb.append(elements.get(i).toString());
      if (i < elements.size() - 1) {
        sb.append(", ");
      }
    }

    sb.append("}");
    return sb.toString();
  }

  /** Returns the collection kind based on Ctype properties. */
  private String getCollectionKind() {
    if (runtimeType.isUnique() && !runtimeType.isOrdered()) return "Set";
    if (!runtimeType.isUnique() && runtimeType.isOrdered()) return "Sequence";
    if (!runtimeType.isUnique() && !runtimeType.isOrdered()) return "Bag";
    if (runtimeType.isUnique() && runtimeType.isOrdered()) return "OrderedSet";
    return "Collection";
  }

  /**
   * Flattens nested collections: Collection(Collection(T)) → Collection(T)
   *
   * <p>Example: Set{Set{1,2}, Set{3,4}}.flatten() → Set{1,2,3,4}
   *
   * @return Flattened collection
   */
  public Value flatten() {
    List<OCLElement> flattened = new ArrayList<>();

    for (OCLElement elem : elements) {
      // Check if element is a nested collection
      if (elem instanceof OCLElement.NestedCollection nested) {
        // Extract all elements from the nested collection
        flattened.addAll(nested.value().getElements());
      } else {
        // Not a nested collection - add element as-is
        flattened.add(elem);
      }
    }

    // Create result with same type properties
    Value result = new Value(flattened, runtimeType);

    // Apply uniqueness if this is a Set/OrderedSet
    if (runtimeType.isUnique()) {
      result = result.removeDuplicates();
    }

    return result;
  }

  public static Value metaclassValue(EObject instance, Type type) {
    return singleton(new OCLElement.MetaclassValue(instance), type);
  }
}