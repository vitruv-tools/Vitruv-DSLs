package tools.vitruv.dsls.reactions.runtime.reactions;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.impl.AbstractChangePropagationSpecification;
import tools.vitruv.change.utils.ResourceAccess;
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * A {@link ChangePropagationSpecification} that executes {@link Reaction}s.
 */
public abstract class AbstractReactionsChangePropagationSpecification 
    extends AbstractChangePropagationSpecification {
  private static Logger logger = LogManager.getLogger(
      AbstractReactionsChangePropagationSpecification.class);

  private final List<Reaction> reactions;

  /**
   * Creates an {@link AbstractReactionsChangePropagationSpecification} without reactions 
   * between the metamodels described by sourceMetamodelDescriptor and targetMetamodelDescriptor.
   *
   * @param sourceMetamodelDescriptor - {@link MetamodelDescriptor}
   * @param targetMetamodelDescriptor - {@link MetamodelDescriptor}
   */
  protected AbstractReactionsChangePropagationSpecification(
      MetamodelDescriptor sourceMetamodelDescriptor,
      MetamodelDescriptor targetMetamodelDescriptor) {
    super(sourceMetamodelDescriptor, targetMetamodelDescriptor);
    this.reactions = new ArrayList<>();
    this.setup();
  }

  /**
   * Returns the number of reactions.
   *
   * @return int
   */
  public int getNumberOfReactions() {
    return reactions.size();
  }

  /**
   * Adds reaction to this specification.
   *
   * @param reaction - {@link Reaction}
   */
  protected void addReaction(Reaction reaction) {
    this.reactions.add(reaction);
  }

  /**
   * {@inheritDoc}
   * By default, an {@link AbstractReactionsChangePropagationSpecification} handles any change.
   */
  @Override
  public boolean doesHandleChange(EChange<EObject> change,
      EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    return true;
  }

  /**
   * {@inheritDoc}
   * These modifications happen by executing all reactions this specification has.
   */
  @Override
  public void propagateChange(EChange<EObject> change, 
      EditableCorrespondenceModelView<Correspondence> correspondenceModel, 
      ResourceAccess resourceAccess) {
    logger.trace("Call relevant reactions from %s to %s", 
        getSourceMetamodelDescriptor(), getTargetMetamodelDescriptor());
    for (var reaction : reactions) {
      logger.trace("Calling reaction: %s with change: %s",
          reaction.getClass().getSimpleName(), change);
      var executionState = new ReactionExecutionState(
          getUserInteractor(),
          getReactionsView(correspondenceModel),
          resourceAccess,
          this);
      reaction.execute(change, executionState);
    }
  }
  
  private static EditableCorrespondenceModelView<ReactionsCorrespondence> getReactionsView(
      EditableCorrespondenceModelView<Correspondence> correspondenceModel) {
    return correspondenceModel.getEditableView(ReactionsCorrespondence.class, 
        CorrespondenceFactory.eINSTANCE::createReactionsCorrespondence);
  }

  @Override
  public void setUserInteractor(UserInteractor userInteractor) {
    super.setUserInteractor(userInteractor);
    reactions.clear();
    setup();
  }

  /**
   * Sets up this {@link ChangePropagationSpecification}.
   */
  protected abstract void setup();
}
