package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.commonalities.runtime.helper.IntermediateModelHelper;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

@Utility
@SuppressWarnings("all")
public final class AttributeReferenceHelper {
  public static <I extends Intermediate> Iterable<I> getPotentiallyContainedIntermediates(final IReferenceMappingOperator operator, final EObject containerObject, final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel, final Class<I> intermediateType) {
    final Iterable<? extends EObject> containedObjects = operator.getContainedObjects(containerObject);
    final Function1<EObject, I> _function = (EObject it) -> {
      return IntermediateModelHelper.<I>getCorrespondingIntermediate(correspondenceModel, it, intermediateType);
    };
    return IterableExtensions.<I>toSet(IterableExtensions.<I>filterNull(IterableExtensions.map(containedObjects, _function)));
  }

  public static <I extends Intermediate> I getPotentialContainerIntermediate(final IReferenceMappingOperator operator, final EObject containedObject, final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel, final Class<I> intermediateType) {
    final EObject containerObject = operator.getContainer(containedObject);
    if ((containerObject == null)) {
      return null;
    }
    return IntermediateModelHelper.<I>getCorrespondingIntermediate(correspondenceModel, containerObject, intermediateType);
  }

  private AttributeReferenceHelper() {
    
  }
}
