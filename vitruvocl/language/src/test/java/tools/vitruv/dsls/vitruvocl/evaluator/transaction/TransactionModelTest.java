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

package tools.vitruv.dsls.vitruvocl.evaluator.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.description.TransactionalChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;

/**
 * M3 unit tests for {@link TransactionModel}, in complete isolation from the OCL evaluator.
 *
 * <p>Transactions are built by recording real EMF mutations through Vitruv's own {@link
 * ChangeRecorder} (against a {@code brakesystem} instance tree), not hand-built {@code EChange}
 * mocks — this exercises {@code EChange} shapes exactly as Vitruv itself produces them (index
 * bookkeeping, old/new value wiring), rather than assumptions baked into a hand-built double.
 */
class TransactionModelTest {

  private static EClass brakeDiskClass;
  private static EAttribute diameterAttr;
  private static EAttribute minimumThicknessAttr;
  private static EClass brakeCaliperClass;
  private static EClass brakePadClass;
  private static EReference brakePadsRef;

  @BeforeAll
  static void loadMetamodel() {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
    File ecoreFile = new File("src/test/resources/test-metamodels/brakesystem.ecore");
    Resource ecoreResource = rs.getResource(URI.createFileURI(ecoreFile.getAbsolutePath()), true);
    EPackage brakesystemPkg = (EPackage) ecoreResource.getContents().get(0);

    brakeDiskClass = (EClass) brakesystemPkg.getEClassifier("BrakeDisk");
    diameterAttr = (EAttribute) brakeDiskClass.getEStructuralFeature("diameterInMM");
    minimumThicknessAttr = (EAttribute) brakeDiskClass.getEStructuralFeature("minimumThicknessInMM");
    brakeCaliperClass = (EClass) brakesystemPkg.getEClassifier("BrakeCaliper");
    brakePadClass = (EClass) brakesystemPkg.getEClassifier("BrakePad");
    brakePadsRef = (EReference) brakeCaliperClass.getEStructuralFeature("brakePads");
  }

  /** Fresh, empty resource set with an XMI resource ready to hold instances, plus a live recorder. */
  private static ResourceSet newResourceSet() {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
    return rs;
  }

  private static Resource newResource(ResourceSet rs) {
    return rs.createResource(URI.createURI("test:/model.xmi"));
  }

  private static TransactionModel record(ResourceSet rs, Runnable mutation) {
    ChangeRecorder recorder = new ChangeRecorder(rs);
    recorder.addToRecording(rs);
    recorder.beginRecording();
    mutation.run();
    TransactionalChange<EObject> change = recorder.endRecording();
    List<EChange<EObject>> changes = change.getEChanges();
    return new TransactionModel(changes);
  }

  // ==================== Single-valued attribute ====================

  @Test
  void singleValuedAttributeReplace_reconstructsPreState() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject brakeDisk = EcoreUtil.create(brakeDiskClass);
    brakeDisk.eSet(diameterAttr, 10);
    resource.getContents().add(brakeDisk);

    TransactionModel tx = record(rs, () -> brakeDisk.eSet(diameterAttr, 42));

    assertThat(tx.getPreStateSingleValue(brakeDisk, diameterAttr, brakeDisk.eGet(diameterAttr)))
        .isEqualTo(10);
  }

  @Test
  void untouchedFeature_preStateEqualsCurrentValue() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject brakeDisk = EcoreUtil.create(brakeDiskClass);
    brakeDisk.eSet(diameterAttr, 10);
    brakeDisk.eSet(minimumThicknessAttr, 5);
    resource.getContents().add(brakeDisk);

    TransactionModel tx = record(rs, () -> brakeDisk.eSet(diameterAttr, 42));

    assertThat(tx.getPreStateSingleValue(brakeDisk, minimumThicknessAttr, brakeDisk.eGet(minimumThicknessAttr)))
        .isEqualTo(5);
  }

  @Test
  void multipleReplacesInOneTransaction_preStateIsValueBeforeFirstReplace() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject brakeDisk = EcoreUtil.create(brakeDiskClass);
    brakeDisk.eSet(diameterAttr, 10);
    resource.getContents().add(brakeDisk);

    TransactionModel tx =
        record(
            rs,
            () -> {
              brakeDisk.eSet(diameterAttr, 20);
              brakeDisk.eSet(diameterAttr, 42);
            });

    assertThat(tx.getPreStateSingleValue(brakeDisk, diameterAttr, brakeDisk.eGet(diameterAttr)))
        .isEqualTo(10);
  }

  @Test
  void emptyTransaction_preStateEqualsCurrentValue() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject brakeDisk = EcoreUtil.create(brakeDiskClass);
    brakeDisk.eSet(diameterAttr, 10);
    resource.getContents().add(brakeDisk);

    TransactionModel tx = new TransactionModel(List.of());

    assertThat(tx.isEmpty()).isTrue();
    assertThat(tx.getPreStateSingleValue(brakeDisk, diameterAttr, brakeDisk.eGet(diameterAttr)))
        .isEqualTo(10);
    assertThat(tx.wasCreated(brakeDisk)).isFalse();
    assertThat(tx.wasDeleted(brakeDisk)).isFalse();
    assertThat(tx.wasModified(brakeDisk)).isFalse();
  }

  // ==================== Multi-valued reference ====================

  @Test
  void insertIntoList_reconstructsPreStateWithoutInsertedValues() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject caliper = EcoreUtil.create(brakeCaliperClass);
    EObject existingPad = EcoreUtil.create(brakePadClass);
    @SuppressWarnings("unchecked")
    List<EObject> pads = (List<EObject>) caliper.eGet(brakePadsRef);
    pads.add(existingPad);
    resource.getContents().add(caliper);

    EObject newPad1 = EcoreUtil.create(brakePadClass);
    EObject newPad2 = EcoreUtil.create(brakePadClass);
    TransactionModel tx =
        record(
            rs,
            () -> {
              pads.add(newPad1);
              pads.add(newPad2);
            });

    @SuppressWarnings("unchecked")
    List<Object> current = (List<Object>) (List<?>) new java.util.ArrayList<>(pads);
    List<Object> preState = tx.getPreStateMultiValue(caliper, brakePadsRef, current);

    assertThat(preState).containsExactly(existingPad);
  }

  @Test
  void removeFromList_reconstructsPreStateWithRemovedValueReinserted() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject caliper = EcoreUtil.create(brakeCaliperClass);
    EObject pad1 = EcoreUtil.create(brakePadClass);
    EObject pad2 = EcoreUtil.create(brakePadClass);
    @SuppressWarnings("unchecked")
    List<EObject> pads = (List<EObject>) caliper.eGet(brakePadsRef);
    pads.add(pad1);
    pads.add(pad2);
    resource.getContents().add(caliper);

    TransactionModel tx = record(rs, () -> pads.remove(pad1));

    @SuppressWarnings("unchecked")
    List<Object> current = (List<Object>) (List<?>) new java.util.ArrayList<>(pads);
    List<Object> preState = tx.getPreStateMultiValue(caliper, brakePadsRef, current);

    assertThat(preState).containsExactly(pad1, pad2);
  }

  // ==================== Lifecycle flags ====================

  @Test
  void wasCreated_trueForNewInstanceOnly() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject existingDisk = EcoreUtil.create(brakeDiskClass);
    resource.getContents().add(existingDisk);

    EObject newDisk = EcoreUtil.create(brakeDiskClass);
    TransactionModel tx = record(rs, () -> resource.getContents().add(newDisk));

    assertThat(tx.wasCreated(newDisk)).isTrue();
    assertThat(tx.wasCreated(existingDisk)).isFalse();
  }

  @Test
  void wasDeleted_trueForRemovedInstance() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject brakeDisk = EcoreUtil.create(brakeDiskClass);
    resource.getContents().add(brakeDisk);

    TransactionModel tx = record(rs, () -> EcoreUtil.delete(brakeDisk));

    assertThat(tx.wasDeleted(brakeDisk)).isTrue();
  }

  @Test
  void wasModified_trueOnlyForInstanceWithRecordedFeatureChange() {
    ResourceSet rs = newResourceSet();
    Resource resource = newResource(rs);
    EObject changedDisk = EcoreUtil.create(brakeDiskClass);
    EObject untouchedDisk = EcoreUtil.create(brakeDiskClass);
    changedDisk.eSet(diameterAttr, 10);
    resource.getContents().add(changedDisk);
    resource.getContents().add(untouchedDisk);

    TransactionModel tx = record(rs, () -> changedDisk.eSet(diameterAttr, 42));

    assertThat(tx.wasModified(changedDisk)).isTrue();
    assertThat(tx.wasModified(untouchedDisk)).isFalse();
  }
}
