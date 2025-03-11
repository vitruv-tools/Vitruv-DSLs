package tools.vitruv.dsls.commonalities.participation;

import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.EqualsHashCode;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;

@EqualsHashCode
@ToString
@FinalFieldsConstructor
@SuppressWarnings("all")
public abstract class Containment {
  @Accessors(AccessorType.PUBLIC_GETTER)
  private final ParticipationClass container;

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final ParticipationClass contained;

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Containment other = (Containment) obj;
    if (this.container == null) {
      if (other.container != null)
        return false;
    } else if (!this.container.equals(other.container))
      return false;
    if (this.contained == null) {
      if (other.contained != null)
        return false;
    } else if (!this.contained.equals(other.contained))
      return false;
    return true;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.container== null) ? 0 : this.container.hashCode());
    return prime * result + ((this.contained== null) ? 0 : this.contained.hashCode());
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("container", this.container);
    b.add("contained", this.contained);
    return b.toString();
  }

  public Containment(final ParticipationClass container, final ParticipationClass contained) {
    super();
    this.container = container;
    this.contained = contained;
  }

  @Pure
  public ParticipationClass getContainer() {
    return this.container;
  }

  @Pure
  public ParticipationClass getContained() {
    return this.contained;
  }
}
