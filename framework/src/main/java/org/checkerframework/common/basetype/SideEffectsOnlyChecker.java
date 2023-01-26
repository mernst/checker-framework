package org.checkerframework.common.basetype;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.SideEffectsOnly;
import org.checkerframework.framework.util.JavaExpressionParseUtil;
import org.checkerframework.framework.util.StringToJavaExpression;
import org.checkerframework.javacutil.AnnotationProvider;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

/**
 * For methods annotated with {@link SideEffectsOnly}, computes expressions that are side-effected
 * but not permitted by the annotation.
 */
public class SideEffectsOnlyChecker {

  /** Creates a SideEffectsOnlyAnnoChecker. */
  public SideEffectsOnlyChecker() {}

  /**
   * Returns the computed {@code SideEffectsOnlyResult}.
   *
   * @param statement The statement to check
   * @param annoProvider The annotation provider
   * @param sideEffectsOnlyExpressions List of JavaExpressions that are provided as annotation
   *     values to {@link SideEffectsOnly}
   * @param processingEnv The processing environment
   * @param checker The checker to use
   * @return SideEffectsOnlyResult returns the result of {@link SideEffectsOnlyChecker}
   */
  public static SideEffectsOnlyResult checkSideEffectsOnly(
      TreePath statement,
      AnnotationProvider annoProvider,
      List<JavaExpression> sideEffectsOnlyExpressions,
      ProcessingEnvironment processingEnv,
      BaseTypeChecker checker) {
    SideEffectsOnlyCheckerHelper helper =
        new SideEffectsOnlyCheckerHelper(
            annoProvider, sideEffectsOnlyExpressions, processingEnv, checker);
    helper.scan(statement, null);
    return helper.sideEffectsOnlyResult;
  }

  /**
   * Result of the {@link SideEffectsOnlyChecker}. Can be queried to get the list of mutated
   * expressions.
   */
  public static class SideEffectsOnlyResult {

    /** Creates a SideEffectsOnlyResult. */
    public SideEffectsOnlyResult() {}

    /**
     * List of expressions a method side-effects that are not specified in the list of arguments to
     * {@link SideEffectsOnly}.
     */
    protected final List<Pair<Tree, JavaExpression>> mutatedExprs = new ArrayList<>(1);

    /**
     * Adds {@code t} and {@code javaExpr} as a Pair to mutatedExprs.
     *
     * @param t The expression that is mutated
     * @param javaExpr The corresponding Java expression that is mutated
     */
    public void addMutatedExpr(Tree t, JavaExpression javaExpr) {
      mutatedExprs.add(Pair.of(t, javaExpr));
    }

    /**
     * Returns {@code mutatedExprs}.
     *
     * @return mutatedExprs
     */
    public List<Pair<Tree, JavaExpression>> getSeOnlyResult() {
      return mutatedExprs;
    }
  }

  /**
   * Class that visits that visits various nodes and computes mutated expressions that are not
   * specified as annotation values to {@link SideEffectsOnly}.
   */
  protected static class SideEffectsOnlyCheckerHelper extends TreePathScanner<Void, Void> {
    /** Result computed by SideEffectsOnlyCheckerHelper. */
    SideEffectsOnlyResult sideEffectsOnlyResult = new SideEffectsOnlyResult();
    /**
     * List of expressions specified as annotation arguments in {@link SideEffectsOnly} annotation.
     */
    List<JavaExpression> sideEffectsOnlyExpressions;

    /** The annotation provider. */
    protected final AnnotationProvider annoProvider;
    /** The processing environment. */
    ProcessingEnvironment processingEnv;
    /** The checker to use. */
    BaseTypeChecker checker;

    /**
     * Constructor for SideEffectsOnlyCheckerHelper.
     *
     * @param annoProvider The annotation provider
     * @param sideEffectsOnlyExpressions List of JavaExpressions that are provided as annotation
     *     values to {@link SideEffectsOnly}
     * @param processingEnv The processing environment
     * @param checker The checker to use
     */
    public SideEffectsOnlyCheckerHelper(
        AnnotationProvider annoProvider,
        List<JavaExpression> sideEffectsOnlyExpressions,
        ProcessingEnvironment processingEnv,
        BaseTypeChecker checker) {
      this.annoProvider = annoProvider;
      this.sideEffectsOnlyExpressions = sideEffectsOnlyExpressions;
      this.processingEnv = processingEnv;
      this.checker = checker;
    }

    @Override
    public Void visitCatch(CatchTree node, Void aVoid) {
      return super.visitCatch(node, aVoid);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void aVoid) {
      Element treeElem = TreeUtils.elementFromUse(node);
      AnnotationMirror pureAnno = annoProvider.getDeclAnnotation(treeElem, Pure.class);
      AnnotationMirror sideEffectFreeAnno =
          annoProvider.getDeclAnnotation(treeElem, SideEffectFree.class);
      // If the invoked method is annotated as @Pure or @SideEffectFree, nothing to do.
      if (pureAnno != null || sideEffectFreeAnno != null) {
        return super.visitMethodInvocation(node, aVoid);
      }

      AnnotationMirror sideEffectsOnlyAnno =
          annoProvider.getDeclAnnotation(treeElem, SideEffectsOnly.class);
      // The invoked method is not annotated with @SideEffectsOnly; report an error.
      if (sideEffectsOnlyAnno == null) {
        checker.reportError(node, "purity.incorrect.sideeffectsonly", node);
      } else {
        // The invoked method is annotated with @SideEffectsOnly.
        // Add annotation values to seOnlyIncorrectExprs
        // that are not present in sideEffectsOnlyExpressions.
        ExecutableElement sideEffectsOnlyValueElement =
            TreeUtils.getMethod(SideEffectsOnly.class, "value", 0, processingEnv);
        List<String> sideEffectsOnlyExpressionStrings =
            AnnotationUtils.getElementValueArray(
                sideEffectsOnlyAnno, sideEffectsOnlyValueElement, String.class);
        List<JavaExpression> sideEffectsOnlyExprInv = new ArrayList<>();
        for (String st : sideEffectsOnlyExpressionStrings) {
          try {
            JavaExpression exprJe = StringToJavaExpression.atMethodInvocation(st, node, checker);
            sideEffectsOnlyExprInv.add(exprJe);
          } catch (JavaExpressionParseUtil.JavaExpressionParseException ex) {
            checker.report(st, ex.getDiagMessage());
          }
        }

        for (JavaExpression expr : sideEffectsOnlyExprInv) {
          if (!sideEffectsOnlyExpressions.contains(expr)) {
            sideEffectsOnlyResult.addMutatedExpr(node, expr);
          }
        }
      }
      return super.visitMethodInvocation(node, aVoid);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void aVoid) {
      return super.visitNewClass(node, aVoid);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void aVoid) {
      JavaExpression javaExpr = JavaExpression.fromTree(node.getVariable());
      if (!sideEffectsOnlyExpressions.contains(javaExpr)) {
        sideEffectsOnlyResult.addMutatedExpr(node, javaExpr);
      }
      return super.visitAssignment(node, aVoid);
    }

    @Override
    public Void visitUnary(UnaryTree node, Void aVoid) {
      return super.visitUnary(node, aVoid);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void aVoid) {
      return super.visitCompoundAssignment(node, aVoid);
    }
  }
}
