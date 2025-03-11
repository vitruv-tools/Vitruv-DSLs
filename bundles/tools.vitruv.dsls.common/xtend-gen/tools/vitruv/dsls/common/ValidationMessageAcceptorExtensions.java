package tools.vitruv.dsls.common;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

/**
 * Copies some of the protected helpers from AbstractDeclarativeValidator to make it easier to split validators into multiple
 * classes.
 */
@Utility
@SuppressWarnings("all")
public final class ValidationMessageAcceptorExtensions {
  public static void info(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature) {
    ValidationMessageAcceptorExtensions.info(acceptor, message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
  }

  public static void info(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index) {
    ValidationMessageAcceptorExtensions.info(acceptor, message, source, feature, index, null);
  }

  public static void info(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index, final String code, final String... issueData) {
    acceptor.acceptInfo(message, source, feature, index, code, issueData);
  }

  public static void warning(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature) {
    ValidationMessageAcceptorExtensions.warning(acceptor, message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
  }

  public static void warning(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index) {
    ValidationMessageAcceptorExtensions.warning(acceptor, message, source, feature, index, null);
  }

  public static void warning(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index, final String code, final String... issueData) {
    acceptor.acceptWarning(message, source, feature, index, code, issueData);
  }

  public static void warning(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final String code, final String... issueData) {
    acceptor.acceptWarning(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
  }

  public static void error(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature) {
    ValidationMessageAcceptorExtensions.error(acceptor, message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
  }

  public static void error(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index) {
    ValidationMessageAcceptorExtensions.error(acceptor, message, source, feature, index, null);
  }

  public static void error(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final int index, final String code, final String... issueData) {
    acceptor.acceptWarning(message, source, feature, index, code, issueData);
  }

  public static void error(final ValidationMessageAcceptor acceptor, final String message, final EObject source, final EStructuralFeature feature, final String code, final String... issueData) {
    acceptor.acceptError(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
  }

  private ValidationMessageAcceptorExtensions() {
    
  }
}
