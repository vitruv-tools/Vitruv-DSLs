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

package tools.vitruv.dsls.vitruvocl.vitruv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.atomic.uuid.UuidResolver;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.description.VitruviusChangeResolverFactory;
import tools.vitruv.change.composite.recording.ChangeRecorder;
import tools.vitruv.change.correspondence.CorrespondencePackage;
import tools.vitruv.change.interaction.UserInteractionFactory;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.constraints.ConstraintStatus;
import tools.vitruv.framework.vsum.constraints.ConstraintValidationReport;
import tools.vitruv.framework.vsum.constraints.ResolvedConstraintViolation;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/** Validates VitruvOCL constraints registered at a real virtual model via the Vitruv SPI. */
class VitruvOclVsumIntegrationTest {
  private static final String FILE_EXTENSION = "shop";

  @TempDir Path folder;

  private ShopMetamodel shop;
  private InternalVirtualModel vsum;

  @BeforeEach
  void setUp() throws Exception {
    shop = new ShopMetamodel();
    shop.register();
    Resource.Factory.Registry.INSTANCE
        .getExtensionToFactoryMap()
        .put(FILE_EXTENSION, new XMIResourceFactoryImpl());
    vsum = buildVsum();
  }

  private InternalVirtualModel buildVsum() throws Exception {
    // other tests remove the correspondence package from the global registry, but a restarted
    // virtual model needs it to load its correspondences
    EPackage.Registry.INSTANCE.put(CorrespondencePackage.eNS_URI, CorrespondencePackage.eINSTANCE);
    return new VirtualModelBuilder()
        .withStorageFolder(folder.resolve("vsum"))
        .withUserInteractor(
            UserInteractionFactory.instance.createUserInteractor(
                UserInteractionFactory.instance.createPredefinedInteractionResultProvider(null)))
        .withConstraintChecker(
            VitruvOclConstraintChecker.fromString(
                "context shop::Item inv nonNegativePrice: self.price >= 0"))
        .buildAndInitialize();
  }

  @AfterEach
  void tearDown() {
    vsum.dispose();
    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().remove(FILE_EXTENSION);
    shop.unregister();
  }

  @Test
  @DisplayName("violations of VitruvOCL constraints are reported with their VSUM UUID")
  void violationsResolvedToVsumUuids() {
    URI modelUri = URI.createFileURI(folder.resolve("items.shop").toString());
    propagateItems(modelUri, shop.createItem("book", 10), shop.createItem("coupon", -5));

    ConstraintValidationReport report = vsum.validateConstraints();

    assertEquals(ConstraintStatus.VIOLATED, report.results().get(0).status());
    List<ResolvedConstraintViolation> violations = report.results().get(0).violations();
    assertEquals(1, violations.size());
    ResolvedConstraintViolation violation = violations.get(0);
    assertNotNull(violation.uuid());
    EObject vsumElement = vsum.getUuidResolver().getEObject(violation.uuid());
    assertSame(violation.element(), vsumElement);
    assertEquals("coupon", vsumElement.eGet(shop.name));
    assertEquals(modelUri, violation.resourceUri());
  }

  @Test
  @DisplayName("VitruvOCL constraints created at runtime are validated and survive a restart")
  void constraintsCreatedAtRuntime() throws Exception {
    URI modelUri = URI.createFileURI(folder.resolve("items.shop").toString());
    propagateItems(modelUri, shop.createItem("book", 2000));

    vsum.createConstraint(
        VitruvOclConstraintChecker.LANGUAGE, "context shop::Item inv cheap: self.price < 1000");

    assertEquals(
        ConstraintStatus.VIOLATED,
        vsum.validateConstraints(List.of("shop::Item::cheap")).results().get(0).status());

    vsum.dispose();
    vsum = buildVsum();

    assertEquals(
        List.of("shop::Item::nonNegativePrice", "shop::Item::cheap"),
        vsum.getConstraints().stream().map(constraint -> constraint.id()).toList());
  }

  private void propagateItems(URI modelUri, EObject... items) {
    ResourceSet resourceSet = new ResourceSetImpl();
    VitruviusChange<Uuid> change;
    try (ChangeRecorder recorder = new ChangeRecorder(resourceSet)) {
      recorder.addToRecording(resourceSet);
      recorder.beginRecording();
      Resource resource = resourceSet.createResource(modelUri);
      resource.getContents().addAll(List.of(items));
      change =
          VitruviusChangeResolverFactory.forUuids(UuidResolver.create(resourceSet))
              .assignIds(recorder.endRecording());
    }
    vsum.propagateChange(change);
  }
}
