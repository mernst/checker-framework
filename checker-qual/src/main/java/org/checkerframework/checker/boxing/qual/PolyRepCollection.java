package org.checkerframework.checker.boxing.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for the Enhanced For type system.
 *
 * <p>Any method written using @PolyRepCollection conceptually has two versions: one in which every
 * instance of @PolyRepCollection has been replaced by @RefCollection, and one in which every
 * instance of @PolyRepCollection has been replaced by @PrimCollection. (And also versions
 * for @UnknownRepCollection and @CollectionRepBottom.)
 *
 * @checker_framework.manual #boxing-checker Boxing Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(UnknownRepCollection.class)
public @interface PolyRepCollection {}
