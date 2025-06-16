package tools.vitruv.dsls.reactions.runtime.structure

import java.util.logging.Logger
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.io.IOException
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

abstract class AbstractLogStep {

    protected static final String FILE_LOG_NAME = "target/log-reactions.txt"
    protected static final boolean ENABLE_LOGGING = !Boolean.getBoolean("disable.reactions.logging")
    
    val ReactionExecutionState executionState
    val Logger fileLogger

    new(ReactionExecutionState executionState) {
        this.executionState = executionState
        this.fileLogger = initLogger()
    }

    private def Logger initLogger() {
    	if (!ENABLE_LOGGING) return null;
        val logger = Logger.getLogger("ReactionLogger")

        if (logger.getHandlers().length == 0) {
            try {
                val fh = new FileHandler(FILE_LOG_NAME, true)
                fh.level = Level.ALL
                fh.formatter = new Formatter() {
                    override format(LogRecord record) {
                        val timestamp = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a").format(new Date(record.getMillis))
                        var className = record.getSourceClassName
                        return String.format("%s  %s %s: %s%n", timestamp, className, record.getLevel, record.getMessage)
                    }
                }
                logger.addHandler(fh)
                logger.level = Level.ALL
            } catch (IOException e) {
                e.printStackTrace
            }
        }

        return logger
    }

    protected def log(String message, Level level) {
        fileLogger.log(level, message)
    }
}
