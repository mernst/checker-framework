package org.checkerframework.checker.modifiable.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

/**
 * {@link BottomModifiable} is the bottom qualifier in the Modifiable type hierarchy. It is a
 * subtype of both {@code @Modifiable} and {@code @Unmodifiable}, representing a collection that
 * satisfies the requirements of both modifiable and unmodifiable collections.
 *
 * <p>In practice, this qualifier is rarely used directly by programmers. It may appear in the type
 * system when the checker determines that a collection could be treated as either modifiable or
 * unmodifiable, or when dealing with impossible types.
 *
 * @see AnyModifiable
 * @see Modifiable
 * @see Unmodifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND, TypeUseLocation.EXPLICIT_UPPER_BOUND})
@SubtypeOf({Modifiable.class, Unmodifiable.class})
public @interface BottomModifiable {}
