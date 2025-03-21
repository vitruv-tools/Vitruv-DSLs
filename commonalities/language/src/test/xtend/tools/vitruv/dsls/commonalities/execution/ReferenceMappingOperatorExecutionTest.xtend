package tools.vitruv.dsls.commonalities.tests.execution

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.DisplayName
import tools.vitruv.dsls.commonalities.tests.util.TestCommonalitiesGenerator
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeAll
import tools.vitruv.change.testutils.TestProject
import java.nio.file.Path
import org.junit.jupiter.api.Test

@ExtendWith(InjectionExtension)
@InjectWith(CommonalitiesLanguageInjectorProvider)
@TestInstance(PER_CLASS)
@DisplayName('executing a commonality with attribute mapping operators')
class ReferenceMappingOperatorExecutionTest extends CommonalitiesExecutionTest {
	@Inject TestCommonalitiesGenerator generator
	
	@BeforeAll
	def void generate(@TestProject(variant = "commonalities") Path testProject) {
		generator.generate(testProject,
			'WithReferenceMappingOperators.commonality' -> '''
				import tools.vitruv.dsls.commonalities.tests.operators.mock
				
				import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as AllElementTypes
				import "http://tools.vitruv.change.testutils.metamodels.allElementTypes2" as AllElementTypes2
				
				concept operators
				
				commonality WithReferenceMappingOperators {
					with AllElementTypes:(Root in Resource)
					with AllElementTypes2:(Root2 in Resource)
				
					has self referencing operators:WithReferenceMappingOperators {
						= AllElementTypes:Root.mock(ref Root.singleValuedEAttribute)
					}
				}
			'''
		)
	}
	
	override protected getChangePropagationSpecifications() {
		generator.createChangePropagationSpecifications()
	}

	@Test
	@DisplayName('generates')
	def void generates() {
		// TODO validate something if the ref keyword stays, else remove this test
	}
}