package tools.vitruv.dsls.commonalities.language.extensions;

import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType;

/**
 * Mirrors the information available from {@link AttributeType}.
 */
@Data
@SuppressWarnings("all")
public class AttributeTypeDescription {
  private final boolean multiValued;

  private final String qualifiedTypeName;

  public AttributeTypeDescription(final boolean multiValued, final String qualifiedTypeName) {
    super();
    this.multiValued = multiValued;
    this.qualifiedTypeName = qualifiedTypeName;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.multiValued ? 1231 : 1237);
    return prime * result + ((this.qualifiedTypeName== null) ? 0 : this.qualifiedTypeName.hashCode());
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
    AttributeTypeDescription other = (AttributeTypeDescription) obj;
    if (other.multiValued != this.multiValued)
      return false;
    if (this.qualifiedTypeName == null) {
      if (other.qualifiedTypeName != null)
        return false;
    } else if (!this.qualifiedTypeName.equals(other.qualifiedTypeName))
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("multiValued", this.multiValued);
    b.add("qualifiedTypeName", this.qualifiedTypeName);
    return b.toString();
  }

  @Pure
  public boolean isMultiValued() {
    return this.multiValued;
  }

  @Pure
  public String getQualifiedTypeName() {
    return this.qualifiedTypeName;
  }
}
