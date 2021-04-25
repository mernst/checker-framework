package org.checkerframework.checker.boxing.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * Indicates that an iterator may not be used in an enhanced {@code for} statement.
 *
 * @checker_framework.manual #boxing-checker Boxing Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({UnknownRepCollection.class})
public @interface PrimCollection {}
