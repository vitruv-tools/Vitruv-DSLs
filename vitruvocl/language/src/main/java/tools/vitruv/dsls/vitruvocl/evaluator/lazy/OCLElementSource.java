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

package tools.vitruv.dsls.vitruvocl.evaluator.lazy;

import java.util.Iterator;
import java.util.List;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;

/**
 * A replayable, pull-based source of {@link OCLElement}s.
 *
 * <p>Unlike a plain {@link Iterator}, an {@code OCLElementSource} is not consumed by iterating it
 * — each call to {@link #newIterator()} starts a fresh traversal from the beginning. This mirrors
 * OCL's pure, side-effect-free expression semantics: re-evaluating the same expression twice must
 * yield the same elements, so a lazy chain must be able to be walked more than once.
 *
 * <p>Composing sources (via {@link LazyOperations}) never consumes anything by itself — no
 * element is pulled from the ultimate source until a terminal consumer calls {@link
 * #newIterator()} and starts calling {@code next()} on the result.
 */
@FunctionalInterface
public interface OCLElementSource {

  /**
   * Starts a new, independent traversal of this source.
   *
   * @return a fresh iterator; pulling from it may trigger evaluation of upstream predicates/
   *     transformations one element at a time
   */
  Iterator<OCLElement> newIterator();

  /**
   * Wraps an already-materialized list as a source. Used to lift an eager base collection (e.g. a
   * collection literal, or {@code allInstances()}'s result) into a lazy chain.
   *
   * @param list the backing list; re-iterated fresh on every {@link #newIterator()} call
   * @return a source that replays {@code list} on every traversal
   */
  static OCLElementSource of(List<OCLElement> list) {
    return list::iterator;
  }
}
