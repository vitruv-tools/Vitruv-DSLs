package tools.vitruv.dsls.commonalities.names;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.naming.QualifiedName;

@Utility
@SuppressWarnings("all")
public final class QualifiedNameHelper {
  public static final String METAMODEL_METACLASS_SEPARATOR = ":";

  public static final String METACLASS_ATTRIBUTE_SEPARATOR = ".";

  public static final String METAMODEL_METACLASS_SEPARATOR_SEGMENT = ":";

  public static QualifiedName getQualifiedDomainName(final String domainName) {
    return QualifiedName.create(domainName, QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR_SEGMENT);
  }

  public static boolean hasMetamodelName(final QualifiedName name) {
    return ((name.getSegmentCount() > 1) && Objects.equal(name.getSegment(1), QualifiedNameHelper.METAMODEL_METACLASS_SEPARATOR_SEGMENT));
  }

  public static String getMetamodelName(final QualifiedName name) {
    String _xifexpression = null;
    boolean _hasMetamodelName = QualifiedNameHelper.hasMetamodelName(name);
    if (_hasMetamodelName) {
      _xifexpression = name.getSegment(0);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static String getClassName(final QualifiedName name) {
    String _xifexpression = null;
    if (((name.getSegmentCount() > 2) && QualifiedNameHelper.hasMetamodelName(name))) {
      _xifexpression = name.getSegment(2);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static String getMemberName(final QualifiedName name) {
    String _xifexpression = null;
    if (((name.getSegmentCount() > 3) && QualifiedNameHelper.hasMetamodelName(name))) {
      _xifexpression = name.getSegment(3);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  private QualifiedNameHelper() {
    
  }
}
