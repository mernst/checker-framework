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
    // On Windows the file name separator is a backslash, which is a metacharacter in the
    // replacement string of String.replaceAll.
    Assert.assertEquals(
        "org\\checkerframework",
        WholeProgramInferenceJavaParserStorage.packageNameToDirectory(
            "org.checkerframework", '\\'));
    Assert.assertEquals(
        "org", WholeProgramInferenceJavaParserStorage.packageNameToDirectory("org", '\\'));
  }

  /** Tests {@link WholeProgramInferenceJavaParserStorage#indexOfLonelySurrogateCharacter}. */
  @Test
  public void testIndexOfLonelySurrogateCharacter() {
    Assert.assertEquals(
        -1, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter(""));
    Assert.assertEquals(
        -1, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("abc"));
    // A well-formed surrogate pair (U+1F600 GRINNING FACE) is not lonely.
    Assert.assertEquals(
        -1,
        WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("a\uD83D\uDE00b"));
    // A high surrogate at the end of the string.
    Assert.assertEquals(
        0, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("\uD800"));
    Assert.assertEquals(
        1, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("a\uD800"));
    // A low surrogate that is not preceded by a high surrogate.
    Assert.assertEquals(
        1, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("a\uDC00b"));
    // A high surrogate that is not followed by a low surrogate.
    Assert.assertEquals(
        0, WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter("\uD800\uD800"));
    // The lonely surrogate follows a well-formed surrogate pair.
    Assert.assertEquals(
        3,
        WholeProgramInferenceJavaParserStorage.indexOfLonelySurrogateCharacter(
            "a\uD83D\uDE00\uD800"));
  }

  /** Tests {@link WholeProgramInferenceJavaParserStorage#escapeLonelySurrogates}. */
  @Test
  public void testEscapeLonelySurrogates() {
    Assert.assertEquals("", WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates(""));
    Assert.assertEquals(
        "abc", WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("abc"));
    // A well-formed surrogate pair is left alone.
    Assert.assertEquals(
        "a\uD83D\uDE00b",
        WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("a\uD83D\uDE00b"));
    Assert.assertEquals(
        "\\uD800", WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("\uD800"));
    Assert.assertEquals(
        "a\\uD800b", WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("a\uD800b"));
    Assert.assertEquals(
        "a\\uDC00b", WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("a\uDC00b"));
    // Every lonely surrogate is escaped, not just the first one.
    Assert.assertEquals(
        "\\uD800\\uDBFF",
        WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("\uD800\uDBFF"));
    Assert.assertEquals(
        "a\\uD800\uD83D\uDE00\\uDC00",
        WholeProgramInferenceJavaParserStorage.escapeLonelySurrogates("a\uD800\uD83D\uDE00\uDC00"));
  }
}
