package tools.vitruv.dsls.reactions.builder

import static org.hamcrest.MatcherAssert.assertThat
import org.eclipse.xtext.xbase.XbaseFactory
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.hamcrest.CoreMatchers.containsString
import allElementTypes.AllElementTypesPackage
import allElementTypes2.AllElementTypes2Package

class FluentReactionsLanguageBuilderTests extends FluentReactionsBuilderTest {
	@Test
	def void createRoot() {
		val builder = create.reactionsFile('createRootTest') +=
			create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('CreateRootTest').afterElement(Root).created.call [
				create [
					vall('newRoot').create(Root)
				].update [
					addCorrespondenceBetween('newRoot').and.affectedEObject	
				]
			]

		val reactionResult = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call createRootTestRepair(affectedEObject)
			}
			
			routine createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
		'''

		assertThat(builder, builds(reactionResult))
	}

	@Test
	def void removeRoot() {
		val builder = create.reactionsFile('deleteRootTest') +=
			create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('DeleteRootTest').afterElement(Root).deleted.call [
				match [
					vall('toDelete').retrieve(Root).correspondingTo.affectedEObject
				].update [
					delete('toDelete')
				]
			]

		val reactionResult = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction DeleteRootTest {
				after element allElementTypes::Root deleted
				call deleteRootTestRepair(affectedEObject)
			}
			
			routine deleteRootTestRepair(allElementTypes::Root affectedEObject) {
				match {
					val toDelete = retrieve allElementTypes::Root corresponding to affectedEObject
				}
				update {
					removeObject(toDelete)
				}
			}
		'''

		assertThat(builder, builds(reactionResult))
	}

	@Test
	def void importSegments() {
		val builder = create.reactionsFile('importTest')

		val baseSegment = create.reactionsSegment('baseSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE)
		baseSegment += create.reaction('CreateRootTest').afterElement(Root).created.call [
			create [
				vall('newRoot').create(Root)
			].update[
				addCorrespondenceBetween('newRoot').and.affectedEObject
			]
		]

		val baseSegment2 = create.reactionsSegment('baseSegment2').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE)
		baseSegment2 += create.reaction('DeleteRootTest').afterElement(Root).deleted.call [
			match [
				vall('toDelete').retrieve(Root).correspondingTo.affectedEObject
			].update [
				delete('toDelete')
			]
		]

		val extendedSegment = create.reactionsSegment('extendedSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE).importSegment(baseSegment).usingSimpleRoutineNames.
			importSegment(baseSegment2).routinesOnly.usingQualifiedRoutineNames

		builder += baseSegment
		builder += baseSegment2
		builder += extendedSegment

		val reactionResult = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: baseSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call createRootTestRepair(affectedEObject)
			}
			
			routine createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
			
			
			
			reactions: baseSegment2
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction DeleteRootTest {
				after element allElementTypes::Root deleted
				call deleteRootTestRepair(affectedEObject)
			}
			
			routine deleteRootTestRepair(allElementTypes::Root affectedEObject) {
				match {
					val toDelete = retrieve allElementTypes::Root corresponding to affectedEObject
				}
				update {
					removeObject(toDelete)
				}
			}
			
			
			
			reactions: extendedSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			import baseSegment
			import routines baseSegment2 using qualified names
		'''

		assertThat(builder, builds(reactionResult))
	}

	@Test
	def void overrideReaction() {
		val builder = create.reactionsFile('overrideReactionTest')

		val baseSegment = create.reactionsSegment('baseSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE)
		baseSegment += create.reaction('CreateRootTest').afterElement(Root).created.call [
			create [
				vall('newRoot').create(Root)
			].update [
				addCorrespondenceBetween('newRoot').and.affectedEObject
			]
		]

		val extendedSegment = create.reactionsSegment('extendedSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE).importSegment(baseSegment).usingQualifiedRoutineNames
		extendedSegment +=
			create.reaction('CreateRootTest').overrideSegment(baseSegment).afterElement(Root).created.call [
				create [
					vall('newRootInOverride').create(Root)
				].update [
					addCorrespondenceBetween('newRootInOverride').and.affectedEObject
				]
			]

		builder += baseSegment
		builder += extendedSegment

		val reactionResult = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: baseSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call createRootTestRepair(affectedEObject)
			}
			
			routine createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
			
			
			
			reactions: extendedSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			import baseSegment using qualified names
			
			reaction baseSegment::CreateRootTest {
				after element allElementTypes::Root created
				call createRootTestRepair(affectedEObject)
			}
			
			routine createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRootInOverride = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRootInOverride, affectedEObject)
				}
			}
		'''

		assertThat(builder, builds(reactionResult))
	}

	@Test
	def void overrideRoutines() {
		val builder = create.reactionsFile('overrideRoutinesTest')

		val baseSegment = create.reactionsSegment('baseSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE)
		baseSegment += create.reaction('CreateRootTest').afterElement(Root).created.call [
			create [
				vall('newRoot').create(Root)
			].update [
				addCorrespondenceBetween('newRoot').and.affectedEObject
			]
		]

		val extendedSegment = create.reactionsSegment('extendedSegment').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE).importSegment(baseSegment).usingQualifiedRoutineNames
		extendedSegment += create.routine('createRootTestRepair') // TODO this is not working yet
		.overrideAlongImportPath(baseSegment).input [
			model(Root, 'affectedEObject')
		].create [
			vall('newRoot2').create(Root)
		].update [
			addCorrespondenceBetween('newRoot2').and.affectedEObject
		]

		val extendedSegment2 = create.reactionsSegment('extendedSegment2').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE).importSegment(extendedSegment).usingQualifiedRoutineNames
		extendedSegment2 += create.routine('createRootTestRepair') // TODO this is not working yet
		.overrideAlongImportPath(extendedSegment, baseSegment).input [
			model(Root, 'affectedEObject')
		].create [
			vall('newRoot3').create(Root)
		].update [
			addCorrespondenceBetween('newRoot3').and.affectedEObject
		]

		builder += baseSegment
		builder += extendedSegment
		builder += extendedSegment2

		val reactionResult = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: baseSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call createRootTestRepair(affectedEObject)
			}
			
			routine createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
			
			
			
			reactions: extendedSegment
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			import baseSegment using qualified names
			
			routine baseSegment::createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot2 = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot2, affectedEObject)
				}
			}
			
			
			
			reactions: extendedSegment2
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			import extendedSegment using qualified names
			
			routine extendedSegment.baseSegment::createRootTestRepair(allElementTypes::Root affectedEObject) {
				create {
					val newRoot3 = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot3, affectedEObject)
				}
			}
		'''

		assertThat(builder, builds(reactionResult))
	}

	@Test
	def void noEmptyReactionsFile() {
		val exception = assertThrows(IllegalStateException) [
			val builder = create.reactionsFile('empty')
			matcher.build(builder)
		]
		assertThat(exception.message, containsString("No reactions segments"))
	}

	@Test
	def void noEmptyReactionsSegment() {
		val exception = assertThrows(IllegalStateException) [
			val builder = create.reactionsFile('Test') +=
				create.reactionsSegment('empty').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
					executeActionsIn(AllElementTypesPackage.eINSTANCE)
			matcher.build(builder)
		]
		assertThat(exception.message, containsString("Neither routines, nor reactions, nor imports"))
	}

	@Test
	def void routineArgument() {
		val builder = create.reactionsFile('createRootTest') +=
			create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.routine('withArguments').input [
				model(Root, 'rootParameter')
				model(NonRoot, 'nonRootParameter')
			].update [
				addCorrespondenceBetween('rootParameter').and('nonRootParameter')
			]

		val expectedReaction = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			routine withArguments(allElementTypes::Root rootParameter, allElementTypes::NonRoot nonRootParameter) {
				update {
					addCorrespondenceBetween(rootParameter, nonRootParameter)
				}
			}
		'''

		assertThat(builder, builds(expectedReaction))
	}

	@Test
	def void routineForTwoReactionsImplicitlyAdded() {
		val commonRoutine = create.routine('commonRootCreate').input [
			model(EObject, 'affectedEObject')
		].create [
			vall('newRoot').create(Root)
		].update [
			addCorrespondenceBetween('newRoot').and.affectedEObject
		]

		val reactionsFile = create.reactionsFile('createRootTest')
		val reactionsSegment = create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE) += #[
			create.reaction('CreateRootTest').afterElement(Root).created.call(commonRoutine),
			create.reaction('CreateNonRootTest').afterElement(NonRoot).created.call(commonRoutine)
		]

		reactionsFile += reactionsSegment

		val expectedReaction = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			import "http://www.eclipse.org/emf/2002/Ecore" as ecore
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call commonRootCreate(affectedEObject)
			}
			
			reaction CreateNonRootTest {
				after element allElementTypes::NonRoot created
				call commonRootCreate(affectedEObject)
			}
			
			routine commonRootCreate(ecore::EObject affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
		'''

		assertThat(reactionsFile, builds(expectedReaction))
	}

	@Test
	def void routineForTwoReactionsExplicitlyAdded() {
		val commonRoutine = create.routine('commonRootCreate').input [
			model(EObject, 'affectedEObject')
		].create [
			vall('newRoot').create(Root)
		].update [
			addCorrespondenceBetween('newRoot').and.affectedEObject
		]

		val reactionsFile = create.reactionsFile('createRootTest')
		val reactionsSegment = create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE)

		reactionsSegment += commonRoutine

		reactionsSegment += create.reaction('CreateRootTest').afterElement(Root).created.call(commonRoutine)

		reactionsSegment += create.reaction('CreateNonRootTest').afterElement(NonRoot).created.call(commonRoutine)

		reactionsFile += reactionsSegment

		val expectedReaction = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://www.eclipse.org/emf/2002/Ecore" as ecore
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call commonRootCreate(affectedEObject)
			}
			
			reaction CreateNonRootTest {
				after element allElementTypes::NonRoot created
				call commonRootCreate(affectedEObject)
			}
			
			routine commonRootCreate(ecore::EObject affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
		'''

		assertThat(reactionsFile, builds(expectedReaction))
	}

	@Test
	def void routineWithMatch() {
		val objectExtensionsFqn = 'org.eclipse.xtext.xbase.lib.ObjectExtensions'
		val routineWithMatch = create.routine('withMatch').match [
			check [ typeProvider |
				XbaseFactory.eINSTANCE.createXBinaryOperation => [
					leftOperand = XbaseFactory.eINSTANCE.createXStringLiteral => [value = 'test']
					rightOperand = XbaseFactory.eINSTANCE.createXStringLiteral => [value = 'test']
					feature = (typeProvider.findTypeByName(objectExtensionsFqn) as JvmDeclaredType).members.findFirst [
						it.simpleName == 'operator_tripleEquals'
					]
				]
			]
		].create [
			vall('newRoot').create(Root)
		].withoutUpdate

		val reactionsFile = create.reactionsFile('routineWithMatchTest') +=
			create.reactionsSegment('routineWithMatchTest').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) += routineWithMatch

		val expectedReaction = '''
			import «objectExtensionsFqn»
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: routineWithMatchTest
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			routine withMatch() {
				match {
					check {
						"test" === "test"
					}
				}
				create {
					val newRoot = new allElementTypes::Root
				}
			}
		'''
		assertThat(reactionsFile, builds(expectedReaction))
	}

	@Test
	def void routineWithAffectedEObjectInMatch() {
		val objectExtensionsFqn = 'org.eclipse.xtext.xbase.lib.ObjectExtensions'
		val routineWithMatch = create.routine('withMatch').match [
			check [ typeProvider |
				XbaseFactory.eINSTANCE.createXBinaryOperation => [
					leftOperand = XbaseFactory.eINSTANCE.createXStringLiteral => [value = 'test']
					rightOperand = typeProvider.affectedEObject
					feature = (typeProvider.findTypeByName(objectExtensionsFqn) as JvmDeclaredType).members.findFirst [
						it.simpleName == 'operator_tripleEquals'
					]
				]
			]
		].create [
			vall('newRoot').create(Root)
		].update [
			addCorrespondenceBetween("newRoot").and.affectedEObject
		]

		val reaction = create.reaction('CreateRoot').afterElement(Root).created.call(routineWithMatch)

		val segment = create.reactionsSegment('routineWithMatchTest').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE) += reaction
		segment += routineWithMatch

		val reactionsFile = create.reactionsFile('routineWithMatchTest') += segment

		val expectedReaction = '''
			import «objectExtensionsFqn»
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			
			reactions: routineWithMatchTest
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRoot {
				after element allElementTypes::Root created
				call withMatch(affectedEObject)
			}
			
			routine withMatch(allElementTypes::Root affectedEObject) {
				match {
					check {
						"test" === affectedEObject
					}
				}
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
		'''
		assertThat(reactionsFile, builds(expectedReaction))
	}

	@Test
	@Disabled("not supported from text right now")
	def void routineFromDifferentSegmentWithImplicitSegment() {
		val commonRoutine = create.routine('commonRootCreate').input [
			model(EObject, 'affectedEObject')
		].create [
			vall('newRoot').create(Root)
		].update [
			addCorrespondenceBetween('newRoot').and.affectedEObject
		]

		val reactionsFile = create.reactionsFile('createRootTest')

		reactionsFile +=
			create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) +=
				create.reaction('CreateRootTest').afterElement(Root).created.call(commonRoutine)

		reactionsFile +=
			create.reactionsSegment('simpleChangesRoot2Tests').inReactionToChangesIn(AllElementTypes2Package.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) +=
				create.reaction('CreateRoot2Test').afterElement(Root2).created.call(commonRoutine)

		val expectedReaction = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			import "http://www.eclipse.org/emf/2002/Ecore" as ecore
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes2" as allElementTypes2
			
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call commonRootCreate(affectedEObject)
			}
			
			routine commonRootCreate(ecore::EObject affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
			
			
			reactions: simpleChangesRoot2Tests
			in reaction to changes in allElementTypes2
			execute actions in allElementTypes
			
			reaction CreateRoot2Test {
				after element allElementTypes2::Root2 created
				call commonRootCreate(affectedEObject)
			}
		'''

		assertThat(reactionsFile, builds(expectedReaction))
	}

	@Test
	@Disabled("not supported from text right now")
	def void routineFromDifferentSegmentWithExplicitSegment() {
		val commonRoutine = create.routine('commonRootCreate').input [
			model(EObject, 'affectedEObject')
		].create [
			vall('newRoot').create(Root)
		].update [
			addCorrespondenceBetween('newRoot').and.affectedEObject
		]

		val reactionsFile = create.reactionsFile('createRootTest')

		reactionsFile +=
			create.reactionsSegment('simpleChangesRootTests').inReactionToChangesIn(AllElementTypesPackage.eINSTANCE).
				executeActionsIn(AllElementTypesPackage.eINSTANCE) +=
				create.reaction('CreateRootTest').afterElement(Root).created.call(commonRoutine)

		val secondSegment = create.reactionsSegment('simpleChangesRoot2Tests').inReactionToChangesIn(AllElementTypes2Package.eINSTANCE).
			executeActionsIn(AllElementTypesPackage.eINSTANCE) +=
			create.reaction('CreateRoot2Test').afterElement(Root2).created.call(commonRoutine)

		secondSegment += commonRoutine

		reactionsFile += secondSegment

		val expectedReaction = '''
			import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
			
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
			import "http://tools.vitruv.change.testutils.metamodels.allElementTypes2" as allElementTypes2
			import "http://www.eclipse.org/emf/2002/Ecore" as ecore
			
			
			reactions: simpleChangesRootTests
			in reaction to changes in allElementTypes
			execute actions in allElementTypes
			
			reaction CreateRootTest {
				after element allElementTypes::Root created
				call commonRootCreate(affectedEObject)
			}
			
			
			reactions: simpleChangesRoot2Tests
			in reaction to changes in allElementTypes2
			execute actions in allElementTypes
			
			reaction CreateRoot2Test {
				after element allElementTypes2::Root2 created
				call commonRootCreate(affectedEObject)
			}
					
			routine commonRootCreate(ecore::EObject affectedEObject) {
				create {
					val newRoot = new allElementTypes::Root
				}
				update {
					addCorrespondenceBetween(newRoot, affectedEObject)
				}
			}
		'''

		assertThat(reactionsFile, builds(expectedReaction))
	}
}
