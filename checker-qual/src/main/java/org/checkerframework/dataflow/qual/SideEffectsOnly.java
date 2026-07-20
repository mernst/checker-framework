package org.checkerframework.dataflow.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.JavaExpression;

/**
 * A method annotated with the declaration annotation {@code @SideEffectsOnly({"A", "B"})} changes
 * the value of at most the expressions A and B. No other expression is directly modified by the
 * method. Absent aliasing, no other expression has a different value after a call to the method.
 *
 * <p>On a constructor, the annotation does not describe the object being constructed: a constructor
 * may always assign the fields of the object it is constructing, because no other code can observe
 * that object until the constructor returns. The annotation's expressions constrain the
 * constructor's other side effects, such as those on its formal parameters or on static state.
 *
 * <p>Checking of this annotation (under {@code -AcheckPurityAnnotations}) uses an approximate alias
 * analysis: it treats two expressions as possibly aliased only when an assignment relating them
 * appears in the method body, and it never concludes that two expressions have stopped being
 * aliased. Its errors are therefore false negatives: some side effects that the annotation does not
 * permit are accepted.
 *
 * <p>This annotation is inherited by subtypes, just as if it were meta-annotated with
 * {@code @InheritedAnnotation}.
 *
 * @checker_framework.manual #type-refinement-purity Specifying side effects
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface SideEffectsOnly {
  /**
   * An upper bound on the expressions that this method might change the value of.
   *
   * @return the Java expressions that the annotated method might side-effect
   * @checker_framework.manual #java-expressions-as-arguments Syntax of Java expressions
   */
  @JavaExpression
  public String[] value();
}
