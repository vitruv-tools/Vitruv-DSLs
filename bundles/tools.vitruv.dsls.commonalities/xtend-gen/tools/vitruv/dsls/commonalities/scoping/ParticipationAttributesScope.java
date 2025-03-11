package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@SuppressWarnings("all")
public class ParticipationAttributesScope implements IScope {
  @Inject
  private IEObjectDescriptionProvider descriptionProvider;

  private ParticipationClass participationClass;

  public ParticipationAttributesScope forParticipationClass(final ParticipationClass participationClass) {
    ParticipationAttributesScope _xblockexpression = null;
    {
      this.participationClass = Preconditions.<ParticipationClass>checkNotNull(participationClass);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }

  private void checkParticipationClassSet() {
    Preconditions.checkState((this.participationClass != null), "No participation class to get attributes from was set!");
  }

  private Iterable<Attribute> allAttributes() {
    this.checkParticipationClassSet();
    final Metaclass metaclass = this.participationClass.getSuperMetaclass();
    if ((metaclass == null)) {
      return Collections.<Attribute>unmodifiableList(CollectionLiterals.<Attribute>newArrayList());
    }
    return Iterables.<Attribute>filter(metaclass.getAllMembers(), Attribute.class);
  }

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    return IterableExtensions.<Attribute, IEObjectDescription>map(this.allAttributes(), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName qName) {
    final String memberName = QualifiedNameHelper.getMemberName(qName);
    if ((memberName == null)) {
      return Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList());
    }
    final Function1<Attribute, Boolean> _function = (Attribute it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, memberName));
    };
    return IterableExtensions.<Attribute, IEObjectDescription>map(IterableExtensions.<Attribute>filter(this.allAttributes(), _function), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    this.checkParticipationClassSet();
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
}
