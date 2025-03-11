package tools.vitruv.dsls.reactions.util;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

/**
 * Utility methods working with the model objects of the Reactions Language, which might be of use outside of code generation.
 */
@Utility
@SuppressWarnings("all")
public final class ReactionsLanguageUtil {
  /**
   * Gets a formatted representation of the metamodel pair the given reactions segment applies to.
   * 
   * @param reactionsSegment the reactions segment
   * @return the formatted representation of the metamodel pair
   */
  public static String getFormattedMetamodelPair(final ReactionsSegment reactionsSegment) {
    final Function1<MetamodelImport, String> _function = (MetamodelImport it) -> {
      return it.getName();
    };
    final List<String> sourceMetamodelsNames = ListExtensions.<MetamodelImport, String>map(reactionsSegment.getFromMetamodels(), _function);
    final Function1<MetamodelImport, String> _function_1 = (MetamodelImport it) -> {
      return it.getName();
    };
    final List<String> targetMetamodelsNames = ListExtensions.<MetamodelImport, String>map(reactionsSegment.getToMetamodels(), _function_1);
    return (((("(" + sourceMetamodelsNames) + ", ") + targetMetamodelsNames) + ")");
  }

  /**
   * Gets the formatted name of the given reactions segment.
   * <p>
   * This returns the reaction segment's name with a lower-case first character.
   * 
   * @param reactionsSegment the reactions segment
   * @return the formatted reactions segment name
   */
  public static String getFormattedName(final ReactionsSegment reactionsSegment) {
    return ReactionsLanguageUtil.getFormattedReactionsSegmentName(reactionsSegment.getName());
  }

  /**
   * Formats the given reactions segment name.
   * <p>
   * This returns the reaction segment name with a lower-case first character.
   * 
   * @param reactionsSegmentName the reactions segment name
   * @return the formatted reactions segment name
   */
  public static String getFormattedReactionsSegmentName(final String reactionsSegmentName) {
    return StringExtensions.toFirstLower(reactionsSegmentName);
  }

  /**
   * Gets the formatted name of the given reaction.
   * <p>
   * This returns the reaction's name with an upper-case first character.
   * 
   * @param reaction the reaction
   * @return the formatted reaction name
   */
  public static String getFormattedName(final Reaction reaction) {
    return StringExtensions.toFirstUpper(reaction.getName());
  }

  /**
   * Gets the qualified name of the given reaction.
   * <p>
   * The qualified name consists of the reaction's reactions segment name and the
   * {@link #getFormattedName(Reaction) formatted reaction name}, separated by <code>::</code>. In case the reaction overrides
   * another reaction, the name of the overridden reactions segment is used instead of the reaction's own reactions segment
   * name.
   * <p>
   * The qualified name can therefore not be used to differentiate between an original reaction and the reaction overriding it.
   * 
   * @param reaction the reaction
   * @return the qualified name
   */
  public static String getQualifiedName(final Reaction reaction) {
    String reactionsSegmentName = null;
    boolean _isOverride = ReactionsLanguageUtil.isOverride(reaction);
    if (_isOverride) {
      ReactionsSegment _overriddenReactionsSegment = reaction.getOverriddenReactionsSegment();
      String _name = null;
      if (_overriddenReactionsSegment!=null) {
        _name=_overriddenReactionsSegment.getName();
      }
      reactionsSegmentName = _name;
    } else {
      reactionsSegmentName = reaction.getReactionsSegment().getName();
    }
    String _formattedName = ReactionsLanguageUtil.getFormattedName(reaction);
    return ((reactionsSegmentName + ReactionsLanguageConstants.OVERRIDDEN_REACTIONS_SEGMENT_SEPARATOR) + _formattedName);
  }

  /**
   * Gets the display name of the given reaction.
   * <p>
   * In case the given reaction overrides another reaction, the returned name is equal to the reaction's
   * {@link #getQualifiedName(Reaction) qualified name}. Otherwise it consists of only the
   * {@link #getFormattedName(Reaction) formatted reaction name}.
   * 
   * @param reaction the reaction
   * @return the formatted full name
   */
  public static String getDisplayName(final Reaction reaction) {
    boolean _isOverride = ReactionsLanguageUtil.isOverride(reaction);
    if (_isOverride) {
      return ReactionsLanguageUtil.getQualifiedName(reaction);
    } else {
      return ReactionsLanguageUtil.getFormattedName(reaction);
    }
  }

  /**
   * Gets the formatted name of the given routine.
   * <p>
   * This returns the routine's name with a lower-case first character.
   * 
   * @param routine the routine
   * @return the formatted routine name
   */
  public static String getFormattedName(final Routine routine) {
    return StringExtensions.toFirstLower(routine.getName());
  }

  /**
   * Gets the qualified name of the given routine.
   * <p>
   * The qualified name consists of the routine's reactions segment name and the
   * {@link #getFormattedName(Routine) formatted reaction name}, separated by <code>::</code>. In case the routine overrides
   * another routine, the name of the overridden reactions segment is used instead of the routine's own reactions segment
   * name.
   * <p>
   * The qualified name can therefore not be used to differentiate between an original routine and the routine overriding it.
   * 
   * @param routine the routine
   * @return the qualified name
   */
  public static String getQualifiedName(final Routine routine) {
    String reactionsSegmentName = null;
    boolean _isOverride = ReactionsLanguageUtil.isOverride(routine);
    if (_isOverride) {
      ReactionsSegment _reactionsSegment = routine.getOverrideImportPath().getReactionsSegment();
      String _name = null;
      if (_reactionsSegment!=null) {
        _name=_reactionsSegment.getName();
      }
      reactionsSegmentName = _name;
    } else {
      reactionsSegmentName = routine.getReactionsSegment().getName();
    }
    String _formattedName = ReactionsLanguageUtil.getFormattedName(routine);
    return ((reactionsSegmentName + ReactionsLanguageConstants.OVERRIDDEN_REACTIONS_SEGMENT_SEPARATOR) + _formattedName);
  }

  /**
   * Gets the fully qualified name of the given routine.
   * <p>
   * This is equivalent to calling {@link #getFullyQualifiedName(Routine, ReactionsImportPath)} with <code>null</code> as
   * <code>importPath</code> parameter.
   * 
   * @param routine the routine
   * @return the fully qualified name
   * 
   * @see #getFullyQualifiedName(Routine, ReactionsImportPath)
   */
  public static String getFullyQualifiedName(final Routine routine) {
    return ReactionsLanguageUtil.getFullyQualifiedName(routine, null);
  }

  /**
   * Gets the fully qualified name of the given routine.
   * <p>
   * The fully qualified name consists of the routine's reactions segment name and the
   * {@link #getFormattedName(Routine) formatted routine name}, separated by <code>::</code>. In case the routine overrides
   * another routine, the relative <b>import path</b> to the overridden reactions segment is used instead of the routine's
   * own reactions segment name.
   * <p>
   * The optional <code>importPath</code> parameter allows creation of fully qualified names relative to some other reactions
   * segment. If it is specified, it acts as prefix for the fully qualified name. If it doesn't already point to the routine's
   * reactions segment, the routine's reaction segment name gets appended.
   * <p>
   * Note: The returned fully qualified name relies on relative import paths and is therefore only valid inside the routine's
   * own reactions segment, or, if used in conjunction with the <code>importPath</code> parameter, some root reactions segment.
   * 
   * @param routine the routine
   * @param importPath the import path leading to the reactions segment containing the routine, or <code>null</code> or empty
   * @return the fully qualified name
   */
  public static String getFullyQualifiedName(final Routine routine, final ReactionsImportPath importPath) {
    final String reactionsSegmentName = routine.getReactionsSegment().getName();
    final boolean importPathSpecified = ((importPath != null) && (!importPath.isEmpty()));
    String fullyQualifiedName = "";
    if (importPathSpecified) {
      String _fullyQualifiedName = fullyQualifiedName;
      String _pathString = importPath.getPathString();
      fullyQualifiedName = (_fullyQualifiedName + _pathString);
      boolean _equals = importPath.getLastSegment().equals(reactionsSegmentName);
      boolean _not = (!_equals);
      if (_not) {
        String _fullyQualifiedName_1 = fullyQualifiedName;
        fullyQualifiedName = (_fullyQualifiedName_1 + (ReactionsImportPath.PATH_STRING_SEPARATOR + reactionsSegmentName));
      }
    }
    boolean _isOverride = ReactionsLanguageUtil.isOverride(routine);
    if (_isOverride) {
      if (importPathSpecified) {
        String _fullyQualifiedName_2 = fullyQualifiedName;
        fullyQualifiedName = (_fullyQualifiedName_2 + ReactionsImportPath.PATH_STRING_SEPARATOR);
      }
      String _fullyQualifiedName_3 = fullyQualifiedName;
      String _pathString_1 = ReactionsLanguageUtil.toReactionsImportPath(routine.getOverrideImportPath()).getPathString();
      fullyQualifiedName = (_fullyQualifiedName_3 + _pathString_1);
    } else {
      if ((!importPathSpecified)) {
        String _fullyQualifiedName_4 = fullyQualifiedName;
        fullyQualifiedName = (_fullyQualifiedName_4 + reactionsSegmentName);
      }
    }
    String _fullyQualifiedName_5 = fullyQualifiedName;
    String _formattedName = ReactionsLanguageUtil.getFormattedName(routine);
    String _plus = (ReactionsLanguageConstants.OVERRIDDEN_REACTIONS_SEGMENT_SEPARATOR + _formattedName);
    fullyQualifiedName = (_fullyQualifiedName_5 + _plus);
    return fullyQualifiedName;
  }

  /**
   * Gets the display name of the given routine.
   * <p>
   * In case the given routine overrides another routine, the returned name is equal to the routine's
   * {@link #getFullyQualifiedName(Routine) fully qualified name}. Otherwise it consists of only the
   * {@link #getFormattedName(Routine) formatted routine name}.
   * 
   * @param routine the routine
   * @return the formatted full name
   */
  public static String getDisplayName(final Routine routine) {
    boolean _isOverride = ReactionsLanguageUtil.isOverride(routine);
    if (_isOverride) {
      return ReactionsLanguageUtil.getFullyQualifiedName(routine);
    } else {
      return ReactionsLanguageUtil.getFormattedName(routine);
    }
  }

  /**
   * Checks whether the given reaction is a 'regular' reaction.
   * <p>
   * This means that it does not override any other reaction.
   * 
   * @param reaction the reaction
   * @return <code>true</code> if the given reaction is a regular reaction
   */
  public static boolean isRegular(final Reaction reaction) {
    boolean _isOverride = ReactionsLanguageUtil.isOverride(reaction);
    return (!_isOverride);
  }

  /**
   * Checks whether the given reaction overrides another reaction.
   * 
   * @param reaction the reaction
   * @return <code>true</code> if the given reaction overrides another reaction
   */
  public static boolean isOverride(final Reaction reaction) {
    return reaction.eIsSet(TopLevelElementsPackage.Literals.REACTION__OVERRIDDEN_REACTIONS_SEGMENT);
  }

  /**
   * Gets all regular reactions from the given reactions segment.
   * 
   * @param reactionsSegment the reactions segment
   * @return the regular reactions
   * @see #isRegular(Reaction)
   */
  public static Iterable<Reaction> getRegularReactions(final ReactionsSegment reactionsSegment) {
    final Function1<Reaction, Boolean> _function = (Reaction it) -> {
      return Boolean.valueOf(ReactionsLanguageUtil.isRegular(it));
    };
    return IterableExtensions.<Reaction>filter(reactionsSegment.getReactions(), _function);
  }

  /**
   * Gets all reactions from the given reactions segment that are overriding other reactions.
   * 
   * @param reactionsSegment the reactions segment
   * @return the reactions overriding other reactions
   * @see #isOverride(Reaction)
   */
  public static Iterable<Reaction> getOverrideReactions(final ReactionsSegment reactionsSegment) {
    final Function1<Reaction, Boolean> _function = (Reaction it) -> {
      return Boolean.valueOf(ReactionsLanguageUtil.isOverride(it));
    };
    return IterableExtensions.<Reaction>filter(reactionsSegment.getReactions(), _function);
  }

  /**
   * Checks whether the given routine is a 'regular' routine.
   * <p>
   * This means that it does not override any other routine.
   * 
   * @param routine the routine
   * @return <code>true</code> if the given routine is a regular routine
   */
  public static boolean isRegular(final Routine routine) {
    boolean _isOverride = ReactionsLanguageUtil.isOverride(routine);
    return (!_isOverride);
  }

  /**
   * Checks whether the given routine overrides another routine.
   * 
   * @param routine the routine
   * @return <code>true</code> if the given routine overrides another routine
   */
  public static boolean isOverride(final Routine routine) {
    RoutineOverrideImportPath _overrideImportPath = routine.getOverrideImportPath();
    return (_overrideImportPath != null);
  }

  /**
   * Gets all regular routines from the given reactions segment.
   * 
   * @param reactionsSegment the reactions segment
   * @return the regular routines
   * @see #isRegular(Routine)
   */
  public static Iterable<Routine> getRegularRoutines(final ReactionsSegment reactionsSegment) {
    final Function1<Routine, Boolean> _function = (Routine it) -> {
      return Boolean.valueOf(ReactionsLanguageUtil.isRegular(it));
    };
    return IterableExtensions.<Routine>filter(reactionsSegment.getRoutines(), _function);
  }

  /**
   * Gets all routines from the given reactions segment that are overriding other routines.
   * 
   * @param reactionsSegment the reactions segment
   * @return the routines overriding other routines
   * @see #isOverride(Routine)
   */
  public static Iterable<Routine> getOverrideRoutines(final ReactionsSegment reactionsSegment) {
    final Function1<Routine, Boolean> _function = (Routine it) -> {
      return Boolean.valueOf(ReactionsLanguageUtil.isOverride(it));
    };
    return IterableExtensions.<Routine>filter(reactionsSegment.getRoutines(), _function);
  }

  /**
   * Converts the given {@link RoutineOverrideImportPath} to a corresponding {@link ReactionsImportPath}.
   * <p>
   * Any incomplete or unresolvable segments inside the {@link RoutineOverrideImportPath} will get represented by the String
   * {@literal "<unresolved>"} inside the {@link ReactionsImportPath}.
   * 
   * @param routineOverrideImportPath the routine override import path, can be <code>null</code>
   * @return the corresponding reactions import path, or <code>null</code> if the given routine override import path was <code>null</code>
   */
  public static ReactionsImportPath toReactionsImportPath(final RoutineOverrideImportPath routineOverrideImportPath) {
    if ((routineOverrideImportPath == null)) {
      return null;
    }
    final Function1<RoutineOverrideImportPath, String> _function = (RoutineOverrideImportPath it) -> {
      final ReactionsSegment segment = it.getReactionsSegment();
      String _name = null;
      if (segment!=null) {
        _name=segment.getName();
      }
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(_name);
      if (_isNullOrEmpty) {
        return "<unresolved>";
      } else {
        return segment.getName();
      }
    };
    final List<String> fullPathSegments = ListExtensions.<RoutineOverrideImportPath, String>map(ReactionsLanguageUtil.getFullPath(routineOverrideImportPath), _function);
    return ReactionsImportPath.create(fullPathSegments);
  }

  /**
   * Gets the entries of the full routine override import path by traversing the parents of the given routine override import path.
   * <p>
   * The returned full path contains the given routine override import path as last entry.
   * 
   * @param routineOverrideImportPath the routine override import path, can be <code>null</code>
   * @return the segments of the full routine override import path, or <code>null</code> if the given routine override import path was <code>null</code>
   */
  public static List<RoutineOverrideImportPath> getFullPath(final RoutineOverrideImportPath routineOverrideImportPath) {
    if ((routineOverrideImportPath == null)) {
      return null;
    }
    final ArrayList<RoutineOverrideImportPath> pathSegments = new ArrayList<RoutineOverrideImportPath>();
    RoutineOverrideImportPath currentPath = routineOverrideImportPath;
    while ((currentPath != null)) {
      {
        pathSegments.add(currentPath);
        currentPath = currentPath.getParent();
      }
    }
    return ListExtensions.<RoutineOverrideImportPath>reverse(pathSegments);
  }

  private ReactionsLanguageUtil() {
    
  }
}
