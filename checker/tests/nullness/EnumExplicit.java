// Test case for https://github.com/typetools/checker-framework/issues/5165

import org.checkerframework.checker.nullness.qual.Nullable;

public class EnumExplicit {

  public static void client() {
    EnumWithMethod.VALUE.call(null);
  }
}

enum EnumWithMethod {
  VALUE {
    @Override
    public void call(@Nullable String string) {
      // Null string is acceptable in this function.
      System.out.printf("Null string is acceptable in this function.");
    }
  };

  public abstract void call(String string);
}
