package tools.vitruv.dsls.reactions.tests.annotationTests

import java.util.Optional
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.Data
import tools.vitruv.change.interaction.UserInteractor

@Data
class Author {
	String name
}

@Accessors
final class AnnotationTestsMonitor {
	public static val instance = new AnnotationTestsMonitor

	var Optional<Author> authorInReaction
	var Optional<Author> authorInRoutine
	var UserInteractor userInteractorInRoutine

	def reset() {
		authorInReaction = null
		authorInRoutine = null
		userInteractorInRoutine = null
	}
}
