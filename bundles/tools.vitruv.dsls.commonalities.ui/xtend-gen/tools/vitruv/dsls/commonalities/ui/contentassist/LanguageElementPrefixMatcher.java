package tools.vitruv.dsls.commonalities.ui.contentassist;

import com.google.inject.Inject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;

@SuppressWarnings("all")
abstract class LanguageElementPrefixMatcher extends PrefixMatcher {
  @Inject
  private PrefixMatcher.IgnoreCase ignoreCase;

  protected static String segment(final QualifiedName name, final int index) {
    String _xifexpression = null;
    int _segmentCount = name.getSegmentCount();
    boolean _greaterThan = (_segmentCount > index);
    if (_greaterThan) {
      _xifexpression = name.getSegment(index);
    } else {
      _xifexpression = "";
    }
    return _xifexpression;
  }

  protected boolean matchesAnySegmentStartIgnoringCase(final String prefix, final QualifiedName qualifiedName, final int... segmentIndices) {
    for (final int index : segmentIndices) {
      boolean _isCandidateMatchingPrefix = this.ignoreCase.isCandidateMatchingPrefix(LanguageElementPrefixMatcher.segment(qualifiedName, index), prefix);
      if (_isCandidateMatchingPrefix) {
        return true;
      }
    }
    return false;
  }

  protected boolean matchesQualifiedNamePart(final QualifiedName prefix, final QualifiedName qualifiedNamePart, final int... segmentIndices) {
    for (final int index : segmentIndices) {
      {
        final String perfixPart = prefix.getSegment(index);
        final String namePart = prefix.getSegment(index);
        if (((!perfixPart.equalsIgnoreCase(namePart)) && (namePart.length() != 0))) {
          return false;
        }
      }
    }
    return true;
  }

  protected boolean matchesStartIgnoringCase(final QualifiedName prefix, final QualifiedName name, final int index) {
    return this.ignoreCase.isCandidateMatchingPrefix(name.getSegment(index), prefix.getSegment(index));
  }
}
