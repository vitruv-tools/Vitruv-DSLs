package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EcorePackage;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;

@SuppressWarnings("all")
public class DeleteObjectRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<DeleteObjectRoutineBuilder> {
    @Override
    protected DeleteObjectRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<DeleteObjectRoutineBuilder>injectMembers(new DeleteObjectRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getDeleteObjectRoutine(final FluentReactionsSegmentBuilder segment) {
      return this.getFor(segment).getDeleteObjectRoutine();
    }
  }

  private FluentRoutineBuilder deleteObjectRoutine = null;

  private DeleteObjectRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  DeleteObjectRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getDeleteObjectRoutine() {
    if ((this.deleteObjectRoutine == null)) {
      final Consumer<FluentRoutineBuilder.InputBuilder> _function = (FluentRoutineBuilder.InputBuilder it) -> {
        it.model(EcorePackage.Literals.EOBJECT, "object");
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_1 = (FluentRoutineBuilder.UpdateStatementBuilder it) -> {
        it.delete("object");
      };
      this.deleteObjectRoutine = this._reactionsGenerationContext.getCreate().routine("deleteObject").input(_function).update(_function_1);
    }
    return this.deleteObjectRoutine;
  }
}
