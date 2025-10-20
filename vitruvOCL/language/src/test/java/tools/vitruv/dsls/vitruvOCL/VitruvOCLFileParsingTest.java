package tools.vitruv.dsls.vitruvOCL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class VitruvOCLFileParsingTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test-inputs/valid/simple.ocl",
        "test-inputs/valid/collections.ocl"
    })
    public void testParseValidFiles(String fileName) throws IOException {
        String fullPath = "src/test/resources/" + fileName;
        CharStream cs = CharStreams.fromPath(Paths.get(fullPath));

        VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);

        ParseTree tree = parser.contextDeclCS();

        System.out.println("Parse Tree for file " + fileName + ":\n" +
                           VitruvOCLParserTestUtils.treeToString(tree, parser));

        assertNotNull(tree);
        assertTrue(tree.getChildCount() > 0, "Parse tree should not be empty");
    }

    @Test
    public void testParseComplexFile() throws IOException {
        String fileName = "test-inputs/valid/simple.ocl";
        String fullPath = "src/test/resources/" + fileName;
        CharStream cs = CharStreams.fromPath(Paths.get(fullPath));

        VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);

        ParseTree tree = parser.contextDeclCS();

        System.out.println("Parse Tree for complex file " + fileName + ":\n" +
                           VitruvOCLParserTestUtils.treeToString(tree, parser));

        assertNotNull(tree);
        assertTrue(tree.getChildCount() > 0, "Parse tree should not be empty");
        // Hier könnten weitere Assertions über die Struktur erfolgen
    }
}
