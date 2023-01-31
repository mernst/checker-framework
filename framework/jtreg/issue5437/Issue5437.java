/*
 * @test
 * @summary Test case for https://github.com/typetools/checker-framework/issues/5437.
 *
 * @compile -XDrawDiagnostics -Xlint:unchecked Issue5437.java -d build
 * @compile -XDrawDiagnostics -Xlint:unchecked -processor value -processorpath ./build Issue5437.java -AreportEvalWarns -d build
 */

import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;

public class Issue5437 {

  public static final int PUBLIC_CONSTANT = 1;
  public static final int PUBLIC_METHOD = increment(2);
  static final int DEFAULT_CONSTANT = 4;
  static final int DEFAULT_METHOD = increment(5);
  protected static final int PROTECTED_CONSTANT = 7;
  protected static final int PROTECTED_METHOD = increment(8);
  private static final int PRIVATE_CONSTANT = 10;
  private static final int PRIVATE_METHOD = increment(11);

  @Pure
  @StaticallyExecutable
  public static final int increment(final int v) {
    return v + 1;
  }

  public static final int client() {
    return PUBLIC_CONSTANT
        + PUBLIC_METHOD
        + DEFAULT_CONSTANT
        + DEFAULT_METHOD
        + PROTECTED_CONSTANT
        + PROTECTED_METHOD
        + PRIVATE_CONSTANT
        + PRIVATE_METHOD;
  }
}
