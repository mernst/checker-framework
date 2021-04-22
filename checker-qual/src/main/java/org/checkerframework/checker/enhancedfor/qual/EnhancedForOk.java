package org.checkerframework.checker.enhancedfor.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Iterator;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Indicates that an iterator may be used in an enhanced {@code for} statement.
 *
 * @checker_framework.manual #enhancedfor-checker Enhanced For Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({EnhancedForUnknown.class})
@DefaultFor(types = {Iterator.class, Iterable.class, Collection.class})
public @interface EnhancedForOk {}
