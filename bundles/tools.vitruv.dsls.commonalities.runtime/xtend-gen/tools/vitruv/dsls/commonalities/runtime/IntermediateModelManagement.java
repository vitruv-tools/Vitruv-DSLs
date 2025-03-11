package tools.vitruv.dsls.commonalities.runtime;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.util.function.Predicate;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root;

@SuppressWarnings("all")
public class IntermediateModelManagement {
  private IntermediateModelManagement() {
  }

  public static void claimIntermediateId(final Intermediate intermediate) {
    intermediate.setIntermediateId(EcoreUtil.generateUUID());
  }

  public static void addIntermediate(final Resource targetResource, final Intermediate intermediate) {
    final Root root = IntermediateModelManagement.getOrCreateRootIn(targetResource, intermediate.eClass().getEPackage());
    EList<Intermediate> _intermediates = root.getIntermediates();
    _intermediates.add(intermediate);
  }

  public static void addResourceBridge(final Resource targetResource, final tools.vitruv.dsls.commonalities.runtime.resources.Resource intermediateResource, final Intermediate intermediate) {
    final Root root = IntermediateModelManagement.getOrCreateRootIn(targetResource, intermediate.eClass().getEPackage());
    EList<tools.vitruv.dsls.commonalities.runtime.resources.Resource> _resourceBridges = root.getResourceBridges();
    _resourceBridges.add(intermediateResource);
  }

  public static Root getOrCreateRootIn(final Resource targetResource, final EPackage ePackage) {
    synchronized (targetResource) {
      boolean _isEmpty = targetResource.getContents().isEmpty();
      if (_isEmpty) {
        final Function1<EClass, Boolean> _function = (EClass it) -> {
          final Predicate<EClass> _function_1 = (EClass it_1) -> {
            EClass _root = IntermediateModelBasePackage.eINSTANCE.getRoot();
            return Objects.equal(it_1, _root);
          };
          return Boolean.valueOf(IterableUtil.<EClass>containsAny(it.getESuperTypes(), _function_1));
        };
        final EClass rootClass = IterableExtensions.<EClass>findFirst(Iterables.<EClass>filter(ePackage.getEClassifiers(), EClass.class), _function);
        EObject _create = ePackage.getEFactoryInstance().create(rootClass);
        final Root root = ((Root) _create);
        EList<Adapter> _eAdapters = root.eAdapters();
        _eAdapters.add(IntermediateModelRootDisposer.INSTANCE);
        EList<EObject> _contents = targetResource.getContents();
        _contents.add(root);
        targetResource.setModified(true);
        return root;
      }
      EObject _get = targetResource.getContents().get(0);
      return ((Root) _get);
    }
  }
}
