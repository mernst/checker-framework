package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience alias meaning {@code @UnknownGrow @UnknownShrink @UnknownReplace}. Represents an
 * unknown or arbitrary modifiability capability; the checker cannot determine whether the
 * collection is growable, shrinkable, or replaceable.
 *
 * <p>This annotation is not part of the type hierarchy; the Modifiability Checker expands it to
 * {@code @UnknownGrow @UnknownShrink @UnknownReplace} on each annotated type. It is equivalent to
 * {@link Unmodifiable}.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface UnknownModifiability {}
