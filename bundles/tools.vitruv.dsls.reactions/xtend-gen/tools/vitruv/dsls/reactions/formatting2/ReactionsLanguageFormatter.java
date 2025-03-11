package tools.vitruv.dsls.reactions.formatting2;

import com.google.common.base.Objects;
import java.util.Arrays;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmWildcardTypeReference;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBasicForLoopExpression;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XCollectionLiteral;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XPostfixOperation;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XSynchronizedExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XTypeLiteral;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.formatting2.XbaseFormatter;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xtype.XFunctionTypeRef;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.reactions.language.ArbitraryModelChange;
import tools.vitruv.dsls.reactions.language.ElementChangeType;
import tools.vitruv.dsls.reactions.language.ElementReferenceChangeType;
import tools.vitruv.dsls.reactions.language.MatchCheckStatement;
import tools.vitruv.dsls.reactions.language.ModelAttributeChange;
import tools.vitruv.dsls.reactions.language.ModelElementChange;
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveModelElementType;
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock;

@SuppressWarnings("all")
public class ReactionsLanguageFormatter extends XbaseFormatter {
  protected void _format(final ReactionsFile reactionsFile, @Extension final IFormattableDocument document) {
    XImportSection _namespaceImports = reactionsFile.getNamespaceImports();
    EList<XImportDeclaration> _importDeclarations = null;
    if (_namespaceImports!=null) {
      _importDeclarations=_namespaceImports.getImportDeclarations();
    }
    if (_importDeclarations!=null) {
      final Consumer<XImportDeclaration> _function = (XImportDeclaration it) -> {
        this.format(it, document);
      };
      _importDeclarations.forEach(_function);
    }
    XImportSection _namespaceImports_1 = reactionsFile.getNamespaceImports();
    EList<XImportDeclaration> _importDeclarations_1 = null;
    if (_namespaceImports_1!=null) {
      _importDeclarations_1=_namespaceImports_1.getImportDeclarations();
    }
    Iterable<XImportDeclaration> _tail = null;
    if (_importDeclarations_1!=null) {
      _tail=IterableExtensions.<XImportDeclaration>tail(_importDeclarations_1);
    }
    if (_tail!=null) {
      final Consumer<XImportDeclaration> _function_1 = (XImportDeclaration it) -> {
        final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
          it_1.newLine();
        };
        document.<XImportDeclaration>prepend(it, _function_2);
      };
      _tail.forEach(_function_1);
    }
    XImportSection _namespaceImports_2 = reactionsFile.getNamespaceImports();
    if (_namespaceImports_2!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
        it.setNewLines(2);
      };
      document.<XImportSection>append(_namespaceImports_2, _function_2);
    }
    final Consumer<MetamodelImport> _function_3 = (MetamodelImport it) -> {
      this.format(it, document);
    };
    reactionsFile.getMetamodelImports().forEach(_function_3);
    final Consumer<MetamodelImport> _function_4 = (MetamodelImport it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it_1) -> {
        it_1.newLine();
      };
      document.<MetamodelImport>prepend(it, _function_5);
    };
    IterableExtensions.<MetamodelImport>tail(reactionsFile.getMetamodelImports()).forEach(_function_4);
    MetamodelImport _last = IterableExtensions.<MetamodelImport>last(reactionsFile.getMetamodelImports());
    if (_last!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
        it.setNewLines(2);
      };
      document.<MetamodelImport>append(_last, _function_5);
    }
    final Consumer<ReactionsSegment> _function_6 = (ReactionsSegment it) -> {
      this.format(it, document);
    };
    reactionsFile.getReactionsSegments().forEach(_function_6);
    final Consumer<ReactionsSegment> _function_7 = (ReactionsSegment it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_8 = (IHiddenRegionFormatter it_1) -> {
        it_1.setNewLines(4);
      };
      document.<ReactionsSegment>prepend(it, _function_8);
    };
    IterableExtensions.<ReactionsSegment>tail(reactionsFile.getReactionsSegments()).forEach(_function_7);
  }

  protected void _format(final MetamodelImport metamodelImport, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(metamodelImport).keyword("import"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(metamodelImport).keyword("as"), _function_1);
  }

  protected void _format(final ReactionsSegment segment, @Extension final IFormattableDocument document) {
    ISemanticRegion _keyword = this.regionFor(segment).keyword("reactions:");
    final Procedure1<ISemanticRegion> _function = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it_1) -> {
        it_1.setNewLines(2);
      };
      document.prepend(it, _function_1);
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_2);
    };
    ObjectExtensions.<ISemanticRegion>operator_doubleArrow(_keyword, _function);
    final Consumer<Pair<ISemanticRegion, ISemanticRegion>> _function_1 = (Pair<ISemanticRegion, ISemanticRegion> it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
        it_1.newLine();
      };
      document.prepend(it.getKey(), _function_2);
    };
    this.regionFor(segment).keywordPairs("in", "reaction").forEach(_function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(segment).keyword("reaction"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(segment).keyword("to"), _function_3);
    final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(segment).keyword("changes"), _function_4);
    final Consumer<ISemanticRegion> _function_5 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_6 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_6);
    };
    this.regionFor(segment).keywords("in").forEach(_function_5);
    final Consumer<ISemanticRegion> _function_6 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_7 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_7);
    };
    this.regionFor(segment).keywords("and").forEach(_function_6);
    ISemanticRegion _keyword_1 = this.regionFor(segment).keyword("execute");
    final Procedure1<ISemanticRegion> _function_7 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_8 = (IHiddenRegionFormatter it_1) -> {
        it_1.newLine();
      };
      document.prepend(it, _function_8);
      final Procedure1<IHiddenRegionFormatter> _function_9 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_9);
    };
    ObjectExtensions.<ISemanticRegion>operator_doubleArrow(_keyword_1, _function_7);
    final Procedure1<IHiddenRegionFormatter> _function_8 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(segment).keyword("actions"), _function_8);
    final Procedure1<IHiddenRegionFormatter> _function_9 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(segment).keyword("minimal"), _function_9);
    ReactionsImport _head = IterableExtensions.<ReactionsImport>head(segment.getReactionsImports());
    if (_head!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_10 = (IHiddenRegionFormatter it) -> {
        it.highPriority();
        it.setNewLines(2);
      };
      document.<ReactionsImport>prepend(_head, _function_10);
    }
    final Consumer<ReactionsImport> _function_11 = (ReactionsImport it) -> {
      this.format(it, document);
    };
    segment.getReactionsImports().forEach(_function_11);
    ReactionsImport _last = IterableExtensions.<ReactionsImport>last(segment.getReactionsImports());
    if (_last!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_12 = (IHiddenRegionFormatter it) -> {
        it.setNewLines(2);
      };
      document.<ReactionsImport>append(_last, _function_12);
    }
    final Consumer<Reaction> _function_13 = (Reaction it) -> {
      this.format(it, document);
    };
    segment.getReactions().forEach(_function_13);
    final Consumer<Routine> _function_14 = (Routine it) -> {
      this.format(it, document);
    };
    segment.getRoutines().forEach(_function_14);
  }

  protected void _format(final ReactionsImport reactionsImport, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<ReactionsImport>prepend(reactionsImport, _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(reactionsImport).keyword("import"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(reactionsImport).keyword("routines"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(reactionsImport).keyword("using"), _function_3);
    final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(reactionsImport).keyword("qualified"), _function_4);
    final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.prepend(this.regionFor(reactionsImport).keyword("names"), _function_5);
  }

  protected void _format(final Reaction reaction, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.setNewLines(2);
    };
    document.<Reaction>prepend(reaction, _function);
    String _documentation = reaction.getDocumentation();
    boolean _tripleNotEquals = (_documentation != null);
    if (_tripleNotEquals) {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
        it.newLine();
      };
      document.prepend(this.regionFor(reaction).keyword("reaction"), _function_1);
    }
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(reaction).keyword("reaction"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.surround(this.regionFor(reaction).keyword("::"), _function_3);
    this.formatInteriorBlock(reaction, document);
    Trigger _trigger = reaction.getTrigger();
    if (_trigger!=null) {
      this.format(_trigger, document);
    }
    RoutineCall _callRoutine = reaction.getCallRoutine();
    if (_callRoutine!=null) {
      this.format(_callRoutine, document);
    }
  }

  protected void _format(final ArbitraryModelChange arbitraryModelChange, @Extension final IFormattableDocument document) {
    this.formatTrigger(arbitraryModelChange, document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(arbitraryModelChange).keyword("anychange"), _function);
  }

  protected void _format(final ModelElementChange modelElementChange, @Extension final IFormattableDocument document) {
    this.formatTrigger(modelElementChange, document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelElementChange).keyword("element"), _function);
    MetaclassReference _elementType = modelElementChange.getElementType();
    if (_elementType!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<MetaclassReference>surround(_elementType, _function_1);
    }
    this.format(modelElementChange.getElementType(), document);
    ElementChangeType _changeType = modelElementChange.getChangeType();
    if (_changeType!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<ElementChangeType>surround(_changeType, _function_2);
    }
    ElementChangeType _changeType_1 = modelElementChange.getChangeType();
    if (_changeType_1!=null) {
      this.format(_changeType_1, document);
    }
  }

  protected void _format(final ElementChangeType changeType, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("created"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("deleted"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("inserted"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("in"), _function_3);
    final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("removed"), _function_4);
    final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("from"), _function_5);
    final Procedure1<IHiddenRegionFormatter> _function_6 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("replaced"), _function_6);
    final Procedure1<IHiddenRegionFormatter> _function_7 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(changeType).keyword("at"), _function_7);
    final Procedure1<IHiddenRegionFormatter> _function_8 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.prepend(this.regionFor(changeType).keyword("root"), _function_8);
    if ((changeType instanceof ElementReferenceChangeType)) {
      this.format(((ElementReferenceChangeType)changeType).getFeature(), document);
    }
  }

  protected void _format(final ModelAttributeChange modelAttributeChange, @Extension final IFormattableDocument document) {
    this.formatTrigger(modelAttributeChange, document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("attribute"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("inserted"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("in"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("removed"), _function_3);
    final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("from"), _function_4);
    final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("replaced"), _function_5);
    final Procedure1<IHiddenRegionFormatter> _function_6 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(modelAttributeChange).keyword("at"), _function_6);
    MetaclassEAttributeReference _feature = modelAttributeChange.getFeature();
    if (_feature!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_7 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<MetaclassEAttributeReference>surround(_feature, _function_7);
    }
    MetaclassEAttributeReference _feature_1 = modelAttributeChange.getFeature();
    if (_feature_1!=null) {
      this.format(_feature_1, document);
    }
  }

  private void formatTrigger(final Trigger trigger, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(trigger).keyword("after"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.newLine();
      it.indent();
    };
    document.prepend(this.regionFor(trigger).keyword("with"), _function_1);
    XExpression _precondition = trigger.getPrecondition();
    if (_precondition!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_precondition, _function_2);
    }
    XExpression _precondition_1 = trigger.getPrecondition();
    if (_precondition_1!=null) {
      this.format(_precondition_1, document);
    }
  }

  protected void _format(final RoutineCall routineCall, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<RoutineCall>prepend(routineCall, _function);
    XExpression _code = routineCall.getCode();
    if (_code!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_code, _function_1);
    }
    XExpression _code_1 = routineCall.getCode();
    if (_code_1!=null) {
      this.format(_code_1, document);
    }
  }

  protected void _format(final Routine routine, @Extension final IFormattableDocument document) {
    String _documentation = routine.getDocumentation();
    boolean _tripleNotEquals = (_documentation != null);
    if (_tripleNotEquals) {
      final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
        it.setNewLines(2);
      };
      document.<Routine>prepend(routine, _function);
    }
    ISemanticRegion _keyword = this.regionFor(routine).keyword("routine");
    final Procedure1<ISemanticRegion> _function_1 = (ISemanticRegion it) -> {
      String _documentation_1 = routine.getDocumentation();
      boolean _tripleEquals = (_documentation_1 == null);
      if (_tripleEquals) {
        final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
          it_1.setNewLines(2);
        };
        document.prepend(it, _function_2);
      } else {
        final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it_1) -> {
          it_1.newLine();
        };
        document.prepend(it, _function_3);
      }
      final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_4);
    };
    ObjectExtensions.<ISemanticRegion>operator_doubleArrow(_keyword, _function_1);
    RoutineOverrideImportPath _overrideImportPath = routine.getOverrideImportPath();
    if (_overrideImportPath!=null) {
      this.format(_overrideImportPath, document);
    }
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.surround(this.regionFor(routine).keyword("::"), _function_2);
    RoutineInput _input = routine.getInput();
    if (_input!=null) {
      this.format(_input, document);
    }
    this.formatInteriorBlock(routine, document);
    MatchBlock _matchBlock = routine.getMatchBlock();
    if (_matchBlock!=null) {
      this.format(_matchBlock, document);
    }
    CreateBlock _createBlock = routine.getCreateBlock();
    if (_createBlock!=null) {
      this.format(_createBlock, document);
    }
    UpdateBlock _updateBlock = routine.getUpdateBlock();
    if (_updateBlock!=null) {
      this.format(_updateBlock, document);
    }
  }

  protected void _format(final RoutineOverrideImportPath routineOverrideImportPath, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.surround(this.allRegionsFor(routineOverrideImportPath).keyword("."), _function);
  }

  protected void _format(final RoutineInput routineInput, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.surround(this.regionFor(routineInput).keyword("("), _function);
    final Consumer<NamedMetaclassReference> _function_1 = (NamedMetaclassReference it) -> {
      this.format(it, document);
    };
    routineInput.getModelInputElements().forEach(_function_1);
    final Consumer<ISemanticRegion> _function_2 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_3);
    };
    this.regionFor(routineInput).keywords("plain").forEach(_function_2);
    final Consumer<NamedJavaElementReference> _function_3 = (NamedJavaElementReference it) -> {
      this.format(it, document);
    };
    routineInput.getJavaInputElements().forEach(_function_3);
    final Consumer<ISemanticRegion> _function_4 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it_1) -> {
        it_1.noSpace();
      };
      document.prepend(it, _function_5);
      final Procedure1<IHiddenRegionFormatter> _function_6 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.append(it, _function_6);
    };
    this.regionFor(routineInput).keywords(",").forEach(_function_4);
    final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.prepend(this.regionFor(routineInput).keyword(")"), _function_5);
  }

  protected void _format(final MatchBlock match, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<MatchBlock>prepend(match, _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(match).keyword("match"), _function_1);
    this.formatInteriorBlock(match, document);
    final Consumer<MatchStatement> _function_2 = (MatchStatement it) -> {
      this.format(it, document);
    };
    match.getMatchStatements().forEach(_function_2);
  }

  protected void _format(final MatchCheckStatement matchCheckStatement, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<MatchCheckStatement>prepend(matchCheckStatement, _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(matchCheckStatement).keyword("check"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(matchCheckStatement).keyword("asserted"), _function_2);
    XExpression _condition = matchCheckStatement.getCondition();
    if (_condition!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_condition, _function_3);
    }
    XExpression _condition_1 = matchCheckStatement.getCondition();
    if (_condition_1!=null) {
      this.format(_condition_1, document);
    }
  }

  protected void _format(final RetrieveModelElement retrieveStatement, @Extension final IFormattableDocument document) {
    this.formatAssignment(retrieveStatement, document);
    this.format(retrieveStatement.getRetrievalType(), document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveStatement).keyword("retrieve"), _function);
    this.formatRetrieveOrRequireAbsence(retrieveStatement, document);
  }

  protected void _format(final RetrieveModelElementType retrieveElementType, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveElementType).keyword("asserted"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveElementType).keyword("optional"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveElementType).keyword("many"), _function_2);
  }

  protected void _format(final RequireAbscenceOfModelElement requireAbsenceStatement, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(requireAbsenceStatement).keyword("require"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(requireAbsenceStatement).keyword("absence"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(requireAbsenceStatement).keyword("of"), _function_2);
    this.formatRetrieveOrRequireAbsence(requireAbsenceStatement, document);
  }

  private void formatRetrieveOrRequireAbsence(final RetrieveOrRequireAbscenceOfModelElement retrieveOrRequireAbsenceStatment, @Extension final IFormattableDocument document) {
    final Consumer<Pair<ISemanticRegion, ISemanticRegion>> _function = (Pair<ISemanticRegion, ISemanticRegion> it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.surround(it.getKey(), _function_1);
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.surround(it.getValue(), _function_2);
    };
    this.regionFor(retrieveOrRequireAbsenceStatment).keywordPairs("corresponding", "to").forEach(_function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveOrRequireAbsenceStatment).keyword("tagged"), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(retrieveOrRequireAbsenceStatment).keyword("with"), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<RetrieveOrRequireAbscenceOfModelElement>prepend(retrieveOrRequireAbsenceStatment, _function_3);
    MetaclassReference _elementType = retrieveOrRequireAbsenceStatment.getElementType();
    if (_elementType!=null) {
      this.format(_elementType, document);
    }
    XExpression _correspondenceSource = retrieveOrRequireAbsenceStatment.getCorrespondenceSource();
    if (_correspondenceSource!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_4 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_correspondenceSource, _function_4);
    }
    XExpression _correspondenceSource_1 = retrieveOrRequireAbsenceStatment.getCorrespondenceSource();
    if (_correspondenceSource_1!=null) {
      this.format(_correspondenceSource_1, document);
    }
    XExpression _tag = retrieveOrRequireAbsenceStatment.getTag();
    if (_tag!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_5 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_tag, _function_5);
    }
    XExpression _tag_1 = retrieveOrRequireAbsenceStatment.getTag();
    if (_tag_1!=null) {
      this.format(_tag_1, document);
    }
    XExpression _precondition = retrieveOrRequireAbsenceStatment.getPrecondition();
    if (_precondition!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_6 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<XExpression>prepend(_precondition, _function_6);
    }
    XExpression _precondition_1 = retrieveOrRequireAbsenceStatment.getPrecondition();
    if (_precondition_1!=null) {
      this.format(_precondition_1, document);
    }
  }

  protected void _format(final CreateBlock create, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<CreateBlock>prepend(create, _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(create).keyword("create"), _function_1);
    this.formatInteriorBlock(create, document);
    final Consumer<NamedMetaclassReference> _function_2 = (NamedMetaclassReference it) -> {
      this.formatCreateStatement(it, document);
    };
    create.getCreateStatements().forEach(_function_2);
  }

  private void formatCreateStatement(final NamedMetaclassReference createStatement, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(createStatement).keyword("new"), _function);
    this.formatAssignment(createStatement, document);
    this.format(createStatement, document);
  }

  protected void _format(final UpdateBlock update, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<UpdateBlock>prepend(update, _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(update).keyword("update"), _function_1);
    XExpression _code = update.getCode();
    if (_code!=null) {
      this.format(_code, document);
    }
  }

  protected void _format(final MetaclassReference metaclassReference, @Extension final IFormattableDocument document) {
    this.formatMetaclassReference(metaclassReference, document);
  }

  private ISemanticRegion formatMetaclassReference(final MetaclassReference metaclassReference, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    return document.surround(this.regionFor(metaclassReference).keyword("::"), _function);
  }

  protected void _format(final NamedMetaclassReference namedMetaclassReference, @Extension final IFormattableDocument document) {
    this.formatMetaclassReference(namedMetaclassReference, document);
    final Consumer<ISemanticRegion> _function = (ISemanticRegion it) -> {
      final EObject grammarElement = it.getGrammarElement();
      if ((grammarElement instanceof RuleCall)) {
        AbstractRule _rule = ((RuleCall)grammarElement).getRule();
        ParserRule _validIDRule = this.getGrammar().getValidIDRule();
        boolean _equals = Objects.equal(_rule, _validIDRule);
        if (_equals) {
          final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it_1) -> {
            it_1.oneSpace();
          };
          document.prepend(it, _function_1);
        }
      }
    };
    this.semanticRegions(namedMetaclassReference).forEach(_function);
  }

  protected void _format(final NamedJavaElementReference namedJavaElementReference, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(namedJavaElementReference).keyword("as"), _function);
    JvmTypeReference _type = namedJavaElementReference.getType();
    if (_type!=null) {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
        it.oneSpace();
      };
      document.<JvmTypeReference>append(_type, _function_1);
    }
    JvmTypeReference _type_1 = namedJavaElementReference.getType();
    if (_type_1!=null) {
      this.format(_type_1, document);
    }
  }

  protected void _format(final MetaclassEAttributeReference attributeReference, @Extension final IFormattableDocument document) {
    this.formatMetaclassReference(attributeReference, document);
    this.format(attributeReference.getFeature(), document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.append(document.prepend(this.regionFor(attributeReference).keyword("["), _function), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.prepend(this.regionFor(attributeReference).keyword("]"), _function_2);
  }

  protected void _format(final MetaclassEReferenceReference referenceReference, @Extension final IFormattableDocument document) {
    this.formatMetaclassReference(referenceReference, document);
    this.format(referenceReference.getFeature(), document);
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.append(document.prepend(this.regionFor(referenceReference).keyword("["), _function), _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.noSpace();
    };
    document.prepend(this.regionFor(referenceReference).keyword("]"), _function_2);
  }

  private void formatAssignment(final EObject assigment, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.append(this.regionFor(assigment).keyword("val"), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.oneSpace();
    };
    document.surround(this.regionFor(assigment).keyword("="), _function_1);
  }

  private Pair<ISemanticRegion, ISemanticRegion> formatInteriorBlock(final EObject element, @Extension final IFormattableDocument document) {
    ISemanticRegion _keyword = this.regionFor(element).keyword("{");
    final Procedure1<ISemanticRegion> _function = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it_1) -> {
        it_1.oneSpace();
      };
      document.prepend(it, _function_1);
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
        it_1.newLine();
      };
      document.append(it, _function_2);
    };
    ISemanticRegion _doubleArrow = ObjectExtensions.<ISemanticRegion>operator_doubleArrow(_keyword, _function);
    ISemanticRegion _keyword_1 = this.regionFor(element).keyword("}");
    final Procedure1<ISemanticRegion> _function_1 = (ISemanticRegion it) -> {
      final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it_1) -> {
        it_1.newLine();
      };
      document.prepend(it, _function_2);
    };
    ISemanticRegion _doubleArrow_1 = ObjectExtensions.<ISemanticRegion>operator_doubleArrow(_keyword_1, _function_1);
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.indent();
    };
    return document.<ISemanticRegion, ISemanticRegion>interior(_doubleArrow, _doubleArrow_1, _function_2);
  }

  public void format(final Object modelAttributeChange, final IFormattableDocument document) {
    if (modelAttributeChange instanceof JvmTypeParameter) {
      _format((JvmTypeParameter)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof JvmFormalParameter) {
      _format((JvmFormalParameter)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XtextResource) {
      _format((XtextResource)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XAssignment) {
      _format((XAssignment)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XBinaryOperation) {
      _format((XBinaryOperation)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XDoWhileExpression) {
      _format((XDoWhileExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XFeatureCall) {
      _format((XFeatureCall)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XMemberFeatureCall) {
      _format((XMemberFeatureCall)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XPostfixOperation) {
      _format((XPostfixOperation)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XWhileExpression) {
      _format((XWhileExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XFunctionTypeRef) {
      _format((XFunctionTypeRef)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ModelAttributeChange) {
      _format((ModelAttributeChange)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ModelElementChange) {
      _format((ModelElementChange)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RequireAbscenceOfModelElement) {
      _format((RequireAbscenceOfModelElement)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RetrieveModelElement) {
      _format((RetrieveModelElement)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof JvmGenericArrayTypeReference) {
      _format((JvmGenericArrayTypeReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof JvmParameterizedTypeReference) {
      _format((JvmParameterizedTypeReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof JvmWildcardTypeReference) {
      _format((JvmWildcardTypeReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XBasicForLoopExpression) {
      _format((XBasicForLoopExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XBlockExpression) {
      _format((XBlockExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XCastedExpression) {
      _format((XCastedExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XClosure) {
      _format((XClosure)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XCollectionLiteral) {
      _format((XCollectionLiteral)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XConstructorCall) {
      _format((XConstructorCall)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XForLoopExpression) {
      _format((XForLoopExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XIfExpression) {
      _format((XIfExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XInstanceOfExpression) {
      _format((XInstanceOfExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XReturnExpression) {
      _format((XReturnExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XSwitchExpression) {
      _format((XSwitchExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XSynchronizedExpression) {
      _format((XSynchronizedExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XThrowExpression) {
      _format((XThrowExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XTryCatchFinallyExpression) {
      _format((XTryCatchFinallyExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XTypeLiteral) {
      _format((XTypeLiteral)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XVariableDeclaration) {
      _format((XVariableDeclaration)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MetaclassEAttributeReference) {
      _format((MetaclassEAttributeReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MetaclassEReferenceReference) {
      _format((MetaclassEReferenceReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof NamedMetaclassReference) {
      _format((NamedMetaclassReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ArbitraryModelChange) {
      _format((ArbitraryModelChange)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MatchCheckStatement) {
      _format((MatchCheckStatement)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RoutineCall) {
      _format((RoutineCall)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof UpdateBlock) {
      _format((UpdateBlock)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof JvmTypeConstraint) {
      _format((JvmTypeConstraint)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XExpression) {
      _format((XExpression)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XImportDeclaration) {
      _format((XImportDeclaration)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof XImportSection) {
      _format((XImportSection)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MetaclassReference) {
      _format((MetaclassReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MetamodelImport) {
      _format((MetamodelImport)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ElementChangeType) {
      _format((ElementChangeType)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RetrieveModelElementType) {
      _format((RetrieveModelElementType)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof CreateBlock) {
      _format((CreateBlock)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof MatchBlock) {
      _format((MatchBlock)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof NamedJavaElementReference) {
      _format((NamedJavaElementReference)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof Reaction) {
      _format((Reaction)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ReactionsFile) {
      _format((ReactionsFile)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ReactionsImport) {
      _format((ReactionsImport)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof ReactionsSegment) {
      _format((ReactionsSegment)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof Routine) {
      _format((Routine)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RoutineInput) {
      _format((RoutineInput)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof RoutineOverrideImportPath) {
      _format((RoutineOverrideImportPath)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange instanceof EObject) {
      _format((EObject)modelAttributeChange, document);
      return;
    } else if (modelAttributeChange == null) {
      _format((Void)null, document);
      return;
    } else if (modelAttributeChange != null) {
      _format(modelAttributeChange, document);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(modelAttributeChange, document).toString());
    }
  }
}
