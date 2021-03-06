package tools.vitruv.dsls.commonalities.language.elements

import tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsFactoryImpl

class LanguageElementsAdapterFactory extends LanguageElementsFactoryImpl {

	override createMostSpecificType() {
		new MostSpecificTypeI
	}

	override createLeastSpecificType() {
		new LeastSpecificTypeI
	}

	override createMetamodel() {
		new MetamodelAdapter
	}

	override createEFeatureAttribute() {
		new EFeatureAdapter
	}

	override createEClassMetaclass() {
		new EClassAdapter
	}

	override createResourceMetaclass() {
		new ResourceMetaclassI
	}

	override createEDataTypeClassifier() {
		new EDataTypeAdapter
	}
}
