package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Convenience alias meaning {@code @PolyGrow @PolyShrink @PolyReplace}. A polymorphic qualifier for
 * all three modifiability hierarchies.
 *
 * <p>This annotation is not part of the type hierarchy; the Modifiability Checker expands it to
 * {@code @PolyGrow @PolyShrink @PolyReplace} on each annotated type.
 *
 * <p>Use on methods that preserve or transfer modifiability &mdash; for example, {@code
 * List.subList()}, {@code iterator()}, or {@code stream()}.
 *
 * <p>For example:
 *
 * <pre><code>
 * class Example {
 * &nbsp; @PolyModifiable List&lt;E&gt; subList(@PolyModifiable List&lt;E&gt; this, int from, int to);
 * }
 * </code></pre>
 *
 * If the receiver is {@code @Unmodifiable}, the return is {@code @Unmodifiable}. If the receiver is
 * {@code @Modifiable}, the return is {@code @Modifiable}.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 * @checker_framework.manual #qualifier-polymorphism Qualifier polymorphism
 */
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface PolyModifiable {}
