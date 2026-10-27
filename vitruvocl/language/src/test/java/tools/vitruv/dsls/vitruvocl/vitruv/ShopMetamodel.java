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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/** A minimal dynamic metamodel {@code shop} with a single class {@code Item(name, price)}. */
final class ShopMetamodel {
  static final String NS_URI = "http://vitruv.tools/vitruvocl/test/shop";

  final EPackage ePackage;
  final EClass item;
  final EAttribute name;
  final EAttribute price;

  ShopMetamodel() {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    ePackage = factory.createEPackage();
    ePackage.setName("shop");
    ePackage.setNsPrefix("shop");
    ePackage.setNsURI(NS_URI);

    item = factory.createEClass();
    item.setName("Item");
    name = factory.createEAttribute();
    name.setName("name");
    name.setEType(EcorePackage.Literals.ESTRING);
    price = factory.createEAttribute();
    price.setName("price");
    price.setEType(EcorePackage.Literals.EINT);
    item.getEStructuralFeatures().add(name);
    item.getEStructuralFeatures().add(price);
    ePackage.getEClassifiers().add(item);
  }

  void register() {
    EPackage.Registry.INSTANCE.put(NS_URI, ePackage);
  }

  void unregister() {
    EPackage.Registry.INSTANCE.remove(NS_URI);
  }

  EObject createItem(String itemName, int itemPrice) {
    EObject result = EcoreUtil.create(item);
    result.eSet(name, itemName);
    result.eSet(price, itemPrice);
    return result;
  }

  /** Creates an item whose name cannot be read, so evaluating constraints on it fails. */
  EObject createBrokenItem(int itemPrice) {
    EObject result =
        new DynamicEObjectImpl(item) {
          @Override
          public Object eGet(EStructuralFeature feature, boolean resolve) {
            if (feature == name) {
              throw new IllegalStateException("name is not readable");
            }
            return super.eGet(feature, resolve);
          }
        };
    result.eSet(price, itemPrice);
    return result;
  }
}
