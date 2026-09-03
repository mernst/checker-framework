package org.checkerframework.checker.testchecker.nullnesssub;

import org.checkerframework.checker.nullness.NullnessAnnotatedTypeFactory;
import org.checkerframework.checker.nullness.NullnessVisitor;
import org.checkerframework.common.basetype.BaseTypeChecker;

/**
 * A subclass of {@link NullnessVisitor} that uses {@link NullnessSubclassAnnotatedTypeFactory} as
 * its type factory.
 */
public class NullnessSubclassVisitor extends NullnessVisitor {

  /**
   * Creates a NullnessSubclassVisitor.
   *
   * @param checker the associated checker
   */
  @SuppressWarnings("this-escape") // NullnessVisitor's constructor also leaks `this`.
  public NullnessSubclassVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  @Override
  public NullnessAnnotatedTypeFactory createTypeFactory() {
    return new NullnessSubclassAnnotatedTypeFactory(checker);
  }
}
