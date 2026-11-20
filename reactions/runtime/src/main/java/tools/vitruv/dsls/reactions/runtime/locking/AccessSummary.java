package tools.vitruv.dsls.reactions.runtime.locking;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Over-approximation of what a piece of reactions code may read and write.
 *
 * <ul>
 *   <li>features: a structural feature on any instance of its declaring class (or a subclass);
 *       since a feature belongs to exactly one declaring class, equal features are the only way two
 *       feature accesses can touch the same slot,</li>
 *   <li>classes: any feature of instances of that class (reflective access, EOperations),</li>
 *   <li>{@code unknownRead}/{@code unknownWrite}: anything in the metamodels of the specification
 *       (calls the analysis cannot see into),</li>
 *   <li>correspondence tags, {@code *} for unknown or any tag.</li>
 * </ul>
 */
public final class AccessSummary {
  final Set<EStructuralFeature> readFeatures = new LinkedHashSet<>();
  final Set<EStructuralFeature> writtenFeatures = new LinkedHashSet<>();
  /** the writes of {@link #writtenFeatures} with receiver and value types, for trigger matching */
  final Set<FeatureWrite> featureWrites = new LinkedHashSet<>();
  final Set<EClass> readClasses = new LinkedHashSet<>();
  final Set<EClass> writtenClasses = new LinkedHashSet<>();
  final Set<EClass> createdClasses = new LinkedHashSet<>();
  final Set<EClass> deletedClasses = new LinkedHashSet<>();
  final Set<String> correspondenceReads = new TreeSet<>();
  final Set<String> correspondenceWrites = new TreeSet<>();
  boolean unknownRead;
  boolean unknownWrite;
  /** deletes elements of unknown type */
  boolean unknownDelete;
  /** adds or removes resources or their root elements */
  boolean persists;
  boolean interactive;
  /** what made the summary unknown: calls the analysis cannot see into */
  final Set<String> opaqueCalls = new TreeSet<>();

  public static String featureKey(EStructuralFeature feature) {
    return classKey(feature.getEContainingClass()) + "." + feature.getName();
  }

  public static String classKey(EClass eClass) {
    return eClass.getEPackage().getNsURI() + "#" + eClass.getName();
  }

  void addAll(AccessSummary other) {
    readFeatures.addAll(other.readFeatures);
    writtenFeatures.addAll(other.writtenFeatures);
    featureWrites.addAll(other.featureWrites);
    readClasses.addAll(other.readClasses);
    writtenClasses.addAll(other.writtenClasses);
    createdClasses.addAll(other.createdClasses);
    deletedClasses.addAll(other.deletedClasses);
    correspondenceReads.addAll(other.correspondenceReads);
    correspondenceWrites.addAll(other.correspondenceWrites);
    unknownRead |= other.unknownRead;
    unknownWrite |= other.unknownWrite;
    unknownDelete |= other.unknownDelete;
    persists |= other.persists;
    interactive |= other.interactive;
    opaqueCalls.addAll(other.opaqueCalls);
  }

  void write(EStructuralFeature feature, EClass receiver, EClass value, ValueSide side) {
    writtenFeatures.add(feature);
    featureWrites.add(new FeatureWrite(feature, receiver, value, side));
  }

  /** Which value of a feature change a known value type refers to. */
  public enum ValueSide { NEW, OLD, NONE }

  /**
   * A write of {@code feature} on objects of static type {@code receiver} (or a subclass); for
   * references, the static type of the inserted ({@code NEW}) or removed ({@code OLD}) value, if
   * known ({@code null}: unknown).
   */
  public record FeatureWrite(EStructuralFeature feature, EClass receiver, EClass value,
      ValueSide side) { }

  void opaque(String call) {
    opaqueCalls.add(call);
    unknownRead = true;
    unknownWrite = true;
  }

  public Set<EStructuralFeature> getReadFeatures() {
    return readFeatures;
  }

  public Set<EStructuralFeature> getWrittenFeatures() {
    return writtenFeatures;
  }

  public Set<EClass> getReadClasses() {
    return readClasses;
  }

  public Set<EClass> getWrittenClasses() {
    return writtenClasses;
  }

  public Set<EClass> getCreatedClasses() {
    return createdClasses;
  }

  public Set<EClass> getDeletedClasses() {
    return deletedClasses;
  }

  public Set<String> getCorrespondenceReads() {
    return correspondenceReads;
  }

  public Set<String> getCorrespondenceWrites() {
    return correspondenceWrites;
  }

  public boolean isUnknownRead() {
    return unknownRead;
  }

  public boolean isUnknownWrite() {
    return unknownWrite;
  }

  public boolean isUnknownDelete() {
    return unknownDelete;
  }

  public boolean isPersists() {
    return persists;
  }

  public boolean isInteractive() {
    return interactive;
  }

  public Set<String> getOpaqueCalls() {
    return opaqueCalls;
  }

  private static String features(Set<EStructuralFeature> features) {
    return features.stream().map(AccessSummary::featureKey).sorted()
        .collect(Collectors.joining(", ", "[", "]"));
  }

  private static String classes(Set<EClass> classes) {
    return classes.stream().map(AccessSummary::classKey).sorted()
        .collect(Collectors.joining(", ", "[", "]"));
  }

  @Override
  public String toString() {
    return "reads=" + features(readFeatures) + " writes=" + features(writtenFeatures)
        + " readClasses=" + classes(readClasses) + " writtenClasses=" + classes(writtenClasses)
        + " creates=" + classes(createdClasses) + " deletes=" + classes(deletedClasses)
        + " corrReads=" + correspondenceReads + " corrWrites=" + correspondenceWrites
        + " unknownRead=" + unknownRead + " unknownWrite=" + unknownWrite
        + " unknownDelete=" + unknownDelete + " persists=" + persists
        + " interactive=" + interactive + " opaque=" + opaqueCalls;
  }
}
