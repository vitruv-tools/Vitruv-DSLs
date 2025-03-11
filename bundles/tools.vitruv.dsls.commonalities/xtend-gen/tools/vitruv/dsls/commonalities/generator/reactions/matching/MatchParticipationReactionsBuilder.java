package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsSubGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;

/**
 * Generates the matching reaction and routines for a participation, in its own
 * specified context.
 */
@SuppressWarnings("all")
public class MatchParticipationReactionsBuilder extends ReactionsSubGenerator {
  public static class Factory extends InjectingFactoryBase {
    public MatchParticipationReactionsBuilder createFor(final Participation participation) {
      return this.<MatchParticipationReactionsBuilder>injectMembers(new MatchParticipationReactionsBuilder(participation));
    }
  }

  private static final Logger logger = Logger.getLogger(MatchParticipationReactionsBuilder.class);

  @Inject
  private ParticipationMatchingReactionsBuilder.Provider participationMatchingReactionsBuilderProvider;

  private final Participation participation;

  private MatchParticipationReactionsBuilder(final Participation participation) {
    Preconditions.<Participation>checkNotNull(participation, "participation is null");
    this.participation = participation;
  }

  MatchParticipationReactionsBuilder() {
    this.participation = null;
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  public void generateReactions(final FluentReactionsSegmentBuilder segment) {
    boolean _isReferenced = this._generationContext.getCommonality().isReferenced();
    if (_isReferenced) {
      return;
    }
    final Optional<ParticipationContext> participationContext = ParticipationContextHelper.getParticipationContext(this.participation);
    boolean _isPresent = participationContext.isPresent();
    boolean _not = (!_isPresent);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Commonality ");
      Commonality _commonality = this._generationContext.getCommonality();
      _builder.append(_commonality);
      _builder.append(": Found no own participation context for participation ");
      String _name = this.participation.getName();
      _builder.append(_name);
      MatchParticipationReactionsBuilder.logger.debug(_builder);
      return;
    }
    @Extension
    final ParticipationMatchingReactionsBuilder matchingReactionsBuilder = this.participationMatchingReactionsBuilderProvider.getFor(segment);
    matchingReactionsBuilder.generateMatchingReactions(participationContext.get());
  }
}
