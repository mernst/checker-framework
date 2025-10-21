package org.checkerframework.checker.modifiable.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for the Modifiable type system.
 *
 * <p>Any method written using {@code @PolyModifiable} conceptually has two versions: one where
 * every instance of {@code @PolyModifiable} has been replaced by {@code @Modifiable}, and one where
 * every instance of {@code @PolyModifiable} has been replaced by {@code @Unmodifiable}.
 *
 * <p>For example, a method like:
 *
 * <pre>{@code
 * @PolyModifiable List<String> process(@PolyModifiable List<String> input) { ... }
 * }</pre>
 *
 * <p>is equivalent to having both:
 *
 * <pre>{@code
 * @Modifiable List<String> process(@Modifiable List<String> input) { ... }
 * @Unmodifiable List<String> process(@Unmodifiable List<String> input) { ... }
 * }</pre>
 *
 * <p>This allows methods to preserve the modifiability of their arguments in their return types.
 *
 * @see Modifiable
 * @see Unmodifiable
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@PolymorphicQualifier(Unmodifiable.class)
public @interface PolyModifiable {}
