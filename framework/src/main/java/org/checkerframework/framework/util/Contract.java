package org.checkerframework.framework.util;

import com.sun.source.util.TreePath;
import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.ConditionalPostconditionAnnotation;
import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.framework.qual.EnsuresQualifierIf;
import org.checkerframework.framework.qual.PostconditionAnnotation;
import org.checkerframework.framework.qual.PreconditionAnnotation;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;
import org.checkerframework.framework.util.JavaExpressionParseUtil.JavaExpressionContext;
import org.checkerframework.framework.util.dependenttypes.DependentTypesHelper;
import org.checkerframework.javacutil.BugInCF;

/**
 * A contract represents an annotation on an expression. It is a precondition, postcondition, or
 * conditional postcondition.
 *
 * @see Precondition
 * @see Postcondition
 * @see ConditionalPostcondition
 */
public abstract class Contract {

    /**
     * The expression for which the condition must hold, such as {@code "foo"} in
     * {@code @RequiresNonNull("foo")}.
     *
     * <p>An annotation like {@code @RequiresNonNull({"a", "b", "c"})} would be represented by
     * multiple Contracts.
     */
    public final String expression;

    // It is not possible to standardize this annotation when creating the contract.  Trees.typeOf
    // may return null if a call to a method textually precede the definition of the method and its
    // type.  That can happen when a single compilation unit (= file) defines multiple types at its
    // top level.
    /**
     * The annotation on the type of expression, according to this contract. It is not necessarily
     * standardized.
     */
    public final AnnotationMirror annotation;

    /** The annotation that expressed this contract; used for diagnostic messages. */
    public final AnnotationMirror contractAnnotation;

    // This is redundant with the contract's class and is not used in this file, but the field
    // is used by clients, for its fields.
    /** The kind of contract: precondition, postcondition, or conditional postcondition. */
    public final Kind kind;

    /** Enumerates the kinds of contracts. */
    public enum Kind {
        /** A precondition. */
        PRECONDITION(
                "precondition",
                PreconditionAnnotation.class,
                RequiresQualifier.class,
                RequiresQualifier.List.class,
                "value"),
        /** A postcondition. */
        POSTCONDITION(
                "postcondition",
                PostconditionAnnotation.class,
                EnsuresQualifier.class,
                EnsuresQualifier.List.class,
                "value"),
        /** A conditional postcondition. */
        CONDITIONALPOSTCONDITION(
                "conditional postcondition",
                ConditionalPostconditionAnnotation.class,
                EnsuresQualifierIf.class,
                EnsuresQualifierIf.List.class,
                "expression");

        /** Used for constructing error messages. */
        public final String errorKey;

        /** The meta-annotation identifying annotations of this kind. */
        public final Class<? extends Annotation> metaAnnotation;
        /** The built-in framework qualifier for this contract. */
        public final Class<? extends Annotation> frameworkContractClass;
        /** The built-in framework qualifier for repeated occurrences of this contract. */
        public final Class<? extends Annotation> frameworkContractsClass;
        /**
         * The name of the element that contains the Java expressions on which a contract is
         * enforced.
         */
        public final String expressionElementName;

        /**
         * Create a new Kind.
         *
         * @param errorKey used for constructing error messages
         * @param metaAnnotation the meta-annotation identifying annotations of this kind
         * @param frameworkContractClass the built-in framework qualifier for this contract
         * @param frameworkContractsClass the built-in framework qualifier for repeated occurrences
         *     of this contract
         * @param expressionElementName the name of the element that contains the Java expressions
         *     on which a contract is enforced
         */
        Kind(
                String errorKey,
                Class<? extends Annotation> metaAnnotation,
                Class<? extends Annotation> frameworkContractClass,
                Class<? extends Annotation> frameworkContractsClass,
                String expressionElementName) {
            this.errorKey = errorKey;
            this.metaAnnotation = metaAnnotation;
            this.frameworkContractClass = frameworkContractClass;
            this.frameworkContractsClass = frameworkContractsClass;
            this.expressionElementName = expressionElementName;
        }
    }

    /**
     * Creates a new Contract. This should be called only by the constructors for {@link
     * Precondition}, {@link Postcondition}, and {@link ConditionalPostcondition}.
     *
     * @param kind precondition, postcondition, or conditional postcondition
     * @param expression the Java expression that should have a type qualifier
     * @param annotation the type qualifier that {@code expression} should have
     * @param contractAnnotation the pre- or post-condition annotation that the programmer wrote;
     *     used for diagnostic messages
     */
    private Contract(
            Kind kind,
            String expression,
            AnnotationMirror annotation,
            AnnotationMirror contractAnnotation) {
        this.expression = expression;
        this.annotation = annotation;
        this.contractAnnotation = contractAnnotation;
        this.kind = kind;
    }

    /**
     * Creates a new Contract.
     *
     * @param kind precondition, postcondition, or conditional postcondition
     * @param expression the Java expression that should have a type qualifier
     * @param annotation the type qualifier that {@code expression} should have
     * @param contractAnnotation the pre- or post-condition annotation that the programmer wrote;
     *     used for diagnostic messages
     * @param ensuresQualifierIf the ensuresQualifierIf field, for a conditional postcondition
     * @param atypeFactory used for standardizing annotations
     * @param context used for standardizing annotations
     * @param pathToMethodDecl used for standardizing annotations
     * @return a new contract
     */
    // TODO: This needs to return STANDARDIZED annotations.
    protected static Contract create(
            Kind kind,
            String expression,
            AnnotationMirror annotation,
            AnnotationMirror contractAnnotation,
            Boolean ensuresQualifierIf,
            GenericAnnotatedTypeFactory<?, ?, ?, ?> atypeFactory,
            JavaExpressionContext context,
            TreePath pathToMethodDecl) {
        if ((ensuresQualifierIf != null) != (kind == Kind.CONDITIONALPOSTCONDITION)) {
            throw new BugInCF("Mismatch: ensuresQualifierIf=%s, kind=%s", ensuresQualifierIf, kind);
        }
        System.out.printf(
                "Contract.create(kind=%s, expression=%s, annotation=%s, contractAnnotation=%s, ensuresQualifierIf=%s)%n",
                kind, expression, annotation, contractAnnotation, ensuresQualifierIf);

        // pathToMethodDecl is null if the method is not declared in source code.
        // TODO: The annotations still need to be standardized in that case.  We don't currently
        // have a way to standardize such annotations.
        if (pathToMethodDecl != null) {
            DependentTypesHelper dth = atypeFactory.getDependentTypesHelper();
            if (dth != null) {
                AnnotationMirror standardized =
                        dth.standardizeAnnotationIfDependentType(
                                context, pathToMethodDecl, annotation, UseLocalScope.YES, false);
                if (standardized != null) {
                    annotation = standardized;
                }
            }
        }

        switch (kind) {
            case PRECONDITION:
                return new Precondition(expression, annotation, contractAnnotation);
            case POSTCONDITION:
                return new Postcondition(expression, annotation, contractAnnotation);
            case CONDITIONALPOSTCONDITION:
                return new ConditionalPostcondition(
                        expression, annotation, contractAnnotation, ensuresQualifierIf);
            default:
                throw new BugInCF("Unrecognized kind: " + kind);
        }
    }

    // Note that equality requires exact match of the run-time class and that it ignores the
    // `contractAnnotation` field.
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        Contract otherContract = (Contract) o;

        return kind == otherContract.kind
                && Objects.equals(expression, otherContract.expression)
                && Objects.equals(annotation, otherContract.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, expression, annotation);
    }

    @Override
    public String toString() {
        return String.format(
                "%s{expression=%s, annotation=%s, contractAnnotation=%s}",
                getClass().getSimpleName(), expression, annotation, contractAnnotation);
    }

    /** A precondition contract. */
    public static class Precondition extends Contract {
        /**
         * Create a precondition contract.
         *
         * @param expression the Java expression that should have a type qualifier
         * @param annotation the type qualifier that {@code expression} should have. It is not
         *     necessarily standardized.
         * @param contractAnnotation the precondition annotation that the programmer wrote; used for
         *     diagnostic messages
         */
        public Precondition(
                String expression,
                AnnotationMirror annotation,
                AnnotationMirror contractAnnotation) {
            super(Kind.PRECONDITION, expression, annotation, contractAnnotation);
        }
    }

    /** A postcondition contract. */
    public static class Postcondition extends Contract {
        /**
         * Create a postcondition contract.
         *
         * @param expression the Java expression that should have a type qualifier
         * @param annotation the type qualifier that {@code expression} should have. It is not
         *     necessarily standardized.
         * @param contractAnnotation the postcondition annotation that the programmer wrote; used
         *     for diagnostic messages
         */
        public Postcondition(
                String expression,
                AnnotationMirror annotation,
                AnnotationMirror contractAnnotation) {
            super(Kind.POSTCONDITION, expression, annotation, contractAnnotation);
        }
    }

    /**
     * Represents a conditional postcondition that must be verified by {@code BaseTypeVisitor} or
     * one of its subclasses. Automatically extracted from annotations with meta-annotation
     * {@code @ConditionalPostconditionAnnotation}, such as {@code EnsuresNonNullIf}.
     */
    public static class ConditionalPostcondition extends Contract {

        /**
         * The return value for the annotated method that ensures that the conditional postcondition
         * holds. For example, given
         *
         * <pre>
         * {@code @EnsuresNonNullIf(expression="foo", result=false) boolean method()}
         * </pre>
         *
         * {@code foo} is guaranteed to be {@code @NonNull} after a call to {@code method()} that
         * returns {@code false}.
         */
        public final boolean resultValue;

        /**
         * Create a new conditional postcondition.
         *
         * @param expression the Java expression that should have a type qualifier
         * @param annotation the type qualifier that {@code expression} should have. It is not
         *     necessarily standardized.
         * @param contractAnnotation the postcondition annotation that the programmer wrote; used
         *     for diagnostic messages
         * @param resultValue whether the condition is the method returning true or false
         */
        public ConditionalPostcondition(
                String expression,
                AnnotationMirror annotation,
                AnnotationMirror contractAnnotation,
                boolean resultValue) {
            super(Kind.CONDITIONALPOSTCONDITION, expression, annotation, contractAnnotation);
            this.resultValue = resultValue;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            return super.equals(o) && resultValue == ((ConditionalPostcondition) o).resultValue;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), resultValue);
        }

        @Override
        public String toString() {
            String superToString = super.toString();
            return superToString.substring(0, superToString.length() - 1)
                    + ", annoResult="
                    + resultValue
                    + "}";
        }
    }
}
