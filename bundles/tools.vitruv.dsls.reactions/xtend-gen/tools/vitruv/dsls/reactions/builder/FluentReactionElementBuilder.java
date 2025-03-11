package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;
import org.eclipse.xtext.xtype.XtypeFactory;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;

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
@SuppressWarnings("all")
abstract class FluentReactionElementBuilder {
  /**
   * List offering iteration while the list is being modified.
   */
  static class PatientList<T extends Object> implements List<T> {
    @Delegate
    private List<T> delegate = new LinkedList<T>();

    /**
     * Calls the provided {@code consumer} on every element in this list.
     * If elements are attempted to be added to this list during the
     * iteration, these elements are stored and will be iterated over after
     * the current iteration ends. This ends as soon as no new elements are
     * added to the list.
     */
    public void patientForEach(final Consumer<T> consumer) {
      final List<T> allList = this.delegate;
      while ((this.delegate.size() > 0)) {
        {
          final List<T> fromLastIteration = this.delegate;
          LinkedList<T> _linkedList = new LinkedList<T>();
          this.delegate = _linkedList;
          fromLastIteration.forEach(consumer);
          Iterables.<T>addAll(allList, this.delegate);
        }
      }
      this.delegate = allList;
    }

    /**
     * Discards all elements stored in this list. After this method was
     * called, attempting to modify (but not read from) this list will
     * throw an exception.
     */
    public void discardAndClose() {
      this.delegate = Collections.<T>emptyList();
    }

    public boolean add(final T arg0) {
      return this.delegate.add(arg0);
    }

    public void add(final int arg0, final T arg1) {
      this.delegate.add(arg0, arg1);
    }

    public boolean addAll(final Collection<? extends T> arg0) {
      return this.delegate.addAll(arg0);
    }

    public boolean addAll(final int arg0, final Collection<? extends T> arg1) {
      return this.delegate.addAll(arg0, arg1);
    }

    public void clear() {
      this.delegate.clear();
    }

    public boolean contains(final Object arg0) {
      return this.delegate.contains(arg0);
    }

    public boolean containsAll(final Collection<?> arg0) {
      return this.delegate.containsAll(arg0);
    }

    public void forEach(final Consumer<? super T> arg0) {
      this.delegate.forEach(arg0);
    }

    public T get(final int arg0) {
      return this.delegate.get(arg0);
    }

    public int indexOf(final Object arg0) {
      return this.delegate.indexOf(arg0);
    }

    public boolean isEmpty() {
      return this.delegate.isEmpty();
    }

    public Iterator<T> iterator() {
      return this.delegate.iterator();
    }

    public int lastIndexOf(final Object arg0) {
      return this.delegate.lastIndexOf(arg0);
    }

    public ListIterator<T> listIterator() {
      return this.delegate.listIterator();
    }

    public ListIterator<T> listIterator(final int arg0) {
      return this.delegate.listIterator(arg0);
    }

    public Stream<T> parallelStream() {
      return this.delegate.parallelStream();
    }

    public boolean remove(final Object arg0) {
      return this.delegate.remove(arg0);
    }

    public T remove(final int arg0) {
      return this.delegate.remove(arg0);
    }

    public boolean removeAll(final Collection<?> arg0) {
      return this.delegate.removeAll(arg0);
    }

    public boolean removeIf(final Predicate<? super T> arg0) {
      return this.delegate.removeIf(arg0);
    }

    public void replaceAll(final UnaryOperator<T> arg0) {
      this.delegate.replaceAll(arg0);
    }

    public boolean retainAll(final Collection<?> arg0) {
      return this.delegate.retainAll(arg0);
    }

    public T set(final int arg0, final T arg1) {
      return this.delegate.set(arg0, arg1);
    }

    public int size() {
      return this.delegate.size();
    }

    public void sort(final Comparator<? super T> arg0) {
      this.delegate.sort(arg0);
    }

    public Spliterator<T> spliterator() {
      return this.delegate.spliterator();
    }

    public Stream<T> stream() {
      return this.delegate.stream();
    }

    public List<T> subList(final int arg0, final int arg1) {
      return this.delegate.subList(arg0, arg1);
    }

    public Object[] toArray() {
      return this.delegate.toArray();
    }

    public <T extends Object> T[] toArray(final IntFunction<T[]> arg0) {
      return this.delegate.toArray(arg0);
    }

    public <T extends Object> T[] toArray(final T[] arg0) {
      return this.delegate.toArray(arg0);
    }
  }

  private final FluentReactionElementBuilder.PatientList<Runnable> beforeAttached = new FluentReactionElementBuilder.PatientList<Runnable>();

  private final FluentReactionElementBuilder.PatientList<Runnable> afterJvmTypeCreation = new FluentReactionElementBuilder.PatientList<Runnable>();

  protected final FluentBuilderContext context;

  /**
   * Signals whether enough methods have been called on this builder and its
   * subbuilders. If this is {@code false}, attaching the builder to a
   * resource will certainly fail. It might, however, still fail if this is
   * {@code true}.
   */
  protected boolean readyToBeAttached = false;

  @Accessors(AccessorType.PROTECTED_GETTER)
  protected boolean jvmTypesAvailable = false;

  /**
   * Builders mimic the tree structure of the elements they create. Events
   * ({@link #triggerBeforeAttached} and {@link #triggerAfterJvmTypeCreation})
   * are handled bottom-up, so children have a chance to modify their parent
   * before they enter the phase.
   */
  @Accessors(AccessorType.PROTECTED_GETTER)
  private final FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> childBuilders = new FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder>();

  @Accessors(AccessorType.PROTECTED_GETTER)
  private ReactionsFile attachedReactionsFile;

  @Accessors(AccessorType.PROTECTED_GETTER)
  private Resource targetResource;

  protected FluentReactionElementBuilder(final FluentBuilderContext context) {
    this.context = context;
  }

  /**
   * Determines whether building this builder in its current state will
   * generate code. If this method returns {@code false}, there is no
   * use in building this builder, and doing so might throw an exception.
   */
  public boolean willGenerateCode() {
    final Function1<FluentReactionElementBuilder, Boolean> _function = (FluentReactionElementBuilder it) -> {
      return Boolean.valueOf(it.willGenerateCode());
    };
    return IterableExtensions.<FluentReactionElementBuilder>exists(this.childBuilders, _function);
  }

  void triggerBeforeAttached(final ReactionsFile reactionsFile, final Resource targetResource) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The ");
    _builder.append(this);
    _builder.append(" was already attached to a reactions file!");
    Preconditions.checkState((this.attachedReactionsFile == null), _builder);
    final Consumer<FluentReactionElementBuilder> _function = (FluentReactionElementBuilder it) -> {
      it.triggerBeforeAttached(reactionsFile, targetResource);
    };
    this.childBuilders.patientForEach(_function);
    this.attachedReactionsFile = reactionsFile;
    this.targetResource = targetResource;
    this.attachmentPreparation();
    final Consumer<Runnable> _function_1 = (Runnable it) -> {
      it.run();
    };
    this.beforeAttached.patientForEach(_function_1);
    this.beforeAttached.discardAndClose();
  }

  void triggerAfterJvmTypeCreation() {
    Preconditions.checkState((this.attachedReactionsFile != null), "This builder was not yet attached to a reactions file!");
    this.jvmTypesAvailable = true;
    final Consumer<FluentReactionElementBuilder> _function = (FluentReactionElementBuilder it) -> {
      it.triggerAfterJvmTypeCreation();
    };
    this.childBuilders.patientForEach(_function);
    this.childBuilders.discardAndClose();
    final Consumer<Runnable> _function_1 = (Runnable it) -> {
      it.run();
    };
    this.afterJvmTypeCreation.patientForEach(_function_1);
    this.afterJvmTypeCreation.discardAndClose();
  }

  protected void checkNotYetAttached() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("This operation is only allowed before the ");
    _builder.append(this);
    _builder.append(" is attached to a resource!");
    Preconditions.checkState((this.attachedReactionsFile == 
      null), _builder);
  }

  protected void attachmentPreparation() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("The ");
    _builder.append(this);
    _builder.append(" is not sufficiently initialised to be attached to a resource!");
    Preconditions.checkState(this.readyToBeAttached, _builder);
  }

  /**
   * Executes the given {@code initializer} just before this builder is being
   * attached to a resource. The initializer may rely on that the client will
   * not change the builder anymore.
   */
  protected <T extends Object> T beforeAttached(final T element, final Consumer<? super T> initializer) {
    T _xblockexpression = null;
    {
      final Runnable _function = () -> {
        initializer.accept(element);
      };
      this.beforeAttached.add(_function);
      _xblockexpression = element;
    }
    return _xblockexpression;
  }

  /**
   * Executes the given {@code initializer} after this builder has been added
   * to a resource and inferred JVM types are available.
   */
  protected <T extends Object> T whenJvmTypes(final T element, final Consumer<? super T> initializer) {
    T _xblockexpression = null;
    {
      final Runnable _function = () -> {
        initializer.accept(element);
      };
      this.afterJvmTypeCreation.add(_function);
      _xblockexpression = element;
    }
    return _xblockexpression;
  }

  protected IJvmTypeProvider delegateTypeProvider() {
    return this.context.getTypeProviderFactory().findOrCreateTypeProvider(this.attachedReactionsFile.eResource().getResourceSet());
  }

  protected JvmTypeReferenceBuilder referenceBuilderFactory() {
    return this.context.getReferenceBuilderFactory().create(this.attachedReactionsFile.eResource().getResourceSet());
  }

  private <T extends JvmDeclaredType> boolean equalImportTypes(final T importedType, final T type) {
    String _qualifiedName = importedType.getQualifiedName();
    String _qualifiedName_1 = type.getQualifiedName();
    return Objects.equal(_qualifiedName, _qualifiedName_1);
  }

  protected <T extends JvmDeclaredType> T imported(final T type) {
    XImportDeclaration _elvis = null;
    final Function1<XImportDeclaration, Boolean> _function = (XImportDeclaration it) -> {
      return Boolean.valueOf(this.<JvmDeclaredType>equalImportTypes(it.getImportedType(), type));
    };
    XImportDeclaration _findFirst = IterableExtensions.<XImportDeclaration>findFirst(this.getXImportSection().getImportDeclarations(), _function);
    if (_findFirst != null) {
      _elvis = _findFirst;
    } else {
      XImportDeclaration _createTypeImport = this.createTypeImport(type);
      _elvis = _createTypeImport;
    }
    return type;
  }

  protected <T extends JvmIdentifiableElement> T possiblyImported(final T type) {
    boolean _matched = false;
    if (type instanceof JvmDeclaredType) {
      _matched=true;
      this.<JvmDeclaredType>imported(((JvmDeclaredType) type));
    }
    if (!_matched) {
      if (type instanceof JvmMember) {
        _matched=true;
        this.<JvmDeclaredType>imported(((JvmMember)type).getDeclaringType());
      }
    }
    return type;
  }

  protected JvmDeclaredType staticExtensionAllImported(final JvmDeclaredType declaredType) {
    XImportDeclaration _elvis = null;
    final Function1<XImportDeclaration, Boolean> _function = (XImportDeclaration it) -> {
      return Boolean.valueOf((it.isWildcard() && this.<JvmDeclaredType>equalImportTypes(it.getImportedType(), declaredType)));
    };
    XImportDeclaration _findFirst = IterableExtensions.<XImportDeclaration>findFirst(this.getXImportSection().getImportDeclarations(), _function);
    if (_findFirst != null) {
      _elvis = _findFirst;
    } else {
      XImportDeclaration _createTypeWildcardImport = this.createTypeWildcardImport(declaredType);
      _elvis = _createTypeWildcardImport;
    }
    final Procedure1<XImportDeclaration> _function_1 = (XImportDeclaration it) -> {
      it.setExtension(true);
    };
    ObjectExtensions.<XImportDeclaration>operator_doubleArrow(_elvis, _function_1);
    return declaredType;
  }

  protected JvmOperation staticExtensionImported(final JvmOperation operation) {
    return this.staticImport(operation, true);
  }

  protected JvmOperation staticExtensionWildcardImported(final JvmOperation operation) {
    this.staticExtensionAllImported(operation.getDeclaringType());
    return operation;
  }

  protected JvmOperation staticImported(final JvmOperation operation) {
    return this.staticImport(operation, false);
  }

  private JvmOperation staticImport(final JvmOperation operation, final boolean asExtension) {
    final Function1<XImportDeclaration, Boolean> _function = (XImportDeclaration it) -> {
      return Boolean.valueOf((it.isWildcard() && this.<JvmDeclaredType>equalImportTypes(it.getImportedType(), operation.getDeclaringType())));
    };
    final XImportDeclaration existingStarImport = IterableExtensions.<XImportDeclaration>findFirst(this.getXImportSection().getImportDeclarations(), _function);
    if ((existingStarImport != null)) {
      existingStarImport.setExtension((existingStarImport.isExtension() || asExtension));
    } else {
      XImportDeclaration _elvis = null;
      final Function1<XImportDeclaration, Boolean> _function_1 = (XImportDeclaration it) -> {
        return Boolean.valueOf(((this.<JvmDeclaredType>equalImportTypes(it.getImportedType(), operation.getDeclaringType()) && Objects.equal(it.getMemberName(), operation.getSimpleName())) && (it.isStatic() == true)));
      };
      XImportDeclaration _findFirst = IterableExtensions.<XImportDeclaration>findFirst(this.getXImportSection().getImportDeclarations(), _function_1);
      if (_findFirst != null) {
        _elvis = _findFirst;
      } else {
        XImportDeclaration _createStaticOperationImport = this.createStaticOperationImport(operation);
        _elvis = _createStaticOperationImport;
      }
      final Procedure1<XImportDeclaration> _function_2 = (XImportDeclaration it) -> {
        it.setExtension((it.isExtension() || asExtension));
      };
      ObjectExtensions.<XImportDeclaration>operator_doubleArrow(_elvis, _function_2);
    }
    return operation;
  }

  private XImportDeclaration createStaticOperationImport(final JvmOperation operation) {
    XImportDeclaration _createXImportDeclaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
    final Procedure1<XImportDeclaration> _function = (XImportDeclaration it) -> {
      it.setImportedType(operation.getDeclaringType());
      it.setMemberName(operation.getSimpleName());
      it.setStatic(true);
      EList<XImportDeclaration> _importDeclarations = this.getXImportSection().getImportDeclarations();
      _importDeclarations.add(it);
    };
    return ObjectExtensions.<XImportDeclaration>operator_doubleArrow(_createXImportDeclaration, _function);
  }

  private XImportDeclaration createTypeWildcardImport(final JvmDeclaredType type) {
    XImportDeclaration _createXImportDeclaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
    final Procedure1<XImportDeclaration> _function = (XImportDeclaration it) -> {
      it.setImportedType(type);
      it.setStatic(true);
      it.setWildcard(true);
    };
    final XImportDeclaration newDeclaration = ObjectExtensions.<XImportDeclaration>operator_doubleArrow(_createXImportDeclaration, _function);
    EList<XImportDeclaration> _importDeclarations = this.getXImportSection().getImportDeclarations();
    final ArrayList<XImportDeclaration> oldImports = new ArrayList<XImportDeclaration>(_importDeclarations);
    this.getXImportSection().getImportDeclarations().clear();
    EList<XImportDeclaration> _importDeclarations_1 = this.getXImportSection().getImportDeclarations();
    final Function1<XImportDeclaration, Boolean> _function_1 = (XImportDeclaration it) -> {
      return Boolean.valueOf(((!it.isStatic()) || (!this.<JvmDeclaredType>equalImportTypes(it.getImportedType(), type))));
    };
    Iterable<XImportDeclaration> _filter = IterableExtensions.<XImportDeclaration>filter(oldImports, _function_1);
    Iterables.<XImportDeclaration>addAll(_importDeclarations_1, _filter);
    EList<XImportDeclaration> _importDeclarations_2 = this.getXImportSection().getImportDeclarations();
    _importDeclarations_2.add(newDeclaration);
    return newDeclaration;
  }

  private XImportDeclaration createTypeImport(final JvmDeclaredType type) {
    XImportDeclaration _createXImportDeclaration = XtypeFactory.eINSTANCE.createXImportDeclaration();
    final Procedure1<XImportDeclaration> _function = (XImportDeclaration it) -> {
      it.setImportedType(type);
      EList<XImportDeclaration> _importDeclarations = this.getXImportSection().getImportDeclarations();
      _importDeclarations.add(it);
    };
    return ObjectExtensions.<XImportDeclaration>operator_doubleArrow(_createXImportDeclaration, _function);
  }

  private XImportSection getXImportSection() {
    XImportSection _elvis = null;
    XImportSection _namespaceImports = this.attachedReactionsFile.getNamespaceImports();
    if (_namespaceImports != null) {
      _elvis = _namespaceImports;
    } else {
      XImportSection _createXImportSection = XtypeFactory.eINSTANCE.createXImportSection();
      _elvis = _createXImportSection;
    }
    final Procedure1<XImportSection> _function = (XImportSection it) -> {
      this.attachedReactionsFile.setNamespaceImports(it);
    };
    return ObjectExtensions.<XImportSection>operator_doubleArrow(_elvis, _function);
  }

  protected MetamodelImport metamodelImport(final EPackage ePackage) {
    MetamodelImport _xblockexpression = null;
    {
      Preconditions.checkState(((this.attachedReactionsFile != null) && (!this.jvmTypesAvailable)), 
        "Metamodel imports can only be created in the attachment preparation phase!");
      MetamodelImport _elvis = null;
      final Function1<MetamodelImport, Boolean> _function = (MetamodelImport it) -> {
        EPackage _package = it.getPackage();
        return Boolean.valueOf(Objects.equal(_package, ePackage));
      };
      MetamodelImport _findFirst = IterableExtensions.<MetamodelImport>findFirst(this.attachedReactionsFile.getMetamodelImports(), _function);
      if (_findFirst != null) {
        _elvis = _findFirst;
      } else {
        MetamodelImport _createMetamodelImport = this.createMetamodelImport(ePackage);
        _elvis = _createMetamodelImport;
      }
      _xblockexpression = _elvis;
    }
    return _xblockexpression;
  }

  protected MetamodelImport metamodelImport(final EPackage ePackage, final String pname) {
    MetamodelImport _xblockexpression = null;
    {
      Preconditions.checkState(((this.attachedReactionsFile != null) && (!this.jvmTypesAvailable)), 
        "Metamodel imports can only be created in the attachment preparation phase!");
      _xblockexpression = this.createMetamodelImport(ePackage, ePackage.getName());
    }
    return _xblockexpression;
  }

  private MetamodelImport createMetamodelImport(final EPackage ePackage) {
    return this.createMetamodelImport(ePackage, ePackage.getName());
  }

  private MetamodelImport createMetamodelImport(final EPackage ePackage, final String pname) {
    MetamodelImport _createMetamodelImport = ElementsFactory.eINSTANCE.createMetamodelImport();
    final Procedure1<MetamodelImport> _function = (MetamodelImport it) -> {
      it.setPackage(ePackage);
      it.setName(pname);
    };
    final MetamodelImport newImport = ObjectExtensions.<MetamodelImport>operator_doubleArrow(_createMetamodelImport, _function);
    EList<MetamodelImport> _metamodelImports = this.attachedReactionsFile.getMetamodelImports();
    _metamodelImports.add(newImport);
    return newImport;
  }

  protected <T extends MetaclassReference> T reference(final T metaclassReference, final EClass eClass) {
    final Procedure1<T> _function = (T it) -> {
      it.setMetaclass(eClass);
    };
    final Consumer<T> _function_1 = (T it) -> {
      it.setMetamodel(this.metamodelImport(eClass.getEPackage()));
    };
    return this.<T>beforeAttached(ObjectExtensions.<T>operator_doubleArrow(metaclassReference, _function), _function_1);
  }

  protected <T extends MetaclassEReferenceReference> T reference(final T referenceReference, final EClass eClass, final EReference reference) {
    final Procedure1<T> _function = (T it) -> {
      it.setFeature(reference);
      it.setMetaclass(eClass);
    };
    final Consumer<T> _function_1 = (T it) -> {
      it.setMetamodel(this.metamodelImport(eClass.getEPackage()));
    };
    return this.<T>beforeAttached(ObjectExtensions.<T>operator_doubleArrow(referenceReference, _function), _function_1);
  }

  protected <T extends MetaclassEAttributeReference> T reference(final T attributeReference, final EClass eClass, final EAttribute attribute) {
    final Procedure1<T> _function = (T it) -> {
      it.setFeature(attribute);
      it.setMetaclass(eClass);
    };
    final Consumer<T> _function_1 = (T it) -> {
      it.setMetamodel(this.metamodelImport(eClass.getEPackage()));
    };
    return this.<T>beforeAttached(ObjectExtensions.<T>operator_doubleArrow(attributeReference, _function), _function_1);
  }

  protected <T extends NamedJavaElementReference> T reference(final T javaElementReference, final Class<?> clazz) {
    final Consumer<T> _function = (T it) -> {
      it.setType(this.context.getTypeReferences().getTypeForName(clazz, this.targetResource));
    };
    return this.<T>beforeAttached(javaElementReference, _function);
  }

  @Pure
  protected boolean getJvmTypesAvailable() {
    return this.jvmTypesAvailable;
  }

  @Pure
  protected FluentReactionElementBuilder.PatientList<FluentReactionElementBuilder> getChildBuilders() {
    return this.childBuilders;
  }

  @Pure
  protected ReactionsFile getAttachedReactionsFile() {
    return this.attachedReactionsFile;
  }

  @Pure
  protected Resource getTargetResource() {
    return this.targetResource;
  }
}
