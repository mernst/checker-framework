package org.checkerframework.checker.test.junit;

import java.io.File;
import java.util.List;
import org.checkerframework.checker.modifiable.ModifiableChecker;
import org.checkerframework.framework.test.CheckerFrameworkPerDirectoryTest;
import org.junit.runners.Parameterized.Parameters;

public class ModifiableTest extends CheckerFrameworkPerDirectoryTest {

  /**
   * Create a ModifiableTest.
   *
   * @param testFiles the files containing test code, which will be type-checked
   */
  public ModifiableTest(List<File> testFiles) {
    super(testFiles, ModifiableChecker.class, "modifiable", "-Anomsgtext");
  }

  @Parameters
  public static String[] getTestDirs() {
    return new String[] {"modifiable", "all-systems"};
  }
}
