/* ******************************************************************************
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

package tools.vitruv.dsls.vitruvocl.typechecker;

/**
 * Static helper methods for type resolution and type operations.
 *
 * <p><b>Purpose:</b> Shared type resolution logic that can be used by both the TypeCheckVisitor and
 * later by the evaluator.
 *
 * <p><b>Features:</b>
 *
 * <ul>
 *   <li>Binary operator type resolution: {@code resolveBinaryOp("+", Integer, Integer) -> Integer}
 *   <li>Type conformance: {@code isConformantTo(subtype, supertype)}
 *   <li>Common supertype: {@code commonSupertype(Type a, Type b)}
 *   <li>Collection element type extraction
 * </ul>
 *
 * <p><b>Numeric type hierarchy:</b> INTEGER ⊂ FLOAT ⊂ DOUBLE. Any arithmetic between numeric types
 * upcasts to the wider type (e.g., {@code FLOAT + DOUBLE = DOUBLE}, {@code INTEGER + FLOAT =
 * FLOAT}).
 *
 * <p><b>No duplication:</b> This logic is identical for type checking and evaluation, so it is
 * centralized in this helper class.
 */
public class TypeResolver {

  private static final String OP_IMPLIES = "implies";

  private TypeResolver() {} // Pure static utility class

  // ==================== Numeric helpers ====================

  /**
   * Returns true if the type is any floating-point type (FLOAT or DOUBLE).
   *
   * @param t the type to check
   * @return true for FLOAT or DOUBLE
   */
  public static boolean isFloatingPoint(Type t) {
    return t == Type.FLOAT || t == Type.DOUBLE;
  }

  /**
   * Returns true if the type is any numeric type (INTEGER, FLOAT, or DOUBLE).
   *
   * @param t the type to check
   * @return true for INTEGER, FLOAT, or DOUBLE
   */
  public static boolean isNumeric(Type t) {
    return t == Type.INTEGER || t == Type.FLOAT || t == Type.DOUBLE;
  }

  /**
   * Returns the wider of two numeric types following the numeric hierarchy INTEGER ⊂ FLOAT ⊂
   * DOUBLE. Used to determine the result type of mixed-numeric arithmetic.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code INTEGER, INTEGER → INTEGER}
   *   <li>{@code INTEGER, FLOAT → FLOAT}
   *   <li>{@code FLOAT, DOUBLE → DOUBLE}
   *   <li>{@code INTEGER, DOUBLE → DOUBLE}
   * </ul>
   *
   * @param a first numeric type
   * @param b second numeric type
   * @return the wider type; Type.ERROR if either argument is not numeric
   */
  public static Type widenNumeric(Type a, Type b) {
    if (!isNumeric(a) || !isNumeric(b)) {
      return Type.ERROR;
    }
    if (a == Type.DOUBLE || b == Type.DOUBLE) {
      return Type.DOUBLE;
    }
    if (a == Type.FLOAT || b == Type.FLOAT) {
      return Type.FLOAT;
    }
    return Type.INTEGER;
  }

  // ==================== Binary operator resolution ====================

  /**
   * Resolves the result type of a binary operation.
   *
   * <p>Unwraps singleton {@code !T!} and optional {@code ?T?} receivers before resolution.
   * Multi-valued collections (Set, Bag etc.) on either side cause an error unless the operator is
   * {@code ==} or {@code !=} on two collections.
   *
   * @param operator "+", "-", "*", "/", "==", etc.
   * @param leftType type of the left operand
   * @param rightType type of the right operand
   * @return the result type, or {@link Type#ERROR} on type mismatch
   */
  @SuppressWarnings("java:S3776")
  public static Type resolveBinaryOp(String operator, Type leftType, Type rightType) {
    if (leftType == Type.ERROR || rightType == Type.ERROR) {
      return Type.ERROR;
    }

    if (leftType == Type.ANY || rightType == Type.ANY) {
      return switch (operator) {
        case "+", "-", "*" -> Type.INTEGER;
        case "/" -> Type.DOUBLE;
        case "and", "or", "xor", OP_IMPLIES -> Type.BOOLEAN;
        case "<", "<=", ">", ">=", "==", "!=" -> Type.BOOLEAN;
        default -> Type.ERROR;
      };
    }

    // Collection == / != : compare element-wise without unwrapping
    if ((operator.equals("==") || operator.equals("!="))
        && leftType.isCollection()
        && rightType.isCollection()
        && !leftType.isSingleton()
        && !rightType.isSingleton()) {
      Type leftElem = leftType.getElementType();
      Type rightElem = rightType.getElementType();
      if (leftElem.isConformantTo(rightElem)
          || rightElem.isConformantTo(leftElem)
          || leftElem == Type.ANY
          || rightElem == Type.ANY) {
        return Type.BOOLEAN;
      }
      return Type.ERROR;
    }

    // Unwrap singleton !T! and optional ?T? to their scalar member types
    leftType = unwrapToScalar(leftType);
    rightType = unwrapToScalar(rightType);

    // After unwrapping: multi-valued collection on either side → ERROR
    if (leftType.isCollection() || rightType.isCollection()) {
      return Type.ERROR;
    }

    // Re-check ANY after unwrapping (element may be ANY)
    if (leftType == Type.ANY || rightType == Type.ANY) {
      return switch (operator) {
        case "+", "-", "*" -> Type.INTEGER;
        case "/" -> Type.DOUBLE;
        case "and", "or", "xor", OP_IMPLIES -> Type.BOOLEAN;
        case "<", "<=", ">", ">=", "==", "!=" -> Type.BOOLEAN;
        default -> Type.ERROR;
      };
    }

    // Arithmetic
    if (operator.equals("+")
        || operator.equals("-")
        || operator.equals("*")
        || operator.equals("/")) {
      if (isNumeric(leftType) && isNumeric(rightType)) {
        if (operator.equals("/")) {
          // OCL division returns Real; integer division must not truncate to Integer.
          if (leftType == Type.DOUBLE || rightType == Type.DOUBLE) {
            return Type.DOUBLE;
          }
          if (leftType == Type.FLOAT || rightType == Type.FLOAT) {
            return Type.FLOAT;
          }
          return Type.DOUBLE;
        }
        return widenNumeric(leftType, rightType);
      }
      return Type.ERROR;
    }

    // Logical
    if (operator.equals("and") || operator.equals("or") || operator.equals("xor")) {
      if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
        return Type.BOOLEAN;
      }
      return Type.ERROR;
    }
    if (operator.equals(OP_IMPLIES)) {
      if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
        return Type.BOOLEAN;
      }
      return Type.ERROR;
    }

    // Equality (scalars)
    if (operator.equals("==") || operator.equals("!=")) {
      if (isNumeric(leftType) && isNumeric(rightType)) {
        return Type.BOOLEAN;
      }
      if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
        return Type.BOOLEAN;
      }
      if (leftType == Type.STRING && rightType == Type.STRING) {
        return Type.BOOLEAN;
      }
      if (leftType == Type.STRING || rightType == Type.STRING) {
        return Type.BOOLEAN;
      }
      if (leftType.isMetaclassType() && rightType.isMetaclassType()) {
        return Type.BOOLEAN;
      }
      // null-comparison: ?T? compared to anything
      if (leftType.isOptional() || rightType.isOptional()) {
        return Type.BOOLEAN;
      }
      if (leftType == Type.ANY || rightType == Type.ANY) {
        return Type.BOOLEAN;
      }
      return Type.ERROR;
    }

    // Ordering
    if (operator.equals("<")
        || operator.equals(">")
        || operator.equals(">=")
        || operator.equals("<=")) {
      if (isNumeric(leftType) && isNumeric(rightType)) {
        return Type.BOOLEAN;
      }
      if (leftType == Type.STRING && rightType == Type.STRING) {
        return Type.BOOLEAN;
      }
      return Type.ERROR;
    }

    return Type.ERROR;
  }

  /**
   * Unwraps a type to its scalar member type for arithmetic/logical operations.
   *
   * <p>Every scalar value is implicitly {@code ¡T!}, so both explicit {@code Singleton!T!} and bare
   * primitive/metaclass types are treated identically. Multi-valued collections (Set, Bag,
   * Sequence, OrderedSet) are NOT unwrapped — applying arithmetic to a collection is a type error.
   *
   * @param t the type to unwrap
   * @return the scalar element type, or t unchanged if it is a multi-valued collection
   */
  public static Type unwrapToScalar(Type t) {
    if (t.isSingleton()) {
      return t.getElementType(); // !T! → T
    }
    if (t.isOptional()) {
      return t.getElementType(); // ?T? → T (for null comparisons)
    }
    return t; // bare primitive/metaclass or multi-valued collection — return as-is
  }

}
