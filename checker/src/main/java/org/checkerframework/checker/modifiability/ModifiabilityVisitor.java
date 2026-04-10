package org.checkerframework.checker.modifiability;

import com.sun.source.tree.MethodInvocationTree;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.modifiability.qual.ThrowsUOE;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Base visitor for the Modifiability sub-checkers (Grow, Shrink, Replace).
 *
 * <p>This class contains logic shared across all three sub-checkers:
 *
 * <ul>
 *   <li>Reporting errors for invocations of methods annotated with {@link ThrowsUOE}.
 *   <li>Suppressing the "constructor result must be TOP" check, since collection constructors may
 *       legitimately produce {@code @Modifiable}.
 * </ul>
 */
public class ModifiabilityVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

  /**
   * Create a ModifiabilityVisitor.
   *
   * @param checker the checker that uses this visitor
   */
  public ModifiabilityVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  @Override
  public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
    ExecutableElement method = TreeUtils.elementFromUse(node);
    // Check if the method being invoked is annotated with @ThrowsUOE.
    // Methods with this annotation (like SortedSet.addFirst) are guaranteed to throw
    // UnsupportedOperationException at run time, so we report an error immediately.
    if (atypeFactory.getDeclAnnotation(method, ThrowsUOE.class) != null) {
      checker.reportError(node, "usage.throws.uoe", method.getSimpleName());
    }
    return super.visitMethodInvocation(node, p);
  }

  // MDE: Ensure that there are tests that no unsoundness results.  (I didn't check whether they
  // exist.)
  // Suppress the framework's "constructor result must be TOP" check.
  // For Modifiability, constructors may legitimately produce @Modifiable.
  // By default, the BaseTypeChecker requires constructors to return the top type
  // (here @UnknownModifiability). However, many collection constructors (like new ArrayList())
  // produce a @Modifiable object, which is a subtype of @UnknownModifiability.
  // We want to allow this so that we can use these objects for mutation without
  // explicit casting or annotations.
  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}
}
