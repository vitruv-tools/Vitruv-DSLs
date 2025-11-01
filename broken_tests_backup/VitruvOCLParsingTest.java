package test.java.tools.vitruv.dsls.vitruvOCL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.antlr.v4.runtime.tree.ParseTree;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VitruvOCLParsingTest {
    
    @Test
    @DisplayName("Should parse simple context declaration")
    public void testSimpleContext() {
        String input = "context Person inv: self.age > 0";
        ParseTree tree = VitruvOCLParserTestUtils.parseString(input);
        
        assertNotNull(tree);
        assertThat(tree.getChildCount()).isGreaterThan(0);
        // Weitere spezifische Assertions je nach deiner Grammar
    }
    
    @Test
    @DisplayName("Should parse context with qualified class name")
    public void testQualifiedContext() {
        String input = "context University::Student inv: self.age > 18";
        ParseTree tree = VitruvOCLParserTestUtils.parseString(input);
        
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
            ParseTree tree = VitruvOCLParserTestUtils.parseString(input);
            assertNotNull(tree);
        });
    }
    
    @Test
    @DisplayName("Should parse collection operations")
    public void testCollectionOperations() {
        String input = "context Course inv: self.students->select(s | s.age > 20)->size() > 0";
        ParseTree tree = VitruvOCLParserTestUtils.parseString(input);
        assertNotNull(tree);
    }
    
    @Test
    @DisplayName("Should fail on invalid syntax")
    public void testInvalidSyntax() {
        String input = "context Person inv self.age > 0"; // fehlendes ':'
        
        assertThatThrownBy(() -> VitruvOCLParserTestUtils.parseString(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Parse error");
    }
    
    @ParameterizedTest
    @CsvSource({
        "Simple navigation, context Person inv: self.name.size() > 0",
        "Nested navigation, context Person inv: self.address.city.name <> null",
        "Collection filter, context Course inv: self.students->select(s | s.grade > 1.0)->notEmpty()"
    })
    @DisplayName("Test various navigation patterns")
    public void testNavigationPatterns(String description, String oclExpression) {
        ParseTree tree = VitruvOCLParserTestUtils.parseString(oclExpression);
        assertNotNull(tree, "Failed to parse: " + description);
    }
}
