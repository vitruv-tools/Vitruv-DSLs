package tools.vitruv.dsls.reactions.codegen.classgenerators;

import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;

/**
 * JVM Model inference should happen in two phases:
 * <ol>
 * <li>Create empty classes so they can be found when linking</li>
 * <li>After linking is done, generate the bodies</li>
 * </ol>
 * Therefore, the {@link ClassGenerator}s provide separated methods
 * {@link ClassGenerator#generateEmptyClass() generateEmptyClass}
 * and {@link ClassGenerator#generateBody(JvmGenericType) generateBody} for those steps.
 */
@SuppressWarnings("all")
public abstract class ClassGenerator extends TypesBuilderExtensionProvider {
  protected Iterable<JvmFormalParameter> generateAccessibleElementsParameters(final EObject sourceObject, final Iterable<AccessibleElement> accessibleElements) {
    return this._parameterGenerator.generateParameters(sourceObject, accessibleElements);
  }

  public ClassGenerator(final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    typesBuilderExtensionProvider.copyBuildersTo(this);
  }

  public abstract JvmGenericType generateEmptyClass();

  public abstract JvmGenericType generateBody();

  protected String getCommentWithoutMarkers(final String documentation) {
    if (((documentation != null) && (documentation.length() > 4))) {
      final String withoutMultilineCommentMarkers = documentation.replaceAll("\\n \\* ", "\\n");
      int _length = withoutMultilineCommentMarkers.length();
      int _minus = (_length - 2);
      return withoutMultilineCommentMarkers.substring(2, _minus);
    } else {
      return documentation;
    }
  }

  protected JvmGenericType generateElementsContainerClass(final String qualifiedClassName, final Iterable<AccessibleElement> elements) {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      final Iterable<JvmFormalParameter> retrievedElementParameters = this._parameterGenerator.generateParameters(it, elements);
      EList<JvmMember> _members = it.getMembers();
      final Function1<JvmFormalParameter, JvmField> _function_1 = (JvmFormalParameter it_1) -> {
        JvmField _field = this._typesBuilder.toField(it_1, it_1.getName(), it_1.getParameterType());
        final Procedure1<JvmField> _function_2 = (JvmField it_2) -> {
          it_2.setFinal(true);
          it_2.setVisibility(JvmVisibility.PUBLIC);
        };
        return ObjectExtensions.<JvmField>operator_doubleArrow(_field, _function_2);
      };
      List<JvmField> _mapFixed = IterableUtil.<JvmFormalParameter, JvmField>mapFixed(retrievedElementParameters, _function_1);
      this._typesBuilder.<JvmMember>operator_add(_members, _mapFixed);
      EList<JvmMember> _members_1 = it.getMembers();
      final Procedure1<JvmConstructor> _function_2 = (JvmConstructor it_1) -> {
        it_1.setVisibility(JvmVisibility.PUBLIC);
        EList<JvmFormalParameter> _parameters = it_1.getParameters();
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, retrievedElementParameters);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            {
              EList<JvmFormalParameter> _parameters = it_1.getParameters();
              for(final JvmFormalParameter parameter : _parameters) {
                _builder.append("this.");
                String _name = parameter.getName();
                _builder.append(_name);
                _builder.append(" = ");
                String _name_1 = parameter.getName();
                _builder.append(_name_1);
                _builder.append(";");
                _builder.newLineIfNotEmpty();
              }
            }
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmConstructor _constructor = this._typesBuilder.toConstructor(it, _function_2);
      this._typesBuilder.<JvmConstructor>operator_add(_members_1, _constructor);
    };
    return this._typesBuilder.generateUnassociatedClass(qualifiedClassName, _function);
  }
}
