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

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.vitruv.change.correspondence.CorrespondencePackage;
import tools.vitruv.change.interaction.InteractionResultProvider;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.ViewSelector;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * A real (not mocked) Vitruvius VSUM for tests, over two tiny dynamic metamodels {@code incra} and
 * {@code incrb}, each with a root class holding a containment list of named elements.
 *
 * <p>Used by the correspondence-visibility experiment and by the end-to-end listener tests, where
 * a hand-written {@link ChangePropagationSpecification} plays the role that generated Reactions
 * code plays in a real methodology: it reacts to changes in {@code incra} by changing {@code
 * incrb} and by editing the correspondence model.
 */
public final class RealVsumFixture {

  public static final String EXT_A = "incra";
  public static final String EXT_B = "incrb";

  public static final EPackage PACKAGE_A = createPackage("incra", "http://vitruvocl.test/incr/a", "A");
  public static final EPackage PACKAGE_B = createPackage("incrb", "http://vitruvocl.test/incr/b", "B");

  static {
    // Other tests register the test metamodel correspondence.ecore under Vitruvius' own
    // correspondence nsURI; a real VSUM needs the real (generated) package there.
    EPackage.Registry.INSTANCE.put(CorrespondencePackage.eNS_URI, CorrespondencePackage.eINSTANCE);
    EPackage.Registry.INSTANCE.put(PACKAGE_A.getNsURI(), PACKAGE_A);
    EPackage.Registry.INSTANCE.put(PACKAGE_B.getNsURI(), PACKAGE_B);
    var factories = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    factories.putIfAbsent(EXT_A, new XMIResourceFactoryImpl());
    factories.putIfAbsent(EXT_B, new XMIResourceFactoryImpl());
  }

  private RealVsumFixture() {}

  /**
   * Creates {@code <prefix>Root { elements: <prefix>Elem[*] (containment) }} and {@code
   * <prefix>Elem { name: EString, trigger: EInt }}.
   */
  private static EPackage createPackage(String name, String nsUri, String prefix) {
    EcoreFactory f = EcoreFactory.eINSTANCE;
    EPackage pkg = f.createEPackage();
    pkg.setName(name);
    pkg.setNsPrefix(name);
    pkg.setNsURI(nsUri);

    EClass elem = f.createEClass();
    elem.setName(prefix + "Elem");
    EAttribute nameAttr = f.createEAttribute();
    nameAttr.setName("name");
    nameAttr.setEType(EcorePackage.Literals.ESTRING);
    EAttribute trigger = f.createEAttribute();
    trigger.setName("trigger");
    trigger.setEType(EcorePackage.Literals.EINT);
    elem.getEStructuralFeatures().add(nameAttr);
    elem.getEStructuralFeatures().add(trigger);

    EClass root = f.createEClass();
    root.setName(prefix + "Root");
    EReference elements = f.createEReference();
    elements.setName("elements");
    elements.setEType(elem);
    elements.setContainment(true);
    elements.setUpperBound(-1);
    root.getEStructuralFeatures().add(elements);

    pkg.getEClassifiers().add(root);
    pkg.getEClassifiers().add(elem);
    return pkg;
  }

  public static EClass rootA() {
    return (EClass) PACKAGE_A.getEClassifier("ARoot");
  }

  public static EClass elemA() {
    return (EClass) PACKAGE_A.getEClassifier("AElem");
  }

  public static EClass rootB() {
    return (EClass) PACKAGE_B.getEClassifier("BRoot");
  }

  public static EClass elemB() {
    return (EClass) PACKAGE_B.getEClassifier("BElem");
  }

  /** Builds and initializes a real VSUM in {@code storage} with the given specifications. */
  public static InternalVirtualModel build(Path storage, ChangePropagationSpecification... specs)
      throws IOException {
    EPackage.Registry.INSTANCE.put(CorrespondencePackage.eNS_URI, CorrespondencePackage.eINSTANCE);
    return new VirtualModelBuilder()
        .withStorageFolder(storage)
        .withUserInteractorForResultProvider(mock(InteractionResultProvider.class))
        .withChangePropagationSpecifications(specs)
        .buildAndInitialize();
  }

  /** Opens a change-recording view over everything currently in {@code vsum}. */
  public static CommittableView openView(InternalVirtualModel vsum) {
    ViewSelector selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("all"));
    selector.getSelectableElements().forEach(e -> selector.setSelected(e, true));
    return selector.createView().withChangeRecordingTrait();
  }
}
