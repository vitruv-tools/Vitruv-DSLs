package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.language.elements.Metamodel;
import tools.vitruv.dsls.commonalities.language.elements.MetamodelProvider;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

@SuppressWarnings("all")
public class MetamodelMetaclassesScope implements IScope {
  @Data
  public static class ImportedMetamodel {
    private final String name;

    private final EPackage ePackage;

    public ImportedMetamodel(final String name, final EPackage ePackage) {
      super();
      this.name = name;
      this.ePackage = ePackage;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.name== null) ? 0 : this.name.hashCode());
      return prime * result + ((this.ePackage== null) ? 0 : this.ePackage.hashCode());
    }

    @Override
    @Pure
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      MetamodelMetaclassesScope.ImportedMetamodel other = (MetamodelMetaclassesScope.ImportedMetamodel) obj;
      if (this.name == null) {
        if (other.name != null)
          return false;
      } else if (!this.name.equals(other.name))
        return false;
      if (this.ePackage == null) {
        if (other.ePackage != null)
          return false;
      } else if (!this.ePackage.equals(other.ePackage))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("name", this.name);
      b.add("ePackage", this.ePackage);
      return b.toString();
    }

    @Pure
    public String getName() {
      return this.name;
    }

    @Pure
    public EPackage getEPackage() {
      return this.ePackage;
    }
  }

  private final IEObjectDescriptionProvider descriptionProvider;

  private final MetamodelProvider metamodelProvider;

  private final Set<String> metamodelNames = new HashSet<String>();

  public MetamodelMetaclassesScope(final Resource resource, final IEObjectDescriptionProvider descriptionProvider, final MetamodelProvider provider) {
    this.descriptionProvider = descriptionProvider;
    this.metamodelProvider = provider;
    final Iterable<MetamodelMetaclassesScope.ImportedMetamodel> importedMetamodels = this.extractImportedMetamodels(resource);
    final Function1<MetamodelMetaclassesScope.ImportedMetamodel, String> _function = (MetamodelMetaclassesScope.ImportedMetamodel it) -> {
      return it.name;
    };
    Set<String> _set = IterableExtensions.<String>toSet(IterableExtensions.<MetamodelMetaclassesScope.ImportedMetamodel, String>map(importedMetamodels, _function));
    Iterables.<String>addAll(this.metamodelNames, _set);
    final Consumer<MetamodelMetaclassesScope.ImportedMetamodel> _function_1 = (MetamodelMetaclassesScope.ImportedMetamodel it) -> {
      this.metamodelProvider.registerReferencedMetamodel(it.name, it.ePackage);
    };
    importedMetamodels.forEach(_function_1);
  }

  private Iterable<MetamodelMetaclassesScope.ImportedMetamodel> extractImportedMetamodels(final Resource res) {
    final Function1<EObject, Boolean> _function = (EObject it) -> {
      EClass _eClass = it.eClass();
      EClass _metamodelImport = ElementsPackage.eINSTANCE.getMetamodelImport();
      return Boolean.valueOf(Objects.equal(_eClass, _metamodelImport));
    };
    final Function1<MetamodelImport, Boolean> _function_1 = (MetamodelImport it) -> {
      EPackage _package = it.getPackage();
      return Boolean.valueOf((_package != null));
    };
    Iterable<MetamodelImport> imports = IterableExtensions.<MetamodelImport>filter(Iterables.<MetamodelImport>filter(IteratorExtensions.<EObject>toList(IteratorExtensions.<EObject>filter(res.getAllContents(), _function)), 
      MetamodelImport.class), _function_1);
    final Function1<MetamodelImport, MetamodelMetaclassesScope.ImportedMetamodel> _function_2 = (MetamodelImport it) -> {
      String _elvis = null;
      String _name = it.getName();
      if (_name != null) {
        _elvis = _name;
      } else {
        String _name_1 = it.getPackage().getName();
        _elvis = _name_1;
      }
      EPackage _package = it.getPackage();
      return new MetamodelMetaclassesScope.ImportedMetamodel(_elvis, _package);
    };
    final Iterable<MetamodelMetaclassesScope.ImportedMetamodel> importedMetamodels = IterableExtensions.<MetamodelImport, MetamodelMetaclassesScope.ImportedMetamodel>map(imports, _function_2);
    return importedMetamodels;
  }

  @Override
  public Iterable<IEObjectDescription> getAllElements() {
    final Function1<String, Metamodel> _function = (String it) -> {
      return this.metamodelProvider.getMetamodelByName(it);
    };
    final Function1<Metamodel, EList<Metaclass>> _function_1 = (Metamodel it) -> {
      return it.getMetaclasses();
    };
    return IterableExtensions.<Metaclass, IEObjectDescription>map(IterableExtensions.<Metamodel, Metaclass>flatMap(IterableExtensions.<String, Metamodel>map(this.metamodelNames, _function), _function_1), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final QualifiedName qName) {
    final String metamodelName = QualifiedNameHelper.getMetamodelName(qName);
    if (((metamodelName == null) || (!this.metamodelNames.contains(metamodelName)))) {
      return Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList());
    }
    final String className = QualifiedNameHelper.getClassName(qName);
    if ((className == null)) {
      return Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList());
    }
    List<Metaclass> _elvis = null;
    Metamodel _metamodelByName = this.metamodelProvider.getMetamodelByName(metamodelName);
    EList<Metaclass> _metaclasses = null;
    if (_metamodelByName!=null) {
      _metaclasses=_metamodelByName.getMetaclasses();
    }
    if (_metaclasses != null) {
      _elvis = _metaclasses;
    } else {
      _elvis = Collections.<Metaclass>unmodifiableList(CollectionLiterals.<Metaclass>newArrayList());
    }
    final Function1<Metaclass, Boolean> _function = (Metaclass it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, className));
    };
    return IterableExtensions.<Metaclass, IEObjectDescription>map(IterableExtensions.<Metaclass>filter(_elvis, _function), this.descriptionProvider);
  }

  @Override
  public Iterable<IEObjectDescription> getElements(final EObject object) {
    final URI objectURI = EcoreUtil2.getURI(object);
    final Function1<IEObjectDescription, Boolean> _function = (IEObjectDescription it) -> {
      return Boolean.valueOf(((it.getEObjectOrProxy() == object) || Objects.equal(it.getEObjectURI(), objectURI)));
    };
    return IterableExtensions.<IEObjectDescription>filter(this.getAllElements(), _function);
  }

  @Override
  public IEObjectDescription getSingleElement(final QualifiedName name) {
    return IterableExtensions.<IEObjectDescription>head(this.getElements(name));
  }

  @Override
  public IEObjectDescription getSingleElement(final EObject object) {
    return IterableExtensions.<IEObjectDescription>head(this.getElements(object));
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    String _simpleName = MetamodelMetaclassesScope.class.getSimpleName();
    _builder.append(_simpleName);
    _builder.append(" for metamodels ");
    _builder.append(this.metamodelNames);
    return _builder.toString();
  }
}
