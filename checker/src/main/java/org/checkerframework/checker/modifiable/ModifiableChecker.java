package org.checkerframework.checker.modifiable;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SuppressWarningsPrefix;

/**
 * A type-checker that warns, at compile time, if a program might throw {@link
 * UnsupportedOperationException} at run time due to calling a mutating method on an unmodifiable
 * collection.
 *
 * <p>The checker enforces the Modifiable type system, where {@code @Modifiable} collections can be
 * safely mutated and {@code @Unmodifiable} collections cannot be mutated without risking an {@link
 * UnsupportedOperationException}.
 *
 * @checker_framework.manual #modifiable-checker Modifiable Checker
 */
@SuppressWarningsPrefix({"modifiable", "unmodifiable"})
public class ModifiableChecker extends BaseTypeChecker {}
