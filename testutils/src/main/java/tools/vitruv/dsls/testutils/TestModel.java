package tools.vitruv.dsls.testutils;

import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * A model that can be used in tests to encapsulate access to root elements of
 * test model.
 *
 * @param <T> the root element type
 */
public interface TestModel<T extends EObject> {
	Class<T> getRootElementType();

	/**
	 * Provides the root elements of this model.
	 */
	Collection<? extends EObject> getRootObjects();

	/**
	 * Provides all root elements of this model that conform to the root element
	 * type.
	 */
	Collection<T> getTypedRootObjects();

	/**
	 * Persists the given object at the given {@link URI} and adds it as a model
	 * root.
	 * 
	 * @param object    the object to persist, must not be {@code null}
	 * @param persistAt the URI to persist the object at, must be resolvable
	 */
	void registerRoot(T object, URI persistAt);

	/**
	 * Moves the given object to the given {@link URI}. The given {@link EObject}
	 * must already be a root object of the model, otherwise an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param object      the object to persist, must not be {@code null}
	 * @param newLocation the URI to move the object to, must be resolvable
	 */
	void moveRoot(T object, URI newLocation);

}
