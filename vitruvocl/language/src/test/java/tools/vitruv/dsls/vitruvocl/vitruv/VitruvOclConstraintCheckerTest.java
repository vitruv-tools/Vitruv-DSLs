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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.framework.vsum.constraints.ConstraintCheckContext;
import tools.vitruv.framework.vsum.constraints.ConstraintDescriptor;
import tools.vitruv.framework.vsum.constraints.ConstraintKind;
import tools.vitruv.framework.vsum.constraints.ConstraintResult;
import tools.vitruv.framework.vsum.constraints.ConstraintSeverity;
import tools.vitruv.framework.vsum.constraints.ConstraintStatus;
import tools.vitruv.framework.vsum.constraints.ConstraintViolation;

/** Tests for {@link VitruvOclConstraintChecker} against an in-memory model. */
class VitruvOclConstraintCheckerTest {
  private static final String NON_NEGATIVE = "shop::Item::nonNegativePrice";
  private static final String AFFORDABLE = "shop::Item::affordable";
  private static final String NOT_FREE = "shop::Item::notFree";

  private static final String CONSTRAINTS =
      """
      -- constraints for the shop metamodel
      import shop : 'http://vitruv.tools/vitruvocl/test/shop'

      context shop::Item inv nonNegativePrice:
        @severity CRITICAL
        @message "Item {self.name} has a negative price"
        self.price >= 0

      context shop::Item
        -- several invariants in one context
        inv affordable: self.price <= 1000
        inv notFree: self.price > 0
      """;

  @TempDir Path folder;

  private ShopMetamodel shop;
  private Resource resource;

  @BeforeEach
  void setUp() {
    shop = new ShopMetamodel();
    shop.register();
    resource = new ResourceImpl(URI.createURI("test:/items.shop"));
  }

  @AfterEach
  void tearDown() {
    shop.unregister();
  }

  @Test
  @DisplayName("each invariant becomes a constraint, also if a context declares several")
  void describesEachInvariant() {
    List<ConstraintDescriptor> constraints =
        VitruvOclConstraintChecker.fromString(CONSTRAINTS).getConstraints();

    assertEquals(
        List.of(NON_NEGATIVE, AFFORDABLE, NOT_FREE),
        constraints.stream().map(ConstraintDescriptor::id).toList());
    ConstraintDescriptor first = constraints.get(0);
    assertEquals("nonNegativePrice", first.name());
    assertEquals("shop::Item", first.contextType());
    assertEquals(ConstraintKind.INVARIANT, first.kind());
    assertEquals(ConstraintSeverity.CRITICAL, first.severity());
    assertEquals(VitruvOclConstraintChecker.LANGUAGE, first.language());
    assertTrue(first.source().contains("inv nonNegativePrice"));
    assertEquals(ConstraintSeverity.WARNING, constraints.get(1).severity());
    assertFalse(constraints.get(1).source().contains("notFree"));
  }

  @Test
  @DisplayName("a satisfied invariant has no violations")
  void satisfied() {
    resource.getContents().add(shop.createItem("book", 10));

    ConstraintResult result = check(CONSTRAINTS, NON_NEGATIVE);

    assertEquals(ConstraintStatus.SATISFIED, result.status());
    assertTrue(result.violations().isEmpty());
  }

  @Test
  @DisplayName("violations reference the violating element with severity and message")
  void violated() {
    EObject cheap = shop.createItem("coupon", -5);
    resource.getContents().add(shop.createItem("book", 10));
    resource.getContents().add(cheap);

    ConstraintResult result = check(CONSTRAINTS, NON_NEGATIVE);

    assertEquals(ConstraintStatus.VIOLATED, result.status());
    assertEquals(1, result.violations().size());
    ConstraintViolation violation = result.violations().get(0);
    assertSame(cheap, violation.element());
    assertEquals(ConstraintSeverity.CRITICAL, violation.severity());
    assertEquals("Item \"coupon\" has a negative price", violation.message());
  }

  @Test
  @DisplayName("violations of invariants without @message describe the violating element")
  void violationWithoutMessage() {
    resource.getContents().add(shop.createItem("sample", 0));

    ConstraintViolation violation = check(CONSTRAINTS, NOT_FREE).violations().get(0);

    assertNotNull(violation.message());
    assertTrue(violation.message().contains("sample"));
  }

  @Test
  @DisplayName("only the requested constraints are checked, unknown ids are ignored")
  void checksRequestedConstraintsOnly() {
    resource.getContents().add(shop.createItem("free sample", 0));
    VitruvOclConstraintChecker checker = VitruvOclConstraintChecker.fromString(CONSTRAINTS);

    List<ConstraintResult> results =
        checker.check(context(), List.of(NOT_FREE, AFFORDABLE, "shop::Item::unknown"));

    assertEquals(2, results.size());
    assertEquals(ConstraintStatus.VIOLATED, statusOf(results, NOT_FREE));
    assertEquals(ConstraintStatus.SATISFIED, statusOf(results, AFFORDABLE));
  }

  @Test
  @DisplayName("a constraint referencing unknown features results in an error")
  void typeErrorYieldsError() {
    resource.getContents().add(shop.createItem("book", 10));

    ConstraintResult result =
        check("context shop::Item inv broken: self.weight > 0", "shop::Item::broken");

    assertEquals(ConstraintStatus.ERROR, result.status());
    assertFalse(result.errors().isEmpty());
  }

  @Test
  @DisplayName("a failing evaluation only affects its own constraint")
  void evaluationFailureIsIsolated() {
    resource.getContents().add(shop.createBrokenItem(10));
    VitruvOclConstraintChecker checker =
        VitruvOclConstraintChecker.fromString(
            """
            context shop::Item inv named: self.name != ""
            context shop::Item inv positive: self.price > 0
            """);

    List<ConstraintResult> results =
        checker.check(context(), List.of("shop::Item::named", "shop::Item::positive"));

    assertEquals(ConstraintStatus.ERROR, statusOf(results, "shop::Item::named"));
    assertEquals(ConstraintStatus.SATISFIED, statusOf(results, "shop::Item::positive"));
  }

  @Test
  @DisplayName("syntax errors, including trailing input, are rejected when creating the checker")
  void syntaxErrorRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VitruvOclConstraintChecker.fromString("context shop::Item inv broken: self.price >"));
    assertThrows(
        IllegalArgumentException.class,
        () -> VitruvOclConstraintChecker.fromString("context shop::Item inv a: true )"));
  }

  @Test
  @DisplayName("invalid severities are rejected when creating the checker")
  void invalidSeverityRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            VitruvOclConstraintChecker.fromString(
                "context shop::Item inv a: @severity critical true"));
  }

  @Test
  @DisplayName("invariant names must be unique per context only")
  void namesUniquePerContext() {
    VitruvOclConstraintChecker checker =
        VitruvOclConstraintChecker.fromString(
            "context shop::Item inv a: true\ncontext shop::Other inv a: true");
    assertEquals(2, checker.getConstraints().size());

    assertThrows(
        IllegalArgumentException.class,
        () ->
            VitruvOclConstraintChecker.fromString(
                "context shop::Item inv a: true\ncontext shop::Item inv a: false"));
  }

  @Test
  @DisplayName("unnamed invariants are numbered per context")
  void unnamedInvariants() {
    VitruvOclConstraintChecker checker =
        VitruvOclConstraintChecker.fromString(
            "context shop::Item inv: self.price >= 0\ncontext shop::Item inv: self.price < 9");

    assertEquals(
        List.of("shop::Item#1", "shop::Item#2"),
        checker.getConstraints().stream().map(ConstraintDescriptor::id).toList());
  }

  @Test
  @DisplayName("empty and comment-only content has no constraints")
  void emptyContent() {
    assertTrue(VitruvOclConstraintChecker.fromString("").getConstraints().isEmpty());
    assertTrue(VitruvOclConstraintChecker.fromString("-- nothing yet\n").getConstraints().isEmpty());
  }

  @Test
  @DisplayName("constraints can be loaded from several files")
  void fromFiles() throws Exception {
    Path first = Files.writeString(folder.resolve("a.ocl"), "context shop::Item inv a: true");
    Path second = Files.writeString(folder.resolve("b.ocl"), "context shop::Item inv b: true");
    Path duplicate = Files.writeString(folder.resolve("c.ocl"), "context shop::Item inv a: true");

    assertEquals(2, VitruvOclConstraintChecker.fromFiles(first, second).getConstraints().size());
    assertThrows(
        IllegalArgumentException.class,
        () -> VitruvOclConstraintChecker.fromFiles(first, duplicate));
    assertThrows(
        UncheckedIOException.class,
        () -> VitruvOclConstraintChecker.fromFiles(folder.resolve("missing.ocl")));
  }

  @Test
  @DisplayName("constraints can be loaded from the classpath")
  void fromClasspath() {
    VitruvOclConstraintChecker checker =
        VitruvOclConstraintChecker.fromClasspath("vitruv-checker/shop.ocl");

    assertEquals(1, checker.getConstraints().size());
    assertThrows(
        IllegalArgumentException.class,
        () -> VitruvOclConstraintChecker.fromClasspath("vitruv-checker/missing.ocl"));
  }

  private ConstraintResult check(String constraints, String id) {
    VitruvOclConstraintChecker checker = VitruvOclConstraintChecker.fromString(constraints);
    List<ConstraintResult> results = checker.check(context(), List.of(id));
    assertEquals(1, results.size());
    return results.get(0);
  }

  private static ConstraintStatus statusOf(List<ConstraintResult> results, String id) {
    return results.stream()
        .filter(result -> result.descriptor().id().equals(id))
        .findFirst()
        .orElseThrow()
        .status();
  }

  @SuppressWarnings("unchecked")
  private ConstraintCheckContext context() {
    CorrespondenceModelView<Correspondence> correspondences = mock(CorrespondenceModelView.class);
    return new ConstraintCheckContext() {
      @Override
      public Collection<Resource> getModelResources() {
        return List.of(resource);
      }

      @Override
      public CorrespondenceModelView<Correspondence> getCorrespondenceModel() {
        return correspondences;
      }
    };
  }
}
