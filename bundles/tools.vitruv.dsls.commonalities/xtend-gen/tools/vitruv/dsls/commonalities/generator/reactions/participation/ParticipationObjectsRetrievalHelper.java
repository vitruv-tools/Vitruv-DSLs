package tools.vitruv.dsls.commonalities.generator.reactions.participation;

import java.util.function.Function;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationObjectsRetrievalHelper extends ReactionsGenerationHelper {
  ParticipationObjectsRetrievalHelper() {
  }

  public void retrieveUnassertedParticipationObject(@Extension final FluentRoutineBuilder.UndecidedMatchStatementBuilder matcherBuilder, final ParticipationClass participationClass, final Function<TypeProvider, XExpression> correspondenceSource) {
    this.retrieveParticipationObject(matcherBuilder, participationClass, false, correspondenceSource);
  }

  public void retrieveAssertedParticipationObject(@Extension final FluentRoutineBuilder.UndecidedMatchStatementBuilder matcherBuilder, final ParticipationClass participationClass, final Function<TypeProvider, XExpression> correspondenceSource) {
    this.retrieveParticipationObject(matcherBuilder, participationClass, true, correspondenceSource);
  }

  private void retrieveParticipationObject(@Extension final FluentRoutineBuilder.UndecidedMatchStatementBuilder matcherBuilder, final ParticipationClass participationClass, final boolean asserted, final Function<TypeProvider, XExpression> correspondenceSource) {
    boolean _isRootClass = ParticipationContextHelper.isRootClass(participationClass);
    if (_isRootClass) {
      matcherBuilder.vall(ReactionsGeneratorConventions.correspondingVariableName(participationClass)).retrieveOptional(this._generationContext.getChangeClass(participationClass)).correspondingTo(correspondenceSource).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
    } else {
      final FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementBuilder vall = matcherBuilder.vall(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder _xifexpression = null;
      if (asserted) {
        _xifexpression = vall.retrieveAsserted(this._generationContext.getChangeClass(participationClass));
      } else {
        _xifexpression = vall.retrieve(this._generationContext.getChangeClass(participationClass));
      }
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieval = _xifexpression;
      retrieval.correspondingTo(correspondenceSource).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
    }
  }
}
