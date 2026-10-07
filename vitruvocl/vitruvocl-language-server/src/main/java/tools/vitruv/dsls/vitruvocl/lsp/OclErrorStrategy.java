/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.lsp;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Error-recovery strategy for the LSP analysis pass.
 *
 * <p>ANTLR's {@link DefaultErrorStrategy} handles unexpected tokens inside {@code *} and {@code +}
 * loops by calling {@code reportUnwantedToken()}, which reports the error <em>and then consumes the
 * bad token</em> so the loop can continue. In the OCL# grammar this causes the {@code invCS*} loop
 * inside {@code classifierContextCS} to eat tokens that actually belong to the next context block
 * when the {@code context} keyword is misspelled:
 *
 * <pre>
 * context A::B inv foo: ...       ← parsed correctly
 * ↕ blank line
 * constext A::B inv bar: ...      ← "constext" is not CONTEXT
 * </pre>
 *
 * <p>Without an override: {@code sync()} consumes {@code constext}, {@code A}, {@code ::}, {@code
 * B}, then sees {@code inv bar} which IS valid — so it parses {@code bar} as a <em>second invariant
 * of context A::B</em>. The type-checker then reports an error on that mis-parented invariant,
 * producing a squiggle that bleeds into the previous constraint.
 *
 * <p>Fix: override {@link #reportUnwantedToken} to <em>report but not consume</em>. The loop
 * condition {@code while (_la == T__1)} is then false for the first unexpected token, and the loop
 * exits cleanly. The dangling tokens are left in the stream and cause a single syntax-error
 * squiggle at their own positions — no bleeding.
 */
final class OclErrorStrategy extends DefaultErrorStrategy {

  @Override
  protected void reportUnwantedToken(Parser recognizer) {
    if (inErrorRecoveryMode(recognizer)) {
      return;
    }

    beginErrorCondition(recognizer);

    Token t = recognizer.getCurrentToken();
    String tokenName = getTokenErrorDisplay(t);
    IntervalSet expecting = getExpectedTokens(recognizer);
    String msg =
        "extraneous input "
            + tokenName
            + " expecting "
            + expecting.toString(recognizer.getVocabulary());

    recognizer.notifyErrorListeners(t, msg, null);
  }
}
