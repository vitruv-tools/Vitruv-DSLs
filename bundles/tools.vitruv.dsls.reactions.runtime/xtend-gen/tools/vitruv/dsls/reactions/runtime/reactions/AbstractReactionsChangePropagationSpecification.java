package tools.vitruv.dsls.reactions.runtime.reactions;

import java.util.List;
import java.util.function.Supplier;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.ResourceAccess;
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * A {@link ChangePropagationSpecification} that executes {@link Reaction}s.
 */
@SuppressWarnings("all")
public abstract class AbstractReactionsChangePropagationSpecification extends AbstractChangePropagationSpecification {
  private static final Logger LOGGER = Logger.getLogger(AbstractReactionsChangePropagationSpecification.class);

  private final List<Reaction> reactions;

  public AbstractReactionsChangePropagationSpecification(final MetamodelDescriptor sourceMetamodelDescriptor, final MetamodelDescriptor targetMetamodelDescriptor) {
    super(sourceMetamodelDescriptor, targetMetamodelDescriptor);
    this.reactions = CollectionLiterals.<Reaction>newArrayList();
    this.setup();
  }

  protected void addReaction(final Reaction reaction) {
    this.reactions.add(reaction);
  }

  @Override
  public boolean doesHandleChange(final EChange<EObject> change, final EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    return true;
  }

  @Override
  public void propagateChange(final EChange<EObject> change, final EditableCorrespondenceModelView<Correspondence> correspondenceModel, final ResourceAccess resourceAccess) {
    MetamodelDescriptor _sourceMetamodelDescriptor = this.getSourceMetamodelDescriptor();
    String _plus = ("Call relevant reactions from " + _sourceMetamodelDescriptor);
    String _plus_1 = (_plus + " to ");
    MetamodelDescriptor _targetMetamodelDescriptor = this.getTargetMetamodelDescriptor();
    String _plus_2 = (_plus_1 + _targetMetamodelDescriptor);
    AbstractReactionsChangePropagationSpecification.LOGGER.trace(_plus_2);
    for (final Reaction reaction : this.reactions) {
      {
        String _simpleName = reaction.getClass().getSimpleName();
        String _plus_3 = ("Calling reaction: " + _simpleName);
        String _plus_4 = (_plus_3 + " with change: ");
        String _plus_5 = (_plus_4 + change);
        AbstractReactionsChangePropagationSpecification.LOGGER.trace(_plus_5);
        UserInteractor _userInteractor = this.getUserInteractor();
        EditableCorrespondenceModelView<ReactionsCorrespondence> _reactionsView = AbstractReactionsChangePropagationSpecification.getReactionsView(correspondenceModel);
        final ReactionExecutionState executionState = new ReactionExecutionState(_userInteractor, _reactionsView, resourceAccess, this);
        reaction.execute(change, executionState);
      }
    }
  }

  private static EditableCorrespondenceModelView<ReactionsCorrespondence> getReactionsView(final EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    final Supplier<ReactionsCorrespondence> _function = () -> {
      return CorrespondenceFactory.eINSTANCE.createReactionsCorrespondence();
    };
    return correspondenceModel.<ReactionsCorrespondence>getEditableView(ReactionsCorrespondence.class, _function);
  }

  @Override
  public void setUserInteractor(final UserInteractor userInteractor) {
    super.setUserInteractor(userInteractor);
    this.reactions.clear();
    this.setup();
  }

  protected abstract void setup();
}
