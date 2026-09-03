package org.checkerframework.checker.test.junit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.testchecker.nullnesssub.NullnessSubclassChecker;
import org.checkerframework.framework.test.CompilationResult;
import org.checkerframework.framework.test.TestConfiguration;
import org.checkerframework.framework.test.TestConfigurationBuilder;
import org.checkerframework.framework.test.TypecheckExecutor;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests that whole-program inference decides whether to ignore assignments of {@code null} by
 * calling {@code AnnotatedTypeFactory.wpiShouldIgnoreNullAssignments()}, rather than by comparing
 * the simple name of the type factory's class to {@code "NullnessAnnotatedTypeFactory"}. A subclass
 * of the Nullness Checker must record assignments of {@code null}, just as the Nullness Checker
 * itself does.
 */
public class WpiNullnessSubclassTest {

  /** Creates a WpiNullnessSubclassTest. */
  public WpiNullnessSubclassTest() {}

  /** The directory that holds the test input and the whole-program inference output. */
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  /** The test input, which assigns null to a field that is initialized to a non-null value. */
  private static final String TEST_INPUT =
      String.join(
          System.lineSeparator(),
          "public class WpiNullAssignment {",
          "  private String field = \"hello\";",
          "",
          "  void setFieldToNull() {",
          "    this.field = null;",
          "  }",
          "",
          "  String getField() {",
          "    return this.field;",
          "  }",
          "}",
          "");

  @Test
  public void nullAssignmentIsInferredForNullnessSubclass() throws IOException {
    File tempDir = tempFolder.getRoot();
    File sourceFile = new File(tempDir, "WpiNullAssignment.java");
    Files.write(sourceFile.toPath(), TEST_INPUT.getBytes(StandardCharsets.UTF_8));
    File inferOutputDirectory = new File(tempDir, "inference-output");

    List<String> options =
        Arrays.asList("-Ainfer=ajava", "-AinferOutputDirectory=" + inferOutputDirectory, "-Awarns");
    TestConfiguration config =
        TestConfigurationBuilder.buildDefaultConfiguration(
            tempDir.getPath(), sourceFile, NullnessSubclassChecker.class, options, false);
    CompilationResult result = new TypecheckExecutor().compile(config);

    File ajavaFile =
        new File(
            inferOutputDirectory,
            "WpiNullAssignment-" + NullnessSubclassChecker.class.getCanonicalName() + ".ajava");
    Assert.assertTrue(
        "Whole-program inference did not create "
            + ajavaFile
            + "; javac output was:"
            + System.lineSeparator()
            + result.getJavacOutput(),
        ajavaFile.exists());

    String ajavaContents =
        new String(Files.readAllBytes(ajavaFile.toPath()), StandardCharsets.UTF_8);
    Assert.assertTrue(
        "Whole-program inference did not infer @Nullable for a field that is assigned null."
            + "  The contents of "
            + ajavaFile
            + " are:"
            + System.lineSeparator()
            + ajavaContents,
        ajavaContents.contains("Nullable"));
  }
}
