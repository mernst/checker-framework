package org.checkerframework.checker.modifiability;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.RelevantJavaTypes;
import org.checkerframework.framework.source.SuppressWarningsPrefix;

/**
 * A type-checker that warns, at compile time, if a program might throw {@link
 * UnsupportedOperationException} at run time due to calling a mutating method on an unmodifiable
 * collection.
 *
 * <p>The checker enforces the Modifiability type system, where {@code @Modifiable} collections can
 * be safely mutated and {@code @Unmodifiable} collections cannot be mutated without risking an
 * {@link UnsupportedOperationException}.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@RelevantJavaTypes({
  Collection.class,
  Iterator.class,
  Map.class,
  Map.Entry.class,
  Collections.class
})
@SuppressWarningsPrefix({"modifiable", "unmodifiable"})
public class ModifiabilityChecker extends BaseTypeChecker {
  /** Creates a Modifiability checker. */
  public ModifiabilityChecker() {}
}
