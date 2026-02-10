package org.checkerframework.checker.modifiability;

import com.sun.source.tree.MethodInvocationTree;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.modifiability.qual.WillThrowUOE;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

/** Visitor for the {@link ModifiabilityChecker}. */
public class ModifiabilityVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

  public ModifiabilityVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  @Override
  public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
    ExecutableElement method = TreeUtils.elementFromUse(node);
    // Check if the method being invoked is annotated with @WillThrowUOE.
    // Methods with this annotation (like SortedSet.addFirst) are guaranteed to throw
    // UnsupportedOperationException at runtime, so we report an error immediately.
    if (atypeFactory.getDeclAnnotation(method, WillThrowUOE.class) != null) {
      checker.reportError(node, "usage.will.throw.uoe", method.getSimpleName());
    }
    return super.visitMethodInvocation(node, p);
  }

  // Suppress the framework’s “constructor result must be TOP” check.
  // For Modifiability, constructors may legitimately produce @Modifiable.
  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}
}
