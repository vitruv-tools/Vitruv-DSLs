/*******************************************************************************
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
package tools.vitruv.dsls.vitruvOCL.correspondence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Type-matrix test suite for the Correspondence Filter extensions: {@code select(~)}, {@code
 * select(~, Tag='x')}, {@code select(~, Type=T)}, {@code select(~, Type=T, Tag='x')}, and the same
 * for {@code reject} / {@code exists}.
 *
 * <h2>Fixture files</h2>
 *
 * <pre>
 *   src/test/resources/test-metamodels/family.ecore
 *   src/test/resources/test-metamodels/persons.ecore
 *   src/test/resources/test-metamodels/correspondence.ecore
 *   src/test/resources/test-models/testmodel.family
 *   src/test/resources/test-models/testmodel.persons
 *   src/test/resources/test-models/testmodel.correspondence
 * </pre>
 *
 * <h2>Correspondences in the fixture</h2>
 *
 * <pre>
 *   father  "Homer" <->  homer "Homer Simpson"  tag="Husband"
 *   mother  "Marge" <->  marge "Marge Simpson"  tag="Wife"
 *   son     "Bart"  <->  bart  "Bart Simpson"   tag="Son"
 *   daughter "Lisa"     (no correspondence — negative-test anchor)
 * </pre>
 *
 * <h2>Test matrix</h2>
 *
 * <pre>
 * Operation \ Filter    (none)  Tag only  Type only  Type+Tag
 * select(~)              ✓         ✓         ✓          ✓
 * reject(~)              ✓         ✓         ✓          ✓
 * exists(~)              ✓         ✓         ✓          ✓
 * </pre>
 *
 * Plus chaining tests that mirror the CollectionChaining matrix style.
 */
@DisplayName("Correspondence Filter Type-Matrix Tests")
public class CorrespondenceFilterTest {

  // ================================================================
  // Metamodel paths
  // ================================================================

  private static final Path FAMILY_ECORE =
      Path.of("src/test/resources/test-metamodels/family.ecore");
  private static final Path PERSONS_ECORE =
      Path.of("src/test/resources/test-metamodels/persons.ecore");
  private static final Path CORRESPONDENCE_ECORE =
      Path.of("src/test/resources/test-metamodels/correspondence.ecore");

  // ================================================================
  // Model instance paths (relative — resolved against TEST_MODELS_PATH)
  // ================================================================

  private static final Path FAMILY_MODEL = Path.of("testmodel.family");
  private static final Path PERSONS_MODEL = Path.of("testmodel.persons");
  private static final Path CORR_MODEL = Path.of("testmodel.correspondence");

  private static final Path[] ECORES = {FAMILY_ECORE, PERSONS_ECORE, CORRESPONDENCE_ECORE};
  private static final Path[] MODELS = {FAMILY_MODEL, PERSONS_MODEL, CORR_MODEL};

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ================================================================
  // Evaluate helper
  // ================================================================

  private ConstraintResult eval(String constraint) throws Exception {
    return VitruvOCL.evaluateConstraint(constraint, ECORES, MODELS);
  }

  // ================================================================
  // Assertion helpers
  // ================================================================

  private void assertSatisfied(ConstraintResult result) {
    assertTrue(result.isSuccess(), "Evaluation failed with errors: " + result.getWarningsSummary());
    assertTrue(result.isSatisfied(), "Constraint not satisfied");
  }

  private void assertNotSatisfied(ConstraintResult result) {
    assertTrue(result.isSuccess(), "Evaluation failed with errors: " + result.getWarningsSummary());
    assertFalse(result.isSatisfied(), "Constraint should not be satisfied");
  }

  // ================================================================
  // 1. select(~) — no filter
  // ================================================================

  @Nested
  @DisplayName("select(~) — no filter")
  public class SelectNoFilter {

    @Test
    @DisplayName("testSelectTilde_notEmptyForFather — Homer has a correspondence")
    public void testSelectTilde_notEmptyForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTilde_sizeOneForFather — exactly 1 corresponding person")
    public void testSelectTilde_sizeOneForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTilde_emptyForDaughter — Lisa has no correspondence")
    public void testSelectTilde_emptyForDaughter() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Lisa" implies
                  persons::Person.allInstances().select(~).isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTilde_sizeZeroForDaughter")
    public void testSelectTilde_sizeZeroForDaughter() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Lisa" implies
                  persons::Person.allInstances().select(~).size() == 0
              """));
    }

    @Test
    @DisplayName("testSelectTilde_wrongCount_false — father does not have 3 matches")
    public void testSelectTilde_wrongCount_false() throws Exception {
      assertNotSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).size() == 3
              """));
    }
  }

  // ================================================================
  // 2. select(~, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("select(~, Tag='x') — tag filter only")
  public class SelectTagFilter {

    @Test
    @DisplayName("testSelectTildeTag_Husband_notEmpty")
    public void testSelectTildeTag_Husband_notEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Tag = "Husband").notEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_Husband_sizeOne")
    public void testSelectTildeTag_Husband_sizeOne() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Tag = "Husband").size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_Wife_emptyForFather — wrong tag")
    public void testSelectTildeTag_Wife_emptyForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Tag = "Wife").isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_Son_sizeOne")
    public void testSelectTildeTag_Son_sizeOne() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Bart" implies
                  persons::Person.allInstances().select(~, Tag = "Son").size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_NonExistent_empty — unknown tag yields empty")
    public void testSelectTildeTag_NonExistent_empty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().select(~, Tag = "NoSuchTag").isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_Wife_notEmpty_forMother")
    public void testSelectTildeTag_Wife_notEmpty_forMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().select(~, Tag = "Wife").notEmpty()
              """));
    }
  }

  // ================================================================
  // 3. select(~, Type=T)
  // ================================================================

  @Nested
  @DisplayName("select(~, Type=T) — type filter only")
  public class SelectTypeFilter {

    @Test
    @DisplayName("testSelectTildeType_Male_sizeOneForFather — homer is Male")
    public void testSelectTildeType_Male_sizeOneForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Type = persons::Male).size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeType_Female_emptyForFather — homer is not Female")
    public void testSelectTildeType_Female_emptyForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Type = persons::Female).isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeType_Female_sizeOneForMother — marge is Female")
    public void testSelectTildeType_Female_sizeOneForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().select(~, Type = persons::Female).size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeType_Male_emptyForMother — marge is not Male")
    public void testSelectTildeType_Male_emptyForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().select(~, Type = persons::Male).isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeType_Person_sizeOne — abstract supertype accepted")
    public void testSelectTildeType_Person_sizeOne() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Type = persons::Person).size() == 1
              """));
    }
  }

  // ================================================================
  // 4. select(~, Type=T, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("select(~, Type=T, Tag='x') — combined filter")
  public class SelectTypeAndTagFilter {

    @Test
    @DisplayName("testSelectTildeTypeMaleTagHusband_sizeOneForFather")
    public void testSelectTildeTypeMaleTagHusband_sizeOneForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .select(~, Type = persons::Male, Tag = "Husband").size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeTypeFemaleTagHusband_empty — type mismatch")
    public void testSelectTildeTypeFemaleTagHusband_empty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .select(~, Type = persons::Female, Tag = "Husband").isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTypeMaleTagWife_empty — tag mismatch")
    public void testSelectTildeTypeMaleTagWife_empty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .select(~, Type = persons::Male, Tag = "Wife").isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTagFirst_typeSecond — option order is arbitrary")
    public void testSelectTildeTagFirst_typeSecond() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Bart" implies
                  persons::Person.allInstances()
                    .select(~, Tag = "Son", Type = persons::Male).size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeTypeFemaleTagWife_sizeOneForMother")
    public void testSelectTildeTypeFemaleTagWife_sizeOneForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances()
                    .select(~, Type = persons::Female, Tag = "Wife").size() == 1
              """));
    }
  }

  // ================================================================
  // 5. reject(~) — no filter
  // ================================================================

  @Nested
  @DisplayName("reject(~) — no filter")
  public class RejectNoFilter {

    @Test
    @DisplayName("testRejectTilde_sizeThree — 4 persons minus 1 match = 3")
    public void testRejectTilde_sizeThree() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~).size() == 3
              """));
    }

    @Test
    @DisplayName("testRejectTilde_notEmpty — non-matching persons remain")
    public void testRejectTilde_notEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testRejectTilde_sizeFourForDaughter — no match, all 4 kept")
    public void testRejectTilde_sizeFourForDaughter() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Lisa" implies
                  persons::Person.allInstances().reject(~).size() == 4
              """));
    }
  }

  // ================================================================
  // 6. reject(~, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("reject(~, Tag='x') — tag filter")
  public class RejectTagFilter {

    @Test
    @DisplayName("testRejectTildeTag_Husband_sizeThree — removes homer only")
    public void testRejectTildeTag_Husband_sizeThree() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~, Tag = "Husband").size() == 3
              """));
    }

    @Test
    @DisplayName("testRejectTildeTag_Wife_sizeFourForFather — no Wife corr for father")
    public void testRejectTildeTag_Wife_sizeFourForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~, Tag = "Wife").size() == 4
              """));
    }

    @Test
    @DisplayName("testRejectTildeTag_Wife_sizeThreeForMother — removes marge")
    public void testRejectTildeTag_Wife_sizeThreeForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().reject(~, Tag = "Wife").size() == 3
              """));
    }
  }

  // ================================================================
  // 7. reject(~, Type=T)
  // ================================================================

  @Nested
  @DisplayName("reject(~, Type=T) — type filter")
  public class RejectTypeFilter {

    @Test
    @DisplayName("testRejectTildeType_Male_sizeThreeForFather — removes homer")
    public void testRejectTildeType_Male_sizeThreeForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~, Type = persons::Male).size() == 3
              """));
    }

    @Test
    @DisplayName("testRejectTildeType_Female_sizeFourForFather — type mismatch, all kept")
    public void testRejectTildeType_Female_sizeFourForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~, Type = persons::Female).size() == 4
              """));
    }

    @Test
    @DisplayName("testRejectTildeType_Female_sizeThreeForMother — removes marge")
    public void testRejectTildeType_Female_sizeThreeForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().reject(~, Type = persons::Female).size() == 3
              """));
    }
  }

  // ================================================================
  // 8. reject(~, Type=T, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("reject(~, Type=T, Tag='x') — combined filter")
  public class RejectTypeAndTagFilter {

    @Test
    @DisplayName("testRejectTildeTypeMaleTagHusband_sizeThreeForFather")
    public void testRejectTildeTypeMaleTagHusband_sizeThreeForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .reject(~, Type = persons::Male, Tag = "Husband").size() == 3
              """));
    }

    @Test
    @DisplayName("testRejectTildeTypeFemaleTagHusband_sizeFourForFather — type mismatch")
    public void testRejectTildeTypeFemaleTagHusband_sizeFourForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .reject(~, Type = persons::Female, Tag = "Husband").size() == 4
              """));
    }

    @Test
    @DisplayName("testRejectTildeTypeMaleTagWife_sizeFourForFather — tag mismatch")
    public void testRejectTildeTypeMaleTagWife_sizeFourForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .reject(~, Type = persons::Male, Tag = "Wife").size() == 4
              """));
    }
  }

  // ================================================================
  // 9. exists(~) — no filter
  // ================================================================

  @Nested
  @DisplayName("exists(~) — no filter")
  public class ExistsNoFilter {

    @Test
    @DisplayName("testExistsTilde_trueForFather — father has a correspondence")
    public void testExistsTilde_trueForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().exists(~)
              """));
    }

    @Test
    @DisplayName("testExistsTilde_falseForDaughter — daughter has no correspondence")
    public void testExistsTilde_falseForDaughter() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Lisa" implies
                  not persons::Person.allInstances().exists(~)
              """));
    }
  }

  // ================================================================
  // 10. exists(~, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("exists(~, Tag='x') — tag filter")
  public class ExistsTagFilter {

    @Test
    @DisplayName("testExistsTildeTag_Husband_trueForFather")
    public void testExistsTildeTag_Husband_trueForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().exists(~, Tag = "Husband")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_Wife_falseForFather — wrong tag")
    public void testExistsTildeTag_Wife_falseForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances().exists(~, Tag = "Wife")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_Son_trueForSon")
    public void testExistsTildeTag_Son_trueForSon() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Bart" implies
                  persons::Person.allInstances().exists(~, Tag = "Son")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_NonExistent_false")
    public void testExistsTildeTag_NonExistent_false() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                not persons::Person.allInstances().exists(~, Tag = "NoSuchTag")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_Wife_trueForMother")
    public void testExistsTildeTag_Wife_trueForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().exists(~, Tag = "Wife")
              """));
    }
  }

  // ================================================================
  // 11. exists(~, Type=T)
  // ================================================================

  @Nested
  @DisplayName("exists(~, Type=T) — type filter")
  public class ExistsTypeFilter {

    @Test
    @DisplayName("testExistsTildeType_Male_trueForFather — homer is Male")
    public void testExistsTildeType_Male_trueForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().exists(~, Type = persons::Male)
              """));
    }

    @Test
    @DisplayName("testExistsTildeType_Female_falseForFather — homer is not Female")
    public void testExistsTildeType_Female_falseForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances().exists(~, Type = persons::Female)
              """));
    }

    @Test
    @DisplayName("testExistsTildeType_Female_trueForMother — marge is Female")
    public void testExistsTildeType_Female_trueForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances().exists(~, Type = persons::Female)
              """));
    }

    @Test
    @DisplayName("testExistsTildeType_Male_falseForMother — marge is not Male")
    public void testExistsTildeType_Male_falseForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  not persons::Person.allInstances().exists(~, Type = persons::Male)
              """));
    }
  }

  // ================================================================
  // 12. exists(~, Type=T, Tag='x')
  // ================================================================

  @Nested
  @DisplayName("exists(~, Type=T, Tag='x') — combined filter")
  public class ExistsTypeAndTagFilter {

    @Test
    @DisplayName("testExistsTildeTypeMaleTagHusband_trueForFather")
    public void testExistsTildeTypeMaleTagHusband_trueForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances()
                    .exists(~, Type = persons::Male, Tag = "Husband")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTypeFemaleTagHusband_false — type mismatch")
    public void testExistsTildeTypeFemaleTagHusband_false() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances()
                    .exists(~, Type = persons::Female, Tag = "Husband")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTypeMaleTagWife_false — tag mismatch")
    public void testExistsTildeTypeMaleTagWife_false() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances()
                    .exists(~, Type = persons::Male, Tag = "Wife")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTypeFemaleTagWife_trueForMother")
    public void testExistsTildeTypeFemaleTagWife_trueForMother() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Marge" implies
                  persons::Person.allInstances()
                    .exists(~, Type = persons::Female, Tag = "Wife")
              """));
    }
  }

  // ================================================================
  // 13. Chaining: select(~) → ...
  // ================================================================

  @Nested
  @DisplayName("Chaining: select(~) → ...")
  public class SelectChainingTests {

    @Test
    @DisplayName("testSelectTilde_thenNotEmpty")
    public void testSelectTilde_thenNotEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTilde_thenIsEmpty_false")
    public void testSelectTilde_thenIsEmpty_false() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances().select(~).isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTilde_thenSize")
    public void testSelectTilde_thenSize() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTilde_thenExists")
    public void testSelectTilde_thenExists() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).exists(p | p == p)
              """));
    }

    @Test
    @DisplayName("testSelectTilde_thenForAll")
    public void testSelectTilde_thenForAll() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).forAll(p | p == p)
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_thenSize")
    public void testSelectTildeTag_thenSize() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Tag = "Husband").size() == 1
              """));
    }

    @Test
    @DisplayName("testSelectTildeType_thenNotEmpty")
    public void testSelectTildeType_thenNotEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~, Type = persons::Male).notEmpty()
              """));
    }
  }

  // ================================================================
  // 14. Chaining: reject(~) → ...
  // ================================================================

  @Nested
  @DisplayName("Chaining: reject(~) → ...")
  public class RejectChainingTests {

    @Test
    @DisplayName("testRejectTilde_thenNotEmpty")
    public void testRejectTilde_thenNotEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testRejectTilde_thenSize")
    public void testRejectTilde_thenSize() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~).size() == 3
              """));
    }

    @Test
    @DisplayName("testRejectTilde_thenIsEmpty_false")
    public void testRejectTilde_thenIsEmpty_false() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not persons::Person.allInstances().reject(~).isEmpty()
              """));
    }

    @Test
    @DisplayName("testRejectTildeTag_thenSize")
    public void testRejectTildeTag_thenSize() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().reject(~, Tag = "Husband").size() == 3
              """));
    }
  }

  // ================================================================
  // 15. Chaining: exists(~) in logical expressions
  // ================================================================

  @Nested
  @DisplayName("Chaining: exists(~) in logical expressions")
  public class ExistsLogicalChainingTests {

    @Test
    @DisplayName("testExistsTilde_andTrue")
    public void testExistsTilde_andTrue() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  (persons::Person.allInstances().exists(~) and true)
              """));
    }

    @Test
    @DisplayName("testExistsTilde_andFalse_isAlwaysFalse")
    public void testExistsTilde_andFalse_isAlwaysFalse() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not (persons::Person.allInstances().exists(~) and false)
              """));
    }

    @Test
    @DisplayName("testExistsTilde_orFalse_staysTrue")
    public void testExistsTilde_orFalse_staysTrue() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  (persons::Person.allInstances().exists(~) or false)
              """));
    }

    @Test
    @DisplayName("testNotExistsTilde_falseForFather")
    public void testNotExistsTilde_falseForFather() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  not (not persons::Person.allInstances().exists(~))
              """));
    }

    @Test
    @DisplayName("testExistsTilde_implies_selectNotEmpty")
    public void testExistsTilde_implies_selectNotEmpty() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().exists(~)
                  implies
                persons::Person.allInstances().select(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_and_selectTildeTagSize")
    public void testExistsTildeTag_and_selectTildeTagSize() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  (persons::Person.allInstances().exists(~, Tag = "Husband")
                    and
                  persons::Person.allInstances().select(~, Tag = "Husband").size() == 1)
              """));
    }

    @Test
    @DisplayName("testSelectTilde_thenCollect — collect firstName from corresponding persons")
    public void testSelectTilde_thenCollect() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  persons::Person.allInstances().select(~).collect(p | p.fullName).notEmpty()
              """));
    }
  }

  // ================================================================
  // 16. Cross-operation consistency
  // ================================================================

  @Nested
  @DisplayName("Cross-operation consistency")
  public class CrossOperationConsistency {

    @Test
    @DisplayName("testSelectAndRejectSizeSumToTotal")
    public void testSelectAndRejectSizeSumToTotal() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().select(~).size()
                  + persons::Person.allInstances().reject(~).size()
                  == persons::Person.allInstances().size()
              """));
    }

    @Test
    @DisplayName("testExistsEqualsSelectNotEmpty_whenMatchExists")
    public void testExistsEqualsSelectNotEmpty_whenMatchExists() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().exists(~)
                  == persons::Person.allInstances().select(~).notEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTagSize_consistentWithExistsTag")
    public void testSelectTagSize_consistentWithExistsTag() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                self.firstName == "Homer" implies
                  (persons::Person.allInstances().select(~, Tag = "Husband").size() == 1
                    and
                  persons::Person.allInstances().exists(~, Tag = "Husband"))
              """));
    }

    @Test
    @DisplayName("testSelectAndRejectTagSumToTotal")
    public void testSelectAndRejectTagSumToTotal() throws Exception {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().select(~, Tag = "Husband").size()
                  + persons::Person.allInstances().reject(~, Tag = "Husband").size()
                  == persons::Person.allInstances().size()
              """));
    }
  }

  @Test
  public void debugStep1_grammarParses() throws Exception {
    // Wenn das hier schon fehlschlägt → ANTLR nicht regeneriert
    VitruvOCLLexer lexer =
        new VitruvOCLLexer(
            CharStreams.fromString(
                """
                context family::Member inv:
                  persons::Person.allInstances().select(~).size() >= 0
                """));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    var tree = parser.contextDeclCS();
    System.out.println("Parse errors: " + parser.getNumberOfSyntaxErrors());
    assertEquals(0, parser.getNumberOfSyntaxErrors());
  }

  @Test
  public void debugStep2_grammarParsesWithTag() throws Exception {
    // Wenn DAS fehlschlägt aber Step 1 nicht → correspondenceFilterCS fehlt im generierten Parser
    VitruvOCLLexer lexer =
        new VitruvOCLLexer(
            CharStreams.fromString(
                """
                context family::Member inv:
                  persons::Person.allInstances().select(~, Tag = "Husband").size() >= 0
                """));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    var tree = parser.contextDeclCS();
    System.out.println("Parse errors: " + parser.getNumberOfSyntaxErrors());
    assertEquals(0, parser.getNumberOfSyntaxErrors());
  }

  @Test
  public void debugStep3_trivialConstraint() throws Exception {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context family::Member inv: true", ECORES, MODELS);
    System.out.println("isSuccess=" + result.isSuccess());
    System.out.println("isSatisfied=" + result.isSatisfied());
    System.out.println("errors=" + result.getWarningsSummary());
    // Wenn das fehlschlägt → Modelle laden nicht korrekt
  }

  @Test
  public void debugStep4_oldSelectTildeNoFilter() throws Exception {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context family::Member inv:
              persons::Person.allInstances().select(~).size() >= 0
            """,
            ECORES,
            MODELS);
    System.out.println("isSuccess=" + result.isSuccess());
    System.out.println("isSatisfied=" + result.isSatisfied());
    System.out.println("errors=" + result.toDetailedErrorString());
    // Wenn das fehlschlägt aber Step 3 nicht → select(~) Grammatikregel kaputt
  }

  @Test
  public void debugStep_trivialTrue() throws Exception {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context family::Member inv: true", ECORES, MODELS);
    System.out.println("isSuccess=" + result.isSuccess());
    System.out.println("isSatisfied=" + result.isSatisfied());
    System.out.println("errors=" + result.toDetailedErrorString());
  }
}