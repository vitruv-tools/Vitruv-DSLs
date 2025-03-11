package tools.vitruv.dsls.reactions.codegen.classgenerators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class RoutinesFacadesProviderClassGenerator extends ClassGenerator {
  private final ReactionsSegment reactionsSegment;

  private JvmGenericType generatedClass;

  public RoutinesFacadesProviderClassGenerator(final ReactionsSegment reactionsSegment, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
    boolean _isReferenceable = ReactionsElementsCompletionChecker.isReferenceable(reactionsSegment);
    boolean _not = (!_isReferenceable);
    if (_not) {
      throw new IllegalArgumentException("incomplete");
    }
    this.reactionsSegment = reactionsSegment;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
    };
    return this.generatedClass = this._typesBuilder.toClass(this.reactionsSegment, 
      ClassNamesGenerators.getRoutinesFacadesProviderClassNameGenerator(this.reactionsSegment).getQualifiedName(), _function);
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutinesFacadesProvider.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmMember> _members = it.getMembers();
      final Procedure1<JvmConstructor> _function_1 = (JvmConstructor it_1) -> {
        AccessibleElement _accessibleElement = new AccessibleElement("executionState", ReactionExecutionState.class);
        final JvmFormalParameter executionStateParameter = this._parameterGenerator.generateParameter(it_1, _accessibleElement);
        EList<JvmFormalParameter> _parameters = it_1.getParameters();
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, executionStateParameter);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("super(");
            String _name = executionStateParameter.getName();
            _builder.append(_name);
            _builder.append(");");
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmConstructor _constructor = this._typesBuilder.toConstructor(this.reactionsSegment, _function_1);
      this._typesBuilder.<JvmConstructor>operator_add(_members, _constructor);
      EList<JvmMember> _members_1 = it.getMembers();
      final Procedure1<JvmOperation> _function_2 = (JvmOperation it_1) -> {
        it_1.setVisibility(JvmVisibility.PUBLIC);
        AccessibleElement _accessibleElement = new AccessibleElement("reactionsImportPath", ReactionsImportPath.class);
        final JvmFormalParameter reactionsImportPathParameter = this._parameterGenerator.generateParameter(it_1, _accessibleElement);
        EList<JvmFormalParameter> _parameters = it_1.getParameters();
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, reactionsImportPathParameter);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("switch(");
            String _name = reactionsImportPathParameter.getName();
            _builder.append(_name);
            _builder.append(".getPathString()) {");
            _builder.newLineIfNotEmpty();
            {
              Set<Map.Entry<ReactionsImportPath, ClassNameGenerator>> _entrySet = RoutinesFacadesProviderClassGenerator.getImportHierarchyRoutinesFacades(RoutinesFacadesProviderClassGenerator.this.reactionsSegment).entrySet();
              for(final Map.Entry<ReactionsImportPath, ClassNameGenerator> importHierarchyEntry : _entrySet) {
                final ReactionsImportPath importPath = importHierarchyEntry.getKey();
                _builder.newLineIfNotEmpty();
                final ClassNameGenerator routinesFacadeClassNameGenerator = importHierarchyEntry.getValue();
                _builder.newLineIfNotEmpty();
                _builder.append("\t");
                _builder.append("case \"");
                String _pathString = importPath.getPathString();
                _builder.append(_pathString, "\t");
                _builder.append("\": {");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t");
                _builder.append("return new ");
                String _qualifiedName = routinesFacadeClassNameGenerator.getQualifiedName();
                _builder.append(_qualifiedName, "\t\t");
                _builder.append("(this, ");
                String _name_1 = reactionsImportPathParameter.getName();
                _builder.append(_name_1, "\t\t");
                _builder.append(");");
                _builder.newLineIfNotEmpty();
                _builder.append("\t");
                _builder.append("}");
                _builder.newLine();
              }
            }
            _builder.append("\t");
            _builder.append("default: {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("throw new IllegalArgumentException(\"Unexpected import path: \" + ");
            String _name_2 = reactionsImportPathParameter.getName();
            _builder.append(_name_2, "\t\t");
            _builder.append(".getPathString());");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
            _builder.append("}");
            _builder.newLine();
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmOperation _method = this._typesBuilder.toMethod(this.reactionsSegment, "createRoutinesFacade", this._typeReferenceBuilder.typeRef(AbstractRoutinesFacade.class), _function_2);
      this._typesBuilder.<JvmOperation>operator_add(_members_1, _method);
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(
      this.generatedClass, _function);
  }

  private static Map<ReactionsImportPath, ClassNameGenerator> getImportHierarchyRoutinesFacades(final ReactionsSegment rootReactionsSegment) {
    final LinkedHashMap<ReactionsImportPath, ClassNameGenerator> importHierarchyRoutinesFacades = new LinkedHashMap<ReactionsImportPath, ClassNameGenerator>();
    Set<Map.Entry<ReactionsImportPath, ReactionsSegment>> _entrySet = ReactionsImportsHelper.getRoutinesImportHierarchy(rootReactionsSegment).entrySet();
    for (final Map.Entry<ReactionsImportPath, ReactionsSegment> importHierarchyEntry : _entrySet) {
      {
        final ReactionsImportPath absoluteImportPath = importHierarchyEntry.getKey();
        final ReactionsImportPath relativeImportPath = absoluteImportPath.relativeToRoot();
        final ReactionsSegment currentReactionsSegment = importHierarchyEntry.getValue();
        final Pair<ReactionsImportPath, ReactionsSegment> overrideRootResult = ReactionsImportsHelper.getRoutinesOverrideRoot(rootReactionsSegment, relativeImportPath, true);
        final ReactionsSegment overrideRootSegment = overrideRootResult.getValue();
        ClassNameGenerator routinesFacadeClassNameGenerator = null;
        boolean _equals = overrideRootSegment.getName().equals(currentReactionsSegment.getName());
        if (_equals) {
          routinesFacadeClassNameGenerator = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(currentReactionsSegment);
        } else {
          final ReactionsImportPath relativeImportPathFromOverrideRoot = absoluteImportPath.relativeTo(overrideRootSegment.getName());
          routinesFacadeClassNameGenerator = ClassNamesGenerators.getOverriddenRoutinesFacadeClassNameGenerator(overrideRootSegment, relativeImportPathFromOverrideRoot);
        }
        importHierarchyRoutinesFacades.put(absoluteImportPath, routinesFacadeClassNameGenerator);
      }
    }
    return importHierarchyRoutinesFacades;
  }
}
