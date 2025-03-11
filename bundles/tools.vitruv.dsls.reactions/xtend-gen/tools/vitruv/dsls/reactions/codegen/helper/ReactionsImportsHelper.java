package tools.vitruv.dsls.reactions.codegen.helper;

import com.google.common.base.Preconditions;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;
import tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil;

/**
 * Utilities related to reactions imports, import hierarchies, and reaction and routine overrides.
 */
@Utility
@SuppressWarnings("all")
public final class ReactionsImportsHelper {
  /**
   * This visitor gets called for each reactions segment while walking the import hierarchy, starting with the root reactions segment.
   * 
   * @param <D>
   *            the type of the data being passed along
   */
  public interface ImportHierarchyVisitor<D extends Object> {
    /**
     * Gets called for each reactions segment along the walked import hierarchy.
     * 
     * @param sourceImport
     *            the reactions import that lead to the current reactions segment, or <code>null</code> at the root segment
     * @param currentImportPath
     *            the absolute import path to the current reactions segment, starting with the root reactions segment
     * @param currentReactionsSegment
     *            the current reactions segment
     * @param data
     *            the data being passed along
     */
    void visit(final ReactionsImport sourceImport, final ReactionsImportPath currentImportPath, final ReactionsSegment currentReactionsSegment, final D data);
  }

  /**
   * This visitor gets called for each reactions segment while walking along a certain import path, starting with the root
   * reactions segment.
   * <p>
   * It can abort the walking by returning a <code>non-null</code> return value.
   * 
   * @param <T>
   *            the type of the return value
   */
  public interface ImportPathVisitor<T extends Object> {
    /**
     * Gets called for each reactions segment along the walked import path and can abort the walking by returning a
     * <code>non-null</code> return value.
     * 
     * @param sourceImport
     *            the reactions import that lead to the current reactions segment, or <code>null</code> at the root segment
     * @param currentImportPath
     *            the absolute import path to the current reactions segment, starting with the root reactions segment
     * @param currentReactionsSegment
     *            the current reactions segment
     * @param remainingImportPath
     *            the remaining import path to walk, relative to the current reactions segment, empty if the end of the path
     *            is reached
     * @return the return value, or <code>null</code> to continue walking
     */
    T visit(final ReactionsImport sourceImport, final ReactionsImportPath currentImportPath, final ReactionsSegment currentReactionsSegment, final ReactionsImportPath remainingImportPath);
  }

  /**
   * Gets the name of the overridden reactions segment.
   * <p>
   * Before accessing the overridden reactions segment directly and therefore resolving the potentially still unresolved
   * cross-reference, this first attempts to get the parsed overridden reactions segment name.
   * 
   * @param reaction the reaction
   * @return the name of the overridden reactions segment, or <code>null</code> if the given reaction doesn't override any
   *         other reaction
   */
  public static String getParsedOverriddenReactionsSegmentName(final Reaction reaction) {
    final String parsed = ReactionsImportsHelper.getFeatureNodeText(reaction, TopLevelElementsPackage.Literals.REACTION__OVERRIDDEN_REACTIONS_SEGMENT);
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(parsed);
    boolean _not = (!_isNullOrEmpty);
    if (_not) {
      return parsed;
    }
    ReactionsSegment _overriddenReactionsSegment = reaction.getOverriddenReactionsSegment();
    String _name = null;
    if (_overriddenReactionsSegment!=null) {
      _name=_overriddenReactionsSegment.getName();
    }
    return _name;
  }

  /**
   * Gets the reactions segment names of the override import path for the given routine.
   * <p>
   * Before accessing the reactions segments inside the override import path directly and therefore resolving the potentially
   * still unresolved cross-references, this first attempts to get the parsed import path segment names.
   * <p>
   * Any incomplete or unresolvable segments inside the import path will get represented by an empty String.
   * 
   * @param routine the routine
   * @return the reactions import path consisting of the found segment names, or <code>null</code> if the given routine does
   *         not override any other routine
   */
  public static ReactionsImportPath getParsedOverrideImportPath(final Routine routine) {
    return ReactionsImportsHelper.getParsedOverrideImportPath(routine.getOverrideImportPath());
  }

  /**
   * Gets the reactions segment names of the given override import path.
   * <p>
   * Before accessing the reactions segments inside the override import path directly and therefore resolving the potentially
   * still unresolved cross-references, this first attempts to get the parsed import path segment names.
   * <p>
   * Any incomplete or unresolvable segments inside the import path will get represented by an empty String.
   * 
   * @param routineOverrideImportPath the routine override import path
   * @return the reactions import path consisting of the found segment names, or <code>null</code> if the given routine
   *         override path was <code>null</code>
   */
  public static ReactionsImportPath getParsedOverrideImportPath(final RoutineOverrideImportPath routineOverrideImportPath) {
    if ((routineOverrideImportPath == null)) {
      return null;
    }
    final Function1<RoutineOverrideImportPath, String> _function = (RoutineOverrideImportPath it) -> {
      final String parsed = ReactionsImportsHelper.getFeatureNodeText(it, TopLevelElementsPackage.Literals.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT);
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(parsed);
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        return parsed;
      }
      String _elvis = null;
      ReactionsSegment _reactionsSegment = it.getReactionsSegment();
      String _name = null;
      if (_reactionsSegment!=null) {
        _name=_reactionsSegment.getName();
      }
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = "";
      }
      return _elvis;
    };
    final List<String> parsedSegments = ListExtensions.<RoutineOverrideImportPath, String>map(ReactionsLanguageUtil.getFullPath(routineOverrideImportPath), _function);
    return ReactionsImportPath.create(parsedSegments);
  }

  private static String getFeatureNodeText(final EObject semanticObject, final EStructuralFeature structuralFeature) {
    final List<INode> nodes = NodeModelUtils.findNodesForFeature(semanticObject, structuralFeature);
    boolean _isEmpty = nodes.isEmpty();
    if (_isEmpty) {
      return null;
    }
    return NodeModelUtils.getTokenText(nodes.get(0));
  }

  /**
   * Gets the parsed import paths for all complete routine overrides contained in the given reactions segment.
   * <p>
   * The returned import paths are unique, and relative to the given reactions segment.
   * 
   * @param reactionsSegment the reactions segment
   * @return the parsed overridden routines import paths
   * @see #getParsedOverrideImportPath(Routine)
   */
  public static Set<ReactionsImportPath> getParsedOverriddenRoutinesImportPaths(final ReactionsSegment reactionsSegment) {
    final Function1<Routine, Boolean> _function = (Routine it) -> {
      return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it));
    };
    final Function1<Routine, ReactionsImportPath> _function_1 = (Routine it) -> {
      return ReactionsImportsHelper.getParsedOverrideImportPath(it);
    };
    return IterableExtensions.<ReactionsImportPath>toSet(IterableExtensions.<Routine, ReactionsImportPath>map(IterableExtensions.<Routine>filter(ReactionsLanguageUtil.getOverrideRoutines(reactionsSegment), _function), _function_1));
  }

  /**
   * Checks if all reactions imports of the given reactions segment can be resolved currently.
   * 
   * @param reactionsSegment the reactions segment
   * @return <code>true</code> if all imports are resolvable
   * @see #isResolvable(ReactionsImport)
   */
  public static boolean isAllImportsResolvable(final ReactionsSegment reactionsSegment) {
    final Function1<ReactionsImport, Boolean> _function = (ReactionsImport it) -> {
      boolean _isResolvable = ReactionsImportsHelper.isResolvable(it);
      return Boolean.valueOf((!_isResolvable));
    };
    ReactionsImport _findFirst = IterableExtensions.<ReactionsImport>findFirst(reactionsSegment.getReactionsImports(), _function);
    return (_findFirst == null);
  }

  /**
   * Checks if the given reactions import can be resolved currently.
   * <p>
   * This checks whether:
   * <ul>
   * <li>The reactions import is not <code>null</code>.
   * <li>The imported reactions segment is not <code>null</code>.
   * <li>The imported reactions segment is no proxy.
   * </ul>
   * Note: This will trigger a resolve of the potentially not yet resolved cross-reference to the imported reactions segment!
   * 
   * @param reactionsImport the reactions import
   * @return <code>true</code> if the given import is resolvable
   */
  public static boolean isResolvable(final ReactionsImport reactionsImport) {
    return (((reactionsImport != null) && (reactionsImport.getImportedReactionsSegment() != null)) && (!reactionsImport.getImportedReactionsSegment().eIsProxy()));
  }

  /**
   * Walks the import hierarchy with depth-first order, starting at the given root reactions segment.
   * <p>
   * The optional <code>importFilter</code> can be used to determine which import branches in the hierarchy to follow and which
   * to skip. Only imports for which it returns <code>true</code> are followed further. If not specified, all imports are
   * followed. Any cyclic imports or imports that cannot be resolved will be considered non-existent and therefore not be
   * followed, nor passed to the <code>importFilter</code> in the first place.
   * <p>
   * The optional <code>earlyVisitor</code> gets called before going deeper in the hierarchy, and <code>lateVisitor</code> is
   * called after all deeper branches have been visited.
   * <p>
   * The optional <code>dataInitializer</code> can be used to initially create some data object that gets passed along to the
   * visitors while walking the hierarchy. If not specified, then <code>null</code> is passed along as data object.
   * <p>
   * The <code>returnValueFunction</code> gets called at the end to calculate a return value.
   * 
   * @param <R> the type of the return value
   * @param <D> the type of the data being passed along
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @param dataInitializer creates a data object which gets then passed around to the called visitors, optional
   * @param earlyVisitor the visitor that gets called right when reaching a reactions segment in the import hierarchy, optional
   * @param lateVisitor the visitor that gets called during backtracking, after all imports of a reactions segment have been fully followed, optional
   * @param importFilter a predicate that decides whether an import is further followed, optional
   * @param returnValueFunction the return value function, not <code>null</code>
   * @return the return value calculated by the return value function
   */
  public static <R extends Object, D extends Object> R walkImportHierarchy(final ReactionsSegment rootReactionsSegment, final Supplier<D> dataInitializer, final ReactionsImportsHelper.ImportHierarchyVisitor<D> earlyVisitor, final ReactionsImportsHelper.ImportHierarchyVisitor<D> lateVisitor, final Predicate<ReactionsImport> importFilter, final Function<D, R> returnValueFunction) {
    Preconditions.<ReactionsSegment>checkNotNull(rootReactionsSegment, "rootReactionsSegment is null");
    Preconditions.<Function<D, R>>checkNotNull(returnValueFunction, "returnValueFunction is null");
    D _get = null;
    if (dataInitializer!=null) {
      _get=dataInitializer.get();
    }
    final D data = _get;
    final ReactionsImportPath rootImportPath = ReactionsImportPath.create(rootReactionsSegment.getName());
    Predicate<ReactionsImport> _elvis = null;
    if (importFilter != null) {
      _elvis = importFilter;
    } else {
      final Predicate<ReactionsImport> _function = (ReactionsImport it) -> {
        return true;
      };
      _elvis = _function;
    }
    ReactionsImportsHelper.<D>_walkImportHierarchy(null, rootImportPath, rootReactionsSegment, data, earlyVisitor, lateVisitor, _elvis);
    return returnValueFunction.apply(data);
  }

  private static <D extends Object> void _walkImportHierarchy(final ReactionsImport sourceImport, final ReactionsImportPath currentImportPath, final ReactionsSegment currentReactionsSegment, final D data, final ReactionsImportsHelper.ImportHierarchyVisitor<D> earlyVisitor, final ReactionsImportsHelper.ImportHierarchyVisitor<D> lateVisitor, final Predicate<ReactionsImport> importFilter) {
    if (earlyVisitor!=null) {
      earlyVisitor.visit(sourceImport, currentImportPath, currentReactionsSegment, data);
    }
    final Function1<ReactionsImport, Boolean> _function = (ReactionsImport it) -> {
      return Boolean.valueOf(ReactionsImportsHelper.isResolvable(it));
    };
    Iterable<ReactionsImport> _filter = IterableExtensions.<ReactionsImport>filter(currentReactionsSegment.getReactionsImports(), _function);
    for (final ReactionsImport nextImport : _filter) {
      {
        final ReactionsSegment importedSegment = nextImport.getImportedReactionsSegment();
        if (((!currentImportPath.getSegments().contains(importedSegment.getName())) && importFilter.test(nextImport))) {
          final ReactionsImportPath importedSegmentImportPath = currentImportPath.append(importedSegment.getName());
          ReactionsImportsHelper.<D>_walkImportHierarchy(nextImport, importedSegmentImportPath, importedSegment, data, earlyVisitor, lateVisitor, importFilter);
        }
      }
    }
    if (lateVisitor!=null) {
      lateVisitor.visit(sourceImport, currentImportPath, currentReactionsSegment, data);
    }
  }

  /**
   * Gets all reactions segments that contribute routines to the given root reactions segment, including the root reactions
   * segment itself. This basically returns the whole import hierarchy.
   * <p>
   * Reactions segments can be contained more than once at different import paths.
   * <p>
   * The reactions segments are returned together with their absolute reactions import path (starting with the root segment)
   * denoting their position in the import hierarchy.
   * 
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @return all reactions segments in the import hierarchy by their absolute import paths
   */
  public static Map<ReactionsImportPath, ReactionsSegment> getRoutinesImportHierarchy(final ReactionsSegment rootReactionsSegment) {
    final Supplier<LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function = () -> {
      return new LinkedHashMap<ReactionsImportPath, ReactionsSegment>();
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function_1 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, LinkedHashMap<ReactionsImportPath, ReactionsSegment> data) -> {
      final LinkedHashMap<ReactionsImportPath, ReactionsSegment> importHierarchy = data;
      importHierarchy.put(currentImportPath, currentReactionsSegment);
    };
    final Predicate<ReactionsImport> _function_2 = (ReactionsImport it) -> {
      return true;
    };
    final Function<LinkedHashMap<ReactionsImportPath, ReactionsSegment>, LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function_3 = (LinkedHashMap<ReactionsImportPath, ReactionsSegment> it) -> {
      return it;
    };
    return ReactionsImportsHelper.<LinkedHashMap<ReactionsImportPath, ReactionsSegment>, LinkedHashMap<ReactionsImportPath, ReactionsSegment>>walkImportHierarchy(rootReactionsSegment, _function, _function_1, 
      null, _function_2, _function_3);
  }

  /**
   * Gets all reactions segments that contribute reactions to the given root reactions segment, including the root reactions
   * segment itself.
   * <p>
   * This walks the import hierarchy and only follows imports that include reactions.
   * <p>
   * Each reactions segment can be contained only once. So there exists exactly one path from the root reactions segment to
   * each of the contained reactions segments.
   * <p>
   * The reactions segments are returned together with their absolute reactions import path (starting with the root segment)
   * denoting their position in the import hierarchy.
   * 
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @return all reactions segments in the import hierarchy, that contribute reactions, by their absolute import paths
   */
  public static Map<ReactionsImportPath, ReactionsSegment> getReactionsImportHierarchy(final ReactionsSegment rootReactionsSegment) {
    final Supplier<LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function = () -> {
      return new LinkedHashMap<ReactionsImportPath, ReactionsSegment>();
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function_1 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, LinkedHashMap<ReactionsImportPath, ReactionsSegment> data) -> {
      final LinkedHashMap<ReactionsImportPath, ReactionsSegment> importHierarchy = data;
      boolean _containsValue = importHierarchy.containsValue(currentReactionsSegment);
      boolean _not = (!_containsValue);
      if (_not) {
        importHierarchy.put(currentImportPath, currentReactionsSegment);
      }
    };
    final Predicate<ReactionsImport> _function_2 = (ReactionsImport it) -> {
      boolean _isRoutinesOnly = it.isRoutinesOnly();
      return (!_isRoutinesOnly);
    };
    final Function<LinkedHashMap<ReactionsImportPath, ReactionsSegment>, LinkedHashMap<ReactionsImportPath, ReactionsSegment>> _function_3 = (LinkedHashMap<ReactionsImportPath, ReactionsSegment> it) -> {
      return it;
    };
    return ReactionsImportsHelper.<LinkedHashMap<ReactionsImportPath, ReactionsSegment>, LinkedHashMap<ReactionsImportPath, ReactionsSegment>>walkImportHierarchy(rootReactionsSegment, _function, _function_1, 
      null, _function_2, _function_3);
  }

  /**
   * Gets all reactions that are included in the executor of the given root reactions segment.
   * <p>
   * This includes the own reactions, as well as all reactions found in the reactions import hierarchy, with overridden
   * reactions being replaced. This can contain reactions with duplicate names, as long as those originate from differently
   * named reactions segments.
   * <p>
   * The reactions are returned together with the absolute reactions import paths (starting with the root segment) denoting the
   * positions of the their reactions segments in the import hierarchy.
   * 
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @return all included reactions with their absolute import paths
   */
  public static Map<Reaction, ReactionsImportPath> getIncludedReactions(final ReactionsSegment rootReactionsSegment) {
    final Supplier<Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>>> _function = () -> {
      LinkedHashMap<String, Reaction> _linkedHashMap = new LinkedHashMap<String, Reaction>();
      LinkedHashMap<Reaction, ReactionsImportPath> _linkedHashMap_1 = new LinkedHashMap<Reaction, ReactionsImportPath>();
      return Tuples.<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>>create(_linkedHashMap, _linkedHashMap_1);
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>>> _function_1 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>> data) -> {
      final LinkedHashMap<String, Reaction> reactionsByQualifiedName = data.getFirst();
      final LinkedHashMap<Reaction, ReactionsImportPath> reactionsToImportPath = data.getSecond();
      final Function1<Reaction, Boolean> _function_2 = (Reaction it) -> {
        return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it));
      };
      Iterable<Reaction> _filter = IterableExtensions.<Reaction>filter(currentReactionsSegment.getReactions(), _function_2);
      for (final Reaction reaction : _filter) {
        {
          final String qualifiedName = ReactionsLanguageUtil.getQualifiedName(reaction);
          final Reaction previousReaction = reactionsByQualifiedName.put(qualifiedName, reaction);
          if ((previousReaction != null)) {
            reactionsToImportPath.remove(previousReaction);
          }
          reactionsToImportPath.put(reaction, currentImportPath);
        }
      }
    };
    final Predicate<ReactionsImport> _function_2 = (ReactionsImport it) -> {
      boolean _isRoutinesOnly = it.isRoutinesOnly();
      return (!_isRoutinesOnly);
    };
    final Function<Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>>, LinkedHashMap<Reaction, ReactionsImportPath>> _function_3 = (Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>> it) -> {
      return it.getSecond();
    };
    return ReactionsImportsHelper.<LinkedHashMap<Reaction, ReactionsImportPath>, Pair<LinkedHashMap<String, Reaction>, LinkedHashMap<Reaction, ReactionsImportPath>>>walkImportHierarchy(rootReactionsSegment, _function, 
      null, _function_1, _function_2, _function_3);
  }

  /**
   * Gets all routines that are included in the routines facade of the given root reactions segment.
   * <p>
   * This includes all routines that are directly and transitively imported without qualified names. Duplicately included or
   * named routines are only contained once.
   * <p>
   * The <code>includeRootRoutines</code> parameter controls whether the routines of the given root reactions segment are
   * included in the result.
   * <p>
   * The <code>resolveOverrides</code> parameter controls whether overridden routines get replaced with their overriding
   * routine. This considers the overrides of the root reactions segment regardless of the <code>includeRootRoutines</code> parameter.
   * <p>
   * The routines are returned together with the absolute reactions import paths (starting with the root segment) denoting the
   * position of the their reactions segments in the import hierarchy.
   * 
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @param includeRootRoutines whether to include the routines of the root reactions segment
   * @param resolveOverrides whether to replace routines with their overriding routine
   * @return all included routines with their absolute import paths
   */
  public static Map<Routine, ReactionsImportPath> getIncludedRoutines(final ReactionsSegment rootReactionsSegment, final boolean includeRootRoutines, final boolean resolveOverrides) {
    final Supplier<Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>> _function = () -> {
      LinkedHashMap<String, Routine> _linkedHashMap = new LinkedHashMap<String, Routine>();
      LinkedHashMap<String, Routine> _linkedHashMap_1 = new LinkedHashMap<String, Routine>();
      LinkedHashMap<Routine, ReactionsImportPath> _linkedHashMap_2 = new LinkedHashMap<Routine, ReactionsImportPath>();
      return Tuples.<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>create(_linkedHashMap, _linkedHashMap_1, _linkedHashMap_2);
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>> _function_1 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>> data) -> {
      if (((!includeRootRoutines) && (currentImportPath.getLength() == 1))) {
        return;
      }
      final LinkedHashMap<String, Routine> routinesByName = data.getFirst();
      final LinkedHashMap<String, Routine> routinesByFullyQualifiedName = data.getSecond();
      final LinkedHashMap<Routine, ReactionsImportPath> routinesToImportPath = data.getThird();
      final Function1<Routine, Boolean> _function_2 = (Routine it) -> {
        return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it));
      };
      Iterable<Routine> _filter = IterableExtensions.<Routine>filter(ReactionsLanguageUtil.getRegularRoutines(currentReactionsSegment), _function_2);
      for (final Routine routine : _filter) {
        Routine _putIfAbsent = routinesByName.putIfAbsent(ReactionsLanguageUtil.getFormattedName(routine), routine);
        boolean _tripleEquals = (_putIfAbsent == null);
        if (_tripleEquals) {
          String fullyQualifiedName = ReactionsLanguageUtil.getFullyQualifiedName(routine, currentImportPath);
          routinesByFullyQualifiedName.put(fullyQualifiedName, routine);
          routinesToImportPath.put(routine, currentImportPath);
        }
      }
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>> _function_2 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>> data) -> {
      if ((!resolveOverrides)) {
        return;
      }
      final LinkedHashMap<String, Routine> routinesByName = data.getFirst();
      final LinkedHashMap<String, Routine> routinesByFullyQualifiedName = data.getSecond();
      final LinkedHashMap<Routine, ReactionsImportPath> routinesToImportPath = data.getThird();
      final Function1<Routine, Boolean> _function_3 = (Routine it) -> {
        return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it));
      };
      Iterable<Routine> _filter = IterableExtensions.<Routine>filter(ReactionsLanguageUtil.getOverrideRoutines(currentReactionsSegment), _function_3);
      for (final Routine routine : _filter) {
        {
          String fullyQualifiedName = ReactionsLanguageUtil.getFullyQualifiedName(routine, currentImportPath);
          final Routine previousRoutine = routinesByFullyQualifiedName.replace(fullyQualifiedName, routine);
          if ((previousRoutine != null)) {
            routinesToImportPath.remove(previousRoutine);
            routinesToImportPath.put(routine, currentImportPath);
            routinesByName.put(ReactionsLanguageUtil.getFormattedName(routine), routine);
          }
        }
      }
    };
    final Predicate<ReactionsImport> _function_3 = (ReactionsImport it) -> {
      boolean _isUseQualifiedNames = it.isUseQualifiedNames();
      return (!_isUseQualifiedNames);
    };
    final Function<Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>, LinkedHashMap<Routine, ReactionsImportPath>> _function_4 = (Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>> it) -> {
      return it.getThird();
    };
    return ReactionsImportsHelper.<LinkedHashMap<Routine, ReactionsImportPath>, Triple<LinkedHashMap<String, Routine>, LinkedHashMap<String, Routine>, LinkedHashMap<Routine, ReactionsImportPath>>>walkImportHierarchy(rootReactionsSegment, _function, _function_1, _function_2, _function_3, _function_4);
  }

  /**
   * Gets all reactions segments whose routines facades are included in the routines facade of the given root reactions segment.
   * <p>
   * This includes all reactions segments for imports using qualified names, as well as all reactions segments for routines
   * facades that are transitively included via imports not using qualified names. Duplicately included or named reactions
   * segments are only contained once.
   * <p>
   * The reactions segments are returned together with their absolute reactions import path (starting with the root segment)
   * denoting their position in the import hierarchy.
   * 
   * @param rootReactionsSegment the root reactions segment, not <code>null</code>
   * @return all reactions segments whose routines facades are included, with their absolute import paths
   */
  public static Map<ReactionsSegment, ReactionsImportPath> getIncludedRoutinesFacades(final ReactionsSegment rootReactionsSegment) {
    final Supplier<Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>>> _function = () -> {
      LinkedHashMap<String, ReactionsSegment> _linkedHashMap = new LinkedHashMap<String, ReactionsSegment>();
      LinkedHashMap<ReactionsSegment, ReactionsImportPath> _linkedHashMap_1 = new LinkedHashMap<ReactionsSegment, ReactionsImportPath>();
      return Tuples.<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>>create(_linkedHashMap, _linkedHashMap_1);
    };
    final ReactionsImportsHelper.ImportHierarchyVisitor<Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>>> _function_1 = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>> data) -> {
      final LinkedHashMap<String, ReactionsSegment> segmentsByName = data.getFirst();
      final LinkedHashMap<ReactionsSegment, ReactionsImportPath> segmentsToImportPath = data.getSecond();
      final Function1<ReactionsImport, Boolean> _function_2 = (ReactionsImport it) -> {
        return Boolean.valueOf(((ReactionsElementsCompletionChecker.isReferenceable(it) && ReactionsImportsHelper.isResolvable(it)) && it.isUseQualifiedNames()));
      };
      final Iterable<ReactionsImport> currentSegmentIncludedImports = IterableExtensions.<ReactionsImport>filter(currentReactionsSegment.getReactionsImports(), _function_2);
      final Function1<ReactionsImport, ReactionsSegment> _function_3 = (ReactionsImport it) -> {
        return it.getImportedReactionsSegment();
      };
      final Iterable<ReactionsSegment> currentSegmentIncludedSegments = IterableExtensions.<ReactionsImport, ReactionsSegment>map(currentSegmentIncludedImports, _function_3);
      for (final ReactionsSegment includedSegment : currentSegmentIncludedSegments) {
        {
          final String includedSegmentFormattedName = ReactionsLanguageUtil.getFormattedName(includedSegment);
          final ReactionsImportPath includedSegmentImportPath = currentImportPath.append(includedSegment.getName());
          ReactionsSegment _putIfAbsent = segmentsByName.putIfAbsent(includedSegmentFormattedName, includedSegment);
          boolean _tripleEquals = (_putIfAbsent == null);
          if (_tripleEquals) {
            segmentsToImportPath.put(includedSegment, includedSegmentImportPath);
          }
        }
      }
    };
    final Predicate<ReactionsImport> _function_2 = (ReactionsImport it) -> {
      boolean _isUseQualifiedNames = it.isUseQualifiedNames();
      return (!_isUseQualifiedNames);
    };
    final Function<Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>> _function_3 = (Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>> it) -> {
      return it.getSecond();
    };
    return ReactionsImportsHelper.<LinkedHashMap<ReactionsSegment, ReactionsImportPath>, Pair<LinkedHashMap<String, ReactionsSegment>, LinkedHashMap<ReactionsSegment, ReactionsImportPath>>>walkImportHierarchy(rootReactionsSegment, _function, _function_1, 
      null, _function_2, _function_3);
  }

  /**
   * Walks down the import hierarchy starting at the given root reactions segment along the given reactions import path and
   * asks the <code>visitor</code> at each reactions segment along the path (including the root reactions segment)
   * for a return value and returns the first one that isn't </code>null</code>.
   * <p>
   * The given import path is expected to be relative to the specified root reactions segment.
   * <p>
   * Any cyclic imports or imports that cannot be resolved along the path will be considered non-existent and therefore not be
   * followed.
   * 
   * @param <T> the type of the return value
   * @param rootReactionsSegment the reactions segment to start the walk at, not <code>null</code>
   * @param relativeImportPath the relative import path to walk, not <code>null</code>
   * @param visitor the visitor, not <code>null</code>
   * @return the return value provided by the visitor, or <code>null</code> if the end of the path is reached without the
   *         visitor providing a <code>non-null</code> return value, or if the path does not exist in the import hierarchy of
   *         the root reactions segment
   */
  public static <T extends Object> T walkImportPath(final ReactionsSegment rootReactionsSegment, final ReactionsImportPath relativeImportPath, final ReactionsImportsHelper.ImportPathVisitor<T> visitor) {
    Preconditions.<ReactionsSegment>checkNotNull(rootReactionsSegment, "rootReactionsSegment is null");
    Preconditions.<ReactionsImportPath>checkNotNull(relativeImportPath, "relativeImportPath is null");
    Preconditions.<ReactionsImportsHelper.ImportPathVisitor<T>>checkNotNull(visitor, "visitor is null");
    final Set<String> uniquePathSegments = IterableExtensions.<String>toSet(relativeImportPath.getSegments());
    if (((uniquePathSegments.size() != relativeImportPath.getLength()) || uniquePathSegments.contains(rootReactionsSegment.getName()))) {
      return null;
    }
    final ReactionsImportPath rootImportPath = ReactionsImportPath.create(rootReactionsSegment.getName());
    return ReactionsImportsHelper.<T>_walkImportPath(null, rootImportPath, rootReactionsSegment, relativeImportPath, visitor);
  }

  private static <T extends Object> T _walkImportPath(final ReactionsImport sourceImport, final ReactionsImportPath currentImportPath, final ReactionsSegment currentReactionsSegment, final ReactionsImportPath remainingImportPath, final ReactionsImportsHelper.ImportPathVisitor<T> visitor) {
    final T returnValue = visitor.visit(sourceImport, currentImportPath, currentReactionsSegment, remainingImportPath);
    if ((returnValue != null)) {
      return returnValue;
    }
    boolean _isEmpty = remainingImportPath.isEmpty();
    if (_isEmpty) {
      return null;
    }
    final String nextReactionsSegmentName = remainingImportPath.getFirstSegment();
    final ReactionsImportPath nextImportPath = currentImportPath.append(nextReactionsSegmentName);
    final ReactionsImportPath nextRemainingImportPath = remainingImportPath.relativeToRoot();
    final Function1<ReactionsImport, Boolean> _function = (ReactionsImport it) -> {
      return Boolean.valueOf(ReactionsImportsHelper.isResolvable(it));
    };
    final Function1<ReactionsImport, Boolean> _function_1 = (ReactionsImport it) -> {
      return Boolean.valueOf(nextReactionsSegmentName.equals(it.getImportedReactionsSegment().getName()));
    };
    final ReactionsImport nextImport = IterableExtensions.<ReactionsImport>findFirst(IterableExtensions.<ReactionsImport>filter(currentReactionsSegment.getReactionsImports(), _function), _function_1);
    if ((nextImport == null)) {
      return null;
    }
    final ReactionsSegment nextReactionsSegment = nextImport.getImportedReactionsSegment();
    return ReactionsImportsHelper.<T>_walkImportPath(nextImport, nextImportPath, nextReactionsSegment, nextRemainingImportPath, visitor);
  }

  /**
   * Gets the reactions segment at the specified import path.
   * <p>
   * This walks the import hierarchy along the specified import path, starting from the given root reactions segment. The
   * import path is expected to be relative to the specified root reactions segment.
   * 
   * @param rootReactionsSegment the reactions segment to start the walk at, not <code>null</code>
   * @param relativeImportPath the relative import path to walk, not <code>null</code>
   * @return the reactions segment at the end of the specified import path, or <code>null</code> if no reactions segment is
   *         found for the specified path
   */
  public static ReactionsSegment getReactionsSegment(final ReactionsSegment rootReactionsSegment, final ReactionsImportPath relativeImportPath) {
    final ReactionsImportsHelper.ImportPathVisitor<ReactionsSegment> _function = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, ReactionsImportPath remainingPath) -> {
      boolean _isEmpty = remainingPath.isEmpty();
      if (_isEmpty) {
        return currentReactionsSegment;
      } else {
        return null;
      }
    };
    return ReactionsImportsHelper.<ReactionsSegment>walkImportPath(rootReactionsSegment, relativeImportPath, _function);
  }

  /**
   * Searches through the import hierarchy for the first reactions segment that overrides routines of the specified reactions
   * segment. If no such reactions segment is found, the overridden reactions segment itself is returned.
   * <p>
   * The search starts at the given root reactions segment. The <code>checkRootReactionsSegment</code> parameter controls
   * whether the given root reactions segment gets considered as possible override root (in case it itself overrides routines
   * of the specified reactions segment).
   * <p>
   * The <code>overriddenRoutineImportPath</code> is expected to be relative to the specified root reactions segment. If it is
   * empty, the root reactions segment is returned (regardless of the <code>checkRootReactionsSegment</code> parameter).
   * 
   * @param rootReactionsSegment the reactions segment to start the walk at, not <code>null</code>
   * @param overriddenRoutineImportPath the relative import path to the overridden segment, not <code>null</code>
   * @param checkRootReactionsSegment whether to consider the root reactions segment as possible override root
   * @return the first reactions segment that overrides routines of the specified reactions segment, or the overridden
   *         reactions segment itself, together with its absolute reactions import path (starting with the root segment)
   */
  public static org.eclipse.xtext.xbase.lib.Pair<ReactionsImportPath, ReactionsSegment> getRoutinesOverrideRoot(final ReactionsSegment rootReactionsSegment, final ReactionsImportPath overriddenRoutineImportPath, final boolean checkRootReactionsSegment) {
    final ReactionsImportsHelper.ImportPathVisitor<org.eclipse.xtext.xbase.lib.Pair<ReactionsImportPath, ReactionsSegment>> _function = (ReactionsImport sourceImport, ReactionsImportPath currentImportPath, ReactionsSegment currentReactionsSegment, ReactionsImportPath remainingPath) -> {
      boolean _isEmpty = remainingPath.isEmpty();
      if (_isEmpty) {
        return org.eclipse.xtext.xbase.lib.Pair.<ReactionsImportPath, ReactionsSegment>of(currentImportPath, currentReactionsSegment);
      } else {
        if ((checkRootReactionsSegment || (currentImportPath.getLength() > 1))) {
          final Function1<Routine, ReactionsImportPath> _function_1 = (Routine it) -> {
            return ReactionsLanguageUtil.toReactionsImportPath(it.getOverrideImportPath());
          };
          final Iterable<ReactionsImportPath> overriddenRoutinesImportPaths = IterableExtensions.<Routine, ReactionsImportPath>map(ReactionsLanguageUtil.getOverrideRoutines(currentReactionsSegment), _function_1);
          final Function1<ReactionsImportPath, Boolean> _function_2 = (ReactionsImportPath it) -> {
            return Boolean.valueOf(it.equals(remainingPath));
          };
          ReactionsImportPath _findFirst = IterableExtensions.<ReactionsImportPath>findFirst(overriddenRoutinesImportPaths, _function_2);
          boolean _tripleNotEquals = (_findFirst != null);
          if (_tripleNotEquals) {
            return org.eclipse.xtext.xbase.lib.Pair.<ReactionsImportPath, ReactionsSegment>of(currentImportPath, currentReactionsSegment);
          }
        }
        return null;
      }
    };
    return ReactionsImportsHelper.<org.eclipse.xtext.xbase.lib.Pair<ReactionsImportPath, ReactionsSegment>>walkImportPath(rootReactionsSegment, overriddenRoutineImportPath, _function);
  }

  private ReactionsImportsHelper() {
    
  }
}
