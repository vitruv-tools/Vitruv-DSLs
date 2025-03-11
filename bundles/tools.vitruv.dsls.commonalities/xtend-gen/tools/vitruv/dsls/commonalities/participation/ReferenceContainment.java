package tools.vitruv.dsls.commonalities.participation;

import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;

@Data
@SuppressWarnings("all")
public class ReferenceContainment extends Containment {
  /**
   * Can be <code>null</code>.
   */
  private final ParticipationAttribute reference;

  public ReferenceContainment(final ParticipationClass container, final ParticipationClass contained, final ParticipationAttribute reference) {
    super(container, contained);
    this.reference = reference;
  }

  @Override
  @Pure
  public int hashCode() {
    return 31 * super.hashCode() + ((this.reference== null) ? 0 : this.reference.hashCode());
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
    if (!super.equals(obj))
      return false;
    ReferenceContainment other = (ReferenceContainment) obj;
    if (this.reference == null) {
      if (other.reference != null)
        return false;
    } else if (!this.reference.equals(other.reference))
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    return new ToStringBuilder(this)
    	.addAllFields()
    	.toString();
  }

  @Pure
  public ParticipationAttribute getReference() {
    return this.reference;
  }
}
