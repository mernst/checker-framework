package org.checkerframework.common.wholeprograminference;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link WholeProgramInferenceJavaParserStorage}. */
public class WholeProgramInferenceJavaParserStorageTest {

  /** Tests {@link WholeProgramInferenceJavaParserStorage#packageNameToDirectory}. */
  @Test
  public void testPackageNameToDirectory() {
    Assert.assertEquals(
        "org/checkerframework",
        WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org.checkerframework", '/'));
    Assert.assertEquals(
        "org", WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org", '/'));
    // The separator character is used literally, even a Windows backslash, which is a
    // metacharacter in some string-replacement routines.
    Assert.assertEquals(
        "org\\checkerframework",
        WholeProgramInferenceJavaParserStorage.packageNameToDirectory(
            "org.checkerframework", '\\'));
    Assert.assertEquals(
        "org", WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org", '\\'));
  }
}
