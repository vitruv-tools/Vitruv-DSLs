package tools.vitruv.dsls.commonalities.generator;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.commonalities.generator.intermediatemodel.IntermediateModelConstants;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.EClassAdapter;
import tools.vitruv.dsls.commonalities.language.elements.EFeatureAdapter;
import tools.vitruv.dsls.commonalities.language.elements.MetamodelAdapter;
import tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalityAttributeExtension;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;

@GenerationScoped
@SuppressWarnings("all")
public class GenerationContext {
  public static class Factory extends InjectingFactoryBase {
    public GenerationContext create(final IFileSystemAccess2 fsa, final CommonalityFile commonalityFile) {
      return this.<GenerationContext>injectMembers(new GenerationContext(fsa, commonalityFile));
    }
  }

  private static int lastSeenResourceSetHash;

  private static Set<String> generatedConcepts = CollectionLiterals.<String>emptySet();

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final IFileSystemAccess2 fsa;

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final CommonalityFile commonalityFile;

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final boolean isNewResourceSet;

  @Accessors(AccessorType.PUBLIC_GETTER)
  @Inject
  private CommonalitiesGenerationSettings settings;

  private final Map<String, EPackage> metamodelRootPackageForConcepts = new HashMap<String, EPackage>();

  private GenerationContext(final IFileSystemAccess2 fsa, final CommonalityFile commonalityFile) {
    Preconditions.<IFileSystemAccess2>checkNotNull(fsa, "fsa is null");
    Preconditions.<CommonalityFile>checkNotNull(commonalityFile, "commonalityFile is null");
    this.fsa = fsa;
    this.commonalityFile = commonalityFile;
    this.isNewResourceSet = this.updateResourceSetContext();
  }

  GenerationContext() {
    this.fsa = null;
    this.commonalityFile = null;
    this.isNewResourceSet = false;
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  private boolean updateResourceSetContext() {
    final int resourceSetHashCode = this.getResourceSet().hashCode();
    final boolean isNewResourceSet = (resourceSetHashCode != GenerationContext.lastSeenResourceSetHash);
    if (isNewResourceSet) {
      GenerationContext.lastSeenResourceSetHash = resourceSetHashCode;
      GenerationContext.generatedConcepts = CollectionLiterals.<String>emptySet();
    }
    return isNewResourceSet;
  }

  public Commonality getCommonality() {
    return this.commonalityFile.getCommonality();
  }

  public Concept getConcept() {
    return this.commonalityFile.getConcept();
  }

  public ResourceSet getResourceSet() {
    return this.commonalityFile.eResource().getResourceSet();
  }

  public Set<String> setGeneratedConcepts(final Set<String> newGeneratedConcepts) {
    return GenerationContext.generatedConcepts = newGeneratedConcepts;
  }

  public Set<String> getGeneratedConcepts() {
    return GenerationContext.generatedConcepts;
  }

  public EPackage reportGeneratedIntermediateMetamodel(final String conceptName, final EPackage intermediateMetamodelPackage) {
    return this.metamodelRootPackageForConcepts.put(conceptName, intermediateMetamodelPackage);
  }

  public EPackage getIntermediateMetamodelPackage(final Concept concept) {
    return this.getMetamodelRootPackage(concept);
  }

  public EPackage getIntermediateMetamodelPackage(final String conceptName) {
    return this.getMetamodelRootPackage(conceptName);
  }

  public URI getIntermediateMetamodelUri(final String conceptName) {
    return this.fsa.getURI(IntermediateModelConstants.getIntermediateMetamodelFilePath(conceptName));
  }

  public EClass getIntermediateMetamodelRootClass(final Concept concept) {
    return this.getIntermediateMetamodelRootClass(concept.getName());
  }

  public EClass getIntermediateMetamodelRootClass(final String conceptName) {
    EClassifier _eClassifier = this.getIntermediateMetamodelPackage(conceptName).getEClassifier(
      IntermediateModelConstants.getIntermediateMetamodelRootClassName(conceptName).getSimpleName());
    return ((EClass) _eClassifier);
  }

  public EClass getIntermediateMetamodelClass(final Commonality commonality) {
    EClassifier _eClassifier = this.getIntermediateMetamodelPackage(CommonalitiesLanguageModelExtensions.getConcept(commonality)).getEClassifier(
      IntermediateModelConstants.getIntermediateMetamodelClassName(commonality).getSimpleName());
    return ((EClass) _eClassifier);
  }

  protected EClass _getChangeClass(final Commonality commonality) {
    return this.getIntermediateMetamodelClass(commonality);
  }

  protected EClass _getChangeClass(final ParticipationClass participationClass) {
    return this.getChangeClass(participationClass.getSuperMetaclass());
  }

  protected EClass _getChangeClass(final EClassAdapter eClassAdapter) {
    return eClassAdapter.getWrapped();
  }

  protected EClass _getChangeClass(final ResourceMetaclass resourceMetaclass) {
    return ResourcesPackage.eINSTANCE.getIntermediateResourceBridge();
  }

  protected EClass _getChangeClass(final ClassLike classLike) {
    String _name = classLike.getClass().getName();
    String _plus = ("Unhandled ClassLike: " + _name);
    throw new IllegalStateException(_plus);
  }

  protected EStructuralFeature _getCorrespondingEFeature(final CommonalityAttribute attribute) {
    return this.getIntermediateMetamodelClass(CommonalityAttributeExtension.getDeclaringCommonality(attribute)).getEStructuralFeature(attribute.getName());
  }

  protected EStructuralFeature _getCorrespondingEFeature(final EFeatureAdapter adapter) {
    return adapter.getWrapped();
  }

  protected EStructuralFeature _getCorrespondingEFeature(final ParticipationAttribute participationAttribute) {
    return this.getCorrespondingEFeature(participationAttribute.getAttribute());
  }

  protected EStructuralFeature _getCorrespondingEFeature(final CommonalityReference reference) {
    return this.getIntermediateMetamodelClass(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(reference)).getEStructuralFeature(reference.getName());
  }

  protected EStructuralFeature _getCorrespondingEFeature(final Attribute attribute) {
    String _name = attribute.getClass().getName();
    String _plus = ("Unhandled Attribute type: " + _name);
    throw new IllegalStateException(_plus);
  }

  public EAttribute getCorrespondingEAttribute(final Attribute attribute) {
    EStructuralFeature _correspondingEFeature = this.getCorrespondingEFeature(attribute);
    return ((EAttribute) _correspondingEFeature);
  }

  public EReference getCorrespondingEReference(final Attribute attribute) {
    EStructuralFeature _correspondingEFeature = this.getCorrespondingEFeature(attribute);
    return ((EReference) _correspondingEFeature);
  }

  public EStructuralFeature getCommonalityEFeature(final CommonalityAttributeMapping mapping) {
    return this.getCorrespondingEFeature(CommonalitiesLanguageModelExtensions.getDeclaringAttribute(mapping));
  }

  public EStructuralFeature getParticipationEFeature(final CommonalityAttributeMapping mapping) {
    ParticipationAttribute _participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
    EStructuralFeature _correspondingEFeature = null;
    if (_participationAttribute!=null) {
      _correspondingEFeature=this.getCorrespondingEFeature(_participationAttribute);
    }
    return _correspondingEFeature;
  }

  public EPackage getMetamodelRootPackage(final Domain domain) {
    return this.findMetamodelRootPackage(domain);
  }

  private EPackage _findMetamodelRootPackage(final MetamodelAdapter adapter) {
    return adapter.getWrapped();
  }

  private EPackage _findMetamodelRootPackage(final Concept concept) {
    return this.getMetamodelRootPackage(concept);
  }

  public EPackage getMetamodelRootPackage(final Concept concept) {
    return this.getMetamodelRootPackage(concept.getName());
  }

  public EPackage getMetamodelRootPackage(final String conceptName) {
    final Function<String, EPackage> _function = (String cName) -> {
      EPackage _xblockexpression = null;
      {
        EObject _head = IterableExtensions.<EObject>head(this.getResourceSet().getResource(this.getIntermediateMetamodelUri(cName), false).getContents());
        final EPackage ePackage = ((EPackage) _head);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("No ePackage was registered for the concept ‹");
        _builder.append(conceptName);
        _builder.append("›!");
        Preconditions.checkState((ePackage != null), _builder);
        _xblockexpression = ePackage;
      }
      return _xblockexpression;
    };
    return this.metamodelRootPackageForConcepts.computeIfAbsent(conceptName, _function);
  }

  public EClass getChangeClass(final ClassLike eClassAdapter) {
    if (eClassAdapter instanceof EClassAdapter) {
      return _getChangeClass((EClassAdapter)eClassAdapter);
    } else if (eClassAdapter instanceof Commonality) {
      return _getChangeClass((Commonality)eClassAdapter);
    } else if (eClassAdapter instanceof ResourceMetaclass) {
      return _getChangeClass((ResourceMetaclass)eClassAdapter);
    } else if (eClassAdapter instanceof ParticipationClass) {
      return _getChangeClass((ParticipationClass)eClassAdapter);
    } else if (eClassAdapter != null) {
      return _getChangeClass(eClassAdapter);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(eClassAdapter).toString());
    }
  }

  public EStructuralFeature getCorrespondingEFeature(final Attribute adapter) {
    if (adapter instanceof EFeatureAdapter) {
      return _getCorrespondingEFeature((EFeatureAdapter)adapter);
    } else if (adapter instanceof CommonalityAttribute) {
      return _getCorrespondingEFeature((CommonalityAttribute)adapter);
    } else if (adapter instanceof CommonalityReference) {
      return _getCorrespondingEFeature((CommonalityReference)adapter);
    } else if (adapter instanceof ParticipationAttribute) {
      return _getCorrespondingEFeature((ParticipationAttribute)adapter);
    } else if (adapter != null) {
      return _getCorrespondingEFeature(adapter);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(adapter).toString());
    }
  }

  private EPackage findMetamodelRootPackage(final Domain adapter) {
    if (adapter instanceof MetamodelAdapter) {
      return _findMetamodelRootPackage((MetamodelAdapter)adapter);
    } else if (adapter instanceof Concept) {
      return _findMetamodelRootPackage((Concept)adapter);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(adapter).toString());
    }
  }

  @Pure
  public IFileSystemAccess2 getFsa() {
    return this.fsa;
  }

  @Pure
  public CommonalityFile getCommonalityFile() {
    return this.commonalityFile;
  }

  @Pure
  public boolean isNewResourceSet() {
    return this.isNewResourceSet;
  }

  @Pure
  public CommonalitiesGenerationSettings getSettings() {
    return this.settings;
  }
}
