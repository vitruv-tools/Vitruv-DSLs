package tools.vitruv.extensions.dslsruntime.reactions

import tools.vitruv.change.propagation.impl.CompositeChangePropagationSpecification
import tools.vitruv.change.composite.MetamodelDescriptor

/**
 * A {@link CompositeChangePropagationSpecification} that contains the reactions change processor.
 * To add further change processors extend the implementing class and override the setup method.
 */
abstract class AbstractReactionsChangePropagationSpecification extends CompositeChangePropagationSpecification {
	new(MetamodelDescriptor sourceMetamodelDescriptor, MetamodelDescriptor targetMetamodelDescriptor) {
		super(sourceMetamodelDescriptor, targetMetamodelDescriptor);
		this.setup();
	}

	/**
	 * Adds the reactions change processor to this {@link CompositeChangePropagationSpecification}.
	 * For adding further change processors overwrite this method and call the super method at the right place.
	 */
	protected abstract def void setup();
}
