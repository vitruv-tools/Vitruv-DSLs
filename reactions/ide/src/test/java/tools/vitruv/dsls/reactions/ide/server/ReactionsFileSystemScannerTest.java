package tools.vitruv.dsls.reactions.ide.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.inject.Guice;

/**
 * Verifies that {@link ReactionsFileSystemScanner} skips build-output directories such as
 * {@code target}, which otherwise cause the language server to index a Maven-copied duplicate of
 * a {@code .reactions} file alongside its original, source, resulting in spurious
 * "already defined" duplicate-type errors.
 */
class ReactionsFileSystemScannerTest {

	@Test
	void skipsBuildOutputDirectories(@TempDir Path projectRoot) throws IOException {
		Path sourceFile = Files.createDirectories(projectRoot.resolve("reactions")).resolve("test.reactions");
		Files.writeString(sourceFile, "content");
		Path targetCopy = Files.createDirectories(projectRoot.resolve("target/classes/reactions"))
				.resolve("test.reactions");
		Files.writeString(targetCopy, "content");

		ReactionsFileSystemScanner scanner = Guice.createInjector().getInstance(ReactionsFileSystemScanner.class);

		List<URI> visited = new ArrayList<>();
		scanner.scan(URI.createFileURI(projectRoot.toString()), visited::add);

		List<String> visitedLastTwoSegments = visited.stream().filter(uri -> uri.segmentCount() >= 2)
				.map(uri -> uri.segment(uri.segmentCount() - 2) + "/" + uri.lastSegment()).toList();

		assertTrue(visitedLastTwoSegments.contains("reactions/test.reactions"),
				() -> "Expected source file to be visited, got: " + visitedLastTwoSegments);
		assertFalse(visitedLastTwoSegments.contains("classes/test.reactions"),
				() -> "Expected target/ copy to be skipped, got: " + visitedLastTwoSegments);
	}

}
