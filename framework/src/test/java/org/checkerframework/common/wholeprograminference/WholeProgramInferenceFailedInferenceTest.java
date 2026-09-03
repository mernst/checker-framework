package org.checkerframework.common.wholeprograminference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the diagnostics that {@code -AshowWpiFailedInferences} produces when whole-program
 * inference has no storage location for a method.
 */
public class WholeProgramInferenceFailedInferenceTest {

  /**
   * A compilation unit that calls an element of an annotation type. The scenes (JAIF) storage has
   * no storage location for an annotation element, so whole-program inference fails for the call.
   */
  private static final String SOURCE =
      String.join(
          System.lineSeparator(),
          "package testpkg;",
          "public class UsesAnno {",
          "  String get(MyAnno a) {",
          "    return a.value();",
          "  }",
          "}",
          "@interface MyAnno {",
          "  String value();",
          "}");

  /**
   * The failure of {@code updateFromMethodInvocation} to find a storage location must be reported,
   * just as the failure of {@code updateFromObjectCreation} is.
   */
  @Test
  public void methodInvocationWithoutStorageLocation() {
    String output = runInference();
    Assert.assertTrue(
        "-AshowWpiFailedInferences did not report the call to MyAnno.value(); output was:"
            + System.lineSeparator()
            + output,
        output.contains(
            "WPI failed to make an inference: WPI could not store information about this method:"
                + " value()Ljava/lang/String;"));
  }

  /**
   * Runs the Value Checker with whole-program inference over {@link #SOURCE} and returns everything
   * that the checker printed to standard output.
   *
   * @return the standard output of the compilation
   */
  private static String runInference() {
    Path directory;
    Path sourceFile;
    try {
      directory = Files.createTempDirectory("wpi-failed-inference");
      sourceFile = directory.resolve("UsesAnno.java");
      Files.write(sourceFile, SOURCE.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    List<String> options =
        Arrays.asList(
            "-processor",
            "org.checkerframework.common.value.ValueChecker",
            "-Ainfer=jaifs",
            "-AinferOutputDirectory=" + directory,
            "-AshowWpiFailedInferences",
            "-Awarns",
            "-ApermitMissingJdk",
            "-d",
            directory.toString(),
            "-classpath",
            System.getProperty("java.class.path"));
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
    PrintStream oldOut = System.out;
    System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      Iterable<? extends JavaFileObject> javaFiles =
          fileManager.getJavaFileObjects(sourceFile.toFile());
      compiler.getTask(null, fileManager, null, options, null, javaFiles).call();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      System.out.flush();
      System.setOut(oldOut);
    }
    return new String(capturedOutput.toByteArray(), StandardCharsets.UTF_8);
  }
}
