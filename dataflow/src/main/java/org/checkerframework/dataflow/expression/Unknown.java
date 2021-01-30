package org.checkerframework.dataflow.expression;

import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.interning.qual.UsesObjectEquals;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.Store;

/** Stands for any expression that the Dataflow Framework lacks explicit support for. */
@UsesObjectEquals
public class Unknown extends JavaExpression {
    /**
     * Create a new Unknown receiver.
     *
     * @param type the Java type of this receiver
     */
    public Unknown(TypeMirror type) {
        super(type);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj == this;
    }

    // Overridden to avoid an error "overrides equals, but does not override hashCode"
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "?";
    }

    @Override
    public boolean syntacticEquals(JavaExpression je) {
        return this == je;
    }

    @Override
    public boolean containsModifiableAliasOf(Store<?> store, JavaExpression other) {
        return true;
    }

    @Override
    public boolean containsOfClass(Class<? extends JavaExpression> clazz) {
        return getClass() == clazz;
    }

    @Override
    public boolean isUnassignableByOtherCode() {
        return false;
    }

    @Override
    public boolean isUnmodifiableByOtherCode() {
        return false;
    }
}
