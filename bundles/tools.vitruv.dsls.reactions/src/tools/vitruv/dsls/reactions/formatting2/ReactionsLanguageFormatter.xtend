package tools.vitruv.dsls.reactions.formatting2

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.formatting2.IFormattableDocument
import tools.vitruv.dsls.reactions.language.ElementChangeType
import tools.vitruv.dsls.reactions.language.ElementReferenceChangeType
import tools.vitruv.dsls.reactions.language.ModelAttributeChange
import tools.vitruv.dsls.reactions.language.ModelElementChange
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement
import tools.vitruv.dsls.common.elements.MetaclassReference
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock
import tools.vitruv.dsls.reactions.language.MatchCheckStatement
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.PreconditionCodeBlock
import tools.vitruv.dsls.reactions.language.ArbitraryModelChange
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCallBlock
import org.eclipse.xtext.xbase.formatting2.XbaseFormatter
import tools.vitruv.dsls.reactions.language.RetrieveModelElement
import tools.vitruv.dsls.reactions.language.CorrespondingObjectCodeBlock
import tools.vitruv.dsls.reactions.language.TagCodeBlock
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference
import tools.vitruv.dsls.common.elements.NamedMetaclassReference

class ReactionsLanguageFormatter extends XbaseFormatter {

	def dispatch void format(ReactionsFile reactionsFile, extension IFormattableDocument document) {
		reactionsFile.metamodelImports.tail.forEach[prepend [newLine]]
		reactionsFile.metamodelImports.last?.append[newLines = 2]
		reactionsFile.namespaceImports?.importDeclarations?.tail?.forEach[prepend [newLine]]
		reactionsFile.namespaceImports?.append[newLines = 2]
		reactionsFile.reactionsSegments.forEach[it.format(document)]
		reactionsFile.reactionsSegments.tail.forEach[prepend [newLines = 4]]
	}

	def dispatch void format(ReactionsSegment segment, extension IFormattableDocument document) {
		segment.regionFor.keywordPairs("in", "reaction").forEach [
			key.prepend[newLine]
		]
		segment.regionFor.keyword('execute').prepend[newLine]
		segment.reactionsImports.head?.prepend[highPriority; newLines = 2]
		segment.reactionsImports.forEach[it.format(document)]
		segment.reactionsImports.last?.append[newLines = 2]
		segment.reactions.forEach[it.format(document)]
		segment.routines.forEach[it.format(document)]
	}

	def dispatch void format(ReactionsImport reactionsImport, extension IFormattableDocument document) {
		reactionsImport.prepend[newLine]
		reactionsImport.regionFor.keyword('import').append[oneSpace]
	}

	def dispatch void format(Reaction reaction, extension IFormattableDocument document) {
		reaction.prepend[newLines = 2]
		if (reaction.documentation !== null) {
			reaction.regionFor.keyword('reaction').prepend[newLine]
		}
		reaction.regionFor.keyword('reaction').append[oneSpace]
		reaction.regionFor.keyword('::').surround[noSpace]
		reaction.formatInteriorBlock(document)
		reaction.trigger?.format(document)
		reaction.callRoutine?.format(document)
	}

	def dispatch format(ArbitraryModelChange arbitraryModelChange, extension IFormattableDocument document) {
		arbitraryModelChange.formatTrigger(document)
		arbitraryModelChange.regionFor.keywordPairs('any', 'change').forEach [
			key.surround[oneSpace]
			value.surround[oneSpace]
		]
	}

	def dispatch format(ModelElementChange modelElementChange, extension IFormattableDocument document) {
		modelElementChange.formatTrigger(document)
		modelElementChange.regionFor.keyword('element').surround[oneSpace]
		modelElementChange.elementType?.surround[oneSpace]
		modelElementChange.elementType.format(document)
		modelElementChange.changeType?.surround[oneSpace]
		modelElementChange.changeType?.format(document)
	}

	def dispatch format(ElementChangeType changeType, extension IFormattableDocument document) {
		if (changeType instanceof ElementReferenceChangeType) {
			changeType.feature.format(document)
		}
	}

	def dispatch format(ModelAttributeChange modelAttributeChange, extension IFormattableDocument document) {
		modelAttributeChange.formatTrigger(document)
		modelAttributeChange.regionFor.keyword('attribute').surround[oneSpace]
		modelAttributeChange.feature?.surround[oneSpace]
		modelAttributeChange.feature?.format(document)
	}

	private def formatTrigger(Trigger trigger, extension IFormattableDocument document) {
		trigger.regionFor.keyword('after').append[oneSpace]
		trigger.precondition?.prepend[oneSpace]
		trigger.precondition?.format(document)
	}

	def dispatch format(PreconditionCodeBlock precondition, extension IFormattableDocument document) {
		precondition.code?.prepend[oneSpace]
		precondition.code?.format(document)
	}

	def dispatch format(RoutineCallBlock routineCall, extension IFormattableDocument document) {
		routineCall.prepend[newLine]
		routineCall.code?.prepend[oneSpace]
		routineCall.code?.format(document)
	}

	def dispatch format(Routine routine, extension IFormattableDocument document) {
		routine.prepend[newLines = 2]
		if (routine.documentation !== null) {
			routine.regionFor.keyword('routine').prepend[newLine]
		}
		routine.regionFor.keyword('routine').append[oneSpace]
		routine.overrideImportPath?.format(document)
		routine.regionFor.keyword('::').surround[noSpace]
		routine.input?.format(document)
		routine.formatInteriorBlock(document)
		routine.matchBlock?.format(document)
		routine.createBlock?.format(document)
		routine.updateBlock?.format(document)
	}

	def dispatch format(RoutineOverrideImportPath routineOverrideImportPath, extension IFormattableDocument document) {
		routineOverrideImportPath.allRegionsFor.keyword('.').surround[noSpace]
	}

	def dispatch format(RoutineInput routineInput, extension IFormattableDocument document) {
		routineInput.regionFor.keyword('(').surround[noSpace]
		routineInput.modelInputElements.forEach [
			it.format(document)
		]
		routineInput.javaInputElements.forEach [
			it.format(document)
		]
		routineInput.allRegionsFor.keyword(',').prepend[noSpace].append[oneSpace]
		routineInput.regionFor.keyword(')').prepend[noSpace]
	}

	def dispatch format(MatchBlock match, extension IFormattableDocument document) {
		match.prepend[newLine]
		match.regionFor.keyword('match').append[oneSpace]
		match.formatInteriorBlock(document)
		match.matchStatements.forEach[it.format(document)]
	}

	def dispatch void format(MatchCheckStatement matchCheckStatement, extension IFormattableDocument document) {
		matchCheckStatement.prepend[newLine]
		matchCheckStatement.code?.format(document)
	}

	def dispatch void format(RetrieveModelElement retrieveStatement, extension IFormattableDocument document) {
		retrieveStatement.formatAssignment(document)
		retrieveStatement.formatRetrieveOrRequireAbsence(document)
	}

	def dispatch void format(RequireAbscenceOfModelElement requireAbsenceStatement,
		extension IFormattableDocument document) {
		requireAbsenceStatement.formatRetrieveOrRequireAbsence(document)
	}

	private def void formatRetrieveOrRequireAbsence(
		RetrieveOrRequireAbscenceOfModelElement retrieveOrRequireAbsenceStatment,
		extension IFormattableDocument document) {
		retrieveOrRequireAbsenceStatment.prepend[newLine]
		retrieveOrRequireAbsenceStatment.elementType?.format(document)
		retrieveOrRequireAbsenceStatment.correspondenceSource?.prepend[oneSpace]
		retrieveOrRequireAbsenceStatment.correspondenceSource?.format(document)
		retrieveOrRequireAbsenceStatment.tag?.prepend[oneSpace]
		retrieveOrRequireAbsenceStatment.tag?.format(document)
	}

	def dispatch format(CorrespondingObjectCodeBlock correspondingObject, extension IFormattableDocument document) {
		correspondingObject.regionFor.keywordPairs('corresponds', 'to').forEach [
			key.surround[oneSpace]
			value.surround[oneSpace]
		]
		correspondingObject.code?.format(document)
	}

	def dispatch format(TagCodeBlock tag, extension IFormattableDocument document) {
		tag.regionFor.keywordPairs('tagged', 'with').forEach [
			key.surround[oneSpace]
			value.surround[oneSpace]
		]
		tag.code?.format(document)
	}

	def dispatch format(CreateBlock create, extension IFormattableDocument document) {
		create.prepend[newLine]
		create.regionFor.keyword('create').append[oneSpace]
		create.formatInteriorBlock(document)
		create.createStatements.forEach[it.formatCreateStatement(document)]
	}

	private def formatCreateStatement(NamedMetaclassReference createStatement,
		extension IFormattableDocument document) {
		createStatement.regionFor.keyword('new').surround[oneSpace]
		createStatement.formatAssignment(document)
		createStatement.format(document)
	}

	def dispatch format(UpdateBlock update, extension IFormattableDocument document) {
		update.prepend[newLine]
		update.regionFor.keyword('update').append[oneSpace]
		update.code?.format(document)
	}

	def dispatch format(MetaclassReference metaclassReference, extension IFormattableDocument document) {
		metaclassReference.regionFor.keyword('::').surround[noSpace]
	}

	def dispatch format(NamedMetaclassReference namedMetaclassReference, extension IFormattableDocument document) {
		namedMetaclassReference.metaclass?.append[oneSpace]
		namedMetaclassReference.regionFor.keyword('::').surround[noSpace]
	}

	def dispatch format(NamedJavaElementReference namedJavaElementReference, extension IFormattableDocument document) {
		namedJavaElementReference.type?.append[oneSpace]
		namedJavaElementReference.type?.format(document)
	}

	def dispatch format(MetaclassEAttributeReference attributeReference, extension IFormattableDocument document) {
		attributeReference.format(document)
		attributeReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		attributeReference.regionFor.keyword(']').prepend[noSpace]
	}

	def dispatch format(MetaclassEReferenceReference referenceReference, extension IFormattableDocument document) {
		referenceReference.format(document)
		referenceReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		referenceReference.regionFor.keyword(']').prepend[noSpace]
	}

	private def void formatAssignment(EObject assigment, extension IFormattableDocument document) {
		assigment.regionFor.keyword('val').append[oneSpace]
		assigment.regionFor.keyword('=').surround[oneSpace]
	}

	private def formatInteriorBlock(EObject element, extension IFormattableDocument document) {
		interior(
			element.regionFor.keyword('{').append[newLine],
			element.regionFor.keyword('}').prepend[newLine],
			[indent]
		)
	}
}
