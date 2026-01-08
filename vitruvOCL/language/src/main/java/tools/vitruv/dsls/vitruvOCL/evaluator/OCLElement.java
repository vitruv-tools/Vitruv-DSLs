package tools.vitruv.dsls.vitruvOCL.evaluator;


/**
 * OCL# Element - represents a single element in an OCL collection.
 * 
 * In OCL#, all values are collections of elements. Elements can be:
 * - Primitive values (int, bool, string)
 * - Object references (EObject IDs)
 * - Nested collections (for nested collection types like {{int}})
 * 
 * This is a sealed interface to ensure exhaustiveness in pattern matching.
 */
public sealed interface OCLElement permits 
    OCLElement.IntValue, 
    OCLElement.BoolValue, 
    OCLElement.StringValue, 
    OCLElement.ObjectRef, 
    OCLElement.NestedCollection {
    
    /**
     * Integer value element.
     * Example: In Set{1, 2, 3}, each number is an IntValue
     */
    record IntValue(int value) implements OCLElement {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    /**
     * Boolean value element.
     * Example: In Set{true, false}, each boolean is a BoolValue
     */
    record BoolValue(boolean value) implements OCLElement {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    /**
     * String value element.
     * Example: In Set{"hello", "world"}, each string is a StringValue
     */
    record StringValue(String value) implements OCLElement {
        @Override
        public String toString() {
            return "\"" + value + "\"";
        }
    }
    
    /**
     * Object reference element (for EObjects from VSUM).
     * Stores the object ID (OID) as a string.
     * 
     * Example: Person objects from a metamodel
     */
    record ObjectRef(String oid) implements OCLElement {
        @Override
        public String toString() {
            return "@" + oid;
        }
    }
    
    /**
     * Nested collection element.
     * Used for nested collection types like Bag{Set{Integer}}.
     * 
     * Example: 
     * - {{int}} (Bag of Sets)
     * - [[int]] (Sequence of Sequences)
     * - {¡int!} (Set of Singletons)
     */
    record NestedCollection(Value value) implements OCLElement {
        @Override
        public String toString() {
            return value.toString();
        }
    }
    
    /**
     * Compares two OCL elements for semantic equality.
     * Used by merge operations and equality checking.
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
        
        return false;
    }
    
    /**
     * Compares two OCL elements for ordering.
     * Used for normalizing unordered collections (Sets, Bags).
     * 
     * Order: Integers < Booleans < Strings < ObjectRefs < NestedCollections
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
        
        return 0;
    }
    
    /**
     * Returns a numeric order for element types.
     * Used for sorting heterogeneous collections.
     */
    static int getTypeOrder(OCLElement elem) {
        if (elem instanceof IntValue) return 0;
        if (elem instanceof BoolValue) return 1;
        if (elem instanceof StringValue) return 2;
        if (elem instanceof ObjectRef) return 3;
        if (elem instanceof NestedCollection) return 4;
        return 5;
    }
}
