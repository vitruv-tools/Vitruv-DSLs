package tools.vitruv.dsls.commonalities.names;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

@SuppressWarnings("all")
public final class EPackageURINameResolver {
  private EPackageURINameResolver() {
  }

  /**
   * Predicate deciding whether a part of a model URI looks like a version number
   */
  private static final Predicate<String> IS_MODEL_VERSION_PART = Pattern.compile("[0-9]+(\\.[0-9]+)*").asPredicate();

  /**
   * Constructs a suitable, friendly name out of a metamodel URI. Chooses the last
   * part of the URI most of the time. However, some URIs contain a version number at
   * the end, which will be skipped in favor of the next part from behind.
   */
  public static String getPackageName(final String ePackageURI) {
    String _xblockexpression = null;
    {
      final String[] uriParts = ePackageURI.split("/");
      int _length = uriParts.length;
      int i = (_length - 1);
      for (; ((i >= 0) && EPackageURINameResolver.IS_MODEL_VERSION_PART.test(uriParts[i])); i--) {
      }
      String _xifexpression = null;
      if ((i >= 0)) {
        _xifexpression = uriParts[i];
      } else {
        _xifexpression = ePackageURI;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  public static String getPackageName(final EPackage ePackage) {
    Resource _eResource = null;
    if (ePackage!=null) {
      _eResource=ePackage.eResource();
    }
    return EPackageURINameResolver.getPackageName(_eResource.getURI().toString());
  }
}
