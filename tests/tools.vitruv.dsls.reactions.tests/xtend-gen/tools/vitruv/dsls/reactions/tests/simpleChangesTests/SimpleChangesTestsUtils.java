package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import org.eclipse.emf.ecore.EObject;

@SuppressWarnings("all")
public class SimpleChangesTestsUtils {
  public static EObject findTypeInContainmentHierarchy(final EObject startElement, final Class<? extends EObject> searchedContainerType) {
    EObject currentObject = startElement;
    while (((!searchedContainerType.isInstance(currentObject)) && (currentObject != null))) {
      currentObject = currentObject.eContainer();
    }
    return currentObject;
  }
}
