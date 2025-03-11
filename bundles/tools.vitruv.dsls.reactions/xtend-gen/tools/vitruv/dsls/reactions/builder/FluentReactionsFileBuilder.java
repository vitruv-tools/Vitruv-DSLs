package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Preconditions;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

@SuppressWarnings("all")
public class FluentReactionsFileBuilder extends FluentReactionElementBuilder {
  @Accessors(AccessorType.PUBLIC_GETTER)
  private final ReactionsFile reactionsFile = TopLevelElementsFactory.eINSTANCE.createReactionsFile();

  @Accessors(AccessorType.PUBLIC_GETTER)
  private String fileName;

  FluentReactionsFileBuilder(final String fileName, final FluentBuilderContext context) {
    super(context);
    this.fileName = fileName;
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    int _size = this.reactionsFile.getReactionsSegments().size();
    boolean _greaterThan = (_size > 0);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("No reactions segments were added to this reactions file (");
    _builder.append(this.fileName);
    _builder.append(")!");
    Preconditions.checkState(_greaterThan, _builder);
  }

  FluentReactionsFileBuilder start() {
    FluentReactionsFileBuilder _xblockexpression = null;
    {
      this.readyToBeAttached = true;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  public void attachTo(final Resource resource) {
    this.triggerBeforeAttached(this.reactionsFile, resource);
    final int resourceContentLength = resource.getContents().size();
    EList<EObject> _contents = resource.getContents();
    _contents.add(this.reactionsFile);
    final EList<EObject> newContents = resource.getContents();
    int _size = newContents.size();
    boolean _greaterThan = (_size > (resourceContentLength + 1));
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Jvm type creation for failed for the reactions file ");
    _builder.append(this.fileName);
    _builder.append("!");
    Preconditions.checkState(_greaterThan, _builder);
    this.triggerAfterJvmTypeCreation();
  }

  public boolean importMetamodel(final MetamodelImport mmImport) {
    FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> _childBuilders = this.getChildBuilders();
    MetamodelImportBuilder _metamodelImportBuilder = new MetamodelImportBuilder(mmImport, this.context);
    return _childBuilders.add(_metamodelImportBuilder);
  }

  public FluentReactionsFileBuilder operator_add(final FluentReactionsSegmentBuilder reactionsSegmentBuilder) {
    FluentReactionsFileBuilder _xblockexpression = null;
    {
      this.checkNotYetAttached();
      EList<ReactionsSegment> _reactionsSegments = this.reactionsFile.getReactionsSegments();
      ReactionsSegment _segment = reactionsSegmentBuilder.getSegment();
      _reactionsSegments.add(_segment);
      FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> _childBuilders = this.getChildBuilders();
      _childBuilders.add(reactionsSegmentBuilder);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("reactions file builder for “");
    _builder.append(this.fileName);
    _builder.append("”");
    return _builder.toString();
  }

  @Pure
  public ReactionsFile getReactionsFile() {
    return this.reactionsFile;
  }

  @Pure
  public String getFileName() {
    return this.fileName;
  }
}
