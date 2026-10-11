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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Computes the {@link PseSet} of a type-checked constraint (incremental level 2) by annotating the
 * parse tree top-down with {@link Polarity} values, following Cabot &amp; Teniente 2009, §3.1,
 * Tab. 3.1–3.3, and drawing the events per Tab. 3.4.
 *
 * <p>Adaptations to VitruvOCL's semantics (not in the paper), all conservative:
 *
 * <ul>
 *   <li><b>Undefined values.</b> VitruvOCL has no {@code invalid}: an empty operand makes a
 *       comparison {@code true}, a non-singleton operand makes it {@code false} (with a run-time
 *       error), {@code and/or/xor} yield {@code false} and {@code implies} {@code true} for empty
 *       operands, and {@code not} of an empty value is empty ({@code EvaluationVisitor}). The
 *       direction rules are therefore applied to an operand of a comparison, of a Boolean
 *       operator, of {@code not}, to an iterator body and to the constraint root only if that
 *       operand is <em>guaranteed</em> to yield exactly one value (size, count, includes,
 *       isEmpty, forAll, comparisons, Boolean operators, literals, ...); otherwise it is annotated
 *       {@link Polarity#BOTH}.
 *   <li><b>No {@code und}.</b> Wherever the paper propagates {@code und} or has no table row, the
 *       child is annotated {@link Polarity#BOTH}; navigations reached with {@code und} would
 *       otherwise produce no PSE at all.
 *   <li><b>Attribute access on a collection</b> (implicit collect) propagates its own annotation
 *       joined with {@code +} to the receiver instead of only {@code +} (Tab. 3.1 assumes a single
 *       receiver object).
 *   <li><b>{@code not} and {@code select}</b> (paper: opposite events of the subtree) are handled by
 *       flipping the annotation for {@code not} and by annotating the {@code select}/{@code reject}
 *       body {@link Polarity#BOTH}.
 * </ul>
 */
public final class PolarityAnalyzer {

  private final ParseTreeProperty<Type> nodeTypes;
  private final boolean untrustedTypes;
  private final Map<String, Map<EClass, Polarity>> typed = new LinkedHashMap<>();
  private final Map<String, Polarity> untyped = new LinkedHashMap<>();
  private final Set<EClass> insertSensitive = new LinkedHashSet<>();
  private final Set<EClass> deleteSensitive = new LinkedHashSet<>();
  private boolean unknownExtension;
  private EClass currentContext;

  private PolarityAnalyzer(ParseTreeProperty<Type> nodeTypes, boolean untrustedTypes) {
    this.nodeTypes = nodeTypes;
    this.untrustedTypes = untrustedTypes;
  }

  /**
   * Analyses {@code tree}. The level-1 {@code readSet} contributes the extension types the
   * polarity walk cannot direct (owners of container features, the evaluator's {@code
   * allInstances} fallback type): they are made sensitive to insertion and deletion.
   */
  public static PseSet analyze(
      VitruvOCLParser.ContextDeclCSContext tree, ParseTreeProperty<Type> nodeTypes, ReadSet readSet) {
    PolarityAnalyzer analyzer =
        new PolarityAnalyzer(nodeTypes, ReadSetAnalyzer.containsUntrustedTypeProducer(tree));
    for (VitruvOCLParser.ClassifierContextCSContext ctx : tree.classifierContextCS()) {
      analyzer.visitContext(ctx);
    }
    // Safety net: every read of the level-1 read set (e.g. @message template attributes) that the
    // polarity walk did not record with a direction is a PSE in both directions.
    readSet
        .typedFeatureReads()
        .forEach(
            (name, classes) -> {
              for (EClass c : classes) {
                analyzer.typed.computeIfAbsent(name, k -> new LinkedHashMap<>()).putIfAbsent(c, Polarity.BOTH);
              }
            });
    for (String name : readSet.untypedFeatureReads()) {
      analyzer.untyped.putIfAbsent(name, Polarity.BOTH);
    }
    Set<EClass> directed = new LinkedHashSet<>(analyzer.insertSensitive);
    directed.addAll(analyzer.deleteSensitive);
    for (EClass type : readSet.extensionTypes()) {
      if (!directed.contains(type)) {
        analyzer.insertSensitive.add(type);
        analyzer.deleteSensitive.add(type);
      }
    }
    return new PseSet(
        analyzer.typed,
        analyzer.untyped,
        analyzer.insertSensitive,
        analyzer.deleteSensitive,
        analyzer.unknownExtension || readSet.unknownExtension());
  }

  private void visitContext(VitruvOCLParser.ClassifierContextCSContext ctx) {
    currentContext = ReadSetAnalyzer.elementClass(nodeTypes.get(ctx));
    if (currentContext == null) {
      unknownExtension = true;
    } else {
      // Tab. 3.4, "Access self variable": InsertET of the context type in rows + and −
      insertSensitive.add(currentContext);
    }
    for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
      visitBlock(inv.specificationCS(), inv.annotationCS());
    }
    for (VitruvOCLParser.PreCSContext pre : ctx.preCS()) {
      visitBlock(pre.specificationCS(), pre.annotationCS());
    }
    for (VitruvOCLParser.PostCSContext post : ctx.postCS()) {
      visitBlock(post.specificationCS(), post.annotationCS());
    }
  }

  /** Only the last expression of the last specification decides the result (see report §10). */
  private void visitBlock(
      List<VitruvOCLParser.SpecificationCSContext> specs,
      List<VitruvOCLParser.AnnotationCSContext> annotations) {
    for (VitruvOCLParser.AnnotationCSContext annotation : annotations) {
      annotateChildren(annotation, Polarity.BOTH);
    }
    for (int s = 0; s < specs.size(); s++) {
      List<VitruvOCLParser.ExpCSContext> exps = specs.get(s).expCS();
      for (int e = 0; e < exps.size(); e++) {
        boolean decisive = s == specs.size() - 1 && e == exps.size() - 1;
        annotate(exps.get(e), decisive ? guarded(exps.get(e), Polarity.MINUS) : Polarity.BOTH);
      }
    }
  }

  // ------------------------------------------------------------------ expressions

  private void annotate(ParseTree node, Polarity p) {
    if (node instanceof VitruvOCLParser.ExpCSContext e) {
      annotate(e.infixedExpCS(), p);
    } else if (node instanceof VitruvOCLParser.PrefixedExprContext e) {
      annotate(e.prefixedExpCS(), p);
    } else if (node instanceof VitruvOCLParser.LessThanComparisonContext
        || node instanceof VitruvOCLParser.LessThanOrEqualComparisonContext) {
      // Tab. 3.2, column <, ≤: left +, right − for a constraint that must stay true
      ParserRuleContext ctx = (ParserRuleContext) node;
      binary(ctx, p.flip(), p);
    } else if (node instanceof VitruvOCLParser.GreaterThanComparisonContext
        || node instanceof VitruvOCLParser.GreaterThanOrEqualComparisonContext) {
      binary((ParserRuleContext) node, p, p.flip());
    } else if (node instanceof VitruvOCLParser.LogicalAndContext
        || node instanceof VitruvOCLParser.LogicalOrContext) {
      binary((ParserRuleContext) node, p, p); // Tab. 3.2: and/or (und in the paper)
    } else if (node instanceof VitruvOCLParser.ImplicationContext impl) {
      annotate(impl.left, guarded(impl.left, p.flip())); // a implies b ≡ not a or b
      annotate(impl.right, guarded(impl.right, p));
    } else if (node instanceof VitruvOCLParser.LogicalNotContext not) {
      annotate(not.operand, guarded(not.operand, p.flip()));
    } else if (node instanceof VitruvOCLParser.PrimaryWithNavContext nav) {
      chain(nav.base, nav.navigationChainCS(), p, nodeTypes.get(nav));
    } else if (node instanceof VitruvOCLParser.PrefixedQualifiedContext q) {
      chain(null, q.navigationChainCS(), p, null);
    } else if (node instanceof VitruvOCLParser.NestedContext nested) {
      List<VitruvOCLParser.ExpCSContext> exps = nested.nestedExpCS().expCS();
      for (int i = 0; i < exps.size(); i++) {
        annotate(exps.get(i), i == exps.size() - 1 ? p : Polarity.BOTH);
      }
    } else if (node instanceof VitruvOCLParser.LetBindingContext let) {
      VitruvOCLParser.LetExpCSContext l = let.letExpCS();
      annotateChildren(l.variableDeclarations(), Polarity.BOTH);
      List<VitruvOCLParser.ExpCSContext> body = l.expCS();
      for (int i = 0; i < body.size(); i++) {
        annotate(body.get(i), i == body.size() - 1 ? p : Polarity.BOTH);
      }
    } else if (node instanceof VitruvOCLParser.SelfContext
        || node instanceof VitruvOCLParser.VariableContext
        || node instanceof VitruvOCLParser.LiteralContext
        || node instanceof VitruvOCLParser.TypeLiteralContext) {
      return; // leaves: no events (the self extension is handled per context)
    } else {
      // equality, arithmetic, xor, ~, if, collection literals, error alternatives, ...
      annotateChildren(node, Polarity.BOTH);
    }
  }

  private void binary(ParserRuleContext ctx, Polarity left, Polarity right) {
    ParserRuleContext l = (ParserRuleContext) ctx.getChild(0);
    ParserRuleContext r = (ParserRuleContext) ctx.getChild(ctx.getChildCount() - 1);
    annotate(l, guarded(l, left));
    annotate(r, guarded(r, right));
  }

  private void annotateChildren(ParseTree node, Polarity p) {
    for (int i = 0; i < node.getChildCount(); i++) {
      annotate(node.getChild(i), p);
    }
  }

  /** {@code p} if {@code operand} always yields exactly one value, else {@link Polarity#BOTH}. */
  private static Polarity guarded(ParseTree operand, Polarity p) {
    return isGuaranteedSingleton(operand) ? p : Polarity.BOTH;
  }

  /**
   * Whether evaluating {@code node} always yields exactly one value, whatever the model state.
   * Verified in {@code EvaluationVisitor}: comparisons and {@code ~} (via {@code
   * evaluateBinaryComparison}), {@code and/or/xor} ({@code evaluateBinaryLogical}), {@code
   * implies}, {@code size}, {@code count}, {@code includes/excludes(All)}, {@code
   * isEmpty/notEmpty}, {@code forAll}, {@code exists}, {@code one}, {@code isUnique} always return
   * a singleton; {@code not} only if its operand does; literals.
   */
  static boolean isGuaranteedSingleton(ParseTree node) {
    if (node instanceof VitruvOCLParser.ExpCSContext e) {
      return isGuaranteedSingleton(e.infixedExpCS());
    }
    if (node instanceof VitruvOCLParser.PrefixedExprContext e) {
      return isGuaranteedSingleton(e.prefixedExpCS());
    }
    if (node instanceof VitruvOCLParser.LessThanComparisonContext
        || node instanceof VitruvOCLParser.LessThanOrEqualComparisonContext
        || node instanceof VitruvOCLParser.GreaterThanComparisonContext
        || node instanceof VitruvOCLParser.GreaterThanOrEqualComparisonContext
        || node instanceof VitruvOCLParser.EqualityComparisonContext
        || node instanceof VitruvOCLParser.InequalityComparisonContext
        || node instanceof VitruvOCLParser.LogicalAndContext
        || node instanceof VitruvOCLParser.LogicalOrContext
        || node instanceof VitruvOCLParser.LogicalXorContext
        || node instanceof VitruvOCLParser.ImplicationContext
        || node instanceof VitruvOCLParser.CorrespondenceContext) {
      return true;
    }
    if (node instanceof VitruvOCLParser.LogicalNotContext not) {
      return isGuaranteedSingleton(not.operand);
    }
    if (node instanceof VitruvOCLParser.PrimaryWithNavContext nav) {
      List<VitruvOCLParser.NavigationChainCSContext> chain = nav.navigationChainCS();
      if (!chain.isEmpty()) {
        return isGuaranteedSingletonStep(chain.get(chain.size() - 1));
      }
      if (nav.base instanceof VitruvOCLParser.NestedContext nested) {
        List<VitruvOCLParser.ExpCSContext> exps = nested.nestedExpCS().expCS();
        return exps.size() == 1 && isGuaranteedSingleton(exps.get(0));
      }
      return nav.base instanceof VitruvOCLParser.LiteralContext;
    }
    if (node instanceof VitruvOCLParser.PrefixedQualifiedContext q) {
      List<VitruvOCLParser.NavigationChainCSContext> chain = q.navigationChainCS();
      return !chain.isEmpty() && isGuaranteedSingletonStep(chain.get(chain.size() - 1));
    }
    return false;
  }

  private static boolean isGuaranteedSingletonStep(VitruvOCLParser.NavigationChainCSContext nav) {
    if (!(nav.navigationTargetCS() instanceof VitruvOCLParser.OperationNavContext op)) {
      return false;
    }
    ParserRuleContext concrete = concreteOperation(op.operationCall());
    return concrete instanceof VitruvOCLParser.SizeOpContext
        || concrete instanceof VitruvOCLParser.CountOpContext
        || concrete instanceof VitruvOCLParser.IncludesOpContext
        || concrete instanceof VitruvOCLParser.ExcludesOpContext
        || concrete instanceof VitruvOCLParser.IncludesAllOpContext
        || concrete instanceof VitruvOCLParser.ExcludesAllOpContext
        || concrete instanceof VitruvOCLParser.IsEmptyOpContext
        || concrete instanceof VitruvOCLParser.NotEmptyOpContext
        || concrete instanceof VitruvOCLParser.ForAllOpContext
        || concrete instanceof VitruvOCLParser.ExistsOpContext
        || concrete instanceof VitruvOCLParser.OneOpContext
        || concrete instanceof VitruvOCLParser.IsUniqueOpContext;
  }

  private static ParserRuleContext concreteOperation(VitruvOCLParser.OperationCallContext call) {
    if (call instanceof VitruvOCLParser.CollectionOperationContext c) {
      return c.collectionOpCS();
    }
    if (call instanceof VitruvOCLParser.IteratorOperationContext i) {
      return i.iteratorOpCS();
    }
    if (call instanceof VitruvOCLParser.StringOperationContext s) {
      return s.stringOpCS();
    }
    if (call instanceof VitruvOCLParser.TypeOperationContext t) {
      return t.typeOpCS();
    }
    return call;
  }

  // ------------------------------------------------------------------ navigation chains

  /**
   * Annotates a navigation chain whose value has annotation {@code p}: walks the steps from the
   * last to the first, each step turning its own annotation into its receiver's (Tab. 3.1, 3.3),
   * and finally annotates the base expression.
   */
  private void chain(
      ParseTree base,
      List<VitruvOCLParser.NavigationChainCSContext> steps,
      Polarity p,
      Type chainType) {
    List<Type> receiverTypes = new ArrayList<>(steps.size());
    Type current = base instanceof ParserRuleContext b ? ReadSetAnalyzer.spineType(nodeTypes, b, 3) : null;
    if (current == null && base instanceof VitruvOCLParser.SelfContext && currentContext != null) {
      current = Type.metaclassType(currentContext);
    }
    if (base == null && !steps.isEmpty()) {
      current = nodeTypes.get(steps.get(0).getParent()); // prefixedQualified: the metaclass
    }
    for (VitruvOCLParser.NavigationChainCSContext nav : steps) {
      receiverTypes.add(current);
      if (nav.AT_PRE() == null) {
        current = ReadSetAnalyzer.spineType(nodeTypes, nav, 4);
      }
    }

    Polarity annotation = p;
    for (int i = steps.size() - 1; i >= 0; i--) {
      VitruvOCLParser.NavigationChainCSContext nav = steps.get(i);
      if (nav.AT_PRE() != null) {
        continue;
      }
      annotation = step(nav, receiverTypes.get(i), annotation, chainType);
    }
    if (base != null) {
      annotate(base, annotation);
    }
  }

  /** Records the events of one step and returns the annotation of its receiver. */
  private Polarity step(
      VitruvOCLParser.NavigationChainCSContext nav, Type receiverType, Polarity p, Type chainType) {
    VitruvOCLParser.NavigationTargetCSContext target = nav.navigationTargetCS();
    EClass receiverClass = ReadSetAnalyzer.elementClass(receiverType);
    if (target instanceof VitruvOCLParser.PropertyNavContext prop) {
      String name = prop.propertyAccess().propertyName.getText();
      recordRead(name, receiverClass, p);
      EStructuralFeature feature = receiverClass == null ? null : receiverClass.getEStructuralFeature(name);
      if (feature instanceof EAttribute) {
        return p.join(Polarity.PLUS); // Tab. 3.1: attribute access propagates + (adapted)
      }
      return feature instanceof EReference ? p : Polarity.BOTH; // Tab. 3.1: navigation keeps p
    }
    if (!(target instanceof VitruvOCLParser.OperationNavContext op)) {
      annotateChildren(target, Polarity.BOTH); // lifecycle predicates (always evaluated anyway)
      return Polarity.BOTH;
    }
    VitruvOCLParser.OperationCallContext call = op.operationCall();
    if (ReadSetAnalyzer.isAllInstances(call)) {
      recordExtension(receiverClass, p); // Tab. 3.4, column allInstances
      if (nodeTypes.get(nav) == null && chainType != null) {
        recordExtension(ReadSetAnalyzer.elementClass(chainType), Polarity.BOTH);
      }
      annotateChildren(call, Polarity.BOTH);
      return Polarity.BOTH;
    }
    ParserRuleContext concrete = concreteOperation(call);
    OperationRule rule = OperationRule.of(concrete);
    for (int i = 0; i < concrete.getChildCount(); i++) {
      ParseTree child = concrete.getChild(i);
      if (child instanceof VitruvOCLParser.ExpCSContext arg) {
        Polarity argPolarity = rule.argument(p);
        annotate(arg, rule.guardedArgument() ? guarded(arg, argPolarity) : argPolarity);
      } else if (child instanceof ParserRuleContext other) {
        annotateChildren(other, Polarity.BOTH); // iterate accumulator, correspondence filters
      }
    }
    return rule.receiver(p);
  }

  private void recordRead(String name, EClass receiverClass, Polarity p) {
    if (receiverClass == null || untrustedTypes) {
      untyped.merge(name, p, Polarity::join);
    } else {
      typed.computeIfAbsent(name, k -> new LinkedHashMap<>()).merge(receiverClass, p, Polarity::join);
    }
  }

  private void recordExtension(EClass type, Polarity p) {
    if (type == null) {
      unknownExtension = true;
      return;
    }
    if (p.increaseMayViolate()) {
      insertSensitive.add(type);
    }
    if (p.decreaseMayViolate()) {
      deleteSensitive.add(type);
    }
  }

  /**
   * Propagation rule of a collection/iterator operation: how its own annotation becomes the
   * annotation of its receiver and of its argument/body expressions (Tab. 3.3, adapted).
   */
  private enum OperationRule {
    /** Receiver keeps the annotation (size, count, select, collect, asX, union, ...); args ±. */
    SAME,
    /** Receiver gets the opposite annotation (isEmpty, excludes, excludesAll); args ±. */
    FLIP,
    /** {@code forAll}: receiver opposite (more elements may violate), body keeps the annotation. */
    FOR_ALL,
    /** {@code exists}: receiver and body keep the annotation (≡ not forAll(not …)). */
    EXISTS,
    /** {@code union}/{@code intersection}: receiver and argument keep the annotation. */
    SAME_ALL,
    /** Everything else: receiver and arguments ±. */
    BOTH;

    static OperationRule of(ParserRuleContext op) {
      if (op instanceof VitruvOCLParser.ForAllOpContext) {
        return FOR_ALL;
      }
      if (op instanceof VitruvOCLParser.ExistsOpContext) {
        return EXISTS;
      }
      if (op instanceof VitruvOCLParser.UnionOpContext
          || op instanceof VitruvOCLParser.IntersectionOpContext) {
        return SAME_ALL;
      }
      if (op instanceof VitruvOCLParser.IsEmptyOpContext
          || op instanceof VitruvOCLParser.ExcludesOpContext
          || op instanceof VitruvOCLParser.ExcludesAllOpContext) {
        return FLIP;
      }
      if (op instanceof VitruvOCLParser.SizeOpContext
          || op instanceof VitruvOCLParser.CountOpContext
          || op instanceof VitruvOCLParser.NotEmptyOpContext
          || op instanceof VitruvOCLParser.IncludesOpContext
          || op instanceof VitruvOCLParser.IncludesAllOpContext
          || op instanceof VitruvOCLParser.SelectOpContext
          || op instanceof VitruvOCLParser.RejectOpContext
          || op instanceof VitruvOCLParser.CollectOpContext
          || op instanceof VitruvOCLParser.FlattenOpContext
          || op instanceof VitruvOCLParser.IncludingOpContext
          || op instanceof VitruvOCLParser.ExcludingOpContext
          || op instanceof VitruvOCLParser.AppendOpContext
          || op instanceof VitruvOCLParser.PrependOpContext
          || op instanceof VitruvOCLParser.InsertAtOpContext
          || op instanceof VitruvOCLParser.AsSetOpContext
          || op instanceof VitruvOCLParser.AsBagOpContext
          || op instanceof VitruvOCLParser.AsSequenceOpContext
          || op instanceof VitruvOCLParser.AsOrderedSetOpContext
          || op instanceof VitruvOCLParser.OclAsTypeOpContext) {
        return SAME;
      }
      return BOTH;
    }

    Polarity receiver(Polarity p) {
      return switch (this) {
        case SAME, EXISTS, SAME_ALL -> p;
        case FLIP, FOR_ALL -> p.flip();
        case BOTH -> Polarity.BOTH;
      };
    }

    Polarity argument(Polarity p) {
      return switch (this) {
        case FOR_ALL, EXISTS, SAME_ALL -> p;
        default -> Polarity.BOTH;
      };
    }

    /** Iterator bodies decide a Boolean per element: emptiness matters there, too. */
    boolean guardedArgument() {
      return this == FOR_ALL || this == EXISTS;
    }
  }
}
