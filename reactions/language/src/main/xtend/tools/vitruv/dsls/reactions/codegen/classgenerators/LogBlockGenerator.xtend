package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmGenericType

import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtend2.lib.StringConcatenationClient
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.language.toplevelelements.LogBlock
import static com.google.common.base.Preconditions.checkNotNull
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.StepExecutionClassGenerator
import org.eclipse.xtext.common.types.JvmTypeReference
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

class LogBlockGenerator extends StepExecutionClassGenerator {
	
	static val LOG_MODELS_METHOD_NAME = "logAction"
	static val ROUTINES_FACADE_CLASS_PARAMETER_NAME = CALL_BLOCK_FACADE_PARAMETER_NAME

    val LogBlock logBlock
    val Iterable<AccessibleElement> accessibleElements
    val String qualifiedClassName
    var JvmGenericType generatedClass
	JvmTypeReference routinesFacadeClassReference
    
	new(
	    TypesBuilderExtensionProvider typesBuilderExtensionProvider,
	    String qualifiedClassName,
	    LogBlock logBlock,
	    JvmTypeReference routinesFacadeClassReference,
	    Iterable<AccessibleElement> accessibleElements
	) {
	    super(typesBuilderExtensionProvider)
	    checkNotNull(logBlock, "log block must not be null")
	    checkNotNull(accessibleElements, "accessible elements must not be null")
	    checkNotNull(routinesFacadeClassReference, "facade class reference must not be null")
	
	    this.logBlock = logBlock
	    this.accessibleElements = accessibleElements
	    this.qualifiedClassName = qualifiedClassName
	    this.routinesFacadeClassReference = routinesFacadeClassReference
	}

    override boolean isEmpty() {
        return logBlock === null
    }

    override generateEmptyClass() {
        this.generatedClass = logBlock.toClass(qualifiedClassName) [
            visibility = JvmVisibility.PRIVATE
            static = true
        ]
        return generatedClass
    }
    
	private def generateConstructor() {
		logBlock.toConstructor [
			val reactionExecutionStateParameter = generateParameter(new AccessibleElement("reactionExecutionState", ReactionExecutionState))
			parameters += reactionExecutionStateParameter
			//body = '''super(«reactionExecutionStateParameter.name»);'''
		]
	}

    override generateBody() {
        generatedClass => [
            //superTypes += typeRef(AbstractLogStep)
            members += generateConstructor()
            members += generateLogMethod(LOG_MODELS_METHOD_NAME)
            
        ]
    }

    def JvmOperation generateLogMethod(String methodName) {
        return logBlock.toMethod(methodName, typeRef(Void.TYPE)) [
            visibility = JvmVisibility.PRIVATE
            parameters += generateAccessibleElementsParameters(accessibleElements)
            val facadeParam = toParameter(ROUTINES_FACADE_CLASS_PARAMETER_NAME, routinesFacadeClassReference)
			facadeParam.annotations += annotationRef(Extension)
			parameters += facadeParam
            body = generateLogMethodBody(logBlock)
        ]
    }

    def StringConcatenationClient generateLogMethodBody(LogBlock logBlock) '''
        try {
            java.util.logging.Logger fileLogger = java.util.logging.Logger.getLogger("ReactionLogger");

            if (fileLogger.getHandlers().length == 0) {
                java.util.logging.FileHandler fh = new java.util.logging.FileHandler("target/log-reactions.txt", true);
                fh.setLevel(java.util.logging.Level.ALL);

                fh.setFormatter(new java.util.logging.Formatter() {
                    @Override
                    public String format(java.util.logging.LogRecord record) {
                        String timestamp = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm:ss a").format(new java.util.Date(record.getMillis()));
                        String className = record.getSourceClassName();
                        if (className.contains(".")) {
                            className = className.substring(className.lastIndexOf('.') + 1);
                        }
                        return String.format("%s  %s %s: %s%n", timestamp, className, record.getLevel(), record.getMessage());
                    }
                });

                fileLogger.addHandler(fh);
                fileLogger.setLevel(java.util.logging.Level.ALL);
            }

            StringBuilder logMessage = new StringBuilder("«logBlock.message»");

            «IF logBlock.details.size > 0»
                logMessage.append(" Details: {");
                «FOR logDetail : logBlock.details»
                    logMessage.append("«logDetail.key»=").append(String.valueOf(«logDetail.value.toGetterCall»)).append(", ");
                «ENDFOR»
                logMessage.setLength(logMessage.length() - 2);
                logMessage.append("}");
            «ENDIF»

            fileLogger.«logBlock.level.getName().toLowerCase()»(logMessage.toString());
        } catch (java.io.IOException e) {
            e.printStackTrace(); 
        }
    '''

    def String toGetterCall(String qualifiedName) {
        val parts = qualifiedName.split("\\.")
        if (parts.size == 1)
            return parts.get(0)
        return parts.head + parts.tail.map[name | ".get" + name.toFirstUpper + "()"].join("")
    }

	override generateStepExecutionCode(
	    StringConcatenationClient prefix,
	    String executionStateAccessExpression,
	    String routinesFacadeAccessExpression,
	    Iterable<String> accessibleElementsAccessExpressions,
	    StringConcatenationClient suffix
	) '''
	    «prefix»new «qualifiedClassName»(«executionStateAccessExpression»).«LOG_MODELS_METHOD_NAME»(« //
	    FOR argument : accessibleElementsAccessExpressions SEPARATOR ", " AFTER ", "»«argument»«ENDFOR»«routinesFacadeAccessExpression»);
	    «suffix»
	'''


    override getNewlyAccessibleElementsAfterExecution() {
		return emptyList
	}

	override getNewlyAccessibleElementsContainerType() {
		return null
	}
}
