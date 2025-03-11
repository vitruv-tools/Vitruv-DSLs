package tools.vitruv.dsls.commonalities.participation;

import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;

@Data
@SuppressWarnings("all")
public class OperatorContainment extends Containment {
  private final OperatorReferenceMapping mapping;

  public OperatorContainment(final ParticipationClass container, final ParticipationClass contained, final OperatorReferenceMapping mapping) {
    super(container, contained);
    this.mapping = mapping;
  }

  @Override
  @Pure
  public int hashCode() {
    return 31 * super.hashCode() + ((this.mapping== null) ? 0 : this.mapping.hashCode());
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
    OperatorContainment other = (OperatorContainment) obj;
    if (this.mapping == null) {
      if (other.mapping != null)
        return false;
    } else if (!this.mapping.equals(other.mapping))
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
  public OperatorReferenceMapping getMapping() {
    return this.mapping;
  }
}
