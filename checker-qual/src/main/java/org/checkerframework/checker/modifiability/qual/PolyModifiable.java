package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.PolymorphicQualifier;

/**
 * A polymorphic qualifier for modifiability.
 *
 * <p>Use on methods that preserve or transfer modifiability — for example, {@code List.subList()},
 * {@code iterator()}, or {@code stream()}.
 *
 * <p>The same qualifier that appears on the receiver will also appear on the return type (and
 * possibly parameters). For example:
 *
 * <pre><code>
 * class Example {
 * &nbsp; @PolyModifiable List&lt;E&gt; subList(@PolyModifiable List&lt;E&gt; this, int from, int to);
 * }
 * </code></pre>
 *
 * If the receiver is {@code @Unmodifiable}, the return is {@code @Unmodifiable}. If the receiver is
 * {@code @Modifiable}, the return is {@code @Modifiable}.
 */
@PolymorphicQualifier(UnknownModifiability.class)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface PolyModifiable {}
