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

package tools.vitruv.dsls.vitruvocl.correspondence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

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
 * <p>Plus chaining tests that mirror the CollectionChaining matrix style.
 */
@DisplayName("Correspondence Filter Type-Matrix Tests")
class CorrespondenceFilterTest {

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
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  // ================================================================
  // Evaluate helper
  // ================================================================

  private ConstraintResult eval(String constraint) {
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
  class SelectNoFilter {

    @Test
    @DisplayName("testSelectTilde_notEmptyForFather — Homer has a correspondence")
    void testSelectTilde_notEmptyForFather() {
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
    void testSelectTilde_sizeOneForFather() {
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
    void testSelectTilde_emptyForDaughter() {
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
    void testSelectTilde_sizeZeroForDaughter() {
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
    void testSelectTilde_wrongCount_false() {
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
  class SelectTagFilter {

    @Test
    @DisplayName("testSelectTildeTag_Husband_notEmpty")
    void testSelectTildeTag_husband_notEmpty() {
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
    void testSelectTildeTag_husband_sizeOne() {
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
    void testSelectTildeTag_wife_emptyForFather() {
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
    void testSelectTildeTag_son_sizeOne() {
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
    void testSelectTildeTag_nonExistent_empty() {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                persons::Person.allInstances().select(~, Tag = "NoSuchTag").isEmpty()
              """));
    }

    @Test
    @DisplayName("testSelectTildeTag_Wife_notEmpty_forMother")
    void testSelectTildeTag_wife_notEmpty_forMother() {
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
  class SelectTypeFilter {

    @Test
    @DisplayName("testSelectTildeType_Male_sizeOneForFather — homer is Male")
    void testSelectTildeType_male_sizeOneForFather() {
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
    void testSelectTildeType_female_emptyForFather() {
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
    void testSelectTildeType_female_sizeOneForMother() {
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
    void testSelectTildeType_male_emptyForMother() {
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
    void testSelectTildeType_person_sizeOne() {
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
  class SelectTypeAndTagFilter {

    @Test
    @DisplayName("testSelectTildeTypeMaleTagHusband_sizeOneForFather")
    void testSelectTildeTypeMaleTagHusband_sizeOneForFather() {
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
    void testSelectTildeTypeFemaleTagHusband_empty() {
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
    void testSelectTildeTypeMaleTagWife_empty() {
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
    void testSelectTildeTagFirst_typeSecond() {
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
    void testSelectTildeTypeFemaleTagWife_sizeOneForMother() {
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
  class RejectNoFilter {

    @Test
    @DisplayName("testRejectTilde_sizeThree — 4 persons minus 1 match = 3")
    void testRejectTilde_sizeThree() {
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
    void testRejectTilde_notEmpty() {
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
    void testRejectTilde_sizeFourForDaughter() {
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
  class RejectTagFilter {

    @Test
    @DisplayName("testRejectTildeTag_Husband_sizeThree — removes homer only")
    void testRejectTildeTag_husband_sizeThree() {
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
    void testRejectTildeTag_wife_sizeFourForFather() {
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
    void testRejectTildeTag_wife_sizeThreeForMother() {
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
  class RejectTypeFilter {

    @Test
    @DisplayName("testRejectTildeType_Male_sizeThreeForFather — removes homer")
    void testRejectTildeType_male_sizeThreeForFather() {
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
    void testRejectTildeType_female_sizeFourForFather() {
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
    void testRejectTildeType_female_sizeThreeForMother() {
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
  class RejectTypeAndTagFilter {

    @Test
    @DisplayName("testRejectTildeTypeMaleTagHusband_sizeThreeForFather")
    void testRejectTildeTypeMaleTagHusband_sizeThreeForFather() {
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
    void testRejectTildeTypeFemaleTagHusband_sizeFourForFather() {
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
    void testRejectTildeTypeMaleTagWife_sizeFourForFather() {
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
  class ExistsNoFilter {

    @Test
    @DisplayName("testExistsTilde_trueForFather — father has a correspondence")
    void testExistsTilde_trueForFather() {
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
    void testExistsTilde_falseForDaughter() {
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
  class ExistsTagFilter {

    @Test
    @DisplayName("testExistsTildeTag_Husband_trueForFather")
    void testExistsTildeTag_husband_trueForFather() {
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
    void testExistsTildeTag_wife_falseForFather() {
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
    void testExistsTildeTag_son_trueForSon() {
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
    void testExistsTildeTag_nonExistent_false() {
      assertSatisfied(
          eval(
              """
              context family::Member inv:
                not persons::Person.allInstances().exists(~, Tag = "NoSuchTag")
              """));
    }

    @Test
    @DisplayName("testExistsTildeTag_Wife_trueForMother")
    void testExistsTildeTag_wife_trueForMother() {
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
  class ExistsTypeFilter {

    @Test
    @DisplayName("testExistsTildeType_Male_trueForFather — homer is Male")
    void testExistsTildeType_male_trueForFather() {
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
    void testExistsTildeType_female_falseForFather() {
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
    void testExistsTildeType_female_trueForMother() {
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
    void testExistsTildeType_male_falseForMother() {
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
  class ExistsTypeAndTagFilter {

    @Test
    @DisplayName("testExistsTildeTypeMaleTagHusband_trueForFather")
    void testExistsTildeTypeMaleTagHusband_trueForFather() {
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
    void testExistsTildeTypeFemaleTagHusband_false() {
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
    void testExistsTildeTypeMaleTagWife_false() {
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
    void testExistsTildeTypeFemaleTagWife_trueForMother() {
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
  class SelectChainingTests {

    @Test
    @DisplayName("testSelectTilde_thenNotEmpty")
    void testSelectTilde_thenNotEmpty() {
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
    void testSelectTilde_thenIsEmpty_false() {
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
    void testSelectTilde_thenSize() {
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
    void testSelectTilde_thenExists() {
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
    void testSelectTilde_thenForAll() {
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
    void testSelectTildeTag_thenSize() {
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
    void testSelectTildeType_thenNotEmpty() {
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
  class RejectChainingTests {

    @Test
    @DisplayName("testRejectTilde_thenNotEmpty")
    void testRejectTilde_thenNotEmpty() {
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
    void testRejectTilde_thenSize() {
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
    void testRejectTilde_thenIsEmpty_false() {
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
    void testRejectTildeTag_thenSize() {
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
  class ExistsLogicalChainingTests {

    @Test
    @DisplayName("testExistsTilde_andTrue")
    void testExistsTilde_andTrue() {
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
    void testExistsTilde_andFalse_isAlwaysFalse() {
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
    void testExistsTilde_orFalse_staysTrue() {
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
    void testNotExistsTilde_falseForFather() {
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
    void testExistsTilde_implies_selectNotEmpty() {
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
    void testExistsTildeTag_and_selectTildeTagSize() {
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
    void testSelectTilde_thenCollect() {
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
  class CrossOperationConsistency {

    @Test
    @DisplayName("testSelectAndRejectSizeSumToTotal")
    void testSelectAndRejectSizeSumToTotal() {
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
    void testExistsEqualsSelectNotEmpty_whenMatchExists() {
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
    void testSelectTagSize_consistentWithExistsTag() {
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
    void testSelectAndRejectTagSumToTotal() {
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
  void debugStep1_grammarParses() {
    // Wenn das hier schon fehlschlägt → ANTLR nicht regeneriert
    VitruvOCLLexer lexer =
        new VitruvOCLLexer(
            CharStreams.fromString(
                """
                context family::Member inv:
                  persons::Person.allInstances().select(~).size() >= 0
                """));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    parser.contextDeclCS();
    System.out.println("Parse errors: " + parser.getNumberOfSyntaxErrors());
    assertEquals(0, parser.getNumberOfSyntaxErrors());
  }

  @Test
  void debugStep2_grammarParsesWithTag() {
    // Wenn DAS fehlschlägt aber Step 1 nicht → correspondenceFilterCS fehlt im generierten Parser
    VitruvOCLLexer lexer =
        new VitruvOCLLexer(
            CharStreams.fromString(
                """
                context family::Member inv:
                  persons::Person.allInstances().select(~, Tag = "Husband").size() >= 0
                """));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    parser.contextDeclCS();
    System.out.println("Parse errors: " + parser.getNumberOfSyntaxErrors());
    assertEquals(0, parser.getNumberOfSyntaxErrors());
  }

  @Test
  void debugStep3_trivialConstraint() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context family::Member inv: true", ECORES, MODELS);
    assertTrue(
        result.isSuccess(), "Trivial constraint should compile: " + result.getWarningsSummary());
    assertTrue(result.isSatisfied(), "Trivial 'true' constraint must be satisfied");
  }

  @Test
  void debugStep4_oldSelectTildeNoFilter() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context family::Member inv:
              persons::Person.allInstances().select(~).size() >= 0
            """,
            ECORES,
            MODELS);
    // select(~) with tilde is expected to fail at parse/type-check level, not produce a crash
    assertNotNull(result, "Result must not be null even for invalid select(~)");
  }

  @Test
  void debugStep_trivialTrue() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context family::Member inv: true", ECORES, MODELS);
    assertTrue(
        result.isSuccess(), "Trivial constraint must compile: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Trivial 'true' constraint must be satisfied");
  }

  // ================================================================
  // Tag type-error tests
  // ================================================================

  @Test
  @DisplayName("Tag = 3 (integer) reports tag-type error, not implies error")
  void testTagIntegerReportsTagTypeError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Tag = 3).size() == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: Tag = 3 is not a String");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("Tag") && errors.contains("String"),
        "Error should mention Tag and String, got: " + errors);
    assertFalse(
        errors.contains("implies"), "Should NOT produce an 'implies' error, got: " + errors);
  }

  @Test
  @DisplayName("Type = \"momma\" (string) reports type-filter error, not implies error")
  void testTypeStringReportsTypeFilterError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Type = "momma").size() == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: Type = \"momma\" is not a metaclass");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("metaclass") || errors.contains("Type filter"),
        "Error should mention metaclass or Type filter, got: " + errors);
    assertFalse(
        errors.contains("implies"), "Should NOT produce an 'implies' error, got: " + errors);
  }

  // ================================================================
  // Unknown filter option name tests (Tags, Types, etc.)
  // ================================================================

  @Test
  @DisplayName("Tags = '...' reports unknown-option error with 'Tag' suggestion, no implies error")
  void testTagsTypoReportsFilterOptionError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Tags = "Husband").size() == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: 'Tags' is not a valid filter option");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("Tags") && (errors.contains("Tag") || errors.contains("filter option")),
        "Error should mention 'Tags' and suggest 'Tag', got: " + errors);
    assertFalse(errors.contains("implies"), "Should NOT cascade to implies error, got: " + errors);
  }

  @Test
  @DisplayName("Types = T reports unknown-option error with 'Type' suggestion")
  void testTypesTypoReportsFilterOptionError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Types = persons::Person).size() == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: 'Types' is not a valid filter option");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("Types") && (errors.contains("Type") || errors.contains("filter option")),
        "Error should mention 'Types' and suggest 'Type', got: " + errors);
    assertFalse(errors.contains("implies"), "Should NOT cascade to implies error, got: " + errors);
  }

  // ================================================================
  // Extended invalid operator tests (++, +-+, etc.)
  // ================================================================

  @Test
  @DisplayName("++ reports invalid-operator error, no cascade to inv/implies")
  void testDoublePlusReportsError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~).size() ++ 1 == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: ++ is not a valid operator");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("++") || errors.contains("Invalid operator"),
        "Error should mention ++, got: " + errors);
    assertFalse(errors.contains("implies"), "Should NOT cascade to implies error, got: " + errors);
  }

  @Test
  @DisplayName("+-+ reports invalid-operator error, no cascade to inv/implies")
  void testPlusMinusPlusReportsError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~).size() +-+ 1 == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: +-+ is not a valid operator");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("+-+") || errors.contains("Invalid operator"),
        "Error should mention +-+, got: " + errors);
    assertFalse(errors.contains("implies"), "Should NOT cascade to implies error, got: " + errors);
  }

  @Test
  @DisplayName("selcft(~, ...) gives 'did you mean select?' not a cryptic ~ error")
  void testSelectTypo_selcft_suggests_select() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              persons::Person.allInstances().selcft(~, Tag = "Husband").isEmpty()
            """);
    assertFalse(result.isSuccess(), "Should fail: 'selcft' is unknown");
    String errors = result.toDetailedErrorString();
    // Must NOT produce the cryptic ANTLR '~' token error
    assertFalse(
        errors.contains("mismatched input"),
        "Should not show cryptic parser error, got: " + errors);
    assertTrue(
        errors.contains("did you mean") && errors.contains("select"),
        "Should suggest 'select' for 'selcft', got: " + errors);
  }

  @Test
  @DisplayName("rejct(~) gives 'did you mean reject?'")
  void testRejectTypo_rejct_suggests_reject() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              persons::Person.allInstances().rejct(~).isEmpty()
            """);
    assertFalse(result.isSuccess());
    String errors = result.toDetailedErrorString();
    assertFalse(
        errors.contains("mismatched input"),
        "Should not show cryptic parser error, got: " + errors);
    assertTrue(
        errors.contains("did you mean") && errors.contains("reject"),
        "Should suggest 'reject' for 'rejct', got: " + errors);
  }

  @Test
  @DisplayName("Type = persons::Person (valid metaclass) produces no error")
  void testTypeMetaclassIsValid() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Type = persons::Person).size() == 1
            """);
    assertTrue(
        result.isSuccess(),
        "Type = persons::Person should be valid, errors: " + result.getWarningsSummary());
  }

  // ================================================================
  // Invalid operator sequence tests
  // ================================================================

  @Test
  @DisplayName("<> reports invalid-operator error, not implies error")
  void testDiamondOperatorReportsError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Tag = "Husband").size() <> 1
            """);
    assertFalse(result.isSuccess(), "Should fail: <> is not a valid operator");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("<>"), "Error should mention the invalid operator <>, got: " + errors);
    assertFalse(
        errors.contains("implies"), "Should NOT produce an 'implies' error, got: " + errors);
  }

  @Test
  @DisplayName("+- reports invalid-operator error")
  void testPlusMinusOperatorReportsError() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~).size() +- 0 == 1
            """);
    assertFalse(result.isSuccess(), "Should fail: +- is not a valid operator");
    String errors = result.toDetailedErrorString();
    assertTrue(
        errors.contains("+-"), "Error should mention the invalid operator +-, got: " + errors);
  }

  @Test
  @DisplayName("Tag = \"Husband\" (string) is valid — no type error")
  void testTagStringIsValid() {
    ConstraintResult result =
        eval(
            """
            context family::Member inv:
              self.firstName == "Homer" implies
                persons::Person.allInstances().select(~, Tag = "Husband").size() == 1
            """);
    assertTrue(
        result.isSuccess(),
        "Tag = \"Husband\" should be valid, errors: " + result.getWarningsSummary());
  }
}
