package org.checkerframework.checker.enhancedfor;

import java.util.Iterator;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.RelevantJavaTypes;

/**
 * A checker that can forbid use of the enhanced {@code for} statement (foreach loops).
 *
 * @checker_framework.manual #enhancedfor-checker Enhanced For Checker
 */
@RelevantJavaTypes({
  Iterator.class,
  Iterable.class,
})
public final class EnhancedForChecker extends BaseTypeChecker {}
