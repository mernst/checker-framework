package org.checkerframework.checker.enhancedfor.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * The bottom qualifier. Represents the {@code null} value. Should rarely be written by programmers.
 *
 * @checker_framework.manual #enhancedfor-checker Enhanced For Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({EnhancedForOk.class, EnhancedForForbidden.class})
public @interface EnhancedForBottom {}
