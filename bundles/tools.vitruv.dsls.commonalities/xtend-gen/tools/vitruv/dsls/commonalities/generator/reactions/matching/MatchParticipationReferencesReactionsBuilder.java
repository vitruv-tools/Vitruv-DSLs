package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsSubGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;

/**
 * Generates the matching reaction and routines for participations referenced
 * in commonality reference mappings.
 */
@SuppressWarnings("all")
public class MatchParticipationReferencesReactionsBuilder extends ReactionsSubGenerator {
  public static class Factory extends InjectingFactoryBase {
    public MatchParticipationReferencesReactionsBuilder createFor(final Participation participation) {
      return this.<MatchParticipationReferencesReactionsBuilder>injectMembers(new MatchParticipationReferencesReactionsBuilder(participation));
    }
  }

  @Inject
  private ParticipationMatchingReactionsBuilder.Provider participationMatchingReactionsBuilderProvider;

  private final Participation participation;

  private MatchParticipationReferencesReactionsBuilder(final Participation participation) {
    Preconditions.<Participation>checkNotNull(participation, "participation is null");
    this.participation = participation;
  }

  MatchParticipationReferencesReactionsBuilder() {
    this.participation = null;
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  public void generateReactions(final FluentReactionsSegmentBuilder segment) {
    @Extension
    final ParticipationMatchingReactionsBuilder matchingReactionsBuilder = this.participationMatchingReactionsBuilderProvider.getFor(segment);
    final Function1<CommonalityReference, Iterable<ParticipationContext>> _function = (CommonalityReference it) -> {
      return ParticipationContextHelper.getReferenceParticipationContexts(it);
    };
    final Function1<ParticipationContext, Boolean> _function_1 = (ParticipationContext it) -> {
      String _domainName = it.getParticipation().getDomainName();
      String _domainName_1 = this.participation.getDomainName();
      return Boolean.valueOf(Objects.equal(_domainName, _domainName_1));
    };
    final Consumer<ParticipationContext> _function_2 = (ParticipationContext it) -> {
      matchingReactionsBuilder.generateMatchingReactions(it);
    };
    IterableExtensions.<ParticipationContext>filter(IterableExtensions.<CommonalityReference, ParticipationContext>flatMap(this._generationContext.getCommonality().getReferences(), _function), _function_1).forEach(_function_2);
  }
}
