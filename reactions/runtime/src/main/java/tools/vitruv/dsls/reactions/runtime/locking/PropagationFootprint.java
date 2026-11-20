package tools.vitruv.dsls.reactions.runtime.locking;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * What propagating one kind of change through the reactions may read and write, including all
 * reactions it may trigger transitively. Meant to be turned into locks by a transaction manager
 * (conservative two-phase locking: acquire everything before the propagation starts).
 *
 * <p>Lock keys (type level, i.e. for all instances):
 *
 * <ul>
 *   <li>{@code f:<nsURI>#<declaring EClass>.<feature>}: a structural feature,</li>
 *   <li>{@code c:<nsURI>#<EClass>}: any feature of instances of that class and its subclasses,</li>
 *   <li>{@code mm:<nsURI>}: anything in that metamodel (unknown accesses),</li>
 *   <li>{@code corr:<tag>}, {@code corr:*}: correspondences with that tag / any tag,</li>
 *   <li>{@code res}: resources and their root elements.</li>
 * </ul>
 *
 * @param readKeys keys that are only read
 * @param writeKeys keys that may be written
 * @param reactions the reactions that may run, as class names
 * @param summary the union of the reactions' access summaries
 */
public record PropagationFootprint(Set<String> readKeys, Set<String> writeKeys,
    List<String> reactions, AccessSummary summary) {

  /** Whether a propagation with this footprint conflicts with one with {@code other}. */
  public boolean conflictsWith(PropagationFootprint other, KeyConflict keyConflict) {
    for (String write : writeKeys) {
      for (String key : other.readKeys) {
        if (keyConflict.conflicts(write, key)) {
          return true;
        }
      }
      for (String key : other.writeKeys) {
        if (keyConflict.conflicts(write, key)) {
          return true;
        }
      }
    }
    for (String write : other.writeKeys) {
      for (String key : readKeys) {
        if (keyConflict.conflicts(write, key)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Decides whether a write of one key conflicts with an access of another. */
  @FunctionalInterface
  public interface KeyConflict {
    boolean conflicts(String writtenKey, String accessedKey);
  }

  /** All keys, for logging. */
  public Set<String> allKeys() {
    Set<String> all = new TreeSet<>(readKeys);
    all.addAll(writeKeys);
    return all;
  }
}
