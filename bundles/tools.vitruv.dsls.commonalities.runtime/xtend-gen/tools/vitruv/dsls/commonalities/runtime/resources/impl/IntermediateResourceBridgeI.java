package tools.vitruv.dsls.commonalities.runtime.resources.impl;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper;

@SuppressWarnings("all")
public class IntermediateResourceBridgeI extends IntermediateResourceBridgeImpl {
  private static final URI SAME_FOLDER = URI.createURI(".");

  private Intermediate _intermediateCorrespondence;

  private Intermediate getIntermediateCorrespondence() {
    if ((this._intermediateCorrespondence == null)) {
      this._intermediateCorrespondence = this.findIntermediateCorrespondence(this);
    }
    return this._intermediateCorrespondence;
  }

  private void fileExtensionChanged(final String oldFileExtension) {
    this.fullPathChanged(this.path, this.name, oldFileExtension);
  }

  private void nameChanged(final String oldName) {
    if (this.isPersisted) {
      return;
    }
    this.fullPathChanged(this.path, oldName, this.fileExtension);
  }

  private void pathChanged(final String oldPath) {
    this.fullPathChanged(oldPath, this.name, this.fileExtension);
  }

  private void fullPathChanged(final String oldPath, final String oldName, final String oldFileExtension) {
    if (this.isPersisted) {
      this.discard();
    }
    boolean _canBePersisted = this.canBePersisted();
    if (_canBePersisted) {
      this.persist();
    }
  }

  private void contentChanged() {
    if (this.isPersisted) {
      this.discard();
    }
    boolean _canBePersisted = this.canBePersisted();
    if (_canBePersisted) {
      this.persist();
    }
  }

  @Override
  public String getFullPath() {
    return IntermediateResourceBridgeI.fullPath(this.path, this.name, this.fileExtension);
  }

  private static String fullPath(final String path, final String name, final String fileExtension) {
    if ((((path == null) || (name == null)) || (fileExtension == null))) {
      return null;
    }
    final StringBuilder fullPath = new StringBuilder();
    boolean _isEmpty = path.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      fullPath.append(path);
      boolean _endsWith = path.endsWith("/");
      boolean _not_1 = (!_endsWith);
      if (_not_1) {
        fullPath.append("/");
      }
    }
    fullPath.append(name);
    fullPath.append(".");
    fullPath.append(fileExtension);
    return fullPath.toString();
  }

  private boolean canBePersisted() {
    boolean _xblockexpression = false;
    {
      XtendAssertHelper.assertTrue(((this.content == null) || (this.baseURI != null)));
      _xblockexpression = (((((((this.path != null) && (this.name != null)) && (this.fileExtension != null)) && (this.content != null)) && (this.correspondenceModel != null)) && (this.resourceAccess != null)) && this.isPersistenceEnabled);
    }
    return _xblockexpression;
  }

  private boolean discard() {
    return this.isPersisted = false;
  }

  private void persist() {
    this.resourceAccess.persistAsRoot(this.content, this.getResourceUri());
    this.isPersisted = true;
    if ((this.eContainer == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("IntermediateResourceBridge has not been added to the intermediate model yet: ");
      _builder.append(this);
      throw new RuntimeException(_builder.toString());
    }
  }

  private URI getResourceUri() {
    return this.getResourceUri(this.path, this.name, this.fileExtension);
  }

  private URI getResourceUri(final String path, final String name, final String fileExtension) {
    XtendAssertHelper.assertTrue(((((this.baseURI != null) && (path != null)) && (name != null)) && (fileExtension != null)));
    final URI relativePathUri = URI.createURI(IntermediateResourceBridgeI.fullPath(path, name, fileExtension));
    final URI resourceUri = relativePathUri.resolve(this.baseURI);
    return resourceUri;
  }

  @Override
  public void remove() {
    this.discard();
  }

  @Override
  public void setContent(final EObject newContent) {
    boolean _equals = Objects.equal(this.content, newContent);
    if (_equals) {
      return;
    }
    this.content = newContent;
    if ((this.baseURI == null)) {
      if ((this.correspondenceModel == null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("IntermediateResourceBridge has not yet been setup (correspondence ");
        _builder.append("model is null)!");
        throw new IllegalStateException(_builder.toString());
      }
      this.baseURI = this.calculateBaseUri(this.findPersistedNonIntermediateObject());
    }
    this.contentChanged();
  }

  @Override
  public EObject getContent() {
    return null;
  }

  @Override
  public boolean eIsSet(final int featureID) {
    if ((featureID == ResourcesPackage.RESOURCE__CONTENT)) {
      return false;
    }
    return super.eIsSet(featureID);
  }

  @Override
  public Resource getEmfResource() {
    if ((((((this.resourceAccess == null) || (this.baseURI == null)) || (this.path == null)) || (this.name == null)) || (this.fileExtension == null))) {
      return null;
    }
    final URI resourceUri = this.getResourceUri();
    final Resource resource = this.resourceAccess.getModelResource(resourceUri);
    return resource;
  }

  @Override
  public String getIntermediateId() {
    return this.getFullPath();
  }

  @Override
  public void setPath(final String newPath) {
    boolean _equals = Objects.equal(this.path, newPath);
    if (_equals) {
      return;
    }
    final String oldPath = this.path;
    super.setPath(newPath);
    this.pathChanged(oldPath);
  }

  @Override
  public void setName(final String newName) {
    boolean _equals = Objects.equal(this.name, newName);
    if (_equals) {
      return;
    }
    final String oldName = this.name;
    super.setName(newName);
    this.nameChanged(oldName);
  }

  @Override
  public void setFileExtension(final String newFileExtension) {
    boolean _equals = Objects.equal(this.fileExtension, newFileExtension);
    if (_equals) {
      return;
    }
    final String oldFileExtension = this.fileExtension;
    super.setFileExtension(newFileExtension);
    this.fileExtensionChanged(oldFileExtension);
  }

  @Override
  public void setIsPersistenceEnabled(final boolean newIsPersistenceEnabled) {
    super.setIsPersistenceEnabled(newIsPersistenceEnabled);
    if ((newIsPersistenceEnabled && this.canBePersisted())) {
      this.persist();
    }
  }

  private EObject findPersistedNonIntermediateObject() {
    final Set<Intermediate> intermediates = this.getTransitiveIntermediateCorrespondences(this.getIntermediateCorrespondence());
    Intermediate _intermediateCorrespondenceContainer = this.getIntermediateCorrespondenceContainer();
    intermediates.add(_intermediateCorrespondenceContainer);
    final Function1<Intermediate, Iterable<EObject>> _function = (Intermediate it) -> {
      final Function1<EObject, Boolean> _function_1 = (EObject it_1) -> {
        return Boolean.valueOf((((!(it_1 instanceof Intermediate)) && (!(it_1 instanceof tools.vitruv.dsls.commonalities.runtime.resources.Resource))) && (it_1.eResource() != null)));
      };
      return IterableExtensions.<EObject>filter(this.correspondenceModel.getCorrespondingEObjects(it), _function_1);
    };
    final EObject resourceHaving = IterableExtensions.<EObject>head(IterableExtensions.<Intermediate, EObject>flatMap(intermediates, _function));
    if ((resourceHaving == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find any transitive correspondence or container of ‹");
      _builder.append(this.content);
      _builder.append("› that already has a resource!");
      throw new IllegalStateException(_builder.toString());
    }
    return resourceHaving;
  }

  private Intermediate getIntermediateCorrespondenceContainer() {
    final EObject container = this.getIntermediateCorrespondence().eContainer();
    if ((container instanceof Intermediate)) {
      return ((Intermediate)container);
    } else {
      return null;
    }
  }

  private Set<Intermediate> getTransitiveIntermediateCorrespondences(final Intermediate startIntermediate) {
    final HashSet<Intermediate> existingIntermediate = new HashSet<Intermediate>();
    existingIntermediate.add(startIntermediate);
    return this.getTransitiveIntermediateCorrespondences(existingIntermediate);
  }

  private Set<Intermediate> getTransitiveIntermediateCorrespondences(final Set<Intermediate> foundIntermediates) {
    final Function1<Intermediate, Iterable<Intermediate>> _function = (Intermediate intermediate) -> {
      return Iterables.<Intermediate>filter(this.correspondenceModel.getCorrespondingEObjects(intermediate), Intermediate.class);
    };
    final Set<Intermediate> transitiveIntermediates = IterableExtensions.<Intermediate>toSet(IterableExtensions.<Intermediate, Intermediate>flatMap(foundIntermediates, _function));
    boolean _addAll = foundIntermediates.addAll(transitiveIntermediates);
    if (_addAll) {
      return this.getTransitiveIntermediateCorrespondences(foundIntermediates);
    } else {
      return foundIntermediates;
    }
  }

  private Intermediate findIntermediateCorrespondence(final EObject object) {
    if ((this.correspondenceModel == null)) {
      return null;
    }
    final Intermediate result = IterableExtensions.<Intermediate>head(Iterables.<Intermediate>filter(this.correspondenceModel.getCorrespondingEObjects(object), Intermediate.class));
    if ((result == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find the intermediate correspondence of ‹");
      _builder.append(object);
      _builder.append("›!");
      throw new IllegalStateException(_builder.toString());
    }
    return result;
  }

  private static String withoutFileExtension(final String s) {
    final int dotIndex = s.lastIndexOf(".");
    String _xifexpression = null;
    if ((dotIndex != (-1))) {
      _xifexpression = s.substring(0, dotIndex);
    } else {
      _xifexpression = s;
    }
    return _xifexpression;
  }

  @Override
  public void initialiseForModelElement(final EObject eObject) {
    final Resource resource = eObject.eResource();
    Preconditions.checkArgument((resource != null), "The provided object must be in a resource!");
    final URI objectResourceUri = resource.getURI();
    this.baseURI = this.calculateBaseUri(eObject);
    this.path = IntermediateResourceBridgeI.SAME_FOLDER.resolve(objectResourceUri).deresolve(this.baseURI).toString();
    this.fileExtension = objectResourceUri.fileExtension();
    this.name = IntermediateResourceBridgeI.withoutFileExtension(objectResourceUri.lastSegment().toString());
    this.content = eObject;
    this.isPersisted = true;
  }

  private URI calculateBaseUri(final EObject persistedObject) {
    XtendAssertHelper.assertTrue((persistedObject != null));
    Resource _eResource = persistedObject.eResource();
    boolean _tripleNotEquals = (_eResource != null);
    XtendAssertHelper.assertTrue(_tripleNotEquals);
    final URI projectFileUri = PersistenceHelper.getURIFromSourceProjectFolder(persistedObject, "fake.ext");
    return IntermediateResourceBridgeI.SAME_FOLDER.resolve(projectFileUri);
  }
}
