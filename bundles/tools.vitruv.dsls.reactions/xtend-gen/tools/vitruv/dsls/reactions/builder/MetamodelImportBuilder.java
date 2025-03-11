package tools.vitruv.dsls.reactions.builder;

import tools.vitruv.dsls.common.elements.MetamodelImport;

@SuppressWarnings("all")
public class MetamodelImportBuilder extends FluentReactionElementBuilder {
  private MetamodelImport mmImport;

  public MetamodelImportBuilder(final MetamodelImport mmImport, final FluentBuilderContext context) {
    super(context);
    this.mmImport = mmImport;
    this.readyToBeAttached = true;
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    this.metamodelImport(this.mmImport.getPackage(), this.mmImport.getName());
  }
}
