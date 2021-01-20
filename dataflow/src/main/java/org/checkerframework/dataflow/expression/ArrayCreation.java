package org.checkerframework.dataflow.expression;

import java.util.List;
import java.util.Objects;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.TypesUtils;
import org.plumelib.util.StringsPlume;

/** FlowExpression for array creations. {@code new String[]()}. */
public class ArrayCreation extends JavaExpression {

    /** List of dimensions expressions. {code null} means that there is no dimension expression. */
    protected final List<? extends @Nullable JavaExpression> dimensions;
    /** List of initializers. */
    protected final List<JavaExpression> initializers;

    /**
     * Creates an ArrayCreation object.
     *
     * @param type array type
     * @param dimensions list of dimension expressions; {code null} means that there is no dimension
     *     expression
     * @param initializers list of initializer expressions
     */
    public ArrayCreation(
            TypeMirror type,
            List<? extends @Nullable JavaExpression> dimensions,
            List<JavaExpression> initializers) {
        super(type);
        assert type.getKind() == TypeKind.ARRAY;
        this.dimensions = dimensions;
        this.initializers = initializers;
    }

    /**
     * Returns a list of receivers representing the dimension of this array creation.
     *
     * @return a list of receivers representing the dimension of this array creation
     */
    public List<? extends @Nullable JavaExpression> getDimensions() {
        return dimensions;
    }

    public List<JavaExpression> getInitializers() {
        return initializers;
    }

    @Override
    public boolean containsOfClass(Class<? extends JavaExpression> clazz) {
        for (JavaExpression n : dimensions) {
            if (n != null && n.getClass() == clazz) {
                return true;
            }
        }
        for (JavaExpression n : initializers) {
            if (n.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUnassignableByOtherCode() {
        return false;
    }

    @Override
    public boolean isUnmodifiableByOtherCode() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions, initializers, getType().toString());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ArrayCreation)) {
            return false;
        }
        ArrayCreation other = (ArrayCreation) obj;
        return this.dimensions.equals(other.getDimensions())
                && this.initializers.equals(other.getInitializers())
                // It might be better to use Types.isSameType(getType(), other.getType()), but I
                // don't have a Types object.
                && getType().toString().equals(other.getType().toString());
    }

    @Override
    public boolean syntacticEquals(JavaExpression other) {
        return this.equals(other);
    }

    @Override
    public boolean containsSyntacticEqualJavaExpression(JavaExpression other) {
        return syntacticEquals(other);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (false) {
            System.out.printf(
                    "type=%s, dimensions=%s initializers=%s%n", type, dimensions, initializers);
        }
        if (dimensions.isEmpty()) {
            sb.append("new " + type);
        } else {
            sb.append("new " + TypesUtils.getInnermostComponentType((ArrayType) type));
            for (JavaExpression dim : dimensions) {
                sb.append("[");
                sb.append(dim == null ? "" : dim);
                sb.append("]");
            }
        }
        if (!initializers.isEmpty()) {
            sb.append(" {");
            sb.append(StringsPlume.join(", ", initializers));
            sb.append("}");
        }
        if (false) {
            System.out.printf("toString => %s%n", sb);
        }
        return sb.toString();
    }

    @Override
    public String toStringDebug() {
        return "\""
                + super.toStringDebug()
                + "\""
                + " type="
                + type
                + " dimensions="
                + dimensions
                + " initializers="
                + initializers;
    }
}
