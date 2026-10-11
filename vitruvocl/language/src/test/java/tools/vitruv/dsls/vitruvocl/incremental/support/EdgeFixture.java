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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * A small dynamic metamodel {@code edge} for targeted edge-case tests, with inheritance, a
 * feature name shared by unrelated classes, ordered non-unique references and containment:
 *
 * <pre>
 * Root  { items: Item[*] (containment), others: Other[*] (containment), tags: EString[*] (non-unique) }
 * Item  (abstract) { name: EString, value: EInt, next: Item[0..1], refs: Item[*] (ordered, non-unique),
 *                    owner: Root[0..1] (container, opposite of Root.items), cache: EInt (transient) }
 * SubA  extends Item { extra: EInt }
 * SubB  extends Item { label: EString }
 * Other { label: EString, amount: EInt }
 * </pre>
 *
 * Initial model: {@code edge1.edge} with {@code Root r1 { items: a1(SubA, value 5, extra 1, next
 * b1), b1(SubB, value 3, label "lb1", refs [a1, a2]), a2(SubA, value 7, extra 2); others: o1(label
 * "x", amount 1), o2(label "y", amount 2) }} and {@code edge2.edge} with {@code Root r2 { items:
 * b2(SubB, value 9) }}.
 */
public final class EdgeFixture {

  public static final EPackage PACKAGE = createPackage();

  static {
    EPackage.Registry.INSTANCE.put(PACKAGE.getNsURI(), PACKAGE);
  }

  private final LiveModel model;

  private EdgeFixture() {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    Resource r1 = rs.createResource(URI.createURI("memory:/edge1.edge"));
    Resource r2 = rs.createResource(URI.createURI("memory:/edge2.edge"));

    EObject root1 = create("Root");
    EObject a1 = item("SubA", "a1", 5);
    a1.eSet(feature(a1, "extra"), 1);
    EObject b1 = item("SubB", "b1", 3);
    b1.eSet(feature(b1, "label"), "lb1");
    EObject a2 = item("SubA", "a2", 7);
    a2.eSet(feature(a2, "extra"), 2);
    list(root1, "items").addAll(List.of(a1, b1, a2));
    a1.eSet(feature(a1, "next"), b1);
    list(b1, "refs").addAll(List.of(a1, a2));
    EObject o1 = create("Other");
    o1.eSet(feature(o1, "label"), "x");
    o1.eSet(feature(o1, "amount"), 1);
    EObject o2 = create("Other");
    o2.eSet(feature(o2, "label"), "y");
    o2.eSet(feature(o2, "amount"), 2);
    list(root1, "others").addAll(List.of(o1, o2));
    r1.getContents().add(root1);

    EObject root2 = create("Root");
    list(root2, "items").add(item("SubB", "b2", 9));
    r2.getContents().add(root2);

    model = LiveModel.of(rs, List.of(r1, r2), new FakeCorrespondenceModel());
  }

  /** A fresh copy of the initial model. */
  public static EdgeFixture create() {
    return new EdgeFixture();
  }

  public LiveModel model() {
    return model;
  }

  /** The root of {@code edgeN.edge} (1-based). */
  public EObject root(int n) {
    return model.modelResources().get(n - 1).getContents().get(0);
  }

  /** The object (item or other) whose {@code name} or {@code label} attribute equals {@code key}. */
  public EObject find(String key) {
    for (EObject o : model.allObjects()) {
      for (String f : List.of("name", "label")) {
        if (o.eClass().getEStructuralFeature(f) != null && key.equals(o.eGet(feature(o, f)))) {
          return o;
        }
      }
    }
    throw new IllegalArgumentException("no object " + key);
  }

  public static EObject create(String className) {
    return EcoreUtil.create((EClass) PACKAGE.getEClassifier(className));
  }

  public static EObject item(String className, String name, int value) {
    EObject o = create(className);
    o.eSet(feature(o, "name"), name);
    o.eSet(feature(o, "value"), value);
    return o;
  }

  public static org.eclipse.emf.ecore.EStructuralFeature feature(EObject o, String name) {
    return o.eClass().getEStructuralFeature(name);
  }

  @SuppressWarnings("unchecked")
  public static List<Object> list(EObject o, String name) {
    return (List<Object>) o.eGet(feature(o, name));
  }

  private static EPackage createPackage() {
    EcoreFactory f = EcoreFactory.eINSTANCE;
    EPackage pkg = f.createEPackage();
    pkg.setName("edge");
    pkg.setNsPrefix("edge");
    pkg.setNsURI("http://vitruvocl.test/incr/edge");

    EClass item = eClass(pkg, "Item");
    item.setAbstract(true);
    EClass subA = eClass(pkg, "SubA");
    subA.getESuperTypes().add(item);
    EClass subB = eClass(pkg, "SubB");
    subB.getESuperTypes().add(item);
    EClass other = eClass(pkg, "Other");
    EClass root = eClass(pkg, "Root");

    attribute(item, "name", EcorePackage.Literals.ESTRING, false, true);
    attribute(item, "value", EcorePackage.Literals.EINT, false, true);
    reference(item, "next", item, false, false, true);
    reference(item, "refs", item, false, true, false);
    attribute(subA, "extra", EcorePackage.Literals.EINT, false, true);
    attribute(subB, "label", EcorePackage.Literals.ESTRING, false, true);
    attribute(other, "label", EcorePackage.Literals.ESTRING, false, true);
    attribute(other, "amount", EcorePackage.Literals.EINT, false, true);
    EReference items = reference(root, "items", item, true, true, true);
    EReference owner = reference(item, "owner", root, false, false, true);
    owner.setEOpposite(items);
    items.setEOpposite(owner);
    owner.setTransient(true);
    EAttribute cache = EcoreFactory.eINSTANCE.createEAttribute();
    cache.setName("cache");
    cache.setEType(EcorePackage.Literals.EINT);
    cache.setTransient(true);
    item.getEStructuralFeatures().add(cache);
    reference(root, "others", other, true, true, true);
    attribute(root, "tags", EcorePackage.Literals.ESTRING, true, false);
    return pkg;
  }

  private static EClass eClass(EPackage pkg, String name) {
    EClass c = EcoreFactory.eINSTANCE.createEClass();
    c.setName(name);
    pkg.getEClassifiers().add(c);
    return c;
  }

  private static void attribute(EClass owner, String name, EDataType type, boolean many, boolean unique) {
    EAttribute a = EcoreFactory.eINSTANCE.createEAttribute();
    a.setName(name);
    a.setEType(type);
    a.setUpperBound(many ? -1 : 1);
    a.setUnique(unique);
    owner.getEStructuralFeatures().add(a);
  }

  private static EReference reference(
      EClass owner, String name, EClass type, boolean containment, boolean many, boolean unique) {
    EReference r = EcoreFactory.eINSTANCE.createEReference();
    r.setName(name);
    r.setEType(type);
    r.setContainment(containment);
    r.setUpperBound(many ? -1 : 1);
    r.setUnique(unique);
    owner.getEStructuralFeatures().add(r);
    return r;
  }
}
