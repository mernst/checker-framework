package org.checkerframework.framework.test.junit;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class ValueReadNullnessStringsConcatenationTest extends CheckerFrameworkPerDirectoryTest {

  /** @param testFiles the files containing test code, which will be type-checked */
  public ValueReadNullnessStringsConcatenationTest(List<File> testFiles) {
    super(
        testFiles,
        Arrays.asList(
            org.checkerframework.common.value.ValueChecker.class.getCanonicalName(),
            org.checkerframework.checker.nullness.NullnessChecker.class.getCanonicalName()),
        "value-read-nullness-strings-concatenation",
        Collections.emptyList(),
        "-Anomsgtext",
        "-A" + ValueChecker.REPORT_EVAL_WARNS);
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {
      // Not "all-systems" because This runs two type-checkers and the second one won't run if the
      // first one issues any (expected) warnings.
      // "all-systems",
      "value-read-nullness-strings-concatenation"
    };
  }
}
