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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.resource.Resource;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.CorrespondenceModelView;
import tools.vitruv.framework.vsum.constraints.ConstraintCheckContext;

/**
 * A check context without models and correspondences, used to type-check constraints against the
 * registered metamodels without evaluating them on a virtual model.
 */
final class EmptyCheckContext implements ConstraintCheckContext {
  static final EmptyCheckContext INSTANCE = new EmptyCheckContext();

  private static final CorrespondenceModelView<Correspondence> NO_CORRESPONDENCES =
      emptyCorrespondences();

  private EmptyCheckContext() {}

  @Override
  public Collection<Resource> getModelResources() {
    return List.of();
  }

  @Override
  public CorrespondenceModelView<Correspondence> getCorrespondenceModel() {
    return NO_CORRESPONDENCES;
  }

  /**
   * Creates a correspondence model without correspondences. It is a proxy answering every query
   * with an empty result, so it does not depend on the methods of the Vitruv-Change version.
   */
  @SuppressWarnings("unchecked")
  private static CorrespondenceModelView<Correspondence> emptyCorrespondences() {
    InvocationHandler handler =
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
              return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                default -> "EmptyCorrespondences";
              };
            }
            return emptyValue(method.getReturnType(), proxy);
          }
        };
    return (CorrespondenceModelView<Correspondence>)
        Proxy.newProxyInstance(
            EmptyCheckContext.class.getClassLoader(),
            new Class<?>[] {CorrespondenceModelView.class},
            handler);
  }

  private static Object emptyValue(Class<?> type, Object proxy) {
    if (type == boolean.class) {
      return false;
    }
    if (Set.class.isAssignableFrom(type)) {
      return Set.of();
    }
    if (Map.class.isAssignableFrom(type)) {
      return Map.of();
    }
    if (Collection.class.isAssignableFrom(type)) {
      return List.of();
    }
    if (CorrespondenceModelView.class.isAssignableFrom(type)) {
      return proxy;
    }
    return null;
  }
}
