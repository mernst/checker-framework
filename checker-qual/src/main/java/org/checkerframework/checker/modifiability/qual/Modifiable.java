package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience alias meaning {@code @Growable @Shrinkable @Replaceable}. Calling grow, shrink, and
 * replace operations such as {@code add}, {@code remove}, {@code set}, etc. on this collection will
 * not result in throwing {@link UnsupportedOperationException}.
 *
 * <p>This annotation is not part of the type hierarchy; the Modifiability Checker expands it to
 * {@code @Growable @Shrinkable @Replaceable} on each annotated type.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Modifiable {}
