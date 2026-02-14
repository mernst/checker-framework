package org.checkerframework.framework.ajava;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreeScanner;
import org.checkerframework.javacutil.SystemUtil;

/**
 * A visitor that performs some default action on a tree and then all of its children. To use this
 * class, override {@code defaultAction}.
 */
public abstract class TreeScannerWithDefaults extends TreeScanner<Void, Void> {

  /**
   * Action performed on each visited tree.
   *
   * @param tree tree to perform action on
   */
  public abstract void defaultAction(Tree tree);

  @Override
  public R scan(Tree tree, P p) {
    if (tree != null && SystemUtil.jreVersion >= 14) {
      switch (tree.getKind().name()) {
        case "SWITCH_EXPRESSION":
          visitSwitchExpression(tree, unused);
          return null;
        case "YIELD":
          visitYield(tree, unused);
          return null;
        case "BINDING_PATTERN":
          visitBindingPattern(tree, unused);
          return null;
      }
    }
    return super.scan(tree, unused);
  }

  @Override
  public R visitCompilationUnit(CompilationUnitTree node, P p) {
    defaultAction(tree);
    return super.visitCompilationUnit(tree, p);
  }

  @Override
  public R visitPackage(PackageTree node, P p) {
    defaultAction(tree);
    return super.visitPackage(tree, p);
  }

  @Override
  public R visitImport(ImportTree node, P p) {
    defaultAction(tree);
    return super.visitImport(tree, p);
  }

  @Override
  public R visitClass(ClassTree node, P p) {
    defaultAction(tree);
    return super.visitClass(tree, p);
  }

  @Override
  public R visitMethod(MethodTree node, P p) {
    defaultAction(tree);
    return super.visitMethod(tree, p);
  }

  @Override
  public R visitVariable(VariableTree node, P p) {
    defaultAction(tree);
    return super.visitVariable(tree, p);
  }

  @Override
  public R visitEmptyStatement(EmptyStatementTree node, P p) {
    defaultAction(tree);
    return super.visitEmptyStatement(tree, p);
  }

  @Override
  public R visitBlock(BlockTree node, P p) {
    defaultAction(tree);
    return super.visitBlock(tree, p);
  }

  @Override
  public R visitDoWhileLoop(DoWhileLoopTree node, P p) {
    defaultAction(tree);
    return super.visitDoWhileLoop(tree, p);
  }

  @Override
  public R visitWhileLoop(WhileLoopTree node, P p) {
    defaultAction(tree);
    return super.visitWhileLoop(tree, p);
  }

  @Override
  public R visitForLoop(ForLoopTree node, P p) {
    defaultAction(tree);
    return super.visitForLoop(tree, p);
  }

  @Override
  public R visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
    defaultAction(tree);
    return super.visitEnhancedForLoop(tree, p);
  }

  @Override
  public R visitLabeledStatement(LabeledStatementTree node, P p) {
    defaultAction(tree);
    return super.visitLabeledStatement(tree, p);
  }

  @Override
  public R visitSwitch(SwitchTree node, P p) {
    defaultAction(tree);
    return super.visitSwitch(tree, p);
  }

  /**
   * Visit a switch expression tree.
   *
   * @param tree switch expression tree
   * @param p null
   * @return null
   */
  public R visitSwitchExpression(Tree node, P p) {
    defaultAction(tree);
    return super.scan(tree, p);
  }

  @Override
  public R visitCase(CaseTree node, P p) {
    defaultAction(tree);
    return super.visitCase(tree, p);
  }

  @Override
  public R visitSynchronized(SynchronizedTree node, P p) {
    defaultAction(tree);
    return super.visitSynchronized(tree, p);
  }

  @Override
  public R visitTry(TryTree node, P p) {
    defaultAction(tree);
    return super.visitTry(tree, p);
  }

  @Override
  public R visitCatch(CatchTree node, P p) {
    defaultAction(tree);
    return super.visitCatch(tree, p);
  }

  @Override
  public R visitConditionalExpression(ConditionalExpressionTree node, P p) {
    defaultAction(tree);
    return super.visitConditionalExpression(tree, p);
  }

  @Override
  public R visitIf(IfTree node, P p) {
    defaultAction(tree);
    return super.visitIf(tree, p);
  }

  @Override
  public R visitExpressionStatement(ExpressionStatementTree node, P p) {
    defaultAction(tree);
    return super.visitExpressionStatement(tree, p);
  }

  @Override
  public R visitBreak(BreakTree node, P p) {
    defaultAction(tree);
    return super.visitBreak(tree, p);
  }

  @Override
  public R visitContinue(ContinueTree node, P p) {
    defaultAction(tree);
    return super.visitContinue(tree, p);
  }

  @Override
  public R visitReturn(ReturnTree node, P p) {
    defaultAction(tree);
    return super.visitReturn(tree, p);
  }

  @Override
  public R visitThrow(ThrowTree node, P p) {
    defaultAction(tree);
    return super.visitThrow(tree, p);
  }

  @Override
  public R visitAssert(AssertTree node, P p) {
    defaultAction(tree);
    return super.visitAssert(tree, p);
  }

  @Override
  public R visitMethodInvocation(MethodInvocationTree node, P p) {
    defaultAction(tree);
    return super.visitMethodInvocation(tree, p);
  }

  @Override
  public R visitNewClass(NewClassTree node, P p) {
    defaultAction(tree);
    return super.visitNewClass(tree, p);
  }

  @Override
  public R visitNewArray(NewArrayTree node, P p) {
    defaultAction(tree);
    return super.visitNewArray(tree, p);
  }

  @Override
  public R visitLambdaExpression(LambdaExpressionTree node, P p) {
    defaultAction(tree);
    return super.visitLambdaExpression(tree, p);
  }

  @Override
  public R visitParenthesized(ParenthesizedTree node, P p) {
    defaultAction(tree);
    return super.visitParenthesized(tree, p);
  }

  @Override
  public R visitAssignment(AssignmentTree node, P p) {
    defaultAction(tree);
    return super.visitAssignment(tree, p);
  }

  @Override
  public R visitCompoundAssignment(CompoundAssignmentTree node, P p) {
    defaultAction(tree);
    return super.visitCompoundAssignment(tree, p);
  }

  @Override
  public R visitUnary(UnaryTree node, P p) {
    defaultAction(tree);
    return super.visitUnary(tree, p);
  }

  @Override
  public R visitBinary(BinaryTree node, P p) {
    defaultAction(tree);
    return super.visitBinary(tree, p);
  }

  @Override
  public R visitTypeCast(TypeCastTree node, P p) {
    defaultAction(tree);
    return super.visitTypeCast(tree, p);
  }

  @Override
  public R visitInstanceOf(InstanceOfTree node, P p) {
    defaultAction(tree);
    return super.visitInstanceOf(tree, p);
  }

  /**
   * Visit a binding pattern tree.
   *
   * @param tree a binding pattern tree
   * @param p null
   * @return null
   */
  public R visitBindingPattern(Tree node, P p) {
    defaultAction(tree);
    return super.scan(tree, p);
  }

  @Override
  public R visitArrayAccess(ArrayAccessTree node, P p) {
    defaultAction(tree);
    return super.visitArrayAccess(tree, p);
  }

  @Override
  public R visitMemberSelect(MemberSelectTree node, P p) {
    defaultAction(tree);
    return super.visitMemberSelect(tree, p);
  }

  @Override
  public R visitMemberReference(MemberReferenceTree node, P p) {
    defaultAction(tree);
    return super.visitMemberReference(tree, p);
  }

  @Override
  public R visitIdentifier(IdentifierTree node, P p) {
    defaultAction(tree);
    return super.visitIdentifier(tree, p);
  }

  @Override
  public R visitLiteral(LiteralTree node, P p) {
    defaultAction(tree);
    return super.visitLiteral(tree, p);
  }

  @Override
  public R visitPrimitiveType(PrimitiveTypeTree node, P p) {
    defaultAction(tree);
    return super.visitPrimitiveType(tree, p);
  }

  @Override
  public R visitArrayType(ArrayTypeTree node, P p) {
    defaultAction(tree);
    return super.visitArrayType(tree, p);
  }

  @Override
  public R visitParameterizedType(ParameterizedTypeTree node, P p) {
    defaultAction(tree);
    return super.visitParameterizedType(tree, p);
  }

  @Override
  public R visitUnionType(UnionTypeTree node, P p) {
    defaultAction(tree);
    return super.visitUnionType(tree, p);
  }

  @Override
  public R visitIntersectionType(IntersectionTypeTree node, P p) {
    defaultAction(tree);
    return super.visitIntersectionType(tree, p);
  }

  @Override
  public R visitTypeParameter(TypeParameterTree node, P p) {
    defaultAction(tree);
    return super.visitTypeParameter(tree, p);
  }

  @Override
  public R visitWildcard(WildcardTree node, P p) {
    defaultAction(tree);
    return super.visitWildcard(tree, p);
  }

  @Override
  public R visitModifiers(ModifiersTree node, P p) {
    defaultAction(tree);
    return super.visitModifiers(tree, p);
  }

  @Override
  public R visitAnnotation(AnnotationTree node, P p) {
    defaultAction(tree);
    return super.visitAnnotation(tree, p);
  }

  @Override
  public R visitAnnotatedType(AnnotatedTypeTree node, P p) {
    defaultAction(tree);
    return super.visitAnnotatedType(tree, p);
  }

  @Override
  public R visitModule(ModuleTree node, P p) {
    defaultAction(tree);
    return super.visitModule(tree, p);
  }

  @Override
  public R visitExports(ExportsTree node, P p) {
    defaultAction(tree);
    return super.visitExports(tree, p);
  }

  @Override
  public R visitOpens(OpensTree node, P p) {
    defaultAction(tree);
    return super.visitOpens(tree, p);
  }

  @Override
  public R visitProvides(ProvidesTree node, P p) {
    defaultAction(tree);
    return super.visitProvides(tree, p);
  }

  @Override
  public R visitRequires(RequiresTree node, P p) {
    defaultAction(tree);
    return super.visitRequires(tree, p);
  }

  @Override
  public R visitUses(UsesTree node, P p) {
    defaultAction(tree);
    return super.visitUses(tree, p);
  }

  @Override
  public R visitOther(Tree node, P p) {
    defaultAction(tree);
    return super.visitOther(tree, p);
  }

  @Override
  public R visitErroneous(ErroneousTree node, P p) {
    defaultAction(tree);
    return super.visitErroneous(tree, p);
  }

  /**
   * Visit a yield tree.
   *
   * @param tree a yield tree
   * @param p null
   * @return null
   */
  public R visitYield(Tree node, P p) {
    defaultAction(tree);
    return super.scan(tree, p);
  }
}
