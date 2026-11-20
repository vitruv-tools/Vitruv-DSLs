package mir.reactions.locking.sample;

import allElementTypes.AllElementTypesFactory;
import allElementTypes.AllElementTypesPackage;
import allElementTypes.NonRoot;
import allElementTypes.Root;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Stands in for a generated reaction class, in a {@code mir} package like those (calls into
 * {@code tools.vitruv} packages count as framework API): the analysis starts at
 * {@code executeReaction} and follows everything it calls (helper methods, lambdas).
 */
public final class SampleReaction {
  private SampleReaction() {
  }

  public static void executeReaction(Root root, NonRoot obsolete) {
    // read one attribute, write another
    root.setSingleValuedEAttribute(root.getSingleValuedPrimitiveTypeEAttribute());
    addChild(root);
    // a lambda: its body is analyzed too
    Consumer<Root> rename = r -> r.setId("renamed");
    rename.accept(root);
    EcoreUtil.delete(obsolete);
    // reflective access: any feature of the class
    root.eGet(AllElementTypesPackage.Literals.ROOT__RECURSIVE_ROOT);
  }

  private static void addChild(Root root) {
    NonRoot child = AllElementTypesFactory.eINSTANCE.createNonRoot();
    // mutation of a list returned by a many-valued getter: a write of that feature
    root.getMultiValuedContainmentEReference().add(child);
  }
}
