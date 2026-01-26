package tools.vitruv.dsls.vitruvOCL.common;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EObject;

/**
 * Central access point for Virtual Single Underlying Model (VSUM) operations.
 * 
 * Responsibilities:
 * - Metamodel registration and EClass resolution
 * - Variable binding and scope management
 * - (Future) VSUM instance queries and correspondence navigation
 * 
 * Immutable: New scopes create new instances.
 * 
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor
 */
public class VSUMWrapper {

    // Registered metamodels (name -> EPackage)
    private final Map<String, EPackage> metamodels;

    // Variable bindings for current scope
    private final Map<String, Object> variables;

    /**
     * Creates empty VSUMWrapper.
     */
    public VSUMWrapper() {
        this.metamodels = new HashMap<>();
        this.variables = new HashMap<>();
    }

    /**
     * Creates VSUMWrapper with existing state (for scope extension).
     */
    private VSUMWrapper(Map<String, EPackage> metamodels, Map<String, Object> variables) {
        this.metamodels = new HashMap<>(metamodels);
        this.variables = new HashMap<>(variables);
    }

    // ==================== Metamodel Registration ====================

    /**
     * Registers a metamodel by name.
     * 
     * @param name     Metamodel identifier (e.g., "uml", "java")
     * @param ePackage The EPackage root
     */
    public void registerMetamodel(String name, EPackage ePackage) {
        metamodels.put(name, ePackage);
    }

    /**
     * Resolves an EClass from a registered metamodel.
     * 
     * @param metamodel Metamodel name
     * @param className Class name within metamodel
     * @return The resolved EClass
     * @throws IllegalArgumentException if metamodel or class not found
     */
    public EClass resolveEClass(String metamodel, String className) {
        EPackage pkg = metamodels.get(metamodel);
        if (pkg == null) {
            throw new IllegalArgumentException("Unknown metamodel: " + metamodel);
        }

        EClassifier classifier = pkg.getEClassifier(className);
        if (classifier == null) {
            throw new IllegalArgumentException(
                    "Class not found: " + className + " in metamodel " + metamodel);
        }

        if (!(classifier instanceof EClass)) {
            throw new IllegalArgumentException(
                    className + " is not an EClass in metamodel " + metamodel);
        }

        return (EClass) classifier;
    }

    // ==================== Variable Binding ====================

    /**
     * Creates new scope with additional variable binding.
     * 
     * @param name  Variable name
     * @param value Variable value
     * @return New VSUMWrapper with extended scope
     */
    public VSUMWrapper withVariable(String name, Object value) {
        VSUMWrapper newWrapper = new VSUMWrapper(this.metamodels, this.variables);
        newWrapper.variables.put(name, value);
        return newWrapper;
    }

    /**
     * Retrieves variable value from current scope.
     * 
     * @param name Variable name
     * @return Variable value, or null if not found
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Checks if variable exists in current scope.
     * 
     * @param name Variable name
     * @return true if variable is bound
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    // ==================== VSUM Instance Queries (TODO) ====================

    /**
     * Retrieves all instances of an EClass from the VSUM.
     * 
     * TODO: Implement actual VSUM query
     * 
     * @param eClass The metaclass to query
     * @return List of instances
     */
    public List<EObject> allInstances(EClass eClass) {
        // Stub for now
        return new ArrayList<>();
    }

    /**
     * Retrieves an EObject by ID from the VSUM.
     * 
     * TODO: Implement actual VSUM lookup
     * 
     * @param id Object identifier
     * @return The EObject, or null if not found
     */
    public EObject getObjectById(String id) {
        // Stub for now
        return null;
    }

    // ==================== Correspondence Navigation (TODO) ====================

    /**
     * Navigates correspondences between metamodels (~ operator).
     * 
     * TODO: Implement correspondence resolution
     * 
     * @param sourceObject    Source EObject
     * @param targetMetamodel Target metamodel name
     * @param targetClass     Target class name
     * @return Corresponding objects in target metamodel
     */
    public List<EObject> getCorrespondences(EObject sourceObject,
            String targetMetamodel,
            String targetClass) {
        // Stub for now
        return new ArrayList<>();
    }
}