package org.checkerframework.javacutil;

import com.sun.source.tree.Tree;
import java.lang.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.nullness.qual.Nullable;

// This class exists to break a circular dependency between the dataflow framework and
// type-checkers.
/** An implementation of AnnotationProvider returns annotations on Java AST elements. */
public interface AnnotationProvider {

  /**
   * Returns the AnnotationMirror, of the given class or an alias of it, used to annotate the
   * element. Returns null if no annotation equivalent to {@code anno} exists on {@code elt}.
   *
   * @param elt the element
   * @param anno annotation class
   * @return an annotation mirror of class {@code anno} on {@code elt}, or an equivalent one, or
   *     null if none exists on {@code anno}
   */
  @Nullable AnnotationMirror getDeclAnnotation(Element elt, Class<? extends Annotation> anno);

  /**
   * Returns the annotation on {@code tree} that is in the hierarchy that contains the qualifier
   * {@code target}. Returns null if none exists.
   *
   * @param tree the tree of which the annotation is returned
   * @param target the class of the annotation
   * @return the annotation on {@code tree} that has the class {@code target}, or null
   */
  @Nullable AnnotationMirror getAnnotationMirror(Tree tree, Class<? extends Annotation> target);

  /**
   * Returns true if the given method is side-effect-free according to this AnnotationProvider
   * &mdash; that is, if a call to the given method does not undo flow-sensitive type refinement.
   *
   * <p>Note that this method takes account of this AnnotationProvider's semantics, whereas {@code
   * org.checkerframework.dataflow.util.PurityUtils#isSideEffectFree} does not.
   *
   * @param methodElement a method
   * @return true if a call to the method does not undo flow-sensitive type refinement
   */
  boolean isSideEffectFree(ExecutableElement methodElement);

  /**
   * Returns true if the given method is deterministic according to this AnnotationProvider &mdash;
   * that is, if multiple calls to the given method (with the same arguments) return the same value.
   *
   * <p>Note that this method takes account of this AnnotationProvider's semantics, whereas {@code
   * org.checkerframework.dataflow.util.PurityUtils#isDeterministic} does not.
   *
   * @param methodElement a method
   * @return true if multiple calls to the method (with the same arguments) return the same value
   */
  boolean isDeterministic(ExecutableElement methodElement);
}
