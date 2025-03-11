package tools.vitruv.dsls.reactions.generator;

import com.google.inject.Inject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.AbstractGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.IGeneratorContext;

/**
 * This generator is called by Xtext when compiling Reaction language
 * resources. When it’s called, reactions will already have been translated
 * to their JVM types. So this generator only handles the remaining tasks,
 * namely generating the environment for the reactions.
 */
@SuppressWarnings("all")
public class ReactionsLanguageGenerator extends AbstractGenerator {
  @Inject
  private IGenerator reactionGenerator;

  @Override
  public void doGenerate(final Resource input, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    this.reactionGenerator.doGenerate(input, fsa);
  }
}
