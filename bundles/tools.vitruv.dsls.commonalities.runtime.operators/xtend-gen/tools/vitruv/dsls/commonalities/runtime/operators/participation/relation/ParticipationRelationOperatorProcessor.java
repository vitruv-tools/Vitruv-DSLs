package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation;

import java.util.List;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtend.lib.macro.CodeGenerationContext;
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.ValidationContext;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import tools.vitruv.dsls.commonalities.runtime.operators.ClassProcessor;
import tools.vitruv.dsls.commonalities.runtime.operators.OperatorNameProcessor;

@SuppressWarnings("all")
public final class ParticipationRelationOperatorProcessor implements ClassProcessor {
  @Delegate
  private final ClassProcessor processsor = new OperatorNameProcessor(ParticipationRelationOperator.class);

  public void doGenerateCode(final List<? extends ClassDeclaration> arg0, final CodeGenerationContext arg1) {
    this.processsor.doGenerateCode(arg0, arg1);
  }

  public void doRegisterGlobals(final List<? extends ClassDeclaration> arg0, final RegisterGlobalsContext arg1) {
    this.processsor.doRegisterGlobals(arg0, arg1);
  }

  public void doTransform(final List<? extends MutableClassDeclaration> arg0, final TransformationContext arg1) {
    this.processsor.doTransform(arg0, arg1);
  }

  public void doValidate(final List<? extends ClassDeclaration> arg0, final ValidationContext arg1) {
    this.processsor.doValidate(arg0, arg1);
  }
}
