package tools.vitruv.dsls.commonalities.runtime.matching;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtend.lib.annotations.ToString;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.ReferenceMappingOperatorHelper;

/**
 * Represents a participation's containment context.
 * <p>
 * Participation objects are represented as nodes with names and types
 * according to their participation classes. Containment relations are
 * represented as directed edges between nodes. Each edge provides either the
 * EReference that realizes the containment relationship, or an operator that
 * can be queried for the contained objects.
 * <p>
 * The containment context of a participation can be thought of as a forest
 * (i.e. a collection of disjoint containment trees). In most cases the
 * containment context will only consist of a single containment tree. However,
 * in the context of commonality references it is possible to define multiple
 * reference mappings for the same participation. If these mappings specify
 * different reference containers, the containment context will contain
 * multiple root nodes.
 * <p>
 * A containment relation can also be defined implicitly according to the
 * attributes of the involved objects (eg. in Java sub-packages exist
 * independently from each other and their 'containment' relationship is
 * expressed implicitly by their namespaces and name attributes). We support
 * the containment context to define one additional (external) root node which
 * is connected by such attribute references to nodes of the participation's
 * main containment tree.
 * <p>
 * Assumptions:
 * <ul>
 * <li>Each object is contained by at most one other object (with the exception
 * of objects 'contained' via attribute references).
 * <li>No cyclic containments.
 * <li>No self containment (no loops).
 * <li>There is at most one root container (i.e. Resource) per participation.
 * TODO Support multiple root containers?
 * </ul>
 */
@ToString
@SuppressWarnings("all")
public class ContainmentContext {
  @Data
  public static class Node {
    private final String name;

    private final EClass type;

    private final String correspondenceTag;

    public String toSimpleString() {
      return this.name;
    }

    public Node(final String name, final EClass type, final String correspondenceTag) {
      super();
      this.name = name;
      this.type = type;
      this.correspondenceTag = correspondenceTag;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.name== null) ? 0 : this.name.hashCode());
      result = prime * result + ((this.type== null) ? 0 : this.type.hashCode());
      return prime * result + ((this.correspondenceTag== null) ? 0 : this.correspondenceTag.hashCode());
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
      ContainmentContext.Node other = (ContainmentContext.Node) obj;
      if (this.name == null) {
        if (other.name != null)
          return false;
      } else if (!this.name.equals(other.name))
        return false;
      if (this.type == null) {
        if (other.type != null)
          return false;
      } else if (!this.type.equals(other.type))
        return false;
      if (this.correspondenceTag == null) {
        if (other.correspondenceTag != null)
          return false;
      } else if (!this.correspondenceTag.equals(other.correspondenceTag))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("name", this.name);
      b.add("type", this.type);
      b.add("correspondenceTag", this.correspondenceTag);
      return b.toString();
    }

    @Pure
    public String getName() {
      return this.name;
    }

    @Pure
    public EClass getType() {
      return this.type;
    }

    @Pure
    public String getCorrespondenceTag() {
      return this.correspondenceTag;
    }
  }

  public interface Edge {
    ContainmentContext.Node getContainer();

    ContainmentContext.Node getContained();

    String toSimpleString();
  }

  @Data
  public static class ReferenceEdge implements ContainmentContext.Edge {
    private final ContainmentContext.Node container;

    private final ContainmentContext.Node contained;

    private final EReference reference;

    @Override
    public String toSimpleString() {
      StringConcatenation _builder = new StringConcatenation();
      String _simpleString = this.container.toSimpleString();
      _builder.append(_simpleString);
      _builder.append("[");
      String _name = this.reference.getName();
      _builder.append(_name);
      _builder.append("] -> ");
      String _simpleString_1 = this.contained.toSimpleString();
      _builder.append(_simpleString_1);
      return _builder.toString();
    }

    public ReferenceEdge(final ContainmentContext.Node container, final ContainmentContext.Node contained, final EReference reference) {
      super();
      this.container = container;
      this.contained = contained;
      this.reference = reference;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.container== null) ? 0 : this.container.hashCode());
      result = prime * result + ((this.contained== null) ? 0 : this.contained.hashCode());
      return prime * result + ((this.reference== null) ? 0 : this.reference.hashCode());
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
      ContainmentContext.ReferenceEdge other = (ContainmentContext.ReferenceEdge) obj;
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
      if (this.reference == null) {
        if (other.reference != null)
          return false;
      } else if (!this.reference.equals(other.reference))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("container", this.container);
      b.add("contained", this.contained);
      b.add("reference", this.reference);
      return b.toString();
    }

    @Pure
    @Override
    public ContainmentContext.Node getContainer() {
      return this.container;
    }

    @Pure
    @Override
    public ContainmentContext.Node getContained() {
      return this.contained;
    }

    @Pure
    public EReference getReference() {
      return this.reference;
    }
  }

  @Data
  public static class OperatorEdge implements ContainmentContext.Edge {
    private final ContainmentContext.Node container;

    private final ContainmentContext.Node contained;

    private final IReferenceMappingOperator operator;

    @Override
    public String toSimpleString() {
      StringConcatenation _builder = new StringConcatenation();
      String _simpleString = this.container.toSimpleString();
      _builder.append(_simpleString);
      _builder.append("[operator: ");
      String _simpleName = this.operator.getClass().getSimpleName();
      _builder.append(_simpleName);
      _builder.append("] -> ");
      String _simpleString_1 = this.contained.toSimpleString();
      _builder.append(_simpleString_1);
      return _builder.toString();
    }

    public OperatorEdge(final ContainmentContext.Node container, final ContainmentContext.Node contained, final IReferenceMappingOperator operator) {
      super();
      this.container = container;
      this.contained = contained;
      this.operator = operator;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.container== null) ? 0 : this.container.hashCode());
      result = prime * result + ((this.contained== null) ? 0 : this.contained.hashCode());
      return prime * result + ((this.operator== null) ? 0 : this.operator.hashCode());
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
      ContainmentContext.OperatorEdge other = (ContainmentContext.OperatorEdge) obj;
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
      if (this.operator == null) {
        if (other.operator != null)
          return false;
      } else if (!this.operator.equals(other.operator))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      ToStringBuilder b = new ToStringBuilder(this);
      b.add("container", this.container);
      b.add("contained", this.contained);
      b.add("operator", this.operator);
      return b.toString();
    }

    @Pure
    @Override
    public ContainmentContext.Node getContainer() {
      return this.container;
    }

    @Pure
    @Override
    public ContainmentContext.Node getContained() {
      return this.contained;
    }

    @Pure
    public IReferenceMappingOperator getOperator() {
      return this.operator;
    }
  }

  private final Map<String, ContainmentContext.Node> nodesByName = new LinkedHashMap<String, ContainmentContext.Node>();

  private final Set<ContainmentContext.Edge> edges = new LinkedHashSet<ContainmentContext.Edge>();

  private final Set<ContainmentContext.Node> roots = new LinkedHashSet<ContainmentContext.Node>();

  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private EClass rootIntermediateType = null;

  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private ContainmentContext.Node attributeReferenceRootNode = null;

  private Set<ContainmentContext.OperatorEdge> attributeReferenceEdges = new HashSet<ContainmentContext.OperatorEdge>();

  private final transient Set<ContainmentContext.OperatorEdge> attributeReferenceEdgesView = Collections.<ContainmentContext.OperatorEdge>unmodifiableSet(this.attributeReferenceEdges);

  public ContainmentContext() {
  }

  private void commonPreValidateNode(final String name, final EClass type) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(name);
    boolean _not = (!_isNullOrEmpty);
    Preconditions.checkArgument(_not, "name is null or empty");
    Preconditions.<EClass>checkNotNull(type, "type is null");
    boolean _and = false;
    boolean _containsKey = this.nodesByName.containsKey(name);
    boolean _not_1 = (!_containsKey);
    if (!_not_1) {
      _and = false;
    } else {
      String _name = null;
      if (this.attributeReferenceRootNode!=null) {
        _name=this.attributeReferenceRootNode.name;
      }
      boolean _notEquals = (!Objects.equal(name, _name));
      _and = _notEquals;
    }
    Preconditions.checkArgument(_and, 
      ("There already exists a node with this name: " + name));
  }

  public ContainmentContext.Node addNode(final String name, final EClass type, final String correspondenceTag) {
    this.commonPreValidateNode(name, type);
    final ContainmentContext.Node newNode = new ContainmentContext.Node(name, type, correspondenceTag);
    this.nodesByName.put(name, newNode);
    this.roots.clear();
    return newNode;
  }

  private void validateNodeExists(final String nodeName) {
    Preconditions.checkArgument(this.nodesByName.containsKey(nodeName), ("There is no node with this name: " + nodeName));
  }

  private void commonPreValidateEdge(final String containerName, final String containedName) {
    this.validateNodeExists(containerName);
    this.validateNodeExists(containedName);
    boolean _notEquals = (!Objects.equal(containerName, containedName));
    Preconditions.checkArgument(_notEquals, "Container cannot contain itself (no loops)");
  }

  private void addEdge(final ContainmentContext.Edge newEdge) {
    boolean _contains = this.edges.contains(newEdge);
    boolean _not = (!_contains);
    Preconditions.checkArgument(_not, "The edge already exists");
    this.edges.add(newEdge);
    this.roots.clear();
  }

  public ContainmentContext.ReferenceEdge addReferenceEdge(final String containerName, final String containedName, final EReference reference) {
    this.commonPreValidateEdge(containerName, containedName);
    final ContainmentContext.Node container = this.nodesByName.get(containerName);
    final ContainmentContext.Node contained = this.nodesByName.get(containedName);
    XtendAssertHelper.assertTrue(((container != null) && (contained != null)));
    Preconditions.<EReference>checkNotNull(reference, "Containment reference is null");
    Preconditions.checkArgument(reference.isContainment(), "The given reference is not a containment");
    Preconditions.checkArgument(container.type.getEAllReferences().contains(reference), 
      "Containment reference belongs to a different container class");
    final ContainmentContext.ReferenceEdge newEdge = new ContainmentContext.ReferenceEdge(container, contained, reference);
    this.addEdge(newEdge);
    return newEdge;
  }

  public ContainmentContext.OperatorEdge addOperatorEdge(final String containerName, final String containedName, final IReferenceMappingOperator operator) {
    this.commonPreValidateEdge(containerName, containedName);
    final ContainmentContext.Node container = this.nodesByName.get(containerName);
    final ContainmentContext.Node contained = this.nodesByName.get(containedName);
    XtendAssertHelper.assertTrue(((container != null) && (contained != null)));
    Preconditions.<IReferenceMappingOperator>checkNotNull(operator, "Operator is null");
    boolean _isAttributeReference = ReferenceMappingOperatorHelper.isAttributeReference(operator);
    boolean _not = (!_isAttributeReference);
    Preconditions.checkArgument(_not, 
      "Operator edge cannot use an attribute reference operator!");
    final ContainmentContext.OperatorEdge newEdge = new ContainmentContext.OperatorEdge(container, contained, operator);
    this.addEdge(newEdge);
    return newEdge;
  }

  public EClass setRootIntermediateType(final EClass type) {
    EClass _xblockexpression = null;
    {
      Preconditions.checkState((this.rootIntermediateType == null), "The root intermediate type has already been set!");
      _xblockexpression = this.rootIntermediateType = type;
    }
    return _xblockexpression;
  }

  public ContainmentContext.Node setAttributeReferenceRootNode(final String name, final EClass type, final String correspondenceTag) {
    Preconditions.checkState((this.attributeReferenceRootNode == null), 
      "The attribute reference root node has already been set!");
    this.commonPreValidateNode(name, type);
    ContainmentContext.Node _node = new ContainmentContext.Node(name, type, correspondenceTag);
    this.attributeReferenceRootNode = _node;
    return this.attributeReferenceRootNode;
  }

  public ContainmentContext.OperatorEdge addAttributeReferenceEdge(final String containedName, final IReferenceMappingOperator operator) {
    Preconditions.checkState((this.attributeReferenceRootNode != null), 
      "The attribute reference root node has not yet been set!");
    this.validateNodeExists(containedName);
    boolean _notEquals = (!Objects.equal(containedName, this.attributeReferenceRootNode.name));
    Preconditions.checkArgument(_notEquals, 
      "The attribute reference root node cannot contain itself (no loops)");
    Preconditions.checkArgument(ReferenceMappingOperatorHelper.isAttributeReference(operator), 
      "Attribute reference edge needs to use an attribute reference operator!");
    final ContainmentContext.Node contained = this.nodesByName.get(containedName);
    final ContainmentContext.Node container = this.attributeReferenceRootNode;
    final ContainmentContext.OperatorEdge newEdge = new ContainmentContext.OperatorEdge(container, contained, operator);
    boolean _contains = this.attributeReferenceEdges.contains(newEdge);
    boolean _not = (!_contains);
    Preconditions.checkArgument(_not, "The edge already exists");
    this.attributeReferenceEdges.add(newEdge);
    return newEdge;
  }

  /**
   * Gets the root nodes.
   * 
   * @return the root nodes, or empty if there are no nodes at all
   */
  public Set<ContainmentContext.Node> getRoots() {
    boolean _isEmpty = this.roots.isEmpty();
    if (_isEmpty) {
      final Function1<ContainmentContext.Node, ContainmentContext.Node> _function = (ContainmentContext.Node it) -> {
        return this.findRoot(it);
      };
      Iterable<ContainmentContext.Node> _map = IterableExtensions.<ContainmentContext.Node, ContainmentContext.Node>map(this.nodesByName.values(), _function);
      Iterables.<ContainmentContext.Node>addAll(this.roots, _map);
    }
    return this.roots;
  }

  /**
   * Finds the root node starting at the given node.
   * 
   * @param start
   * 		the start node, not <code>null</code>
   * @return the root node, not <code>null</code>
   */
  private ContainmentContext.Node findRoot(final ContainmentContext.Node start) {
    ContainmentContext.Node current = start;
    ContainmentContext.Node container = this.getContainer(current);
    while ((container != null)) {
      {
        current = container;
        container = this.getContainer(current);
      }
    }
    return current;
  }

  /**
   * Gets the container node of the given node.
   * 
   * @return The container node, or <code>null</code> if the given node is
   * 		<code>null</code>, does not belong to this containment context, or
   * 		has no container.
   */
  public ContainmentContext.Node getContainer(final ContainmentContext.Node node) {
    boolean _equals = Objects.equal(node, this.attributeReferenceRootNode);
    if (_equals) {
      return null;
    }
    final Function1<ContainmentContext.Edge, Boolean> _function = (ContainmentContext.Edge it) -> {
      ContainmentContext.Node _contained = it.getContained();
      return Boolean.valueOf(Objects.equal(_contained, node));
    };
    ContainmentContext.Edge _findFirst = IterableExtensions.<ContainmentContext.Edge>findFirst(this.edges, _function);
    ContainmentContext.Node _container = null;
    if (_findFirst!=null) {
      _container=_findFirst.getContainer();
    }
    return _container;
  }

  public ContainmentContext.Node getNode(final String name) {
    String _name = null;
    if (this.attributeReferenceRootNode!=null) {
      _name=this.attributeReferenceRootNode.name;
    }
    boolean _equals = Objects.equal(name, _name);
    if (_equals) {
      return this.attributeReferenceRootNode;
    }
    return this.nodesByName.get(name);
  }

  /**
   * Gets the containment edges of the given node.
   */
  public Iterable<? extends ContainmentContext.Edge> getContainments(final ContainmentContext.Node node) {
    boolean _equals = Objects.equal(node, this.attributeReferenceRootNode);
    if (_equals) {
      return this.attributeReferenceEdges;
    }
    final Function1<ContainmentContext.Edge, Boolean> _function = (ContainmentContext.Edge it) -> {
      ContainmentContext.Node _container = it.getContainer();
      return Boolean.valueOf(Objects.equal(_container, node));
    };
    return IterableExtensions.<ContainmentContext.Edge>filter(this.edges, _function);
  }

  /**
   * Gets a view on the attribute reference edges.
   */
  public Set<ContainmentContext.OperatorEdge> getAttributeReferenceEdges() {
    return this.attributeReferenceEdgesView;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("nodesByName", this.nodesByName);
    b.add("edges", this.edges);
    b.add("roots", this.roots);
    b.add("rootIntermediateType", this.rootIntermediateType);
    b.add("attributeReferenceRootNode", this.attributeReferenceRootNode);
    b.add("attributeReferenceEdges", this.attributeReferenceEdges);
    return b.toString();
  }

  @Pure
  public EClass getRootIntermediateType() {
    return this.rootIntermediateType;
  }

  @Pure
  public ContainmentContext.Node getAttributeReferenceRootNode() {
    return this.attributeReferenceRootNode;
  }

  public void setAttributeReferenceRootNode(final ContainmentContext.Node attributeReferenceRootNode) {
    this.attributeReferenceRootNode = attributeReferenceRootNode;
  }
}
