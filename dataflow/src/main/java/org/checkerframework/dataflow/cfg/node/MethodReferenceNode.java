package org.checkerframework.dataflow.cfg.node;

import com.sun.source.tree.MemberReferenceTree;
import java.util.Arrays;
import java.util.Collection;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.javacutil.TreeUtils;

/**
 * A node for a binary expression.
 *
 * <p>For example:
 *
 * <pre>
 *   <em>expression</em> :: <em>methodName</em>
 * </pre>
 */
public abstract class MethodReferenceNode extends Node {

  /** What precedes "::", which javac calls the "qualifier expression. */
  protected final Node scope;

  /** The method name, or "new". */
  protected final String methodName;

  /**
   * Creates a new MethodReferenceNode.
   *
   * @param tree the tree from which the new MethodReferenceNode is created
   */
  protected MethodReferenceNode(MemberReferenceTree tree, Node scope) {
    super(TreeUtils.typeOf(tree));
    this.scope = scope;
    this.methodName = tree.getName().toString();
  }

  /**
   * Returns the scope: what precedes "::".
   *
   * @returns the scope
   */
  public Node getScope() {
    return scope;
  }

  /**
   * Returns the method name: what follows "::", and may be "new".
   *
   * @returns the method name
   */
  public String getMethodName() {
    return methodName;
  }

  @Override
  @SideEffectFree
  public Collection<Node> getOperands() {
    return Arrays.asList(getScope());
  }
}
