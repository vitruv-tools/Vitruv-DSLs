package tools.vitruv.dsls.vitruvOCL.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * gather errors during compilation
 * all passes gahter found errors in this class instead of throwing exceptions allowing
 * - to collect multiple errors in one go
 * - distinction into severity-levels(error, warning, info)
 * 
 * 
 * @see CompileError single error with severity and position
 */
public class ErrorCollector {
    
    private final List<CompileError> errors = new ArrayList<>();
    
    public void add(int line, int column, String message, ErrorSeverity severity, String source) {
        errors.add(new CompileError(line, column, message, severity, source));
    }
    
    public void add(CompileError error) {
        errors.add(error);
    }
    
    public boolean hasErrors() {
        return errors.stream().anyMatch(e -> e.getSeverity() == ErrorSeverity.ERROR);
    }
    
    public List<CompileError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public int getErrorCount() {
        return (int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.ERROR).count();
    }
}