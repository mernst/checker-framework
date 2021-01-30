package org.checkerframework.dataflow.expression;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Objects;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TypeAnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;

/**
 * A local variable. May be a formal parameter expressed using its name, but is not a formal
 * parameter expressed using the "#2" notation.
 */
public class LocalVariable extends JavaExpression {
    /** The element for this local variable. */
    protected final Element element;

    /**
     * Creates a new LocalVariable.
     *
     * @param localVar a CFG local variable
     */
    protected LocalVariable(LocalVariableNode localVar) {
        super(localVar.getType());
        this.element = localVar.getElement();
    }

    public LocalVariable(Element elem) {
        super(ElementUtils.getType(elem));
        this.element = elem;
    }

    /**
     * Creates a LocalVariable or a FormalParameter, depending on the argument.
     *
     * @param localVar a CFG node for a variable
     * @return a LocalVariable or FormalParameter for the given CFG variable
     */
    public static LocalVariable create(LocalVariableNode localVar) {
        String name = localVar.getName();
        if (name.startsWith(FormalParameter.PARAMETER_REPLACEMENT)) {
            try {
                return new FormalParameter(
                        localVar.getElement(),
                        Integer.parseInt(
                                name.substring(FormalParameter.PARAMETER_REPLACEMENT_LENGTH)));
            } catch (NumberFormatException e) {
                // fallthrough
            }
        }
        return new LocalVariable(localVar);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LocalVariable)) {
            return false;
        }

        LocalVariable other = (LocalVariable) obj;
        VarSymbol vs = (VarSymbol) element;
        VarSymbol vsother = (VarSymbol) other.element;
        // The code below isn't just return vs.equals(vsother) because an element might be
        // different between subcheckers.  The owner of a lambda parameter is the enclosing
        // method, so a local variable and a lambda parameter might have the same name and the
        // same owner.  pos is used to differentiate this case.
        return vs.pos == vsother.pos
                && vsother.name.contentEquals(vs.name)
                && vsother.owner.toString().equals(vs.owner.toString());
    }

    public Element getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        VarSymbol vs = (VarSymbol) element;
        return Objects.hash(
                vs.name.toString(),
                TypeAnnotationUtils.unannotatedType(vs.type).toString(),
                vs.owner.toString());
    }

    @Override
    public String toString() {
        return element.toString();
    }

    @Override
    public boolean containsOfClass(Class<? extends JavaExpression> clazz) {
        return getClass() == clazz;
    }

    @Override
    public boolean syntacticEquals(JavaExpression other) {
        if (!(other instanceof LocalVariable)) {
            return false;
        }
        LocalVariable l = (LocalVariable) other;
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
        return TypesUtils.isImmutableTypeInJdk(((VarSymbol) element).type);
    }
}
