package org.checkerframework.dataflow.expression;

import java.util.Objects;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.javacutil.AnnotationProvider;

/**
 * A method reference, which is one of:
 *
 * <ul>
 *   <li>ExpressionName :: [TypeArguments] Identifier
 *   <li>Primary :: [TypeArguments] Identifier
 *   <li>ReferenceType :: [TypeArguments] Identifier
 *   <li>super :: [TypeArguments] Identifier
 *   <li>TypeName . super :: [TypeArguments] Identifier
 *   <li>ClassType :: [TypeArguments] new
 *   <li>ArrayType :: new
 * </ul>
 *
 * This implementation does not yet handle all the cases, only the most common ones. For example, it
 * does not handle the optional "[TypeArguments]" syntax.
 */
public class MethodReference extends JavaExpression {

  /** What comes before "::", which is an expression, a type, or "super". */
  JavaExpression scope;

  // Exactly one of declaringExpr, declaringType, and declaringSuper is non-null.
  // /** The expression before "::", or null. */
  // JavaExpression declaringExpr;
  // /** The type before "::", or null. */
  // TypeMirror declaringType;
  // /** The "super" bfore "::", or null. */
  // SuperReference declaringSuper;

  // TODO: handle type arguments, which come after "::" but before the method name.

  /** The name of a method, or "new". This is what comes after "::". */
  String methodName;

  /**
   * Creates a new {@link MethodReference}.
   *
   * @param type the type of the method reference
   * @param scope what comes before "::", which is an expression, a type, or "super"
   * @param methodName the name of the method
   */
  public MethodReference(TypeMirror type, JavaExpression scope, String methodName) {
    super(type);
    this.scope = scope;
    this.methodName = methodName;
  }

  @SuppressWarnings("unchecked") // generic cast
  @Override
  public <T extends JavaExpression> @Nullable T containedOfClass(Class<T> clazz) {
    return getClass() == clazz ? (T) this : null;
  }

  @Override
  public boolean isDeterministic(AnnotationProvider provider) {
    return scope.isDeterministic(provider);
  }

  @Override
  public boolean isAssignableByOtherCode() {
    return scope.isAssignableByOtherCode();
  }

  @Override
  public boolean isModifiableByOtherCode() {
    return scope.isModifiableByOtherCode();
  }

  @Override
  public boolean containsModifiableAliasOf(Store<?> store, JavaExpression other) {
    return scope.containsModifiableAliasOf(store, other);
  }

  @Override
  public boolean syntacticEquals(JavaExpression je) {
    if (!(je instanceof MethodReference)) {
      return false;
    }
    MethodReference mr = (MethodReference) je;
    return this.scope.syntacticEquals(mr.scope) && this.methodName.equals(mr.methodName);
  }

  @Override
  public boolean containsSyntacticEqualJavaExpression(JavaExpression other) {
    return this.syntacticEquals(other) || this.scope.containsSyntacticEqualJavaExpression(other);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof MethodReference)) {
      return false;
    }
    MethodReference other = (MethodReference) obj;
    return this.scope.equals(other.scope) && this.methodName.equals(other.methodName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, methodName);
  }

  @Override
  public <R, P> R accept(JavaExpressionVisitor<R, P> visitor, P p) {
    return visitor.visitMethodReference(this, p);
  }

  @Override
  public String toString() {
    return scope.toString() + "::" + methodName;
  }
}
