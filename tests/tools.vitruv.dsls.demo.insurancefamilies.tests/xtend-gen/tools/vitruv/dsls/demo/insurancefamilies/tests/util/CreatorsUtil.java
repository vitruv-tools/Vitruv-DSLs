package tools.vitruv.dsls.demo.insurancefamilies.tests.util;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@Utility
@SuppressWarnings("all")
public final class CreatorsUtil {
  public static Family createFamily(final Procedure1<? super Family> familyInitalization) {
    Family family = FamiliesFactory.eINSTANCE.createFamily();
    familyInitalization.apply(family);
    return family;
  }

  public static Member createFamilyMember(final Procedure1<? super Member> familyMemberInitalization) {
    Member member = FamiliesFactory.eINSTANCE.createMember();
    familyMemberInitalization.apply(member);
    return member;
  }

  public static InsuranceDatabase createInsuranceDatabase(final Procedure1<? super InsuranceDatabase> insuranceDatabaseInitialization) {
    InsuranceDatabase insuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    insuranceDatabaseInitialization.apply(insuranceDatabase);
    return insuranceDatabase;
  }

  public static InsuranceClient createInsuranceClient(final Procedure1<? super InsuranceClient> insuranceClientInitialization) {
    InsuranceClient insuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
    insuranceClientInitialization.apply(insuranceClient);
    return insuranceClient;
  }

  private CreatorsUtil() {
    
  }
}
