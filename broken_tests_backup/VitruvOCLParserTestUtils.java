package tools.vitruv.dsls.vitruvOCL;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VitruvOCLParserTestUtils {
    
    /**
     * Parse a VitruvOCL string and return the parse tree
     */
    public static ParseTree parseString(String input) {
        CharStream cs = CharStreams.fromString(input);
        return parse(cs);
    }
    
    /**
     * Parse a VitruvOCL file and return the parse tree
     */
    public static ParseTree parseFile(String fileName) throws IOException {
        String fullPath = "src/test/resources/" + fileName;
        CharStream cs = CharStreams.fromPath(Paths.get(fullPath));
        return parse(cs);
    }
    
    private static ParseTree parse(CharStream cs) {
        VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);
        
        // Error handling
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                   int line, int charPositionInLine,
                                   String msg, RecognitionException e) {
                throw new IllegalStateException("Parse error at " + line + ":" + charPositionInLine + " - " + msg, e);
            }
        });
        
        return parser.contextDeclCS();
    }
    
    /**
     * Get a pretty-printed string representation of the parse tree
     */
    public static String treeToString(ParseTree tree, Parser parser) {
        return tree.toStringTree(parser);
    }
}
