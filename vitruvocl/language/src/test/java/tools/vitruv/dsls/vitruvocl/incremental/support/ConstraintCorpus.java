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

package tools.vitruv.dsls.vitruvocl.incremental.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The constraint corpus for the oracle tests: every {@code context …} constraint string found in
 * the existing test sources ({@code src/test/resources/incremental/corpus/
 * constraints-from-existing-tests.txt}, extracted mechanically, entries separated by {@code ###}
 * lines) plus the hand-written edge-case constraints next to it.
 */
public final class ConstraintCorpus {

  private static final Path DIR = Path.of("src/test/resources/incremental/corpus");

  private ConstraintCorpus() {}

  /** All corpus constraints whose context or body mentions one of {@code set}'s metamodels. */
  public static List<String> forModelSet(ModelSet set) {
    List<String> result = new ArrayList<>();
    for (String constraint : all()) {
      for (String name : set.metamodelNames()) {
        if (Pattern.compile("\\b" + Pattern.quote(name) + "::").matcher(constraint).find()) {
          result.add(constraint);
          break;
        }
      }
    }
    return result;
  }

  /** Every constraint of every corpus file. */
  public static List<String> all() {
    List<String> result = new ArrayList<>();
    result.addAll(read(DIR.resolve("constraints-from-existing-tests.txt")));
    result.addAll(read(DIR.resolve("edge-cases.txt")));
    return result;
  }

  private static List<String> read(Path file) {
    if (!Files.exists(file)) {
      return List.of();
    }
    List<String> lines;
    try {
      lines = Files.readAllLines(file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    List<String> result = new ArrayList<>();
    StringBuilder current = null;
    for (String line : lines) {
      if (line.equals("###")) {
        if (current != null && !current.toString().isBlank()) {
          result.add(current.toString().strip());
        }
        current = new StringBuilder();
      } else if (line.startsWith("### ")) {
        continue; // header comment
      } else if (current != null) {
        current.append(line).append('\n');
      }
    }
    if (current != null && !current.toString().isBlank()) {
      result.add(current.toString().strip());
    }
    return result;
  }
}
