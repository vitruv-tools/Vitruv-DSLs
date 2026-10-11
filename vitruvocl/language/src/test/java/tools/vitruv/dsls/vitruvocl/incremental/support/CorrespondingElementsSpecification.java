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

import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.change.atomic.feature.reference.InsertEReference;
import tools.vitruv.change.atomic.feature.reference.RemoveEReference;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification;
import tools.vitruv.change.utils.ResourceAccess;

/**
 * Hand-written stand-in for generated Reactions code, mirroring what typical Reactions do with the
 * correspondence model:
 *
 * <ul>
 *   <li>new {@code ARoot} → create {@code BRoot} (plus one pre-existing "spare" {@code BElem}) and
 *       a correspondence {@code ARoot ~ BRoot} (tag {@code root});
 *   <li>{@code AElem} inserted into {@code ARoot.elements} → create {@code BElem} and a
 *       correspondence (tag {@code elem});
 *   <li>{@code AElem} removed → remove the correspondence and the partner {@code BElem};
 *   <li>{@code AElem.trigger := 1} → add a correspondence (tag {@code late}) between the {@code
 *       AElem} and the <em>already existing</em> spare {@code BElem} — no model change in {@code
 *       incrb}; {@code trigger := 2} removes it again.
 * </ul>
 */
public final class CorrespondingElementsSpecification extends AbstractChangePropagationSpecification {

  public static final String SPARE_NAME = "spare";

  public CorrespondingElementsSpecification() {
    super(
        MetamodelDescriptor.of(RealVsumFixture.PACKAGE_A),
        MetamodelDescriptor.of(RealVsumFixture.PACKAGE_B));
  }

  @Override
  public boolean doesHandleChange(
      EChange<EObject> change, EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    return change instanceof InsertRootEObject<EObject> insert
            && insert.getNewValue().eClass() == RealVsumFixture.rootA()
        || change instanceof FeatureEChange<?, ?> featureChange
            && ((EObject) featureChange.getAffectedElement()).eClass().getEPackage()
                == RealVsumFixture.PACKAGE_A;
  }

  @Override
  public void propagateChange(
      EChange<EObject> change,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel,
      ResourceAccess resourceAccess) {
    if (change instanceof InsertRootEObject<EObject> insert) {
      createPartnerRoot(insert, correspondenceModel, resourceAccess);
    } else if (change instanceof InsertEReference<EObject> insert && insert.isContainment()) {
      createPartnerElement(insert, correspondenceModel);
    } else if (change instanceof RemoveEReference<EObject> remove && remove.isContainment()) {
      removePartnerElement(remove, correspondenceModel);
    } else if (change instanceof ReplaceSingleValuedEAttribute<EObject, ?> replace
        && "trigger".equals(replace.getAffectedFeature().getName())) {
      toggleLateCorrespondence(replace, correspondenceModel);
    }
  }

  private static void createPartnerRoot(
      InsertRootEObject<EObject> insert,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel,
      ResourceAccess resourceAccess) {
    EObject rootA = insert.getNewValue();
    EObject rootB = EcoreUtil.create(RealVsumFixture.rootB());
    EObject spare = EcoreUtil.create(RealVsumFixture.elemB());
    spare.eSet(RealVsumFixture.elemB().getEStructuralFeature("name"), SPARE_NAME);
    elements(rootB).add(spare);
    URI uriB = URI.createURI(insert.getUri()).trimFileExtension().appendFileExtension(RealVsumFixture.EXT_B);
    resourceAccess.persistAsRoot(rootB, uriB);
    correspondenceModel.addCorrespondenceBetween(rootA, rootB, "root");
  }

  private static void createPartnerElement(
      InsertEReference<EObject> insert,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    EObject elemA = insert.getNewValue();
    EObject rootB = single(correspondenceModel.getCorrespondingEObjects(insert.getAffectedElement(), "root"));
    if (rootB == null) {
      return;
    }
    EObject elemB = EcoreUtil.create(RealVsumFixture.elemB());
    elemB.eSet(
        RealVsumFixture.elemB().getEStructuralFeature("name"),
        elemA.eGet(RealVsumFixture.elemA().getEStructuralFeature("name")));
    elements(rootB).add(elemB);
    correspondenceModel.addCorrespondenceBetween(elemA, elemB, "elem");
  }

  private static void removePartnerElement(
      RemoveEReference<EObject> remove,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    EObject elemA = remove.getOldValue();
    for (EObject elemB : List.copyOf(correspondenceModel.getCorrespondingEObjects(elemA, "elem"))) {
      correspondenceModel.removeCorrespondencesBetween(elemA, elemB, "elem");
      EcoreUtil.delete(elemB);
    }
  }

  private static void toggleLateCorrespondence(
      ReplaceSingleValuedEAttribute<EObject, ?> replace,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    EObject elemA = replace.getAffectedElement();
    EObject rootB =
        single(correspondenceModel.getCorrespondingEObjects(elemA.eContainer(), "root"));
    if (rootB == null) {
      return;
    }
    EObject spare =
        elements(rootB).stream()
            .filter(e -> SPARE_NAME.equals(e.eGet(RealVsumFixture.elemB().getEStructuralFeature("name"))))
            .findFirst()
            .orElse(null);
    if (spare == null) {
      return;
    }
    Object newValue = replace.getNewValue();
    if (Integer.valueOf(1).equals(newValue)) {
      correspondenceModel.addCorrespondenceBetween(elemA, spare, "late");
    } else if (Integer.valueOf(2).equals(newValue)) {
      correspondenceModel.removeCorrespondencesBetween(elemA, spare, "late");
    }
  }

  @SuppressWarnings("unchecked")
  private static List<EObject> elements(EObject root) {
    return (List<EObject>) root.eGet(root.eClass().getEStructuralFeature("elements"));
  }

  private static EObject single(java.util.Set<EObject> objects) {
    return objects.isEmpty() ? null : objects.iterator().next();
  }
}
