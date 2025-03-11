package tools.vitruv.dsls.commonalities.tests.execution;

import java.nio.file.Path;
import javax.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.TestCommonalitiesGenerator;
import tools.vitruv.testutils.TestProject;

@ExtendWith(InjectionExtension.class)
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("executing a commonality with attribute mapping operators")
@SuppressWarnings("all")
public class ReferenceMappingOperatorExecutionTest extends CommonalitiesExecutionTest {
  @Inject
  private TestCommonalitiesGenerator generator;

  @BeforeAll
  public void generate(@TestProject(variant = "commonalities") final Path testProject) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import tools.vitruv.dsls.commonalities.tests.operators.mock");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes2\" as AllElementTypes2");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept operators");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality WithReferenceMappingOperators {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:(Root in Resource)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes2:(Root2 in Resource)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has self referencing operators:WithReferenceMappingOperators {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes:Root.mock(ref Root.singleValuedEAttribute)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, CharSequence> _mappedTo = Pair.<String, CharSequence>of("WithReferenceMappingOperators.commonality", _builder.toString());
    this.generator.generate(testProject, _mappedTo);
  }

  @Override
  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return this.generator.createChangePropagationSpecifications();
  }

  @Test
  @DisplayName("generates")
  public void generates() {
  }
}
