package tools.vitruv.dsls.commonalities.names;

import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

@SuppressWarnings("all")
public interface IEObjectDescriptionProvider extends Function<EObject, IEObjectDescription>, Function1<EObject, IEObjectDescription> {
  IEObjectDescription describe(final EObject object);

  @Override
  default IEObjectDescription apply(final EObject object) {
    return this.describe(object);
  }
}
