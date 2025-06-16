package tools.vitruv.dsls.reactions.generator

import com.google.inject.Inject
import com.google.inject.Provider
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.xbase.XbaseFactory
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator
import tools.vitruv.dsls.reactions.builder.FluentReactionsLanguageBuilder
import tools.vitruv.dsls.reactions.builder.TypeProvider
import tools.vitruv.dsls.reactions.tests.ReactionsLanguageInjectorProvider

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import allElementTypes.AllElementTypesPackage
import tools.vitruv.dsls.reactions.language.toplevelelements.LogLevel
import allElementTypes.Root

@ExtendWith(InjectionExtension)
@InjectWith(ReactionsLanguageInjectorProvider)
class ReactionsLogBlockGeneratorTest {

	@Inject Provider<InMemoryFileSystemAccess> fsaProvider
	@Inject Provider<IReactionsGenerator> generatorProvider
	@Inject Provider<XtextResourceSet> resourceSetProvider
	static val CHANGE_PROPAGATION_SPEC_NAME_SUFFIX = 'ChangePropagationSpecification'
	static val REACTION_NAME = 'TestReaction'
	static final String LOG_SEGMENT = "logTestReaction";
	static val ROUTINE_NAME = "MyRoutine"
    static val SEGMENT_NAME = "routineLogTest"



	private def createPrintlnStatement(TypeProvider typeProvider) {
		XbaseFactory.eINSTANCE.createXFeatureCall => [
			val type = typeProvider.findTypeByName(InputOutput.name) as JvmGenericType
			feature = type.members.filter(JvmOperation).filter[it.simpleName == 'println'].head
			explicitOperationCall = true
			it.featureCallArguments += XbaseFactory.eINSTANCE.createXStringLiteral() => [
				it.value = 'That\'s it';
			]
		]
	}

	private static def assertFilesForReaction(InMemoryFileSystemAccess fsa, String segmentName, String reactionName) {
		assertFilesForReactionWithoutChangePropagationSpecification(fsa, segmentName, reactionName)
		assertThat(fsa.allFiles.keySet,
			hasItem(endsWith(segmentName + '/' + segmentName.toFirstUpper + CHANGE_PROPAGATION_SPEC_NAME_SUFFIX + '.java')))
	}

	private static def assertFilesForReactionWithoutChangePropagationSpecification(InMemoryFileSystemAccess fsa, String segmentName, String reactionName) {
		assertThat(fsa.allFiles.keySet,
			hasItem(endsWith(segmentName + '/' + reactionName + 'Reaction.java')))
		assertThat(fsa.allFiles.keySet,
			hasItem(endsWith(segmentName + '/' + reactionName + 'RepairRoutine.java')))
	}

	
	   // Helper method: Create a reaction with logging
    def private createReactionWithLogBlock(String reactionName, String reactionsFileName) {
        val create = new FluentReactionsLanguageBuilder();
        var fileBuilder = create.reactionsFile(reactionsFileName);
        
        fileBuilder += create.reactionsSegment(reactionsFileName)
            .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
            .executeActionsIn(AllElementTypesPackage.eINSTANCE)
            += create.reaction(reactionName)
                .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
                .log(LogLevel.INFO, "Logging triggered")
                .call [
                    update [
                        execute[createPrintlnStatement]
                    ]
                ];

        return fileBuilder;
    }
    
    
    @Test
    def testGenerateReactionsWithLogBlock() {
        var generator = generatorProvider.get();
        generator.useResourceSet(resourceSetProvider.get());

        // Step 1: Setup in-memory file system
        var fsa = fsaProvider.get();
        fsa.currentSource = "src";
        fsa.outputPath = "src-gen";

        // Step 2: Add reactions (including log block)
        generator.addReactionsFile(createReactionWithLogBlock(REACTION_NAME, LOG_SEGMENT));

        // Step 3: Generate the reaction file
        generator.generate(fsa);

        // Step 4: Validate that the files are generated correctly
        fsa.assertFilesForReaction(LOG_SEGMENT, REACTION_NAME);

		 // Step 5: Extract generated reaction file content
		val generatedReactionFileName = fsa.allFiles.entrySet.findFirst [
		    key.endsWith(LOG_SEGMENT + '/' + REACTION_NAME + 'Reaction.java')
		].key

		// Step 6: Retrieve the actual Java code content
		val reactionCode = fsa.allFiles.get(generatedReactionFileName).toString();

        // Step 6: Ensure logAction method is present
        assertThat(reactionCode, containsString("logAction"));
        assertThat(reactionCode, containsString("INFO"));
        assertThat(reactionCode, containsString("Logging triggered"));
    }
    
	def private createReactionWithLogBeforeMatch(String reactionName, String reactionsFileName, LogLevel level, String message) {
	    val create = new FluentReactionsLanguageBuilder
	    val fileBuilder = create.reactionsFile(reactionsFileName)
	
	    fileBuilder += create.reactionsSegment(reactionsFileName)
	        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	        .executeActionsIn(AllElementTypesPackage.eINSTANCE)
	        += create.reaction(reactionName)
	            .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
	            .call [
	                alwaysRequireAffectedEObject()
	                logBlockBeforeMatch(level, message)
	                match [
	                    vall("matched").retrieve(AllElementTypesPackage.eINSTANCE.root).correspondingTo("affectedEObject")
	                ]
	                update []
	            ]
	
	    return fileBuilder
	}
	
	@Test
	def testGenerateReactionWithLogBeforeMatch() {
	    val generator = generatorProvider.get()
	    generator.useResourceSet(resourceSetProvider.get())
	
	    val fsa = fsaProvider.get()
	    fsa.currentSource = "src"
	    fsa.outputPath = "src-gen"
	
	    generator.addReactionsFile(createReactionWithLogBeforeMatch("LogBeforeMatch", "logSegment", LogLevel.INFO, "Before matching started"))
	    generator.generate(fsa)
	
	    fsa.assertFilesForReaction("logSegment", "LogBeforeMatch")
	    
	    println("Generated files:") 
	    fsa.allFiles.keySet.forEach[ println(it) ]
	    
	
	    val generatedFile = fsa.allFiles.entrySet.findFirst [
	        key.endsWith("logSegment/LogBeforeMatchRepairRoutine.java")
	    ].key
	    val code = fsa.allFiles.get(generatedFile).toString
	
	    assertThat(code, containsString("logAction"))
	    assertThat(code, containsString("INFO"))
	    assertThat(code, containsString("Before matching started"))
	}
	
	def private createReactionWithLogBeforeCreate(String reactionName, String reactionsFileName, LogLevel level, String message) {
	    val create = new FluentReactionsLanguageBuilder
	    val fileBuilder = create.reactionsFile(reactionsFileName)
	
	    fileBuilder += create.reactionsSegment(reactionsFileName)
	        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	        .executeActionsIn(AllElementTypesPackage.eINSTANCE)
	        += create.reaction(reactionName)
	            .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
	            .call [
	                alwaysRequireAffectedEObject()
	                logBlockBeforeCreate(level, message)
	                create [
	                    vall("newRoot").create(AllElementTypesPackage.eINSTANCE.root)
	                ]
	                update [
	                    addCorrespondenceBetween("newRoot").and.affectedEObject
	                ]
	            ]
	
	    return fileBuilder
	}
	
	def private createReactionWithLogBeforeUpdate(String reactionName, String reactionsFileName, LogLevel level, String message) {
	    val create = new FluentReactionsLanguageBuilder	
	    val fileBuilder = create.reactionsFile(reactionsFileName)
	
	    fileBuilder += create.reactionsSegment(reactionsFileName)
	        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	        .executeActionsIn(AllElementTypesPackage.eINSTANCE)
	        += create.reaction(reactionName)
	            .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
	            .call [
	                alwaysRequireAffectedEObject()
	                create [
	                    vall("newRoot").create(AllElementTypesPackage.eINSTANCE.root)
	                ]
	                logBlockBeforeUpdate(level, message)
	                update [
	                    addCorrespondenceBetween("newRoot").and.affectedEObject
	                ]
	            ]
	
	    return fileBuilder
	}
	
	def private createReactionWithLogAfterUpdate(String reactionName, String reactionsFileName, LogLevel level, String message) {
	    val create = new FluentReactionsLanguageBuilder
	    val fileBuilder = create.reactionsFile(reactionsFileName)
	
	    fileBuilder += create.reactionsSegment(reactionsFileName)
	        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	        .executeActionsIn(AllElementTypesPackage.eINSTANCE)
	        += create.reaction(reactionName)
	            .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
	            .call [
	                alwaysRequireAffectedEObject()
	                create [
	                    vall("newRoot").create(AllElementTypesPackage.eINSTANCE.root)
	                ]
	                update [
	                    addCorrespondenceBetween("newRoot").and.affectedEObject
	                ]
	                logBlockAfterUpdate(level, message)
	            ]
	
	    return fileBuilder
	}
	
	def private createReactionWithAllLogBlocks(String reactionName, String reactionsFileName,
	    LogLevel beforeMatchLevel, String beforeMatchMsg,
	    LogLevel beforeCreateLevel, String beforeCreateMsg,
	    LogLevel beforeUpdateLevel, String beforeUpdateMsg,
	    LogLevel afterUpdateLevel, String afterUpdateMsg) {
	
	    val create = new FluentReactionsLanguageBuilder
	    val fileBuilder = create.reactionsFile(reactionsFileName)
	
	    fileBuilder += create.reactionsSegment(reactionsFileName)
	        .inReactionToChangesIn(AllElementTypesPackage.eINSTANCE)
	        .executeActionsIn(AllElementTypesPackage.eINSTANCE)
	        += create.reaction(reactionName)
	            .afterElement(AllElementTypesPackage.eINSTANCE.root).created()
	            .call [
	                alwaysRequireAffectedEObject()
	                logBlockBeforeMatch(beforeMatchLevel, beforeMatchMsg)
	                match [
	                    vall("newRoot").retrieve(AllElementTypesPackage.eINSTANCE.root).correspondingTo("affectedEObject")
	                ]
	                logBlockBeforeCreate(beforeCreateLevel, beforeCreateMsg)
	                create []
	                logBlockBeforeUpdate(beforeUpdateLevel, beforeUpdateMsg)
	                update [
	                    addCorrespondenceBetween("newRoot").and.affectedEObject
	                ]
	                logBlockAfterUpdate(afterUpdateLevel, afterUpdateMsg)
	            ]
	
	    return fileBuilder
	}
	


	@Test
	def testGenerateReactionWithLogBeforeCreate() {
	    val generator = generatorProvider.get()
	    generator.useResourceSet(resourceSetProvider.get())
	
	    val fsa = fsaProvider.get()
	    fsa.currentSource = "src"
	    fsa.outputPath = "src-gen"
	
	    generator.addReactionsFile(createReactionWithLogBeforeCreate("LogBeforeCreate", "logSegment", LogLevel.INFO, "Preparing to create element"))
	    generator.generate(fsa)
	
	    fsa.assertFilesForReaction("logSegment", "LogBeforeCreate")
	
	    val generatedFile = fsa.allFiles.entrySet.findFirst [
	        key.endsWith("logSegment/LogBeforeCreateRepairRoutine.java")
	    ].key
	    val code = fsa.allFiles.get(generatedFile).toString
	
	    assertThat(code, containsString("logAction"))
	    assertThat(code, containsString("INFO"))
	    assertThat(code, containsString("Preparing to create element"))
	}

	@Test
	def testGenerateReactionWithLogBeforeUpdate() {
	    val generator = generatorProvider.get()
	    generator.useResourceSet(resourceSetProvider.get())
	
	    val fsa = fsaProvider.get()
	    fsa.currentSource = "src"
	    fsa.outputPath = "src-gen"
	
	    generator.addReactionsFile(createReactionWithLogBeforeUpdate("LogBeforeUpdate", "logSegment", LogLevel.INFO, "Starting update of correspondences"))
	    generator.generate(fsa)
	
	    fsa.assertFilesForReaction("logSegment", "LogBeforeUpdate")
	
	    val generatedFile = fsa.allFiles.entrySet.findFirst [
	        key.endsWith("logSegment/LogBeforeUpdateRepairRoutine.java")
	    ].key
	    val code = fsa.allFiles.get(generatedFile).toString
	
	    assertThat(code, containsString("logAction"))
	    assertThat(code, containsString("INFO"))
	    assertThat(code, containsString("Starting update of correspondences"))
	}
	
	@Test
	def testGenerateReactionWithLogAfterUpdate() {
	    val generator = generatorProvider.get()
	    generator.useResourceSet(resourceSetProvider.get())
	
	    val fsa = fsaProvider.get()
	    fsa.currentSource = "src"
	    fsa.outputPath = "src-gen"
	
	    generator.addReactionsFile(createReactionWithLogAfterUpdate("LogAfterUpdate", "logSegment", LogLevel.INFO, "Update completed"))
	    generator.generate(fsa)
	
	    fsa.assertFilesForReaction("logSegment", "LogAfterUpdate")
	
	    val generatedFile = fsa.allFiles.entrySet.findFirst [
	        key.endsWith("logSegment/LogAfterUpdateRepairRoutine.java")
	    ].key
	    val code = fsa.allFiles.get(generatedFile).toString
	
	    assertThat(code, containsString("logAction"))
	    assertThat(code, containsString("INFO"))
	    assertThat(code, containsString("Update completed"))
	}
	
	@Test
	def testGenerateReactionWithAllLogBlocks() {
	    val generator = generatorProvider.get()
	    generator.useResourceSet(resourceSetProvider.get())
	
	    val fsa = fsaProvider.get()
	    fsa.currentSource = "src"
	    fsa.outputPath = "src-gen"
	
	    generator.addReactionsFile(
	        createReactionWithAllLogBlocks(
	            "LogAllBlocks",
	            "logSegment",
	            LogLevel.INFO, "Start matching",
	            LogLevel.INFO, "Matching done",
	            LogLevel.INFO, "Start updating",
	            LogLevel.INFO, "Update done"
	        )
	    )
	    generator.generate(fsa)
	
	    fsa.assertFilesForReaction("logSegment", "LogAllBlocks")
	
	    val generatedFile = fsa.allFiles.entrySet.findFirst [
	        key.endsWith("logSegment/LogAllBlocksRepairRoutine.java")
	    ].key
	    val code = fsa.allFiles.get(generatedFile).toString
	
	    assertThat(code, containsString("logAction"))
	    assertThat(code, containsString("Start matching"))
	    assertThat(code, containsString("Matching done"))
	    assertThat(code, containsString("Start updating"))
	    assertThat(code, containsString("Update done"))
	}
}
