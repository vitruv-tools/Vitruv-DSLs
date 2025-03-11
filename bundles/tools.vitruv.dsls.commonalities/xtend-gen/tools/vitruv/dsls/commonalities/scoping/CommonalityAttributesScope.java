package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class CommonalityAttributesScope implements IScope {
  public static class Factory extends InjectingFactoryBase {
    public CommonalityAttributesScope forCommonality(final Commonality commonality) {
      Commonality _checkNotNull = Preconditions.<Commonality>checkNotNull(commonality);
      return this.<CommonalityAttributesScope>injectMembers(new CommonalityAttributesScope(_checkNotNull));
    }
  }

  @Inject
  private IEObjectDescriptionProvider descriptionProvider;

  private final Commonality commonality;

  private EList<CommonalityAttribute> allAttributes() {
    return this.commonality.getAttributes();
  }

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    return ListExtensions.<CommonalityAttribute, IEObjectDescription>map(this.allAttributes(), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName qName) {
    final String memberName = QualifiedNameHelper.getMemberName(qName);
    Iterable<IEObjectDescription> _xifexpression = null;
    if ((memberName != null)) {
      final Function1<CommonalityAttribute, Boolean> _function = (CommonalityAttribute it) -> {
        String _name = it.getName();
        return Boolean.valueOf(Objects.equal(_name, memberName));
      };
      _xifexpression = IterableExtensions.<CommonalityAttribute, IEObjectDescription>map(IterableExtensions.<CommonalityAttribute>filter(this.allAttributes(), _function), this.descriptionProvider);
    } else {
      _xifexpression = CollectionLiterals.<IEObjectDescription>emptyList();
    }
    return _xifexpression;
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    final URI objectURI = EcoreUtil2.getURI(object);
    final Function1<IEObjectDescription, Boolean> _function = (IEObjectDescription it) -> {
      return Boolean.valueOf(((it.getEObjectOrProxy() == object) || Objects.equal(it.getEObjectURI(), objectURI)));
    };
    return IterableExtensions.<IEObjectDescription>filter(this.getAllElements(), _function);
  }

  @Override
  public IEObjectDescription getSingleElement(final QualifiedName name) {
    return IterableExtensions.<IEObjectDescription>head(this.getElements(name));
  }

  @Override
  public IEObjectDescription getSingleElement(final EObject object) {
    return IterableExtensions.<IEObjectDescription>head(this.getElements(object));
  }

  public CommonalityAttributesScope(final Commonality commonality) {
    super();
    this.commonality = commonality;
  }
}
