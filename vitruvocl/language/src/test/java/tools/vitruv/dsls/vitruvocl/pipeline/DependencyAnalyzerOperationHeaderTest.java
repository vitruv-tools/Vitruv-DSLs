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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The {@code context Type::operation(params)} header (added for pre/post constraints) introduces a
 * second {@code ::} in a context declaration — {@code metamodel::className::operationName} — which
 * {@link DependencyAnalyzer}'s token-scanning heuristic must not mistake for two separate metamodel
 * package references.
 */
class DependencyAnalyzerOperationHeaderTest {

  @Test
  void operationHeaderOnlyTreatsMetamodelAsPackage() {
    Set<String> packages =
        DependencyAnalyzer.analyzeConstraint(
            "context brakesystem::BrakeDisk::inspect(threshold: Integer)"
                + " pre: threshold > 0 post: self.diameterInMM > threshold");

    assertThat(packages).containsExactly("brakesystem");
  }

  @Test
  void plainQualifiedContextStillDetectsPackage() {
    Set<String> packages =
        DependencyAnalyzer.analyzeConstraint("context brakesystem::BrakeDisk inv: self.diameterInMM > 0");

    assertThat(packages).containsExactly("brakesystem");
  }

  @Test
  void enumLiteralAfterEqualsIsStillExcluded() {
    Set<String> packages =
        DependencyAnalyzer.analyzeConstraint(
            "context brakesystem::BrakeDisk::inspect() pre: true"
                + " post: self.unit == Unit::MM");

    assertThat(packages).containsExactly("brakesystem");
  }
}
