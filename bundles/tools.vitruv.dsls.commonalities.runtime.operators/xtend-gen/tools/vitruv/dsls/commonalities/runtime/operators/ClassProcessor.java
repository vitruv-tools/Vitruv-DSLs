package tools.vitruv.dsls.commonalities.runtime.operators;

import org.eclipse.xtend.lib.macro.CodeGenerationParticipant;
import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.ValidationParticipant;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;

@SuppressWarnings("all")
public interface ClassProcessor extends RegisterGlobalsParticipant<ClassDeclaration>, TransformationParticipant<MutableClassDeclaration>, CodeGenerationParticipant<ClassDeclaration>, ValidationParticipant<ClassDeclaration> {
}
