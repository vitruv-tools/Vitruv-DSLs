package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import org.eclipse.emf.ecore.EObject;

/**
 * Assumptions which all operators need to fulfill:
 * <ul>
 * <li>Every object is contained by at most one other object. This also implies
 * that reference mapping operators are 'disjunct': If one reference mapping
 * operator provides a container for a certain object then no other reference
 * mapping is allowed to provide a different container for the same object.
 * <li>No cyclic containments.
 * <li>No self containments.
 * </ul>
 */
@SuppressWarnings("all")
public interface IReferenceMappingOperator {
  Iterable<? extends EObject> getContainedObjects(final EObject container);

  EObject getContainer(final EObject object);

  boolean isContained(final EObject container, final EObject contained);

  void insert(final EObject container, final EObject object);
}
