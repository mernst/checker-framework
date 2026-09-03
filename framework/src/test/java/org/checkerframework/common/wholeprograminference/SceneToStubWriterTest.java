package org.checkerframework.common.wholeprograminference;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.util.Collections;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.afu.scenelib.Annotation;
import org.checkerframework.afu.scenelib.el.AField;
import org.checkerframework.afu.scenelib.el.AMethod;
import org.checkerframework.afu.scenelib.el.AScene;
import org.checkerframework.afu.scenelib.el.AnnotationDef;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link SceneToStubWriter}. */
public class SceneToStubWriterTest {

  /** The processing environment, used to create type mirrors. */
  private final ProcessingEnvironment env;

  /** Creates a new SceneToStubWriterTest. */
  public SceneToStubWriterTest() {
    env = JavacProcessingEnvironment.instance(new Context());
  }

  /**
   * Returns an annotation with the given binary name and no elements (fields).
   *
   * @param binaryName the binary name of the annotation type
   * @return a scene-lib annotation with the given name
   */
  private static Annotation markerAnnotation(String binaryName) {
    AnnotationDef def = new AnnotationDef(binaryName, "SceneToStubWriterTest");
    def.setFieldTypes(Collections.emptyMap());
    return new Annotation(def, Collections.emptyMap());
  }

  /**
   * Returns an AField named "x" whose type is {@code int} and that has the given declaration
   * annotations.
   *
   * @param declAnnoNames the binary names of the declaration annotations on the formal parameter
   * @return the formal parameter
   */
  private AField intParameter(String... declAnnoNames) {
    TypeMirror intType = env.getTypeUtils().getPrimitiveType(TypeKind.INT);
    AField param = new AField("x", intType);
    for (String declAnnoName : declAnnoNames) {
      param.tlAnnotationsHere.add(markerAnnotation(declAnnoName));
    }
    return param;
  }

  @Test
  public void formatParameterWithoutDeclarationAnnotation() {
    AField param = intParameter();
    Assert.assertEquals("int x", SceneToStubWriter.formatParameter(param, "x", "MyClass"));
  }

  /**
   * A declaration annotation on a formal parameter must be separated from the parameter's type by a
   * space; otherwise the stub file cannot be parsed.
   */
  @Test
  public void formatParameterWithDeclarationAnnotation() {
    AField param = intParameter("org.checkerframework.checker.mustcall.qual.Owning");
    Assert.assertEquals("@Owning int x", SceneToStubWriter.formatParameter(param, "x", "MyClass"));
  }

  @Test
  public void formatParameterWithTwoDeclarationAnnotations() {
    AField param =
        intParameter("org.checkerframework.checker.mustcall.qual.Owning", "java.lang.Deprecated");
    Assert.assertEquals(
        "@Owning @Deprecated int x", SceneToStubWriter.formatParameter(param, "x", "MyClass"));
  }

  /**
   * Returns a method with three formal parameters of type {@code int}, named "x0", "x1", and "x2".
   * The parameters are vivified in the given order, which need not be index order.
   *
   * @param vivificationOrder the indices of the formal parameters, in the order in which to vivify
   *     them
   * @return a method with three formal parameters
   */
  private AMethod methodWithThreeIntParameters(int... vivificationOrder) {
    AMethod aMethod = new AScene().classes.getVivify("MyClass").methods.getVivify("myMethod(III)V");
    TypeMirror intType = env.getTypeUtils().getPrimitiveType(TypeKind.INT);
    for (int index : vivificationOrder) {
      aMethod.vivifyAndAddTypeMirrorToParameter(
          index, intType, env.getElementUtils().getName("x" + index));
    }
    return aMethod;
  }

  @Test
  public void formatParametersVivifiedInIndexOrder() {
    Assert.assertEquals(
        "int x0, int x1, int x2",
        SceneToStubWriter.formatParameters(methodWithThreeIntParameters(0, 1, 2), "MyClass"));
  }

  /**
   * Formal parameters must be printed in index order. The iteration order of {@code
   * AMethod.getParameters()} is insertion order, which is not necessarily index order: reading a
   * pre-existing annotation file can vivify the formal parameters in any order.
   */
  @Test
  public void formatParametersVivifiedOutOfIndexOrder() {
    Assert.assertEquals(
        "int x0, int x1, int x2",
        SceneToStubWriter.formatParameters(methodWithThreeIntParameters(2, 0, 1), "MyClass"));
  }
}
