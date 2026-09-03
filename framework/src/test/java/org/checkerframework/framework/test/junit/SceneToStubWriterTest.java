package org.checkerframework.framework.test.junit;

import com.sun.source.util.JavacTask;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.checkerframework.afu.scenelib.el.AClass;
import org.checkerframework.afu.scenelib.el.AScene;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.wholeprograminference.SceneToStubWriter;
import org.checkerframework.common.wholeprograminference.scenelib.ASceneWrapper;
import org.checkerframework.javacutil.BugInCF;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/** Tests for {@link SceneToStubWriter}. */
public class SceneToStubWriterTest {

  /** A concrete checker; {@link SceneToStubWriter} uses only its name. */
  private static class TestChecker extends BaseTypeChecker {
    /** Creates a TestChecker. */
    TestChecker() {}
  }

  /**
   * Returns a scene containing a single printable class named "Foo", whose body is empty.
   *
   * @return a scene containing a single printable class
   */
  private ASceneWrapper sceneWithOnePrintableClass() {
    AScene scene = new AScene();
    AClass aClass = scene.classes.getVivify("Foo");
    aClass.setTypeElement(objectTypeElement());
    return new ASceneWrapper(scene);
  }

  /**
   * Returns the TypeElement for {@code java.lang.Object}, for use as the type element of a class in
   * a scene.
   *
   * @return the TypeElement for {@code java.lang.Object}
   */
  private static TypeElement objectTypeElement() {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    JavacTask task =
        (JavacTask)
            compiler.getTask(null, null, null, null, null, Collections.<JavaFileObject>emptyList());
    return task.getElements().getTypeElement("java.lang.Object");
  }

  /** When the output file cannot be opened, the {@code IOException} is the cause of the error. */
  @Test
  public void unopenableFile() {
    String filename =
        new File(new File("no-such-directory-created-by-SceneToStubWriterTest"), "Foo.astub")
            .getPath();
    try {
      SceneToStubWriter.write(sceneWithOnePrintableClass(), filename, new TestChecker());
      Assert.fail("SceneToStubWriter.write should have thrown BugInCF");
    } catch (BugInCF e) {
      Assert.assertTrue(
          "The IOException should be the cause, but the cause is " + e.getCause(),
          e.getCause() instanceof IOException);
    }
  }

  /**
   * When writing to the output file fails, the error is detected. This test writes to /dev/full,
   * which discards all writes and reports that the device is full; the test is skipped if /dev/full
   * is not available.
   */
  @Test
  public void unwritableFile() {
    File devFull = new File("/dev/full");
    Assume.assumeTrue(devFull.exists() && devFull.canWrite());
    try {
      SceneToStubWriter.write(sceneWithOnePrintableClass(), devFull.getPath(), new TestChecker());
      Assert.fail("SceneToStubWriter.write should have thrown BugInCF");
    } catch (BugInCF e) {
      Assert.assertTrue(
          "Unexpected message: " + e.getMessage(),
          e.getMessage().contains("error writing file during WPI"));
    }
  }

  /**
   * An enum with no enum constants needs no enum constant declaration, so the stub file should
   * contain neither the {@code // enum constants:} header nor the semicolon that terminates the
   * list of enum constants.
   */
  @Test
  public void emptyEnumHasNoEnumConstantDeclaration() throws IOException {
    AScene scene = new AScene();
    AClass aClass = scene.classes.getVivify("Foo");
    aClass.setTypeElement(objectTypeElement());
    aClass.markAsEnum("Foo");
    aClass.setEnumConstants(Collections.emptyList());

    File astub = File.createTempFile("SceneToStubWriterTest", ".astub");
    astub.deleteOnExit();
    SceneToStubWriter.write(new ASceneWrapper(scene), astub.getPath(), new TestChecker());
    String contents = new String(Files.readAllBytes(astub.toPath()), StandardCharsets.UTF_8);

    Assert.assertFalse(
        "The stub file should not mention enum constants, but is:\n" + contents,
        contents.contains("enum constants"));
    Assert.assertFalse(
        "The stub file should not contain an empty enum constant declaration, but is:\n" + contents,
        Pattern.compile("^\\s*;\\s*$", Pattern.MULTILINE).matcher(contents).find());
  }
}
