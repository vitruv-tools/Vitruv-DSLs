package tools.vitruv.dsls.vitruvOCL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class VitruvOCLFileParsingTest {
    
    @ParameterizedTest
    @ValueSource(strings = {
        "test-inputs/valid/simple.ocl",
        "test-inputs/valid/collections.ocl"
    })
    public void testParseValidFiles(String fileName) throws IOException {
        var tree = VitruvOCLParserTestUtils.parseFile(fileName);
        assertNotNull(tree);
    }
    
    @Test
    public void testParseComplexFile() throws IOException {
        var tree = VitruvOCLParserTestUtils.parseFile("test-inputs/valid/simple.ocl");
        assertNotNull(tree);
        // Weitere Assertions Ã¼ber die Struktur
    }
}
