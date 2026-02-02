package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.List;
import org.eclipse.emf.ecore.EClass;

/**
 * Represents a type in the VitruviusOCL type system.
 *
 * <p>Extended to support OCL# multiplicity semantics: - Primitive types (INTEGER, STRING, BOOLEAN)
 * have implicit SINGLETON multiplicity - Collections created via factory methods (set(),
 * sequence(), optional()) - Metamodel types (EClass references) for cross-metamodel constraints
 *
 * @see TypeResolver for type operations and conformance checks
 * @see TypeRegistry for metamodel access
 */
public abstract class Type {

  /** Collection kind for type checking. */
  public enum CollectionKind {
    SET,
    SEQUENCE,
    BAG,
    ORDERED_SET
  }

  /** Error Type */
  public static final Type ERROR = new ErrorType();

  /** Integer Type */
  public static final Type INTEGER = new IntegerType();

  /** Boolean Type */
  public static final Type BOOLEAN = new BooleanType();

  /** Double Type */
  public static final Type DOUBLE = new DoubleType();

  /** String Type */
  public static final Type STRING = new StringType();

  /** Any Type */
  public static final Type ANY = new AnyType();

  // ==================== OCL# Factory Methods ====================

  /**
   * Creates a Set type: {T}
   *
   * @param elementType The type of elements in the set
   * @return A collection type with SET multiplicity
   */
  public static Type set(Type elementType) {
    return new CollectionType(elementType, Multiplicity.SET);
  }

  /**
   * Creates a bag type: {T}
   *
   * @param elementType The type of elements in the bag
   * @return A collection type with SET multiplicity
   */
  public static Type bag(Type elementType) {
    return new CollectionType(elementType, Multiplicity.BAG);
  }

  /**
   * Creates a orderedSet type: {T}
   *
   * @param elementType The type of elements in the orderedSet
   * @return A collection type with SET multiplicity
   */
  public static Type orderedSet(Type elementType) {
    return new CollectionType(elementType, Multiplicity.ORDERED_SET);
  }

  /**
   * Creates a Sequence type: [T]
   *
   * @param elementType The type of elements in the sequence
   * @return A collection type with SEQUENCE multiplicity
   */
  public static Type sequence(Type elementType) {
    return new CollectionType(elementType, Multiplicity.SEQUENCE);
  }

  /**
   * Creates an Optional type: ?T?
   *
   * <p>Replaces null in Standard-OCL!
   *
   * @param elementType The type of the optional element
   * @return A collection type with OPTIONAL multiplicity
   */
  public static Type optional(Type elementType) {
    return new CollectionType(elementType, Multiplicity.OPTIONAL);
  }

  /**
   * Creates a Singleton type: !T!
   *
   * <p>For explicit singleton wrapping (usually not needed as primitives are already singletons)
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
   * @param eClass The EClass from the metamodel
   * @return A metaclass type wrapping the EClass
   */
  public static Type metaclassType(EClass eClass) {
    return new MetaclassType(eClass);
  }

  // ==================== OCL# Multiplicity Support ====================

  /**
   * Returns the multiplicity of this type.
   *
   * <p>Default: SINGLETON for all primitive types (INTEGER, STRING, BOOLEAN) Override:
   * CollectionType returns its explicit multiplicity
   *
   * @return The multiplicity of this type
   */
  public Multiplicity getMultiplicity() {
    return Multiplicity.SINGLETON;
  }

  /**
   * Returns the element type of this type.
   *
   * <p>Default: this (for primitive types, the element type is themselves) Override: CollectionType
   * returns its wrapped element type
   *
   * @return The element type
   */
  public Type getElementType() {
    return this;
  }

  /**
   * Checks if this type is a collection (SET or SEQUENCE multiplicity).
   *
   * @return true if this type is a collection
   */
  public boolean isCollection() {
    return getMultiplicity().isCollection();
  }

  /**
   * Checks if this type is optional (?T? multiplicity).
   *
   * @return true if this type is optional
   */
  public boolean isOptional() {
    return getMultiplicity() == Multiplicity.OPTIONAL;
  }

  /**
   * Checks if this type is a singleton (!T! multiplicity).
   *
   * @return true if this type is a singleton
   */
  public boolean isSingleton() {
    return getMultiplicity() == Multiplicity.SINGLETON;
  }

  /**
   * Checks if this type is an error type.
   *
   * @return true if this is an error type
   */
  public boolean isError() {
    return this == ERROR;
  }

  /**
   * Checks if this type is a metamodel type.
   *
   * @return true if this is a metaclass type
   */
  public boolean isMetaclassType() {
    return this instanceof MetaclassType;
  }

  /**
   * Returns the EClass if this is a metaclass type.
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
   * Checks if this type conforms to another type.
   *
   * @param other The supertype to check conformance against
   * @return true if this type conforms to other
   */
  public abstract boolean isConformantTo(Type other);

  /**
   * Returns the name of this type.
   *
   * @return Type name
   */
  public abstract String getTypeName();

  @Override
  public String toString() {
    return getTypeName();
  }

  // ==================== Primitive Type Implementations ====================

  /** Error Type - conforms to everything to prevent follow-up errors. */
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

  /** Integer Type */
  private static class IntegerType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == INTEGER) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // ✅ Singleton !int! conforms to base int
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

  /** Boolean Type */
  private static class BooleanType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == BOOLEAN) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // ✅ Singleton !bool! conforms to base bool
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

  /** Double Type */
  private static class DoubleType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == DOUBLE) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // ✅ Singleton !double! conforms to base double
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

  /** String Type */
  private static class StringType extends Type {
    @Override
    public boolean isConformantTo(Type other) {
      if (other == STRING) return true;
      if (other == ERROR) return true;
      if (other == ANY) return true;

      // ✅ Singleton !String! conforms to base String
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

  /** Any Type */
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
   * Metaclass Type - represents an EClass from a Vitruvius metamodel.
   *
   * <p>Used for cross-metamodel consistency checking with the ~ operator.
   */
  private static class MetaclassType extends Type {
    private final EClass eClass;

    private MetaclassType(EClass eClass) {
      this.eClass = eClass;
    }

    @Override
    public boolean isConformantTo(Type other) {
      if (other == ERROR || other == ANY) return true;

      // ✅ Singleton metaclass conforms to base metaclass
      if (other.isSingleton() && other.getElementType() instanceof MetaclassType otherMeta) {
        return otherMeta.eClass.isSuperTypeOf(this.eClass) || this.eClass.equals(otherMeta.eClass);
      }

      if (other instanceof MetaclassType otherMeta) {
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
   * Collection Type with explicit multiplicity.
   *
   * <p>Represents OCL# collection types: - Set{T} - Sequence[T] - Bag{{T}} - OrderedSet<T> -
   * Optional ?T?
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

      // ✅ Singleton conforms to its element type
      if (this.isSingleton() && this.elementType.equals(other)) {
        return true;
      }

      // Collection conformance: element type must conform
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

    /** Returns the collection kind for this type. */
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
   * Checks if this type represents a unique collection (Set, OrderedSet). In OCL#, this corresponds
   * to μ = u (unique).
   */
  public boolean isUnique() {
    return getMultiplicity().isUnique();
  }

  /** Checks if this type represents an ordered collection (Sequence, OrderedSet). */
  public boolean isOrdered() {
    return getMultiplicity().isOrdered();
  }

  /** Returns the collection kind (if this is a collection type). */
  public CollectionKind getCollectionKind() {
    if (this instanceof CollectionType collType) {
      return collType.getCollectionKind();
    }
    return null;
  }

  /**
   * Returns the common supertype of two types.
   *
   * <p>Used in type inference for expressions with multiple branches (e.g., if-then-else).
   *
   * @param t1 First type
   * @param t2 Second type
   * @return The common supertype of t1 and t2
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