import java.util.Map;
import org.checkerframework.checker.nullness.qual.*;

public class ParameterExpression {
    public void m1(
            @Nullable Object o, @Nullable Object o1, @Nullable Object o2, @Nullable Object o3) {
        // :: error: (flowexpr.parse.error.postcondition)
        m2(o);
        // :: error: (dereference.of.nullable)
        o.toString();
        m3(o);
        o.toString();
        m4(o1, o2, o3);
        // :: error: (dereference.of.nullable)
        o1.toString();
        // :: error: (dereference.of.nullable)
        o2.toString();
        o3.toString();
    }

    @SuppressWarnings("assert.postcondition.not.satisfied")
    // "#0" is illegal syntax; it should be "#1"
    @EnsuresNonNull("#0")
    // :: error: (flowexpr.parse.error)
    public void m2(final @Nullable Object o) {}

    @SuppressWarnings("contracts.postcondition.not.satisfied")
    @EnsuresNonNull("#1")
    public void m3(final @Nullable Object o) {}

    @SuppressWarnings("contracts.postcondition.not.satisfied")
    @EnsuresNonNull("#3")
    public void m4(@Nullable Object x1, @Nullable Object x2, final @Nullable Object x3) {}

    // When a method has a formal parameter named "field", then the identifier 'field' is
    // interpreted as the formal parameter, not as the field that it shadows.
    // To avoid ambiguity, the programmer should write "#1" for the formal parameter, and
    // "this.field" for the field.

    @Nullable Object field = null;

    // Postconditions
    @EnsuresNonNull("field") // OK
    public void m5() {
        field = new Object();
    }

    @EnsuresNonNull("param")
    // :: warning: (contracts.postcondition.expression.parameter.name)
    public void m6a(@Nullable Object param) {
        param = new Object();
    }

    @EnsuresNonNull("param")
    // :: error: (contracts.postcondition.not.satisfied)
    // :: warning: (contracts.postcondition.expression.parameter.name)
    public void m6b(@Nullable Object param) {
        param = null;
    }

    @EnsuresNonNull("field")
    // :: warning: (contracts.postcondition.expression.parameter.name)
    public void m7a(@Nullable Object field) {
        field = new Object();
    }

    @EnsuresNonNull("field")
    // :: error: (contracts.postcondition.not.satisfied)
    // :: warning: (contracts.postcondition.expression.parameter.name)
    public void m7b(@Nullable Object field) {
        field = null;
    }

    // Preconditions
    @RequiresNonNull("field") // OK
    public void m8() {}

    @RequiresNonNull("param")
    // :: warning: (contracts.precondition.expression.parameter.name)
    public void m9(Object param) {}

    @RequiresNonNull("field")
    // :: warning: (contracts.precondition.expression.parameter.name)
    public void m10(Object field) {}

    // Conditional postconditions
    @EnsuresNonNullIf(result = true, expression = "field") // OK
    public boolean m11() {
        field = new Object();
        return true;
    }

    @EnsuresNonNullIf(result = true, expression = "param")
    // :: warning: (contracts.conditional.postcondition.expression.parameter.name)
    public boolean m12(Object param) {
        param = new Object();
        return true;
    }

    @EnsuresNonNullIf(result = true, expression = "field")
    // :: warning: (contracts.conditional.postcondition.expression.parameter.name)
    public boolean m13a(@Nullable Object field) {
        field = new Object();
        return true;
    }

    @EnsuresNonNullIf(result = true, expression = "field")
    // :: warning: (contracts.conditional.postcondition.expression.parameter.name)
    public boolean m13b(@Nullable Object field) {
        field = new Object();
        return false;
    }

    @EnsuresNonNullIf(result = true, expression = "field")
    // :: warning: (contracts.conditional.postcondition.expression.parameter.name)
    public boolean m13c(@Nullable Object field) {
        field = null;
        // :: error: (contracts.conditional.postcondition.not.satisfied)
        return true;
    }

    @EnsuresNonNullIf(result = true, expression = "field")
    // :: warning: (contracts.conditional.postcondition.expression.parameter.name)
    public boolean m13d(@Nullable Object field) {
        field = null;
        return false;
    }

    // Annotations on formal parameters referring to a formal parameter of the same method.
    // :: error: (expression.unparsable.type.invalid)
    public void m14(@KeyFor("param2") Object param1, Map<Object, Object> param2) {}
}
