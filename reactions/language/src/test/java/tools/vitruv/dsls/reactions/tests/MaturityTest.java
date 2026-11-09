package tools.vitruv.dsls.reactions.tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.vitruv.dsls.reactions.language.toplevelelements.MaturityLevelEnum;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

public class MaturityTest {

  @Test
  void testMaturity() {
    var mmodel = TopLevelElementsFactory.eINSTANCE.createMaturityModel();
    var finall = TopLevelElementsFactory.eINSTANCE.createMaturityLevel();
    finall.setDescription("The highest maturity level.");
    finall.setLevel(MaturityLevelEnum.FINAL);
    var reviewed = TopLevelElementsFactory.eINSTANCE.createMaturityLevel();
    reviewed.setDescription("The highest maturity level.");
    reviewed.setLevel(MaturityLevelEnum.REVIEWED);
    var draft = TopLevelElementsFactory.eINSTANCE.createMaturityLevel();
    draft.setDescription("The highest maturity level.");
    draft.setLevel(MaturityLevelEnum.FINAL);
    draft.setNext(reviewed);
    reviewed.setNext(finall);
    mmodel.getLevels().addAll(List.of(draft, reviewed, finall));
    var trigger = TopLevelElementsFactory.eINSTANCE.createTrigger();
    trigger.setLevel(draft);
    assertEquals(draft, trigger.getLevel());
  }
  
}
