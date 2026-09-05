package org.checkerframework.common.wholeprograminference;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.afu.scenelib.field.AnnotationFieldType;
import org.checkerframework.javacutil.BugInCF;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link AnnotationConverter}. */
public class AnnotationConverterTest {

  /** The compilation unit whose annotation elements the tests use. */
  private static final String SOURCE =
      String.join(
          System.lineSeparator(),
          "package testpkg;",
          "public @interface AnAnnotation {",
          "  int intElement();",
          "  String stringElement();",
          "  Class<?> classElement();",
          "  AnEnum enumElement();",
          "  String[] stringArrayElement();",
          "  AnEnum[] enumArrayElement();",
          "  Nested annotationElement();",
          "  Nested[] annotationArrayElement();",
          "  enum AnEnum {",
          "    CONST;",
          "  }",
          "  @interface Nested {",
          "  }",
          "}");

  /** Maps the name of each element of {@code testpkg.AnAnnotation} to its element. */
  private static final Map<String, ExecutableElement> annotationElements =
      annotationElementsOf(SOURCE, "testpkg.AnAnnotation");

  @Test
  public void primitiveElement() {
    assertFieldType("int", "intElement");
  }

  @Test
  public void stringElement() {
    assertFieldType("String", "stringElement");
    assertFieldType("String[]", "stringArrayElement");
  }

  @Test
  public void classElement() {
    assertFieldType("Class", "classElement");
  }

  @Test
  public void enumElement() {
    assertFieldType("enum testpkg.AnAnnotation.AnEnum", "enumElement");
    assertFieldType("enum testpkg.AnAnnotation.AnEnum[]", "enumArrayElement");
  }

  @Test
  public void annotationElementIsRejected() {
    assertFieldTypeThrows("annotationElement");
    assertFieldTypeThrows("annotationArrayElement");
  }

  /**
   * Asserts that {@link AnnotationConverter#getAnnotationFieldType} returns an {@link
   * AnnotationFieldType} whose {@code toString()} is {@code expected}, for the element of {@code
   * testpkg.AnAnnotation} named {@code elementName}.
   *
   * @param expected the expected string representation of the annotation field type
   * @param elementName the name of an element of {@code testpkg.AnAnnotation}
   */
  private void assertFieldType(String expected, String elementName) {
    Assert.assertEquals(
        "field type of " + elementName,
        expected,
        AnnotationConverter.getAnnotationFieldType(annotationElement(elementName)).toString());
  }

  /**
   * Asserts that {@link AnnotationConverter#getAnnotationFieldType} throws {@link BugInCF} for the
   * element of {@code testpkg.AnAnnotation} named {@code elementName}.
   *
   * @param elementName the name of an element of {@code testpkg.AnAnnotation}
   */
  private void assertFieldTypeThrows(String elementName) {
    ExecutableElement element = annotationElement(elementName);
    try {
      AnnotationFieldType aft = AnnotationConverter.getAnnotationFieldType(element);
      Assert.fail("getAnnotationFieldType(" + elementName + ") returned " + aft);
    } catch (BugInCF e) {
      Assert.assertTrue(
          "unexpected message: " + e.getMessage(),
          e.getMessage().contains("testpkg.AnAnnotation.Nested"));
    }
  }

  /**
   * Returns the element of {@code testpkg.AnAnnotation} with the given name.
   *
   * @param elementName the name of an element of {@code testpkg.AnAnnotation}
   * @return the element with the given name
   */
  private ExecutableElement annotationElement(String elementName) {
    ExecutableElement result = annotationElements.get(elementName);
    Assert.assertNotNull("no annotation element named " + elementName, result);
    return result;
  }

  /**
   * Compiles {@code source} and returns the elements of the annotation type named {@code
   * annotationName}, indexed by name.
   *
   * @param source the text of a Java compilation unit
   * @param annotationName the fully-qualified name of an annotation type declared in {@code source}
   * @return the elements of the given annotation type, indexed by name
   */
  private static Map<String, ExecutableElement> annotationElementsOf(
      String source, String annotationName) {
    JavaFileObject fileObject =
        new SimpleJavaFileObject(
            URI.create("string:///testpkg/AnAnnotation.java"), JavaFileObject.Kind.SOURCE) {
          @Override
          public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
          }
        };
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    JavacTask task =
        (JavacTask)
            compiler.getTask(
                null,
                null,
                null,
                Collections.singletonList("-proc:none"),
                null,
                Collections.singletonList(fileObject));
    try {
      Iterable<? extends CompilationUnitTree> compilationUnits = task.parse();
      task.analyze();
      // Referencing compilationUnits ensures that parsing happened.
      Assert.assertTrue("no compilation units", compilationUnits.iterator().hasNext());
    } catch (IOException e) {
      throw new Error("Cannot compile " + source, e);
    }
    TypeElement annotationType = task.getElements().getTypeElement(annotationName);
    if (annotationType == null) {
      throw new Error("Cannot find " + annotationName + " in " + source);
    }
    Map<String, ExecutableElement> result = new LinkedHashMap<>();
    for (ExecutableElement element :
        ElementFilter.methodsIn(annotationType.getEnclosedElements())) {
      result.put(element.getSimpleName().toString(), element);
    }
    return result;
  }
}
