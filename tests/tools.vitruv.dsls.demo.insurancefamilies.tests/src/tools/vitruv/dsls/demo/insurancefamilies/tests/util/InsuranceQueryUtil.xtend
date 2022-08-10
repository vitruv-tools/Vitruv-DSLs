package tools.vitruv.dsls.demo.insurancefamilies.tests.util

import edu.kit.ipd.sdq.activextendannotations.Utility
import tools.vitruv.framework.views.View

import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.claimOne
//import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase

@Utility
class InsuranceQueryUtil {
	
	static def claimInsuranceDatabase(View view){
		view.getRootObjects(InsuranceDatabase).claimOne
	}
	
	static def claimInsuranceClient(InsuranceDatabase insuranceDatabase, String firstName, String lastName){
		insuranceDatabase.insuranceclient.filter[name == firstName + " " + lastName].claimOne
	}
}