package tools.vitruv.dsls.commonalities.participation;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Lazy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;

/**
 * Represents a participation in the context of a specific containment
 * hierarchy.
 * <p>
 * If a commonality is referenced by other commonalities, its participations
 * need to be matched in differently rooted containment hierarchies: A
 * participation may specify an own containment hierarchy that roots inside a
 * <code>Resource</code>, and/or it may be referenced by external commonality
 * reference mappings, which each specify different root containers for the
 * participation's non-root objects. This class represents the participation
 * adapted to one of those various contexts.
 * <p>
 * When a participation is referenced via commonality reference mappings, a
 * single participation context is derived from the combination of all
 * reference mappings involving the same participation domain.
 * <p>
 * For a commonality participation in its own context the root is empty, since
 * the participation objects are implicitly contained inside their intermediate
 * model's root.
 */
@FinalFieldsConstructor
@ToString
@SuppressWarnings("all")
public class ParticipationContext {
  public enum ParticipationClassRole {
    EXTERNAL_ROOT,

    ROOT,

    NON_ROOT;
  }

  /**
   * A ParticipationClass with its context specific role.
   */
  @Data
  public static class ContextClass {
    private final ParticipationClass participationClass;

    private final ParticipationContext.ParticipationClassRole role;

    public boolean isRootClass() {
      return (Objects.equal(this.role, ParticipationContext.ParticipationClassRole.ROOT) || Objects.equal(this.role, ParticipationContext.ParticipationClassRole.EXTERNAL_ROOT));
    }

    /**
     * Returns whether the ParticipationClass belongs to an external
     * participation, i.e. to the referencing participation if the
     * participation context is for a reference mapping.
     * <p>
     * Note that if a commonality references itself, its participations act
     * both as referencing and as referenced participation. In that case,
     * the participation context may contain multiple ContextClasses for
     * the same ParticipationClass, once for the 'external' and once for
     * the non-external role.
     */
    public boolean isExternal() {
      return Objects.equal(this.role, ParticipationContext.ParticipationClassRole.EXTERNAL_ROOT);
    }

    public ContextClass(final ParticipationClass participationClass, final ParticipationContext.ParticipationClassRole role) {
      super();
      this.participationClass = participationClass;
      this.role = role;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.participationClass== null) ? 0 : this.participationClass.hashCode());
      return prime * result + ((this.role== null) ? 0 : this.role.hashCode());
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
      ParticipationContext.ContextClass other = (ParticipationContext.ContextClass) obj;
      if (this.participationClass == null) {
        if (other.participationClass != null)
          return false;
      } else if (!this.participationClass.equals(other.participationClass))
        return false;
      if (this.role == null) {
        if (other.role != null)
          return false;
      } else if (!this.role.equals(other.role))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("participationClass", this.participationClass);
      b.add("role", this.role);
      return b.toString();
    }

    @Pure
    public ParticipationClass getParticipationClass() {
      return this.participationClass;
    }

    @Pure
    public ParticipationContext.ParticipationClassRole getRole() {
      return this.role;
    }
  }

  @Data
  public static class ContextContainment<T extends Containment> {
    private final ParticipationContext.ContextClass container;

    private final ParticipationContext.ContextClass contained;

    private final T containment;

    public ContextContainment(final ParticipationContext.ContextClass container, final ParticipationContext.ContextClass contained, final T containment) {
      super();
      this.container = container;
      this.contained = contained;
      this.containment = containment;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.container== null) ? 0 : this.container.hashCode());
      result = prime * result + ((this.contained== null) ? 0 : this.contained.hashCode());
      return prime * result + ((this.containment== null) ? 0 : this.containment.hashCode());
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
      ParticipationContext.ContextContainment<?> other = (ParticipationContext.ContextContainment<?>) obj;
      if (this.container == null) {
        if (other.container != null)
          return false;
      } else if (!this.container.equals(other.container))
        return false;
      if (this.contained == null) {
        if (other.contained != null)
          return false;
      } else if (!this.contained.equals(other.contained))
        return false;
      if (this.containment == null) {
        if (other.containment != null)
          return false;
      } else if (!this.containment.equals(other.containment))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("container", this.container);
      b.add("contained", this.contained);
      b.add("containment", this.containment);
      return b.toString();
    }

    @Pure
    public ParticipationContext.ContextClass getContainer() {
      return this.container;
    }

    @Pure
    public ParticipationContext.ContextClass getContained() {
      return this.contained;
    }

    @Pure
    public T getContainment() {
      return this.containment;
    }
  }

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final Participation participation;

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final ParticipationRoot root;

  @Lazy
  private List<ParticipationContext.ContextClass> _rootClasses;

  @Lazy
  private List<ParticipationContext.ContextClass> _nonRootClasses;

  @Lazy
  private List<ParticipationContext.ContextContainment<?>> _rootContainments;

  @Lazy
  private List<ParticipationContext.ContextContainment<?>> _boundaryContainments;

  @Lazy
  private List<ParticipationContext.ContextContainment<?>> _nonRootContainments;

  @Lazy
  private ParticipationContext.ContextClass _attributeReferenceRoot;

  @Lazy
  private List<ParticipationContext.ContextContainment<OperatorContainment>> _attributeReferenceContainments;

  private List<ParticipationContext.ContextClass> calculateRootClasses() {
    final Function1<ParticipationClass, ParticipationContext.ContextClass> _function = (ParticipationClass it) -> {
      ParticipationContext.ContextClass _xblockexpression = null;
      {
        ParticipationContext.ParticipationClassRole _xifexpression = null;
        boolean _isRootContext = this.isRootContext();
        boolean _not = (!_isRootContext);
        if (_not) {
          _xifexpression = ParticipationContext.ParticipationClassRole.EXTERNAL_ROOT;
        } else {
          _xifexpression = ParticipationContext.ParticipationClassRole.ROOT;
        }
        final ParticipationContext.ParticipationClassRole role = _xifexpression;
        _xblockexpression = new ParticipationContext.ContextClass(it, role);
      }
      return _xblockexpression;
    };
    return Collections.<ParticipationContext.ContextClass>unmodifiableList(IterableExtensions.<ParticipationContext.ContextClass>toList(IterableExtensions.<ParticipationClass, ParticipationContext.ContextClass>map(this.root.getClasses(), _function)));
  }

  private List<ParticipationContext.ContextClass> calculateNonRootClasses() {
    final Function1<ParticipationClass, ParticipationContext.ContextClass> _function = (ParticipationClass it) -> {
      return new ParticipationContext.ContextClass(it, ParticipationContext.ParticipationClassRole.NON_ROOT);
    };
    return Collections.<ParticipationContext.ContextClass>unmodifiableList(IterableExtensions.<ParticipationContext.ContextClass>toList(IterableExtensions.<ParticipationClass, ParticipationContext.ContextClass>map(ParticipationContextHelper.getNonRootClasses(this.participation), _function)));
  }

  private ParticipationContext.ContextClass getRootClass(final ParticipationClass participationClass) {
    final Function1<ParticipationContext.ContextClass, Boolean> _function = (ParticipationContext.ContextClass it) -> {
      return Boolean.valueOf(Objects.equal(it.participationClass, participationClass));
    };
    return IterableExtensions.<ParticipationContext.ContextClass>findFirst(this.getRootClasses(), _function);
  }

  private ParticipationContext.ContextClass getNonRootClass(final ParticipationClass participationClass) {
    final Function1<ParticipationContext.ContextClass, Boolean> _function = (ParticipationContext.ContextClass it) -> {
      return Boolean.valueOf(Objects.equal(it.participationClass, participationClass));
    };
    return IterableExtensions.<ParticipationContext.ContextClass>findFirst(this.getNonRootClasses(), _function);
  }

  private List<ParticipationContext.ContextContainment<?>> calculateRootContainments() {
    final Function1<Containment, ParticipationContext.ContextContainment<?>> _function = (Containment it) -> {
      ParticipationContext.ContextClass _rootClass = this.getRootClass(it.getContainer());
      ParticipationContext.ContextClass _rootClass_1 = this.getRootClass(it.getContained());
      ParticipationContext.ContextContainment<Containment> _contextContainment = new ParticipationContext.ContextContainment<Containment>(_rootClass, _rootClass_1, it);
      return ((ParticipationContext.ContextContainment<?>) _contextContainment);
    };
    return Collections.<ParticipationContext.ContextContainment<?>>unmodifiableList(IterableExtensions.<ParticipationContext.ContextContainment<?>>toList(IterableExtensions.<Containment, ParticipationContext.ContextContainment<?>>map(this.root.getRootContainments(), _function)));
  }

  private List<ParticipationContext.ContextContainment<?>> calculateBoundaryContainments() {
    final Function1<Containment, ParticipationContext.ContextContainment<?>> _function = (Containment it) -> {
      ParticipationContext.ContextClass _rootClass = this.getRootClass(it.getContainer());
      ParticipationContext.ContextClass _nonRootClass = this.getNonRootClass(it.getContained());
      ParticipationContext.ContextContainment<Containment> _contextContainment = new ParticipationContext.ContextContainment<Containment>(_rootClass, _nonRootClass, it);
      return ((ParticipationContext.ContextContainment<?>) _contextContainment);
    };
    return Collections.<ParticipationContext.ContextContainment<?>>unmodifiableList(IterableExtensions.<ParticipationContext.ContextContainment<?>>toList(IterableExtensions.<Containment, ParticipationContext.ContextContainment<?>>map(this.root.getBoundaryContainments(), _function)));
  }

  private List<ParticipationContext.ContextContainment<?>> calculateNonRootContainments() {
    final Function1<Containment, ParticipationContext.ContextContainment<?>> _function = (Containment it) -> {
      ParticipationContext.ContextClass _nonRootClass = this.getNonRootClass(it.getContainer());
      ParticipationContext.ContextClass _nonRootClass_1 = this.getNonRootClass(it.getContained());
      ParticipationContext.ContextContainment<Containment> _contextContainment = new ParticipationContext.ContextContainment<Containment>(_nonRootClass, _nonRootClass_1, it);
      return ((ParticipationContext.ContextContainment<?>) _contextContainment);
    };
    return Collections.<ParticipationContext.ContextContainment<?>>unmodifiableList(IterableExtensions.<ParticipationContext.ContextContainment<?>>toList(IterableExtensions.<Containment, ParticipationContext.ContextContainment<?>>map(ParticipationContextHelper.getNonRootContainments(this.participation), _function)));
  }

  private ParticipationContext.ContextClass calculateAttributeReferenceRoot() {
    ParticipationClass _attributeReferenceRoot = this.root.getAttributeReferenceRoot();
    boolean _tripleEquals = (_attributeReferenceRoot == null);
    if (_tripleEquals) {
      return null;
    } else {
      ParticipationClass _attributeReferenceRoot_1 = this.root.getAttributeReferenceRoot();
      return new ParticipationContext.ContextClass(_attributeReferenceRoot_1, ParticipationContext.ParticipationClassRole.EXTERNAL_ROOT);
    }
  }

  private List<ParticipationContext.ContextContainment<OperatorContainment>> calculateAttributeReferenceContainments() {
    final Function1<OperatorContainment, ParticipationContext.ContextContainment<OperatorContainment>> _function = (OperatorContainment it) -> {
      ParticipationContext.ContextContainment<OperatorContainment> _xblockexpression = null;
      {
        ParticipationContext.ContextClass _attributeReferenceRoot = this.getAttributeReferenceRoot();
        boolean _tripleNotEquals = (_attributeReferenceRoot != null);
        XtendAssertHelper.assertTrue(_tripleNotEquals);
        ParticipationContext.ContextClass _attributeReferenceRoot_1 = this.getAttributeReferenceRoot();
        ParticipationContext.ContextClass _nonRootClass = this.getNonRootClass(it.getContained());
        _xblockexpression = new ParticipationContext.ContextContainment<OperatorContainment>(_attributeReferenceRoot_1, _nonRootClass, it);
      }
      return _xblockexpression;
    };
    return Collections.<ParticipationContext.ContextContainment<OperatorContainment>>unmodifiableList(IterableExtensions.<ParticipationContext.ContextContainment<OperatorContainment>>toList(IterableExtensions.<OperatorContainment, ParticipationContext.ContextContainment<OperatorContainment>>map(this.root.getAttributeReferenceContainments(), _function)));
  }

  public boolean isRootContext() {
    return ((!this.isForReferenceMapping()) || this.isForAttributeReferenceMapping());
  }

  public boolean isForSingletonRoot() {
    return (this.isRootContext() && CommonalitiesLanguageModelExtensions.hasSingletonClass(this.participation));
  }

  public boolean isForReferenceMapping() {
    boolean _isEmpty = this.getReferenceMappings().isEmpty();
    return (!_isEmpty);
  }

  public boolean isForAttributeReferenceMapping() {
    return (this.isForReferenceMapping() && (this.getAttributeReferenceRoot() != null));
  }

  public Set<CommonalityReferenceMapping> getReferenceMappings() {
    return this.root.getReferenceMappings();
  }

  public CommonalityReference getDeclaringReference() {
    CommonalityReferenceMapping _head = IterableExtensions.<CommonalityReferenceMapping>head(this.getReferenceMappings());
    CommonalityReference _declaringReference = null;
    if (_head!=null) {
      _declaringReference=CommonalitiesLanguageModelExtensions.getDeclaringReference(_head);
    }
    return _declaringReference;
  }

  public Commonality getReferencingCommonality() {
    CommonalityReferenceMapping _head = IterableExtensions.<CommonalityReferenceMapping>head(this.getReferenceMappings());
    CommonalityReference _declaringReference = null;
    if (_head!=null) {
      _declaringReference=CommonalitiesLanguageModelExtensions.getDeclaringReference(_head);
    }
    return CommonalitiesLanguageModelExtensions.getDeclaringCommonality(_declaringReference);
  }

  public Commonality getReferencedCommonality() {
    CommonalityReferenceMapping _head = IterableExtensions.<CommonalityReferenceMapping>head(this.getReferenceMappings());
    Commonality _referencedCommonality = null;
    if (_head!=null) {
      _referencedCommonality=CommonalitiesLanguageModelExtensions.getReferencedCommonality(_head);
    }
    return _referencedCommonality;
  }

  /**
   * Note: In the context of an external reference mapping, this may include
   * participation classes that originate from an external participation. If
   * the referenced commonality is the referencing commonality itself (eg. a
   * Package commonality containing other packages as subpackages), the same
   * participation class may act as both root and non-root class.
   * <p>
   * This does not include the attribute reference root class.
   */
  public Iterable<ParticipationContext.ContextClass> getClasses() {
    List<ParticipationContext.ContextClass> _rootClasses = this.getRootClasses();
    List<ParticipationContext.ContextClass> _nonRootClasses = this.getNonRootClasses();
    return Iterables.<ParticipationContext.ContextClass>concat(_rootClasses, _nonRootClasses);
  }

  /**
   * Gets all classes that are managed by the corresponding Intermediate.
   * <p>
   * In the context of a reference mapping this does not include the external
   * reference root classes (since those are managed by another
   * Intermediate).
   * <p>
   * For root participation contexts it does include the root Resource
   * container class. If the participation has a singleton root, the
   * singleton root classes are not included.
   */
  public Iterable<ParticipationContext.ContextClass> getManagedClasses() {
    boolean _isForSingletonRoot = this.isForSingletonRoot();
    if (_isForSingletonRoot) {
      return this.getNonRootClasses();
    } else {
      final Function1<ParticipationContext.ContextClass, Boolean> _function = (ParticipationContext.ContextClass it) -> {
        boolean _isExternal = it.isExternal();
        return Boolean.valueOf((!_isExternal));
      };
      return IterableExtensions.<ParticipationContext.ContextClass>filter(this.getClasses(), _function);
    }
  }

  /**
   * Gets the list of external reference root classes.
   * <p>
   * These act as containers for objects of the referenced participation.
   * <p>
   * For attribute reference contexts this returns a list containing the
   * attribute reference root class. For regular reference mapping contexts
   * this returns the root classes. Otherwise this return an empty list.
   */
  public List<ParticipationContext.ContextClass> getReferenceRootClasses() {
    boolean _isForAttributeReferenceMapping = this.isForAttributeReferenceMapping();
    if (_isForAttributeReferenceMapping) {
      ParticipationContext.ContextClass _attributeReferenceRoot = this.getAttributeReferenceRoot();
      return Collections.<ParticipationContext.ContextClass>unmodifiableList(CollectionLiterals.<ParticipationContext.ContextClass>newArrayList(_attributeReferenceRoot));
    } else {
      boolean _isForReferenceMapping = this.isForReferenceMapping();
      if (_isForReferenceMapping) {
        return this.getRootClasses();
      } else {
        return Collections.<ParticipationContext.ContextClass>unmodifiableList(CollectionLiterals.<ParticipationContext.ContextClass>newArrayList());
      }
    }
  }

  /**
   * Gets the Resource root class, or <code>null</code> if there is none.
   */
  public ParticipationContext.ContextClass getResourceClass() {
    final Function1<ParticipationContext.ContextClass, Boolean> _function = (ParticipationContext.ContextClass it) -> {
      return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isForResource(it.participationClass));
    };
    return IterableExtensions.<ParticipationContext.ContextClass>head(IterableExtensions.<ParticipationContext.ContextClass>filter(this.getRootClasses(), _function));
  }

  /**
   * Gets all containments: Root, boundary, non-root and attribute reference
   * containments.
   */
  public Iterable<ParticipationContext.ContextContainment<?>> getContainments() {
    List<ParticipationContext.ContextContainment<?>> _rootContainments = this.getRootContainments();
    List<ParticipationContext.ContextContainment<?>> _boundaryContainments = this.getBoundaryContainments();
    Iterable<ParticipationContext.ContextContainment<?>> _plus = Iterables.<ParticipationContext.ContextContainment<?>>concat(_rootContainments, _boundaryContainments);
    List<ParticipationContext.ContextContainment<?>> _nonRootContainments = this.getNonRootContainments();
    Iterable<ParticipationContext.ContextContainment<?>> _plus_1 = Iterables.<ParticipationContext.ContextContainment<?>>concat(_plus, _nonRootContainments);
    List<ParticipationContext.ContextContainment<OperatorContainment>> _attributeReferenceContainments = this.getAttributeReferenceContainments();
    return Iterables.<ParticipationContext.ContextContainment<?>>concat(_plus_1, _attributeReferenceContainments);
  }

  /**
   * Gets all containments that this participation context is responsible
   * for.
   * <p>
   * I.e. this omits the root containments if this context is for a singleton
   * root.
   */
  public Iterable<ParticipationContext.ContextContainment<?>> getManagedContainments() {
    boolean _isForSingletonRoot = this.isForSingletonRoot();
    if (_isForSingletonRoot) {
      List<ParticipationContext.ContextContainment<?>> _boundaryContainments = this.getBoundaryContainments();
      List<ParticipationContext.ContextContainment<?>> _nonRootContainments = this.getNonRootContainments();
      Iterable<ParticipationContext.ContextContainment<?>> _plus = Iterables.<ParticipationContext.ContextContainment<?>>concat(_boundaryContainments, _nonRootContainments);
      List<ParticipationContext.ContextContainment<OperatorContainment>> _attributeReferenceContainments = this.getAttributeReferenceContainments();
      return Iterables.<ParticipationContext.ContextContainment<?>>concat(_plus, _attributeReferenceContainments);
    } else {
      return this.getContainments();
    }
  }

  public ParticipationContext(final Participation participation, final ParticipationRoot root) {
    super();
    this.participation = participation;
    this.root = root;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("participation", this.participation);
    b.add("root", this.root);
    b.add("_rootClasses", this._rootClasses);
    b.add("_nonRootClasses", this._nonRootClasses);
    b.add("_rootContainments", this._rootContainments);
    b.add("_boundaryContainments", this._boundaryContainments);
    b.add("_nonRootContainments", this._nonRootContainments);
    b.add("_attributeReferenceRoot", this._attributeReferenceRoot);
    b.add("_attributeReferenceContainments", this._attributeReferenceContainments);
    return b.toString();
  }

  @Pure
  public Participation getParticipation() {
    return this.participation;
  }

  @Pure
  public ParticipationRoot getRoot() {
    return this.root;
  }

  private boolean _rootClasses_isInitialised = false;

  private List<ParticipationContext.ContextClass> _rootClasses_initialise() {
    List<ParticipationContext.ContextClass> _calculateRootClasses = this.calculateRootClasses();
    return _calculateRootClasses;
  }

  public List<ParticipationContext.ContextClass> getRootClasses() {
    if (!_rootClasses_isInitialised) {
    	try {
    		_rootClasses = _rootClasses_initialise();
    	} finally {
    		_rootClasses_isInitialised = true;
    	}
    }
    return _rootClasses;
  }

  private boolean _nonRootClasses_isInitialised = false;

  private List<ParticipationContext.ContextClass> _nonRootClasses_initialise() {
    List<ParticipationContext.ContextClass> _calculateNonRootClasses = this.calculateNonRootClasses();
    return _calculateNonRootClasses;
  }

  public List<ParticipationContext.ContextClass> getNonRootClasses() {
    if (!_nonRootClasses_isInitialised) {
    	try {
    		_nonRootClasses = _nonRootClasses_initialise();
    	} finally {
    		_nonRootClasses_isInitialised = true;
    	}
    }
    return _nonRootClasses;
  }

  private boolean _rootContainments_isInitialised = false;

  private List<ParticipationContext.ContextContainment<?>> _rootContainments_initialise() {
    List<ParticipationContext.ContextContainment<?>> _calculateRootContainments = this.calculateRootContainments();
    return _calculateRootContainments;
  }

  public List<ParticipationContext.ContextContainment<?>> getRootContainments() {
    if (!_rootContainments_isInitialised) {
    	try {
    		_rootContainments = _rootContainments_initialise();
    	} finally {
    		_rootContainments_isInitialised = true;
    	}
    }
    return _rootContainments;
  }

  private boolean _boundaryContainments_isInitialised = false;

  private List<ParticipationContext.ContextContainment<?>> _boundaryContainments_initialise() {
    List<ParticipationContext.ContextContainment<?>> _calculateBoundaryContainments = this.calculateBoundaryContainments();
    return _calculateBoundaryContainments;
  }

  public List<ParticipationContext.ContextContainment<?>> getBoundaryContainments() {
    if (!_boundaryContainments_isInitialised) {
    	try {
    		_boundaryContainments = _boundaryContainments_initialise();
    	} finally {
    		_boundaryContainments_isInitialised = true;
    	}
    }
    return _boundaryContainments;
  }

  private boolean _nonRootContainments_isInitialised = false;

  private List<ParticipationContext.ContextContainment<?>> _nonRootContainments_initialise() {
    List<ParticipationContext.ContextContainment<?>> _calculateNonRootContainments = this.calculateNonRootContainments();
    return _calculateNonRootContainments;
  }

  public List<ParticipationContext.ContextContainment<?>> getNonRootContainments() {
    if (!_nonRootContainments_isInitialised) {
    	try {
    		_nonRootContainments = _nonRootContainments_initialise();
    	} finally {
    		_nonRootContainments_isInitialised = true;
    	}
    }
    return _nonRootContainments;
  }

  private boolean _attributeReferenceRoot_isInitialised = false;

  private ParticipationContext.ContextClass _attributeReferenceRoot_initialise() {
    ParticipationContext.ContextClass _calculateAttributeReferenceRoot = this.calculateAttributeReferenceRoot();
    return _calculateAttributeReferenceRoot;
  }

  public ParticipationContext.ContextClass getAttributeReferenceRoot() {
    if (!_attributeReferenceRoot_isInitialised) {
    	try {
    		_attributeReferenceRoot = _attributeReferenceRoot_initialise();
    	} finally {
    		_attributeReferenceRoot_isInitialised = true;
    	}
    }
    return _attributeReferenceRoot;
  }

  private boolean _attributeReferenceContainments_isInitialised = false;

  private List<ParticipationContext.ContextContainment<OperatorContainment>> _attributeReferenceContainments_initialise() {
    List<ParticipationContext.ContextContainment<OperatorContainment>> _calculateAttributeReferenceContainments = this.calculateAttributeReferenceContainments();
    return _calculateAttributeReferenceContainments;
  }

  public List<ParticipationContext.ContextContainment<OperatorContainment>> getAttributeReferenceContainments() {
    if (!_attributeReferenceContainments_isInitialised) {
    	try {
    		_attributeReferenceContainments = _attributeReferenceContainments_initialise();
    	} finally {
    		_attributeReferenceContainments_isInitialised = true;
    	}
    }
    return _attributeReferenceContainments;
  }
}
