package org.checkerframework.common.wholeprograminference;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.afu.scenelib.Annotation;
import org.checkerframework.afu.scenelib.el.AField;
import org.checkerframework.afu.scenelib.el.AnnotationDef;
import org.checkerframework.afu.scenelib.field.AnnotationFieldType;
import org.checkerframework.afu.scenelib.field.BasicAFT;
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
   * Returns an annotation with the given binary name and the given elements (fields).
   *
   * @param binaryName the binary name of the annotation type
   * @param fieldTypes the types of the annotation's elements
   * @param fieldValues the values of the annotation's elements
   * @return a scene-lib annotation with the given name and elements
   */
  private static Annotation annotation(
      String binaryName,
      Map<String, AnnotationFieldType> fieldTypes,
      Map<String, Object> fieldValues) {
    AnnotationDef def = new AnnotationDef(binaryName, "SceneToStubWriterTest");
    def.setFieldTypes(fieldTypes);
    return new Annotation(def, fieldValues);
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
  public void formatAnnotationWithoutElements() {
    Assert.assertEquals(
        "@Owning",
        SceneToStubWriter.formatAnnotation(
            markerAnnotation("org.checkerframework.checker.mustcall.qual.Owning")));
  }

  /** An annotation with a sole element named "value" is printed without the element name. */
  @Test
  public void formatAnnotationWithValueElement() {
    Map<String, AnnotationFieldType> fieldTypes = new LinkedHashMap<>(1);
    fieldTypes.put("value", BasicAFT.forType(String.class));
    Map<String, Object> fieldValues = new LinkedHashMap<>(1);
    fieldValues.put("value", "hello");
    Assert.assertEquals(
        "@MyAnno(\"hello\")",
        SceneToStubWriter.formatAnnotation(annotation("MyAnno", fieldTypes, fieldValues)));
  }

  /** An annotation with multiple elements is printed with each element name. */
  @Test
  public void formatAnnotationWithMultipleElements() {
    Map<String, AnnotationFieldType> fieldTypes = new LinkedHashMap<>(2);
    fieldTypes.put("value", BasicAFT.forType(String.class));
    fieldTypes.put("count", BasicAFT.forType(int.class));
    Map<String, Object> fieldValues = new LinkedHashMap<>(2);
    fieldValues.put("value", "hello");
    fieldValues.put("count", 22);
    Assert.assertEquals(
        "@MyAnno(value=\"hello\", count=22)",
        SceneToStubWriter.formatAnnotation(annotation("MyAnno", fieldTypes, fieldValues)));
  }
}
