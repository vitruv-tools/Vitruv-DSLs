package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.xbase.lib.Extension;

@Singleton
@SuppressWarnings("all")
class MetaclassPrefixMatcher extends LanguageElementPrefixMatcher {
  @Inject
  @Extension
  private IQualifiedNameConverter qualifiedNameConverter;

  @Override
  public boolean isCandidateMatchingPrefix(final String name, final String prefix) {
    return this.isCandidateMatchingPrefix(this.qualifiedNameConverter.toQualifiedName(name), this.qualifiedNameConverter.toQualifiedName(prefix));
  }

  boolean isCandidateMatchingPrefix(final QualifiedName name, final QualifiedName prefix) {
    int _segmentCount = prefix.getSegmentCount();
    boolean _greaterThan = (_segmentCount > 1);
    if (_greaterThan) {
      return (this.matchesQualifiedNamePart(prefix, name, 0) && this.matchesStartIgnoringCase(prefix, name, 1));
    }
    return this.matchesAnySegmentStartIgnoringCase(prefix.getSegment(0), name, 0, 1);
  }
}
