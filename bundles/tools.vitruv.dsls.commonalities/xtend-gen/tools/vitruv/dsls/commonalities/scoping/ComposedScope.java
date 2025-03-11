package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class ComposedScope implements IScope {
  private List<IScope> delegates;

  public ComposedScope(final IScope... delegates) {
    this.delegates = ((List<IScope>)Conversions.doWrapArray(delegates));
  }

  public boolean operator_plus(final IScope delegate) {
    return this.delegates.add(delegate);
  }

  public boolean operator_plus(final IScope... delegate) {
    return Iterables.<IScope>addAll(this.delegates, ((Iterable<? extends IScope>)Conversions.doWrapArray(delegate)));
  }

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    final Function1<IScope, Iterable<IEObjectDescription>> _function = (IScope it) -> {
      return it.getAllElements();
    };
    return IterableExtensions.<IScope, IEObjectDescription>flatMap(this.delegates, _function);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
    final Function1<IScope, Iterable<IEObjectDescription>> _function = (IScope it) -> {
      return it.getElements(name);
    };
    return IterableExtensions.<IScope, IEObjectDescription>flatMap(this.delegates, _function);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    final Function1<IScope, Iterable<IEObjectDescription>> _function = (IScope it) -> {
      return it.getElements(object);
    };
    return IterableExtensions.<IScope, IEObjectDescription>flatMap(this.delegates, _function);
  }

  @Override
  public IEObjectDescription getSingleElement(final QualifiedName name) {
    final Function1<IScope, IEObjectDescription> _function = (IScope it) -> {
      return it.getSingleElement(name);
    };
    final Function1<IEObjectDescription, Boolean> _function_1 = (IEObjectDescription it) -> {
      return Boolean.valueOf((it != null));
    };
    return IterableExtensions.<IEObjectDescription>findFirst(ListExtensions.<IScope, IEObjectDescription>map(this.delegates, _function), _function_1);
  }

  @Override
  public IEObjectDescription getSingleElement(final EObject object) {
    final Function1<IScope, IEObjectDescription> _function = (IScope it) -> {
      return it.getSingleElement(object);
    };
    final Function1<IEObjectDescription, Boolean> _function_1 = (IEObjectDescription it) -> {
      return Boolean.valueOf((it != null));
    };
    return IterableExtensions.<IEObjectDescription>findFirst(ListExtensions.<IScope, IEObjectDescription>map(this.delegates, _function), _function_1);
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[");
    _builder.newLine();
    {
      for(final IScope delegate : this.delegates) {
        _builder.append("\t");
        _builder.append("-> ");
        _builder.append(delegate, "\t");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("]");
    _builder.newLine();
    return _builder.toString();
  }
}
