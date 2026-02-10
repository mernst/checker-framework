package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for modifiability that preserves the shrink capability.
 *
 * <p>Use on methods that preserve shrinkability — for example, {@code Map.keySet()}.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 *     <p>TODO: need to implement
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(UnknownModifiability.class)
public @interface PolyShrink {}
