package tools.vitruv.dsls.vitruvOCL.common;

/**
 * compile and runtime error with position and severity
 * is Immutable Value Object
 * 
 */
public class CompileError {
    
    private final int line;
    private final int column;
    private final String message;
    private final ErrorSeverity severity;
    private final String source;
    private final String errorCode;
    
    public CompileError(int line, int column, String message, ErrorSeverity severity, String source) {
        this(line, column, message, severity, source, null);
    }
    
    public CompileError(int line, int column, String message, ErrorSeverity severity, String source, String errorCode) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.severity = severity;
        this.source = source;
        this.errorCode = errorCode;
    }
    
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getMessage() { return message; }
    public ErrorSeverity getSeverity() { return severity; }
    public String getSource() { return source; }
    public String getErrorCode() { return errorCode; }
}