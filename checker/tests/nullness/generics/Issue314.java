// Test case for Issue 314:
// https://github.com/typetools/checker-framework/issues/314

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Issue314 {
  <T extends @NonNull Object> List<T> m1(List<@NonNull T> l1) {
    return l1;
  }

  <T> List<T> m2(List<@NonNull T> l1) {
    // :: error: (return)
    return l1;
  }

  class Also<S extends @NonNull Object> {
    S f1;
    @NonNull S f2;

    {
      // :: error: (assignment)
      f1 = f2;
      // :: error: (assignment)
      f2 = f1;
    }
  }
}
