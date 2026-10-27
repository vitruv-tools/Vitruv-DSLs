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

package tools.vitruv.dsls.vitruvocl.pipeline;

import org.eclipse.emf.ecore.EObject;

/**
 * A model element violating a constraint.
 *
 * @param severity the {@code @severity} of the violated invariant, {@code WARNING} by default
 * @param message the interpolated {@code @message} of the invariant, or a description of the
 *     violating element if the invariant has no {@code @message}
 * @param instance the violating element
 */
public record Violation(String severity, String message, EObject instance) {}
