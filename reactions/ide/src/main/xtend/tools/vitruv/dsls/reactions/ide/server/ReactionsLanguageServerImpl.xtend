package tools.vitruv.dsls.reactions.ide.server

import com.google.inject.Inject
import com.google.inject.Provider
import java.util.ArrayList
import java.util.List
import java.util.concurrent.CompletableFuture
import org.eclipse.emf.common.util.URI
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.CodeLensOptions
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.xtext.findReferences.IReferenceFinder
import org.eclipse.xtext.findReferences.ReferenceAcceptor
import org.eclipse.xtext.findReferences.TargetURICollector
import org.eclipse.xtext.findReferences.TargetURIs
import org.eclipse.xtext.ide.server.DocumentExtensions
import org.eclipse.xtext.ide.server.LanguageServerImpl
import org.eclipse.xtext.resource.ILocationInFileProvider
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.IResourceDescriptions
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.service.OperationCanceledManager
import org.eclipse.xtext.util.CancelIndicator
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine

/**
 * Widens the completion trigger characters that {@link LanguageServerImpl} advertises to the
 * client, which by default is only {@code "."}. Since the Reactions grammar (and most other
 * Xtext grammars) separates keywords by whitespace rather than punctuation (e.g. {@code 'in'
 * 'reaction' 'to' 'changes' 'in'}), VS Code otherwise only asks the server for completions while
 * a word is being typed, never right after a space or a newline, so proposals for the next
 * expected keyword never show up automatically the way they do in the Eclipse-based Xtext editor.
 * {@code ":"} is needed the same way for metaclass references (e.g. {@code model::System}), whose
 * grammar rule is {@code (metamodel=[MetamodelImport] '::')? metaclass=[EClassifier|QualifiedName]}
 * — VS Code never re-queried after the second {@code ':'} of {@code '::'} without it.
 * <p>
 * Also adds a "N callers" code lens above every routine declaration, since routines have no
 * classic call stack (they can be called from multiple reactions, and from other routines) and
 * jumping to who calls a given routine otherwise requires a manual "Find References". Since a
 * routine's caller count can only be computed correctly once the whole workspace has been
 * indexed (a codelens requested right after opening a file may be computed before cross-file
 * references are known yet, and VS Code never re-asks for it on its own), a build listener tells
 * the client to refresh code lenses after every (re-)build.
 */
class ReactionsLanguageServerImpl extends LanguageServerImpl {

	@Inject IReferenceFinder referenceFinder
	@Inject Provider<TargetURIs> targetURIProvider
	@Inject TargetURICollector targetURICollector
	@Inject IResourceServiceProvider.Registry resourceServiceProviderRegistry
	@Inject ILocationInFileProvider locationInFileProvider
	@Inject DocumentExtensions documentExtensions
	@Inject OperationCanceledManager operationCanceledManager

	override CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		getLanguageServerAccess().addBuildListener[ deltas |
			getLanguageServerAccess().languageClient.refreshCodeLenses()
		]
		return super.initialize(params)
	}

	override protected ServerCapabilities createServerCapabilities(InitializeParams params) {
		val capabilities = super.createServerCapabilities(params)
		capabilities.completionProvider.triggerCharacters = #[".", " ", ":"]
		capabilities.codeLensProvider = new CodeLensOptions(false)
		return capabilities
	}

	override protected List<? extends CodeLens> codeLens(CodeLensParams params, CancelIndicator cancelIndicator) {
		val uri = getURI(params.textDocument)
		return getWorkspaceManager().doRead(uri) [ document, resource |
			resource.allContents.filter(Routine).map[toCallerCodeLens(resource, cancelIndicator)].toList
		]
	}

	def private CodeLens toCallerCodeLens(Routine routine, XtextResource resource, CancelIndicator cancelIndicator) {
		val callers = routine.findCallers(cancelIndicator)
		val region = locationInFileProvider.getSignificantTextRegion(routine)
		val range = documentExtensions.newRange(resource, region)
		val title = switch (callers.size) {
			case 0: "no callers"
			case 1: "1 caller"
			default: '''«callers.size» callers'''
		}
		val codeLens = new CodeLens(range)
		codeLens.command = new Command(title, "reactions.showReferences",
			#[resource.URI.toString, range.start, callers])
		return codeLens
	}

	def private List<Location> findCallers(Routine routine, CancelIndicator cancelIndicator) {
		val targetURIs = targetURIProvider.get()
		targetURICollector.add(routine, targetURIs)
		val locations = new ArrayList<Location>
		val resourceAccess = getWorkspaceResourceAccess()
		referenceFinder.findAllReferences(targetURIs, resourceAccess, getWorkspaceManager().index,
			new ReferenceAcceptor(resourceServiceProviderRegistry, [ IReferenceDescription reference |
				operationCanceledManager.checkCanceled(cancelIndicator)
				resourceAccess.readOnly(reference.sourceEObjectUri.trimFragment) [ resourceSet |
					val sourceObject = resourceSet.getEObject(reference.sourceEObjectUri, true)
					if (sourceObject !== null) {
						val location = documentExtensions.newLocation(sourceObject, reference.getEReference(),
							reference.getIndexInList())
						if (location !== null) {
							locations.add(location)
						}
					}
					return null
				]
			]), null)
		return locations
	}

}
