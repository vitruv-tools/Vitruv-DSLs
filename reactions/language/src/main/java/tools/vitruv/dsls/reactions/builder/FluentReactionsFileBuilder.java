package tools.vitruv.dsls.reactions.builder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Accessors;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

import static com.google.common.base.Preconditions.*;

public class FluentReactionsFileBuilder extends FluentReactionElementBuilder {

	@Getter(AccessLevel.PUBLIC)
	private final ReactionsFile reactionsFile = TopLevelElementsFactory.eINSTANCE.createReactionsFile();

	private final String fileName;

	public String getFileName() {
		return fileName;
	}

	FluentReactionsFileBuilder(String fileName, FluentBuilderContext context) {
		super(context);
		this.fileName = fileName;
	}

	@Override
	protected void attachmentPreparation() {
		super.attachmentPreparation();
		checkState(!reactionsFile.getReactionsSegments().isEmpty(),
			"No reactions segments were added to this reactions file (" + fileName + ")!");
	}

	FluentReactionsFileBuilder start() {
		readyToBeAttached = true;
		return this;
	}

	public void attachTo(Resource resource) {
		triggerBeforeAttached(reactionsFile, resource);
		int resourceContentLength = resource.getContents().size();
		resource.getContents().add(reactionsFile);

		// this call has the (desired!) side effect to create the jvm types!
		EList<EObject> newContents = resource.getContents();
		// very rough check
		checkState(newContents.size() > resourceContentLength + 1,
			"Jvm type creation for failed for the reactions file " + fileName + "!");
		triggerAfterJvmTypeCreation();
	}

	public void importMetamodel(MetamodelImport mmImport) {
		getChildBuilders().add(new MetamodelImportBuilder(mmImport, context));
	}

	public FluentReactionsFileBuilder operator_add(FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
		checkNotYetAttached();
		reactionsFile.getReactionsSegments().add(reactionsSegmentBuilder.getSegment());
		getChildBuilders().add(reactionsSegmentBuilder);
		return this;
	}

	@Override
	public String toString() {
		return "reactions file builder for " + fileName;
	}
}

