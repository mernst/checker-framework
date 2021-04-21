package org.checkerframework.checker.test.junit;

import java.io.File;
import java.util.List;
import org.checkerframework.checker.enhancedfor.EnhancedForChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/** Basic tests for the Enhanced For Checker. */
public class EnhancedForTest extends CheckerFrameworkPerDirectoryTest {
  public EnhancedForTest(List<File> testFiles) {
    super(
        testFiles,
        EnhancedForChecker.class,
        "enhancedfor",
        "-Anomsgtext",
        "-nowarn",
        "-encoding",
        "UTF-8");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"enhancedfor"};
  }
}
