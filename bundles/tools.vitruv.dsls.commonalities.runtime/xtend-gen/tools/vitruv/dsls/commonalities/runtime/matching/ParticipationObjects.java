package tools.vitruv.dsls.commonalities.runtime.matching;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.EqualsHashCode;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * A set of named EObjects.
 */
@EqualsHashCode
@ToString
@SuppressWarnings("all")
public class ParticipationObjects {
  private final BiMap<String, EObject> objectsByName = HashBiMap.<String, EObject>create();

  public ParticipationObjects() {
  }

  public Set<EObject> getObjects() {
    return this.objectsByName.values();
  }

  public EObject addObject(final String name, final EObject object) {
    EObject _xblockexpression = null;
    {
      boolean _containsKey = this.objectsByName.containsKey(name);
      boolean _not = (!_containsKey);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("There is already an object for this name: ");
      _builder.append(name);
      Preconditions.checkArgument(_not, _builder);
      boolean _containsValue = this.objectsByName.containsValue(object);
      boolean _not_1 = (!_containsValue);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("The object is already present: ");
      _builder_1.append(object);
      Preconditions.checkArgument(_not_1, _builder_1);
      _xblockexpression = this.objectsByName.put(name, object);
    }
    return _xblockexpression;
  }

  public <T extends EObject> T getObject(final String name) {
    EObject _get = this.objectsByName.get(name);
    return ((T) _get);
  }

  public ParticipationObjects copy() {
    return new ParticipationObjects().merge(this);
  }

  /**
   * ParticipationObjects can only be merged if they don't contain
   * contradicting mappings for the same name or object.
   */
  public boolean canBeMerged(final ParticipationObjects other) {
    if ((other == this)) {
      return false;
    }
    final Function1<Map.Entry<String, EObject>, Boolean> _function = (Map.Entry<String, EObject> it) -> {
      EObject _object = this.<EObject>getObject(it.getKey());
      EObject _value = it.getValue();
      return Boolean.valueOf((!Objects.equal(_object, _value)));
    };
    final Function1<Map.Entry<String, EObject>, Boolean> _function_1 = (Map.Entry<String, EObject> it) -> {
      return Boolean.valueOf(((!this.objectsByName.containsKey(it.getKey())) && (!this.getObjects().contains(it.getValue()))));
    };
    return IterableExtensions.<Map.Entry<String, EObject>>forall(IterableExtensions.<Map.Entry<String, EObject>>filter(other.objectsByName.entrySet(), _function), _function_1);
  }

  /**
   * Adds the entries of the given ParticipationObjects to this
   * ParticipationObjects, if they are not already present.
   * <p>
   * Throws an exception when trying to add entries for names or objects that
   * are already present but mapped differently.
   */
  public ParticipationObjects merge(final ParticipationObjects other) {
    Preconditions.checkArgument((other != this), "Cannot merge with self");
    final Function1<Map.Entry<String, EObject>, Boolean> _function = (Map.Entry<String, EObject> it) -> {
      EObject _object = this.<EObject>getObject(it.getKey());
      EObject _value = it.getValue();
      return Boolean.valueOf((!Objects.equal(_object, _value)));
    };
    final Consumer<Map.Entry<String, EObject>> _function_1 = (Map.Entry<String, EObject> it) -> {
      this.addObject(it.getKey(), it.getValue());
    };
    IterableExtensions.<Map.Entry<String, EObject>>filter(other.objectsByName.entrySet(), _function).forEach(_function_1);
    return this;
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
    ParticipationObjects other = (ParticipationObjects) obj;
    if (this.objectsByName == null) {
      if (other.objectsByName != null)
        return false;
    } else if (!this.objectsByName.equals(other.objectsByName))
      return false;
    return true;
  }

  @Override
  @Pure
  public int hashCode() {
    return 31 * 1 + ((this.objectsByName== null) ? 0 : this.objectsByName.hashCode());
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("objectsByName", this.objectsByName);
    return b.toString();
  }
}
