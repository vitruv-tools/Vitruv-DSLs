/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.lsp;

/**
 * Damerau-Levenshtein edit-distance utilities shared by the keyword "did you mean?" suggestion
 * logic in {@link LspErrorListener} and {@link DocumentAnalyzer}.
 *
 * <p>Adjacent transpositions count as a single edit.
 */
final class EditDistance {

  private EditDistance() {}

  /** Maximum edit distance that still counts as a plausible typo for a word of the given length. */
  static int editThreshold(int len) {
    if (len <= 3) return 1;
    if (len <= 6) return 2;
    return 3;
  }

  static int damerauLevenshtein(String a, String b) {
    if (a.equals(b)) return 0;
    if (a.isEmpty()) return b.length();
    if (b.isEmpty()) return a.length();
    int la = a.length();
    int lb = b.length();
    int[][] d = new int[la + 1][lb + 1];
    for (int i = 0; i <= la; i++) d[i][0] = i;
    for (int j = 0; j <= lb; j++) d[0][j] = j;
    for (int i = 1; i <= la; i++) {
      for (int j = 1; j <= lb; j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(d[i-1][j] + 1, Math.min(d[i][j-1] + 1, d[i-1][j-1] + cost));
        if (i > 1 && j > 1
            && a.charAt(i-1) == b.charAt(j-2)
            && a.charAt(i-2) == b.charAt(j-1)) {
          d[i][j] = Math.min(d[i][j], d[i-2][j-2] + cost);
        }
      }
    }
    return d[la][lb];
  }
}
