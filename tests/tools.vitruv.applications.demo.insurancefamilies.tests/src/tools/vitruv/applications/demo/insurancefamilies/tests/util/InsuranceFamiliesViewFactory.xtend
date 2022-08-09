package tools.vitruv.applications.demo.insurancefamilies.tests.util

import tools.vitruv.testutils.TestViewFactory
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import tools.vitruv.framework.views.View
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister

@FinalFieldsConstructor
class InsuranceFamiliesViewFactory extends TestViewFactory {
	
	private def View createInsuranceView(){
		createViewOfElements("insurance", #{InsuranceDatabase})
	}
	
	private def View createFamilyView(){
		createViewOfElements("families", #{FamilyRegister})
	}
	
	def void changeInsuranceView((View)=> void modelModification){
		changeViewRecordingChanges(createInsuranceView, modelModification)
	}
	
	def void changeFamilyRegisterView((View)=> void modelModification){
		changeViewRecordingChanges(createFamilyView, modelModification)
	}
	
	def void validateInsuranceView((View)=>void viewValidation){
		validateView(createInsuranceView, viewValidation)
	}
	
	def void validateFamilyView((View)=>void viewValidation){
		validateView(createFamilyView, viewValidation)
	}
}
