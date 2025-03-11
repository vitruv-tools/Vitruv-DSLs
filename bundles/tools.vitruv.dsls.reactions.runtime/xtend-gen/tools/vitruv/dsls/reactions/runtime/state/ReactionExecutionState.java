package tools.vitruv.dsls.reactions.runtime.state;

import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.propagation.ChangePropagationObservable;
import tools.vitruv.change.propagation.ResourceAccess;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

@Data
@SuppressWarnings("all")
public class ReactionExecutionState {
  private final UserInteractor userInteractor;

  private final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel;

  private final ResourceAccess resourceAccess;

  private final ChangePropagationObservable changePropagationObservable;

  public ReactionExecutionState(final UserInteractor userInteractor, final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel, final ResourceAccess resourceAccess, final ChangePropagationObservable changePropagationObservable) {
    super();
    this.userInteractor = userInteractor;
    this.correspondenceModel = correspondenceModel;
    this.resourceAccess = resourceAccess;
    this.changePropagationObservable = changePropagationObservable;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.userInteractor== null) ? 0 : this.userInteractor.hashCode());
    result = prime * result + ((this.correspondenceModel== null) ? 0 : this.correspondenceModel.hashCode());
    result = prime * result + ((this.resourceAccess== null) ? 0 : this.resourceAccess.hashCode());
    return prime * result + ((this.changePropagationObservable== null) ? 0 : this.changePropagationObservable.hashCode());
  }

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ReactionExecutionState other = (ReactionExecutionState) obj;
    if (this.userInteractor == null) {
      if (other.userInteractor != null)
        return false;
    } else if (!this.userInteractor.equals(other.userInteractor))
      return false;
    if (this.correspondenceModel == null) {
      if (other.correspondenceModel != null)
        return false;
    } else if (!this.correspondenceModel.equals(other.correspondenceModel))
      return false;
    if (this.resourceAccess == null) {
      if (other.resourceAccess != null)
        return false;
    } else if (!this.resourceAccess.equals(other.resourceAccess))
      return false;
    if (this.changePropagationObservable == null) {
      if (other.changePropagationObservable != null)
        return false;
    } else if (!this.changePropagationObservable.equals(other.changePropagationObservable))
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("userInteractor", this.userInteractor);
    b.add("correspondenceModel", this.correspondenceModel);
    b.add("resourceAccess", this.resourceAccess);
    b.add("changePropagationObservable", this.changePropagationObservable);
    return b.toString();
  }

  @Pure
  public UserInteractor getUserInteractor() {
    return this.userInteractor;
  }

  @Pure
  public EditableCorrespondenceModelView<ReactionsCorrespondence> getCorrespondenceModel() {
    return this.correspondenceModel;
  }

  @Pure
  public ResourceAccess getResourceAccess() {
    return this.resourceAccess;
  }

  @Pure
  public ChangePropagationObservable getChangePropagationObservable() {
    return this.changePropagationObservable;
  }
}
