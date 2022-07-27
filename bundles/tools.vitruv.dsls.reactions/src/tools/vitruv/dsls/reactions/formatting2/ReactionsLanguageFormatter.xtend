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
import tools.vitruv.dsls.reactions.language.ArbitraryModelChange
import org.eclipse.xtext.xbase.formatting2.XbaseFormatter
import tools.vitruv.dsls.reactions.language.RetrieveModelElement
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference
import tools.vitruv.dsls.common.elements.NamedMetaclassReference
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall
import tools.vitruv.dsls.common.elements.MetamodelImport
import org.eclipse.xtext.RuleCall
import tools.vitruv.dsls.reactions.language.RetrieveModelElementType

class ReactionsLanguageFormatter extends XbaseFormatter {

	def dispatch void format(ReactionsFile reactionsFile, extension IFormattableDocument document) {
		reactionsFile.namespaceImports?.importDeclarations?.forEach[it.format(document)]
		reactionsFile.namespaceImports?.importDeclarations?.tail?.forEach[prepend [newLine]]
		reactionsFile.namespaceImports?.append[newLines = 2]
		reactionsFile.metamodelImports.forEach[it.format(document)]
		reactionsFile.metamodelImports.tail.forEach[prepend [newLine]]
		reactionsFile.metamodelImports.last?.append[newLines = 2]
		reactionsFile.reactionsSegments.forEach[it.format(document)]
		reactionsFile.reactionsSegments.tail.forEach[prepend [newLines = 4]]
	}

	def dispatch void format(MetamodelImport metamodelImport, extension IFormattableDocument document) {
		metamodelImport.regionFor.keyword('import').append[oneSpace]
		metamodelImport.regionFor.keyword('as').surround[oneSpace]
	}

	def dispatch void format(ReactionsSegment segment, extension IFormattableDocument document) {
		segment.regionFor.keyword("reactions:") => [
			prepend[newLines = 2]
			append[oneSpace]
		]
		segment.regionFor.keywordPairs("in", "reaction").forEach [
			key.prepend[newLine]
		]
		segment.regionFor.keyword('reaction').surround[oneSpace]
		segment.regionFor.keyword('to').surround[oneSpace]
		segment.regionFor.keyword('changes').surround[oneSpace]
		segment.regionFor.keywords('in').forEach[append[oneSpace]]
		segment.regionFor.keywords('and').forEach[append[oneSpace]]
		segment.regionFor.keyword('execute') => [
			prepend[newLine]
			append[oneSpace]
		]
		segment.regionFor.keyword('actions').surround[oneSpace]
		segment.regionFor.keyword('minimal').surround[oneSpace]
		segment.reactionsImports.head?.prepend[highPriority; newLines = 2]
		segment.reactionsImports.forEach[it.format(document)]
		segment.reactionsImports.last?.append[newLines = 2]
		segment.reactions.forEach[it.format(document)]
		segment.routines.forEach[it.format(document)]
	}

	def dispatch void format(ReactionsImport reactionsImport, extension IFormattableDocument document) {
		reactionsImport.prepend[newLine]
		reactionsImport.regionFor.keyword('import').append[oneSpace]
		reactionsImport.regionFor.keyword('routines').surround[oneSpace]
		reactionsImport.regionFor.keyword('using').surround[oneSpace]
		reactionsImport.regionFor.keyword('qualified').surround[oneSpace]
		reactionsImport.regionFor.keyword('names').prepend[oneSpace]
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
		arbitraryModelChange.regionFor.keyword('anychange').surround[oneSpace]
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
		changeType.regionFor.keyword("created").surround[oneSpace]
		changeType.regionFor.keyword("deleted").surround[oneSpace]
		changeType.regionFor.keyword("inserted").surround[oneSpace]
		changeType.regionFor.keyword("in").surround[oneSpace]
		changeType.regionFor.keyword("removed").surround[oneSpace]
		changeType.regionFor.keyword("from").surround[oneSpace]
		changeType.regionFor.keyword("replaced").surround[oneSpace]
		changeType.regionFor.keyword("at").surround[oneSpace]
		changeType.regionFor.keyword("root").prepend[oneSpace]
		if (changeType instanceof ElementReferenceChangeType) {
			changeType.feature.format(document)
		}
	}

	def dispatch format(ModelAttributeChange modelAttributeChange, extension IFormattableDocument document) {
		modelAttributeChange.formatTrigger(document)
		modelAttributeChange.regionFor.keyword('attribute').surround[oneSpace]
		modelAttributeChange.regionFor.keyword("inserted").surround[oneSpace]
		modelAttributeChange.regionFor.keyword("in").surround[oneSpace]
		modelAttributeChange.regionFor.keyword("removed").surround[oneSpace]
		modelAttributeChange.regionFor.keyword("from").surround[oneSpace]
		modelAttributeChange.regionFor.keyword("replaced").surround[oneSpace]
		modelAttributeChange.regionFor.keyword("at").surround[oneSpace]
		modelAttributeChange.feature?.surround[oneSpace]
		modelAttributeChange.feature?.format(document)
	}

	private def formatTrigger(Trigger trigger, extension IFormattableDocument document) {
		trigger.regionFor.keyword('after').append[oneSpace]
		trigger.regionFor.keyword('with').prepend[newLine; indent]
		trigger.precondition?.prepend[oneSpace]
		trigger.precondition?.format(document)
	}

	def dispatch format(RoutineCall routineCall, extension IFormattableDocument document) {
		routineCall.prepend[newLine]
		routineCall.code?.prepend[oneSpace]
		routineCall.code?.format(document)
	}

	def dispatch format(Routine routine, extension IFormattableDocument document) {
		if (routine.documentation !== null) {
			routine.regionFor.keyword('routine').prepend[newLine]
		}
		routine.regionFor.keyword('routine') => [
			prepend[newLines = 2]
			append[oneSpace]
		]
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
		routineInput.regionFor.keywords('plain').forEach [
			append[oneSpace]
		]
		routineInput.javaInputElements.forEach [
			it.format(document)
		]
		routineInput.regionFor.keywords(',').forEach [
			prepend[noSpace]
			append[oneSpace]
		]
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
		matchCheckStatement.regionFor.keyword('check').append[oneSpace]
		matchCheckStatement.regionFor.keyword('asserted').surround[oneSpace]
		matchCheckStatement.condition?.prepend[oneSpace]
		matchCheckStatement.condition?.format(document)
	}

	def dispatch void format(RetrieveModelElement retrieveStatement, extension IFormattableDocument document) {
		retrieveStatement.formatAssignment(document)
		retrieveStatement.retrievalType.format(document)
		retrieveStatement.regionFor.keyword('retrieve').surround[oneSpace]
		retrieveStatement.formatRetrieveOrRequireAbsence(document)
	}

	def dispatch void format(RetrieveModelElementType retrieveElementType, extension IFormattableDocument document) {
		retrieveElementType.regionFor.keyword('asserted').surround[oneSpace]
		retrieveElementType.regionFor.keyword('optional').surround[oneSpace]
		retrieveElementType.regionFor.keyword('many').surround[oneSpace]
	}

	def dispatch void format(RequireAbscenceOfModelElement requireAbsenceStatement,
		extension IFormattableDocument document) {
		requireAbsenceStatement.regionFor.keyword('require').append[oneSpace]
		requireAbsenceStatement.regionFor.keyword('absence').surround[oneSpace]
		requireAbsenceStatement.regionFor.keyword('of').surround[oneSpace]
		requireAbsenceStatement.formatRetrieveOrRequireAbsence(document)
	}

	private def void formatRetrieveOrRequireAbsence(
		RetrieveOrRequireAbscenceOfModelElement retrieveOrRequireAbsenceStatment,
		extension IFormattableDocument document) {
		retrieveOrRequireAbsenceStatment.regionFor.keywordPairs('corresponding', 'to').forEach [
			key.surround[oneSpace]
			value.surround[oneSpace]
		]
		retrieveOrRequireAbsenceStatment.regionFor.keyword('tagged').surround[oneSpace]
		retrieveOrRequireAbsenceStatment.regionFor.keyword('with').surround[oneSpace]
		retrieveOrRequireAbsenceStatment.prepend[newLine]
		retrieveOrRequireAbsenceStatment.elementType?.format(document)
		retrieveOrRequireAbsenceStatment.correspondenceSource?.prepend[oneSpace]
		retrieveOrRequireAbsenceStatment.correspondenceSource?.format(document)
		retrieveOrRequireAbsenceStatment.tag?.prepend[oneSpace]
		retrieveOrRequireAbsenceStatment.tag?.format(document)
		retrieveOrRequireAbsenceStatment.precondition?.prepend[oneSpace]
		retrieveOrRequireAbsenceStatment.precondition?.format(document)
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
		metaclassReference.formatMetaclassReference(document)
	}

	private def formatMetaclassReference(MetaclassReference metaclassReference,
		extension IFormattableDocument document) {
		metaclassReference.regionFor.keyword('::').surround[noSpace]
	}

	def dispatch format(NamedMetaclassReference namedMetaclassReference, extension IFormattableDocument document) {
		namedMetaclassReference.formatMetaclassReference(document)
		// There needs to be a space between type reference and name, but neither element can be referenced,
		// so we prepend a space before the only RuleCall (which is the rule for the name, as the metamodel
		// and metaclass reference are cross references).
		namedMetaclassReference.semanticRegions.forEach [
			val grammarElement = it.grammarElement
			if (grammarElement instanceof RuleCall) {
				if (grammarElement.rule == grammar.validIDRule) {
					prepend[oneSpace]
				}
			}
		]
	}

	def dispatch format(NamedJavaElementReference namedJavaElementReference, extension IFormattableDocument document) {
		namedJavaElementReference.regionFor.keyword('as').surround[oneSpace]
		namedJavaElementReference.type?.append[oneSpace]
		namedJavaElementReference.type?.format(document)
	}

	def dispatch format(MetaclassEAttributeReference attributeReference, extension IFormattableDocument document) {
		attributeReference.formatMetaclassReference(document)
		attributeReference.feature.format(document)
		attributeReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		attributeReference.regionFor.keyword(']').prepend[noSpace]
	}

	def dispatch format(MetaclassEReferenceReference referenceReference, extension IFormattableDocument document) {
		referenceReference.formatMetaclassReference(document)
		referenceReference.feature.format(document)
		referenceReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		referenceReference.regionFor.keyword(']').prepend[noSpace]
	}

	private def void formatAssignment(EObject assigment, extension IFormattableDocument document) {
		assigment.regionFor.keyword('val').append[oneSpace]
		assigment.regionFor.keyword('=').surround[oneSpace]
	}

	private def formatInteriorBlock(EObject element, extension IFormattableDocument document) {
		interior(
			element.regionFor.keyword('{') => [
				prepend[oneSpace]
				append[newLine]
			],
			element.regionFor.keyword('}') => [
				prepend[newLine]
			],
			[indent]
		)
	}
}
