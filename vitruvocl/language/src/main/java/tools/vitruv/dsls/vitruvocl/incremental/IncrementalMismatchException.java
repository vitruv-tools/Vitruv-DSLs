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

package tools.vitruv.dsls.vitruvocl.incremental;

/**
 * Thrown in oracle mode when an incremental evaluation's outcome differs from a full evaluation of
 * the same model state. The message contains the diff.
 */
public class IncrementalMismatchException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public IncrementalMismatchException(String diff) {
    super(diff);
  }
}
