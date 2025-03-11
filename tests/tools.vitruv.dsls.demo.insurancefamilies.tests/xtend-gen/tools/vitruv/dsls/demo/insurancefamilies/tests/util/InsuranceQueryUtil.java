package tools.vitruv.dsls.demo.insurancefamilies.tests.util;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.Collection;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.testutils.TestModel;

@Utility
@SuppressWarnings("all")
public final class InsuranceQueryUtil {
  public static InsuranceDatabase claimInsuranceDatabase(final TestModel<InsuranceDatabase> model) {
    return IterableUtil.<Collection<InsuranceDatabase>, InsuranceDatabase>claimOne(model.getTypedRootObjects());
  }

  public static InsuranceClient claimInsuranceClient(final InsuranceDatabase insuranceDatabase, final String firstName, final String lastName) {
    final Function1<InsuranceClient, Boolean> _function = (InsuranceClient it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, ((firstName + " ") + lastName)));
    };
    return IterableUtil.<Iterable<InsuranceClient>, InsuranceClient>claimOne(IterableExtensions.<InsuranceClient>filter(insuranceDatabase.getInsuranceclient(), _function));
  }

  private InsuranceQueryUtil() {
    
  }
}
