package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.HashMap;
import java.util.Map;
import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;

/**
 * registry for metamodels in the vsum
 * - access on metaclasses via fully qualified names
 * - chaching if type informations
 * - type conformance checks
 * 
 * 
 * @see Type for representation of types
 * @see VSUMWrapper for access to metamodels
 */
public class TypeRegistry {
    
    private final VSUMWrapper vsumWrapper;
    private final Map<String, Type> typeCache = new HashMap<>();
    
    public TypeRegistry(VSUMWrapper vsumWrapper) {
        this.vsumWrapper = vsumWrapper;
    }
    
    /**
     * resolves a fully qualified type name to a type.
     * 
     * uses caching: types that have already been resolved are loaded from cache
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