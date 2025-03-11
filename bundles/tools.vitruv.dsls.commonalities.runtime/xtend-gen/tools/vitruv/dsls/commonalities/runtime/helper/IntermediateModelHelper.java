package tools.vitruv.dsls.commonalities.runtime.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;

@Utility
@SuppressWarnings("all")
public final class IntermediateModelHelper {
  public static List<String> getMetadataModelKey(final String conceptDomainName) {
    String _firstLower = StringExtensions.toFirstLower(conceptDomainName);
    String _plus = ((conceptDomainName + ".") + _firstLower);
    return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("commonalities", _plus));
  }

  public static Intermediate getCorrespondingIntermediate(final EditableCorrespondenceModelView<?> correspondenceModel, final EObject object) {
    return IntermediateModelHelper.<Intermediate>getCorrespondingIntermediate(correspondenceModel, object, Intermediate.class);
  }

  public static <I extends Intermediate> I getCorrespondingIntermediate(final EditableCorrespondenceModelView<?> correspondenceModel, final EObject object, final Class<I> intermediateType) {
    return IterableExtensions.<I>head(Iterables.<I>filter(correspondenceModel.getCorrespondingEObjects(object), intermediateType));
  }

  public static IntermediateResourceBridge getCorrespondingResourceBridge(final EditableCorrespondenceModelView<?> correspondenceModel, final EObject object) {
    Preconditions.checkArgument((!(object instanceof Intermediate)), "object cannot be of type Intermediate");
    return IterableExtensions.<IntermediateResourceBridge>head(Iterables.<IntermediateResourceBridge>filter(correspondenceModel.getCorrespondingEObjects(object), IntermediateResourceBridge.class));
  }

  private IntermediateModelHelper() {
    
  }
}
