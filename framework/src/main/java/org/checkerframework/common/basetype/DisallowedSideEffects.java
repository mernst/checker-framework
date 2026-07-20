package org.checkerframework.common.basetype;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.expression.FieldAccess;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.JavaExpressionParseException;
import org.checkerframework.dataflow.expression.ThisReference;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.SideEffectsOnly;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.util.StringToJavaExpression;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TreeUtils;
import org.plumelib.util.CollectionsP;
import org.plumelib.util.IPair;
import org.plumelib.util.UnionFind;

/**
 * Checks that a method side-effects no expression beyond those listed in its {@link
 * SideEffectsOnly} annotation.
 */
public class DisallowedSideEffects {

  /** This class is not instantiated; it only contains a static method. */
  private DisallowedSideEffects() {
    throw new AssertionError("Class DisallowedSideEffects cannot be instantiated.");
  }

  /**
   * Issues warnings about side effects beyond the {@code @SideEffectsOnly} annotation
   *
   * @param statement the statement to check
   * @param sideEffectsOnlyExpressions the values in the {@link SideEffectsOnly} annotation
   * @param checker the checker to use
   * @param methodTree the method, used for diagnostics
   */
  public static void checkSideEffectsOnly(
      TreePath statement,
      List<JavaExpression> sideEffectsOnlyExpressions,
      BaseTypeChecker checker,
      MethodTree methodTree) {
    DisallowedSideEffectsHelper helper =
        new DisallowedSideEffectsHelper(sideEffectsOnlyExpressions, checker);
    helper.scan(statement, null);

    for (IPair<Tree, JavaExpression> s : helper.disallowedExprs) {
      checker.reportError(
          s.first, "purity.incorrect.sideeffectsonly", methodTree.getName(), s.second.toString());
    }
  }

  /**
   * Visitor that collects mutated expressions that are not listed in a {@link SideEffectsOnly}
   * annotation.
   */
  protected static class DisallowedSideEffectsHelper extends TreePathScanner<Void, Void> {
    /**
     * The expressions the scanned method side-effects that are not listed in its {@link
     * SideEffectsOnly} annotation, each paired with the tree that side-effects it.
     */
    final List<IPair<Tree, JavaExpression>> disallowedExprs = new ArrayList<>(1);

    /**
     * List of expressions specified as annotation arguments in a {@link SideEffectsOnly}
     * annotation.
     */
    List<JavaExpression> sideEffectsOnlyExpressionsFromAnnotation;

    /**
     * Groups expressions into sets, where all the elements in each set might be aliased to one
     * other.
     */
    UnionFind<JavaExpression> aliasedExpressions =
        new UnionFind<>(null, JavaExpression::containsAsReceiver);

    /** The checker to use. */
    BaseTypeChecker checker;

    /** The {@code SideEffectsOnly.value} argument/element. */
    ExecutableElement sideEffectsOnlyValueElement;

    /**
     * Creates a new DisallowedSideEffectsHelper.
     *
     * @param sideEffectsOnlyExpressions the arguments/values of a {@link SideEffectsOnly}
     *     annotation
     * @param checker the checker to use
     */
    public DisallowedSideEffectsHelper(
        List<JavaExpression> sideEffectsOnlyExpressions, BaseTypeChecker checker) {
      this.sideEffectsOnlyExpressionsFromAnnotation = sideEffectsOnlyExpressions;
      this.checker = checker;
      // TreeUtils.getMethod throws BugInCF if there is not exactly one match, so no null check.
      this.sideEffectsOnlyValueElement =
          TreeUtils.getMethod(
              SideEffectsOnly.class, "value", 0, checker.getProcessingEnvironment());
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void aVoid) {
      Element invokedElem = TreeUtils.elementFromUse(node);
      if (invokedElem == null || TreeUtils.isEnumSuperCall(node)) {
        return super.visitMethodInvocation(node, aVoid);
      }
      AnnotatedTypeFactory atypeFactory = checker.getTypeFactory();
      boolean isMarkedPure = atypeFactory.getDeclAnnotation(invokedElem, Pure.class) != null;
      boolean isMarkedSideEffectFree =
          atypeFactory.getDeclAnnotation(invokedElem, SideEffectFree.class) != null;
      if (isMarkedPure || isMarkedSideEffectFree) {
        // TODO: Should all the checking be integrated together?
        return super.visitMethodInvocation(node, aVoid);
      }

      AnnotationMirror seOnlyAnnotation =
          atypeFactory.getDeclAnnotation(invokedElem, SideEffectsOnly.class);

      List<JavaExpression> actualSideEffectedExprs;
      if (seOnlyAnnotation != null) {
        // The invoked method modifies exactly what its annotation says it does.
        actualSideEffectedExprs = sideEffectsOnlyExpressionsAtCallSite(seOnlyAnnotation, node);
        if (actualSideEffectedExprs == null) {
          // An expression in the annotation could not be parsed at the call site, so nothing is
          // known about what the call modifies.  The parse error is reported elsewhere; see
          // `sideEffectsOnlyExpressionsAtCallSite`.
          return super.visitMethodInvocation(node, aVoid);
        }
      } else {
        // The invoked method is NOT marked with @SideEffectsOnly, so it may modify anything.
        // What does it modify?  Check the receiver and the arguments of the method invocation.
        // TODO: This is unsound.  When the call has a receiver or arguments, this assumes that an
        // unannotated method modifies only those.  In fact an unannotated method may also modify
        // static state and any other state not reachable from its receiver and arguments.
        actualSideEffectedExprs = this.getJavaExpressionsFromMethodInvocation(node);
        if (actualSideEffectedExprs.isEmpty()) {
          // The call has no receiver or arguments, so it might modify arbitrary state.
          // A different message key than `purity.incorrect.sideeffectsonly` is used because the
          // subject of this message is the callee, not the method being checked.
          checker.reportError(node, "purity.unknown.sideeffectsonly", invokedElem.getSimpleName());
        }
      }
      actualSideEffectedExprs.stream()
          .filter(this::isDisallowedSideEffectedExpression)
          .forEach(expr -> disallowedExprs.add(IPair.of(node, expr)));
      return super.visitMethodInvocation(node, aVoid);
    }

    /**
     * Returns the expressions that the invoked method side-effects, according to its {@link
     * SideEffectsOnly} annotation, view-adapted to the given call site.
     *
     * <p>Returns null if an expression in the annotation cannot be parsed at the call site. This
     * method does not report the parse error, because {@link
     * org.checkerframework.framework.flow.CFAbstractAnalysis#getSideEffectsOnlyExpressions} already
     * reports it at the same call site.
     *
     * @param seOnlyAnnotation the {@link SideEffectsOnly} annotation on the invoked method
     * @param methodInvok the call site to which the expressions are view-adapted
     * @return the expressions the invoked method side-effects, or null if one cannot be parsed
     */
    private @Nullable List<JavaExpression> sideEffectsOnlyExpressionsAtCallSite(
        AnnotationMirror seOnlyAnnotation, MethodInvocationTree methodInvok) {
      List<String> seOnlyExpressionStrings =
          AnnotationUtils.getElementValueArray(
              seOnlyAnnotation, sideEffectsOnlyValueElement, String.class);
      List<JavaExpression> result = new ArrayList<>(seOnlyExpressionStrings.size());
      for (String st : seOnlyExpressionStrings) {
        try {
          result.add(StringToJavaExpression.atMethodInvocation(st, methodInvok, checker));
        } catch (JavaExpressionParseException ex) {
          return null;
        }
      }
      return result;
    }

    /**
     * Returns the arguments to a method invocation, including the receiver.
     *
     * @param methodInvok a method invocation
     * @return the arguments to a method invocation, including the receiver
     */
    private List<JavaExpression> getJavaExpressionsFromMethodInvocation(
        MethodInvocationTree methodInvok) {
      // TODO: collect all subexpressions of the given expression.  For now it just considers the
      // actual arguments, which is incomplete.
      List<? extends ExpressionTree> args = methodInvok.getArguments();
      ExpressionTree receiver = TreeUtils.getReceiverTree(methodInvok);
      List<ExpressionTree> exprs;
      if (receiver == null) {
        // Unfortunate, unnecessary copying.
        exprs = new ArrayList<>(args);
      } else {
        exprs = new ArrayList<>(args.size() + 1);
        exprs.add(receiver);
        exprs.addAll(args);
      }
      return CollectionsP.mapList(JavaExpression::fromTree, exprs);
    }

    /**
     * Returns true if the given expression is a side-effected expression beyond what is listed in
     * the {@link SideEffectsOnly} annotation. That is, all of the following hold:
     *
     * <ul>
     *   <li>The expression's value is modifiable by other code.
     *   <li>The expression is not covered by the {@link SideEffectsOnly} annotation, in the sense
     *       of {@link #isCoveredByAnnotation}.
     * </ul>
     *
     * <p>Use this for an expression whose <em>value</em> is mutated, such as the receiver or an
     * argument of a method call. For an expression that is <em>assigned to</em>, use {@link
     * #isDisallowedAssignmentTarget}.
     *
     * @param expr the expression to check for side-effecting
     * @return true if the given expression is a side-effected expression beyond what is listed in
     *     the {@link SideEffectsOnly} annotation
     */
    private boolean isDisallowedSideEffectedExpression(JavaExpression expr) {
      return expr.isModifiableByOtherCode() && !isCoveredByAnnotation(expr);
    }

    /**
     * Returns true if assigning to the given expression is a side effect beyond what is listed in
     * the {@link SideEffectsOnly} annotation. That is, all of the following hold:
     *
     * <ul>
     *   <li>The expression is assignable by other code; equivalently, the assignment is visible
     *       outside the method being checked. (Assigning to a local variable is not.)
     *   <li>The expression is not a field of the object under construction, in the sense of {@link
     *       #isFieldOfObjectUnderConstruction}.
     *   <li>The expression is not covered by the {@link SideEffectsOnly} annotation, in the sense
     *       of {@link #isCoveredByAnnotation}.
     * </ul>
     *
     * @param expr the expression that is assigned to
     * @return true if assigning to the given expression is a side effect beyond what is listed in
     *     the {@link SideEffectsOnly} annotation
     */
    private boolean isDisallowedAssignmentTarget(JavaExpression expr) {
      return expr.isAssignableByOtherCode()
          && !isFieldOfObjectUnderConstruction(expr)
          && !isCoveredByAnnotation(expr);
    }

    /**
     * Returns true if the given expression is a field of the object that the enclosing constructor
     * is constructing; that is, the expression is a field access whose receiver is {@code this},
     * and the code being checked is in a constructor.
     *
     * <p>Assigning such a field is not an externally-visible side effect, because no other code can
     * observe the object until the constructor returns. A {@code @SideEffectFree} constructor may
     * assign its own fields, and so may a {@code @SideEffectsOnly} one, without listing {@code
     * this}.
     *
     * @param expr the expression that is assigned to
     * @return true if the given expression is a field of the object under construction
     */
    private boolean isFieldOfObjectUnderConstruction(JavaExpression expr) {
      return expr instanceof FieldAccess fieldAccess
          && fieldAccess.getReceiver() instanceof ThisReference
          && TreePathUtil.inConstructor(getCurrentPath());
    }

    /**
     * Returns true if the given expression is listed in the {@link SideEffectsOnly} annotation, is
     * a subexpression of one of those expressions, or may be aliased to one of them.
     *
     * @param expr the expression to look for
     * @return true if the given expression is covered by the {@link SideEffectsOnly} annotation
     */
    private boolean isCoveredByAnnotation(JavaExpression expr) {
      aliasedExpressions.add(expr);
      for (JavaExpression seOnlyExpr : sideEffectsOnlyExpressionsFromAnnotation) {
        aliasedExpressions.add(seOnlyExpr);
        // Argument order matters: `test` lifts the asymmetric `containsAsReceiver` relation over
        // the two elements' alias sets, and `expr` must be the potential sub-expression.
        if (aliasedExpressions.test(expr, seOnlyExpr)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void aVoid) {
      JavaExpression lhs = JavaExpression.fromTree(node.getVariable());
      JavaExpression rhs = JavaExpression.fromTree(node.getExpression());
      if (isDisallowedAssignmentTarget(lhs)) {
        disallowedExprs.add(IPair.of(node, lhs));
      }
      aliasedExpressions.union(lhs, rhs);
      return super.visitAssignment(node, aVoid);
    }

    @Override
    public Void visitVariable(VariableTree node, Void aVoid) {
      ExpressionTree initializer = node.getInitializer();
      if (initializer == null) {
        // A declaration with no initializer, such as `int x;`, creates no alias.
        return super.visitVariable(node, aVoid);
      }
      JavaExpression name = JavaExpression.fromVariableTree(node);
      JavaExpression expr = JavaExpression.fromTree(initializer);
      // `union` adds both arguments, so they need not be added first.
      aliasedExpressions.union(name, expr);
      return super.visitVariable(node, aVoid);
    }

    @Override
    public Void visitUnary(UnaryTree node, Void aVoid) {
      switch (node.getKind()) {
        case POSTFIX_INCREMENT, POSTFIX_DECREMENT, PREFIX_INCREMENT, PREFIX_DECREMENT -> {
          JavaExpression operand = JavaExpression.fromTree(node.getExpression());
          if (isDisallowedAssignmentTarget(operand)) {
            disallowedExprs.add(IPair.of(node, operand));
          }
        }
        default -> {}
      }
      return super.visitUnary(node, aVoid);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void aVoid) {
      // Does not make the left-hand side an alias of the right-hand side,
      // because the rhs expression uses the lhs.
      JavaExpression lhs = JavaExpression.fromTree(node.getVariable());
      if (isDisallowedAssignmentTarget(lhs)) {
        disallowedExprs.add(IPair.of(node, lhs));
      }
      return super.visitCompoundAssignment(node, aVoid);
    }
  }
}
