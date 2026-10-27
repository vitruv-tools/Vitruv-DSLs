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

package tools.vitruv.dsls.vitruvocl.vitruv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.vitruv.framework.vsum.constraints.ConstraintDescriptor;
import tools.vitruv.framework.vsum.constraints.ConstraintDiagnostic;
import tools.vitruv.framework.vsum.constraints.DuplicateConstraintException;
import tools.vitruv.framework.vsum.constraints.InvalidConstraintException;
import tools.vitruv.framework.vsum.constraints.UnknownConstraintException;

/** Tests creating, updating and deleting VitruvOCL constraints at runtime. */
class VitruvOclEditableCheckerTest {
  private static final String POSITIVE = "shop::Item::positive";

  private ShopMetamodel shop;
  private VitruvOclConstraintChecker checker;

  @BeforeEach
  void setUp() {
    shop = new ShopMetamodel();
    shop.register();
    checker = VitruvOclConstraintChecker.fromString("");
  }

  @AfterEach
  void tearDown() {
    shop.unregister();
  }

  @Test
  @DisplayName("a created invariant is listed with an id of context type and name")
  void create() {
    ConstraintDescriptor created =
        checker.create("context shop::Item inv positive: self.price > 0");

    assertEquals(POSITIVE, created.id());
    assertEquals("positive", created.name());
    assertEquals(VitruvOclConstraintChecker.LANGUAGE, checker.getLanguage());
    assertEquals(List.of(created), checker.getConstraints());
  }

  @Test
  @DisplayName("an existing constraint cannot be created again")
  void duplicate() {
    checker.create("context shop::Item inv positive: self.price > 0");

    assertThrows(
        DuplicateConstraintException.class,
        () -> checker.create("context shop::Item inv positive: self.price >= 0"));
  }

  @Test
  @DisplayName("sources with syntax errors are rejected with their position")
  void syntaxError() {
    InvalidConstraintException exception =
        assertThrows(
            InvalidConstraintException.class,
            () -> checker.create("context shop::Item inv positive: self.price >"));

    ConstraintDiagnostic diagnostic = exception.getDiagnostics().get(0);
    assertEquals(1, diagnostic.line());
    assertTrue(checker.getConstraints().isEmpty());
  }

  @Test
  @DisplayName("sources referring to unknown features are rejected with their position")
  void typeError() {
    InvalidConstraintException exception =
        assertThrows(
            InvalidConstraintException.class,
            () -> checker.create("context shop::Item\n  inv heavy: self.weight > 0"));

    assertFalse(exception.getDiagnostics().isEmpty());
    assertEquals(2, exception.getDiagnostics().get(0).line());
    assertTrue(checker.getConstraints().isEmpty());
  }

  @Test
  @DisplayName("exactly one named invariant must be given")
  void singleNamedInvariant() {
    assertThrows(
        InvalidConstraintException.class,
        () -> checker.create("context shop::Item inv: self.price > 0"));
    assertThrows(
        InvalidConstraintException.class,
        () -> checker.create("context shop::Item inv a: true inv b: true"));
    assertThrows(InvalidConstraintException.class, () -> checker.create("-- nothing"));
  }

  @Test
  @DisplayName("an updated constraint keeps its id and gets the new source")
  void update() {
    checker.create("context shop::Item inv positive: self.price > 0");

    ConstraintDescriptor updated =
        checker.update(POSITIVE, "context shop::Item inv positive: self.price > 10");

    assertEquals(POSITIVE, updated.id());
    assertTrue(checker.getConstraints().get(0).source().contains("self.price > 10"));
  }

  @Test
  @DisplayName("an update cannot change the id or introduce errors")
  void invalidUpdate() {
    checker.create("context shop::Item inv positive: self.price > 0");

    assertThrows(
        InvalidConstraintException.class,
        () -> checker.update(POSITIVE, "context shop::Item inv renamed: self.price > 0"));
    assertThrows(
        InvalidConstraintException.class,
        () -> checker.update(POSITIVE, "context shop::Item inv positive: self.weight > 0"));
    assertThrows(
        UnknownConstraintException.class,
        () -> checker.update("shop::Item::unknown", "context shop::Item inv unknown: true"));
    assertTrue(checker.getConstraints().get(0).source().contains("self.price > 0"));
  }

  @Test
  @DisplayName("deleted constraints are no longer listed")
  void delete() {
    checker.create("context shop::Item inv positive: self.price > 0");

    checker.delete(POSITIVE);

    assertTrue(checker.getConstraints().isEmpty());
    assertThrows(UnknownConstraintException.class, () -> checker.delete(POSITIVE));
  }

  @Test
  @DisplayName("constraints loaded from a file can be updated and deleted")
  void builtInConstraints() {
    VitruvOclConstraintChecker withBuiltIn =
        VitruvOclConstraintChecker.fromString("context shop::Item inv positive: self.price > 0");

    withBuiltIn.update(POSITIVE, "context shop::Item inv positive: self.price > 5");
    withBuiltIn.delete(POSITIVE);

    assertTrue(withBuiltIn.getConstraints().isEmpty());
  }
}
