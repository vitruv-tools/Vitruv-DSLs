package tools.vitruv.dsls.commonalities.names;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.xbase.scoping.XbaseQualifiedNameProvider;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.MemberLike;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;

@Singleton
@SuppressWarnings("all")
public class CommonalitiesLanguageQualifiedNameProvider extends XbaseQualifiedNameProvider {
  @Override
  public QualifiedName getFullyQualifiedName(final EObject object) {
    if ((object instanceof JvmIdentifiableElement)) {
      return super.getFullyQualifiedName(((JvmIdentifiableElement)object));
    }
    final List<String> segments = this.getFullyQualifiedNameSegments(object);
    if ((segments != null)) {
      return QualifiedName.create(segments);
    }
    return null;
  }

  private List<String> _getFullyQualifiedNameSegments(final MemberLike member) {
    return this.segmentList(this.getFullyQualifiedNameSegments(member.getClassLikeContainer()), member.getName());
  }

  private List<String> _getFullyQualifiedNameSegments(final ClassLike classl) {
    return this.segmentList(this.getFullyQualifiedNameSegments(classl.getPackageLikeContainer()), classl.getName());
  }

  private List<String> _getFullyQualifiedNameSegments(final PackageLike packagel) {
    return this.segmentList(packagel.getName());
  }

  private List<String> _getFullyQualifiedNameSegments(final EObject object) {
    return null;
  }

  private List<String> _getFullyQualifiedNameSegments(final Void nill) {
    return null;
  }

  private List<String> segmentList(final List<String> existing, final String element) {
    if ((element == null)) {
      return null;
    }
    existing.add(element);
    return existing;
  }

  private ArrayList<String> segmentList(final String packageLikeName) {
    if ((packageLikeName == null)) {
      return null;
    }
    final ArrayList<String> result = new ArrayList<String>(4);
    result.add(packageLikeName);
    result.add(QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR_SEGMENT);
    return result;
  }

  private List<String> getFullyQualifiedNameSegments(final EObject classl) {
    if (classl instanceof ClassLike) {
      return _getFullyQualifiedNameSegments((ClassLike)classl);
    } else if (classl instanceof MemberLike) {
      return _getFullyQualifiedNameSegments((MemberLike)classl);
    } else if (classl instanceof PackageLike) {
      return _getFullyQualifiedNameSegments((PackageLike)classl);
    } else if (classl != null) {
      return _getFullyQualifiedNameSegments(classl);
    } else {
      return _getFullyQualifiedNameSegments((Void)null);
    }
  }
}
