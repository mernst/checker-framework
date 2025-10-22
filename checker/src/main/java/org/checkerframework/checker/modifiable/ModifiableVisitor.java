package org.checkerframework.checker.modifiable;

import javax.lang.model.element.ExecutableElement;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

/** Visitor for the {@link ModifiableChecker}. */
public class ModifiableVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

  /**
   * Creates a {@link ModifiableVisitor}.
   *
   * @param checker the checker that uses this visitor
   */
  public ModifiableVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  /** Don't check that the constructor result is top. */
  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}
}
