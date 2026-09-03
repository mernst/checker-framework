package org.checkerframework.checker.testchecker.nullnesssub;

import org.checkerframework.checker.nullness.NullnessAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

/**
 * A subclass of {@link NullnessAnnotatedTypeFactory} that adds no behavior. It exists so that a
 * test can confirm that whole-program inference behaves the same for a subclass of the Nullness
 * Checker's type factory as for the type factory itself.
 */
public class NullnessSubclassAnnotatedTypeFactory extends NullnessAnnotatedTypeFactory {

  /**
   * Creates a NullnessSubclassAnnotatedTypeFactory.
   *
   * @param checker the associated checker
   */
  public NullnessSubclassAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
  }
}
