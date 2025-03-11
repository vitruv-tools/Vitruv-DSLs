package tools.vitruv.dsls.commonalities.runtime.matching;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.StringUtil;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.propagation.ResourceAccess;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesFactory;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * Matches participation classes to their objects according to the specified
 * containment hierarchy.
 */
@SuppressWarnings("all")
public class ParticipationMatcher {
  private static final Logger logger = Logger.getLogger(ParticipationMatcher.class);

  private final ContainmentContext containmentContext;

  private final EObject startObject;

  private final boolean followAttributeReferences;

  private final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel;

  private final ResourceAccess resourceAccess;

  /**
   * Creates a new {@link ParticipationMatcher}.
   * 
   * @param containmentContext
   * 		the containment context
   * @param start
   * 		an existing object inside the containment hierarchy that is used to
   * 		find the containment hierarchy's root object
   * @param followAttributeReferences
   * 		If <code>true</code> and the containment context specifies attribute
   * 		references, attribute references outgoing from an object that
   * 		matches the attribute reference root are followed in order to find
   * 		candidate root objects for the matching to start at. For this to
   * 		work as expected, the given start object should either already be
   * 		the attribute reference root, or be contained by it.
   * @param correspondenceModel
   * 		the correspondence model
   */
  public ParticipationMatcher(final ContainmentContext containmentContext, final EObject startObject, final boolean followAttributeReferences, final EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel, final ResourceAccess resourceAccess) {
    Preconditions.<ContainmentContext>checkNotNull(containmentContext, "containmentContext is null");
    Preconditions.<EObject>checkNotNull(startObject, "startObject is null");
    Preconditions.<EditableCorrespondenceModelView<ReactionsCorrespondence>>checkNotNull(correspondenceModel, "correspondenceModel is null");
    Preconditions.<ResourceAccess>checkNotNull(resourceAccess, "resourceAccess is null");
    this.containmentContext = containmentContext;
    this.startObject = startObject;
    this.followAttributeReferences = followAttributeReferences;
    this.correspondenceModel = correspondenceModel;
    this.resourceAccess = resourceAccess;
  }

  /**
   * Matches participation classes to their objects.
   * <p>
   * Participations can exist in either their own specified context, in which
   * case the containment hierarchy is either rooted in a Resource or, for
   * commonality participations, an intermediate model root. Or they can be
   * referenced in external commonality reference mappings, in which case the
   * containment hierarchy is rooted in one or more externally specified
   * objects that already correspond to some Intermediate. The type of this
   * Intermediate can be specified via
   * {@link ContainmentContext#setRootIntermediateType}.
   * <p>
   * All other objects that need to be matched are expected to not already
   * correspond to some Intermediate. Any objects that already correspond to
   * an Intermediate are therefore ignored.
   * <p>
   * A reference mapping may also use an attribute reference operator, in
   * which case the referenced participation uses its own declared root
   * context, but the matching needs to additionally check that the attribute
   * references are established according to that operator.
   * <p>
   * The given start object has to already reside inside the containment
   * hierarchy. It is used to find a candidate for the containment
   * hierarchy's root object, at which the matching procedure starts.
   * <p>
   * There may be multiple matching objects at every containment reference.
   * Each object choice is followed and checked if it can complete the
   * expected containment structure. The matching may therefore return
   * multiple candidate results.
   * 
   * @return candidate mappings between participation class names and their
   * 		objects
   */
  public Iterable<ParticipationObjects> matchObjects() {
    ContainmentContext.Node _attributeReferenceRootNode = this.containmentContext.getAttributeReferenceRootNode();
    boolean _tripleNotEquals = (_attributeReferenceRootNode != null);
    if (_tripleNotEquals) {
      boolean _isEmpty = this.containmentContext.getAttributeReferenceEdges().isEmpty();
      boolean _not = (!_isEmpty);
      Preconditions.checkArgument(_not, 
        "Containment context specifies an attribute reference root node but no attribute reference edges!");
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("ContainmentContext: ");
    _builder.append(this.containmentContext);
    ParticipationMatcher.logger.trace(_builder);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Start object: ");
    _builder_1.append(this.startObject);
    ParticipationMatcher.logger.trace(_builder_1);
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("followAttributeReferences: ");
    _builder_2.append(this.followAttributeReferences);
    ParticipationMatcher.logger.trace(_builder_2);
    final ContainmentContext.Node rootNode = IterableExtensions.<ContainmentContext.Node>head(this.containmentContext.getRoots());
    final Iterable<? extends EObject> candidateRootObjects = this.getCandidateRoots(this.startObject, this.followAttributeReferences);
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append("Candidate root objects: ");
    _builder_3.append(candidateRootObjects);
    ParticipationMatcher.logger.trace(_builder_3);
    final Function1<EObject, Boolean> _function = (EObject candidateRootObject) -> {
      return Boolean.valueOf(this.matchesRootNode(candidateRootObject));
    };
    final Function1<EObject, Iterable<ParticipationObjects>> _function_1 = (EObject rootObject) -> {
      ParticipationObjects rootMatch = new ParticipationObjects();
      rootMatch.addObject(rootNode.getName(), rootObject);
      return this.matchChilds(rootMatch, rootNode, rootObject, 1);
    };
    final Iterable<ParticipationObjects> candidateMatches = IterableExtensions.flatMap(IterableExtensions.filter(candidateRootObjects, _function), _function_1);
    boolean _isEmpty_1 = IterableExtensions.isEmpty(candidateMatches);
    if (_isEmpty_1) {
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("No candidate matches found.");
      ParticipationMatcher.logger.trace(_builder_4);
    }
    final Function1<ParticipationObjects, ParticipationObjects> _function_2 = (ParticipationObjects match) -> {
      StringConcatenation _builder_5 = new StringConcatenation();
      _builder_5.append("Candidate match: ");
      _builder_5.append(match);
      ParticipationMatcher.logger.trace(_builder_5);
      ContainmentContext.Node _attributeReferenceRootNode_1 = this.containmentContext.getAttributeReferenceRootNode();
      boolean _tripleNotEquals_1 = (_attributeReferenceRootNode_1 != null);
      if (_tripleNotEquals_1) {
        boolean _matchAttributeReferenceRoot = this.matchAttributeReferenceRoot(match);
        boolean _not_1 = (!_matchAttributeReferenceRoot);
        if (_not_1) {
          return null;
        }
      }
      return match;
    };
    final Function1<ParticipationObjects, Boolean> _function_3 = (ParticipationObjects match) -> {
      ContainmentContext.Node _attributeReferenceRootNode_1 = this.containmentContext.getAttributeReferenceRootNode();
      boolean _tripleNotEquals_1 = (_attributeReferenceRootNode_1 != null);
      if (_tripleNotEquals_1) {
        final Function1<ContainmentContext.OperatorEdge, Boolean> _function_4 = (ContainmentContext.OperatorEdge attributeReferenceEdge) -> {
          final boolean attributeEdgeFulfilled = this.isAttributeReferenceEdgeFulfilled(match, attributeReferenceEdge);
          StringConcatenation _builder_5 = new StringConcatenation();
          _builder_5.append("Attribute reference edge ");
          String _simpleString = attributeReferenceEdge.toSimpleString();
          _builder_5.append(_simpleString);
          _builder_5.append(" fulfilled: ");
          _builder_5.append(attributeEdgeFulfilled);
          ParticipationMatcher.logger.trace(_builder_5);
          return Boolean.valueOf(attributeEdgeFulfilled);
        };
        return Boolean.valueOf(IterableExtensions.<ContainmentContext.OperatorEdge>forall(this.containmentContext.getAttributeReferenceEdges(), _function_4));
      } else {
        return Boolean.valueOf(true);
      }
    };
    return IterableExtensions.<ParticipationObjects>filter(IterableExtensions.<ParticipationObjects>filterNull(IterableExtensions.<ParticipationObjects, ParticipationObjects>map(candidateMatches, _function_2)), _function_3);
  }

  private boolean matchesRootNode(final EObject candidateRootObject) {
    final ContainmentContext.Node rootNode = IterableExtensions.<ContainmentContext.Node>head(this.containmentContext.getRoots());
    EClass rootIntermediateType = null;
    ContainmentContext.Node _attributeReferenceRootNode = this.containmentContext.getAttributeReferenceRootNode();
    boolean _tripleEquals = (_attributeReferenceRootNode == null);
    if (_tripleEquals) {
      rootIntermediateType = this.containmentContext.getRootIntermediateType();
    }
    return this.matchesObject(rootNode, candidateRootObject, rootIntermediateType, 0);
  }

  private boolean matchAttributeReferenceRoot(final ParticipationObjects match) {
    final ContainmentContext.Node attributeReferenceRootNode = this.containmentContext.getAttributeReferenceRootNode();
    XtendAssertHelper.assertTrue((attributeReferenceRootNode != null));
    final ContainmentContext.OperatorEdge attributeReferenceEdge = IterableExtensions.<ContainmentContext.OperatorEdge>head(this.containmentContext.getAttributeReferenceEdges());
    XtendAssertHelper.assertTrue((attributeReferenceEdge != null));
    final EObject containedObject = match.<EObject>getObject(attributeReferenceEdge.getContained().getName());
    XtendAssertHelper.assertTrue((containedObject != null));
    EObject _object = match.<EObject>getObject(attributeReferenceRootNode.getName());
    boolean _tripleEquals = (_object == null);
    XtendAssertHelper.assertTrue(_tripleEquals);
    final IReferenceMappingOperator operator = attributeReferenceEdge.getOperator();
    final EObject containerObject = operator.getContainer(containedObject);
    if (((containerObject != null) && this.matchesObject(attributeReferenceRootNode, containerObject, 
      this.containmentContext.getRootIntermediateType(), 0))) {
      match.addObject(attributeReferenceRootNode.getName(), containerObject);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Matched attribute reference root: ");
      _builder.append(containerObject);
      ParticipationMatcher.logger.trace(_builder);
      return true;
    }
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Could not match attribute reference root: ");
    String _string = Objects.toString(containerObject);
    _builder_1.append(_string);
    ParticipationMatcher.logger.trace(_builder_1);
    return false;
  }

  private boolean isAttributeReferenceEdgeFulfilled(final ParticipationObjects match, final ContainmentContext.OperatorEdge attributeReferenceEdge) {
    final EObject containerObject = match.<EObject>getObject(attributeReferenceEdge.getContainer().getName());
    XtendAssertHelper.assertTrue((containerObject != null));
    final EObject containedObject = match.<EObject>getObject(attributeReferenceEdge.getContained().getName());
    XtendAssertHelper.assertTrue((containedObject != null));
    return attributeReferenceEdge.getOperator().isContained(containerObject, containedObject);
  }

  /**
   * Finds candidate starting objects for our matching procedure.
   * <p>
   * This searches the given object itself and its containers for the first
   * object that either already corresponds to some Intermediate (as it is
   * the case when we match participations in the context of commonality
   * reference mappings), or is an intermediate model root (as it is the case
   * when matching commonality participations rooted in their intermediate
   * model root), or is not contained in any other container (as it is the
   * case when we match non-commonality participations rooted inside a
   * Resource). In the latter case, if the object is contained inside a
   * Resource, this returns a new IntermediateResourceBridge as
   * representation of that Resource root.
   * <p>
   * If the containment context specifies an attribute reference root and we
   * found an object that already corresponds to some Intermediate, we check
   * if the object matches the expected attribute reference root and then
   * query the operators of its attribute reference edges for its contained
   * objects and continue the search for candidate root objects from there.
   */
  private Iterable<? extends EObject> getCandidateRoots(final EObject object, final boolean followAttributeReferences) {
    XtendAssertHelper.assertTrue((object != null));
    boolean _isIntermediateRoot = ParticipationMatcher.isIntermediateRoot(object);
    if (_isIntermediateRoot) {
      return Collections.<EObject>singleton(object);
    }
    boolean _isEmpty = IterableExtensions.isEmpty(Iterables.<Intermediate>filter(this.correspondenceModel.getCorrespondingEObjects(object), Intermediate.class));
    boolean _not = (!_isEmpty);
    if (_not) {
      final ContainmentContext.Node attributeReferenceRootNode = this.containmentContext.getAttributeReferenceRootNode();
      if ((attributeReferenceRootNode != null)) {
        if ((followAttributeReferences && this.matchesObject(attributeReferenceRootNode, object, 
          this.containmentContext.getRootIntermediateType(), 0))) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Found attribute reference root: ");
          _builder.append(object);
          ParticipationMatcher.logger.trace(_builder);
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("Following attribute references in order to find candidate root objects ...");
          ParticipationMatcher.logger.trace(_builder_1);
          final Function1<ContainmentContext.OperatorEdge, IReferenceMappingOperator> _function = (ContainmentContext.OperatorEdge it) -> {
            return it.getOperator();
          };
          final Function1<IReferenceMappingOperator, Iterable<? extends EObject>> _function_1 = (IReferenceMappingOperator it) -> {
            return it.getContainedObjects(object);
          };
          final Function1<EObject, Iterable<? extends EObject>> _function_2 = (EObject containedObject) -> {
            return this.getCandidateRoots(containedObject, false);
          };
          return IterableExtensions.<EObject>toSet(Iterables.<EObject>concat(IterableExtensions.<EObject, Iterable<? extends EObject>>map(IterableExtensions.<EObject>toSet(Iterables.<EObject>concat(IterableExtensions.<IReferenceMappingOperator, Iterable<? extends EObject>>map(IterableExtensions.<IReferenceMappingOperator>toSet(IterableExtensions.<ContainmentContext.OperatorEdge, IReferenceMappingOperator>map(this.containmentContext.getAttributeReferenceEdges(), _function)), _function_1))), _function_2)));
        } else {
          return Collections.<EObject>emptySet();
        }
      } else {
        return Collections.<EObject>singleton(object);
      }
    }
    EObject container = object.eContainer();
    if ((container == null)) {
      Resource _eResource = object.eResource();
      boolean _tripleNotEquals = (_eResource != null);
      if (_tripleNotEquals) {
        final IntermediateResourceBridge resourceBridge = ResourcesFactory.eINSTANCE.createIntermediateResourceBridge();
        this.setupResourceBridge(resourceBridge);
        resourceBridge.initialiseForModelElement(object);
        return Collections.<IntermediateResourceBridge>singleton(resourceBridge);
      } else {
        throw new IllegalStateException(
          ("Could not find a valid root object to start the matching at." + " The given start object is not contained inside a resource."));
      }
    }
    return this.getCandidateRoots(container, followAttributeReferences);
  }

  private static boolean isIntermediateRoot(final EObject object) {
    return object.eClass().getESuperTypes().contains(IntermediateModelBasePackage.eINSTANCE.getRoot());
  }

  private void setupResourceBridge(final IntermediateResourceBridge resourceBridge) {
    resourceBridge.setCorrespondenceModel(this.correspondenceModel);
    resourceBridge.setResourceAccess(this.resourceAccess);
  }

  /**
   * For each outgoing containment reference of the current node there may be
   * multiple matching candidate objects. Objects that have already been
   * matched are ignored. Each remaining object choice is followed and
   * checked if it can complete the expected containment structure.
   * <p>
   * The resulting candidate matches for the individual containment
   * references are combined via their cartesian product. Any combinations
   * which would match the same object to more than one node are ignored.
   * <p>
   * The result is a set of candidate matches for the current parent match
   * and node.
   */
  private Iterable<ParticipationObjects> matchChilds(final ParticipationObjects parentMatch, final ContainmentContext.Node currentNode, final EObject currentObject, final int depth) {
    final List<? extends ContainmentContext.Edge> nextContainments = IterableExtensions.toList(this.containmentContext.getContainments(currentNode));
    boolean _isEmpty = nextContainments.isEmpty();
    if (_isEmpty) {
      return Collections.<ParticipationObjects>singleton(parentMatch);
    }
    final Function1<ContainmentContext.Edge, Iterable<ParticipationObjects>> _function = (ContainmentContext.Edge containment) -> {
      List<ParticipationObjects> _list = IterableExtensions.<ParticipationObjects>toList(this.matchContainmentEdge(parentMatch, currentNode, currentObject, containment, depth));
      return ((Iterable<ParticipationObjects>) _list);
    };
    final Function2<Iterable<ParticipationObjects>, Iterable<ParticipationObjects>, Iterable<ParticipationObjects>> _function_1 = (Iterable<ParticipationObjects> prevMatches, Iterable<ParticipationObjects> nextMatches) -> {
      final Function1<ParticipationObjects, Iterable<ParticipationObjects>> _function_2 = (ParticipationObjects prevMatch) -> {
        final Function1<ParticipationObjects, Boolean> _function_3 = (ParticipationObjects nextMatch) -> {
          return Boolean.valueOf(prevMatch.canBeMerged(nextMatch));
        };
        final Function1<ParticipationObjects, ParticipationObjects> _function_4 = (ParticipationObjects nextMatch) -> {
          return prevMatch.copy().merge(nextMatch);
        };
        return IterableExtensions.<ParticipationObjects, ParticipationObjects>map(IterableExtensions.<ParticipationObjects>filter(nextMatches, _function_3), _function_4);
      };
      return IterableExtensions.<ParticipationObjects, ParticipationObjects>flatMap(prevMatches, _function_2);
    };
    return IterableExtensions.<Iterable<ParticipationObjects>>reduce(ListExtensions.map(nextContainments, _function), _function_1);
  }

  private Iterable<ParticipationObjects> matchContainmentEdge(final ParticipationObjects parentMatch, final ContainmentContext.Node currentNode, final EObject currentObject, final ContainmentContext.Edge containment, final int depth) {
    Iterable<ParticipationObjects> _xblockexpression = null;
    {
      StringConcatenation _builder = new StringConcatenation();
      String _indent = ParticipationMatcher.indent(depth);
      _builder.append(_indent);
      _builder.append("Matching edge ");
      String _simpleString = containment.toSimpleString();
      _builder.append(_simpleString);
      _builder.append(" ...");
      ParticipationMatcher.logger.trace(_builder);
      final Function1<EObject, Boolean> _function = (EObject childObject) -> {
        boolean _xblockexpression_1 = false;
        {
          final boolean alreadyMatched = parentMatch.getObjects().contains(childObject);
          if (alreadyMatched) {
            StringConcatenation _builder_1 = new StringConcatenation();
            String _indent_1 = ParticipationMatcher.indent(depth);
            _builder_1.append(_indent_1);
            _builder_1.append("Edge ");
            String _simpleString_1 = containment.toSimpleString();
            _builder_1.append(_simpleString_1);
            _builder_1.append(": Ignoring already matched object ");
            _builder_1.append(childObject);
            ParticipationMatcher.logger.trace(_builder_1);
          } else {
            StringConcatenation _builder_2 = new StringConcatenation();
            String _indent_2 = ParticipationMatcher.indent(depth);
            _builder_2.append(_indent_2);
            _builder_2.append("Edge ");
            String _simpleString_2 = containment.toSimpleString();
            _builder_2.append(_simpleString_2);
            _builder_2.append(": Found matching object ");
            _builder_2.append(childObject);
            ParticipationMatcher.logger.trace(_builder_2);
          }
          _xblockexpression_1 = (!alreadyMatched);
        }
        return Boolean.valueOf(_xblockexpression_1);
      };
      final Function1<EObject, Iterable<ParticipationObjects>> _function_1 = (EObject childObject) -> {
        final ContainmentContext.Node childNode = containment.getContained();
        final ParticipationObjects childMatch = parentMatch.copy();
        childMatch.addObject(childNode.getName(), childObject);
        return this.matchChilds(childMatch, childNode, childObject, (depth + 1));
      };
      _xblockexpression = IterableExtensions.flatMap(IterableExtensions.filter(this.getMatchingObjects(currentObject, containment, depth), _function), _function_1);
    }
    return _xblockexpression;
  }

  private Iterable<? extends EObject> getMatchingObjects(final EObject container, final ContainmentContext.Edge containmentEdge, final int depth) {
    Iterable<? extends EObject> candidateObjects = Collections.<EObject>emptySet();
    boolean _isResourceBridge = ParticipationMatcher.isResourceBridge(container);
    if (_isResourceBridge) {
      Resource _emfResource = ((IntermediateResourceBridge) container).getEmfResource();
      EList<EObject> _contents = null;
      if (_emfResource!=null) {
        _contents=_emfResource.getContents();
      }
      candidateObjects = _contents;
    } else {
      if ((containmentEdge instanceof ContainmentContext.ReferenceEdge)) {
        final EReference reference = ((ContainmentContext.ReferenceEdge)containmentEdge).getReference();
        final Object rawValue = container.eGet(reference);
        boolean _isMany = reference.isMany();
        if (_isMany) {
          candidateObjects = ((Iterable<? extends EObject>) rawValue);
        } else {
          if ((rawValue != null)) {
            candidateObjects = Collections.<EObject>singleton(((EObject) rawValue));
          }
        }
      } else {
        if ((containmentEdge instanceof ContainmentContext.OperatorEdge)) {
          candidateObjects = ((ContainmentContext.OperatorEdge)containmentEdge).getOperator().getContainedObjects(container);
        }
      }
    }
    final ContainmentContext.Node containedNode = containmentEdge.getContained();
    final Function1<EObject, Boolean> _function = (EObject it) -> {
      return Boolean.valueOf(this.matchesObject(containedNode, it, null, depth));
    };
    return IterableExtensions.filter(candidateObjects, _function);
  }

  /**
   * Checks:
   * - Object is of expected type.
   * - If correspondingIntermediateType is specified: A corresponding
   *   Intermediate of that type has to exist.
   * - Otherwise: The object does not correspond to any Intermediate instance yet.
   */
  private boolean matchesObject(final ContainmentContext.Node node, final EObject object, final EClass correspondingIntermediateType, final int depth) {
    boolean _isInstance = node.getType().isInstance(object);
    boolean _not = (!_isInstance);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      String _indent = ParticipationMatcher.indent(depth);
      _builder.append(_indent);
      _builder.append("Node ");
      String _simpleString = node.toSimpleString();
      _builder.append(_simpleString);
      _builder.append(": Object is of wrong type ");
      _builder.append(object);
      ParticipationMatcher.logger.trace(_builder);
      return false;
    }
    boolean _isResourceBridge = ParticipationMatcher.isResourceBridge(object);
    boolean _not_1 = (!_isResourceBridge);
    if (_not_1) {
      Intermediate correspondingIntermediate = IterableExtensions.<Intermediate>head(Iterables.<Intermediate>filter(this.correspondenceModel.getCorrespondingEObjects(object), Intermediate.class));
      EClass _eClass = null;
      if (correspondingIntermediate!=null) {
        _eClass=correspondingIntermediate.eClass();
      }
      boolean _notEquals = (!com.google.common.base.Objects.equal(correspondingIntermediateType, _eClass));
      if (_notEquals) {
        if ((correspondingIntermediateType == null)) {
          StringConcatenation _builder_1 = new StringConcatenation();
          String _indent_1 = ParticipationMatcher.indent(depth);
          _builder_1.append(_indent_1);
          _builder_1.append("Node ");
          String _simpleString_1 = node.toSimpleString();
          _builder_1.append(_simpleString_1);
          _builder_1.append(": Object already corresponds to an ");
          _builder_1.append("Intermediate ");
          _builder_1.append(object);
          ParticipationMatcher.logger.trace(_builder_1);
        } else {
          StringConcatenation _builder_2 = new StringConcatenation();
          String _indent_2 = ParticipationMatcher.indent(depth);
          _builder_2.append(_indent_2);
          _builder_2.append("Node ");
          String _simpleString_2 = node.toSimpleString();
          _builder_2.append(_simpleString_2);
          _builder_2.append(": Object has no matching Intermediate ");
          _builder_2.append("correspondence ");
          _builder_2.append(object);
          ParticipationMatcher.logger.trace(_builder_2);
        }
        return false;
      }
    }
    StringConcatenation _builder_3 = new StringConcatenation();
    String _indent_3 = ParticipationMatcher.indent(depth);
    _builder_3.append(_indent_3);
    _builder_3.append("Node ");
    String _simpleString_3 = node.toSimpleString();
    _builder_3.append(_simpleString_3);
    _builder_3.append(": Found matching object ");
    _builder_3.append(object);
    ParticipationMatcher.logger.trace(_builder_3);
    return true;
  }

  private static boolean isResourceBridge(final EObject object) {
    return (object instanceof IntermediateResourceBridge);
  }

  private static String indent(final int depth) {
    return StringUtil.repeat("  ", depth);
  }
}
