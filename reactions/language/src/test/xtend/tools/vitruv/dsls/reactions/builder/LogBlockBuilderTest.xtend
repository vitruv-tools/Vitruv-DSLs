package tools.vitruv.dsls.reactions.builder

import static org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import allElementTypes.AllElementTypesPackage
import tools.vitruv.dsls.reactions.language.toplevelelements.LogLevel

class LogBlockBuilderTest extends FluentReactionsBuilderTest {

	@Test
	def void testLogBlockCreation() {
	    val builder = create.reactionsFile('logTestFile') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogTestReaction')
	                .afterElement(Root).created()  // Specify the affected element (Root)
	                .log(LogLevel.INFO, 'Logging triggered') // Log before calling routine
	                .call [
	                    create [
	                        vall('newRoot').create(Root)
	                    ].update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject	
	                    ]
	                ]
	
	    val reactionResult = '''
	        import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update
	        
	        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
	        
	        reactions: logSegment
	        in reaction to changes in allElementTypes
	        execute actions in allElementTypes
	        
	        reaction LogTestReaction {
	            after element allElementTypes::Root created
	            log {
	                level INFO
	                message "Logging triggered"
	            }
	            call logTestReactionRepair(affectedEObject)
	        }
	        
	        routine logTestReactionRepair(allElementTypes::Root affectedEObject) {
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
    def void testLogBeforeMatchInRoutine() {
        val builder = create.reactionsFile('logRoutineMatch') +=
            create.reactionsSegment('logSegment')
                .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
                .executeActionsIn(AllElementTypesPackage.eINSTANCE) +=
            create.reaction('LogRoutineReaction')
                .afterElement(Root).created()
                .call [
                	alwaysRequireAffectedEObject()
                    logBlockBeforeMatch(LogLevel.INFO, "Before matching started")
                    match [
                        vall('matched').retrieve(Root).correspondingTo('affectedEObject')
                    ]
                    update[]
                ]

        val expected = '''
		    import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes
		    
		    reactions: logSegment
		    in reaction to changes in allElementTypes
		    execute actions in allElementTypes

		    reaction LogRoutineReaction {
		        after element allElementTypes::Root created
		        call logRoutineReactionRepair(affectedEObject)
		    }

		    routine logRoutineReactionRepair(allElementTypes::Root affectedEObject) {
		        log {
		            level INFO
		            message "Before matching started"
		        }
		        match {
		            val matched = retrieve allElementTypes::Root corresponding to affectedEObject
		        }
		        update {
		        }
		    }
        '''


        assertThat(builder, builds(expected))
    }

   @Test
	def void testLogAfterUpdateInRoutine() {
	    val builder = create.reactionsFile('logRoutineUpdate') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogAfterUpdateReaction')
	                .afterElement(Root).created()
	                .call [
	                	alwaysRequireAffectedEObject()
	                    create [
	                        vall('newRoot').create(Root)
	                    ]
	                    update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject
	                    ]
	                    logBlockAfterUpdate(LogLevel.INFO, "Update completed")
	                ]
	
	    val expected = '''
	        import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update

	        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

	        reactions: logSegment
	        in reaction to changes in allElementTypes
	        execute actions in allElementTypes

	        reaction LogAfterUpdateReaction {
	            after element allElementTypes::Root created
	            call logAfterUpdateReactionRepair(affectedEObject)
	        }

	        routine logAfterUpdateReactionRepair(allElementTypes::Root affectedEObject) {
	            create {
	                val newRoot = new allElementTypes::Root
	            }
	            update {
	                addCorrespondenceBetween(newRoot, affectedEObject)
	            }
	            log {
	                level INFO
	                message "Update completed"
	            }
	        }
	    '''
	
	    assertThat(builder, builds(expected))
	}
	
	@Test
	def void testLogBeforeCreateInRoutine() {
	    val builder = create.reactionsFile('logBeforeCreateFile') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogBeforeCreateReaction')
	                .afterElement(Root).created()
	                .call [
	                	alwaysRequireAffectedEObject()
	                    logBlockBeforeCreate(LogLevel.INFO, "Preparing to create element")
	                    create [
	                        vall('newRoot').create(Root)
	                    ]
	                    update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject
	                    ]
	                ]
	
	    val expected = '''
	        import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update

	        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

	        reactions: logSegment
	        in reaction to changes in allElementTypes
	        execute actions in allElementTypes

	        reaction LogBeforeCreateReaction {
	            after element allElementTypes::Root created
	            call logBeforeCreateReactionRepair(affectedEObject)
	        }
	
	        routine logBeforeCreateReactionRepair(allElementTypes::Root affectedEObject) {
	            log {
	                level INFO
	                message "Preparing to create element"
	            }
	            create {
	                val newRoot = new allElementTypes::Root
	            }
	            update {
	                addCorrespondenceBetween(newRoot, affectedEObject)
	            }
	        }
	    '''
	
	    assertThat(builder, builds(expected))
	}
	
	@Test
	def void testLogBeforeUpdateInRoutine() {
	    val builder = create.reactionsFile('logBeforeUpdateFile') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogBeforeUpdateReaction')
	                .afterElement(Root).created()
	                .call [
	                	alwaysRequireAffectedEObject()
	                    create [
	                        vall('newRoot').create(Root)
	                    ]
	                    logBlockBeforeUpdate(LogLevel.INFO, "Starting update of correspondences")
	                    update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject
	                    ]
	                ]
	
	    val expected = '''
	        import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update

	        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

	        reactions: logSegment
	        in reaction to changes in allElementTypes
	        execute actions in allElementTypes

	        reaction LogBeforeUpdateReaction {
	            after element allElementTypes::Root created
	            call logBeforeUpdateReactionRepair(affectedEObject)
	        }

	        routine logBeforeUpdateReactionRepair(allElementTypes::Root affectedEObject) {
	            create {
	                val newRoot = new allElementTypes::Root
	            }
	            log {
	                level INFO
	                message "Starting update of correspondences"
	            }
	            update {
	                addCorrespondenceBetween(newRoot, affectedEObject)
	            }
	        }
	    '''
	
	    assertThat(builder, builds(expected))
	}
	
	@Test
	def void testLogBeforeCreateAndBeforeUpdateInRoutine() {
	    val builder = create.reactionsFile('logBeforeCreateUpdateFile') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogBeforeCreateUpdateReaction')
	                .afterElement(Root).created()
	                .call [
	                	alwaysRequireAffectedEObject()
	                    logBlockBeforeCreate(LogLevel.INFO, "Prepare for creation")
	                    create [
	                        vall('newRoot').create(Root)
	                    ]
	                    logBlockBeforeUpdate(LogLevel.INFO, "Prepare for update")
	                    update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject
	                    ]
	                ]
	
	    val expected = '''
	        import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update

	        import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

	        reactions: logSegment
	        in reaction to changes in allElementTypes
	        execute actions in allElementTypes

	        reaction LogBeforeCreateUpdateReaction {
	            after element allElementTypes::Root created
	            call logBeforeCreateUpdateReactionRepair(affectedEObject)
	        }

	        routine logBeforeCreateUpdateReactionRepair(allElementTypes::Root affectedEObject) {
	            log {
	                level INFO
	                message "Prepare for creation"
	            }
	            create {
	                val newRoot = new allElementTypes::Root
	            }
	            log {
	                level INFO
	                message "Prepare for update"
	            }
	            update {
	                addCorrespondenceBetween(newRoot, affectedEObject)
	            }
	        }
	    '''
	
	    assertThat(builder, builds(expected))
	}
	
	@Test
	def void testAllLogBlocksInRoutine() {
	    val builder = create.reactionsFile('logAllBlocksFile') +=
	        create.reactionsSegment('logSegment')
	            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	            .executeActionsIn(AllElementTypesPackage.eINSTANCE) += create.reaction('LogAllBlocksReaction')
	                .afterElement(Root).created()
	                .call [
	                	alwaysRequireAffectedEObject()
	                    logBlockBeforeMatch(LogLevel.INFO, "Start matching")
	                    match [
	                        vall('newRoot').retrieve(Root).correspondingTo('affectedEObject')
	                    ]
	                    logBlockBeforeCreate(LogLevel.INFO, "Matching done")
	                    create [
	                       
	                    ]
	                    logBlockBeforeUpdate(LogLevel.INFO, "Start updating")
	                    update [
	                        addCorrespondenceBetween('newRoot').and.affectedEObject
	                    ]
	                    logBlockAfterUpdate(LogLevel.INFO, "Update done")
	                ]
	
		val expected = '''
		    import tools.vitruv.dsls.reactions.runtime.^routines.AbstractRoutine.Update

		    import "http://tools.vitruv.change.testutils.metamodels.allElementTypes" as allElementTypes

		    reactions: logSegment
		    in reaction to changes in allElementTypes
		    execute actions in allElementTypes

		    reaction LogAllBlocksReaction {
		        after element allElementTypes::Root created
		        call logAllBlocksReactionRepair(affectedEObject)
		    }

		    routine logAllBlocksReactionRepair(allElementTypes::Root affectedEObject) {
		        log { level INFO message "Start matching" }
		        match {
		            val newRoot = retrieve allElementTypes::Root corresponding to affectedEObject
		        } log { level INFO message "Matching done" }
		        create {
		        } log { level INFO message "Start updating" }
		        update {
		            addCorrespondenceBetween(newRoot, affectedEObject)
		        } log { level INFO message "Update done" }
		    }
		'''

	    assertThat(builder, builds(expected))
	}
}