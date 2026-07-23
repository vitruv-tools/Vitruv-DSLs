package tools.vitruv.dsls.reactions.util

import com.google.inject.Inject
import com.google.inject.Provider
import java.util.concurrent.atomic.AtomicBoolean
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.findReferences.IReferenceFinder
import org.eclipse.xtext.findReferences.ReferenceAcceptor
import org.eclipse.xtext.findReferences.TargetURICollector
import org.eclipse.xtext.findReferences.TargetURIs
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.util.concurrent.IUnitOfWork
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine

/**
 * Checks whether a routine has any call sites anywhere in the workspace, by searching the Xtext
 * index for cross-references to the routine's associated Java method (generated on its routines
 * facade) — the same mechanism that lets "Find References" locate the callers of an Xtend
 * function, since {@code Routine#associatePrimary} links a routine to that generated method.
 * <p>
 * Used both by the "N callers" code lens in the {@code ide} module and by
 * {@link tools.vitruv.dsls.reactions.validation.ReactionsLanguageValidator}'s orphaned-routine
 * warning.
 * <p>
 * Deliberately goes through {@link ResourceDescriptionsProvider} rather than injecting
 * {@code IResourceDescriptions} directly: the latter's default binding
 * ({@code ResourceSetBasedResourceDescriptions}) needs a resource set wired up for it separately
 * and throws a {@code NullPointerException} when used as-is inside the language server, whereas
 * {@code ResourceDescriptionsProvider} finds the workspace-wide {@code ChunkedResourceDescriptions}
 * already attached to the routine's own resource set by the server's workspace manager.
 * <p>
 * Also passes a resource access backed by the routine's own resource set (not {@code null}):
 * without it, a reference from the very same (or another not-yet-indexed) resource is invisible,
 * since the index alone only reflects previously-built cross-resource references.
 */
class RoutineCallerFinder {

	@Inject IReferenceFinder referenceFinder
	@Inject Provider<TargetURIs> targetURIProvider
	@Inject TargetURICollector targetURICollector
	@Inject IResourceServiceProvider.Registry resourceServiceProviderRegistry
	@Inject ResourceDescriptionsProvider resourceDescriptionsProvider

	def boolean hasCallers(Routine routine) {
		val targetURIs = targetURIProvider.get()
		targetURICollector.add(routine, targetURIs)
		val index = resourceDescriptionsProvider.getResourceDescriptions(routine.eResource)
		val resourceSet = routine.eResource.resourceSet
		val resourceAccess = new IReferenceFinder.IResourceAccess {
			override <R> R readOnly(URI targetURI, IUnitOfWork<R, ResourceSet> work) {
				work.exec(resourceSet)
			}
		}
		val found = new AtomicBoolean(false)
		referenceFinder.findAllReferences(targetURIs, resourceAccess, index,
			new ReferenceAcceptor(resourceServiceProviderRegistry, [ IReferenceDescription reference | found.set(true) ]), null)
		return found.get
	}

}
