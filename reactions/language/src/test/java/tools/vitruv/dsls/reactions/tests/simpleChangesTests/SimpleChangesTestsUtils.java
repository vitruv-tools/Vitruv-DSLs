package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import org.eclipse.emf.ecore.EObject;

public final class SimpleChangesTestsUtils {
  private SimpleChangesTestsUtils() {}

  public static EObject findTypeInContainmentHierarchy(EObject startElement,
                                                       Class<? extends EObject> searchedContainerType) {
    EObject currentObject = startElement;
    while (!searchedContainerType.isInstance(currentObject)
        && currentObject != null) {
      currentObject = currentObject.eContainer();
    }
    return currentObject;
  }
}
