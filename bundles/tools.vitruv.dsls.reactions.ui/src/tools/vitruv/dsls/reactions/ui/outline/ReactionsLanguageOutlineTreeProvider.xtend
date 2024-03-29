/*
 * generated by Xtext 2.9.0
 */
package tools.vitruv.dsls.reactions.ui.outline

import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider
import org.eclipse.xtext.ui.editor.outline.impl.DocumentRootNode
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage
import tools.vitruv.dsls.common.elements.MetamodelImport
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction
import static extension tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil.*
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport
import tools.vitruv.dsls.reactions.language.ModelElementChange
import tools.vitruv.dsls.reactions.language.ModelAttributeChange
import tools.vitruv.dsls.reactions.language.ArbitraryModelChange
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall

/**
 * Outline structure definition for a reactions file.
 * 
 * @author Heiko Klare
 */
class ReactionsLanguageOutlineTreeProvider extends DefaultOutlineTreeProvider {
	protected def void _createChildren(DocumentRootNode root, ReactionsFile reactionsFile) {
		createEStructuralFeatureNode(root, reactionsFile,
			TopLevelElementsPackage.Literals.REACTIONS_FILE__METAMODEL_IMPORTS, imageDispatcher.invoke(reactionsFile),
			"imports", false);
		reactionsFile.reactionsSegments.forEach[createChildren(root, it)]
	}

	protected def void _createChildren(DocumentRootNode parentNode, ReactionsSegment reactionsSegment) {
		val segmentNode = createEObjectNode(parentNode, reactionsSegment);
		createEStructuralFeatureNode(segmentNode, reactionsSegment,
			TopLevelElementsPackage.Literals.REACTIONS_SEGMENT__REACTIONS_IMPORTS,
			imageDispatcher.invoke(reactionsSegment), "imported segments", false)
		createEStructuralFeatureNode(segmentNode, reactionsSegment,
			TopLevelElementsPackage.Literals.REACTIONS_SEGMENT__REACTIONS, imageDispatcher.invoke(reactionsSegment),
			"reactions", false)
		createEStructuralFeatureNode(segmentNode, reactionsSegment,
			TopLevelElementsPackage.Literals.REACTIONS_SEGMENT__ROUTINES, imageDispatcher.invoke(reactionsSegment),
			"routines", false)
	}

	protected def Object _text(MetamodelImport mmImport) {
		return mmImport?.name + ": " + mmImport?.package.nsURI;
	}

	protected def Object _text(ReactionsImport reactionsImport) {
		return reactionsImport.importedReactionsSegment?.name;
	}

	protected def Object _text(Reaction reaction) {
		return "reaction: " + reaction.displayName;
	}

	protected def Object _text(Routine routine) {
		return "routine: " + routine.displayName;
	}

	protected def Object _text(RoutineInput routineInput) {
		return "parameters";
	}

	protected def Object _text(MatchBlock matcher) {
		return "match";
	}

	protected def Object _text(CreateBlock creator) {
		return "create";
	}
	
	protected def Object _text(UpdateBlock update) {
		return "update";
	}

	protected def Object _text(ReactionsSegment reactionsSegment) {
		return "segment: " + reactionsSegment.name;
	}

	protected def Object _text(Trigger trigger) {
		return "there is no outline for this trigger";
	}

	protected def Object _text(ModelElementChange event) {
		return "element change";
	}

	protected def Object _text(ModelAttributeChange event) {
		return "attribute change";
	}

	protected def Object _text(ArbitraryModelChange event) {
		return "any change";
	}

	protected def boolean _isLeaf(Trigger element) {
		return true;
	}

	protected def boolean _isLeaf(MatchBlock element) {
		return true;
	}

	protected def boolean _isLeaf(CreateBlock element) {
		return true;
	}
	
	protected def boolean _isLeaf(UpdateBlock element) {
		return true;
	}

	protected def boolean _isLeaf(RoutineCall element) {
		return true;
	}

}
