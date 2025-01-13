package org.checkerframework.checker.signature.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * A sequence of identifiers, joined by periods.
 *
 * <p>As a type, this represents a non-array, non-inner, non-primitive class. It is a string that is
 * a valid {@linkplain org.checkerframework.checker.signature.qual.FullyQualifiedName fully
 * qualified name} and a valid {@linkplain org.checkerframework.checker.signature.qual.BinaryName
 * binary name}.
 *
 * <p>This may also represent a package name or a module name.
 *
 * <p>All of the above have the same syntactic form, but they have different syntactic meanings, and
 * therefore there are different qualifiers to prevent accidental misuse (say, passing a package
 * name where a module name is needed).
 *
 * <p>Examples: int, MyClass, java.lang, java.lang.Integer
 *
 * @checker_framework.manual #signature-checker Signature Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({
  DotSeparatedIdentifiersOrPrimitiveType.class,
  BinaryName.class,
  CanonicalName.class,
  PackageName.class,
  ModuleName.class
})
public @interface DotSeparatedIdentifiers {}
