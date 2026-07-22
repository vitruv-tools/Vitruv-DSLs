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

package tools.vitruv.dsls.vitruvocl.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Correctness proof for the {@code navigateOne()} many-valued-feature streaming fix, following the
 * same nested-shadowing-with-revert-verification protocol used for the earlier {@code iterate}/
 * {@code isUnique}/{@code one}/{@code sortedBy}/{@code collectNested} streaming fixes in this
 * session.
 *
 * <h2>Why a shadowing construction here is a lower a priori risk, but tested anyway</h2>
 *
 * <p>Unlike those earlier fixes, {@code navigateOne()} itself never touches {@code symbolTable} or
 * constructs a {@code LocalScope} - it is a pure data-access change (lazy vs. eager) with no
 * iterator-variable binding of its own. The scope-corruption failure mode those earlier fixes
 * guarded against (a wrongly-ordered {@code LocalScope} construction relative to draining a lazy
 * receiver) has no analogue here. This test is built anyway, for the same due-diligence reason the
 * task asked for it: to empirically confirm that expectation rather than assume it.
 *
 * <p>Metamodel: {@code dummy3::Root} has a many-valued {@code EReference children} (to {@code
 * dummy3::Child}, exercising {@code navigateOne()}'s {@code EReference} path across two distinct
 * receiver elements per {@link EvaluationVisitor#visitPropertyAccessWithReceiver}'s outer {@code
 * flatMap}); each {@code dummy3::Child} has a many-valued {@code EAttribute liste} (exercising the
 * {@code EAttribute} path, the one actually changed by the streaming fix).
 *
 * <p>Expression: {@code let x = 999 in self.children.select(c | let x = 20 in
 * c.liste.select(v | v == x).notEmpty()).size() == 1} - the inner {@code let x = 20} shadows the
 * outer {@code let x = 999} for the duration of each child's navigation+select. Child A's {@code
 * liste} contains no {@code 20} (so it is excluded from the outer select() only if {@code x}
 * resolves to the shadowed {@code 20}); Child B's does (so it is included only under the same
 * condition). Wrong resolution (to the outer {@code x = 999}, which no child's {@code liste}
 * contains) does not "self-heal" to the same answer - it collapses the outer {@code select()} to
 * empty ({@code size() == 0}), a definitively different, detectable result, not a lucky match.
 */
class NavigationManyValuedShadowingTest {

  private static final EPackage DUMMY_PACKAGE;
  private static final EClass ROOT_CLASS;
  private static final EClass CHILD_CLASS;
  private static final EReference CHILDREN_FEATURE;
  private static final EAttribute LISTE_FEATURE;

  static {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    DUMMY_PACKAGE = factory.createEPackage();
    DUMMY_PACKAGE.setName("dummy3");
    DUMMY_PACKAGE.setNsPrefix("dummy3");
    DUMMY_PACKAGE.setNsURI("http://test/dummy3");

    ROOT_CLASS = factory.createEClass();
    ROOT_CLASS.setName("Root");
    DUMMY_PACKAGE.getEClassifiers().add(ROOT_CLASS);

    CHILD_CLASS = factory.createEClass();
    CHILD_CLASS.setName("Child");
    DUMMY_PACKAGE.getEClassifiers().add(CHILD_CLASS);

    CHILDREN_FEATURE = factory.createEReference();
    CHILDREN_FEATURE.setName("children");
    CHILDREN_FEATURE.setEType(CHILD_CLASS);
    CHILDREN_FEATURE.setUpperBound(-1); // many-valued
    CHILDREN_FEATURE.setOrdered(true);
    CHILDREN_FEATURE.setUnique(true);
    ROOT_CLASS.getEStructuralFeatures().add(CHILDREN_FEATURE);

    LISTE_FEATURE = factory.createEAttribute();
    LISTE_FEATURE.setName("liste");
    LISTE_FEATURE.setEType(EcorePackage.Literals.EINT);
    LISTE_FEATURE.setUpperBound(-1); // many-valued
    LISTE_FEATURE.setOrdered(true);
    LISTE_FEATURE.setUnique(false);
    CHILD_CLASS.getEStructuralFeatures().add(LISTE_FEATURE);
  }

  private static final class Child extends DynamicEObjectImpl {
    private final List<Integer> liste;

    Child(List<Integer> liste) {
      super(CHILD_CLASS);
      this.liste = liste;
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
      if (feature == LISTE_FEATURE) {
        return liste;
      }
      return super.eGet(feature);
    }
  }

  private static final class Root extends DynamicEObjectImpl {
    private final List<EObject> children;

    Root(List<EObject> children) {
      super(ROOT_CLASS);
      this.children = children;
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
      if (feature == CHILDREN_FEATURE) {
        return children;
      }
      return super.eGet(feature);
    }
  }

  private MetamodelWrapperInterface wrapperFor(EObject instance) {
    return new MetamodelWrapperInterface() {
      @Override
      public EClass resolveEClass(String metamodelName, String className) {
        if (!"dummy3".equals(metamodelName)) {
          return null;
        }
        if ("Root".equals(className)) {
          return ROOT_CLASS;
        }
        if ("Child".equals(className)) {
          return CHILD_CLASS;
        }
        return null;
      }

      @Override
      public List<EObject> getAllInstances(EClass eClass) {
        return eClass == ROOT_CLASS ? List.of(instance) : List.of();
      }

      @Override
      public org.eclipse.emf.ecore.EEnum resolveEEnum(String enumName) {
        return null;
      }

      @Override
      public Set<String> getAvailableMetamodels() {
        return Set.of("dummy3");
      }

      @Override
      public String getInstanceNameByIndex(int index) {
        return null;
      }

      @Override
      public List<EObject> getAllRootObjects() {
        return List.of(instance);
      }

      @Override
      public EObject getContextObjectByIndex(int index) {
        return index == 0 ? instance : null;
      }

      @Override
      public EClass resolveEClassByShortName(String shortName) {
        if ("Root".equals(shortName)) {
          return ROOT_CLASS;
        }
        if ("Child".equals(shortName)) {
          return CHILD_CLASS;
        }
        return null;
      }

      @Override
      public String getSourceFileForInstance(EObject inst) {
        return null;
      }

      @Override
      public Set<EObject> getCorrespondingObjects(EObject source) {
        return Set.of();
      }

      @Override
      public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
        return false;
      }
    };
  }

  private boolean evaluateInvariant(String constraint, MetamodelWrapperInterface wrapper) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(constraint));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.contextDeclCS();

    SymbolTable symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);
    if (errors.hasErrors()) {
      throw new AssertionError("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.visit(tree);
    if (typeChecker.hasErrors()) {
      StringBuilder sb = new StringBuilder("Pass 2 (Type checking) failed:");
      for (var e : typeChecker.getErrorCollector().getErrors()) {
        sb.append("\n  ").append(e.getMessage());
      }
      throw new AssertionError(sb.toString());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);
    if (evaluator.hasErrors()) {
      throw new AssertionError("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    assertEquals(1, result.size(), "Expected exactly one invariant result");
    Boolean bool = result.getElements().get(0).tryGetBool();
    assertTrue(bool != null, "Expected a boolean invariant result");
    return bool;
  }

  @Test
  void navigationOnManyValuedFeatureResolvesShadowedVariableCorrectlyAcrossMultipleReceiverElements() {
    // Child A: no "20" in liste -> excluded regardless (never matches, correct or wrong x).
    // Child B: has a "20" -> included ONLY if x correctly resolves to the inner shadowed 20.
    Child childA = new Child(List.of(1, 2, 3));
    Child childB = new Child(List.of(10, 20, 30));
    Root root = new Root(List.of(childA, childB));

    boolean result =
        evaluateInvariant(
            "context dummy3::Root inv: "
                + "let x = 999 in "
                + "self.children.select(c | let x = 20 in c.liste.select(v | v == x).notEmpty())"
                + ".size() == 1",
            wrapperFor(root));

    assertTrue(
        result,
        "Expected exactly Child B to match under the correctly-shadowed inner x = 20 "
            + "(wrong resolution to the outer x = 999 would collapse the result to size() == 0)");
  }
}
