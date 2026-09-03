package org.checkerframework.common.wholeprograminference;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.PrimitiveType;
import org.checkerframework.common.wholeprograminference.WholeProgramInferenceJavaParserStorage.FieldAnnos;
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

  /**
   * Tests that {@link FieldAnnos#transferAnnotations} does not throw when the wrapped variable
   * declarator has no parent node.
   */
  @Test
  public void testTransferAnnotationsWithoutParent() {
    VariableDeclarator declaration = new VariableDeclarator(PrimitiveType.intType(), "f");
    Assert.assertFalse(declaration.getParentNode().isPresent());
    new FieldAnnos(declaration).transferAnnotations();
  }
}
