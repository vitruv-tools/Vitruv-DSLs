package tools.vitruv.dsls.common;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import edu.kit.ipd.sdq.activextendannotations.Utility;

/** Extensions for {@link ValidationMessageAcceptor}. */
@Utility
public final class ValidationMessageAcceptorExtensions {
  private ValidationMessageAcceptorExtensions() {
    // utility
  }

  /**
   * Sends an info message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   */
  public static void info(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature) {
    acceptor.acceptInfo(
        message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
  }

  /**
   * Sends an info message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   * @param index    the index
   */
  public static void info(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index) {
    acceptor.acceptInfo(message, source, feature, index, null);
  }

  /**
   * Sends an info message to the acceptor.
   *
   * @param acceptor  the acceptor
   * @param message   the message
   * @param source    the source
   * @param feature   the feature
   * @param index     the index
   * @param code      the code
   * @param issueData the issue data
   */
  public static void info(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index,
      String code,
      String... issueData) {
    acceptor.acceptInfo(message, source, feature, index, code, issueData);
  }

  /**
   * Sends an warning message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   */
  public static void warning(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature) {
    acceptor.acceptWarning(
        message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
  }

  /**
   * Sends a warning message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   * @param index    the index
   */
  public static void warning(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index) {
    acceptor.acceptWarning(message, source, feature, index, null);
  }

  /**
   * Sends a warning message to the acceptor.
   *
   * @param acceptor  the acceptor
   * @param message   the message
   * @param source    the source
   * @param feature   the feature
   * @param index     the index
   * @param code      the code
   * @param issueData the issue data
   */
  public static void warning(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index,
      String code,
      String... issueData) {
    acceptor.acceptWarning(message, source, feature, index, code, issueData);
  }

  /**
   * Sends a warning message to the acceptor.
   *
   * @param acceptor  the acceptor
   * @param message   the message
   * @param source    the source
   * @param feature   the feature
   * @param code      the code
   * @param issueData the issue data
   */
  public static void warning(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      String code,
      String... issueData) {
    acceptor.acceptWarning(
        message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
  }

  /**
   * Sends an error message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   */
  public static void error(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature) {
    acceptor.acceptError(
        message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, null);
  }

  /**
   * Sends an error message to the acceptor.
   *
   * @param acceptor the acceptor
   * @param message  the message
   * @param source   the source
   * @param feature  the feature
   * @param index    the index
   */
  public static void error(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index) {
    acceptor.acceptError(message, source, feature, index, null);
  }

  /**
   * Sends an error message to the acceptor.
   *
   * @param acceptor  the acceptor
   * @param message   the message
   * @param source    the source
   * @param feature   the feature
   * @param index     the index
   * @param code      the code
   * @param issueData the issue data
   */
  public static void error(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      int index,
      String code,
      String... issueData) {
    acceptor.acceptError(message, source, feature, index, code, issueData);
  }

  /**
   * Sends an error message to the acceptor.
   *
   * @param acceptor  the acceptor
   * @param message   the message
   * @param source    the source
   * @param feature   the feature
   * @param code      the code
   * @param issueData the issue data
   */
  public static void error(
      ValidationMessageAcceptor acceptor,
      String message,
      EObject source,
      EStructuralFeature feature,
      String code,
      String... issueData) {
    acceptor.acceptError(
        message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
  }
}
