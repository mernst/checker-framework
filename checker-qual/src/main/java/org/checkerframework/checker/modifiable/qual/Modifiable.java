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
 * @see AnyModifiable
 * @see Unmodifiable
 * @see BottomModifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(AnyModifiable.class)
@DefaultQualifierInHierarchy
public @interface Modifiable {}
