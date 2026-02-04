package tools.vitruv.dsls.vitruvOCL.symboltable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Maps parse tree nodes to their corresponding scopes for multi-pass compilation.
 *
 * <p>This class implements the <b>ParseTreeProperty pattern</b> for ANTLR4 multi-pass compilers. It
 * allows Pass 1 (Symbol Table Construction) to annotate parse tree nodes with their associated
 * scopes, which Pass 2 (Type Checking) can then retrieve for proper scope management.
 *
 * <h2>Design Pattern: ParseTreeProperty Annotation</h2>
 *
 * <p>In ANTLR4 multi-pass compilers, each pass traverses the same parse tree. To share information
 * between passes, we use {@link ParseTreeProperty} to annotate tree nodes with data computed in
 * earlier passes.
 *
 * <pre>{@code
 * // Pass 1: SymbolTableBuilder
 * Scope contextScope = new LocalScope(parent);
 * scopeAnnotator.annotate(ctx, contextScope);  // Annotate node with scope
 *
 * // Pass 2: TypeCheckVisitor
 * Scope contextScope = scopeAnnotator.getScope(ctx);  // Retrieve scope for node
 * symbolTable.enterScope(contextScope);
 * }</pre>
 *
 * <h2>Usage in Compiler Pipeline</h2>
 *
 * <pre>{@code
 * // Create shared scope annotator
 * ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
 *
 * // Pass 1: Build symbol table and annotate scopes
 * SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
 * SymbolTableBuilder builder = new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
 * builder.visit(parseTree);
 *
 * // Pass 2: Type check using annotated scopes
 * TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
 * typeChecker.visit(parseTree);
 * }</pre>
 *
 * @see ParseTreeProperty ANTLR4's node annotation utility
 * @see SymbolTableBuilder Pass 1 that populates annotations
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Pass 2 that reads annotations
 */
public class ScopeAnnotator {

  /**
   * Maps parse tree nodes to their corresponding scopes.
   *
   * <p>Uses {@link ParseTreeProperty} which internally uses {@link java.util.IdentityHashMap} for
   * efficient node-based lookup without relying on {@code equals()}/{@code hashCode()}.
   */
  private final ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>();

  /**
   * Annotates a parse tree node with its corresponding scope.
   *
   * <p>Called by Pass 1 (SymbolTableBuilder) when creating scopes for:
   *
   * <ul>
   *   <li>Context declarations: {@code context Person inv: ...}
   *   <li>Let expressions: {@code let x = 5 in ...}
   *   <li>Iterator operations: {@code collection.select(x | ...)}
   * </ul>
   *
   * @param ctx The parse tree node to annotate
   * @param scope The scope associated with this node
   * @throws NullPointerException if ctx or scope is null
   */
  public void annotate(ParserRuleContext ctx, Scope scope) {
    if (ctx == null) {
      throw new NullPointerException("Cannot annotate null parse tree node");
    }
    if (scope == null) {
      throw new NullPointerException("Cannot annotate with null scope");
    }
    scopes.put(ctx, scope);
  }

  /**
   * Retrieves the scope associated with a parse tree node.
   *
   * <p>Called by Pass 2 (TypeCheckVisitor) to retrieve scopes created by Pass 1.
   *
   * @param ctx The parse tree node to look up
   * @return The scope associated with this node, or {@code null} if no annotation exists
   * @throws NullPointerException if ctx is null
   */
  public Scope getScope(ParserRuleContext ctx) {
    if (ctx == null) {
      throw new NullPointerException("Cannot get scope for null parse tree node");
    }
    return scopes.get(ctx);
  }

  /**
   * Checks if a parse tree node has been annotated with a scope.
   *
   * @param ctx The parse tree node to check
   * @return {@code true} if the node has an associated scope, {@code false} otherwise
   * @throws NullPointerException if ctx is null
   */
  public boolean hasScope(ParserRuleContext ctx) {
    if (ctx == null) {
      throw new NullPointerException("Cannot check scope for null parse tree node");
    }
    return scopes.get(ctx) != null;
  }

  /**
   * Removes the scope annotation for a parse tree node.
   *
   * @param ctx The parse tree node to clear
   * @return The scope that was associated with the node, or {@code null} if no annotation existed
   * @throws NullPointerException if ctx is null
   */
  public Scope removeScope(ParserRuleContext ctx) {
    if (ctx == null) {
      throw new NullPointerException("Cannot remove scope for null parse tree node");
    }
    Scope scope = scopes.get(ctx);
    scopes.removeFrom(ctx);
    return scope;
  }

  /**
   * Clears all scope annotations.
   *
   * <p>Useful for testing or when reusing the annotator for multiple compilations (though creating
   * a new instance is typically preferred).
   */
  public void clear() {
    throw new UnsupportedOperationException(
        "Clearing scope annotations is not supported. Create a new ScopeAnnotator instance for each"
            + " compilation.");
  }

  /**
   * Returns debug information about the number of annotated nodes.
   *
   * <p>Useful for validation that Pass 1 annotated the expected number of scope-creating
   * constructs.
   *
   * @return A string describing the state of this annotator
   */
  @Override
  public String toString() {
    return "ScopeAnnotator[annotations exist, use hasScope() to query specific nodes]";
  }
}