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

import java.util.ArrayList;
import java.util.Arrays;
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
 * Targeted checks that the {@code navigateOne()} many-valued-feature streaming fix ({@link
 * EvaluationVisitor.SkipNullMappingIterator}) preserves two behaviors the old eager {@code for
 * (Object item : list) if (item != null) ...} loop had, now that pulling happens lazily one
 * element at a time instead of all at once up front:
 *
 * <ol>
 *   <li><b>Null-skipping</b>: {@code null} entries in the backing list must still be skipped, one
 *       at a time as encountered, not just when the whole list happens to be pre-scanned.
 *   <li><b>EReference vs. EAttribute</b>: a many-valued {@code EReference} (whose elements wrap to
 *       {@link OCLElement.MetaclassValue} via {@link EvaluationVisitor#wrapValue}) must work
 *       per-element exactly like a many-valued {@code EAttribute} - including when a reference
 *       slot is {@code null} (an unset/absent reference in the list) interspersed with EObject
 *       values.
 * </ol>
 */
class NavigationNullAndReferenceHandlingTest {

  private static final EPackage DUMMY_PACKAGE;
  private static final EClass ROOT_CLASS;
  private static final EClass CHILD_CLASS;
  private static final EReference CHILDREN_FEATURE;
  private static final EAttribute LISTE_FEATURE;
  private static final EAttribute NAME_FEATURE;

  static {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    DUMMY_PACKAGE = factory.createEPackage();
    DUMMY_PACKAGE.setName("dummy4");
    DUMMY_PACKAGE.setNsPrefix("dummy4");
    DUMMY_PACKAGE.setNsURI("http://test/dummy4");

    ROOT_CLASS = factory.createEClass();
    ROOT_CLASS.setName("Root");
    DUMMY_PACKAGE.getEClassifiers().add(ROOT_CLASS);

    CHILD_CLASS = factory.createEClass();
    CHILD_CLASS.setName("Child");
    DUMMY_PACKAGE.getEClassifiers().add(CHILD_CLASS);

    CHILDREN_FEATURE = factory.createEReference();
    CHILDREN_FEATURE.setName("children");
    CHILDREN_FEATURE.setEType(CHILD_CLASS);
    CHILDREN_FEATURE.setUpperBound(-1);
    CHILDREN_FEATURE.setOrdered(true);
    CHILDREN_FEATURE.setUnique(false);
    ROOT_CLASS.getEStructuralFeatures().add(CHILDREN_FEATURE);

    LISTE_FEATURE = factory.createEAttribute();
    LISTE_FEATURE.setName("liste");
    LISTE_FEATURE.setEType(EcorePackage.Literals.EINT);
    LISTE_FEATURE.setUpperBound(-1);
    LISTE_FEATURE.setOrdered(true);
    LISTE_FEATURE.setUnique(false);
    ROOT_CLASS.getEStructuralFeatures().add(LISTE_FEATURE);

    NAME_FEATURE = factory.createEAttribute();
    NAME_FEATURE.setName("name");
    NAME_FEATURE.setEType(EcorePackage.Literals.ESTRING);
    NAME_FEATURE.setUpperBound(1);
    CHILD_CLASS.getEStructuralFeatures().add(NAME_FEATURE);
  }

  private static final class Child extends DynamicEObjectImpl {
    private final String name;

    Child(String name) {
      super(CHILD_CLASS);
      this.name = name;
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
      if (feature == NAME_FEATURE) {
        return name;
      }
      return super.eGet(feature);
    }
  }

  private static final class Root extends DynamicEObjectImpl {
    private final List<?> liste;
    private final List<?> children;

    Root(List<?> liste, List<?> children) {
      super(ROOT_CLASS);
      this.liste = liste;
      this.children = children;
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
      if (feature == LISTE_FEATURE) {
        return liste;
      }
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
        if (!"dummy4".equals(metamodelName)) {
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
        return Set.of("dummy4");
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
  void nullsInterspersedInAManyValuedEAttributeAreSkippedOneAtATime() {
    // [5, null, null, 3, null, 9, null] - nulls at the start, middle (consecutive), and end.
    List<Object> listeWithNulls = new ArrayList<>(Arrays.asList(5, null, null, 3, null, 9, null));
    Root root = new Root(listeWithNulls, List.of());

    boolean sizeOk =
        evaluateInvariant("context dummy4::Root inv: self.liste.size() == 3", wrapperFor(root));
    assertTrue(sizeOk, "Expected the 4 nulls to be skipped, leaving 3 elements");

    boolean orderOk =
        evaluateInvariant(
            "context dummy4::Root inv: "
                + "self.liste.at(1) == 5 and self.liste.at(2) == 3 and self.liste.at(3) == 9",
            wrapperFor(root));
    assertTrue(orderOk, "Expected surviving elements [5, 3, 9] to keep their relative order");
  }

  @Test
  void nullReferenceSlotsInAManyValuedEReferenceAreSkippedAndRemainingObjectsWrapCorrectly() {
    // children = [A, null, B, null] - two real Child EObjects with an unset reference slot
    // between and after each. Proves wrapValue()'s EObject branch still runs correctly per
    // element when reached lazily, and that a null EReference slot is skipped exactly like a
    // null EAttribute slot.
    Child a = new Child("alpha");
    Child b = new Child("beta");
    List<Object> childrenWithNulls = new ArrayList<>(Arrays.asList(a, null, b, null));
    Root root = new Root(List.of(), childrenWithNulls);

    boolean sizeOk =
        evaluateInvariant("context dummy4::Root inv: self.children.size() == 2", wrapperFor(root));
    assertTrue(sizeOk, "Expected the 2 null reference slots to be skipped, leaving 2 children");

    boolean namesOk =
        evaluateInvariant(
            "context dummy4::Root inv: "
                + "self.children.select(c | c.name == \"alpha\").notEmpty() and "
                + "self.children.select(c | c.name == \"beta\").notEmpty()",
            wrapperFor(root));
    assertTrue(namesOk, "Expected both real children (alpha, beta) to be independently navigable");
  }
}
