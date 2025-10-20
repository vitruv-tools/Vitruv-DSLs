package tools.vitruv.dsls.vitruvOCL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VitruvOCLParsingTest {

    @Test
    @DisplayName("Should parse simple context declaration")
    public void testSimpleContext() {
        String input = "context Person inv: self.age > 0";
        VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);

        ParseTree tree = parser.contextDeclCS();

        System.out.println("Parse Tree:\n" + VitruvOCLParserTestUtils.treeToString(tree, parser));

        assertNotNull(tree);
        assertThat(tree.getChildCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should parse context with qualified class name")
    public void testQualifiedContext() {
        String input = "context University::Student inv: self.age > 18";
        VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);

        ParseTree tree = parser.contextDeclCS();

        System.out.println("Parse Tree:\n" + VitruvOCLParserTestUtils.treeToString(tree, parser));

        assertNotNull(tree);
    }

    @ParameterizedTest(name = "Should parse: {0}")
    @ValueSource(strings = {
        "context Person inv: self.age > 0",
        "context Student inv: self.name <> null",
        "context Course inv: self.students->size() > 0",
        "context Person::allInstances()->select(p | p.age > 18)"
    })
    @DisplayName("Should parse various valid OCL expressions")
    public void testValidExpressions(String input) {
        assertDoesNotThrow(() -> {
            VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            VitruvOCLParser parser = new VitruvOCLParser(tokens);

            ParseTree tree = parser.contextDeclCS();

            System.out.println("Parse Tree for: " + input);
            System.out.println("is:   " + VitruvOCLParserTestUtils.treeToString(tree, parser));

            assertNotNull(tree);
        });
    }

    // Andere Tests wie testCollectionOperations, testNavigationPatterns können analog angepasst werden
}
