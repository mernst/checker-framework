package org.checkerframework.checker.modifiable.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * {@link Unmodifiable} indicates that the annotated collection does not support mutating
 * operations. Calling methods like {@code add()} or {@code remove()} on such collections will throw
 * {@code UnsupportedOperationException} at runtime.
 *
 * @see AnyModifiable
 * @see Modifiable
 * @see BottomModifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(AnyModifiable.class)
public @interface Unmodifiable {}
