package tools.vitruv.dsls.reactions.runtime.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import allElementTypes.AllElementTypesPackage;
import allElementTypes.Identified;
import allElementTypes.NonRoot;
import allElementTypes.Root;
import allElementTypes.impl.RootImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Maps generated EMF classes and accessor methods to EClasses and features. */
class EmfModelIndexTest {
  private static final AllElementTypesPackage MM = AllElementTypesPackage.eINSTANCE;
  private final EmfModelIndex index = new EmfModelIndex();

  @BeforeAll
  static void registerMetamodel() {
    MM.eClass(); // registers the package
  }

  @Test
  void generatedInterfaceAndImplementationMapToTheirEClass() {
    assertEquals(MM.getRoot(), index.getEClass(Root.class));
    assertEquals(MM.getRoot(), index.getEClass(RootImpl.class));
    assertNull(index.getEClass(String.class));
  }

  @Test
  void accessorsMapToFeatures() {
    var getter = EmfModelIndex.findAccessor(MM.getRoot(), "getSingleValuedEAttribute", 0);
    assertEquals(MM.getRoot_SingleValuedEAttribute(), getter.feature());
    assertEquals(EmfModelIndex.AccessorKind.GET, getter.kind());
    var setter = EmfModelIndex.findAccessor(MM.getRoot(), "setSingleValuedEAttribute", 1);
    assertEquals(MM.getRoot_SingleValuedEAttribute(), setter.feature());
    assertEquals(EmfModelIndex.AccessorKind.SET, setter.kind());
    var inherited = EmfModelIndex.findAccessor(MM.getRoot(), "getId", 0);
    assertEquals(MM.getIdentified_Id(), inherited.feature());
    assertNull(EmfModelIndex.findAccessor(MM.getRoot(), "toString", 0));
  }

  @Test
  void classHierarchy() {
    EmfModelIndexTestSupport.assertContains(
        index.withSubclasses(MM.getIdentified()), MM.getRoot());
    assertTrue(index.overlap(MM.getIdentified(), MM.getRoot()));
    assertTrue(index.overlap(MM.getRoot(), MM.getRoot()));
    assertFalse(index.overlap(MM.getRoot(), MM.getNonRoot()));
    assertTrue(index.implementing(Identified.class).contains(MM.getNonRoot()));
    assertFalse(index.implementing(NonRoot.class).contains(MM.getRoot()));
  }
}
