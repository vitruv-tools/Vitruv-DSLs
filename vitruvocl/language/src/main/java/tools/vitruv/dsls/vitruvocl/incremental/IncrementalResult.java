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

import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;

/**
 * Result of one incremental evaluation: all <em>current</em> violations (same semantics as a full
 * batch evaluation) plus the statistics of what was re-evaluated.
 *
 * @param result per-constraint results in the session's constraint order
 * @param stats what was evaluated or skipped
 */
public record IncrementalResult(BatchValidationResult result, IncrementalStats stats) {}
