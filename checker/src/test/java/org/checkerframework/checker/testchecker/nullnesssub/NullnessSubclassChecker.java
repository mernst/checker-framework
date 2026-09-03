package org.checkerframework.checker.testchecker.nullnesssub;

import javax.annotation.processing.SupportedOptions;
import org.checkerframework.checker.nullness.NullnessChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

/**
 * A subclass of {@link NullnessChecker} that adds no behavior, other than using {@link
 * NullnessSubclassAnnotatedTypeFactory} as its type factory.
 */
// javax.annotation.processing.SupportedOptions is not inherited, so it must be repeated here.
@SupportedOptions({"assumeKeyFor", "invocationPreservesArgumentNullness"})
public class NullnessSubclassChecker extends NullnessChecker {

  /** Creates a NullnessSubclassChecker. */
  public NullnessSubclassChecker() {}

  @Override
  protected BaseTypeVisitor<?> createSourceVisitor() {
    return new NullnessSubclassVisitor(this);
  }
}
