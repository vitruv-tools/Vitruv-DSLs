package tools.vitruv.dsls.reactions.builder;

import tools.vitruv.dsls.common.elements.MetamodelImport;

public class MetamodelImportBuilder extends FluentReactionElementBuilder {
	private MetamodelImport mmImport;

	public MetamodelImportBuilder (MetamodelImport mmImport, FluentBuilderContext context) {
		super(context);
		this.mmImport = mmImport;
		readyToBeAttached = true;
	}

	@Override
	protected void attachmentPreparation() {
		super.attachmentPreparation();
		metamodelImport(mmImport.getPackage(), mmImport.getName());
	}
}
