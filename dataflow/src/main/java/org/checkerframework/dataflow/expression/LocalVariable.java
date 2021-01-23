package org.checkerframework.dataflow.expression;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TypeAnnotationUtils;
import org.checkerframework.javacutil.TypesUtils;

public class LocalVariable extends JavaExpression {
    protected final Element element;

    public LocalVariable(LocalVariableNode localVar) {
        super(localVar.getType());
        this.element = localVar.getElement();
    }

    public LocalVariable(Element elem) {
        super(ElementUtils.getType(elem));
        this.element = elem;
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
    public String toString(@Nullable List<JavaExpression> parameterIndex) {
        String result = element.toString();
        if (parameterIndex != null) {
            System.out.printf(
                    "LocalVariable.toString: %s [%s] %d%n",
                    this.toString(null), this.getClass(), parameterIndex.size());
            for (JavaExpression je : parameterIndex) {
                System.out.printf("  %s [%s]%n", je.toString(null), je.getClass());
            }
            int zeroBased = parameterIndex.indexOf(this);
            if (zeroBased != -1) {
                return "#" + (zeroBased + 1);
            }
        }
        return result;
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
