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
package tools.vitruv.dsls.vitruvocl.evaluator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;

/**
 * OCL Element — represents a single element in an OCL collection.
 *
 * <p>This is a sealed interface to ensure exhaustiveness in pattern matching. Numeric types follow
 * the hierarchy {@code INTEGER ⊂ FLOAT ⊂ DOUBLE}: integer values can be promoted to float or double
 * for mixed arithmetic and comparison.
 *
 * <p>EMF {@code EEnum} attribute values are wrapped as {@link EnumValue} records containing the
 * {@link EEnumLiteral}. Equality between two enum values uses literal identity; equality between an
 * enum value and a string uses the literal name.
 */
public sealed interface OCLElement
    permits OCLElement.IntValue,
        OCLElement.BoolValue,
        OCLElement.StringValue,
        OCLElement.ObjectRef,
        OCLElement.DoubleValue,
        OCLElement.FloatValue,
        OCLElement.EnumValue,
        OCLElement.NestedCollection,
        OCLElement.MetaclassValue,
        OCLElement.CastedMetaclassValue {

  /** Returns the EClass for metamodel elements, null for primitives. */
  default EClass getEClass() {
    return null;
  }

  /**
   * Try to get boolean value; returns {@code null} if not a {@link BoolValue}. Avoids instanceof
   * checks in calling code. Null is the documented "not-a-Boolean" sentinel for this type
   * hierarchy.
   */
  @SuppressWarnings(
      "java:S2447") // null is intentional sentinel meaning "this element is not a Boolean"
  default Boolean tryGetBool() {
    return null;
  }

  /**
   * Try to get int value; returns {@code null} if not an {@link IntValue}. Avoids instanceof checks
   * in calling code.
   */
  default Integer tryGetInt() {
    return null;
  }

  /**
   * Try to get string value; returns {@code null} if not a {@link StringValue}. Avoids instanceof
   * checks in calling code.
   */
  default String tryGetString() {
    return null;
  }

  /**
   * Try to get float value; returns {@code null} if not a {@link FloatValue}. Avoids instanceof
   * checks in calling code.
   */
  default Float tryGetFloat() {
    return null;
  }

  /**
   * Try to get double value; returns {@code null} if not a {@link DoubleValue}. Avoids instanceof
   * checks in calling code.
   */
  default Double tryGetDouble() {
    return null;
  }

  /**
   * Try to get EObject instance; returns {@code null} if not a {@link MetaclassValue} or {@link
   * CastedMetaclassValue}. Avoids instanceof checks in calling code.
   */
  default EObject tryGetInstance() {
    return null;
  }

  /**
   * Promotes this element to a {@code double} for mixed numeric operations. Returns the numeric
   * value as a {@code double} for {@link IntValue}, {@link FloatValue}, and {@link DoubleValue};
   * throws {@link UnsupportedOperationException} for all other types.
   *
   * @return this element's value as a Java {@code double}
   * @throws UnsupportedOperationException if the element is not numeric
   */
  default double toDoubleValue() {
    throw new UnsupportedOperationException("Not a numeric element: " + this);
  }

  // ==================== Concrete value types ====================

  /** Integer value element. Example: {@code 42} in {@code Set{42}}. */
  record IntValue(int value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @Override
    public Integer tryGetInt() {
      return value;
    }

    @Override
    public double toDoubleValue() {
      return value;
    }
  }

  /**
   * Double (64-bit floating-point) value element. Produced by OCL {@code Real} literals and by
   * arithmetic that involves at least one {@link DoubleValue} operand.
   */
  record DoubleValue(double value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @Override
    public Double tryGetDouble() {
      return value;
    }

    @Override
    public double toDoubleValue() {
      return value;
    }
  }

  /**
   * Float (32-bit floating-point) value element. Produced when reading EMF {@code EFloat}
   * attributes (Java {@link Float}). Arithmetic with a {@link DoubleValue} upcasts to {@link
   * DoubleValue}; arithmetic with an {@link IntValue} stays as {@link FloatValue}.
   */
  record FloatValue(float value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @Override
    public Float tryGetFloat() {
      return value;
    }

    @Override
    public Double tryGetDouble() {
      // Expose as double for comparison helpers that only know tryGetDouble()
      return (double) value;
    }

    @Override
    public double toDoubleValue() {
      return value;
    }
  }

  /** Boolean value element. Example: {@code true} in {@code Set{true, false}}. */
  record BoolValue(boolean value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @Override
    public Boolean tryGetBool() {
      return value;
    }
  }

  /** String value element. Example: {@code "hello"} in {@code Set{"hello", "world"}}. */
  record StringValue(String value) implements OCLElement {
    @Override
    public String toString() {
      return "\"" + value + "\"";
    }

    @Override
    public String tryGetString() {
      return value;
    }
  }

  /**
   * Enum value element wrapping an EMF {@link EEnumLiteral}. Produced when reading an {@code EEnum}
   * attribute from an EMF model instance (e.g., {@code Unit.MM}).
   *
   * <p>Equality semantics:
   *
   * <ul>
   *   <li>{@code EnumValue == EnumValue} → compare by literal identity ({@code
   *       EEnumLiteral.equals})
   *   <li>{@code EnumValue == StringValue} → compare the literal's {@code getName()} to the string
   * </ul>
   */
  record EnumValue(EEnumLiteral literal) implements OCLElement {
    @Override
    public String toString() {
      return literal.getEEnum().getEPackage().getName()
          + "::"
          + literal.getEEnum().getName()
          + "::"
          + literal.getName();
    }

    /** Returns the enum literal's name as a string (useful for string comparisons). */
    @Override
    public String tryGetString() {
      return literal.getName();
    }
  }

  /**
   * Object reference element (for EObjects from VSUM). Stores the object ID (OID) as a string.
   *
   * <p>Example: {@code Person} objects from a metamodel.
   */
  record ObjectRef(String oid) implements OCLElement {
    @Override
    public String toString() {
      return "@" + oid;
    }
  }

  /**
   * Nested collection element. Used for nested collection types like {@code Bag{Set{Integer}}}.
   *
   * <p>Example: {@code {{int}}} (Bag of Sets), {@code [[int]]} (Sequence of Sequences).
   */
  record NestedCollection(Value value) implements OCLElement {
    @Override
    public String toString() {
      return value.toString();
    }
  }

  /**
   * Metaclass value element (for EObjects from metamodels). Used for cross-metamodel constraints
   * with the {@code ~} operator.
   */
  record MetaclassValue(EObject instance) implements OCLElement {
    @Override
    public EClass getEClass() {
      return instance.eClass();
    }

    @Override
    public String toString() {
      return instance.eClass().getName() + "@" + System.identityHashCode(instance);
    }

    @Override
    public EObject tryGetInstance() {
      return instance;
    }
  }

  /**
   * A metaclass value that has been cast to a specific target type via {@code oclAsType}. {@link
   * #getEClass()} returns the cast target type, not the runtime type of the instance. This allows
   * property access on the target type's features after casting.
   */
  record CastedMetaclassValue(EObject instance, EClass castedTo) implements OCLElement {
    @Override
    public EClass getEClass() {
      // Return the cast target type, not instance.eClass()
      return castedTo;
    }

    @Override
    public String toString() {
      return "("
          + castedTo.getName()
          + ") "
          + instance.eClass().getName()
          + "@"
          + System.identityHashCode(instance);
    }

    @Override
    public EObject tryGetInstance() {
      return instance;
    }
  }

  // ==================== Static helpers ====================

  /**
   * Returns {@code true} if {@code elem} is any numeric element ({@link IntValue}, {@link
   * FloatValue}, or {@link DoubleValue}).
   *
   * @param elem the element to test
   * @return {@code true} if numeric
   */
  static boolean isNumeric(OCLElement elem) {
    return elem instanceof IntValue || elem instanceof FloatValue || elem instanceof DoubleValue;
  }

  /**
   * Compares two OCL elements for semantic equality.
   *
   * <p>Numeric cross-type rules:
   *
   * <ul>
   *   <li>{@code IntValue == IntValue} → integer comparison
   *   <li>{@code IntValue == FloatValue / DoubleValue} → promote to double
   *   <li>{@code FloatValue == FloatValue} → float comparison
   *   <li>{@code FloatValue == DoubleValue} → promote to double
   *   <li>{@code DoubleValue == DoubleValue} → double comparison
   * </ul>
   *
   * <p>Enum rules:
   *
   * <ul>
   *   <li>{@code EnumValue == EnumValue} → literal identity
   *   <li>{@code EnumValue == StringValue} → literal name equals string
   * </ul>
   *
   * @param a first element
   * @param b second element
   * @return {@code true} if semantically equal
   */
  @SuppressWarnings("java:S3776")
  static boolean semanticEquals(OCLElement a, OCLElement b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }

    // ── Numeric cross-type equality ──────────────────────────────────────────
    if (isNumeric(a) && isNumeric(b)) {
      return Double.compare(a.toDoubleValue(), b.toDoubleValue()) == 0;
    }

    // ── Typed comparisons ────────────────────────────────────────────────────
    if (a instanceof BoolValue ba && b instanceof BoolValue bb) {
      return ba.value == bb.value;
    }
    if (a instanceof StringValue sa && b instanceof StringValue sb) {
      return sa.value.equals(sb.value);
    }
    if (a instanceof ObjectRef oa && b instanceof ObjectRef ob) {
      return oa.oid.equals(ob.oid);
    }
    if (a instanceof NestedCollection na && b instanceof NestedCollection nb) {
      return Value.semanticEquals(na.value, nb.value);
    }
    if (a instanceof MetaclassValue ma && b instanceof MetaclassValue mb) {
      return ma.instance.equals(mb.instance);
    }

    // ── Enum equality ────────────────────────────────────────────────────────
    if (a instanceof EnumValue ea && b instanceof EnumValue eb) {
      // Use literal name for equality, not object identity. EEnumLiteral.equals() is
      // reference equality, so two literals from different EPackages (e.g.
      // Labelgraph1::Label::ORANGE vs Labelgraph2::Label::ORANGE) would never be equal
      // even if they represent the same value. Name-based comparison is the correct
      // cross-metamodel semantics.
      return ea.literal().getName().equals(eb.literal().getName());
    }
    // EnumValue == StringValue: compare by literal name
    if (a instanceof EnumValue ea && b instanceof StringValue sb) {
      return ea.literal().getName().equals(sb.value());
    }
    if (a instanceof StringValue sa && b instanceof EnumValue eb) {
      return sa.value().equals(eb.literal().getName());
    }

    return false;
  }

  /**
   * Compares two OCL elements for ordering. Used for normalizing unordered collections (Sets, Bags)
   * and for relational operators ({@code <}, {@code <=}, {@code >}, {@code >=}).
   *
   * <p>Ordering: Integers/Floats/Doubles (by numeric value) &lt; Booleans &lt; Strings &lt; Enums
   * &lt; ObjectRefs &lt; MetaclassValues &lt; NestedCollections.
   *
   * @param a first element
   * @param b second element
   * @return negative if {@code a < b}, zero if equal, positive if {@code a > b}
   */
  @SuppressWarnings("java:S3776")
  static int compare(OCLElement a, OCLElement b) {
    if (a == b) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }

    int typeA = getTypeOrder(a);
    int typeB = getTypeOrder(b);
    if (typeA != typeB) {
      // Cross-numeric comparison: promote both to double
      if (isNumeric(a) && isNumeric(b)) {
        return Double.compare(a.toDoubleValue(), b.toDoubleValue());
      }
      return Integer.compare(typeA, typeB);
    }

    // Same type bucket
    if (isNumeric(a) && isNumeric(b)) {
      return Double.compare(a.toDoubleValue(), b.toDoubleValue());
    }
    if (a instanceof BoolValue ba && b instanceof BoolValue bb) {
      return Boolean.compare(ba.value, bb.value);
    }
    if (a instanceof StringValue sa && b instanceof StringValue sb) {
      return sa.value.compareTo(sb.value);
    }
    if (a instanceof EnumValue ea && b instanceof EnumValue eb) {
      return Integer.compare(ea.literal().getValue(), eb.literal().getValue());
    }
    if (a instanceof ObjectRef oa && b instanceof ObjectRef ob) {
      return oa.oid.compareTo(ob.oid);
    }
    if (a instanceof NestedCollection na && b instanceof NestedCollection nb) {
      return Value.compare(na.value, nb.value);
    }
    if (a instanceof MetaclassValue ma && b instanceof MetaclassValue mb) {
      return Integer.compare(
          System.identityHashCode(ma.instance), System.identityHashCode(mb.instance));
    }

    return 0;
  }

  /**
   * Returns a numeric ordering bucket for element types. All numeric types share bucket 0 so that
   * cross-type numeric comparison falls into the same bucket path.
   *
   * @param elem the element to classify
   * @return bucket index
   */
  static int getTypeOrder(OCLElement elem) {
    // All numeric types share bucket 0 for cross-type comparison
    if (elem instanceof IntValue) {
      return 0;
    }
    if (elem instanceof FloatValue) {
      return 0;
    }
    if (elem instanceof DoubleValue) {
      return 0;
    }
    if (elem instanceof BoolValue) {
      return 1;
    }
    if (elem instanceof StringValue) {
      return 2;
    }
    if (elem instanceof EnumValue) {
      return 3;
    }
    if (elem instanceof ObjectRef) {
      return 4;
    }
    if (elem instanceof MetaclassValue) {
      return 5;
    }
    if (elem instanceof NestedCollection) {
      return 6;
    }
    return 7;
  }
}
