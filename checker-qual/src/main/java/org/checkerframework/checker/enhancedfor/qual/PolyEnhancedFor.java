package org.checkerframework.checker.enhancedfor.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for the Enhanced For type system.
 *
 * <p>Any method written using @PolyEnhancedFor conceptually has two versions: one in which every
 * instance of @PolyEnhancedFor has been replaced by @EnhancedForOk, and one in which every instance
 * of @PolyEnhancedFor has been replaced by @EnhancedForForbidden. (And also versions
 * for @EnhancedForUnknown and @EnhancedForBottom.)
 *
 * @checker_framework.manual #enhancedfor-checker Enhanced For Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(EnhancedForUnknown.class)
public @interface PolyEnhancedFor {}
