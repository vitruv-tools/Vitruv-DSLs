package tools.vitruv.dsls.vitruvOCL.evaluator;

import org.eclipse.emf.ecore.EClass;

/**
 * OCL# Element - represents a single element in an OCL collection.
 *
 * <p>In OCL#, all values are collections of elements. Elements can be: - Primitive values (int,
 * bool, string) - Object references (EObject IDs) - Nested collections (for nested collection types
 * like {{int}})
 *
 * <p>This is a sealed interface to ensure exhaustiveness in pattern matching.
 */
public sealed interface OCLElement
    permits OCLElement.IntValue,
        OCLElement.BoolValue,
        OCLElement.StringValue,
        OCLElement.ObjectRef,
        OCLElement.DoubleValue,
        OCLElement.NestedCollection,
        OCLElement.MetaclassValue {

  /** Returns the EClass for metamodel elements, null for primitives. */
  default EClass getEClass() {
    return null;
  }

  /** Integer value element. Example: In Set{1, 2, 3}, each number is an IntValue */
  record IntValue(int value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /** Double value element. */
  record DoubleValue(int value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /** Boolean value element. Example: In Set{true, false}, each boolean is a BoolValue */
  record BoolValue(boolean value) implements OCLElement {
    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /** String value element. Example: In Set{"hello", "world"}, each string is a StringValue */
  record StringValue(String value) implements OCLElement {
    @Override
    public String toString() {
      return "\"" + value + "\"";
    }
  }

  /**
   * Object reference element (for EObjects from VSUM). Stores the object ID (OID) as a string.
   *
   * <p>Example: Person objects from a metamodel
   */
  record ObjectRef(String oid) implements OCLElement {
    @Override
    public String toString() {
      return "@" + oid;
    }
  }

  /**
   * Nested collection element. Used for nested collection types like Bag{Set{Integer}}.
   *
   * <p>Example: - {{int}} (Bag of Sets) - [[int]] (Sequence of Sequences) - {¡int!} (Set of
   * Singletons)
   */
  record NestedCollection(Value value) implements OCLElement {
    @Override
    public String toString() {
      return value.toString();
    }
  }

  /**
   * Metaclass value element (for EObjects from metamodels). Used for cross-metamodel constraints
   * with ~ operator.
   */
  record MetaclassValue(org.eclipse.emf.ecore.EObject instance) implements OCLElement {
    @Override
    public EClass getEClass() {
      return instance.eClass();
    }

    @Override
    public String toString() {
      return instance.eClass().getName() + "@" + System.identityHashCode(instance);
    }
  }

  /**
   * Compares two OCL elements for semantic equality. Used by merge operations and equality
   * checking.
   */
  static boolean semanticEquals(OCLElement a, OCLElement b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;

    // Handle different types
    if (a instanceof IntValue ia && b instanceof IntValue ib) {
      return ia.value == ib.value;
    }
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
    return false;
  }

  /**
   * Compares two OCL elements for ordering. Used for normalizing unordered collections (Sets,
   * Bags).
   *
   * <p>Order: Integers < Booleans < Strings < ObjectRefs < NestedCollections
   */
  static int compare(OCLElement a, OCLElement b) {
    if (a == b) return 0;
    if (a == null) return -1;
    if (b == null) return 1;

    // First compare by type order
    int typeA = getTypeOrder(a);
    int typeB = getTypeOrder(b);
    if (typeA != typeB) {
      return Integer.compare(typeA, typeB);
    }

    // Same type - compare values
    if (a instanceof IntValue ia && b instanceof IntValue ib) {
      return Integer.compare(ia.value, ib.value);
    }
    if (a instanceof BoolValue ba && b instanceof BoolValue bb) {
      return Boolean.compare(ba.value, bb.value);
    }
    if (a instanceof StringValue sa && b instanceof StringValue sb) {
      return sa.value.compareTo(sb.value);
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

  /** Returns a numeric order for element types. Used for sorting heterogeneous collections. */
  static int getTypeOrder(OCLElement elem) {
    if (elem instanceof IntValue) return 0;
    if (elem instanceof BoolValue) return 1;
    if (elem instanceof StringValue) return 2;
    if (elem instanceof ObjectRef) return 3;
    if (elem instanceof MetaclassValue) return 4;
    if (elem instanceof NestedCollection) return 5;
    return 6;
  }
}