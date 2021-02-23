package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * {@link NullableWhen} is a type annotation that indicates that the value may be {@code null} (like
 * {@link Nullable}, but is known to be non-null when a given expression evaluates to true.
 *
 * <p>Currently, the Nullness Checker does not analyze the expression; in the future, it will do so.
 *
 * @see NonNull
 * @see MonotonicNonNull
 * @see org.checkerframework.checker.nullness.NullnessChecker
 * @checker_framework.manual #nullness-checker Nullness Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({Nullable.class})
@QualifierForLiterals(LiteralKind.NULL)
@DefaultFor(types = Void.class)
public @interface NullableWhen {
    /**
     * Suppose that expression {@emph E} has type {@code @NullableWhen("C")}. When {@emph C}
     * evaluates to true, then {@emph E} may evaluate to null; otherwise, {@emph E} must evaluate to
     * a non-null value.
     *
     * @checker_framework.manual #java-expressions-as-arguments Syntax of Java expressions
     */
    String value() default "?";
}
