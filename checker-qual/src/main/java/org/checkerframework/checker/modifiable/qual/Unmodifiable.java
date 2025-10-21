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
 * <p>This is the top qualifier in the Modifiable type hierarchy. Collections returned by methods
 * such as {@code List.of()}, {@code Collections.unmodifiableList()}, and {@code
 * Collections.emptyList()} are automatically inferred to be {@code @Unmodifiable}.
 *
 * <p>The Modifiable Checker issues a warning if a mutating method is called on a collection
 * annotated with {@code @Unmodifiable}, preventing {@code UnsupportedOperationException} at compile
 * time.
 *
 * @see Modifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
public @interface Unmodifiable {}
