package tools.vitruv.dsls.commonalities.generator.reactions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.IGlobalServiceProvider;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.SubGenerator;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.CommonalityAttributeChangeReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.ParticipationAttributeChangeReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.CommonalityInsertReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.MatchParticipationReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.MatchParticipationReferencesReactionsBuilder;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.reactions.api.generator.IReactionsGenerator;
import tools.vitruv.dsls.reactions.builder.FluentReactionsFileBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;

/**
 * Generates reactions in the Reactions language and the corresponding Java
 * code to transform changes between a commonality and its participations.
 */
@SuppressWarnings("all")
public class ReactionsGenerator implements SubGenerator {
  private static final Logger logger = Logger.getLogger(ReactionsGenerator.class);

  @Inject
  private IGlobalServiceProvider globalServiceProvider;

  @Inject
  private Provider<IReactionsGenerator> reactionsGeneratorProvider;

  @Inject
  @Extension
  private GenerationContext _generationContext;

  @Inject
  @Extension
  private ReactionsGenerationContext reactionsGenerationContext;

  @Inject
  private CommonalityInsertReactionsBuilder.Factory commonalityInsertReactionsBuilder;

  @Inject
  private CommonalityAttributeChangeReactionsBuilder.Factory commonalityAttributeChangeReactionsBuilder;

  @Inject
  private MatchParticipationReactionsBuilder.Factory matchParticipationReactionsBuilder;

  @Inject
  private MatchParticipationReferencesReactionsBuilder.Factory matchParticipationReferencesReactionsBuilder;

  @Inject
  private ParticipationAttributeChangeReactionsBuilder.Factory participationAttributeChangeReactionsBuilder;

  @Override
  public void beforeGenerate() {
    boolean _isNewResourceSet = this._generationContext.isNewResourceSet();
    if (_isNewResourceSet) {
      this.globalServiceProvider.<IReactionsGenerator>findService(URI.createFileURI("fake.reactions"), IReactionsGenerator.class);
    }
  }

  /**
   * Generates the reactions and corresponding Java code to transform changes
   * between a commonality and its participations.
   * <p>
   * We generate two reaction segments for each pair of a commonality and one
   * of its participations:
   * <ol>
   * <li>Commonality from participation: In reaction to changes in the
   * participation's domain, we perform corresponding actions in the
   * intermediate model.
   * <li>Commonality to participation: In reaction to changes regarding the
   * commonality, we perform corresponding actions with the participation's
   * objects.
   * </ol>
   */
  @Override
  public void generate() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Generating reactions for commonality ");
    Commonality _commonality = this._generationContext.getCommonality();
    _builder.append(_commonality);
    ReactionsGenerator.logger.debug(_builder);
    final FluentReactionsFileBuilder reactionsFile = this.generateReactions();
    boolean _willGenerateCode = reactionsFile.willGenerateCode();
    if (_willGenerateCode) {
      this.generateReactionsCode(reactionsFile);
    } else {
      ReactionsGenerator.logger.debug("  Ignoring empty commonality");
    }
  }

  private FluentReactionsFileBuilder generateReactions() {
    final FluentReactionsFileBuilder reactionsFile = this.reactionsGenerationContext.getCreate().reactionsFile(this._generationContext.getCommonality().getName());
    EList<Participation> _participations = this._generationContext.getCommonalityFile().getCommonality().getParticipations();
    for (final Participation participation : _participations) {
      {
        final FluentReactionsSegmentBuilder fromSegment = this.generateCommonalityFromParticipationSegment(participation);
        reactionsFile.operator_add(fromSegment);
        final FluentReactionsSegmentBuilder toSegment = this.generateCommonalityToParticipationSegment(participation);
        reactionsFile.operator_add(toSegment);
      }
    }
    return reactionsFile;
  }

  private void generateReactionsCode(final FluentReactionsFileBuilder reactionsFile) {
    try {
      IReactionsGenerator _get = this.reactionsGeneratorProvider.get();
      final Procedure1<IReactionsGenerator> _function = (IReactionsGenerator it) -> {
        it.useResourceSet(this._generationContext.getResourceSet());
      };
      final IReactionsGenerator reactionsGenerator = ObjectExtensions.<IReactionsGenerator>operator_doubleArrow(_get, _function);
      reactionsGenerator.addReactionsFile(reactionsFile);
      reactionsGenerator.generate(this._generationContext.getFsa());
      boolean _isCreateReactionFiles = this._generationContext.getSettings().isCreateReactionFiles();
      if (_isCreateReactionFiles) {
        reactionsGenerator.writeReactions(this._generationContext.getFsa());
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private FluentReactionsSegmentBuilder generateCommonalityFromParticipationSegment(final Participation participation) {
    final FluentReactionsSegmentBuilder segment = this.reactionsGenerationContext.getCreate().reactionsSegment(
      ReactionsGeneratorConventions.getReactionsSegmentFromParticipationToCommonalityName(this._generationContext.getCommonality(), participation)).inReactionToChangesIn(this._generationContext.getMetamodelRootPackage(CommonalitiesLanguageModelExtensions.getDomain(participation))).executeActionsIn(this._generationContext.getMetamodelRootPackage(this._generationContext.getCommonalityFile().getConcept()));
    this.generateParticipationChangeReactions(participation, segment);
    return segment;
  }

  private FluentReactionsSegmentBuilder generateCommonalityToParticipationSegment(final Participation participation) {
    final FluentReactionsSegmentBuilder segment = this.reactionsGenerationContext.getCreate().reactionsSegment(
      ReactionsGeneratorConventions.getReactionsSegmentFromCommonalityToParticipationName(this._generationContext.getCommonality(), participation)).inReactionToChangesIn(this._generationContext.getMetamodelRootPackage(this._generationContext.getCommonalityFile().getConcept())).executeActionsIn(this._generationContext.getMetamodelRootPackage(CommonalitiesLanguageModelExtensions.getDomain(participation)));
    this.generateCommonalityChangeReactions(participation, segment);
    return segment;
  }

  private void generateParticipationChangeReactions(final Participation participation, final FluentReactionsSegmentBuilder segment) {
    this.generateMatchParticipationReactions(participation, segment);
    this.generateMatchParticipationReferencesReactions(participation, segment);
    this.generateReactionsForParticipationAttributeChange(participation, segment);
  }

  private void generateCommonalityChangeReactions(final Participation participation, final FluentReactionsSegmentBuilder segment) {
    this.generateCommonalityInsertReactions(participation, segment);
    this.generateReactionsForCommonalityAttributeChange(participation, segment);
  }

  private void generateCommonalityInsertReactions(final Participation targetParticipation, final FluentReactionsSegmentBuilder segment) {
    this.commonalityInsertReactionsBuilder.createFor(targetParticipation).generateReactions(segment);
  }

  private void generateReactionsForCommonalityAttributeChange(final Participation targetParticipation, final FluentReactionsSegmentBuilder segment) {
    final Consumer<CommonalityAttribute> _function = (CommonalityAttribute attribute) -> {
      this.commonalityAttributeChangeReactionsBuilder.createFor(attribute, targetParticipation).generateReactions(segment);
    };
    this._generationContext.getCommonality().getAttributes().forEach(_function);
  }

  private void generateMatchParticipationReactions(final Participation participation, final FluentReactionsSegmentBuilder segment) {
    this.matchParticipationReactionsBuilder.createFor(participation).generateReactions(segment);
  }

  private void generateMatchParticipationReferencesReactions(final Participation participation, final FluentReactionsSegmentBuilder segment) {
    this.matchParticipationReferencesReactionsBuilder.createFor(participation).generateReactions(segment);
  }

  private void generateReactionsForParticipationAttributeChange(final Participation participation, final FluentReactionsSegmentBuilder segment) {
    this.participationAttributeChangeReactionsBuilder.createFor(participation).generateReactions(segment);
  }
}
