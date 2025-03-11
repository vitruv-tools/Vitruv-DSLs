package tools.vitruv.dsls.commonalities.names;

import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;

@SuppressWarnings("all")
public class QualifiedNameProviderDescriptionProvider implements IEObjectDescriptionProvider {
  @Inject
  private IQualifiedNameProvider qualifiedNameProvider;

  @Override
  public IEObjectDescription describe(final EObject object) {
    return EObjectDescription.create(this.qualifiedNameProvider.getFullyQualifiedName(object), object);
  }
}
