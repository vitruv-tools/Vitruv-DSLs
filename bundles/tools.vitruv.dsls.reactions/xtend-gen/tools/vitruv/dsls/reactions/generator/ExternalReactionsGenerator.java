package tools.vitruv.dsls.reactions.generator;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.resource.IGlobalServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.builder.FluentReactionsFileBuilder;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;

@SuppressWarnings("all")
public class ExternalReactionsGenerator implements IReactionsGenerator {
  private static Logger logger = Logger.getLogger(ExternalReactionsGenerator.class);

  @Inject
  private static Provider<InternalReactionsGenerator> internalGeneratorProvider;

  @Delegate
  private final InternalReactionsGenerator internalGenerator;

  @Inject
  ExternalReactionsGenerator(final IGlobalServiceProvider globalServiceProvider) {
    if ((ExternalReactionsGenerator.internalGeneratorProvider == null)) {
      ExternalReactionsGenerator.logger.warn("The Reactions language has not been setup yet. Performing setup now.");
      globalServiceProvider.<IReactionsGenerator>findService(URI.createFileURI("dummy.reactions"), IReactionsGenerator.class);
      if ((ExternalReactionsGenerator.internalGeneratorProvider == null)) {
        throw new IllegalStateException("Setup of the Reactions language has failed!");
      }
    }
    this.internalGenerator = ExternalReactionsGenerator.internalGeneratorProvider.get();
  }

  public void addReaction(final String sourceFileName, final Iterable<? extends Reaction> reactions) {
    this.internalGenerator.addReaction(sourceFileName, reactions);
  }

  public void addReaction(final String sourceFileName, final Reaction... reactions) {
    this.internalGenerator.addReaction(sourceFileName, reactions);
  }

  public void addReactionsFile(final FluentReactionsFileBuilder reactionBuilder) {
    this.internalGenerator.addReactionsFile(reactionBuilder);
  }

  public void addReactionsFile(final String sourceFileName, final ReactionsFile reactionsFile) {
    this.internalGenerator.addReactionsFile(sourceFileName, reactionsFile);
  }

  public void addReactionsFiles(final XtextResourceSet resourceSet) {
    this.internalGenerator.addReactionsFiles(resourceSet);
  }

  public void generate(final IFileSystemAccess2 fsa) {
    this.internalGenerator.generate(fsa);
  }

  public void useResourceSet(final ResourceSet resourceSet) {
    this.internalGenerator.useResourceSet(resourceSet);
  }

  public void writeReactions(final IFileSystemAccess2 fsa) throws IOException {
    this.internalGenerator.writeReactions(fsa);
  }

  public void writeReactions(final IFileSystemAccess2 fsa, final String subPath) throws IOException {
    this.internalGenerator.writeReactions(fsa, subPath);
  }
}
