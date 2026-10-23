package tools.vitruv.dsls.reactions.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;
import org.eclipse.xtext.xtype.XtypeFactory;
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;

import static com.google.common.base.Preconditions.*;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;

/**
 * Parent class of all fluent builders. The builders work in three phases:
 *
 * <ol>
 * <li>Building phase. The user is using the offered methods to create the
 * desired elements. They must leave all builders in a sufficiently initialized
 * state (such that {@link #readyToBeAttached} is {@code true}).
 * <li>Attachment preparation phase. Just before the builder is to be attached
 * to a resource, this phase is triggered. The user may not modify builders
 * anymore and the builders do outstanding initializations (which can now rely
 * on the fact that nothing will be changed by the user anymore).
 * <li>Jvm Types Linking phase. After the builders have been attached to a 
 * resource, generated JVM types will be available. The builders now use them
 * to create the missing elements. Building is finished afterwards.
 * </ol>
 */
abstract class FluentReactionElementBuilder {

  /**
   * Calls the provided {@code consumer} on every element in this list.
   * If elements are attempted to be added to this list during the
   * iteration, these elements are stored and will be iterated over after
   * the current iteration ends. This ends as soon as no new elements are
   * added to the list.
   */
  public static <T> void patientForEach(List<T> delegate, Consumer<T> consumer) {
    for (int i = 0; i < delegate.size(); i++) {
      consumer.accept(delegate.get(i));
    }
  }

  /**
   * Discards all elements. After this method was
   * called, attempting to modify (but not read from) this list will
   * throw an exception.
   */
  <T> List<T> discardAndClose() {
    return Collections.emptyList();
  }

  protected List<Runnable> beforeAttached = new LinkedList<>();
  protected List<Runnable> afterJvmTypeCreation = new LinkedList<>();
  /**
   * Builders mimic the tree structure of the elements they create. Events
   * ({@link #triggerBeforeAttached} and {@link #triggerAfterJvmTypeCreation})
   * are handled bottom-up, so children have a chance to modify their parent
   * before they enter the phase.
   */
  @Getter(AccessLevel.PROTECTED)
  private List<FluentReactionElementBuilder> childBuilders = new LinkedList<>();

  protected final FluentBuilderContext context;

  /**
   * Signals whether enough methods have been called on this builder and its
   * subbuilders. If this is {@code false}, attaching the builder to a
   * resource will certainly fail. It might, however, still fail if this is
   * {@code true}.
   */
  protected boolean readyToBeAttached = false;

  @Getter(AccessLevel.PROTECTED)
  private boolean jvmTypesAvailable = false;


  @Getter(AccessLevel.PROTECTED)
  private ReactionsFile attachedReactionsFile;
  @Getter(AccessLevel.PROTECTED)
  private Resource targetResource;

  protected FluentReactionElementBuilder(FluentBuilderContext context) {
    this.context = context;
  }

  /**
   * Determines whether building this builder in its current state will
   * generate code. If this method returns {@code false}, there is no
   * use in building this builder, and doing so might throw an exception.
   */
  public boolean willGenerateCode() {
    return childBuilders.stream().anyMatch(builder -> builder.willGenerateCode());
  }

  void triggerBeforeAttached(ReactionsFile reactionsFile, Resource targetResource) {
    checkState(attachedReactionsFile == null, "The " + this + " was already attached to a reactions file!");
    patientForEach(childBuilders, childBuilder -> childBuilder.triggerBeforeAttached(reactionsFile, targetResource));
    this.attachedReactionsFile = reactionsFile;
    this.targetResource = targetResource;
    attachmentPreparation();
    patientForEach(beforeAttached, Runnable::run);
    beforeAttached = discardAndClose();
  }

  void triggerAfterJvmTypeCreation() {
    checkState(attachedReactionsFile != null, "This builder was not yet attached to a reactions file!");
    jvmTypesAvailable = true;
    patientForEach(childBuilders, FluentReactionElementBuilder::triggerAfterJvmTypeCreation);
    childBuilders = discardAndClose();
    patientForEach(afterJvmTypeCreation, Runnable::run);
    afterJvmTypeCreation = discardAndClose();
  }

  protected void checkNotYetAttached() {
    checkState(attachedReactionsFile == null,
        "This operation is only allowed before the " + this + " is attached to a resource!");
  }

  protected void attachmentPreparation() {
    checkState(readyToBeAttached,
        "The " + this + " is not sufficiently initialised to be attached to a resource!");
  }

  /**
   * Executes the given {@code initializer} just before this builder is being
   * attached to a resource. The initializer may rely on that the client will
   * not change the builder anymore.
   */
  protected <T> T beforeAttached(T element, Consumer<? super T> initializer) {
    beforeAttached.add(() -> initializer.accept(element));
    return element;
  }

  /**
   * Executes the given {@code initializer} after this builder has been added
   * to a resource and inferred JVM types are available.
   */
  protected <T> T whenJvmTypes(T element, Consumer<? super T> initializer) {
    afterJvmTypeCreation.add(() -> initializer.accept(element));
    return element;
  }

  protected IJvmTypeProvider delegateTypeProvider() {
    return context.getTypeProviderFactory()
        .findOrCreateTypeProvider(attachedReactionsFile.eResource().getResourceSet());
  }

  protected JvmTypeReferenceBuilder referenceBuilderFactory() {
    return context.getReferenceBuilderFactory()
        .create(attachedReactionsFile.eResource().getResourceSet());
  }

  private <T extends JvmDeclaredType> boolean equalImportTypes(T importedType, T type) {
    return (importedType.getQualifiedName().equals(type.getQualifiedName()));
  }

  protected <T extends JvmDeclaredType> T imported(T type) {
    Optional<XImportDeclaration> section =
        getXImportSection().getImportDeclarations().stream()
            .filter(declaration -> equalImportTypes(declaration.getImportedType(), type))
            .findFirst();
    if (section.isEmpty()) {
      createTypeImport(type);
    }
    return type;
  }

  protected <T extends JvmIdentifiableElement> T possiblyImported(T type) {
    if (type instanceof JvmDeclaredType declaredType) {
      imported(declaredType);
    } else if (type instanceof JvmMember member) {
      imported(member.getDeclaringType());
    }
    return type;
  }

  protected JvmDeclaredType staticExtensionAllImported(JvmDeclaredType declaredType) {
    Optional<XImportDeclaration> extensionDeclarationOpt =
        this.getXImportSection().getImportDeclarations()
            .stream()
            .filter(declaration -> declaration.isWildcard()
                && equalImportTypes(declaration.getImportedType(), declaredType))
            .findFirst();
    XImportDeclaration declaration =
        extensionDeclarationOpt.isPresent()
            ? extensionDeclarationOpt.get()
            : createTypeWildcardImport(declaredType);
    declaration.setExtension(true);
    return declaredType;
  }

  protected JvmOperation staticExtensionImported(JvmOperation operation) {
    return staticImport(operation, true);
  }

  protected JvmOperation staticExtensionWildcardImported(JvmOperation operation) {
    staticExtensionAllImported(operation.getDeclaringType());
    return operation;
  }

  protected JvmOperation staticImported(JvmOperation operation) {
    return staticImport(operation, false);
  }

  private JvmOperation staticImport(JvmOperation operation, boolean asExtension) {
    Optional<XImportDeclaration> existingStarImport =
        getXImportSection().getImportDeclarations().stream()
            .filter(xImportDeclaration -> xImportDeclaration.isWildcard()
                && equalImportTypes(xImportDeclaration.getImportedType(), operation.getDeclaringType()))
            .findFirst();
    if (existingStarImport.isPresent()) {
      XImportDeclaration starImport = existingStarImport.get();
      starImport.setExtension(starImport.isExtension() || asExtension);
    } else {
      Optional<XImportDeclaration> existingStaticOperationImport =
          getXImportSection().getImportDeclarations().stream()
              .filter(xImportDeclaration
                  -> equalImportTypes(xImportDeclaration.getImportedType(), operation.getDeclaringType())
                  && xImportDeclaration.getImportedName().equals(operation.getSimpleName())
                  && xImportDeclaration.isStatic())
              .findFirst();
      XImportDeclaration staticOperationImport =
          existingStaticOperationImport.orElseGet(() -> createStaticOperationImport(operation));
      staticOperationImport.setExtension(
          staticOperationImport.isExtension() || asExtension
      );
    }
    return operation;
  }


  private XImportDeclaration createStaticOperationImport(JvmOperation operation) {
    XImportDeclaration newDeclaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
    newDeclaration.setImportedType(operation.getDeclaringType());
    newDeclaration.setMemberName(operation.getSimpleName());
    newDeclaration.setStatic(true);
    getXImportSection().getImportDeclarations().add(newDeclaration);
    return newDeclaration;
  }

  private XImportDeclaration createTypeWildcardImport(JvmDeclaredType type) {
    XImportDeclaration newDeclaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
    newDeclaration.setImportedType(type);
    newDeclaration.setStatic(true);
    newDeclaration.setWildcard(true);

    List<XImportDeclaration> oldImports = new ArrayList<>(getXImportSection().getImportDeclarations());
    List<XImportDeclaration> newImports = getXImportSection().getImportDeclarations();
    newImports.clear();
    oldImports.stream().filter(
        oldImport -> !oldImport.isStatic()
            || !equalImportTypes(oldImport.getImportedType(), type)
    ).forEach(newImports::add);
    newImports.add(newDeclaration);
    return newDeclaration;
  }

  private XImportDeclaration createTypeImport(JvmDeclaredType type) {
    XImportDeclaration typeImport = XtypeFactory.eINSTANCE.createXImportDeclaration();
    typeImport.setImportedType(type);
    getXImportSection().getImportDeclarations().add(typeImport);
    return typeImport;
  }

  private XImportSection getXImportSection() {
    if (this.attachedReactionsFile.getNamespaceImports() == null) {
      XImportSection newImportSection = XtypeFactory.eINSTANCE.createXImportSection();
      this.attachedReactionsFile.setNamespaceImports(newImportSection);
    }
    return this.attachedReactionsFile.getNamespaceImports();
  }

  protected MetamodelImport metamodelImport(EPackage ePackage) {
    checkState(attachedReactionsFile != null && !jvmTypesAvailable,
        "Metamodel imports can only be created in the attachment preparation phase!");
    // there will usually only be a few metamodel imports, so no need for caching
    Optional<MetamodelImport> existingMMImport =
        attachedReactionsFile.getMetamodelImports().stream().filter(
            mmImport -> mmImport.getPackage().equals(ePackage)
        ).findFirst();
    if (existingMMImport.isPresent()) {
      return existingMMImport.get();
    }
    return createMetamodelImport(ePackage);
  }

  protected MetamodelImport metamodelImport(EPackage ePackage, String pname) {
    checkState(attachedReactionsFile != null && !jvmTypesAvailable,
        "Metamodel imports can only be created in the attachment preparation phase!");
    return createMetamodelImport(ePackage, ePackage.getName());
  }

  private MetamodelImport createMetamodelImport(EPackage ePackage) {
    return createMetamodelImport(ePackage, ePackage.getName());
  }

  private MetamodelImport createMetamodelImport(EPackage ePackage, String pname) {
    MetamodelImport newImport = ElementsFactory.eINSTANCE.createMetamodelImport();
    newImport.setPackage(ePackage);
    newImport.setName(pname);
    attachedReactionsFile.getMetamodelImports().add(newImport);
    return newImport;
  }

  protected <T extends MetaclassReference> T reference(T metaclassReference, EClass eClass) {
    metaclassReference.setMetaclass(eClass);
    return beforeAttached(metaclassReference, reference -> {
      reference.setMetamodel(metamodelImport(eClass.getEPackage()));
    });
  }

  protected <T extends MetaclassEReferenceReference> T reference(T referenceReference, EClass eClass,
                                                               EReference eReference) {
    referenceReference.setFeature(eReference);
    referenceReference.setMetaclass(eClass);
    return beforeAttached(referenceReference, reference -> {
      reference.setMetamodel(metamodelImport(eClass.getEPackage()));
    });
  }

  protected <T extends MetaclassEAttributeReference> T reference(T attributeReference, EClass eClass,
                                                               EAttribute attribute) {
    attributeReference.setFeature(attribute);
    attributeReference.setMetaclass(eClass);
    return beforeAttached(attributeReference, reference -> {
      reference.setMetamodel(metamodelImport(eClass.getEPackage()));
    });
  }

  protected <T extends NamedJavaElementReference> T reference(T javaElementReference, Class<?> clazz) {
    return beforeAttached(javaElementReference, reference -> {
      reference.setType(context.getTypeReferences().getTypeForName(clazz, targetResource));
    });
  }
}
