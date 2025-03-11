package tools.vitruv.dsls.commonalities.generator.changepropagationspecification;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@Utility
@SuppressWarnings("all")
public final class ChangePropagationSpecificationConstants {
  private static final String CHANGE_PROPAGATION_PACKAGE_NAME = "tools.vitruv.commonalities";

  private static final String CHANGE_PROPAGATION_PROVIDER_NAME = "CommonalitiesChangePropagationSpecificationProvider";

  @Pure
  public static String getChangePropagationSpecificationName(final EPackage sourceMetamodel, final EPackage targetMetamodel) {
    String _firstUpper = StringExtensions.toFirstUpper(sourceMetamodel.getName());
    String _plus = (_firstUpper + "To");
    String _firstUpper_1 = StringExtensions.toFirstUpper(targetMetamodel.getName());
    String _plus_1 = (_plus + _firstUpper_1);
    return (_plus_1 + "ChangePropagationSpecification");
  }

  @Pure
  public static String getChangePropagationSpecificationProviderName() {
    return ChangePropagationSpecificationConstants.CHANGE_PROPAGATION_PROVIDER_NAME;
  }

  @Pure
  public static String getChangePropagationSpecificationPackageName() {
    return ChangePropagationSpecificationConstants.CHANGE_PROPAGATION_PACKAGE_NAME;
  }

  private ChangePropagationSpecificationConstants() {
    
  }
}
