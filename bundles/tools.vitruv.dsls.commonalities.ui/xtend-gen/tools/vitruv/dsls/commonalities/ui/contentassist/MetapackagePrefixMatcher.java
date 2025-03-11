package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import tools.vitruv.dsls.commonalities.names.EPackageURINameResolver;

@SuppressWarnings("all")
public class MetapackagePrefixMatcher extends PrefixMatcher {
  private String name;

  @Inject
  private PrefixMatcher defaultMatcher;

  @Override
  public boolean isCandidateMatchingPrefix(final String name, final String prefix) {
    return this.defaultMatcher.isCandidateMatchingPrefix(this.name, prefix);
  }

  public String setEPackage(final EPackage ePackage) {
    return this.name = EPackageURINameResolver.getPackageName(ePackage);
  }
}
