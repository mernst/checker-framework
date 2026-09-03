package org.checkerframework.common.wholeprograminference;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.util.Collections;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.afu.scenelib.Annotation;
import org.checkerframework.afu.scenelib.el.AField;
import org.checkerframework.afu.scenelib.el.AnnotationDef;
import org.checkerframework.javacutil.BugInCF;
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

  @Test
  public void stripGenericsRemovesTypeArguments() {
    Assert.assertEquals("int", SceneToStubWriter.stripGenerics("int"));
    Assert.assertEquals("List", SceneToStubWriter.stripGenerics("List<String>"));
    Assert.assertEquals("Map", SceneToStubWriter.stripGenerics("Map<String, List<Integer>>"));
    Assert.assertEquals("Map.Entry", SceneToStubWriter.stripGenerics("Map<K, V>.Entry<K, V>"));
    Assert.assertEquals("List[]", SceneToStubWriter.stripGenerics("List<String>[]"));
  }

  /**
   * A type whose string representation has an unmatched {@code <} indicates a bug elsewhere in the
   * Checker Framework, which must be reported as such rather than as an index out of bounds.
   */
  @Test
  public void stripGenericsOnUnbalancedTypeIsDiagnosable() {
    BugInCF e =
        Assert.assertThrows(BugInCF.class, () -> SceneToStubWriter.stripGenerics("Foo<Bar"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("Foo<Bar"));
  }

  /**
   * The type of a receiver parameter is passed to the formatting routines as a string, so a
   * malformed one reaches {@link SceneToStubWriter#stripGenerics}.
   */
  @Test
  public void formatReceiverParameterWithUnbalancedType() {
    AField param = intParameter();
    Assert.assertThrows(
        BugInCF.class, () -> SceneToStubWriter.formatParameter(param, "this", "Foo<Bar"));
  }
}
