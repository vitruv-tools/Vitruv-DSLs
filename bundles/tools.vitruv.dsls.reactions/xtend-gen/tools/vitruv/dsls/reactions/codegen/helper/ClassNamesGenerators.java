package tools.vitruv.dsls.reactions.codegen.helper;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;
import tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil;

@Utility
@SuppressWarnings("all")
public final class ClassNamesGenerators {
  private static class ChangePropagationSpecificationClassNameGenerator implements ClassNameGenerator {
    private final ReactionsSegment reactionsSegment;

    public ChangePropagationSpecificationClassNameGenerator(final ReactionsSegment reactionsSegment) {
      this.reactionsSegment = reactionsSegment;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.reactionsSegment.getName());
      _builder.append(_firstUpper);
      _builder.append("ChangePropagationSpecification");
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      StringConcatenation _builder = new StringConcatenation();
      String _reactionsPackageQualifiedName = ClassNamesGenerators.getReactionsPackageQualifiedName(this.reactionsSegment);
      _builder.append(_reactionsPackageQualifiedName);
      return _builder.toString();
    }
  }

  private static class ExecutorClassNameGenerator implements ClassNameGenerator {
    private final ReactionsSegment reactionsSegment;

    public ExecutorClassNameGenerator(final ReactionsSegment reactionsSegment) {
      this.reactionsSegment = reactionsSegment;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(ClassNamesGenerators.REACTIONS_EXECUTOR_CLASS_NAME);
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      StringConcatenation _builder = new StringConcatenation();
      String _reactionsPackageQualifiedName = ClassNamesGenerators.getReactionsPackageQualifiedName(this.reactionsSegment);
      _builder.append(_reactionsPackageQualifiedName);
      return _builder.toString();
    }
  }

  private static class RoutinesFacadesProviderClassNameGenerator implements ClassNameGenerator {
    private final ReactionsSegment reactionsSegment;

    public RoutinesFacadesProviderClassNameGenerator(final ReactionsSegment reactionsSegment) {
      this.reactionsSegment = reactionsSegment;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.reactionsSegment.getName());
      _builder.append(_firstUpper);
      _builder.append(ClassNamesGenerators.ROUTINES_FACADES_PROVIDER_CLASS_NAME);
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      StringConcatenation _builder = new StringConcatenation();
      String _basicRoutinesPackageQualifiedName = ClassNamesGenerators.getBasicRoutinesPackageQualifiedName();
      _builder.append(_basicRoutinesPackageQualifiedName);
      _builder.append(".");
      String _packageName = ClassNamesGenerators.getPackageName(this.reactionsSegment);
      _builder.append(_packageName);
      return _builder.toString();
    }
  }

  private static class ReactionClassNameGenerator implements ClassNameGenerator {
    private final Reaction reaction;

    public ReactionClassNameGenerator(final Reaction reaction) {
      this.reaction = reaction;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.reaction.getName());
      _builder.append(_firstUpper);
      _builder.append("Reaction");
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      String packageName = ClassNamesGenerators.getReactionsPackageQualifiedName(this.reaction.getReactionsSegment());
      boolean _isOverride = ReactionsLanguageUtil.isOverride(this.reaction);
      if (_isOverride) {
        String _packageName = packageName;
        String _reactionsSegmentPackageName = ClassNamesGenerators.getReactionsSegmentPackageName(ReactionsImportsHelper.getParsedOverriddenReactionsSegmentName(this.reaction));
        String _plus = ("." + _reactionsSegmentPackageName);
        packageName = (_packageName + _plus);
      }
      return packageName;
    }
  }

  private static class RoutineClassNameGenerator implements ClassNameGenerator {
    private final Routine routine;

    public RoutineClassNameGenerator(final Routine routine) {
      this.routine = routine;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.routine.getName());
      _builder.append(_firstUpper);
      _builder.append("Routine");
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      String packageName = ClassNamesGenerators.getRoutinesPackageQualifiedName(this.routine.getReactionsSegment());
      boolean _isOverride = ReactionsLanguageUtil.isOverride(this.routine);
      if (_isOverride) {
        String _packageName = packageName;
        final Function1<String, CharSequence> _function = (String it) -> {
          return ClassNamesGenerators.getReactionsSegmentPackageName(it);
        };
        String _join = IterableExtensions.<String>join(ReactionsImportsHelper.getParsedOverrideImportPath(this.routine).getSegments(), ".", _function);
        String _plus = ("." + _join);
        packageName = (_packageName + _plus);
      }
      return packageName;
    }
  }

  private static class RoutinesFacadeClassNameGenerator implements ClassNameGenerator {
    private final ReactionsSegment reactionsSegment;

    public RoutinesFacadeClassNameGenerator(final ReactionsSegment reactionsSegment) {
      this.reactionsSegment = reactionsSegment;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.reactionsSegment.getName());
      _builder.append(_firstUpper);
      _builder.append(ClassNamesGenerators.ROUTINES_FACADE_CLASS_NAME);
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      StringConcatenation _builder = new StringConcatenation();
      String _routinesPackageQualifiedName = ClassNamesGenerators.getRoutinesPackageQualifiedName(this.reactionsSegment);
      _builder.append(_routinesPackageQualifiedName);
      return _builder.toString();
    }
  }

  private static class OverriddenRoutinesFacadeClassNameGenerator implements ClassNameGenerator {
    private final ReactionsSegment reactionsSegment;

    private final ReactionsImportPath relativeImportPath;

    public OverriddenRoutinesFacadeClassNameGenerator(final ReactionsSegment reactionsSegment, final ReactionsImportPath relativeImportPath) {
      this.reactionsSegment = reactionsSegment;
      this.relativeImportPath = relativeImportPath;
    }

    @Override
    public String getSimpleName() {
      StringConcatenation _builder = new StringConcatenation();
      String _firstUpper = StringExtensions.toFirstUpper(this.reactionsSegment.getName());
      _builder.append(_firstUpper);
      _builder.append(ClassNamesGenerators.ROUTINES_FACADE_CLASS_NAME);
      return _builder.toString();
    }

    @Override
    public String getPackageName() {
      StringConcatenation _builder = new StringConcatenation();
      String _routinesPackageQualifiedName = ClassNamesGenerators.getRoutinesPackageQualifiedName(this.reactionsSegment);
      _builder.append(_routinesPackageQualifiedName);
      _builder.append(".");
      final Function1<String, CharSequence> _function = (String it) -> {
        return ClassNamesGenerators.getReactionsSegmentPackageName(it);
      };
      String _join = IterableExtensions.<String>join(this.relativeImportPath.getSegments(), ".", _function);
      _builder.append(_join);
      return _builder.toString();
    }
  }

  private static final String BASIC_PACKAGE = "mir";

  private static final String REACTIONS_PACKAGE = "reactions";

  private static final String ROUTINES_PACKAGE = "routines";

  private static final String REACTIONS_EXECUTOR_CLASS_NAME = "ReactionsExecutor";

  private static final String ROUTINES_FACADE_CLASS_NAME = "RoutinesFacade";

  private static final String ROUTINES_FACADES_PROVIDER_CLASS_NAME = "RoutinesFacadesProvider";

  public static String getBasicMirPackageQualifiedName() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(ClassNamesGenerators.BASIC_PACKAGE);
    return _builder.toString();
  }

  public static String getBasicReactionsPackageQualifiedName() {
    StringConcatenation _builder = new StringConcatenation();
    String _basicMirPackageQualifiedName = ClassNamesGenerators.getBasicMirPackageQualifiedName();
    _builder.append(_basicMirPackageQualifiedName);
    _builder.append(".");
    _builder.append(ClassNamesGenerators.REACTIONS_PACKAGE);
    return _builder.toString();
  }

  public static String getBasicRoutinesPackageQualifiedName() {
    StringConcatenation _builder = new StringConcatenation();
    String _basicMirPackageQualifiedName = ClassNamesGenerators.getBasicMirPackageQualifiedName();
    _builder.append(_basicMirPackageQualifiedName);
    _builder.append(".");
    _builder.append(ClassNamesGenerators.ROUTINES_PACKAGE);
    return _builder.toString();
  }

  private static String getReactionsPackageQualifiedName(final ReactionsSegment reactionSegment) {
    StringConcatenation _builder = new StringConcatenation();
    String _basicReactionsPackageQualifiedName = ClassNamesGenerators.getBasicReactionsPackageQualifiedName();
    _builder.append(_basicReactionsPackageQualifiedName);
    _builder.append(".");
    String _packageName = ClassNamesGenerators.getPackageName(reactionSegment);
    _builder.append(_packageName);
    return _builder.toString();
  }

  private static String getRoutinesPackageQualifiedName(final ReactionsSegment reactionSegment) {
    StringConcatenation _builder = new StringConcatenation();
    String _basicRoutinesPackageQualifiedName = ClassNamesGenerators.getBasicRoutinesPackageQualifiedName();
    _builder.append(_basicRoutinesPackageQualifiedName);
    _builder.append(".");
    String _packageName = ClassNamesGenerators.getPackageName(reactionSegment);
    _builder.append(_packageName);
    return _builder.toString();
  }

  private static String getPackageName(final ReactionsSegment reactionSegment) {
    StringConcatenation _builder = new StringConcatenation();
    String _reactionsSegmentPackageName = ClassNamesGenerators.getReactionsSegmentPackageName(reactionSegment.getName());
    _builder.append(_reactionsSegmentPackageName);
    return _builder.toString();
  }

  private static String getReactionsSegmentPackageName(final String reactionSegmentName) {
    StringConcatenation _builder = new StringConcatenation();
    String _firstLower = StringExtensions.toFirstLower(reactionSegmentName);
    _builder.append(_firstLower);
    return _builder.toString();
  }

  public static ClassNameGenerator getChangePropagationSpecificationClassNameGenerator(final ReactionsSegment reactionSegment) {
    return new ClassNamesGenerators.ChangePropagationSpecificationClassNameGenerator(reactionSegment);
  }

  public static ClassNameGenerator getExecutorClassNameGenerator(final ReactionsSegment reactionSegment) {
    return new ClassNamesGenerators.ExecutorClassNameGenerator(reactionSegment);
  }

  public static ClassNameGenerator getRoutinesFacadesProviderClassNameGenerator(final ReactionsSegment reactionSegment) {
    return new ClassNamesGenerators.RoutinesFacadesProviderClassNameGenerator(reactionSegment);
  }

  public static ClassNameGenerator getRoutinesFacadeClassNameGenerator(final ReactionsSegment reactionSegment) {
    return new ClassNamesGenerators.RoutinesFacadeClassNameGenerator(reactionSegment);
  }

  public static ClassNameGenerator getOverriddenRoutinesFacadeClassNameGenerator(final ReactionsSegment reactionsSegment, final ReactionsImportPath relativeImportPath) {
    return new ClassNamesGenerators.OverriddenRoutinesFacadeClassNameGenerator(reactionsSegment, relativeImportPath);
  }

  public static ClassNameGenerator getReactionClassNameGenerator(final Reaction reaction) {
    return new ClassNamesGenerators.ReactionClassNameGenerator(reaction);
  }

  public static ClassNameGenerator getRoutineClassNameGenerator(final Routine routine) {
    return new ClassNamesGenerators.RoutineClassNameGenerator(routine);
  }

  private ClassNamesGenerators() {
    
  }
}
