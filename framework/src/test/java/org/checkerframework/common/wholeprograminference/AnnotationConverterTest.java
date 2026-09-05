package org.checkerframework.common.wholeprograminference;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.checkerframework.afu.scenelib.field.AnnotationFieldType;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link AnnotationConverter}. */
public class AnnotationConverterTest {

  /** The processing environment, used to create type mirrors. */
  private static final ProcessingEnvironment env =
      JavacProcessingEnvironment.instance(new Context());

  /** Creates a new AnnotationConverterTest. */
  public AnnotationConverterTest() {}

  /**
   * An implementation of {@link ArrayType} that is not javac's. Only {@link #getComponentType} and
   * {@link #getKind} are implemented.
   */
  private static class SimpleArrayType implements ArrayType {

    /** The component type of this array type. */
    private final TypeMirror componentType;

    /**
     * Creates a new SimpleArrayType.
     *
     * @param componentType the component type of the array type
     */
    SimpleArrayType(TypeMirror componentType) {
      this.componentType = componentType;
    }

    @Override
    public TypeMirror getComponentType() {
      return componentType;
    }

    @Override
    public TypeKind getKind() {
      return TypeKind.ARRAY;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitArray(this, p);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return componentType + "[]";
    }
  }

  /**
   * Tests that {@link AnnotationConverter#typeMirrorToAnnotationFieldType} works on an array type
   * that is not javac's implementation of {@link ArrayType}.
   */
  @Test
  public void typeMirrorToAnnotationFieldTypeArray() {
    TypeMirror intType = env.getTypeUtils().getPrimitiveType(TypeKind.INT);
    AnnotationFieldType aft =
        AnnotationConverter.typeMirrorToAnnotationFieldType(new SimpleArrayType(intType));
    Assert.assertEquals("int[]", aft.toString());
  }
}
