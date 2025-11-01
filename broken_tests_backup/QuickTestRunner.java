package tools.vitruv.dsls.vitruvOCL; // <-- matcht das Verzeichnis unter target/generated-sources

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class QuickTestRunner {
    public static void main(String[] args) throws Exception {
        String input = "context Person inv: self.age > 0";
        CharStream cs = CharStreams.fromString(input);

        // fully-qualified names entsprechen dem Package der generierten Klassen
        VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);

        ParseTree tree = parser.contextDeclCS();
        System.out.println(tree.toStringTree(parser));
    }
}
