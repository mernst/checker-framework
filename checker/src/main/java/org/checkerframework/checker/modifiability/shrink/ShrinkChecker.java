package org.checkerframework.checker.modifiability.shrink;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.RelevantJavaTypes;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.source.SuppressWarningsPrefix;

/**
 * A type-checker that warns, at compile time, if a program might throw {@link
 * UnsupportedOperationException} at run time due to calling a shrink method on a collection.
 *
 * <p>The checker enforces the Modifiability type system, where {@code @Shrinkable} collections can
 * be safely mutated and {@code @UnknownShrink} collections cannot call remove without risking an
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
@StubFiles({"ical4j.astub", "javaparser.astub"})
@SuppressWarningsPrefix({"Shrinkable", "UnknownShrink"})
public class ShrinkChecker extends BaseTypeChecker {
  /** Creates a Shrink checker. */
  public ShrinkChecker() {}
}
