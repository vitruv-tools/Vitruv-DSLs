package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import java.util.List;
import org.eclipse.xtext.common.types.JvmGenericType;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import org.eclipse.xtend2.lib.StringConcatenationClient;

public class EmptyStepExecutionClassGenerator extends StepExecutionClassGenerator {

	public EmptyStepExecutionClassGenerator
			(TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
		super(typesBuilderExtensionProvider);
	}

	@Override
  public boolean isEmpty() {
		return true;
	}

	@Override
	public StringConcatenationClient generateStepExecutionCode(
		StringConcatenationClient prefix,
		String executionStateAccessExpression,
		String routinesFacadeAccessExpression,
		Iterable<String> accessibleElementsAccessExpressions,
		StringConcatenationClient suffix
	) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation target) {
        target.append("// This execution step is empty");
      }
    };
  }

	@Override
	public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
		return List.of();
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
