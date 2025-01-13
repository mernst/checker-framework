package org.checkerframework.checker.signature.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A sequence of dot-separated identifiers, followed by any number of array square brackets. A
 * string with this format might be interpreted as a {@linkplain FullyQualifiedName fully-qualified
 * name} or as a {@linkplain FqBinaryName fully-qualified binary name}. Those two formats are
 * syntactically identical but are interpreted differently.
 *
 * @checker_framework.manual #signature-checker Signature Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({FullyQualifiedName.class, FqBinaryName.class})
public @interface FullyQualifiedNameOrFqBinaryName {}
