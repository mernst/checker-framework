package org.checkerframework.checker.test.junit;

import java.io.File;
import java.util.List;
import org.checkerframework.checker.boxing.BoxingChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

/** Basic tests for the Boxing Checker. */
public class BoxingTest extends CheckerFrameworkPerDirectoryTest {
  public BoxingTest(List<File> testFiles) {
    super(testFiles, BoxingChecker.class, "boxing", "-Anomsgtext", "-nowarn", "-encoding", "UTF-8");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"boxing"};
  }
}
