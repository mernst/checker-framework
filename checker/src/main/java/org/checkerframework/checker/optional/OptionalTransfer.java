package org.checkerframework.checker.optional;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.optional.qual.Present;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGLambda;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.TreeUtils;

/** The transfer function for the Optional Checker. */
public class OptionalTransfer extends CFTransfer {

  /** The @{@link Present} annotation. */
  public final AnnotationMirror PRESENT;

  /** The element for java.util.Optional.ifPresent(). */
  private final ExecutableElement optionalIfPresent;

  /** The element for java.util.Optional.ifPresentOrElse(). */
  private final ExecutableElement optionalIfPresentOrElse;

  /** The type factory associated with this transfer function. */
  private final AnnotatedTypeFactory atypeFactory;

  /**
   * Create an OptionalTransfer.
   *
   * @param analysis the Optional Checker instance
   */
  public OptionalTransfer(CFAnalysis analysis) {
    super(analysis);
    atypeFactory = analysis.getTypeFactory();
    Elements elements = atypeFactory.getElementUtils();
    PRESENT = AnnotationBuilder.fromClass(elements, Present.class);
    ProcessingEnvironment env = atypeFactory.getProcessingEnv();
    optionalIfPresent = TreeUtils.getMethod("java.util.Optional", "ifPresent", 1, env);
    optionalIfPresentOrElse = TreeUtils.getMethod("java.util.Optional", "ifPresentOrElse", 2, env);
  }

  @Override
  public CFStore initialStore(UnderlyingAST underlyingAST, List<LocalVariableNode> parameters) {

    List<CFValue> paramValues = null;

    // This is WRONG!  I want to refine the receiver of `ifPresent()`, not the param of the lambda.
    // But, this code would be useful to the Nullness Checker, to handle
    // Jodd's `StringUtil.ifNotNull()` which also takes a function as an argument, and many methods
    // in the Checker Framework's own `Opt` class.
    /*
    if (underlyingAST.getKind() == UnderlyingAST.Kind.LAMBDA) {
      CFGLambda cfgLambda = (CFGLambda) underlyingAST;
      LambdaExpressionTree lambdaTree = cfgLambda.getLambdaTree();
      List<? extends VariableTree> lambdaParams = lambdaTree.getParameters();
      if (lambdaParams.size() == 1) {
        TreePath lambdaPath = atypeFactory.getPath(lambdaTree);
        Tree lambdaParent = lambdaPath.getParentPath().getLeaf();
        if (lambdaParent.getKind() == Tree.Kind.METHOD_INVOCATION) {
          MethodInvocationTree invok = (MethodInvocationTree) lambdaParent;
          ExecutableElement methodElt = TreeUtils.elementFromUse(invok);
          if (methodElt.equals(optionalIfPresent)
              || methodElt.equals(optionalIfPresentOrElse)) {
            // `underlyingAST` is an invocation of Optional.IfPresent() or Optional.ifPresentOrElse().
            AnnotatedTypeMirror paramAtm = atypeFactory.getAnnotatedType(lambdaParams.get(0));
            paramAtm.replaceAnnotation(PRESENT);
            paramValues = Collections.singletonList(analysis.createAbstractValue(paramAtm));
          }
        }
      }
    }
    */

    CFStore result = super.initialStore(underlyingAST, parameters, paramValues);

    if (underlyingAST.getKind() == UnderlyingAST.Kind.LAMBDA) {
      CFGLambda cfgLambda = (CFGLambda) underlyingAST;
      LambdaExpressionTree lambdaTree = cfgLambda.getLambdaTree();
      List<? extends VariableTree> lambdaParams = lambdaTree.getParameters();
      if (lambdaParams.size() == 1) {
        TreePath lambdaPath = atypeFactory.getPath(lambdaTree);
        Tree lambdaParent = lambdaPath.getParentPath().getLeaf();
        if (lambdaParent.getKind() == Tree.Kind.METHOD_INVOCATION) {
          MethodInvocationTree invok = (MethodInvocationTree) lambdaParent;
          ExecutableElement methodElt = TreeUtils.elementFromUse(invok);
          if (methodElt.equals(optionalIfPresent) || methodElt.equals(optionalIfPresentOrElse)) {
            // `underlyingAST` is an invocation of Optional.IfPresent() or
            // Optional.ifPresentOrElse().  In the lambda, the receiver is @Present.
            ExpressionTree methodSelectTree = TreeUtils.withoutParens(invok.getMethodSelect());
            ExpressionTree receiverTree = ((MemberSelectTree) methodSelectTree).getExpression();
            JavaExpression receiverJe = JavaExpression.fromTree(receiverTree);
            result.insertValue(receiverJe, PRESENT);
          }
        }
      }
    }

    return result;
  }
}
