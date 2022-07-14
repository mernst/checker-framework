// Test case for https://github.com/typetools/checker-framework/issues/5165
// Compared to EnumExplicit.java, the class definitions are in the opposite order.

import org.checkerframework.checker.nullness.qual.Nullable;

enum EnumWithMethod2 {
  VALUE {
    @Override
    public void call(@Nullable String string) {
      // Null string is acceptable in this function.
      System.out.printf("Null string is acceptable in this function.");
    }
  };

  public abstract void call(String string);
}

public class EnumExplicit2 {

  public static void client() {
    EnumWithMethod2.VALUE.call(null);
  }
}
