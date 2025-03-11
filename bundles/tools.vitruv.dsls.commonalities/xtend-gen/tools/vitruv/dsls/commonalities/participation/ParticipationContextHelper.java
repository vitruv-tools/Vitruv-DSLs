package tools.vitruv.dsls.commonalities.participation;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.SimpleReferenceMapping;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;

@Utility
@SuppressWarnings("all")
public final class ParticipationContextHelper {
  private static final Map<Participation, ParticipationRoot> participationRoots = new WeakHashMap<Participation, ParticipationRoot>();

  private static final Map<Participation, Optional<ParticipationContext>> participationContexts = new WeakHashMap<Participation, Optional<ParticipationContext>>();

  private static final Map<Pair<CommonalityReference, String>, ParticipationRoot> referenceParticipationRoots = new WeakHashMap<Pair<CommonalityReference, String>, ParticipationRoot>();

  private static final Map<Pair<CommonalityReference, String>, ParticipationContext> referenceParticipationContexts = new WeakHashMap<Pair<CommonalityReference, String>, ParticipationContext>();

  /**
   * Optional: Empty if the participation specifies no root containment context.
   */
  public static Optional<ParticipationContext> getParticipationContext(final Participation participation) {
    final Function<Participation, Optional<ParticipationContext>> _function = (Participation it) -> {
      final ParticipationRoot participationRoot = ParticipationContextHelper.getParticipationRoot(participation);
      if ((participationRoot.isEmpty() && (!CommonalitiesLanguageModelExtensions.isCommonalityParticipation(participation)))) {
        return Optional.<ParticipationContext>empty();
      }
      boolean _isEmpty = IterableExtensions.isEmpty(ParticipationContextHelper.getNonRootClasses(participation));
      boolean _not = (!_isEmpty);
      XtendAssertHelper.assertTrue(_not);
      ParticipationContext _participationContext = new ParticipationContext(participation, participationRoot);
      return Optional.<ParticipationContext>of(_participationContext);
    };
    return ParticipationContextHelper.participationContexts.computeIfAbsent(participation, _function);
  }

  /**
   * Gets the participation's root.
   * <p>
   * Empty if the participation does not specify a root.
   */
  public static ParticipationRoot getParticipationRoot(final Participation participation) {
    final Function<Participation, ParticipationRoot> _function = (Participation it) -> {
      final ParticipationRoot participationRoot = new ParticipationRoot();
      boolean _hasResourceClass = CommonalitiesLanguageModelExtensions.hasResourceClass(participation);
      boolean _not = (!_hasResourceClass);
      if (_not) {
        return participationRoot;
      }
      final ParticipationClass leaf = IterableExtensions.<ParticipationClass>head(CommonalitiesLanguageModelExtensions.getLeafClasses(IterableExtensions.<ParticipationClass>head(CommonalitiesLanguageModelExtensions.getAllClasses(participation))));
      ParticipationClass current = leaf;
      ParticipationClass container = CommonalitiesLanguageModelExtensions.getDeclaredContainerClass(current);
      while (((!ParticipationContextHelper.hasRootMarker(current)) && (container != null))) {
        {
          current = container;
          container = CommonalitiesLanguageModelExtensions.getDeclaredContainerClass(current);
        }
      }
      XtendAssertHelper.assertTrue((ParticipationContextHelper.hasRootMarker(current) || CommonalitiesLanguageModelExtensions.isForResource(current)));
      final ParticipationClass rootStart = current;
      Set<ParticipationClass> _classes = participationRoot.getClasses();
      _classes.add(rootStart);
      Set<ParticipationClass> _classes_1 = participationRoot.getClasses();
      Iterable<ParticipationClass> _transitiveContainerClasses = CommonalitiesLanguageModelExtensions.getTransitiveContainerClasses(rootStart);
      Iterables.<ParticipationClass>addAll(_classes_1, _transitiveContainerClasses);
      final List<Containment> containments = IterableExtensions.<Containment>toList(CommonalitiesLanguageModelExtensions.getContainments(participation));
      Set<Containment> _rootContainments = participationRoot.getRootContainments();
      final Function1<Containment, Boolean> _function_1 = (Containment it_1) -> {
        return Boolean.valueOf((participationRoot.isRootClass(it_1.getContained()) && participationRoot.isRootClass(it_1.getContainer())));
      };
      Iterable<Containment> _filter = IterableExtensions.<Containment>filter(containments, _function_1);
      Iterables.<Containment>addAll(_rootContainments, _filter);
      Set<Containment> _boundaryContainments = participationRoot.getBoundaryContainments();
      final Function1<Containment, Boolean> _function_2 = (Containment it_1) -> {
        return Boolean.valueOf(((!participationRoot.isRootClass(it_1.getContained())) && participationRoot.isRootClass(it_1.getContainer())));
      };
      Iterable<Containment> _filter_1 = IterableExtensions.<Containment>filter(containments, _function_2);
      Iterables.<Containment>addAll(_boundaryContainments, _filter_1);
      return participationRoot;
    };
    return ParticipationContextHelper.participationRoots.computeIfAbsent(participation, _function);
  }

  private static boolean hasRootMarker(final ParticipationClass participationClass) {
    return participationClass.isSingleton();
  }

  public static Iterable<ParticipationContext> getReferenceParticipationContexts(final CommonalityReference reference) {
    final Function1<CommonalityReferenceMapping, String> _function = (CommonalityReferenceMapping it) -> {
      return CommonalitiesLanguageModelExtensions.getParticipation(it).getDomainName();
    };
    final Function1<String, ParticipationContext> _function_1 = (String it) -> {
      return ParticipationContextHelper.getReferenceParticipationContext(reference, it);
    };
    return IterableExtensions.<String, ParticipationContext>map(IterableExtensions.<String>toSet(IterableExtensions.<String>filterNull(ListExtensions.<CommonalityReferenceMapping, String>map(reference.getMappings(), _function))), _function_1);
  }

  public static ParticipationContext getReferenceParticipationContext(final CommonalityReference reference, final String domainName) {
    final Pair<CommonalityReference, String> referenceDomainPair = Pair.<CommonalityReference, String>of(reference, domainName);
    final Function<Pair<CommonalityReference, String>, ParticipationContext> _function = (Pair<CommonalityReference, String> it) -> {
      final ParticipationRoot referenceParticipationRoot = ParticipationContextHelper.getReferenceParticipationRoot(reference, domainName);
      XtendAssertHelper.assertTrue((referenceParticipationRoot != null));
      boolean _isEmpty = referenceParticipationRoot.isEmpty();
      boolean _not = (!_isEmpty);
      XtendAssertHelper.assertTrue(_not);
      final Set<CommonalityReferenceMapping> referenceMappings = referenceParticipationRoot.getReferenceMappings();
      boolean _isEmpty_1 = referenceMappings.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      XtendAssertHelper.assertTrue(_not_1);
      final Participation referencedParticipation = CommonalitiesLanguageModelExtensions.getReferencedParticipation(IterableExtensions.<CommonalityReferenceMapping>head(referenceMappings));
      return new ParticipationContext(referencedParticipation, referenceParticipationRoot);
    };
    return ParticipationContextHelper.referenceParticipationContexts.computeIfAbsent(referenceDomainPair, _function);
  }

  private static ParticipationRoot getReferenceParticipationRoot(final CommonalityReference reference, final String domainName) {
    final Pair<CommonalityReference, String> referenceDomainPair = Pair.<CommonalityReference, String>of(reference, domainName);
    final Function<Pair<CommonalityReference, String>, ParticipationRoot> _function = (Pair<CommonalityReference, String> it) -> {
      final List<CommonalityReferenceMapping> mappings = CommonalitiesLanguageModelExtensions.getMappings(reference, domainName);
      boolean _isEmpty = mappings.isEmpty();
      if (_isEmpty) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Reference \'");
        String _name = reference.getName();
        _builder.append(_name);
        _builder.append("\' has no mappings for domain \'");
        _builder.append(domainName);
        _builder.append("\'.");
        throw new IllegalArgumentException(_builder.toString());
      }
      int _size = mappings.size();
      boolean _equals = (_size == 1);
      if (_equals) {
        final CommonalityReferenceMapping mapping = IterableExtensions.<CommonalityReferenceMapping>head(mappings);
        if ((mapping instanceof OperatorReferenceMapping)) {
          boolean _isAttributeReference = CommonalitiesLanguageModelExtensions.isAttributeReference(((OperatorReferenceMapping)mapping));
          if (_isAttributeReference) {
            return ParticipationContextHelper.getAttributeReferenceParticipationRoot(((OperatorReferenceMapping)mapping));
          }
        }
      }
      final ParticipationRoot referenceParticipationRoot = new ParticipationRoot();
      Set<CommonalityReferenceMapping> _referenceMappings = referenceParticipationRoot.getReferenceMappings();
      Iterables.<CommonalityReferenceMapping>addAll(_referenceMappings, mappings);
      Set<ParticipationClass> _classes = referenceParticipationRoot.getClasses();
      final Function1<CommonalityReferenceMapping, ParticipationClass> _function_1 = (CommonalityReferenceMapping it_1) -> {
        return CommonalitiesLanguageModelExtensions.getParticipationClass(it_1);
      };
      List<ParticipationClass> _map = ListExtensions.<CommonalityReferenceMapping, ParticipationClass>map(mappings, _function_1);
      Iterables.<ParticipationClass>addAll(_classes, _map);
      Set<Containment> _boundaryContainments = referenceParticipationRoot.getBoundaryContainments();
      final Function1<CommonalityReferenceMapping, Iterable<? extends Containment>> _function_2 = (CommonalityReferenceMapping it_1) -> {
        return ParticipationContextHelper.getReferenceContainments(it_1);
      };
      Iterable<Containment> _flatten = Iterables.<Containment>concat(ListExtensions.<CommonalityReferenceMapping, Iterable<? extends Containment>>map(mappings, _function_2));
      Iterables.<Containment>addAll(_boundaryContainments, _flatten);
      return referenceParticipationRoot;
    };
    return ParticipationContextHelper.referenceParticipationRoots.computeIfAbsent(referenceDomainPair, _function);
  }

  private static ParticipationRoot getAttributeReferenceParticipationRoot(final OperatorReferenceMapping mapping) {
    XtendAssertHelper.assertTrue(CommonalitiesLanguageModelExtensions.isAttributeReference(mapping));
    final ParticipationRoot referenceParticipationRoot = new ParticipationRoot();
    Set<CommonalityReferenceMapping> _referenceMappings = referenceParticipationRoot.getReferenceMappings();
    _referenceMappings.add(mapping);
    final Participation referencedParticipation = CommonalitiesLanguageModelExtensions.getReferencedParticipation(mapping);
    boolean _isEmpty = ParticipationContextHelper.getParticipationContext(referencedParticipation).isEmpty();
    boolean _not = (!_isEmpty);
    XtendAssertHelper.assertTrue(_not);
    final ParticipationRoot participationRoot = ParticipationContextHelper.getParticipationRoot(referencedParticipation);
    Set<ParticipationClass> _classes = referenceParticipationRoot.getClasses();
    Set<ParticipationClass> _classes_1 = participationRoot.getClasses();
    Iterables.<ParticipationClass>addAll(_classes, _classes_1);
    Set<Containment> _rootContainments = referenceParticipationRoot.getRootContainments();
    Set<Containment> _rootContainments_1 = participationRoot.getRootContainments();
    Iterables.<Containment>addAll(_rootContainments, _rootContainments_1);
    Set<Containment> _boundaryContainments = referenceParticipationRoot.getBoundaryContainments();
    Set<Containment> _boundaryContainments_1 = participationRoot.getBoundaryContainments();
    Iterables.<Containment>addAll(_boundaryContainments, _boundaryContainments_1);
    referenceParticipationRoot.setAttributeReferenceRoot(mapping.getParticipationClass());
    Set<OperatorContainment> _attributeReferenceContainments = referenceParticipationRoot.getAttributeReferenceContainments();
    Iterable<OperatorContainment> _operatorContainments = ParticipationContextHelper.getOperatorContainments(mapping);
    Iterables.<OperatorContainment>addAll(_attributeReferenceContainments, _operatorContainments);
    return referenceParticipationRoot;
  }

  /**
   * Gets the cross-commonality containment relationships for the given
   * reference mapping.
   * <p>
   * The referenced participation's own root is used to determine the
   * non-root participation classes at the boundary between root and non-root
   * classes. These are the classes for which implicit containment
   * relationships with the root specified by the reference mapping exist.
   */
  private static Iterable<? extends Containment> _getReferenceContainments(final SimpleReferenceMapping mapping) {
    final Participation participation = CommonalitiesLanguageModelExtensions.getReferencedParticipation(mapping);
    XtendAssertHelper.assertTrue((participation != null));
    final ParticipationClass container = CommonalitiesLanguageModelExtensions.getParticipationClass(mapping);
    final Function1<ParticipationClass, ReferenceContainment> _function = (ParticipationClass contained) -> {
      ParticipationAttribute _reference = mapping.getReference();
      return new ReferenceContainment(container, contained, _reference);
    };
    return IterableExtensions.<ParticipationClass, ReferenceContainment>map(ParticipationContextHelper.getNonRootBoundaryClasses(participation), _function);
  }

  private static Iterable<? extends Containment> _getReferenceContainments(final OperatorReferenceMapping mapping) {
    return ParticipationContextHelper.getOperatorContainments(mapping);
  }

  private static Iterable<OperatorContainment> getOperatorContainments(final OperatorReferenceMapping mapping) {
    final ParticipationClass container = mapping.getParticipationClass();
    final Function1<ParticipationClass, OperatorContainment> _function = (ParticipationClass contained) -> {
      return new OperatorContainment(container, contained, mapping);
    };
    return IterableExtensions.<ParticipationClass, OperatorContainment>map(CommonalitiesLanguageModelExtensions.getReferencedParticipationClasses(mapping), _function);
  }

  /**
   * Gets all non-root participation classes that are located at the boundary
   * between non-root classes and the (containment context specific) root
   * classes.
   * <p>
   * If the given participation does not specify an own root, this returns
   * the participation's root container classes.
   * <p>
   * Since participations are not empty, have at least one root container
   * class and at least one non-root class, the result is expected to not be
   * empty.
   */
  public static Iterable<ParticipationClass> getNonRootBoundaryClasses(final Participation participation) {
    final ParticipationRoot participationRoot = ParticipationContextHelper.getParticipationRoot(participation);
    boolean _isEmpty = participationRoot.isEmpty();
    if (_isEmpty) {
      return CommonalitiesLanguageModelExtensions.getRootContainerClasses(participation);
    } else {
      final Function1<Containment, ParticipationClass> _function = (Containment it) -> {
        return it.getContained();
      };
      return IterableExtensions.<Containment, ParticipationClass>map(participationRoot.getBoundaryContainments(), _function);
    }
  }

  public static Iterable<ParticipationClass> getNonRootClasses(final Participation participation) {
    final ParticipationRoot participationRoot = ParticipationContextHelper.getParticipationRoot(participation);
    final Function1<ParticipationClass, Boolean> _function = (ParticipationClass it) -> {
      boolean _isRootClass = participationRoot.isRootClass(it);
      return Boolean.valueOf((!_isRootClass));
    };
    return IterableExtensions.<ParticipationClass>filter(CommonalitiesLanguageModelExtensions.getAllClasses(participation), _function);
  }

  public static boolean isRootClass(final ParticipationClass participationClass) {
    return ParticipationContextHelper.getParticipationRoot(CommonalitiesLanguageModelExtensions.getParticipation(participationClass)).isRootClass(participationClass);
  }

  public static Iterable<Containment> getNonRootContainments(final Participation participation) {
    final ParticipationRoot participationRoot = ParticipationContextHelper.getParticipationRoot(participation);
    final Function1<Containment, Boolean> _function = (Containment it) -> {
      return Boolean.valueOf(((!participationRoot.isRootClass(it.getContainer())) && (!participationRoot.isRootClass(it.getContained()))));
    };
    return IterableExtensions.<Containment>filter(CommonalitiesLanguageModelExtensions.getContainments(participation), _function);
  }

  private static Iterable<? extends Containment> getReferenceContainments(final CommonalityReferenceMapping mapping) {
    if (mapping instanceof OperatorReferenceMapping) {
      return _getReferenceContainments((OperatorReferenceMapping)mapping);
    } else if (mapping instanceof SimpleReferenceMapping) {
      return _getReferenceContainments((SimpleReferenceMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  private ParticipationContextHelper() {
    
  }
}
