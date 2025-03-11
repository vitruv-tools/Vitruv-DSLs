package tools.vitruv.dsls.reactions.scoping;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

@Singleton
@SuppressWarnings("all")
public class ReactionsImportScopeHelper {
  @Inject
  private IResourceDescription.Manager descriptionManager;

  @Inject
  private IResourceDescriptionsProvider descriptionsProvider;

  @Inject
  private IContainer.Manager containerManager;

  public Iterable<IEObjectDescription> getVisibleReactionsSegmentDescriptions(final ReactionsSegment reactionsSegment) {
    if ((reactionsSegment == null)) {
      return Collections.<IEObjectDescription>emptyList();
    }
    final Resource resource = reactionsSegment.eResource();
    if ((resource == null)) {
      return Collections.<IEObjectDescription>emptyList();
    }
    final IResourceDescription resourceDesc = this.descriptionManager.getResourceDescription(resource);
    final IResourceDescriptions resourceDescriptions = this.descriptionsProvider.getResourceDescriptions(resource.getResourceSet());
    final List<IContainer> visibleContainers = this.containerManager.getVisibleContainers(resourceDesc, resourceDescriptions);
    final Function1<IContainer, Iterable<IResourceDescription>> _function = (IContainer it) -> {
      return it.getResourceDescriptions();
    };
    final Function1<IResourceDescription, Boolean> _function_1 = (IResourceDescription it) -> {
      final String lastURISegment = it.getURI().lastSegment();
      return Boolean.valueOf(((lastURISegment != null) && lastURISegment.endsWith(".reactions")));
    };
    final Iterable<IResourceDescription> visibleReactionsResources = IterableExtensions.<IResourceDescription>filter(Iterables.<IResourceDescription>concat(ListExtensions.<IContainer, Iterable<IResourceDescription>>map(visibleContainers, _function)), _function_1);
    final URI reactionsSegmentURI = EcoreUtil.getURI(reactionsSegment);
    final Function1<IResourceDescription, Iterable<IEObjectDescription>> _function_2 = (IResourceDescription it) -> {
      return it.getExportedObjectsByType(TopLevelElementsPackage.eINSTANCE.getReactionsSegment());
    };
    final Function1<IEObjectDescription, Boolean> _function_3 = (IEObjectDescription it) -> {
      boolean _equals = it.getEObjectURI().equals(reactionsSegmentURI);
      return Boolean.valueOf((!_equals));
    };
    final Iterable<IEObjectDescription> visibleReactionsSegmentDescriptions = IterableExtensions.<IEObjectDescription>filter(Iterables.<IEObjectDescription>concat(IterableExtensions.<IResourceDescription, Iterable<IEObjectDescription>>map(visibleReactionsResources, _function_2)), _function_3);
    return visibleReactionsSegmentDescriptions;
  }
}
