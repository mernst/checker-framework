package org.checkerframework.common.wholeprograminference;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import javax.annotation.processing.ProcessingEnvironment;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.common.wholeprograminference.WholeProgramInferenceJavaParserStorage.CallableDeclarationAnnos;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link WholeProgramInferenceJavaParserStorage}. */
public class WholeProgramInferenceJavaParserStorageTest {

  /** Creates a new WholeProgramInferenceJavaParserStorageTest. */
  public WholeProgramInferenceJavaParserStorageTest() {}

  /** Tests {@link WholeProgramInferenceJavaParserStorage#packageNameToDirectory}. */
  @Test
  public void testPackageNameToDirectory() {
    Assert.assertEquals(
        "org/checkerframework",
        WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org.checkerframework", '/'));
    Assert.assertEquals(
        "org", WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org", '/'));
    // The separator is used literally, even when it is a Windows backslash.
    Assert.assertEquals(
        "org\\checkerframework",
        WholeProgramInferenceJavaParserStorage.packageNameToDirectory(
            "org.checkerframework", '\\'));
    Assert.assertEquals(
        "org", WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org", '\\'));
  }

  /**
   * Tests that {@link CallableDeclarationAnnos#toString} shows every kind of inferred state,
   * including both the preconditions and the postconditions.
   */
  @Test
  public void testCallableDeclarationAnnosToString() {
    MethodDeclaration methodDeclaration =
        StaticJavaParser.parseMethodDeclaration("void aMethod() {}");
    CallableDeclarationAnnos methodAnnos =
        storage.new CallableDeclarationAnnos("testpkg.Outer", methodDeclaration);
    AnnotatedTypeMirror stringType =
        AnnotatedTypeMirror.createType(
            env.getElementUtils().getTypeElement("java.lang.String").asType(), typeFactory, false);
    methodAnnos.getPreconditionsForExpression(
        "testpkg.Outer", "aMethod", "this.aPreconditionField", stringType, typeFactory);
    methodAnnos.getPostconditionsForExpression(
        "testpkg.Outer", "aMethod", "this.aPostconditionField", stringType, typeFactory);

    String methodAnnosString = methodAnnos.toString();
    Assert.assertTrue(methodAnnosString, methodAnnosString.contains("testpkg.Outer.aMethod"));
    Assert.assertTrue(methodAnnosString, methodAnnosString.contains("this.aPreconditionField"));
    Assert.assertTrue(methodAnnosString, methodAnnosString.contains("this.aPostconditionField"));
  }

  /** The processing environment for {@link #typeFactory}. */
  private static final ProcessingEnvironment env;

  /** Creates the annotated types that the tests use. */
  private static final AnnotatedTypeFactory typeFactory;

  /** The storage that owns the {@link CallableDeclarationAnnos} that the tests use. */
  private static final WholeProgramInferenceJavaParserStorage storage;

  static {
    Context context = new Context();
    env = JavacProcessingEnvironment.instance(context);
    JavaCompiler javac = JavaCompiler.instance(context);
    // The list of modules must be initialized before entering symbols.
    javac.initModules(com.sun.tools.javac.util.List.nil());
    javac.enterDone();

    // Any concrete checker would do; ValueChecker is one that the framework tests already depend
    // on.
    ValueChecker checker = new ValueChecker();
    checker.init(env);
    typeFactory = new AnnotatedTypeFactory(checker);
    // The tests never write any file, so the output directory is never created.
    storage = new WholeProgramInferenceJavaParserStorage(typeFactory, "build/wpi-test", false);
  }
}
