/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.incremental.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * A live, mutable model for the incremental oracle tests: real EMF resources loaded from the
 * existing test files, exposed through the production {@link VsumWrapper} (over a mocked {@link
 * InternalVirtualModel}, the same pattern as {@code VsumConstraintTest}), with a {@link
 * FakeCorrespondenceModel} and change recording via Vitruvius' {@link ChangeRecorder}.
 *
 * <p>Only the model resources are recorded; correspondence edits are deliberately silent, as in a
 * real VSUM (see {@code CorrespondenceVisibilityExperimentTest}).
 */
public final class LiveModel {

  private static final Path METAMODELS = Path.of("src/test/resources/test-metamodels");
  private static final Path MODELS = Path.of("src/test/resources/test-models");
  private static final String CORRESPONDENCE_NS_URI =
      "http://vitruv.tools/metamodels/change/correspondence/1.0";

  private final ResourceSet resourceSet;
  private final List<Resource> modelResources;
  private final FakeCorrespondenceModel correspondences;
  private final VsumWrapper wrapper;

  private LiveModel(ResourceSet resourceSet, List<Resource> modelResources, FakeCorrespondenceModel correspondences) {
    this.resourceSet = resourceSet;
    this.modelResources = modelResources;
    this.correspondences = correspondences;
    InternalVirtualModel vsum =
        mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));
    when(((ViewSource) vsum).getViewSourceModels())
        .thenAnswer(inv -> List.<Resource>copyOf(this.modelResources));
    when(vsum.getCorrespondenceModel()).thenReturn(correspondences);
    this.wrapper = new VsumWrapper(vsum);
  }

  /** Wraps programmatically built resources (all in {@code resourceSet}) as a live model. */
  public static LiveModel of(
      ResourceSet resourceSet, List<Resource> modelResources, FakeCorrespondenceModel correspondences) {
    return new LiveModel(resourceSet, new ArrayList<>(modelResources), correspondences);
  }

  /** Loads all models of {@code set} (metamodels are registered globally, as in the existing tests). */
  public static LiveModel load(ModelSet set) {
    registerMetamodels(set);
    ResourceSet rs = newResourceSet();
    List<Resource> resources = new ArrayList<>();
    for (String model : set.models()) {
      resources.add(rs.getResource(fileUri(MODELS.resolve(model)), true));
    }
    FakeCorrespondenceModel corr = new FakeCorrespondenceModel();
    if (set.correspondences() != null) {
      Resource corrResource = rs.getResource(fileUri(MODELS.resolve(set.correspondences())), true);
      EcoreUtil.resolveAll(rs);
      fillCorrespondences(corrResource, corr);
      rs.getResources().remove(corrResource);
    } else {
      EcoreUtil.resolveAll(rs);
    }
    return new LiveModel(rs, resources, corr);
  }

  /**
   * Creates a model with the same (empty) resources as {@code set}, so that its content can be
   * added later as one recorded transaction.
   */
  public static LiveModel empty(ModelSet set) {
    registerMetamodels(set);
    ResourceSet rs = newResourceSet();
    List<Resource> resources = new ArrayList<>();
    for (String model : set.models()) {
      resources.add(rs.createResource(fileUri(MODELS.resolve(model))));
    }
    return new LiveModel(rs, resources, new FakeCorrespondenceModel());
  }

  /**
   * Copies the complete content (including cross-resource references and correspondences) of
   * {@code source} into this (empty) model, inside one recorded transaction, and returns the
   * recorded changes.
   */
  public List<EChange<EObject>> addAllContentOf(LiveModel source) {
    EcoreUtil.Copier copier = new EcoreUtil.Copier();
    List<List<EObject>> copiedRoots = new ArrayList<>();
    for (Resource r : source.modelResources) {
      copiedRoots.add(new ArrayList<>(copier.copyAll(r.getContents())));
    }
    copier.copyReferences();
    return record(
        () -> {
          for (int i = 0; i < modelResources.size(); i++) {
            modelResources.get(i).getContents().addAll(copiedRoots.get(i));
          }
          for (Object[] c : source.correspondences.snapshot()) {
            @SuppressWarnings("unchecked")
            List<EObject> left = ((List<EObject>) c[0]).stream().map(copier::get).toList();
            @SuppressWarnings("unchecked")
            List<EObject> right = ((List<EObject>) c[1]).stream().map(copier::get).toList();
            if (!left.contains(null) && !right.contains(null)) {
              correspondences.addCorrespondenceBetween(left, right, (String) c[2]);
            }
          }
        });
  }

  /** Runs {@code edit} while recording the model resources and returns the recorded changes. */
  public List<EChange<EObject>> record(Runnable edit) {
    try (ChangeRecorder recorder = new ChangeRecorder(resourceSet)) {
      for (Resource r : modelResources) {
        recorder.addToRecording(r);
      }
      recorder.beginRecording();
      edit.run();
      return new ArrayList<>(recorder.endRecording().getEChanges());
    }
  }

  public VsumWrapper wrapper() {
    return wrapper;
  }

  public List<Resource> modelResources() {
    return modelResources;
  }

  public FakeCorrespondenceModel correspondences() {
    return correspondences;
  }

  /** All objects contained in the model resources (roots first, then contents). */
  public List<EObject> allObjects() {
    List<EObject> result = new ArrayList<>();
    for (Resource r : modelResources) {
      for (EObject root : List.copyOf(r.getContents())) {
        result.add(root);
        Iterator<EObject> it = root.eAllContents();
        while (it.hasNext()) {
          result.add(it.next());
        }
      }
    }
    return result;
  }

  private static ResourceSet newResourceSet() {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    // The test metamodel correspondence.ecore shares its nsURI with Vitruvius' own correspondence
    // package. Register it only locally, so that a real VSUM in the same JVM is not affected.
    rs.getPackageRegistry().put(CORRESPONDENCE_NS_URI, correspondencePackage());
    return rs;
  }

  private static EPackage correspondencePackage() {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    Resource r = rs.getResource(fileUri(METAMODELS.resolve("correspondence.ecore")), true);
    return (EPackage) r.getContents().get(0);
  }

  private static synchronized void registerMetamodels(ModelSet set) {
    for (String ecore : set.ecores()) {
      ResourceSet rs = new ResourceSetImpl();
      rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
      Resource r = rs.getResource(fileUri(METAMODELS.resolve(ecore)), true);
      EPackage pkg = (EPackage) r.getContents().get(0);
      if (!(EPackage.Registry.INSTANCE.get(pkg.getNsURI()) instanceof EPackage)) {
        EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void fillCorrespondences(Resource corrResource, FakeCorrespondenceModel corr) {
    for (EObject root : corrResource.getContents()) {
      EStructuralFeature list = root.eClass().getEStructuralFeature("correspondences");
      if (list == null) {
        continue;
      }
      for (EObject c : (Collection<EObject>) root.eGet(list)) {
        List<EObject> left = (List<EObject>) c.eGet(c.eClass().getEStructuralFeature("leftEObjects"));
        List<EObject> right = (List<EObject>) c.eGet(c.eClass().getEStructuralFeature("rightEObjects"));
        EStructuralFeature tagFeature = c.eClass().getEStructuralFeature("tag");
        String tag = tagFeature == null ? null : (String) c.eGet(tagFeature);
        if (left.stream().noneMatch(EObject::eIsProxy) && right.stream().noneMatch(EObject::eIsProxy)) {
          corr.addCorrespondenceBetween(List.copyOf(left), List.copyOf(right), tag);
        }
      }
    }
  }

  private static URI fileUri(Path path) {
    return URI.createFileURI(path.toAbsolutePath().toString());
  }
}
