package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;

@SuppressWarnings("all")
public class EmptyStepExecutionClassGenerator extends StepExecutionClassGenerator {
  public EmptyStepExecutionClassGenerator(final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(final StringConcatenationClient prefix, final String executionStateAccessExpression, final String routinesFacadeAccessExpression, final Iterable<String> accessibleElementsAccessExpressions, final StringConcatenationClient suffix) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("// This execution step is empty");
      }
    };
    return _client;
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    return CollectionLiterals.<AccessibleElement>emptyList();
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return null;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    return null;
  }

  @Override
  public JvmGenericType generateBody() {
    return null;
  }
}
