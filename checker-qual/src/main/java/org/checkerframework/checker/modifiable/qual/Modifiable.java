package org.checkerframework.checker.modifiable.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * {@link Modifiable} indicates that the annotated collection supports mutating operations such as
 * {@code add()} and {@code remove()} without throwing {@code UnsupportedOperationException}.
 *
 * <p>This is the bottom qualifier in the Modifiable type hierarchy and is the default qualifier.
 * Most collection instances (such as {@code ArrayList}, {@code HashSet}, etc.) are modifiable by
 * default.
 *
 * <p>The Modifiable Checker guarantees that if a collection is annotated with {@code @Modifiable},
 * then calling mutating methods on it will not throw {@code UnsupportedOperationException}.
 *
 * @see Unmodifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Unmodifiable.class)
@DefaultQualifierInHierarchy
public @interface Modifiable {}
