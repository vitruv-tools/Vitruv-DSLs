package tools.vitruv.dsls.reactions.runtime.locking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;

/**
 * Measures how much parallelism the footprints allow: for every pair of reactions, whether the
 * propagations they start (including everything they trigger) would conflict
 *
 * <ul>
 *   <li>under one global lock (today's {@code synchronized} VSUM: always),</li>
 *   <li>with one lock per metamodel plus one for the correspondence model and one for resources,
 *   </li>
 *   <li>with the type-level keys of the footprints
 *       ({@link ReactionsPropagationFootprints#keyConflict()}).</li>
 * </ul>
 *
 * <p>{@link #main(String[])} runs it on all reactions specifications compiled into a classes
 * folder (run with that module's classpath).
 */
public final class FootprintReport {
  private FootprintReport() {
  }

  /** Statistics of one set of specifications. */
  public record Result(int reactions, int pairs, int conflictFreeMetamodelLevel,
      int conflictFreeTypeLevel, int conflictFreeWithoutCorrespondences,
      int reactionsWithUnknownAccess, Set<String> opaqueCalls,
      double averageReachedReactions, long analysisMillis, List<String> details) {

    @Override
    public String toString() {
      return String.format(
          "reactions=%d pairs=%d conflict-free: global=0%% metamodel=%.0f%% type=%.0f%% "
              + "(type without correspondences=%.0f%%) | reactions with unknown access=%d, "
              + "avg reached reactions=%.1f, analysis=%d ms",
          reactions, pairs, percent(conflictFreeMetamodelLevel), percent(conflictFreeTypeLevel),
          percent(conflictFreeWithoutCorrespondences), reactionsWithUnknownAccess,
          averageReachedReactions, analysisMillis);
    }

    private double percent(int value) {
      return pairs == 0 ? 0 : 100.0 * value / pairs;
    }
  }

  public static Result analyze(Iterable<? extends ChangePropagationSpecification> specifications) {
    long start = System.nanoTime();
    var footprints = new ReactionsPropagationFootprints(specifications);
    // specifications that import other segments contain the same reaction classes again
    Map<Class<?>, Reaction> unique = new LinkedHashMap<>();
    footprints.allReactions().forEach(r -> unique.putIfAbsent(r.getClass(), r));
    List<Reaction> reactions = new ArrayList<>(unique.values());
    Map<String, PropagationFootprint> byReaction = new TreeMap<>();
    List<String> details = new ArrayList<>();
    int unknown = 0;
    Set<String> opaque = new TreeSet<>();
    long reached = 0;
    for (Reaction reaction : reactions) {
      PropagationFootprint footprint = footprints.footprintOfReaction(reaction);
      String name = reaction.getClass().getName();
      byReaction.put(name, footprint);
      AccessSummary own = footprints.summaryOf(reaction);
      if (own.isUnknownRead() || own.isUnknownWrite() || own.isUnknownDelete()) {
        unknown++;
      }
      opaque.addAll(own.getOpaqueCalls());
      reached += footprint.reactions().size();
      details.add(reaction.getClass().getSimpleName());
      details.add("  trigger:   " + footprints.triggerOf(reaction));
      details.add("  own:       " + own);
      details.add("  footprint: reads=" + footprint.readKeys() + " writes="
          + footprint.writeKeys() + " reactions=" + footprint.reactions().size());
    }
    List<PropagationFootprint> list = new ArrayList<>(byReaction.values());
    PropagationFootprint.KeyConflict typeLevel = footprints.keyConflict();
    PropagationFootprint.KeyConflict metamodelLevel =
        (a, b) -> metamodelLock(a).equals(metamodelLock(b));
    int pairs = 0;
    int freeMetamodel = 0;
    int freeType = 0;
    int freeWithoutCorrespondences = 0;
    PropagationFootprint.KeyConflict withoutCorrespondences = (a, b) ->
        !a.startsWith("corr:") && !b.startsWith("corr:") && typeLevel.conflicts(a, b);
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        pairs++;
        if (!list.get(i).conflictsWith(list.get(j), metamodelLevel)) {
          freeMetamodel++;
        }
        if (!list.get(i).conflictsWith(list.get(j), typeLevel)) {
          freeType++;
        }
        if (!list.get(i).conflictsWith(list.get(j), withoutCorrespondences)) {
          freeWithoutCorrespondences++;
        }
      }
    }
    return new Result(reactions.size(), pairs, freeMetamodel, freeType,
        freeWithoutCorrespondences, unknown, opaque,
        reactions.isEmpty() ? 0 : (double) reached / reactions.size(),
        (System.nanoTime() - start) / 1_000_000, details);
  }

  /** The coarse lock a footprint key falls under with one lock per metamodel. */
  static String metamodelLock(String key) {
    if (key.startsWith("corr:")) {
      return "corr";
    }
    if (key.equals("res")) {
      return "res";
    }
    String body = key.substring(key.indexOf(':') + 1);
    int hash = body.lastIndexOf('#');
    return hash < 0 ? body : body.substring(0, hash);
  }

  /**
   * Usage: {@code FootprintReport <classes folder> [<output file>]}. Instantiates every concrete
   * reactions specification (no-argument constructor) found in the folder and prints the result.
   */
  public static void main(String[] args) throws IOException {
    Path classes = Path.of(args[0]);
    List<ChangePropagationSpecification> specifications = new ArrayList<>();
    ClassLoader loader = FootprintReport.class.getClassLoader();
    try (Stream<Path> files = Files.walk(classes)) {
      Iterable<Path> classFiles = files.filter(f -> f.toString().endsWith(".class"))::iterator;
      for (Path file : classFiles) {
        String name = classes.relativize(file).toString().replace('\\', '/').replace('/', '.');
        name = name.substring(0, name.length() - ".class".length());
        if (!name.endsWith("ChangePropagationSpecification")) {
          continue;
        }
        try {
          Class<?> type = Class.forName(name, true, loader);
          if (AbstractReactionsChangePropagationSpecification.class.isAssignableFrom(type)
              && !java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            specifications.add(
                (ChangePropagationSpecification) type.getConstructor().newInstance());
          }
        } catch (ReflectiveOperationException | LinkageError e) {
          System.err.println("skipped " + name + ": " + e);
        }
      }
    }
    Result result = analyze(specifications);
    System.out.println(classes + ": " + specifications.size() + " specifications, " + result);
    System.out.println("opaque calls: " + result.opaqueCalls());
    if (!result.details().isEmpty()) {
      System.out.println(result.details().get(0));
    }
    if (args.length > 1) {
      List<String> lines = new ArrayList<>();
      lines.add(result.toString());
      lines.add("opaque calls: " + result.opaqueCalls());
      lines.addAll(result.details());
      Files.write(Path.of(args[1]), lines);
    }
  }

}
