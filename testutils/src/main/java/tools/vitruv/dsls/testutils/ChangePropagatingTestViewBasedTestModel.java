package tools.vitruv.dsls.testutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.collect.FluentIterable;

import edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.testutils.views.NonTransactionalTestView;
import tools.vitruv.change.testutils.views.TestView;

/**
 * A {@link TestModel} that propagates performed changes using the functionality
 * of an underlying {@link TestView}.
 * 
 * @param T the model's root element type
 */
public class ChangePropagatingTestViewBasedTestModel<T extends EObject> implements TestViewBasedTestModel<T> {
	private final NonTransactionalTestView testView;
	private final Set<URI> rootResourceURIs = new HashSet<>();
	private final Class<T> rootElementType;

	public ChangePropagatingTestViewBasedTestModel(NonTransactionalTestView testView, Class<T> rootElementType) {
		this.testView = testView;
		this.rootElementType = rootElementType;
	}

	@Override
	public Class<T> getRootElementType() {
		return rootElementType;
	}

	@Override
	public Collection<EObject> getRootObjects() {
		return FluentIterable.from(rootResourceURIs).filter(uri -> uri != null && URIUtil.existsResourceAtUri(uri))
				.transformAndConcat(uri -> testView.resourceAt(uri).getContents()).toSet();
	}

	@Override
	public Collection<T> getTypedRootObjects() {
		return FluentIterable.from(getRootObjects()).filter(rootElementType).toSet();
	}

	@Override
	public void moveRoot(EObject object, URI newLocation) {
		testView.moveTo(object.eResource(), newLocation);
	}

	@Override
	public void registerRoot(EObject object, URI persistAt) {
		rootResourceURIs.add(persistAt);
		Resource resource = testView.resourceAt(persistAt);
		testView.startRecordingChanges(resource);
		resource.getContents().add(object);
	}

	@Override
	public void registerExistingRoot(URI rootResourceURI) {
		rootResourceURIs.add(rootResourceURI);
	}

	@Override
	public void applyChanges(Consumer<TestModel<T>> modelModificationFunction,
			TestViewBasedTestModel<?> otherTestModelToUpdate) {
		Set<Resource> resources = getRootObjects().stream().map(it -> it.eResource()).collect(Collectors.toSet());
		resources.forEach(resource -> testView.startRecordingChanges(resource));
		modelModificationFunction.accept(this);
		resources.forEach(resource -> testView.stopRecordingChanges(resource));
		Iterable<PropagatedChange> changes = testView.propagate();
		FluentIterable.from(changes)
				.transformAndConcat(change -> change.getConsequentialChanges().getAffectedEObjects())
				.filter(otherTestModelToUpdate.getRootElementType()).forEach(object -> {
					if (object.eResource() != null) {
						otherTestModelToUpdate.registerExistingRoot(object.eResource().getURI());
					}
				});
	}

}
