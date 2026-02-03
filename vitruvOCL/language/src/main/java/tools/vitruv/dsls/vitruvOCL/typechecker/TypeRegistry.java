package tools.vitruv.dsls.vitruvOCL.typechecker;


/**
 * registry for metamodels in the vsum - access on metaclasses via fully qualified names - chaching
 * if type informations - type conformance checks
 *
 * @see Type for representation of types
 * @see specificationsumWrapper for access to metamodels
 */
public class TypeRegistry {

  /**
   * resolves a fully qualified type name to a type.
   *
   * <p>uses caching: types that have already been resolved are loaded from cache
   *
   * @param qualifiedName fully qualified name
   * @return the resolved type or null
   */
  public Type resolveType(String qualifiedName) {
    // TODO (Caching)
    return null;
  }

  /**
   * checks if a type exixts in Registry
   *
   * @param qualifiedName qualified name
   * @return true if exists
   */
  public boolean hasType(String qualifiedName) {
    // TODO
    return false;
  }
}