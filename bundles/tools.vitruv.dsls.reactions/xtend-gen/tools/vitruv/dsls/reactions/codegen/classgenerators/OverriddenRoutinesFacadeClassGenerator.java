package tools.vitruv.dsls.reactions.codegen.classgenerators;

import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;
import tools.vitruv.dsls.reactions.util.ReactionsLanguageUtil;

@SuppressWarnings("all")
public class OverriddenRoutinesFacadeClassGenerator extends RoutineFacadeClassGenerator {
  private final ReactionsSegment reactionsSegment;

  private final ReactionsImportPath relativeImportPath;

  private final ClassNameGenerator routinesFacadeNameGenerator;

  private JvmGenericType generatedClass;

  public OverriddenRoutinesFacadeClassGenerator(final ReactionsSegment reactionsSegment, final ReactionsImportPath relativeImportPath, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(reactionsSegment, typesBuilderExtensionProvider);
    boolean _isReferenceable = ReactionsElementsCompletionChecker.isReferenceable(reactionsSegment);
    boolean _not = (!_isReferenceable);
    if (_not) {
      throw new IllegalArgumentException("incomplete");
    }
    this.reactionsSegment = reactionsSegment;
    this.relativeImportPath = relativeImportPath;
    this.routinesFacadeNameGenerator = ClassNamesGenerators.getOverriddenRoutinesFacadeClassNameGenerator(reactionsSegment, relativeImportPath);
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
    };
    return this.generatedClass = this._typesBuilder.toClass(this.reactionsSegment, this.routinesFacadeNameGenerator.getQualifiedName(), _function);
  }

  @Override
  public JvmGenericType generateBody() {
    JvmGenericType _xblockexpression = null;
    {
      final Pair<ReactionsImportPath, ReactionsSegment> overrideRootResult = ReactionsImportsHelper.getRoutinesOverrideRoot(this.reactionsSegment, this.relativeImportPath, false);
      if ((overrideRootResult == null)) {
        return this.generatedClass;
      }
      final ReactionsSegment overrideRootSegment = overrideRootResult.getValue();
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        boolean _equals = overrideRootSegment.getName().equals(this.relativeImportPath.getLastSegment());
        if (_equals) {
          EList<JvmTypeReference> _superTypes = it.getSuperTypes();
          JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(overrideRootSegment).getQualifiedName());
          this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
        } else {
          final ReactionsImportPath relativeImportPathFromOverrideRoot = this.relativeImportPath.relativeTo(overrideRootSegment.getName());
          EList<JvmTypeReference> _superTypes_1 = it.getSuperTypes();
          JvmTypeReference _typeRef_1 = this._typeReferenceBuilder.typeRef(ClassNamesGenerators.getOverriddenRoutinesFacadeClassNameGenerator(overrideRootSegment, relativeImportPathFromOverrideRoot).getQualifiedName());
          this._typesBuilder.<JvmTypeReference>operator_add(_superTypes_1, _typeRef_1);
        }
        EList<JvmMember> _members = it.getMembers();
        JvmConstructor _generateConstructor = this.generateConstructor();
        this._typesBuilder.<JvmConstructor>operator_add(_members, _generateConstructor);
        final Function1<Routine, Boolean> _function_1 = (Routine it_1) -> {
          return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it_1));
        };
        final Function1<Routine, Boolean> _function_2 = (Routine it_1) -> {
          return Boolean.valueOf(ReactionsLanguageUtil.toReactionsImportPath(it_1.getOverrideImportPath()).equals(this.relativeImportPath));
        };
        final Consumer<Routine> _function_3 = (Routine it_1) -> {
          EList<JvmMember> _members_1 = this.generatedClass.getMembers();
          JvmOperation _generateCallMethod = this.generateCallMethod(it_1);
          this._typesBuilder.<JvmOperation>operator_add(_members_1, _generateCallMethod);
        };
        IterableExtensions.<Routine>filter(IterableExtensions.<Routine>filter(ReactionsLanguageUtil.getOverrideRoutines(this.reactionsSegment), _function_1), _function_2).forEach(_function_3);
      };
      _xblockexpression = ObjectExtensions.<JvmGenericType>operator_doubleArrow(
        this.generatedClass, _function);
    }
    return _xblockexpression;
  }

  @Override
  protected StringConcatenationClient getExtendedConstructorBody() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    return _client;
  }

  @Override
  protected StringConcatenationClient generateGetOwnRoutinesFacade() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("this._getRoutinesFacadesProvider().getRoutinesFacade(this._getReactionsImportPath().subPathTo(\"");
        String _name = OverriddenRoutinesFacadeClassGenerator.this.reactionsSegment.getName();
        _builder.append(_name);
        _builder.append("\"))");
      }
    };
    return _client;
  }
}
