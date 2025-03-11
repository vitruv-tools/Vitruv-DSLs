package tools.vitruv.dsls.commonalities.conversion;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.BidiTreeIterable;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@Singleton
@SuppressWarnings("all")
public class QualifiedClassValueConverter implements IValueConverter<String> {
  private static final String DELIMITER = QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR;

  private static final String DOMAIN_NAME_RULE = "DomainName";

  private static final String UNQUALIFIED_CLASS_RULE = "UnqualifiedClass";

  @Inject
  private Provider<IValueConverterService> valueConverterService;

  @Override
  public String toString(final String value) throws ValueConverterException {
    if ((value == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Invalid QualifiedClass value: \'");
      _builder.append(value);
      _builder.append("\'.");
      throw new ValueConverterException(_builder.toString(), null, null);
    }
    final String[] segments = value.split(QualifiedClassValueConverter.DELIMITER, 2);
    int _length = segments.length;
    boolean _notEquals = (_length != 2);
    if (_notEquals) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Invalid QualifiedClass value: \'");
      _builder_1.append(value);
      _builder_1.append("\'.");
      throw new ValueConverterException(_builder_1.toString(), null, null);
    }
    final String domainName = this.valueConverterService.get().toString(segments[0], QualifiedClassValueConverter.DOMAIN_NAME_RULE);
    final String className = this.valueConverterService.get().toString(segments[1], QualifiedClassValueConverter.UNQUALIFIED_CLASS_RULE);
    return ((domainName + QualifiedClassValueConverter.DELIMITER) + className);
  }

  @Override
  public String toValue(final String string, final INode node) throws ValueConverterException {
    if ((node != null)) {
      INode domainNameNode = null;
      INode classNameNode = null;
      BidiTreeIterable<INode> _asTreeIterable = node.getAsTreeIterable();
      for (final INode leafNode : _asTreeIterable) {
        {
          final EObject grammarElement = leafNode.getGrammarElement();
          if ((grammarElement instanceof RuleCall)) {
            final String ruleName = ((RuleCall)grammarElement).getRule().getName();
            boolean _equals = Objects.equal(ruleName, QualifiedClassValueConverter.DOMAIN_NAME_RULE);
            if (_equals) {
              domainNameNode = leafNode;
            } else {
              boolean _equals_1 = Objects.equal(ruleName, QualifiedClassValueConverter.UNQUALIFIED_CLASS_RULE);
              if (_equals_1) {
                classNameNode = leafNode;
              }
            }
          }
        }
      }
      if (((domainNameNode == null) || (classNameNode == null))) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Could not find DomainName and/or UnqualifiedClassName node(s): \'");
        String _text = node.getText();
        _builder.append(_text);
        _builder.append("\'.");
        throw new ValueConverterException(_builder.toString(), null, null);
      }
      Object _value = this.valueConverterService.get().toValue(QualifiedClassValueConverter.getVisibleText(domainNameNode), QualifiedClassValueConverter.DOMAIN_NAME_RULE, domainNameNode);
      final String domainName = ((String) _value);
      Object _value_1 = this.valueConverterService.get().toValue(QualifiedClassValueConverter.getVisibleText(classNameNode), QualifiedClassValueConverter.UNQUALIFIED_CLASS_RULE, classNameNode);
      final String className = ((String) _value_1);
      return ((domainName + QualifiedClassValueConverter.DELIMITER) + className);
    } else {
      final String[] segments = string.split(QualifiedClassValueConverter.DELIMITER, 2);
      int _length = segments.length;
      boolean _notEquals = (_length != 2);
      if (_notEquals) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Invalid QualifiedClass string: \'");
        _builder_1.append(string);
        _builder_1.append("\'.");
        throw new ValueConverterException(_builder_1.toString(), null, null);
      }
      final Object domainName_1 = this.valueConverterService.get().toValue(segments[0], QualifiedClassValueConverter.DOMAIN_NAME_RULE, null);
      final Object className_1 = this.valueConverterService.get().toValue(segments[1], QualifiedClassValueConverter.UNQUALIFIED_CLASS_RULE, null);
      String _plus = (domainName_1 + QualifiedClassValueConverter.DELIMITER);
      return (_plus + className_1);
    }
  }

  private static String getVisibleText(final INode node) {
    final StringBuilder text = new StringBuilder();
    Iterable<ILeafNode> _leafNodes = node.getLeafNodes();
    for (final ILeafNode leafNode : _leafNodes) {
      boolean _isHidden = leafNode.isHidden();
      boolean _not = (!_isHidden);
      if (_not) {
        text.append(leafNode.getText());
      }
    }
    return text.toString();
  }
}
