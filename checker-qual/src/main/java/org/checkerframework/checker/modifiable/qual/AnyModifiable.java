package org.checkerframework.checker.modifiable.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * {@link AnyModifiable} is the top qualifier in the Modifiable type hierarchy. It indicates that
 * the annotated collection may or may not support mutating operations - the modifiability is
 * unknown or could be either modifiable or unmodifiable.
 *
 * <p>This could be called UnknownModifiable (indicating that it is unknown what the modifiability
 * is), but that is less clear on a formal parameter, where "AnyModifiable" implies that the
 * modifiability could be anything. Unmodifiable and AnyModifiable cause the same sorts of errors in
 * general, so "Unmodifiable" could be omitted from the types system, but for clarity of
 * understanding, Unmodifiable is intended to be written on return types and AnyModifiable is
 * intended to be written on formal parameter types.
 *
 * @see Modifiable
 * @see Unmodifiable
 * @see BottomModifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
public @interface AnyModifiable {}
