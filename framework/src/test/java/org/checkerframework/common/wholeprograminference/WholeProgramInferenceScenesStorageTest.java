package org.checkerframework.common.wholeprograminference;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.wholeprograminference.WholeProgramInference.OutputFormat;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link WholeProgramInferenceScenesStorage}. */
public class WholeProgramInferenceScenesStorageTest {

  /** The compilation unit whose elements the tests use. */
  private static final String SOURCE =
      String.join(
          System.lineSeparator(),
          "package testpkg;",
          "@Deprecated",
          "public class Outer {",
          "  int aField;",
          "  Outer(int ctorParam) {",
          "    int ctorLocal = ctorParam;",
          "  }",
          "  void aMethod(int methodParam) {",
          "    int methodLocal = methodParam;",
          "  }",
          "  static class Inner {",
          "    void innerMethod() {",
          "      int innerLocal = 0;",
          "    }",
          "  }",
          "  enum AnEnum {",
          "    CONST;",
          "  }",
          "}");

  /** Maps the name of each declaration in {@link #SOURCE} to its element. */
  private static final Map<String, Element> elements = elementsOf(SOURCE);

  /**
   * A checker, needed to construct a {@link WholeProgramInferenceScenesStorage}. Any concrete
   * checker would do; {@code ValueChecker} is one that the framework tests already depend on.
   */
  private static final ValueChecker checker = new ValueChecker();

  /** A type factory, needed to construct a {@link WholeProgramInferenceScenesStorage}. */
  private static final AnnotatedTypeFactory typeFactory;

  static {
    Context context = new Context();
    ProcessingEnvironment env = JavacProcessingEnvironment.instance(context);
    com.sun.tools.javac.main.JavaCompiler javac =
        com.sun.tools.javac.main.JavaCompiler.instance(context);
    // The list of modules must be initialized before entering symbols.
    javac.initModules(com.sun.tools.javac.util.List.nil());
    javac.enterDone();

    checker.init(env);
    typeFactory = new AnnotatedTypeFactory(checker);
  }

  @Test
  public void classes() {
    assertEnclosingClassName("testpkg.Outer", "Outer", ElementKind.CLASS);
    assertEnclosingClassName("testpkg.Outer$Inner", "Inner", ElementKind.CLASS);
  }

  @Test
  public void methodsAndConstructors() {
    assertEnclosingClassName("testpkg.Outer", "Outer.<init>", ElementKind.CONSTRUCTOR);
    assertEnclosingClassName("testpkg.Outer$Inner", "Inner.<init>", ElementKind.CONSTRUCTOR);
    assertEnclosingClassName("testpkg.Outer", "aMethod", ElementKind.METHOD);
    assertEnclosingClassName("testpkg.Outer$Inner", "innerMethod", ElementKind.METHOD);
  }

  @Test
  public void fieldsAndParameters() {
    assertEnclosingClassName("testpkg.Outer", "aField", ElementKind.FIELD);
    assertEnclosingClassName("testpkg.Outer$AnEnum", "CONST", ElementKind.ENUM_CONSTANT);
    assertEnclosingClassName("testpkg.Outer", "ctorParam", ElementKind.PARAMETER);
    assertEnclosingClassName("testpkg.Outer", "methodParam", ElementKind.PARAMETER);
  }

  @Test
  public void localVariables() {
    assertEnclosingClassName("testpkg.Outer", "ctorLocal", ElementKind.LOCAL_VARIABLE);
    assertEnclosingClassName("testpkg.Outer", "methodLocal", ElementKind.LOCAL_VARIABLE);
    assertEnclosingClassName("testpkg.Outer$Inner", "innerLocal", ElementKind.LOCAL_VARIABLE);
  }

  /**
   * A file is written out only if a Scene was created for it. {@code setFileModified} enforces that
   * invariant, on which {@code writeResultsToFile} depends.
   */
  @Test
  public void setFileModifiedForFileWithoutScene() throws IOException {
    Path outputDirectory = Files.createTempDirectory("wpi-no-scene");
    try {
      WholeProgramInferenceScenesStorage storage =
          new WholeProgramInferenceScenesStorage(typeFactory, outputDirectory.toString());
      storage.setFileModified(outputDirectory.resolve("testpkg.Unknown.jaif").toString());
      storage.writeResultsToFile(OutputFormat.JAIF, checker);
      Assert.assertArrayEquals(
          "wrote a file for a class with no Scene", new String[0], outputDirectory.toFile().list());
    } finally {
      deleteRecursively(outputDirectory.toFile());
    }
  }

  /** A file is written out if a Scene was created for it and it was marked as modified. */
  @Test
  public void setFileModifiedForFileWithScene() throws IOException {
    Path outputDirectory = Files.createTempDirectory("wpi-scene");
    try {
      WholeProgramInferenceScenesStorage storage =
          new WholeProgramInferenceScenesStorage(typeFactory, outputDirectory.toString());
      TypeElement outer = (TypeElement) elements.get("Outer");
      Assert.assertNotNull("no element named Outer", outer);
      AnnotationMirror deprecated = outer.getAnnotationMirrors().get(0);
      Assert.assertTrue(
          "Outer is not annotated with @Deprecated",
          storage.addClassDeclarationAnnotation(outer, deprecated));
      storage.setFileModified(storage.getFileForElement(outer));
      storage.writeResultsToFile(OutputFormat.JAIF, checker);
      Assert.assertArrayEquals(
          "did not write the .jaif file for testpkg.Outer",
          new String[] {"testpkg.Outer.jaif"},
          outputDirectory.toFile().list());
    } finally {
      deleteRecursively(outputDirectory.toFile());
    }
  }

  /**
   * Deletes {@code file}, and everything within it if it is a directory.
   *
   * @param file the file or directory to delete
   */
  private static void deleteRecursively(File file) {
    File[] contents = file.listFiles();
    if (contents != null) {
      for (File child : contents) {
        deleteRecursively(child);
      }
    }
    if (!file.delete()) {
      throw new Error("Cannot delete " + file);
    }
  }

  /**
   * Asserts that {@link WholeProgramInferenceScenesStorage#getEnclosingClassName} returns {@code
   * expectedName} for the element of {@link #SOURCE} that is declared with name {@code
   * elementName}.
   *
   * @param expectedName the expected binary name of the enclosing class
   * @param elementName the name of a declaration in {@link #SOURCE}
   * @param expectedKind the expected kind of the element named {@code elementName}
   */
  private void assertEnclosingClassName(
      String expectedName, String elementName, ElementKind expectedKind) {
    Element element = elements.get(elementName);
    Assert.assertNotNull("no element named " + elementName, element);
    Assert.assertEquals("kind of " + elementName, expectedKind, element.getKind());
    Assert.assertEquals(
        "enclosing class of " + elementName,
        expectedName,
        WholeProgramInferenceScenesStorage.getEnclosingClassName(element));
  }

  /**
   * Compiles {@code source} and returns the element of every class, method, and variable that it
   * declares, indexed by the name of the declaration. The declarations in {@code source} must have
   * distinct names.
   *
   * @param source the text of a Java compilation unit
   * @return the elements declared in {@code source}, indexed by name
   */
  private static Map<String, Element> elementsOf(String source) {
    JavaFileObject fileObject =
        new SimpleJavaFileObject(
            // The URI scheme must be "file:", so that ElementUtils.isElementFromSourceCode
            // returns true for the elements of the compilation unit.
            URI.create("file:///testpkg/Outer.java"), JavaFileObject.Kind.SOURCE) {
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
      task.analyze();
    } catch (IOException e) {
      throw new Error("Cannot compile " + source, e);
    }
    Trees trees = Trees.instance(task);
    Map<String, Element> result = new LinkedHashMap<>();
    TreePathScanner<Void, Void> scanner =
        new TreePathScanner<Void, Void>() {
          @Override
          public Void visitClass(ClassTree tree, Void p) {
            recordElement();
            return super.visitClass(tree, p);
          }

          @Override
          public Void visitMethod(MethodTree tree, Void p) {
            recordElement();
            return super.visitMethod(tree, p);
          }

          @Override
          public Void visitVariable(VariableTree tree, Void p) {
            recordElement();
            return super.visitVariable(tree, p);
          }

          /** Adds the element at the current path to {@code result}. */
          private void recordElement() {
            Element element = trees.getElement(getCurrentPath());
            if (element == null) {
              return;
            }
            // Every class has a constructor, so qualify each constructor by its class name.
            String key =
                element.getKind() == ElementKind.CONSTRUCTOR
                    ? element.getEnclosingElement().getSimpleName() + ".<init>"
                    : element.getSimpleName().toString();
            Element previous = result.put(key, element);
            if (previous != null) {
              throw new Error("Duplicate declaration name " + key);
            }
          }
        };
    for (CompilationUnitTree compilationUnit : compilationUnits) {
      scanner.scan(new TreePath(compilationUnit), null);
    }
    return result;
  }
}
