package tools.vitruv.dsls.reactions.formatting2

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.xbase.XAssignment
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.xbase.XConstructorCall
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XFeatureCall
import org.eclipse.xtext.xbase.XListLiteral
import org.eclipse.xtext.xbase.XMemberFeatureCall
import org.eclipse.xtext.xbase.XVariableDeclaration
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
import tools.vitruv.dsls.reactions.language.Taggable
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement
import org.eclipse.xtext.xbase.XBinaryOperation
import org.eclipse.xtext.xbase.XCastedExpression
import org.eclipse.xtext.common.types.JvmTypeReference
import tools.vitruv.dsls.common.elements.MetaclassReference
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference
import org.eclipse.xtext.formatting2.AbstractFormatter2
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement
import tools.vitruv.dsls.reactions.language.MatchCheckStatement
import tools.vitruv.dsls.reactions.language.toplevelelements.CodeBlock

class ReactionsLanguageFormatter extends AbstractFormatter2 {
	
	def dispatch void format(ReactionsFile reactionsFile, extension IFormattableDocument document) {
		reactionsFile.metamodelImports.tail.forEach[prepend [newLine]]
		reactionsFile.metamodelImports.last?.append[newLines = 2]
		reactionsFile.formatReactionsFile(document)
	}

	def formatReactionsFile(ReactionsFile reactionsFile, extension IFormattableDocument document) {
		reactionsFile.namespaceImports?.importDeclarations?.tail?.forEach[prepend [newLine]]
		reactionsFile.namespaceImports?.append[newLines = 2]
		reactionsFile.reactionsSegments.forEach[formatReactionsSegment(document)]
		reactionsFile.reactionsSegments.tail.forEach[prepend [newLines = 4]]
	}

	def formatReactionsSegment(ReactionsSegment segment, extension IFormattableDocument document) {
		segment.regionFor.keyword('in reaction to changes in').prepend[newLine]
		segment.regionFor.keyword('execute').prepend[newLine]
		segment.reactionsImports.head?.prepend[highPriority; newLines = 2]
		segment.reactionsImports.forEach[formatReactionsImport(document)]
		segment.reactionsImports.last?.append[newLines = 2]
		segment.reactions.forEach[formatReaction(document)]
		segment.routines.forEach[formatRoutine(document)]
	}

	def formatReactionsImport(ReactionsImport reactionsImport, extension IFormattableDocument document) {
		reactionsImport.prepend[newLine]
		reactionsImport.regionFor.keyword('import').append[oneSpace]
	}

	def formatReaction(Reaction reaction, extension IFormattableDocument document) {
		reaction.prepend[newLines = 2]
		if (reaction.documentation !== null) {
			reaction.regionFor.keyword('reaction').prepend[newLine]
		}
		reaction.regionFor.keyword('reaction').append[oneSpace]
		reaction.regionFor.keyword('::').prepend[noSpace].append[noSpace]
		reaction.formatInteriorBlock(document)
		reaction.trigger.formatTrigger(document)
		reaction.callRoutine.prepend[newLine]
		reaction.callRoutine.code.formatIndividually(document)
	}

	def formatTrigger(Trigger trigger, extension IFormattableDocument document) {
		trigger.regionFor.keyword('with').prepend[newLine; indent]
		trigger.formatIndividually(document)
	}

	def dispatch void formatIndividually(ModelElementChange modelElementChange,
		extension IFormattableDocument document) {
		modelElementChange.elementType.formatMetaclassReference(document)
		modelElementChange.changeType.formatChangeType(document)
		modelElementChange?.precondition?.code?.formatIndividually(document)
	}
	
	def void formatChangeType(ElementChangeType changeType, extension IFormattableDocument document) {
		if (changeType instanceof ElementReferenceChangeType) {
			changeType.feature.formatEReferenceReference(document)
		}
	}

	def dispatch void formatIndividually(ModelAttributeChange modelAttributeChange,
		extension IFormattableDocument document) {
		modelAttributeChange.feature.formatEAttributeReference(document)
	}

	def formatRoutine(Routine routine, extension IFormattableDocument document) {
		routine.prepend[newLines = 2]
		if (routine.documentation !== null) {
			routine.regionFor.keyword('routine').prepend[newLine]
		}
		routine.regionFor.keyword('routine').append[oneSpace]
		routine.overrideImportPath?.formatRoutineOverrideImportPath(document)
		routine.regionFor.keyword('::').prepend[noSpace].append[noSpace]
		routine.input.formatRoutineInput(document)
		routine.formatInteriorBlock(document)
		routine.matchBlock?.formatMatchBlock(document)
		routine.createBlock?.formatCreateBlock(document)
		routine.updateBlock?.formatUpdateBlock(document)
	}

	def formatRoutineOverrideImportPath(RoutineOverrideImportPath routineOverrideImportPath, extension IFormattableDocument document) {
		routineOverrideImportPath.allRegionsFor.keyword('.').prepend[noSpace].append[noSpace]
	}

	def formatRoutineInput(RoutineInput routineInput, extension IFormattableDocument document) {
		routineInput.regionFor.keyword('(').prepend[noSpace].append[noSpace]
		routineInput.modelInputElements.forEach[formatMetaclassReference(document)]
		routineInput.allRegionsFor.keyword(',').prepend[noSpace].append[oneSpace]
		routineInput.regionFor.keyword(')').prepend[noSpace]
	}

	def formatMatchBlock(MatchBlock match, extension IFormattableDocument document) {
		match.prepend[newLine]
		match.formatInteriorBlock(document)
		match.matchStatements.forEach[formatMatchStatement(document)]
	}
	
	
	def dispatch void formatIndividually(MatchCheckStatement matchCheckStatement,
		extension IFormattableDocument document) {
		matchCheckStatement.code?.formatIndividually(document)
	}

	def formatMatchStatement(MatchStatement matchStatement, extension IFormattableDocument document) {
		matchStatement.prepend[newLine]
		matchStatement.formatIndividually(document)
	}

	def dispatch void formatIndividually(RetrieveOrRequireAbscenceOfModelElement retrieveStatement,
		extension IFormattableDocument document) {
		retrieveStatement.elementType.formatMetaclassReference(document)
		retrieveStatement.formatTaggable(document)
	}

	def formatCreateBlock(CreateBlock creator, extension IFormattableDocument document) {
		creator.prepend[newLine]
		creator.formatInteriorBlock(document)
		creator.createStatements.forEach[formatMetaclassReference(document)]
	}
	
	def void formatUpdateBlock(CodeBlock update, extension IFormattableDocument document) {
		update.code?.formatIndividually(document)
	}

	def formatTaggable(Taggable taggable, extension IFormattableDocument document) {
		taggable.regionFor.keyword('tagged with').prepend[newLine]
	}

	def dispatch void formatIndividually(XExpression anyExpression, extension IFormattableDocument document) {}

	def dispatch void formatIndividually(XBlockExpression block, extension IFormattableDocument document) {
		block.formatInteriorBlock(document)
		block.expressions.forEach[prepend [newLine]; formatIndividually(document)]
	}

	def dispatch void formatIndividually(XFeatureCall featureCall, extension IFormattableDocument document) {
		featureCall.regionFor.keyword('(').prepend[noSpace].append[noSpace]
		featureCall.featureCallArguments.forEach[formatIndividually(document)]
		featureCall.allRegionsFor.keyword(",").prepend[noSpace].append[oneSpace]
		featureCall.regionFor.keyword(')').prepend[noSpace]
	}

	def dispatch void formatIndividually(XMemberFeatureCall memberFeatureCall,
		extension IFormattableDocument document) {
		memberFeatureCall.memberCallTarget.formatIndividually(document)
		memberFeatureCall.regionFor.keyword('(').prepend[noSpace].append[noSpace]
		memberFeatureCall.regionFor.keyword('.').prepend[noSpace].append[noSpace]
		memberFeatureCall.memberCallArguments.forEach[formatIndividually(document)]
		memberFeatureCall.allRegionsFor.keyword(",").prepend[noSpace].append[oneSpace]
		memberFeatureCall.regionFor.keyword(')').prepend[noSpace]
	}

	def dispatch void formatIndividually(XConstructorCall constructorCall, extension IFormattableDocument document) {
		constructorCall.regionFor.keyword('new').append[oneSpace]
		constructorCall.regionFor.keyword('(').prepend[noSpace].append[noSpace]
		constructorCall.arguments.forEach[formatIndividually(document)]
		constructorCall.allRegionsFor.keyword(",").prepend[noSpace].append[oneSpace]
		constructorCall.regionFor.keyword(')').prepend[noSpace]
	}

	def dispatch void formatIndividually(XListLiteral listLiteral, extension IFormattableDocument document) {
		listLiteral.regionFor.keyword('#').append[noSpace]
		listLiteral.regionFor.keyword('[').append[noSpace]
		listLiteral.regionFor.keyword(']').prepend[noSpace]
		listLiteral.elements.forEach[formatIndividually(document)]
		listLiteral.allRegionsFor.keyword(',').prepend[noSpace].append[oneSpace]
	}

	def dispatch void formatIndividually(XAssignment assignment, extension IFormattableDocument document) {
		assignment.assignable.formatIndividually(document)
		assignment.regionFor.keyword('.').prepend[noSpace].append[noSpace]
		assignment.regionFor.keyword('=').prepend[oneSpace].append[oneSpace]
		assignment.value.formatIndividually(document)
	}
	
	def dispatch void formatIndividually(XBinaryOperation binaryOperation, extension IFormattableDocument document) {
		binaryOperation.leftOperand.formatIndividually(document)
		binaryOperation.rightOperand.formatIndividually(document)
	}

	def dispatch void formatIndividually(XVariableDeclaration variableDeclaration,
		extension IFormattableDocument document) {
		variableDeclaration.right?.formatIndividually(document)
	}
	
	def dispatch void formatIndividually(XCastedExpression cast, extension IFormattableDocument document) {
		cast.target.formatIndividually(document)
		cast.type.formatIndividually(document)
	}
	
	def dispatch void formatIndividually(JvmTypeReference typeReference, extension IFormattableDocument document) {
		typeReference.allRegionsFor.keyword('.').prepend [noSpace].append [noSpace]
		typeReference.regionFor.keyword('<').prepend [noSpace].append [noSpace]
		typeReference.allRegionsFor.keyword(',').prepend [noSpace].append [oneSpace]
		typeReference.regionFor.keyword('>').prepend [noSpace]
	}

	def formatInteriorBlock(EObject element, extension IFormattableDocument document) {
		interior(
			element.regionFor.keyword('{').append[newLine],
			element.regionFor.keyword('}').prepend[newLine],
			[indent]
		)
	}
	
	def protected void formatMetaclassReference(MetaclassReference metaclassReference,
		extension IFormattableDocument document) {
		metaclassReference.regionFor.keyword('::').prepend[noSpace].append[noSpace]
	}

	def protected void formatEAttributeReference(MetaclassEAttributeReference attributeReference,
		extension IFormattableDocument document) {
		attributeReference.formatMetaclassReference(document)
		attributeReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		attributeReference.regionFor.keyword(']').prepend[noSpace]
	}

	def protected void formatEReferenceReference(MetaclassEReferenceReference referenceReference,
		extension IFormattableDocument document) {
		referenceReference.formatMetaclassReference(document)
		referenceReference.regionFor.keyword('[').prepend[noSpace].append[noSpace]
		referenceReference.regionFor.keyword(']').prepend[noSpace]
	}
}
