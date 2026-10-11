/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.incremental.analysis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Computes the {@link ReadSet} of a type-checked constraint (incremental level 1).
 *
 * <p>The analysis is purely static: it walks the parse tree once and uses the static types the
 * type checker stored in {@code nodeTypes}. It is <em>conservative</em> by construction — whenever
 * a receiver type cannot be determined, the feature name is recorded as read on every class, and
 * whenever an {@code allInstances()} receiver cannot be determined, every structural event counts.
 *
 * <p>Model accesses the evaluator performs and this analysis covers [verified in {@code
 * EvaluationVisitor}]: property navigation (feature resolved by name on the runtime class), context
 * extension ({@code visitClassifierContextCS}), {@code allInstances()} (two call sites), {@code
 * @message} template interpolation of {@code {self.attr}}, correspondence look-ups, {@code @pre}
 * and lifecycle predicates (transaction look-ups).
 */
public final class ReadSetAnalyzer {

  private static final Pattern MESSAGE_SELF_ATTRIBUTE =
      Pattern.compile("\\{self\\.([a-zA-Z_]\\w*)\\}");

  private final ParseTreeProperty<Type> nodeTypes;
  private final Map<String, Set<EClass>> typed = new LinkedHashMap<>();
  private final Set<String> untyped = new LinkedHashSet<>();
  private final Set<EClass> extensionTypes = new LinkedHashSet<>();
  private final Deque<EClass> contextStack = new ArrayDeque<>();
  private boolean unknownExtension;
  private boolean usesCorrespondence;
  private boolean usesPre;
  private boolean usesLifecycle;
  private boolean isPrePost;
  private boolean hasErrorNodes;
  private boolean readsUnrecordedFeature;
  private final Set<String> messageTemplateReads = new LinkedHashSet<>();

  /**
   * Static types are not trusted for any read if the constraint uses {@code append} (the type
   * checker types its result as the receiver type without checking the argument's conformance,
   * {@code TypeCheckVisitor#visitAppendOp}) or {@code iterate} (accumulator values come from the
   * body). Every read then counts for every class.
   */
  private boolean untrustedTypes;

  private ReadSetAnalyzer(ParseTreeProperty<Type> nodeTypes) {
    this.nodeTypes = nodeTypes;
  }

  /** Analyses a successfully type-checked constraint tree. */
  public static ReadSet analyze(
      VitruvOCLParser.ContextDeclCSContext tree,
      ParseTreeProperty<Type> nodeTypes,
      FeatureIndex features) {
    ReadSetAnalyzer analyzer = new ReadSetAnalyzer(nodeTypes);
    analyzer.untrustedTypes = containsUntrustedTypeProducer(tree);
    analyzer.walk(tree);
    analyzer.checkUnrecordedFeatures(features);
    return new ReadSet(
        analyzer.typed,
        analyzer.untyped,
        analyzer.extensionTypes,
        analyzer.unknownExtension,
        analyzer.usesCorrespondence,
        analyzer.usesPre,
        analyzer.usesLifecycle,
        analyzer.isPrePost,
        analyzer.hasErrorNodes,
        analyzer.readsUnrecordedFeature);
  }

  private static boolean containsUntrustedTypeProducer(ParseTree node) {
    if (node instanceof VitruvOCLParser.AppendOpContext
        || node instanceof VitruvOCLParser.IterateOpContext) {
      return true;
    }
    for (int i = 0; i < node.getChildCount(); i++) {
      if (containsUntrustedTypeProducer(node.getChild(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Vitruvius records no value changes of transient, derived, volatile, unchangeable or container
   * features ({@link FeatureIndex#isUnrecorded}). A container feature changes exactly when its
   * owner is moved, which is a structural event of the owner: such reads add the owner classes as
   * extension types. Any other read of an unrecorded feature (and a reference in a message
   * template, whose text is the referenced object's {@code toString}) forces full evaluation
   * after every transaction.
   */
  private void checkUnrecordedFeatures(FeatureIndex features) {
    for (String name : untyped) {
      checkResolutions(features.resolve(name, null));
    }
    for (Map.Entry<String, Set<EClass>> read : typed.entrySet()) {
      for (EClass staticType : read.getValue()) {
        checkResolutions(features.resolve(read.getKey(), staticType));
      }
    }
    for (String name : messageTemplateReads) {
      for (EClass context : List.copyOf(extensionTypes)) {
        for (FeatureIndex.Resolution r : features.resolve(name, context)) {
          if (r.feature() instanceof EReference) {
            readsUnrecordedFeature = true;
          }
        }
      }
    }
  }

  private void checkResolutions(List<FeatureIndex.Resolution> resolutions) {
    for (FeatureIndex.Resolution r : resolutions) {
      if (FeatureIndex.isContainer(r.feature())) {
        extensionTypes.add(r.owner());
      } else if (FeatureIndex.isUnrecorded(r.feature())) {
        readsUnrecordedFeature = true;
      }
    }
  }

  private void walk(ParseTree node) {
    if (node instanceof VitruvOCLParser.ClassifierContextCSContext ctx) {
      visitContext(ctx);
      return;
    }
    inspect(node);
    for (int i = 0; i < node.getChildCount(); i++) {
      walk(node.getChild(i));
    }
  }

  private void visitContext(VitruvOCLParser.ClassifierContextCSContext ctx) {
    EClass contextClass = elementClass(nodeTypes.get(ctx));
    if (contextClass == null) {
      unknownExtension = true;
    } else {
      extensionTypes.add(contextClass);
    }
    if (!ctx.preCS().isEmpty() || !ctx.postCS().isEmpty()) {
      isPrePost = true;
    }
    contextStack.push(contextClass);
    try {
      for (int i = 0; i < ctx.getChildCount(); i++) {
        walk(ctx.getChild(i));
      }
    } finally {
      contextStack.pop();
    }
  }

  /** Records what a single node reads; children are visited by {@link #walk}. */
  private void inspect(ParseTree node) {
    if (node instanceof VitruvOCLParser.PrimaryWithNavContext ctx) {
      Type baseType = spineType(ctx.base, 3);
      if (baseType == null && ctx.base instanceof VitruvOCLParser.SelfContext) {
        EClass self = contextStack.peek();
        baseType = self == null ? null : Type.metaclassType(self);
      }
      walkChain(baseType, ctx.navigationChainCS(), nodeTypes.get(ctx));
    } else if (node instanceof VitruvOCLParser.PrefixedQualifiedContext ctx) {
      walkChain(nodeTypes.get(ctx), ctx.navigationChainCS(), null);
    } else if (node instanceof VitruvOCLParser.MessageAnnotationContext ctx) {
      recordMessageTemplateReads(ctx);
    } else if (isCorrespondenceNode(node)) {
      usesCorrespondence = true;
    } else if (isErrorNode(node)) {
      hasErrorNodes = true;
    }
  }

  /**
   * Walks one navigation chain, tracking the static receiver type of each step.
   *
   * @param chainType the type of the whole navigation expression, or {@code null}; needed because
   *     the evaluator's {@code visitAllInstancesOp} falls back to it when the navigation step has
   *     no stored type
   */
  private void walkChain(
      Type receiver, List<VitruvOCLParser.NavigationChainCSContext> chain, Type chainType) {
    Type current = receiver;
    for (VitruvOCLParser.NavigationChainCSContext nav : chain) {
      if (nav.AT_PRE() != null) {
        usesPre = true;
        continue; // '@pre' does not change the type of the step it follows
      }
      VitruvOCLParser.NavigationTargetCSContext target = nav.navigationTargetCS();
      if (target instanceof VitruvOCLParser.PropertyNavContext prop) {
        recordFeatureRead(prop.propertyAccess().propertyName.getText(), elementClass(current));
      } else if (target instanceof VitruvOCLParser.LifecycleNavContext) {
        usesLifecycle = true;
      } else if (target instanceof VitruvOCLParser.OperationNavContext op
          && isAllInstances(op.operationCall())) {
        EClass type = elementClass(current);
        if (type == null) {
          unknownExtension = true;
        } else {
          extensionTypes.add(type);
        }
        if (nodeTypes.get(nav) == null && chainType != null) {
          EClass evaluatorType = elementClass(chainType);
          if (evaluatorType != null) {
            extensionTypes.add(evaluatorType);
          }
        }
      }
      Type next = spineType(nav, 4);
      current = next;
    }
  }

  private static boolean isAllInstances(VitruvOCLParser.OperationCallContext call) {
    if (call instanceof VitruvOCLParser.CollectionOperationContext coll) {
      return coll.collectionOpCS() instanceof VitruvOCLParser.AllInstancesOpContext;
    }
    if (call instanceof VitruvOCLParser.NoArgOpWithArgsContext noArg) {
      return "allInstances".equals(noArg.op.getText());
    }
    if (call instanceof VitruvOCLParser.OpMissingParensContext missing) {
      return "allInstances".equals(missing.op.getText());
    }
    return false;
  }

  private void recordFeatureRead(String name, EClass receiverClass) {
    if (receiverClass == null || untrustedTypes) {
      untyped.add(name);
    } else {
      typed.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(receiverClass);
    }
  }

  private void recordMessageTemplateReads(VitruvOCLParser.MessageAnnotationContext ctx) {
    Matcher m = MESSAGE_SELF_ATTRIBUTE.matcher(ctx.message.getText());
    while (m.find()) {
      recordFeatureRead(m.group(1), contextStack.peek());
      messageTemplateReads.add(m.group(1));
    }
  }

  private static boolean isCorrespondenceNode(ParseTree node) {
    return node instanceof VitruvOCLParser.CorrespondenceContext
        || node instanceof VitruvOCLParser.SelectCorrespondenceContext
        || node instanceof VitruvOCLParser.RejectCorrespondenceContext
        || node instanceof VitruvOCLParser.ExistsCorrespondenceContext
        || node instanceof VitruvOCLParser.UnknownCorrOpContext
        || node instanceof VitruvOCLParser.CorrespondenceFilterCSContext;
  }

  private static boolean isErrorNode(ParseTree node) {
    return node instanceof VitruvOCLParser.InvalidBinaryOpContext
        || node instanceof VitruvOCLParser.MultiEqualsOpContext
        || node instanceof VitruvOCLParser.SingleEqualsOpContext
        || node instanceof VitruvOCLParser.MessageContext
        || node instanceof VitruvOCLParser.UnknownBinaryOpContext
        || node instanceof VitruvOCLParser.UnknownOperationContext
        || node instanceof VitruvOCLParser.NoArgOpWithArgsContext
        || node instanceof VitruvOCLParser.OpMissingParensContext
        || node instanceof VitruvOCLParser.IteratorMissingBodyContext
        || node instanceof VitruvOCLParser.UnknownFilterOptionContext
        || node instanceof VitruvOCLParser.InvalidFilterArgContext;
  }

  /**
   * The static type stored for {@code node}, or — if none is stored there — for the first typed
   * node reachable by descending only through "spine" nodes that have the same value as their
   * parent (navigation target → operation call → concrete operation; primary expression → self /
   * variable / literal / nested / if / let / collection or type literal). Never descends into
   * argument or body expressions, whose types differ from the node's own.
   */
  private Type spineType(ParserRuleContext node, int depth) {
    Type type = nodeTypes.get(node);
    if (type != null || depth == 0) {
      return type;
    }
    for (int i = 0; i < node.getChildCount(); i++) {
      if (node.getChild(i) instanceof ParserRuleContext child && isSpine(child)) {
        Type childType = spineType(child, depth - 1);
        if (childType != null) {
          return childType;
        }
      }
    }
    return null;
  }

  private static boolean isSpine(ParserRuleContext node) {
    return node instanceof VitruvOCLParser.NavigationTargetCSContext
        || node instanceof VitruvOCLParser.OperationCallContext
        || node instanceof VitruvOCLParser.CollectionOpCSContext
        || node instanceof VitruvOCLParser.IteratorOpCSContext
        || node instanceof VitruvOCLParser.StringOpCSContext
        || node instanceof VitruvOCLParser.TypeOpCSContext
        || node instanceof VitruvOCLParser.UnknownOpCSContext
        || node instanceof VitruvOCLParser.SelfExpCSContext
        || node instanceof VitruvOCLParser.VariableExpCSContext
        || node instanceof VitruvOCLParser.LiteralExpCSContext
        || node instanceof VitruvOCLParser.NestedExpCSContext
        || node instanceof VitruvOCLParser.IfExpCSContext
        || node instanceof VitruvOCLParser.LetExpCSContext
        || node instanceof VitruvOCLParser.CollectionLiteralExpCSContext
        || node instanceof VitruvOCLParser.TypeLiteralExpCSContext;
  }

  /** The metaclass a (possibly collection/singleton/optional) type ranges over, or {@code null}. */
  static EClass elementClass(Type type) {
    Type current = type;
    for (int guard = 0; current != null && guard < 8; guard++) {
      if (current.isMetaclassType()) {
        return current.getEClass();
      }
      if (current.isCollection() || current.isSingleton() || current.isOptional()) {
        current = current.getElementType();
      } else {
        return null;
      }
    }
    return null;
  }
}
