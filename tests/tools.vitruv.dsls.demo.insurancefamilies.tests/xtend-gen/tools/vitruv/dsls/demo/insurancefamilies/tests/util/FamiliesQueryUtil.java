package tools.vitruv.dsls.demo.insurancefamilies.tests.util;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import java.util.Collection;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.testutils.TestModel;

@Utility
@SuppressWarnings("all")
public final class FamiliesQueryUtil {
  public static FamilyRegister claimFamilyRegister(final TestModel<FamilyRegister> testModel) {
    return IterableUtil.<Collection<FamilyRegister>, FamilyRegister>claimOne(testModel.getTypedRootObjects());
  }

  public static EList<Family> claimFamilies(final TestModel<FamilyRegister> testModel) {
    return IterableUtil.<Collection<FamilyRegister>, FamilyRegister>claimOne(testModel.getTypedRootObjects()).getFamilies();
  }

  public static Family claimFamily(final FamilyRegister register, final String lastName) {
    final Function1<Family, Boolean> _function = (Family it) -> {
      String _lastName = it.getLastName();
      return Boolean.valueOf(Objects.equal(_lastName, lastName));
    };
    return IterableUtil.<Iterable<Family>, Family>claimOne(IterableExtensions.<Family>filter(register.getFamilies(), _function));
  }

  private FamiliesQueryUtil() {
    
  }
}
