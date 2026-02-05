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
package tools.vitruv.dsls.vitruvOCL.typechecker;

/**
 * Enumeration representing OCL# multiplicities.
 *
 * <p>A {@code Multiplicity} describes how many values a typed expression may evaluate to and, in
 * the case of collections, defines ordering and uniqueness properties.
 *
 * <p>The supported multiplicities are:
 *
 * <ul>
 *   <li>{@link #SINGLETON} ({@code !T!}): exactly one element
 *   <li>{@link #OPTIONAL} ({@code ?T?}): zero or one element
 *   <li>{@link #SET} ({@code {T}}): unordered, unique elements
 *   <li>{@link #SEQUENCE} ({@code [T]}): ordered, non-unique elements
 *   <li>{@link #BAG} ({@code {{T}}}): unordered, non-unique elements
 *   <li>{@link #ORDERED_SET} ({@code <T>}): ordered, unique elements
 * </ul>
 *
 * <p>This enum is used during type checking to reason about collection semantics and multiplicity
 * conformance.
 */
public enum Multiplicity {

  /** Exactly one element ({@code !T!}). */
  SINGLETON("!", "!"),

  /** Zero or one element ({@code ?T?}). */
  OPTIONAL("?", "?"),

  /** Unordered collection with unique elements ({@code {T}}). */
  SET("{", "}"),

  /** Ordered collection with non-unique elements ({@code [T]}). */
  SEQUENCE("[", "]"),

  /** Unordered collection with non-unique elements ({@code {{T}}}). */
  BAG("{{", "}}"),

  /** Ordered collection with unique elements ({@code <T>}). */
  ORDERED_SET("<", ">");

  /** Opening symbol used in the textual representation of the multiplicity. */
  private final String symbol;

  /** Closing symbol used in the textual representation of the multiplicity. */
  private final String closingSymbol;

  Multiplicity(String symbol, String closingSymbol) {
    this.symbol = symbol;
    this.closingSymbol = closingSymbol;
  }

  /**
   * Returns the opening symbol of this multiplicity.
   *
   * @return the opening multiplicity symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the closing symbol of this multiplicity.
   *
   * @return the closing multiplicity symbol
   */
  public String getClosingSymbol() {
    return closingSymbol;
  }

  /**
   * Returns whether this multiplicity represents a collection.
   *
   * @return {@code true} if this multiplicity is a collection type
   */
  public boolean isCollection() {
    return this == SET || this == SEQUENCE || this == BAG || this == ORDERED_SET;
  }

  /**
   * Returns whether this multiplicity enforces element uniqueness.
   *
   * @return {@code true} if elements are unique
   */
  public boolean isUnique() {
    return this == SET || this == ORDERED_SET;
  }

  /**
   * Returns whether this multiplicity preserves element order.
   *
   * @return {@code true} if the collection is ordered
   */
  public boolean isOrdered() {
    return this == SEQUENCE || this == ORDERED_SET;
  }

  /**
   * Checks whether this multiplicity conforms to another multiplicity.
   *
   * <p>The conformance relation follows the OCL# subtyping rules:
   *
   * <pre>
   * !T!  <:  ?T?  <:  {T}
   * </pre>
   *
   * <p>Intuitively:
   *
   * <ul>
   *   <li>A singleton value can be used where any other multiplicity is expected
   *   <li>An optional value can be used where a collection is expected
   * </ul>
   *
   * @param other the target multiplicity
   * @return {@code true} if this multiplicity conforms to {@code other}
   */
  public boolean isConformantTo(Multiplicity other) {
    if (this == other) {
      return true;
    }

    // Singleton conforms to all other multiplicities
    if (this == SINGLETON) {
      return true;
    }

    // Optional conforms to collection multiplicities
    if (this == OPTIONAL && other.isCollection()) {
      return true;
    }

    return false;
  }
}
