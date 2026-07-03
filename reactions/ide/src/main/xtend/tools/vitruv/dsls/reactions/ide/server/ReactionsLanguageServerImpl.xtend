package tools.vitruv.dsls.reactions.ide.server

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.xtext.ide.server.LanguageServerImpl

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
 */
class ReactionsLanguageServerImpl extends LanguageServerImpl {

	override protected ServerCapabilities createServerCapabilities(InitializeParams params) {
		val capabilities = super.createServerCapabilities(params)
		capabilities.completionProvider.triggerCharacters = #[".", " ", ":"]
		return capabilities
	}

}
