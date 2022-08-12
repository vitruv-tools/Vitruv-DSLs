package tools.vitruv.dsls.commonalities.tests.execution

import tools.vitruv.testutils.TestProjectManager
import tools.vitruv.testutils.TestLogging
import org.junit.jupiter.api.^extension.ExtendWith
import tools.vitruv.testutils.views.TestView
import org.eclipse.xtend.lib.annotations.Delegate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import tools.vitruv.testutils.TestProject
import java.nio.file.Path
import static tools.vitruv.testutils.views.ChangePublishingTestView.createDefaultChangePublishingTestView
import tools.vitruv.change.propagation.ChangePropagationSpecification

@ExtendWith(#[TestLogging, TestProjectManager])
abstract class CommonalitiesExecutionTest implements TestView {
	@Delegate var TestView testView
	
	protected def Iterable<ChangePropagationSpecification> getChangePropagationSpecifications();
	
	@BeforeEach
	def void prepareTestView(@TestProject Path testProjectPath) {
		testView = createDefaultChangePublishingTestView(testProjectPath, getChangePropagationSpecifications())
	}

	@AfterEach
	def closeTestView() {
		testView.close()
	}
	
}