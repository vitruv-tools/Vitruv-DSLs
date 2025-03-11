package tools.vitruv.dsls.commonalities.scoping;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.AliasedEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@FinalFieldsConstructor
@SuppressWarnings("all")
public abstract class NameTransformingScope implements IScope {
  protected final IScope delegate;

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    final Function1<IEObjectDescription, IEObjectDescription> _function = (IEObjectDescription it) -> {
      return this.transformResult(it);
    };
    return IterableExtensions.<IEObjectDescription, IEObjectDescription>map(this.delegate.getAllElements(), _function);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
    final Function1<IEObjectDescription, IEObjectDescription> _function = (IEObjectDescription it) -> {
      return this.transformResult(it);
    };
    return IterableExtensions.<IEObjectDescription, IEObjectDescription>map(this.delegate.getElements(this.transformQuery(name)), _function);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    final Function1<IEObjectDescription, IEObjectDescription> _function = (IEObjectDescription it) -> {
      return this.transformResult(it);
    };
    return IterableExtensions.<IEObjectDescription, IEObjectDescription>map(this.delegate.getElements(object), _function);
  }

  @Override
  public IEObjectDescription getSingleElement(final QualifiedName name) {
    return this.transformResult(this.delegate.getSingleElement(this.transformQuery(name)));
  }

  @Override
  public IEObjectDescription getSingleElement(final EObject object) {
    return this.transformResult(this.delegate.getSingleElement(object));
  }

  protected abstract QualifiedName transformQuery(final QualifiedName name);

  protected abstract QualifiedName transformResult(final QualifiedName name);

  private IEObjectDescription transformResult(final IEObjectDescription description) {
    if ((description == null)) {
      return null;
    }
    final QualifiedName transformedName = this.transformResult(description.getName());
    return new AliasedEObjectDescription(transformedName, description);
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("transforming ");
    _builder.append(this.delegate);
    return _builder.toString();
  }

  public NameTransformingScope(final IScope delegate) {
    super();
    this.delegate = delegate;
  }
}
