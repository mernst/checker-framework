package org.checkerframework.dataflow.expression;

import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
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

    /**
     * Returns true if this is the same formal parameter as {@code other}.
     *
     * @param je1 the first JavaExpression to compare
     * @param je2 the second JavaExpression to compare
     * @param elements element utilities
     * @return true if this is the same formal parameter as {@code other}
     */
    public static boolean isSameFormalParameter(
            JavaExpression je1, JavaExpression je2, Elements elements) {
        if (je1 instanceof LocalVariable && je2 instanceof LocalVariable) {
            LocalVariable var1 = (LocalVariable) je1;
            LocalVariable var2 = (LocalVariable) je2;
            Element enclosing1 = var1.element.getEnclosingElement();
            Element enclosing2 = var2.element.getEnclosingElement();
            if (enclosing1 instanceof ExecutableElement
                    && enclosing2 instanceof ExecutableElement) {
                ExecutableElement methodElt1 = (ExecutableElement) enclosing1;
                ExecutableElement methodElt2 = (ExecutableElement) enclosing2;
                int index1 = methodElt1.getParameters().indexOf(var1.element);
                int index2 = methodElt2.getParameters().indexOf(var2.element);
                if (index1 != -1 && index1 == index2) {
                    if (elements.overrides(
                                    methodElt1,
                                    methodElt2,
                                    (TypeElement) methodElt1.getEnclosingElement())
                            || elements.overrides(
                                    methodElt2,
                                    methodElt1,
                                    (TypeElement) methodElt2.getEnclosingElement())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    public String toString(@Nullable List<JavaExpression> parameters) {
        String result = element.toString();
        if (parameters != null) {
            if (false) {
                System.out.printf(
                        "LocalVariable.toString: %s [%s] %d%n",
                        this.toString(null), this.getClass(), parameters.size());
                for (JavaExpression je : parameters) {
                    System.out.printf("  %s [%s]%n", je.toString(null), je.getClass());
                }
            }
            int zeroBased = parameters.indexOf(this);
            if (zeroBased != -1) {
                return "#" + (zeroBased + 1);
            }
        }
        return result;
    }

    @Override
    public String toStringDebug() {
        return super.toStringDebug() + " [owner=" + ((VarSymbol) element).owner + "]";
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
