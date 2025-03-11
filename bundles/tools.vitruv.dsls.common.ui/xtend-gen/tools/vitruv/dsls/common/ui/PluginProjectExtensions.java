package tools.vitruv.dsls.common.ui;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@Utility
@SuppressWarnings("all")
public final class PluginProjectExtensions {
  /**
   * Adds a dependency on the provided {@code requiredBundleName} if it is not required yet. The dependency is not
   * exported, not optional, and does not define any version constraints. Do not forget to call
   * {@link IBundleProjectDescription#apply} to actually apply the change!
   */
  public static void addRequiredBundle(final IBundleProjectDescription pluginProject, final String requiredBundleName) {
    final IRequiredBundleDescription[] currentlyRequiredBundles = pluginProject.getRequiredBundles();
    if (((currentlyRequiredBundles != null) && IterableExtensions.<IRequiredBundleDescription>exists(((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(currentlyRequiredBundles)), ((Function1<IRequiredBundleDescription, Boolean>) (IRequiredBundleDescription it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, requiredBundleName));
    })))) {
      return;
    }
    int _xifexpression = (int) 0;
    if ((currentlyRequiredBundles == null)) {
      _xifexpression = 0;
    } else {
      _xifexpression = currentlyRequiredBundles.length;
    }
    final int currentSize = _xifexpression;
    final IRequiredBundleDescription[] newRequiredBundles = new IRequiredBundleDescription[(currentSize + 1)];
    if ((currentSize > 0)) {
      System.arraycopy(currentlyRequiredBundles, 0, newRequiredBundles, 0, currentSize);
    }
    newRequiredBundles[currentSize] = 
      ProjectAccess.getProjectBundleService().newRequiredBundle(requiredBundleName, null, false, false);
    pluginProject.setRequiredBundles(newRequiredBundles);
  }

  public static boolean requiresBundleDirectly(final IBundleProjectDescription pluginProject, final String requiredBundleName) {
    boolean _xblockexpression = false;
    {
      final IRequiredBundleDescription[] requiredBundles = pluginProject.getRequiredBundles();
      boolean _xifexpression = false;
      if ((requiredBundles == null)) {
        _xifexpression = false;
      } else {
        final Function1<IRequiredBundleDescription, Boolean> _function = (IRequiredBundleDescription it) -> {
          String _name = it.getName();
          return Boolean.valueOf(Objects.equal(_name, requiredBundleName));
        };
        _xifexpression = IterableExtensions.<IRequiredBundleDescription>exists(((Iterable<IRequiredBundleDescription>)Conversions.doWrapArray(requiredBundles)), _function);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private PluginProjectExtensions() {
    
  }
}
