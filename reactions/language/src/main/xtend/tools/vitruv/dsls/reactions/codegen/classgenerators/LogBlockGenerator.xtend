package tools.vitruv.dsls.reactions.codegen.classgenerators

import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtend2.lib.StringConcatenationClient
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider
import tools.vitruv.dsls.reactions.language.toplevelelements.LogBlock
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement


class LogBlockGenerator extends ClassGenerator {

    val LogBlock logBlock
    val Iterable<AccessibleElement> accessibleElements

    new(LogBlock logBlock, TypesBuilderExtensionProvider typesBuilderExtensionProvider, Iterable<AccessibleElement> accessibleElements) {
        super(typesBuilderExtensionProvider)
        this.logBlock = logBlock
        this.accessibleElements = accessibleElements
    }

    def JvmOperation generateLogMethod(String methodName) {
    	
        logBlock.toMethod(methodName, typeRef(Void.TYPE)) [
        	//val changeParameter = generateParameter(new AccessibleElement("change", EChange))
            visibility = JvmVisibility.PRIVATE
           parameters += generateAccessibleElementsParameters(accessibleElements)
            body = generateLogMethodBody(logBlock)
        ]
    }

    def StringConcatenationClient generateLogMethodBody(LogBlock logBlock) {
return '''
    try {
        java.util.logging.Logger fileLogger = java.util.logging.Logger.getLogger("ReactionLogger");

        if (fileLogger.getHandlers().length == 0) {
            java.util.logging.FileHandler fh = new java.util.logging.FileHandler("target/log-reactions.txt", true);
            fh.setLevel(java.util.logging.Level.ALL);

            // Custom formatter for desired format
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

   }
   
def String toGetterCall(String qualifiedName) {
  val parts = qualifiedName.split("\\.")
  if (parts.size == 1)
    return parts.get(0)
  return parts.head + parts.tail.map[name | ".get" + name.toFirstUpper + "()"].join(".")
}
   		
override generateEmptyClass() {
	return null;
}
				
override generateBody() {
	return null;
}
				
}
