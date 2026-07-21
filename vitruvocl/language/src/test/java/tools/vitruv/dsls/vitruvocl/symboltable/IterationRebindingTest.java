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

package tools.vitruv.dsls.vitruvocl.symboltable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Contract tests for the "define once, rebind per element" pattern that {@code
 * EvaluationVisitor.defineIterVar}/{@code rebindIterVar} rely on: a single {@link VariableSymbol}
 * is registered once in an iteration's {@link LocalScope} and its bound {@link Value} is then
 * overwritten in place for every subsequent element, instead of allocating (and re-registering) a
 * fresh symbol per element.
 *
 * <p>{@code EvaluationVisitor}'s own {@code defineIterVar}/{@code rebindIterVar} are private, so
 * this class instead proves the two properties the pattern depends on directly against {@link
 * LocalScope} and {@link VariableSymbol}: (1) rebinding via {@link VariableSymbol#setValue} does
 * not change the symbol's identity or its registration in the scope, and (2) a nested scope with a
 * same-named iterator variable still shadows the outer one correctly — i.e. reusing one symbol
 * instance across the elements of a single iteration never leaks across different nesting levels.
 * End-to-end confirmation that nested lazy iterators resolve variables correctly with this pattern
 * lives in {@code LazyChainRegressionTest#nestedSelectInsideCollectResolvesOuterIteratorVariable}.
 */
class IterationRebindingTest {

  private static OCLElement.IntValue v(int i) {
    return new OCLElement.IntValue(i);
  }

  @Test
  void rebindingAnAlreadyRegisteredSymbolPreservesItsIdentityAndScopeRegistration() {
    LocalScope scope = new LocalScope(new GlobalScope());
    VariableSymbol symbol = new VariableSymbol("x", Type.singleton(Type.INTEGER), scope, true);
    scope.defineVariable(symbol);

    VariableSymbol resolvedBefore = scope.resolveVariable("x");
    assertSame(symbol, resolvedBefore);

    // Simulate three "pulled elements": rebind the same symbol's value repeatedly, the way
    // EvaluationVisitor#rebindIterVar does, instead of registering a new symbol each time.
    for (int i = 1; i <= 3; i++) {
      symbol.setValue(new Value(List.of(v(i)), Type.singleton(Type.INTEGER)));

      VariableSymbol resolvedAfter = scope.resolveVariable("x");
      assertSame(symbol, resolvedAfter, "rebinding must not change which symbol is registered");
      assertEquals(i, ((OCLElement.IntValue) resolvedAfter.getValue().getElements().get(0)).value());
    }
  }

  @Test
  void aNestedScopeWithTheSameVariableNameShadowsTheOuterSymbolInstance() {
    // Mirrors two nested single-variable iterations both named `x` (e.g. select(x | ...) inside
    // collect(x | ...)): each nesting level must get its OWN symbol instance registered in its OWN
    // scope - only *within* one level's own iteration is the same instance reused across elements.
    LocalScope outerScope = new LocalScope(new GlobalScope());
    VariableSymbol outerSymbol = new VariableSymbol("x", Type.singleton(Type.INTEGER), outerScope, true);
    outerScope.defineVariable(outerSymbol);
    outerSymbol.setValue(new Value(List.of(v(1)), Type.singleton(Type.INTEGER)));

    LocalScope innerScope = new LocalScope(outerScope);
    VariableSymbol innerSymbol = new VariableSymbol("x", Type.singleton(Type.INTEGER), innerScope, true);
    innerScope.defineVariable(innerSymbol);
    innerSymbol.setValue(new Value(List.of(v(100)), Type.singleton(Type.INTEGER)));

    assertNotSame(outerSymbol, innerSymbol);
    assertSame(innerSymbol, innerScope.resolveVariable("x"), "inner scope must resolve its own x");
    assertSame(outerSymbol, outerScope.resolveVariable("x"), "outer scope must still resolve its own x");

    // Rebinding the inner iteration's element must not disturb the outer iteration's own binding.
    innerSymbol.setValue(new Value(List.of(v(200)), Type.singleton(Type.INTEGER)));
    assertEquals(1, ((OCLElement.IntValue) outerScope.resolveVariable("x").getValue().getElements().get(0)).value());
    assertEquals(200, ((OCLElement.IntValue) innerScope.resolveVariable("x").getValue().getElements().get(0)).value());
  }
}
