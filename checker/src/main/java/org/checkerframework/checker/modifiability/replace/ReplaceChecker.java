package org.checkerframework.checker.modifiability.replace;

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
 * UnsupportedOperationException} at run time due to calling a replace method on a collection.
 *
 * <p>The checker enforces the Modifiability type system, where {@code @Replaceable} collections can
 * be safely mutated and {@code @UnknownReplace} collections cannot call remove without risking an
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
@SuppressWarningsPrefix({"Replaceable", "UnknownReplace"})
public class ReplaceChecker extends BaseTypeChecker {
  /** Creates a Replace checker. */
  public ReplaceChecker() {}
}
