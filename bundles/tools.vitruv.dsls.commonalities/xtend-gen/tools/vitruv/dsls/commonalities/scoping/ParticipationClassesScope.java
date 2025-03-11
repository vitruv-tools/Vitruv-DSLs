package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class ParticipationClassesScope implements IScope {
  public static class Factory extends InjectingFactoryBase {
    public ParticipationClassesScope forCommonality(final Commonality commonality) {
      Commonality _checkNotNull = Preconditions.<Commonality>checkNotNull(commonality);
      return this.<ParticipationClassesScope>injectMembers(new ParticipationClassesScope(_checkNotNull));
    }
  }

  @Inject
  private IEObjectDescriptionProvider descriptionProvider;

  private final Commonality commonality;

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    final Function1<Participation, Iterable<ParticipationClass>> _function = (Participation it) -> {
      return CommonalitiesLanguageModelExtensions.getAllClasses(it);
    };
    return IterableExtensions.<ParticipationClass, IEObjectDescription>map(IterableExtensions.<Participation, ParticipationClass>flatMap(this.commonality.getParticipations(), _function), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName qName) {
    final String metamodelName = QualifiedNameHelper.getMetamodelName(qName);
    if ((metamodelName == null)) {
      return CollectionLiterals.<IEObjectDescription>emptyList();
    }
    final String className = QualifiedNameHelper.getClassName(qName);
    if ((className == null)) {
      return CollectionLiterals.<IEObjectDescription>emptyList();
    }
    final Function1<Participation, Boolean> _function = (Participation it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, metamodelName));
    };
    final Function1<Participation, Iterable<ParticipationClass>> _function_1 = (Participation it) -> {
      return CommonalitiesLanguageModelExtensions.getAllClasses(it);
    };
    final Function1<ParticipationClass, Boolean> _function_2 = (ParticipationClass it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, className));
    };
    return IterableExtensions.<ParticipationClass, IEObjectDescription>map(IterableExtensions.<ParticipationClass>filter(IterableExtensions.<Participation, ParticipationClass>flatMap(IterableExtensions.<Participation>filter(this.commonality.getParticipations(), _function), _function_1), _function_2), this.descriptionProvider);
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

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("all participations classes of ‹");
    _builder.append(this.commonality);
    _builder.append("›");
    return _builder.toString();
  }

  public ParticipationClassesScope(final Commonality commonality) {
    super();
    this.commonality = commonality;
  }
}
