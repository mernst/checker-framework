package org.checkerframework.common.wholeprograminference;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.afu.scenelib.Annotation;
import org.checkerframework.afu.scenelib.el.AField;
import org.checkerframework.afu.scenelib.el.AnnotationDef;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link SceneToStubWriter}. */
public class SceneToStubWriterTest {

  /** The processing environment, used to create type mirrors. */
  private final ProcessingEnvironment env;

  /**
   * A compilation unit in the default package, in which a type-use annotation that is itself in the
   * default package is written on a type whose printed representation contains spaces.
   */
  private static final String DEFAULT_PACKAGE_SOURCE =
      String.join(
          System.lineSeparator(),
          "import java.lang.annotation.ElementType;",
          "import java.lang.annotation.Target;",
          "@Target(ElementType.TYPE_USE)",
          "@interface DefaultPkgAnno {}",
          "class Outer {",
          "  java.util.@DefaultPkgAnno List<? extends java.lang.CharSequence> aField;",
          "}");

  /** Maps the name of each field in {@link #DEFAULT_PACKAGE_SOURCE} to its type. */
  private static final Map<String, TypeMirror> defaultPackageFieldTypes =
      fieldTypesOf(DEFAULT_PACKAGE_SOURCE);

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
   * An annotation in the default package is not printed, because its name might conflict with an
   * imported annotation name. The rest of the type must still be printed in full: it is not
   * truncated at the last space, which would leave only a fragment of a type whose printed
   * representation contains a space.
   */
  @Test
  public void formatParameterWithDefaultPackageTypeAnnotation() {
    AField param = new AField("x", defaultPackageFieldTypes.get("aField"));
    Assert.assertEquals("java.util.List x", SceneToStubWriter.formatParameter(param, "x", "Outer"));
  }

  /**
   * Compiles {@code source} and returns the type of each field that it declares, indexed by the
   * name of the field. The fields in {@code source} must have distinct names.
   *
   * @param source the text of a Java compilation unit
   * @return the types of the fields declared in {@code source}, indexed by field name
   */
  private static Map<String, TypeMirror> fieldTypesOf(String source) {
    JavaFileObject fileObject =
        new SimpleJavaFileObject(URI.create("string:///Outer.java"), JavaFileObject.Kind.SOURCE) {
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
    Iterable<? extends CompilationUnitTree> compilationUnits;
    try {
      compilationUnits = task.parse();
      // Type annotations are attached to types during attribution, so the source must be analyzed
      // and not merely parsed.
      task.analyze();
    } catch (IOException e) {
      throw new Error("Cannot compile " + source, e);
    }
    Trees trees = Trees.instance(task);
    Map<String, TypeMirror> result = new LinkedHashMap<>();
    TreePathScanner<Void, Void> scanner =
        new TreePathScanner<Void, Void>() {
          @Override
          public Void visitVariable(VariableTree tree, Void p) {
            Element element = trees.getElement(getCurrentPath());
            if (element != null) {
              TypeMirror previous =
                  result.put(element.getSimpleName().toString(), element.asType());
              if (previous != null) {
                throw new Error("Duplicate field name " + element.getSimpleName());
              }
            }
            return super.visitVariable(tree, p);
          }
        };
    for (CompilationUnitTree compilationUnit : compilationUnits) {
      scanner.scan(new TreePath(compilationUnit), null);
    }
    return result;
  }
}
