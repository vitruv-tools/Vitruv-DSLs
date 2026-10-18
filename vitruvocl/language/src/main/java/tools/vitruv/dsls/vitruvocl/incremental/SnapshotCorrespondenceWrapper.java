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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Step C4 of {@code docs/correspondence-incremental-plan.md}: a view of a wrapper that answers
 * every correspondence query from a {@link CorrespondenceSnapshot} of the current state and
 * delegates everything else.
 *
 * <p>The session captures the snapshot once per transaction anyway (to observe correspondence
 * changes), so all constraints evaluated for that transaction share it instead of scanning the
 * correspondence model per object. Valid only while the model is in the captured state; the
 * session creates a new view for every transaction. Stateless apart from the immutable snapshot,
 * so it may be used from several worker threads.
 *
 * <p>The answers equal the wrapper's because the snapshot stores the wrapper's own per-tag lookups
 * and the untagged lookup is their union; {@link #correspondenceHasTag} is the two-way tagged
 * lookup, which is the contract of {@link MetamodelWrapperInterface#hasTaggedCorrespondenceLookup()}
 * that every wrapper with correspondence enumeration (the precondition for a snapshot) fulfils;
 * the session uses this view only for wrappers that declare it. The snapshot compares objects by
 * identity, Vitruvius by {@code equals}; both agree for EMF objects, which do not override it.
 */
final class SnapshotCorrespondenceWrapper implements MetamodelWrapperInterface {

  private final MetamodelWrapperInterface delegate;
  private final CorrespondenceSnapshot snapshot;

  SnapshotCorrespondenceWrapper(MetamodelWrapperInterface delegate, CorrespondenceSnapshot snapshot) {
    this.delegate = delegate;
    this.snapshot = snapshot;
  }

  // ------------------------------------------------------------------ correspondence: snapshot

  @Override
  public Set<EObject> getCorrespondingObjects(EObject source) {
    return snapshot.correspondents(source);
  }

  @Override
  public Set<EObject> getCorrespondingObjectsWithTag(EObject source, String tag) {
    return snapshot.correspondents(source, tag);
  }

  @Override
  public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
    return snapshot.correspondents(obj1, tag).contains(obj2)
        || snapshot.correspondents(obj2, tag).contains(obj1);
  }

  @Override
  public boolean hasTaggedCorrespondenceLookup() {
    return true;
  }

  @Override
  public Optional<Set<EObject>> getAllObjectsInACorrespondence() {
    return Optional.of(snapshot.participants());
  }

  @Override
  public Map<String, Set<EObject>> getCorrespondingObjectsByTag(EObject source) {
    return snapshot.correspondentsByTag(source);
  }

  // ------------------------------------------------------------------ everything else: delegate

  @Override
  public EClass resolveEClass(String metamodelName, String className) {
    return delegate.resolveEClass(metamodelName, className);
  }

  @Override
  public List<EObject> getAllInstances(EClass eClass) {
    return delegate.getAllInstances(eClass);
  }

  @Override
  public EEnum resolveEEnum(String enumName) {
    return delegate.resolveEEnum(enumName);
  }

  @Override
  public Set<String> getAvailableMetamodels() {
    return delegate.getAvailableMetamodels();
  }

  @Override
  public String getInstanceNameByIndex(int index) {
    return delegate.getInstanceNameByIndex(index);
  }

  @Override
  public List<EObject> getAllRootObjects() {
    return delegate.getAllRootObjects();
  }

  @Override
  public EObject getContextObjectByIndex(int index) {
    return delegate.getContextObjectByIndex(index);
  }

  @Override
  public EClass resolveEClassByShortName(String shortName) {
    return delegate.resolveEClassByShortName(shortName);
  }

  @Override
  public String getSourceFileForInstance(EObject instance) {
    return delegate.getSourceFileForInstance(instance);
  }
}
