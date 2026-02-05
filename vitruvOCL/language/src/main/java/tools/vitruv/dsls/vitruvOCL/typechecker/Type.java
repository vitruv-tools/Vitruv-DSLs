package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.List;
import org.eclipse.emf.ecore.EClass;

/**
 * Represents a type in the VitruviusOCL type system.
 *
 * <p>Implements OCL semantics where "everything is a collection" - even primitive values have
 * implicit SINGLETON multiplicity. The type system supports:
 *
 * <ul>
 *   <li>Primitive types (INTEGER, STRING, BOOLEAN, DOUBLE) with implicit singleton multiplicity
 *   <li>Collection types created via factory methods (set(), sequence(), bag(), orderedSet())
 *   <li>Optional types (?T?) replacing OCL's null concept
 *   <li>Metaclass types (EClass references) for cross-metamodel constraints in Vitruvius
 * </ul>
 *
 * <p>This is the base Type AST node produced during Pass 2 (Type Checking) of the compiler. Each
 * parsed expression node receives a Type annotation stored in the nodeTypes map.
 *
 * @see TypeCheckVisitor which produces Type annotations for parsed expressions
 * @see Multiplicity for OCL multiplicity semantics
 * @see MetaclassType for Vitruvius metamodel integration
 */
public abstract class Type {

  /**
   * Collection kind enumeration for type checking and evaluation.
   *
   * <p>Maps to OCL multiplicity properties:
   *
   * <ul>
   *   <li>SET: unordered, unique elements {T}
   *   <li>SEQUENCE: ordered, duplicates allowed [T]
   *   <li>BAG: unordered, duplicates allowed {{T}}
   *   <li>ORDERED_SET: ordered, unique elements &lt;T&gt;
   * </ul>
   */
  public enum CollectionKind {
    SET,
    SEQUENCE,
    BAG,
    ORDERED_SET
  }

  /**
   * Error type that conforms to all types to prevent cascading type errors. Used when type checking
   * fails to allow compilation to continue.
   */
  public static final Type ERROR = new ErrorType();

  /** Primitive Integer type with implicit SINGLETON multiplicity. */
  public static final Type INTEGER = new IntegerType();

  /** Primitive Boolean type with implicit SINGLETON multiplicity. */
  public static final Type BOOLEAN = new BooleanType();

  /** Primitive Double type with implicit SINGLETON multiplicity. */
  public static final Type DOUBLE = new DoubleType();

  /** Primitive String type with implicit SINGLETON multiplicity. */
  public static final Type STRING = new StringType();

  /** Top type in the type hierarchy - all types conform to ANY. */
  public static final Type ANY = new AnyType();

  // ==================== Factory Methods ====================

  /**
   * Creates a Set type {T} with unordered, unique elements.
   *
   * <p>Example: {@code set(INTEGER)} produces Set{Integer}
   *
   * @param elementType The type of elements in the set
   * @return A collection type with SET multiplicity
   */
  public static Type set(Type elementType) {
    return new CollectionType(elementType, Multiplicity.SET);
  }

  /**
   * Creates a Bag type {{T}} with unordered elements allowing duplicates.
   *
   * <p>Example: {@code bag(STRING)} produces Bag{{String}}
   *
   * @param elementType The type of elements in the bag
   * @return A collection type with BAG multiplicity
   */
  public static Type bag(Type elementType) {
    return new CollectionType(elementType, Multiplicity.BAG);
  }

  /**
   * Creates an OrderedSet type &lt;T&gt; with ordered, unique elements.
   *
   * <p>Example: {@code orderedSet(BOOLEAN)} produces OrderedSet&lt;Boolean&gt;
   *
   * @param elementType The type of elements in the ordered set
   * @return A collection type with ORDERED_SET multiplicity
   */
  public static Type orderedSet(Type elementType) {
    return new CollectionType(elementType, Multiplicity.ORDERED_SET);
  }

  /**
   * Creates a Sequence type [T] with ordered elements allowing duplicates.
   *
   * <p>Example: {@code sequence(INTEGER)} produces Sequence[Integer]
   *
   * @param elementType The type of elements in the sequence
   * @return A collection type with SEQUENCE multiplicity
   */
  public static Type sequence(Type elementType) {
    return new CollectionType(elementType, Multiplicity.SEQUENCE);
  }

  /**
   * Creates an Optional type ?T? that may contain zero or one element.
   *
   * <p>Replaces OCL's null concept with an explicit optional type, making the type system
   * null-safe. An empty optional [] represents "no value" instead of null.
   *
   * <p>Example: {@code optional(STRING)} produces Optional?String?
   *
   * @param elementType The type of the optional element
   * @return A collection type with OPTIONAL multiplicity
   */
  public static Type optional(Type elementType) {
    return new CollectionType(elementType, Multiplicity.OPTIONAL);
  }

  /**
   * Creates a Singleton type !T! wrapping exactly one element.
   *
   * <p>Usually not needed explicitly as primitive types already have implicit singleton
   * multiplicity. Use when explicit singleton wrapping is semantically important.
   *
   * <p>Example: {@code singleton(INTEGER)} produces Singleton!Integer!
   *
   * @param elementType The type of the singleton element
   * @return A collection type with SINGLETON multiplicity
   */
  public static Type singleton(Type elementType) {
    return new CollectionType(elementType, Multiplicity.SINGLETON);
  }

  /**
   * Creates a Metaclass type for Vitruvius metamodel elements.
   *
   * <p>Used in cross-metamodel constraints where the ~ operator accesses instances from other
   * metamodels in the VSUM. The EClass represents a metaclass from a loaded EMF metamodel.
   *
   * <p>Example: For a Spacecraft EClass, produces type "spacemissionmodel::Spacecraft"
   *
   * @param eClass The EClass from the EMF metamodel
   * @return A metaclass type wrapping the EClass
   */
  public static Type metaclassType(EClass eClass) {
    return new MetaclassType(eClass);
  }

  // ==================== OCL Multiplicity Support ====================

  /**
   * Returns the multiplicity of this type.
   *
   * <p>Overridden by CollectionType to return its explicit multiplicity.
   *
   * @return The multiplicity of this type (default: SINGLETON)
   */
  public Multiplicity getMultiplicity() {
    return Multiplicity.SINGLETON;
  }

  /**
   * Returns the element type of this type.
   *
   * <p>For primitive types, returns self (the element type of Integer is Integer). For collection
   * types, returns the wrapped element type.
   *
   * <p>Example: For Set{Integer}, returns INTEGER
   *
   * @return The element type (default: this)
   */
  public Type getElementType() {
    return this;
  }

  /**
   * Checks if this type is a collection (SET, SEQUENCE, BAG, or ORDERED_SET multiplicity).
   *
   * <p>Returns false for SINGLETON and OPTIONAL multiplicities.
   *
   * @return true if this type represents a multi-valued collection
   */
  public boolean isCollection() {
    return getMultiplicity().isCollection();
  }

  /**
   * Checks if this type has optional multiplicity (?T?).
   *
   * <p>Optional types can contain zero or one element, replacing null in standard OCL.
   *
   * @return true if this type is optional
   */
  public boolean isOptional() {
    return getMultiplicity() == Multiplicity.OPTIONAL;
  }

  /**
   * Checks if this type has singleton multiplicity (!T!).
   *
   * <p>All primitive types return true. Collection types only return true if explicitly wrapped
   * with SINGLETON multiplicity.
   *
   * @return true if this type is a singleton
   */
  public boolean isSingleton() {
    return getMultiplicity() == Multiplicity.SINGLETON;
  }

  /**
   * Checks if this is the ERROR type.
   *
   * <p>Used to suppress cascading type errors during compilation.
   *
   * @return true if this is an error type
   */
  public boolean isError() {
    return this == ERROR;
  }

  /**
   * Checks if this type represents a metamodel class (EClass wrapper).
   *
   * <p>Metaclass types are used for cross-metamodel constraints in Vitruvius VSUMs.
   *
   * @return true if this is a metaclass type
   */
  public boolean isMetaclassType() {
    return this instanceof MetaclassType;
  }

  /**
   * Returns the underlying EClass if this is a metaclass type.
   *
   * <p>Used to access metamodel information during type checking and evaluation of cross-metamodel
   * constraints.
   *
   * @return The EClass, or null if not a metaclass type
   */
  public EClass getEClass() {
    if (this instanceof MetaclassType meta) {
      return meta.eClass;
    }
    return null;
  }

  // ==================== Type Conformance ====================

  /**
   * Checks if this type conforms to (is a subtype of) another type.
   *
   * <p>Implements OCL type conformance rules:
   *
   * <ul>
   *   <li>All types conform to ANY
   *   <li>ERROR conforms to everything (to prevent cascading errors)
   *   <li>Primitive types conform to themselves
   *   <li>Singleton !T! conforms to T
   *   <li>Collection conformance requires element type conformance and multiplicity conformance
   *   <li>Metaclass types use EClass inheritance hierarchy
   * </ul>
   *
   * @param other The supertype to check conformance against
   * @return true if this type can be used where other is expected
   */
  public abstract boolean isConformantTo(Type other);

  /**
   * Returns the string representation of this type.
   *
   * <ul>
   *   <li>Primitives: "Integer", "String", "Boolean", "Double"
   *   <li>Sets: "Set{T}"
   *   <li>Sequences: "Sequence[T]"
   *   <li>Bags: "Bag{{T}}"
   *   <li>OrderedSets: "OrderedSet&lt;T&gt;"
   *   <li>Optionals: "Optional?T?"
   *   <li>Singletons: "Singleton!T!"
   *   <li>Metaclasses: "packageName::ClassName"
   * </ul>
   *
   * @return Type name
   */
  public abstract String getTypeName();

  /**
   * Returns the type name (delegates to getTypeName()).
   *
   * @return String representation of this type
   */
  @Override
  public String toString() {
    return getTypeName();
  }

  // ==================== Primitive Type Implementations ====================

  /**
   * Error type implementation - conforms to everything to prevent cascading errors.
   *
   * <p>When type checking encounters an error (undefined variable, type mismatch, etc.), ERROR is
   * used to annotate the node so compilation can continue without propagating the same error to
   * parent expressions.
   */
  private static class ErrorType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      return true;
    }

    @Override
    public String getTypeName() {
      return "ERROR";
    }
  }

  /**
   * Integer type implementation with implicit SINGLETON multiplicity.
   *
   * <p>Represents OCL integer literals and arithmetic operation results. Conforms to Integer, Any,
   * and singleton Integer types.
   */
  private static class IntegerType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == INTEGER) return true;
      if (other == DOUBLE) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // Singleton !int! conforms to base int
      if (other.isSingleton() && other.getElementType() == INTEGER) {
        return true;
      }

      if (other.getElementType() == INTEGER) {
        return true;
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return "Integer";
    }
  }

  /**
   * Boolean type implementation with implicit SINGLETON multiplicity.
   *
   * <p>Represents OCL boolean literals and logical operation results. Used for control flow
   * (if-then-else) and iterator operation guards (forAll, exists).
   */
  private static class BooleanType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == BOOLEAN) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // Singleton !bool! conforms to base bool
      if (other.isSingleton() && other.getElementType() == BOOLEAN) {
        return true;
      }

      if (other.getElementType() == BOOLEAN) {
        return true;
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return "Boolean";
    }
  }

  /**
   * Double type implementation with implicit SINGLETON multiplicity.
   *
   * <p>Represents OCL floating-point literals and decimal arithmetic results.
   */
  private static class DoubleType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == DOUBLE) return true;
      if (other == INTEGER) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // Singleton !double! conforms to base double
      if (other.isSingleton() && other.getElementType() == DOUBLE) {
        return true;
      }

      if (other.getElementType() == DOUBLE) {
        return true;
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return "Double";
    }
  }

  /**
   * String type implementation with implicit SINGLETON multiplicity.
   *
   * <p>Represents OCL string literals and string operation results. Supports concatenation,
   * substring, size, and other string operations.
   */
  private static class StringType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == STRING) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // Singleton !String! conforms to base String
      if (other.isSingleton() && other.getElementType() == STRING) {
        return true;
      }

      if (other.getElementType() == STRING) {
        return true;
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return "String";
    }
  }

  /**
   * Any type implementation - top of the type hierarchy.
   *
   * <p>All types conform to Any. Used as a fallback when no more specific common supertype exists
   * (e.g., if-then-else with incompatible branches).
   */
  private static class AnyType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == ERROR) return true;
      return other == ANY;
    }

    @Override
    public String getTypeName() {
      return "Any";
    }
  }

  // ==================== Metaclass Type Implementation ====================

  /**
   * Metaclass type representing an EClass from a Vitruvius metamodel.
   *
   * <p>Used for cross-metamodel consistency checking with the ~ operator. The EClass reference
   * provides access to the metamodel's type hierarchy for conformance checking based on EClass
   * inheritance.
   *
   * <p>Example: In a constraint involving Spacecraft and Satellite types, MetaclassType wraps the
   * corresponding EClass objects to enable type checking across metamodel boundaries.
   */
  private static class MetaclassType extends Type {
    private final EClass eClass;

    private MetaclassType(EClass eClass) {
      this.eClass = eClass;
    }

    @Override
    public boolean isConformantTo(Type other) {
      if (other == ERROR || other == ANY) return true;

      // Singleton metaclass conforms to base metaclass
      if (other.isSingleton() && other.getElementType() instanceof MetaclassType otherMeta) {
        return otherMeta.eClass.isSuperTypeOf(this.eClass) || this.eClass.equals(otherMeta.eClass);
      }

      if (other instanceof MetaclassType otherMeta) {
        // Use EClass inheritance: this conforms to other if other is a supertype
        return otherMeta.eClass.isSuperTypeOf(this.eClass) || this.eClass.equals(otherMeta.eClass);
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return eClass.getEPackage().getName() + "::" + eClass.getName();
    }
  }

  // ==================== Collection Type Implementation ====================

  /**
   * Collection type with explicit multiplicity.
   *
   * <p>Represents OCL collection types with different ordering and uniqueness properties:
   *
   * <ul>
   *   <li>Set{T} - unordered, unique
   *   <li>Sequence[T] - ordered, duplicates allowed
   *   <li>Bag{{T}} - unordered, duplicates allowed
   *   <li>OrderedSet&lt;T&gt; - ordered, unique
   *   <li>Optional?T? - zero or one element (replaces null)
   *   <li>Singleton!T! - exactly one element
   * </ul>
   *
   * <p>Collection operations (select, reject, collect, etc.) preserve or transform these
   * multiplicities according to OCL semantics.
   */
  private static class CollectionType extends Type {
    private final Type elementType;
    private final Multiplicity multiplicity;

    public CollectionType(Type elementType, Multiplicity multiplicity) {
      this.elementType = elementType;
      this.multiplicity = multiplicity;
    }

    @Override
    public Multiplicity getMultiplicity() {
      return multiplicity;
    }

    @Override
    public Type getElementType() {
      return elementType;
    }

    @Override
    public boolean isConformantTo(Type other) {
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // Singleton conforms to its unwrapped element type
      if (this.isSingleton() && this.elementType.equals(other)) {
        return true;
      }

      // Collection conformance: both element type and multiplicity must conform
      if (other instanceof CollectionType otherColl) {
        if (!this.elementType.isConformantTo(otherColl.elementType)) {
          return false;
        }
        return this.multiplicity.isConformantTo(otherColl.multiplicity);
      }

      return false;
    }

    @Override
    public String getTypeName() {
      return multiplicity.getSymbol() + elementType.getTypeName() + multiplicity.getClosingSymbol();
    }

    /**
     * Returns the collection kind for this type.
     *
     * <p>Maps multiplicity to CollectionKind enum used in evaluation phase. Returns null for
     * SINGLETON and OPTIONAL multiplicities.
     *
     * @return The collection kind, or null if not a multi-valued collection
     */
    public CollectionKind getCollectionKind() {
      return switch (multiplicity) {
        case SET -> CollectionKind.SET;
        case SEQUENCE -> CollectionKind.SEQUENCE;
        case BAG -> CollectionKind.BAG;
        case ORDERED_SET -> CollectionKind.ORDERED_SET;
        default -> null;
      };
    }
  }

  /**
   * Checks if this type represents a unique collection (Set, OrderedSet).
   *
   * <p>In OCL multiplicity theory, this corresponds to μ = u (unique property). Unique collections
   * do not allow duplicate elements.
   *
   * @return true if this is a Set or OrderedSet type
   */
  public boolean isUnique() {
    return getMultiplicity().isUnique();
  }

  /**
   * Checks if this type represents an ordered collection (Sequence, OrderedSet).
   *
   * <p>In OCL multiplicity theory, this corresponds to μ = o (ordered property). Ordered
   * collections maintain element insertion order and support indexing.
   *
   * @return true if this is a Sequence or OrderedSet type
   */
  public boolean isOrdered() {
    return getMultiplicity().isOrdered();
  }

  /**
   * Returns the collection kind if this is a collection type.
   *
   * <p>Used during evaluation to determine which collection implementation (HashSet, ArrayList,
   * etc.) to use for runtime values.
   *
   * @return The collection kind, or null if not a collection
   */
  public CollectionKind getCollectionKind() {
    if (this instanceof CollectionType collType) {
      return collType.getCollectionKind();
    }
    return null;
  }

  /**
   * Computes the common supertype of two types.
   *
   * <p>Used in type inference for expressions with multiple branches where a single result type is
   * needed (e.g., if-then-else, collection literals with mixed element types).
   *
   * <p>Algorithm:
   *
   * <ul>
   *   <li>If types are equal, return that type
   *   <li>If either is ERROR, return ERROR (error propagation)
   *   <li>For different primitive types, return ANY
   *   <li>For collections, compute element supertype and combine multiplicities:
   *       <ul>
   *         <li>Both unique + both ordered → OrderedSet
   *         <li>Both unique → Set
   *         <li>Both ordered → Sequence
   *         <li>Otherwise → Bag
   *       </ul>
   *   <li>For mixed collection/non-collection, return Bag{Any}
   *   <li>Default fallback: ANY
   * </ul>
   *
   * @param t1 First type
   * @param t2 Second type
   * @return The least common supertype of t1 and t2
   */
  public static Type commonSuperType(Type t1, Type t2) {
    if (t1.equals(t2)) return t1;
    if (t1 == ERROR || t2 == ERROR) return ERROR;

    boolean t1Primitive = (t1 == INTEGER || t1 == STRING || t1 == BOOLEAN);
    boolean t2Primitive = (t2 == INTEGER || t2 == STRING || t2 == BOOLEAN);

    if (t1Primitive && t2Primitive && !t1.equals(t2)) {
      return ANY;
    }

    if (t1.isCollection() && t2.isCollection()) {
      Type elemSuper = commonSuperType(t1.getElementType(), t2.getElementType());

      boolean unique = t1.isUnique() && t2.isUnique();
      boolean ordered = t1.isOrdered() && t2.isOrdered();

      if (unique && ordered) return orderedSet(elemSuper);
      if (unique) return set(elemSuper);
      if (ordered) return sequence(elemSuper);
      return bag(elemSuper);
    }

    if (t1.isCollection() || t2.isCollection()) {
      return bag(ANY);
    }

    return ANY;
  }

  /**
   * Compares two runtime values for ordering.
   *
   * <p>Used during Pass 3 (Evaluation) for comparison operators (&lt;, &gt;, &lt;=, &gt;=).
   * Compares values lexicographically by size first, then element-wise.
   *
   * @param v1 First value
   * @param v2 Second value
   * @return Negative if v1 &lt; v2, zero if equal, positive if v1 &gt; v2
   */
  public static int compare(
      tools.vitruv.dsls.vitruvOCL.evaluator.Value v1,
      tools.vitruv.dsls.vitruvOCL.evaluator.Value v2) {
    if (v1 == v2) return 0;
    if (v1 == null) return -1;
    if (v2 == null) return 1;

    int sizeCompare = Integer.compare(v1.size(), v2.size());
    if (sizeCompare != 0) {
      return sizeCompare;
    }

    List<tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement> elems1 = v1.getElements();
    List<tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement> elems2 = v2.getElements();

    for (int i = 0; i < elems1.size(); i++) {
      int elemCompare =
          tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement.compare(elems1.get(i), elems2.get(i));
      if (elemCompare != 0) {
        return elemCompare;
      }
    }

    return 0;
  }

  /**
   * Checks structural equality of types.
   *
   * <p>Two types are equal if they have the same name, and for collection types, the same
   * multiplicity and element type.
   *
   * @param obj Object to compare against
   * @return true if types are structurally equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || !(obj instanceof Type)) return false;

    Type other = (Type) obj;

    if (!this.getTypeName().equals(other.getTypeName())) {
      return false;
    }

    if (this.isCollection() && other.isCollection()) {
      return this.getMultiplicity() == other.getMultiplicity()
          && this.getElementType().equals(other.getElementType());
    }

    return true;
  }

  /**
   * Computes hash code for type equality checks.
   *
   * <p>Hash combines type name, multiplicity, and element type for collections.
   *
   * @return Hash code for this type
   */
  @Override
  public int hashCode() {
    int result = getTypeName().hashCode();
    if (isCollection()) {
      result = 31 * result + getMultiplicity().hashCode();
      result = 31 * result + getElementType().hashCode();
    }
    return result;
  }
}