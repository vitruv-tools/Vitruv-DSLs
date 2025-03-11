package tools.vitruv.dsls.commonalities.runtime;

import org.eclipse.xtend.lib.annotations.EqualsHashCode;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

@ToString
@EqualsHashCode
@SuppressWarnings("all")
public class BooleanResult {
  private boolean value = false;

  public BooleanResult() {
  }

  public boolean getValue() {
    return this.value;
  }

  public boolean setValue(final boolean value) {
    return this.value = value;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("value", this.value);
    return b.toString();
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
    BooleanResult other = (BooleanResult) obj;
    if (other.value != this.value)
      return false;
    return true;
  }

  @Override
  @Pure
  public int hashCode() {
    return 31 * 1 + (this.value ? 1231 : 1237);
  }
}
