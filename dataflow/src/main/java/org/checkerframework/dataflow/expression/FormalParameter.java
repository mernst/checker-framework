package org.checkerframework.dataflow.expression;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.List;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A formal parameter, represented by its 1-based index. */
public class FormalParameter extends LocalVariable {

    /**
     * Parsable replacement for parameter references. It is parseable because it is a Java
     * identifier.
     */
    public static final String PARAMETER_REPLACEMENT = "__param__";

    /** The length of {@link #PARAMETER_REPLACEMENT}. */
    public static final int PARAMETER_REPLACEMENT_LENGTH = PARAMETER_REPLACEMENT.length();

    /** The 1-based index. */
    protected final int index;

    /**
     * Create a FormalParameter.
     *
     * @param index the 1-based index
     * @param element the element for the formal parameter itself
     */
    public FormalParameter(Element element, int index) {
        super(element);
        this.index = index;
    }

    /*
    public FormalParameter(LocalVariableNode localVar) {
        super(localVar.getType());
        this.element = localVar.getElement();
    }

    public FormalParameter(Element elem) {
        super(ElementUtils.getType(elem));
        this.element = elem;
    }
    */

    // NOTE that equals() is not symmetric.  Maybe create a separate equals method to test local
    // variables as local variables?
    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof FormalParameter)) {
            return false;
        }

        FormalParameter other = (FormalParameter) obj;
        return super.equals(other) && this.index == other.index;
    }

    /**
     * Returns the 1-based index of this formal parameter.
     *
     * @return the 1-based index of this formal parameter
     */
    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return "#" + index;
    }

    @Override
    public String toStringDebug() {
        return super.toStringDebug() + " [owner=" + ((VarSymbol) element).owner + "]";
    }

    @Override
    public boolean containsOfClass(Class<? extends JavaExpression> clazz) {
        return false;
    }

    @Override
    public boolean syntacticEquals(JavaExpression other) {
        if (!(other instanceof FormalParameter)) {
            return false;
        }
        FormalParameter l = (FormalParameter) other;
        return l.equals(this);
    }

    @Override
    public boolean containsSyntacticEqualJavaExpression(JavaExpression other) {
        return syntacticEquals(other);
    }

    @Override
    public boolean isUnassignableByOtherCode() {
        return true;
    }

    @Override
    public boolean isUnmodifiableByOtherCode() {
        return true;
    }

    @Override
    public FormalParameter atMethodScope(List<JavaExpression> parameters) {
        return this;
    }
}
