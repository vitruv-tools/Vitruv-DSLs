package tools.vitruv.applications.demo.insurancefamilies.tests.util

import edu.kit.ipd.sdq.activextendannotations.Utility
import tools.vitruv.framework.views.View
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister

import static extension edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.claimOne
import edu.kit.ipd.sdq.metamodels.families.Family

@Utility
class FamiliesQueryUtil {
	
	static def FamilyRegister claimFamilyRegister(View view) {
		view.getRootObjects(FamilyRegister).claimOne
	}
	
	static def Family claimFamily(FamilyRegister register, String lastName) {
		register.families.filter[it.lastName == lastName].claimOne
	}
}
