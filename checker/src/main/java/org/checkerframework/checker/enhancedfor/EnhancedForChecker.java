package org.checkerframework.checker.enhancedfor;

import java.util.Iterator;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.RelevantJavaTypes;

@RelevantJavaTypes({
  Iterator.class,
  Iterable.class,
})
public final class EnhancedForChecker extends BaseTypeChecker {}
