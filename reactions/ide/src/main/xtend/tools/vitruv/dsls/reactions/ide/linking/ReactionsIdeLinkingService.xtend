package tools.vitruv.dsls.reactions.ide.linking

import com.google.inject.Inject
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.xtext.linking.impl.IllegalNodeException
import org.eclipse.xtext.nodemodel.INode
import tools.vitruv.dsls.reactions.ide.workspace.ReactionsClasspathRegistrar
import tools.vitruv.dsls.reactions.ide.workspace.ReactionsEcoreWorkspaceRegistrar
import tools.vitruv.dsls.reactions.linking.ReactionsLinkingService

/**
 * Extends {@link ReactionsLinkingService} for the language server: whenever a metamodel import is
 * linked, (re-)scans the workspace project containing the file being linked for {@code .ecore}
 * files and {@code target/classes} directories (see {@link ReactionsEcoreWorkspaceRegistrar} and
 * {@link ReactionsClasspathRegistrar}) before delegating to the default resolution.
 * <p>
 * This runs unconditionally rather than only when resolution first fails: once an
 * {@code EPackage} is registered, {@code EPackage.Registry.INSTANCE.getEPackage(nsUri)} keeps
 * succeeding for the rest of the language server's lifetime, so a failure-only trigger would never
 * fire again even after the user builds a model project whose Java classes were previously
 * missing (only the {@code .ecore} file, not compiled code, is needed to register the package
 * itself). Each registrar debounces its own scanning per project root, so this stays cheap.
 */
class ReactionsIdeLinkingService extends ReactionsLinkingService {

	@Inject ReactionsEcoreWorkspaceRegistrar ecoreWorkspaceRegistrar
	@Inject ReactionsClasspathRegistrar classpathRegistrar

	override List<EObject> getLinkedObjects(EObject context, EReference ref, INode node) throws IllegalNodeException {
		if (ref.getEType() != EcorePackage.Literals.EPACKAGE) {
			return super.getLinkedObjects(context, ref, node)
		}
		val resourceUri = context.eResource?.URI
		ecoreWorkspaceRegistrar.ensureRegisteredNear(resourceUri)
		classpathRegistrar.ensureRegisteredNear(resourceUri)
		return super.getLinkedObjects(context, ref, node)
	}

}
