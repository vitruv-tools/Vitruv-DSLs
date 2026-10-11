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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;

/**
 * In-memory correspondence model for tests: symmetric (left/right) and tagged like the Vitruvius
 * one, and — like the real VSUM (see {@code CorrespondenceVisibilityExperimentTest}) — editable
 * without producing any recorded model change.
 */
public final class FakeCorrespondenceModel implements EditableCorrespondenceModelView<Correspondence> {

  private record Entry(List<EObject> left, List<EObject> right, String tag) {}

  private final List<Entry> entries = new ArrayList<>();

  /** Number of stored correspondences. */
  public int size() {
    return entries.size();
  }

  /** The stored correspondences as (left, right, tag) triples of single objects, for mutation. */
  public List<Object[]> snapshot() {
    return entries.stream().map(e -> new Object[] {e.left(), e.right(), e.tag()}).toList();
  }

  /** Removes the {@code index}-th correspondence. */
  public void removeAt(int index) {
    entries.remove(index);
  }

  @Override
  public Correspondence addCorrespondenceBetween(
      List<EObject> leftEObjects, List<EObject> rightEObjects, String tag) {
    entries.add(new Entry(List.copyOf(leftEObjects), List.copyOf(rightEObjects), tag));
    return null;
  }

  @Override
  public Set<Correspondence> removeCorrespondencesBetween(
      List<EObject> leftEObjects, List<EObject> rightEObjects, String tag) {
    entries.removeIf(
        e ->
            sameObjects(e.left(), leftEObjects)
                && sameObjects(e.right(), rightEObjects)
                && (tag == null || tag.equals(e.tag())));
    return Set.of();
  }

  @Override
  public boolean hasCorrespondences(List<EObject> eObjects) {
    return !getCorrespondingEObjects(eObjects).isEmpty();
  }

  @Override
  public Set<List<EObject>> getCorrespondingEObjects(List<EObject> eObjects) {
    return corresponding(eObjects, null);
  }

  @Override
  public Set<List<EObject>> getCorrespondingEObjects(List<EObject> eObjects, String tag) {
    return corresponding(eObjects, tag);
  }

  @Override
  public Set<EObject> getAllEObjectsInACorrespondence() {
    Set<EObject> result = new LinkedHashSet<>();
    for (Entry e : entries) {
      result.addAll(e.left());
      result.addAll(e.right());
    }
    return result;
  }

  @Override
  public Set<String> getAllTags() {
    Set<String> result = new LinkedHashSet<>();
    for (Entry e : entries) {
      if (e.tag() != null) {
        result.add(e.tag());
      }
    }
    return result;
  }

  @Override
  public Map<String, Set<EObject>> getCorrespondingEObjectsWithTag(List<EObject> eObjects) {
    Map<String, Set<EObject>> result = new LinkedHashMap<>();
    for (Entry e : entries) {
      List<EObject> other =
          sameObjects(e.left(), eObjects) ? e.right() : sameObjects(e.right(), eObjects) ? e.left() : null;
      if (other != null) {
        result.computeIfAbsent(e.tag(), k -> new LinkedHashSet<>()).addAll(other);
      }
    }
    return result;
  }

  private Set<List<EObject>> corresponding(List<EObject> eObjects, String tag) {
    Set<List<EObject>> result = new LinkedHashSet<>();
    for (Entry e : entries) {
      if (tag != null && !Objects.equals(tag, e.tag())) {
        continue;
      }
      if (sameObjects(e.left(), eObjects)) {
        result.add(e.right());
      }
      if (sameObjects(e.right(), eObjects)) {
        result.add(e.left());
      }
    }
    return result;
  }

  private static boolean sameObjects(List<EObject> a, List<EObject> b) {
    if (a.size() != b.size()) {
      return false;
    }
    for (int i = 0; i < a.size(); i++) {
      if (a.get(i) != b.get(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <V extends Correspondence> CorrespondenceModelView<V> getView(Class<V> correspondenceType) {
    throw new UnsupportedOperationException("not needed by the tests");
  }

  @Override
  public <V extends Correspondence> EditableCorrespondenceModelView<V> getEditableView(
      Class<V> correspondenceType, Supplier<V> correspondenceCreator) {
    throw new UnsupportedOperationException("not needed by the tests");
  }
}
