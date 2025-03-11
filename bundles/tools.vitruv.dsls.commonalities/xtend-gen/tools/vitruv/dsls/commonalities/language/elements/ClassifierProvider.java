package tools.vitruv.dsls.commonalities.language.elements;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class ClassifierProvider {
  private static final URI CONTAINER_RESOURCE_URI = URI.createURI("synthetic:/commonalities/ecoreAdapters");

  public static final ClassifierProvider INSTANCE = new ClassifierProvider();

  /**
   * In order to be referenced from a Xtext language, EObjects must be
   * contained in a resource. So we create a fake resource to put our EClass
   * and EDataType adapters in. This resource is never serialized and has no
   * other purpose.
   */
  private final Resource container = new ResourceSetImpl().createResource(ClassifierProvider.CONTAINER_RESOURCE_URI);

  private final Map<Domain, Map<EClass, Metaclass>> metaclasses = new HashMap<Domain, Map<EClass, Metaclass>>();

  private final Map<EDataType, EDataTypeClassifier> dataTypes = new HashMap<EDataType, EDataTypeClassifier>();

  private ClassifierProvider() {
  }

  protected Classifier _toClassifier(final EDataType eDataType, final Domain containingDomain) {
    return this.toDataTypeAdapter(eDataType);
  }

  protected Classifier _toClassifier(final EClass eClass, final Domain containingDomain) {
    return this.toMetaclass(eClass, containingDomain);
  }

  public Metaclass toMetaclass(final EClass eClass, final Domain containingDomain) {
    final Function<Domain, Map<EClass, Metaclass>> _function = (Domain it) -> {
      return new HashMap<EClass, Metaclass>();
    };
    final Function<EClass, Metaclass> _function_1 = (EClass it) -> {
      final EClassMetaclass adapter = LanguageElementsFactory.eINSTANCE.createEClassMetaclass().withClassifierProvider(this).forEClass(eClass).fromDomain(containingDomain);
      EList<EObject> _contents = this.container.getContents();
      _contents.add(adapter);
      return adapter;
    };
    return this.metaclasses.computeIfAbsent(containingDomain, _function).computeIfAbsent(eClass, _function_1);
  }

  public EDataTypeClassifier toDataTypeAdapter(final EDataType eDataType) {
    final Function<EDataType, EDataTypeClassifier> _function = (EDataType it) -> {
      final EDataTypeClassifier adapter = LanguageElementsFactory.eINSTANCE.createEDataTypeClassifier().forEDataType(eDataType);
      EList<EObject> _contents = this.container.getContents();
      _contents.add(adapter);
      return adapter;
    };
    return this.dataTypes.computeIfAbsent(eDataType, _function);
  }

  public Classifier findClassifier(final Domain containingDomain, final String qualifiedInstanceClassName) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedInstanceClassName);
    if (_isNullOrEmpty) {
      return null;
    }
    final EClassifier eClassifier = ClassifierProvider.findEClassifier(containingDomain, qualifiedInstanceClassName);
    Classifier _classifier = null;
    if (eClassifier!=null) {
      _classifier=this.toClassifier(eClassifier, containingDomain);
    }
    return _classifier;
  }

  private static EClassifier findEClassifier(final Domain containingDomain, final String qualifiedInstanceClassName) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedInstanceClassName);
    if (_isNullOrEmpty) {
      return null;
    }
    Collection<EPackage> _xifexpression = null;
    if ((containingDomain instanceof MetamodelAdapter)) {
      _xifexpression = ((MetamodelAdapter)containingDomain).getAllPackages();
    } else {
      _xifexpression = CollectionLiterals.<EPackage>emptyList();
    }
    Collection<EPackage> domainPackages = _xifexpression;
    List<EcorePackage> _of = List.<EcorePackage>of(EcorePackage.eINSTANCE);
    final Iterable<EPackage> relevantPackages = Iterables.<EPackage>concat(_of, domainPackages);
    final Function1<EPackage, EClassifier> _function = (EPackage it) -> {
      return ClassifierProvider.findEClassifier(it, qualifiedInstanceClassName);
    };
    return IterableExtensions.<EClassifier>head(IterableExtensions.<EClassifier>filterNull(IterableExtensions.<EPackage, EClassifier>map(relevantPackages, _function)));
  }

  private static EClassifier findEClassifier(final EPackage ePackage, final String qualifiedInstanceClassName) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedInstanceClassName);
    if (_isNullOrEmpty) {
      return null;
    }
    final Function1<EClassifier, Boolean> _function = (EClassifier it) -> {
      String _instanceClassName = it.getInstanceClassName();
      return Boolean.valueOf(Objects.equal(_instanceClassName, qualifiedInstanceClassName));
    };
    return IterableExtensions.<EClassifier>head(IterableExtensions.<EClassifier>filter(ePackage.getEClassifiers(), _function));
  }

  public Classifier toClassifier(final EClassifier eClass, final Domain containingDomain) {
    if (eClass instanceof EClass) {
      return _toClassifier((EClass)eClass, containingDomain);
    } else if (eClass instanceof EDataType) {
      return _toClassifier((EDataType)eClass, containingDomain);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(eClass, containingDomain).toString());
    }
  }
}
