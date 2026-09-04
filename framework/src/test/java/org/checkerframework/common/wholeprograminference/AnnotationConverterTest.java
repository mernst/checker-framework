package org.checkerframework.common.wholeprograminference;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link AnnotationConverter}. */
public class AnnotationConverterTest {

  /** The compilation unit that declares the annotation and the constants used by the tests. */
  private static final String SOURCE =
      String.join(
          System.lineSeparator(),
          "package testpkg;",
          "public class Constants {",
          "  public static final String CONST_A = \"a\";",
          "  public static final String CONST_B = \"b\";",
          "}",
          "@interface MyAnno {",
          "  String[] value();",
          "}");

  /** The processing environment for the compilation of {@link #SOURCE}. */
  private static final ProcessingEnvironment env = processingEnvironmentFor(SOURCE);

  /**
   * A {@code VariableElement[]} is also an {@code Object[]}, so {@link
   * AnnotationConverter#addFieldToAnnotationBuilder} must test for {@code VariableElement[]} first
   * in order to reach {@link AnnotationBuilder#setValue(CharSequence, VariableElement[])}, which
   * (unlike the {@code Object[]} overload) stores the constant value of each element.
   */
  @Test
  public void addVariableElementArrayField() {
    TypeElement constants = env.getElementUtils().getTypeElement("testpkg.Constants");
    Assert.assertNotNull("no element for testpkg.Constants", constants);
    List<VariableElement> fields = ElementFilter.fieldsIn(constants.getEnclosedElements());
    Assert.assertEquals(2, fields.size());

    AnnotationBuilder builder = new AnnotationBuilder(env, "testpkg.MyAnno");
    AnnotationConverter.addFieldToAnnotationBuilder(
        "value", fields.toArray(new VariableElement[0]), builder);
    AnnotationMirror anno = builder.build();

    List<AnnotationValue> elementValues = new ArrayList<>(anno.getElementValues().values());
    Assert.assertEquals(1, elementValues.size());
    Object arrayValue = elementValues.get(0).getValue();
    Assert.assertTrue("not an array value: " + arrayValue, arrayValue instanceof List<?>);
    List<?> arrayElements = (List<?>) arrayValue;
    Assert.assertEquals(2, arrayElements.size());
    Assert.assertEquals("a", ((AnnotationValue) arrayElements.get(0)).getValue());
    Assert.assertEquals("b", ((AnnotationValue) arrayElements.get(1)).getValue());
  }

  /**
   * Compiles {@code source} and returns a processing environment whose element and type utilities
   * know about the declarations in {@code source}.
   *
   * @param source the text of a Java compilation unit that declares {@code testpkg.Constants}
   * @return a processing environment for the compilation of {@code source}
   */
  private static ProcessingEnvironment processingEnvironmentFor(String source) {
    JavaFileObject fileObject =
        new SimpleJavaFileObject(
            URI.create("string:///testpkg/Constants.java"), JavaFileObject.Kind.SOURCE) {
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
      task.parse();
      task.analyze();
    } catch (IOException e) {
      throw new Error("Cannot compile " + source, e);
    }
    return JavacProcessingEnvironment.instance(((BasicJavacTask) task).getContext());
  }
}
